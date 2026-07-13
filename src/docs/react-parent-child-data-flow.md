# React 부모 ↔ 자식 데이터 흐름 정리

Routinary의 `Home` → `RoutinePanel` 구조를 기준으로 정리한 문서.

---

## 핵심 원칙 3가지

1. **데이터는 위에서 아래로 (props로 내려간다)**
   부모가 state를 소유하고, 자식은 그 값을 props로 받아 화면에 그리기만 한다.

2. **이벤트는 아래에서 위로 (콜백 함수로 올라간다)**
   자식은 직접 state를 바꾸지 않는다. 부모가 내려준 함수를 호출해서 "이런 일이 일어났어요"라고 알린다.

3. **state는 그 값을 필요로 하는 컴포넌트들의 가장 가까운 공통 부모에 둔다** (Lift State Up)
   히어로 링과 RoutinePanel이 둘 다 routines를 쓰므로, 공통 부모인 `Home`이 소유한다.

---

## 방향 1 — 부모 → 자식 (Props Down)

부모의 값을 자식에게 넘길 때.

```jsx
// 부모 (Home)
<RoutinePanel routines={routines} onToggle={toggleRoutine} />

// 자식 (RoutinePanel)
function RoutinePanel({ routines, onToggle }) {
  return routines.map(r => <div key={r.id}>{r.title}</div>);
}
```

- `routines` (데이터)와 `onToggle` (함수) 둘 다 props로 내려간다.
- 자식은 받은 props를 **읽기만** 한다. 자식이 props를 직접 수정하려 하면 안 된다.

---

## 방향 2 — 자식 → 부모 (Events Up)

자식에서 벌어진 일을 부모에게 알릴 때. **부모가 함수를 내려주고, 자식이 그 함수를 호출**한다.

```jsx
// 부모 (Home): 함수를 정의하고 자식에게 내려줌
const toggleRoutine = async (id) => {
  await axios.post(`/api/routines/${id}/toggle`);
  setRoutines(prev =>
    prev.map(r => (r.id === id ? { ...r, done: !r.done } : r))
  );
};

// 자식 (RoutinePanel): 클릭 시 부모 함수를 호출 (id를 실어서)
<button onClick={() => onToggle(r.id)}>
  {r.done ? "✓" : "○"}
</button>
```

- 자식은 `setRoutines`의 존재조차 모른다. 그냥 `onToggle(id)`만 부른다.
- state를 실제로 바꾸는 건 부모다. → state 소유권이 한 곳에 모여 동기화 버그가 안 생긴다.

---

## 전체 흐름도

```
┌─────────────────────────────────────────────────────────────┐
│  Home  (routines state 소유)                                 │
│                                                              │
│   const [routines, setRoutines] = useState([]);             │
│                                                              │
│   ┌─ 파생값 (routines에서 매 렌더마다 재계산) ─────────┐      │
│   │  done    = routines.filter(r => r.done).length     │      │
│   │  total   = routines.length                         │      │
│   │  percent = total ? done / total : 0                │      │
│   └────────────────────────────────────────────────────┘      │
│                                                              │
│   const toggleRoutine = (id) => { ... setRoutines(...) }    │
│                                                              │
│        │ percent                     │ routines            │
│        │ (링에 사용)                  │ onToggle={toggleRoutine}
│        ▼                             ▼                       │
│   ┌──────────┐              ┌──────────────────┐            │
│   │ 히어로 링 │              │  RoutinePanel     │            │
│   │  (SVG)   │              │  (props로 받음)   │            │
│   └──────────┘              └──────────────────┘            │
│                                     │                        │
└─────────────────────────────────────┼────────────────────────┘
                                      │ onClick → onToggle(id)
                                      ▼
                          ┌────────────────────────┐
                          │ 사용자가 done 버튼 클릭 │
                          └────────────────────────┘
```

---

## 클릭 한 번에 벌어지는 일 (시간 순서)

```
1. 사용자가 RoutinePanel의 done 버튼 클릭
         │
2.  onClick={() => onToggle(r.id)}  실행
         │   (자식 → 부모로 id 전달)
         ▼
3.  Home의 toggleRoutine(id) 실행
         │
4.  await axios.post(.../toggle)   → 서버가 RoutineLog INSERT/DELETE
         │
5.  setRoutines(prev => ...)       → routines 배열에서 해당 항목 done 뒤집기
         │
6.  routines 바뀜 → Home 리렌더
         │
7.  리렌더 중 done / total / percent 자동 재계산
         │
         ├──▶ 히어로 링: 새 percent로 다시 그려짐
         └──▶ RoutinePanel: 새 routines를 props로 다시 받아 다시 그려짐
```

한 번의 클릭이 **링과 패널을 동시에** 갱신하는 이유:
둘 다 같은 `routines` state 하나에서 나오기 때문. 데이터 소스가 하나라 서로 어긋날 수 없다.

---

## 주의점

- **자식이 자체 fetch를 하고 있으면 지운다.**
  `RoutinePanel`이 자기 안에서 `useState` + `axios.get`으로 routines를 또 들고 있으면,
  데이터 소스가 두 개가 되어 링과 리스트가 다른 값을 본다. fetch는 부모(`Home`) 한 곳에서만.

- **`done`을 별도 state로 저장하지 않는다.**
  `done`은 `routines`에서 `filter`로 파생시킨다. 개수를 따로 `useState`로 들면
  `routines`와 어긋나 동기화 버그(예: undefined 크래시)가 재발할 수 있다.

- **props로 내려온 값은 자식에서 직접 수정하지 않는다.**
  바꾸고 싶으면 부모가 내려준 콜백(`onToggle`)을 호출해서 부모가 바꾸게 한다.

---

## 한 줄 요약

> **데이터는 props로 내려가고, 이벤트는 콜백으로 올라간다.**
> state는 그 데이터를 함께 쓰는 컴포넌트들의 공통 부모가 소유한다.
