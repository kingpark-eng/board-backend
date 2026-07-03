# Spring Data JPA 쿼리 메서드 정리

`JpaRepository`를 상속한 인터페이스에서, **메서드 이름을 규칙대로 지으면** Spring Data JPA가
이름을 파싱해 쿼리를 자동 생성해준다. 구현 클래스는 작성하지 않아도 된다.

---

## 세 가지 방식

| 방식 | 설명 | 예시 |
|------|------|------|
| 상속 메서드 | `JpaRepository`가 기본 제공 | `save()`, `findById()`, `delete()` |
| 쿼리 메서드 | 이름 규칙으로 자동 생성 | `findByRoutineIdAndLogDate(...)` |
| `@Query` | JPQL 직접 작성 | 집계 · 조인 · 서브쿼리 |

기준선: **단순 조회·삭제·카운트는 쿼리 메서드, 집계나 조인이 얽히면 `@Query`.**

---

## 기본 구조

```
[동사] + By + [필드명] + [조건 연산] + [정렬]
```

- 필드명은 **DB 컬럼명이 아니라 자바 필드명** 기준 (`log_date` 컬럼 → `LogDate`)
- 조건 순서와 파라미터 순서가 일치해야 함

---

## 동사 (도입부)

| 키워드 | 동작 | 반환 |
|--------|------|------|
| `findBy` / `getBy` / `readBy` | 조회 | 엔티티 / 리스트 / Optional |
| `countBy` | 개수 세기 | `long` |
| `existsBy` | 존재 여부 | `boolean` |
| `deleteBy` / `removeBy` | 삭제 | `void` / `long` |

---

## 조건 결합

| 키워드 | 의미 |
|--------|------|
| `And` | 그리고 |
| `Or` | 또는 |

```java
findByRoutineIdAndLogDate(Long routineId, LocalDate logDate)
// where routine_id = ? and log_date = ?
```

---

## 조건 연산 키워드

| 키워드 | 의미 |
|--------|------|
| `GreaterThan` / `GreaterThanEqual` | `>` / `>=` |
| `LessThan` / `LessThanEqual` | `<` / `<=` |
| `Between` | 범위 (between A and B) |
| `Like` / `Containing` / `StartingWith` | 문자열 검색 |
| `In` | 여러 값 중 포함 |
| `IsNull` / `IsNotNull` | null 여부 |
| `Not` | 부정 |
| `True` / `False` | boolean 값 |

```java
findByLogDateBetween(LocalDate start, LocalDate end)
// where log_date between ? and ?

findByLogDateGreaterThanEqual(LocalDate date)
// where log_date >= ?

countByRoutineIdIn(List<Long> ids)
// where routine_id in (?)
```

---

## 정렬 · 개수 제한

```java
findByRoutineIdOrderByLogDateDesc(Long routineId)
// order by log_date desc

findTop5ByRoutineIdOrderByLogDateDesc(Long routineId)
// 상위 5개만
```

---

## 중첩 프로퍼티 (연관 엔티티 탐색)

연관 관계를 점 찍듯 타고 들어갈 수 있다.
`RoutineLog → Routine → User → email`:

```java
findByRoutineUserEmail(String email)
// where routine.user.email = ?
```

필드 경계가 애매하면 언더스코어로 명시:

```java
findByRoutine_User_Email(String email)
```

---

## 규칙만으로 벅찰 때 → `@Query`

`group by`, 조인 집계, 특정 컬럼만 select 등은 이름 규칙으로 표현이 안 되므로 JPQL을 직접 쓴다.

```java
@Query("select rl.logDate, count(rl) from RoutineLog rl " +
       "where rl.routine.user.id = :userId " +
       "and rl.logDate between :start and :end group by rl.logDate")
List<Object[]> countByDateRange(Long userId, LocalDate start, LocalDate end);
```

이름이 세 단어를 넘어가기 시작하면 `@Query`가 더 읽기 쉬운 경우가 많다.
