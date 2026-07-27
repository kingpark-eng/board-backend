# Routinary — 추가 기능 구현 가이드

> streak 이후에 붙일 기능들의 소스 + 힌트 모음.
> 우선순위 순으로 정렬했고, 각 항목은 독립적으로 붙일 수 있게 썼다.

**진행 체크리스트**

- [x] 01. 연속 달성일 (streak)
- [ ] 02. 메모 상세 / 수정
- [ ] 03. 삭제 실행 취소 (Undo 토스트)
- [ ] 04. 빈 상태 (Empty state)
- [ ] 05. 로딩 스켈레톤
- [ ] 06. 완료 항목 아래로 정렬
- [ ] 07. 드래그 정렬 (sortOrder)
- [ ] 08. 요일별 루틴 (repeatDays)
- [ ] 09. 반응형
- [ ] 10. 다크 / 라이트 토글

---

## 02. 메모 상세 / 수정

가장 우선순위 높음. 지금은 제목만 보이고 클릭이 안 돼서 기능이 반쪽이다.

### 백엔드

이미 게시판 프로젝트에서 만든 패턴 그대로다. `PostController`에 상세 조회 / 수정만 추가.

```java
@GetMapping("/{id}")
public ResponseEntity<PostResponse> getPost(
        @PathVariable Long id,
        @AuthenticationPrincipal UserDetails userDetails) {
    return ResponseEntity.ok(postService.getPost(id, userDetails));
}

@PutMapping("/{id}")
public ResponseEntity<PostResponse> updatePost(
        @PathVariable Long id,
        @RequestBody PostRequest req,
        @AuthenticationPrincipal UserDetails userDetails) {
    return ResponseEntity.ok(postService.update(id, req, userDetails));
}
```

```java
// PostService
@Transactional
public PostResponse update(Long id, PostRequest req, UserDetails userDetails) {
    Post post = postRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("게시글 없음"));

    User user = userRepository.findByEmail(userDetails.getUsername())
            .orElseThrow(() -> new IllegalArgumentException("유저 없음"));

    // 작성자 본인 확인 — 이거 빠뜨리면 남의 글도 수정됨
    if (!post.getUser().getId().equals(user.getId())) {
        throw new AccessDeniedException("작성자만 수정할 수 있습니다");
    }

    post.update(req.getTitle(), req.getContent());  // 엔티티에 update 메서드
    return PostResponse.from(post);                 // 더티 체킹으로 자동 UPDATE
}
```

> **힌트 — 더티 체킹**
> `@Transactional` 안에서 조회한 엔티티는 영속 상태다.
> 필드를 바꾸면 트랜잭션 커밋 시점에 JPA가 알아서 UPDATE 쿼리를 날린다.
> `postRepository.save(post)`를 호출할 필요가 없다.

```java
// Post 엔티티에 추가
public void update(String title, String content) {
    this.title = title;
    this.content = content;
}
```

> **힌트 — setter 대신 의미 있는 메서드**
> `setTitle()`, `setContent()`를 열어두는 것보다 `update()` 하나로 묶는 게 낫다.
> 엔티티가 언제 어떻게 바뀌는지 추적하기 쉬워진다.

### 프론트

모달로 처리하는 게 가장 간단하다. 라우팅 추가 없이 `PostPanel` 안에서 끝난다.

```jsx
// PostPanel.jsx
const [selected, setSelected] = useState(null);   // 열린 메모
const [editMode, setEditMode] = useState(false);

const openPost = async (id) => {
  const res = await getPost(id);
  setSelected(res.data);
  setEditMode(false);
};

const saveEdit = async () => {
  const res = await updatePost(selected.id, {
    title: selected.title,
    content: selected.content,
  });
  setPosts(prev => prev.map(p => (p.id === res.data.id ? res.data : p)));
  setEditMode(false);
};
```

```jsx
{/* 목록의 각 행을 클릭 가능하게 */}
<div className={styles.postRow} onClick={() => openPost(p.id)}>
  <span className={styles.postTitle}>{p.title}</span>
  <button
    className={styles.deleteBtn}
    onClick={(e) => { e.stopPropagation(); del(p.id); }}
  >
    삭제
  </button>
</div>
```

> **힌트 — `e.stopPropagation()`**
> 삭제 버튼이 행 안에 있으면 클릭이 부모의 `onClick`까지 올라간다(이벤트 버블링).
> 삭제 누르면 모달이 같이 열리는 버그가 여기서 나온다.

```jsx
{/* 모달 */}
{selected && (
  <div className={styles.modalBackdrop} onClick={() => setSelected(null)}>
    <div className={styles.modal} onClick={(e) => e.stopPropagation()}>
      {editMode ? (
        <>
          <input
            className={styles.modalTitleInput}
            value={selected.title}
            onChange={(e) => setSelected({ ...selected, title: e.target.value })}
          />
          <textarea
            className={styles.modalBody}
            value={selected.content}
            onChange={(e) => setSelected({ ...selected, content: e.target.value })}
          />
          <div className={styles.modalActions}>
            <button className={styles.writeCancel} onClick={() => setEditMode(false)}>
              취소
            </button>
            <button className={styles.writeSubmit} onClick={saveEdit}>
              저장
            </button>
          </div>
        </>
      ) : (
        <>
          <h3 className={styles.modalTitle}>{selected.title}</h3>
          <p className={styles.modalContent}>{selected.content}</p>
          <div className={styles.modalActions}>
            <button className={styles.writeCancel} onClick={() => setSelected(null)}>
              닫기
            </button>
            <button className={styles.writeSubmit} onClick={() => setEditMode(true)}>
              수정
            </button>
          </div>
        </>
      )}
    </div>
  </div>
)}
```

> **힌트 — backdrop 클릭으로 닫기**
> 바깥 div에 `onClick={닫기}`, 안쪽 모달에 `onClick={e => e.stopPropagation()}`.
> 이 조합이 "바깥 클릭하면 닫힘" 패턴의 정석이다.

ESC로 닫기도 넣으면 완성도가 올라간다.

```jsx
useEffect(() => {
  if (!selected) return;
  const onKey = (e) => { if (e.key === "Escape") setSelected(null); };
  window.addEventListener("keydown", onKey);
  return () => window.removeEventListener("keydown", onKey);
}, [selected]);
```

> **힌트 — cleanup 함수**
> `useEffect`가 반환하는 함수는 언마운트 시점 + 다음 실행 직전에 호출된다.
> 이벤트 리스너를 안 지우면 모달을 열고 닫을 때마다 리스너가 쌓인다.

### CSS

```css
.modalBackdrop {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.6);
  backdrop-filter: blur(3px);
  display: grid;
  place-items: center;
  z-index: 200;
  animation: fadeIn 0.16s ease;
}
.modal {
  width: min(520px, calc(100vw - 40px));
  max-height: 80vh;
  overflow-y: auto;
  padding: 26px 28px;
  background: var(--surface);
  border: 1px solid var(--border);
  border-radius: 16px;
  animation: modalIn 0.2s cubic-bezier(0.34, 1.4, 0.64, 1);
}
.modalTitle { font-size: 18px; font-weight: 700; margin-bottom: 14px; }
.modalContent {
  font-size: 14px;
  line-height: 1.8;
  color: var(--text2);
  white-space: pre-wrap;
  min-height: 80px;
}
.modalTitleInput {
  width: 100%;
  padding: 10px 12px;
  margin-bottom: 12px;
  background: var(--bg);
  border: 1px solid var(--border);
  border-radius: 8px;
  color: var(--text);
  font-size: 16px;
  font-weight: 600;
  font-family: inherit;
  outline: none;
}
.modalBody {
  width: 100%;
  min-height: 160px;
  padding: 12px;
  background: var(--bg);
  border: 1px solid var(--border);
  border-radius: 8px;
  color: var(--text);
  font-size: 14px;
  line-height: 1.7;
  font-family: inherit;
  resize: vertical;
  outline: none;
}
.modalTitleInput:focus, .modalBody:focus { border-color: var(--accent); }
.modalActions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  margin-top: 18px;
}

@keyframes fadeIn { from { opacity: 0; } to { opacity: 1; } }
@keyframes modalIn {
  from { opacity: 0; transform: translateY(12px) scale(0.97); }
  to   { opacity: 1; transform: translateY(0) scale(1); }
}
```

> **힌트 — `white-space: pre-wrap`**
> 사용자가 입력한 줄바꿈(`\n`)을 그대로 보여준다.
> 이거 없으면 여러 줄로 쓴 메모가 한 줄로 붙어서 나온다.

---

## 03. 삭제 실행 취소 (Undo 토스트)

`confirm()`보다 UX가 좋다. 삭제를 낙관적으로 처리하고, 5초 안에 되돌릴 기회를 준다.

### 훅으로 분리

```jsx
// hooks/useUndoToast.js
import { useState, useRef, useCallback, useEffect } from "react";

export default function useUndoToast(delay = 5000) {
  const [toast, setToast] = useState(null);   // { message, onUndo, onCommit }
  const timerRef = useRef(null);

  const clear = useCallback(() => {
    clearTimeout(timerRef.current);
    timerRef.current = null;
  }, []);

  // { message, onUndo, onCommit } 형태로 호출
  const show = useCallback(({ message, onUndo, onCommit }) => {
    clear();
    setToast({ message, onUndo, onCommit });
    timerRef.current = setTimeout(() => {
      onCommit();          // 시간 지나면 실제 삭제 확정
      setToast(null);
    }, delay);
  }, [clear, delay]);

  const undo = useCallback(() => {
    clear();
    toast?.onUndo();
    setToast(null);
  }, [toast, clear]);

  useEffect(() => clear, [clear]);   // 언마운트 시 타이머 정리

  return { toast, show, undo };
}
```

> **힌트 — `useRef`로 타이머 보관**
> `useState`에 타이머 ID를 담으면 값이 바뀔 때마다 리렌더가 발생한다.
> `useRef`는 값이 바뀌어도 리렌더를 유발하지 않아서 이런 용도에 맞다.

> **힌트 — `useCallback`**
> 함수를 매 렌더마다 새로 만들지 않게 메모이제이션한다.
> 이 함수를 `useEffect`의 의존성 배열에 넣을 때 무한 루프를 막아준다.

### 사용

```jsx
// Home.jsx
const { toast, show, undo } = useUndoToast();

const del = (id) => {
  const target = routines.find(r => r.id === id);
  const index  = routines.findIndex(r => r.id === id);

  // 1. 화면에서 먼저 제거 (낙관적 업데이트)
  setRoutines(prev => prev.filter(r => r.id !== id));

  // 2. 토스트 표시
  show({
    message: `'${target.title}' 삭제됨`,
    onUndo: () => {
      // 원래 위치로 되돌리기
      setRoutines(prev => {
        const next = [...prev];
        next.splice(index, 0, target);
        return next;
      });
    },
    onCommit: () => deleteRoutine(id),   // 여기서 실제 API 호출
  });
};
```

> **힌트 — 낙관적 업데이트(Optimistic Update)**
> 서버 응답을 기다리지 않고 UI를 먼저 바꾸는 방식.
> 체감 속도가 훨씬 빠르다. 대신 실패했을 때 되돌리는 처리가 필요하다.
> 여기서는 아예 5초 뒤에 API를 부르므로 실패 처리 자체를 미룰 수 있다.

> **힌트 — `splice`로 원위치 복원**
> `filter`로 지운 뒤 `push`로 되살리면 항목이 맨 뒤로 간다.
> 삭제 전 인덱스를 기억했다가 `splice(index, 0, item)`으로 끼워 넣어야 자연스럽다.

### 컴포넌트 + CSS

```jsx
{toast && (
  <div className={styles.toast} role="status">
    <span>{toast.message}</span>
    <button className={styles.toastBtn} onClick={undo}>실행 취소</button>
  </div>
)}
```

```css
.toast {
  position: fixed;
  bottom: 28px;
  left: 50%;
  transform: translateX(-50%);
  display: flex;
  align-items: center;
  gap: 18px;
  padding: 13px 16px 13px 20px;
  background: var(--surface2);
  border: 1px solid var(--border);
  border-radius: 10px;
  box-shadow: 0 8px 28px rgba(0, 0, 0, 0.5);
  font-size: 13px;
  z-index: 300;
  animation: toastIn 0.24s cubic-bezier(0.4, 0, 0.2, 1);
}
.toastBtn {
  background: none;
  border: none;
  color: var(--accent);
  font-size: 13px;
  font-weight: 600;
  font-family: inherit;
  cursor: pointer;
  padding: 2px 4px;
}
.toastBtn:hover { text-decoration: underline; }

/* 남은 시간 표시 바 */
.toast::after {
  content: '';
  position: absolute;
  bottom: 0; left: 0;
  height: 2px;
  background: var(--accent);
  border-radius: 0 0 10px 10px;
  animation: countdown 5s linear forwards;
}
@keyframes countdown { from { width: 100%; } to { width: 0; } }
@keyframes toastIn {
  from { opacity: 0; transform: translate(-50%, 14px); }
  to   { opacity: 1; transform: translate(-50%, 0); }
}
```

> **힌트 — `role="status"`**
> 스크린 리더가 이 영역의 변화를 자동으로 읽어준다.
> 토스트처럼 갑자기 나타나는 알림에 붙이면 접근성이 올라간다.

---

## 04. 빈 상태 (Empty state)

루틴 0개일 때 화면이 비어 있으면 앱이 고장 난 것처럼 보인다.

```jsx
// RoutinePanel.jsx
{!loading && routines.length === 0 ? (
  <div className={styles.emptyState}>
    <div className={styles.emptyIcon}>◇</div>
    <p className={styles.emptyTitle}>아직 루틴이 없어요</p>
    <p className={styles.emptyDesc}>
      아래에서 첫 번째 루틴을 추가해보세요
    </p>
  </div>
) : (
  <div className={styles.routineList}>
    {routines.map(r => <RoutineItem key={r.id} ... />)}
  </div>
)}
```

```css
.emptyState {
  padding: 44px 20px;
  text-align: center;
  border: 1px dashed var(--border);
  border-radius: 12px;
}
.emptyIcon {
  font-size: 26px;
  color: var(--text2);
  opacity: 0.4;
  margin-bottom: 12px;
}
.emptyTitle {
  font-size: 14px;
  color: var(--text);
  font-weight: 500;
  margin-bottom: 5px;
}
.emptyDesc { font-size: 12px; color: var(--text2); }
```

> **힌트 — `!loading &&` 조건이 중요**
> 이게 없으면 로딩 중에 "루틴이 없어요"가 잠깐 번쩍이고 사라진다.
> 빈 상태는 "로딩이 끝났는데 데이터가 0개"일 때만 보여야 한다.

메모 패널에도 같은 패턴을 적용한다. 검색 결과가 없을 때는 문구를 다르게:

```jsx
<p className={styles.emptyTitle}>
  {keyword ? `'${keyword}' 검색 결과가 없어요` : "아직 메모가 없어요"}
</p>
```

---

## 05. 로딩 스켈레톤

`loading...` 텍스트보다 실제 레이아웃 모양의 회색 박스가 체감 속도가 빠르다.

```jsx
{loading ? (
  <div className={styles.routineList}>
    {Array.from({ length: 4 }).map((_, i) => (
      <div key={i} className={styles.skeleton} />
    ))}
  </div>
) : ( /* 실제 목록 */ )}
```

> **힌트 — `Array.from({ length: n })`**
> 길이만 있는 빈 배열을 만드는 관용구.
> `[...Array(4)]`도 같은 결과다. 인덱스만 필요하니 `key={i}`로 충분하다.

```css
.skeleton {
  height: 52px;
  border-radius: 12px;
  background: linear-gradient(
    90deg,
    var(--surface) 25%,
    var(--surface2) 50%,
    var(--surface) 75%
  );
  background-size: 200% 100%;
  animation: shimmer 1.4s linear infinite;
}
@keyframes shimmer {
  from { background-position: 200% 0; }
  to   { background-position: -200% 0; }
}
```

> **힌트 — shimmer 원리**
> 그라디언트를 컨테이너보다 2배 넓게(`background-size: 200%`) 만들고
> `background-position`을 움직여서 빛이 훑고 지나가는 효과를 낸다.
> 요소 자체는 움직이지 않으므로 성능 부담이 거의 없다.

---

## 06. 완료 항목 아래로 정렬

DB 변경 없이 프론트 정렬만으로 된다.

```jsx
// Home.jsx — 렌더링 직전에 정렬
const sortedRoutines = [...routines].sort((a, b) => {
  if (a.done !== b.done) return a.done ? 1 : -1;   // 미완료 먼저
  return 0;
});
```

> **힌트 — `[...routines]` 복사 필수**
> `sort()`는 원본 배열을 직접 바꾼다(mutating).
> state를 직접 변형하면 React가 변경을 감지하지 못해 리렌더가 안 될 수 있다.
> 복사본을 만들어서 정렬하는 게 원칙이다.

> **힌트 — boolean 정렬**
> `a.done ? 1 : -1` → `done: true`인 항목을 뒤로 보낸다.
> `sort`의 비교 함수는 양수면 `a`를 뒤로, 음수면 `a`를 앞으로 보낸다.

즉시 순서가 바뀌면 시선이 튀므로, 위치 이동에 애니메이션을 넣으면 좋다.

```css
.routineCard {
  transition: opacity 0.2s ease, transform 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}
```

> 완전한 위치 애니메이션이 필요하면 FLIP 기법이나 `framer-motion`의 `layout` prop을 쓴다.
> 지금 규모에선 과하다. `transition`만으로 충분하다.

---

## 07. 드래그 정렬 (sortOrder)

### 엔티티

```java
@Column(name = "sort_order", nullable = false)
private Integer sortOrder = 0;
```

```sql
ALTER TABLE routines ADD COLUMN sort_order INT NOT NULL DEFAULT 0;
-- 기존 데이터에 순서 부여
SET @row = 0;
UPDATE routines SET sort_order = (@row := @row + 1) ORDER BY id;
```

### 조회 시 정렬

```java
List<Routine> findByUserIdOrderBySortOrderAsc(Long userId);
```

> **힌트 — 쿼리 메서드 규칙**
> `findBy` + 필드명 + `OrderBy` + 필드명 + `Asc`/`Desc`.
> 메서드 이름만으로 JPA가 쿼리를 만들어준다. `@Query` 불필요.

### 순서 저장 API

```java
@PutMapping("/order")
public ResponseEntity<Void> updateOrder(
        @RequestBody List<Long> orderedIds,
        @AuthenticationPrincipal UserDetails userDetails) {
    routineService.reorder(orderedIds, userDetails);
    return ResponseEntity.noContent().build();
}
```

```java
@Transactional
public void reorder(List<Long> orderedIds, UserDetails userDetails) {
    User user = userRepository.findByEmail(userDetails.getUsername())
            .orElseThrow(() -> new IllegalArgumentException("유저 없음"));

    List<Routine> routines = routineRepository.findByUserId(user.getId());
    Map<Long, Routine> map = routines.stream()
            .collect(Collectors.toMap(Routine::getId, r -> r));

    for (int i = 0; i < orderedIds.size(); i++) {
        Routine r = map.get(orderedIds.get(i));
        if (r != null) r.setSortOrder(i);   // 더티 체킹으로 일괄 UPDATE
    }
}
```

> **힌트 — ID 배열만 받는 이유**
> 전체 루틴 객체를 보낼 필요 없이 `[3, 1, 4, 2]` 같은 순서 배열만 보내면 된다.
> 배열의 인덱스가 곧 `sortOrder`가 된다. 페이로드가 작고 로직이 단순하다.

> **힌트 — `Map`으로 변환하는 이유**
> 루프 안에서 매번 `findById`를 호출하면 N번 쿼리가 나간다(N+1 문제).
> 한 번에 다 가져와서 Map으로 만들어두면 조회가 O(1)이다.

### 프론트

라이브러리 없이 HTML5 Drag and Drop API로 구현할 수 있다.

```jsx
const [dragIndex, setDragIndex] = useState(null);

const handleDragStart = (index) => setDragIndex(index);

const handleDragOver = (e, index) => {
  e.preventDefault();                    // 이게 없으면 drop이 발생하지 않음
  if (dragIndex === null || dragIndex === index) return;

  setRoutines(prev => {
    const next = [...prev];
    const [moved] = next.splice(dragIndex, 1);
    next.splice(index, 0, moved);
    return next;
  });
  setDragIndex(index);
};

const handleDragEnd = async () => {
  setDragIndex(null);
  await updateRoutineOrder(routines.map(r => r.id));   // 순서 저장
};
```

```jsx
<div
  className={styles.routineCard}
  draggable
  onDragStart={() => handleDragStart(i)}
  onDragOver={(e) => handleDragOver(e, i)}
  onDragEnd={handleDragEnd}
  data-dragging={dragIndex === i}
>
```

> **힌트 — `e.preventDefault()` 필수**
> HTML5 DnD는 기본적으로 "드롭 불가" 상태다.
> `dragover`에서 `preventDefault()`를 호출해야 드롭 가능 영역이 된다.
> 이거 빠뜨려서 안 되는 경우가 대부분이다.

```css
.routineCard[data-dragging="true"] {
  opacity: 0.4;
  border-color: var(--accent);
  cursor: grabbing;
}
.dragHandle {
  cursor: grab;
  color: var(--text2);
  padding: 0 4px;
  user-select: none;
}
```

> **힌트 — 드래그 핸들 분리**
> 카드 전체를 `draggable`로 만들면 텍스트 선택이 안 된다.
> 왼쪽에 `⠿` 같은 핸들을 두고 거기만 드래그하게 하는 게 낫다.
> 모바일에서는 HTML5 DnD가 동작하지 않으므로, 필요하면 `dnd-kit` 같은 라이브러리를 쓴다.

---

## 08. 요일별 루틴 (repeatDays)

### 저장 방식

비트마스크가 가장 단순하다. 일=1, 월=2, 화=4, 수=8, 목=16, 금=32, 토=64.

```java
@Column(name = "repeat_days", nullable = false)
private Integer repeatDays = 127;   // 127 = 모든 요일 (1111111)
```

```sql
ALTER TABLE routines ADD COLUMN repeat_days INT NOT NULL DEFAULT 127;
```

> **힌트 — 비트마스크**
> 요일 7개를 정수 하나에 담는 방법.
> 월·수·금 = 2 + 8 + 32 = 42.
> 별도 테이블(`routine_repeat_days`)을 만들 필요가 없어서 쿼리가 단순해진다.
> 단점은 SQL만 봐서는 값의 의미를 알기 어렵다는 것.

### 오늘 해당하는 루틴만 조회

```java
public List<RoutineResponse> getTodayRoutines(UserDetails userDetails) {
    User user = userRepository.findByEmail(userDetails.getUsername())
            .orElseThrow(() -> new IllegalArgumentException("유저 없음"));

    // DayOfWeek: 월=1 ... 일=7 → 비트 위치로 변환
    int dow = LocalDate.now().getDayOfWeek().getValue();  // 1~7
    int todayBit = 1 << (dow % 7);   // 일요일(7)을 0번 비트로

    return routineRepository.findByUserIdOrderBySortOrderAsc(user.getId())
            .stream()
            .filter(r -> (r.getRepeatDays() & todayBit) != 0)
            .map(RoutineResponse::from)
            .toList();
}
```

> **힌트 — `&` 비트 AND 연산**
> `repeatDays & todayBit`가 0이 아니면 오늘 해당하는 루틴이다.
> 예: `42 & 8` → 월수금(42)에 수요일 비트(8)가 켜져 있으므로 8 (≠ 0) → 포함.

> **힌트 — 필터를 DB에서 할 수도 있다**
> `@Query("... WHERE FUNCTION('BITAND', r.repeatDays, :bit) > 0")`
> 데이터가 많아지면 DB에서 거르는 게 낫지만, 개인 루틴은 많아야 수십 개다.
> 지금은 자바 스트림으로 충분하다.

### 프론트 — 요일 선택 UI

```jsx
const DAYS = ["일", "월", "화", "수", "목", "금", "토"];

function DayPicker({ value, onChange }) {
  const toggle = (i) => {
    const bit = 1 << i;
    onChange(value & bit ? value & ~bit : value | bit);
  };

  return (
    <div className={styles.dayPicker}>
      {DAYS.map((d, i) => (
        <button
          key={d}
          type="button"
          className={styles.dayChip}
          data-on={(value & (1 << i)) !== 0}
          onClick={() => toggle(i)}
        >
          {d}
        </button>
      ))}
    </div>
  );
}
```

> **힌트 — 비트 켜기 / 끄기**
> 켜기: `value | bit` (OR)
> 끄기: `value & ~bit` (AND NOT)
> 확인: `value & bit` (AND)

```css
.dayPicker { display: flex; gap: 6px; margin-top: 10px; }
.dayChip {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  border: 1px solid var(--border);
  background: transparent;
  color: var(--text2);
  font-size: 12px;
  font-family: inherit;
  cursor: pointer;
  transition: all 0.15s ease;
}
.dayChip:hover { border-color: var(--accent); }
.dayChip[data-on="true"] {
  background: var(--accent);
  border-color: var(--accent);
  color: #fff;
  font-weight: 600;
}
```

카드에 요일 뱃지를 작게 표시하면 알아보기 쉽다.

```jsx
{r.repeatDays !== 127 && (
  <span className={styles.repeatBadge}>
    {DAYS.filter((_, i) => r.repeatDays & (1 << i)).join("·")}
  </span>
)}
```

> **힌트 — 매일이면 뱃지 숨기기**
> `127`(매일)일 때는 "일·월·화·수·목·금·토"를 다 보여줄 필요가 없다.
> 특별한 경우에만 표시하는 게 정보 밀도 면에서 낫다.

---

## 09. 반응형

이미 `@media (max-width: 760px)`로 2단 → 1단은 되어 있다. 나머지 보완:

```css
@media (max-width: 760px) {
  .page { padding: 20px 16px 60px; }

  .hero {
    flex-direction: column;
    align-items: flex-start;
    gap: 18px;
    padding: 22px 24px;
  }
  .heroTitle { font-size: 20px; }
  .heroArt { align-self: center; }
}

@media (max-width: 480px) {
  .heroTitle { font-size: 18px; }

  /* 터치 기기에는 hover가 없으므로 삭제 버튼을 항상 노출 */
  .deleteBtn { opacity: 1; }

  /* 터치 타겟 최소 44px 확보 */
  .check { width: 30px; height: 30px; }
  .routineCard { padding: 15px; }
}
```

> **힌트 — hover가 없는 기기**
> `@media (hover: hover)`로 마우스가 있는 기기만 타겟팅할 수 있다.
> 화면 너비보다 정확한 방법이다.
> ```css
> @media (hover: hover) {
>   .deleteBtn { opacity: 0; }
>   .routineCard:hover .deleteBtn { opacity: 1; }
> }
> ```

> **힌트 — 터치 타겟 44px**
> Apple HIG / Material Design 공통 권장 최소 크기.
> 이보다 작으면 손가락으로 누르기 어렵다.

접근성 기본선:

```css
@media (prefers-reduced-motion: reduce) {
  *, *::before, *::after {
    animation-duration: 0.01ms !important;
    transition-duration: 0.01ms !important;
  }
}

/* 키보드 포커스 표시 */
button:focus-visible, input:focus-visible, a:focus-visible {
  outline: 2px solid var(--accent);
  outline-offset: 2px;
}
```

> **힌트 — `:focus-visible` vs `:focus`**
> `:focus`는 마우스 클릭에도 반응해서 테두리가 생긴다(보기 싫다).
> `:focus-visible`은 키보드 탐색일 때만 적용된다. 이쪽을 쓴다.

---

## 10. 다크 / 라이트 토글

### 구조 변경

지금은 CSS 변수가 `.page` 안에 있다. 전역으로 올려야 한다.

```css
/* index.css 또는 App.css */
:root,
[data-theme="dark"] {
  --bg:       #0f1117;
  --surface:  #181c27;
  --surface2: #1f2436;
  --border:   #2a3050;
  --text:     #e2e6f0;
  --text2:    #8b93b0;
  --accent:   #4f8ef7;
  --green:    #3ecf8e;
  --yellow:   #f0a940;
  --red:      #e05c5c;
}

[data-theme="light"] {
  --bg:       #f7f8fa;
  --surface:  #ffffff;
  --surface2: #eef0f5;
  --border:   #dde1e9;
  --text:     #1a1d26;
  --text2:    #6b7280;
  --accent:   #2563eb;
  --green:    #059669;
  --yellow:   #d97706;
  --red:      #dc2626;
}

body {
  background: var(--bg);
  color: var(--text);
  transition: background 0.25s ease, color 0.25s ease;
}
```

> **힌트 — 라이트 모드는 색을 그대로 못 쓴다**
> 다크에서 잘 보이던 `#3ecf8e`는 흰 배경에서 대비가 부족하다.
> 라이트용은 채도를 낮추고 명도를 어둡게 조정해야 한다.
> WebAIM Contrast Checker로 4.5:1 이상 나오는지 확인하면 확실하다.

### Context

```jsx
// context/ThemeContext.jsx
import { createContext, useContext, useState, useEffect } from "react";

const ThemeContext = createContext();

export function ThemeProvider({ children }) {
  const [theme, setTheme] = useState(() => {
    const saved = localStorage.getItem("theme");
    if (saved) return saved;
    return window.matchMedia("(prefers-color-scheme: light)").matches
      ? "light"
      : "dark";
  });

  useEffect(() => {
    document.documentElement.setAttribute("data-theme", theme);
    localStorage.setItem("theme", theme);
  }, [theme]);

  const toggle = () => setTheme(t => (t === "dark" ? "light" : "dark"));

  return (
    <ThemeContext.Provider value={{ theme, toggle }}>
      {children}
    </ThemeContext.Provider>
  );
}

export const useTheme = () => useContext(ThemeContext);
```

> **힌트 — `useState`에 함수 전달 (lazy initializer)**
> `useState(localStorage.getItem(...))`로 쓰면 매 렌더마다 `localStorage`를 읽는다.
> `useState(() => ...)`로 함수를 넘기면 최초 1회만 실행된다.
> `AuthContext`와 같은 패턴이다.

> **힌트 — `prefers-color-scheme`**
> 저장된 값이 없을 때 OS 설정을 따라간다.
> 사용자가 이미 시스템을 다크로 쓰고 있으면 그대로 다크로 시작하는 게 자연스럽다.

### 토글 버튼

Header의 `userArea`에 넣는다.

```jsx
const { theme, toggle } = useTheme();

<button
  className={styles.themeBtn}
  onClick={toggle}
  aria-label={theme === "dark" ? "라이트 모드로 전환" : "다크 모드로 전환"}
>
  {theme === "dark" ? "☀" : "☾"}
</button>
```

```css
.themeBtn {
  width: 34px;
  height: 34px;
  border-radius: 9px;
  border: 1px solid var(--border);
  background: transparent;
  color: var(--text2);
  font-size: 15px;
  cursor: pointer;
  transition: all 0.15s ease;
}
.themeBtn:hover { color: var(--text); border-color: var(--accent); }
```

> **힌트 — `aria-label`**
> 아이콘만 있는 버튼은 스크린 리더가 읽을 게 없다.
> 무슨 동작을 하는 버튼인지 텍스트로 설명해줘야 한다.

### 깜빡임 방지

React가 마운트되기 전 한순간 다크 배경이 보이는 문제가 있다.

```html
<!-- index.html의 <head> 안, 다른 스크립트보다 먼저 -->
<script>
  (function () {
    var t = localStorage.getItem('theme') ||
      (window.matchMedia('(prefers-color-scheme: light)').matches ? 'light' : 'dark');
    document.documentElement.setAttribute('data-theme', t);
  })();
</script>
```

> **힌트 — FOUC (Flash of Unstyled Content)**
> HTML 파싱 중에 동기 스크립트를 실행해서 렌더링 전에 속성을 붙여버린다.
> 다크모드를 지원하는 사이트는 대부분 이 방식을 쓴다.

---

## 구현 순서 제안

| 순서 | 기능 | 난이도 | 백엔드 |
|---|---|---|---|
| 1 | 메모 상세 / 수정 | 중 | 필요 |
| 2 | 빈 상태 | 하 | — |
| 3 | 로딩 스켈레톤 | 하 | — |
| 4 | 완료 항목 아래로 | 하 | — |
| 5 | Undo 토스트 | 중 | — |
| 6 | 반응형 | 하 | — |
| 7 | 다크 / 라이트 | 중 | — |
| 8 | 요일별 루틴 | 중 | 필요 |
| 9 | 드래그 정렬 | 상 | 필요 |

2~4번은 CSS와 조건부 렌더링만으로 30분 안에 끝난다.
먼저 붙여서 화면 완성도를 올려두면 이후 작업할 때 기분이 낫다.

9번(드래그 정렬)은 모바일 대응까지 하려면 라이브러리가 필요하니 마지막에 둔다.
