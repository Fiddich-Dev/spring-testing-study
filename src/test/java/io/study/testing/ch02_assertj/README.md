# 02장 — AssertJ

레포 루트에서: `./gradlew test --tests '*ch02_assertj*'`

## 파일

| 파일 | 내용 |
|---|---|
| `AssertJBasicsTest` | 객체·문자열·숫자·컬렉션·Map·Optional·예외·날짜 |
| `AssertJAdvancedTest` | `extracting`, `filteredOn`, `usingRecursiveComparison`, `SoftAssertions` |
| `exercise/AssertJExercise` | 연습문제 6개 |

## 핵심

규칙은 하나뿐이다 — **`assertThat(실제값).검증(기대값)`**. 항상 실제값이 앞이다.

**진짜 값어치는 실패 메시지에 있다.** `assertTrue(list.contains(x))`는 실패해도
"expected true but was false"만 남기고 리스트에 뭐가 들었는지 안 알려준다.
AssertJ는 실제 내용을 통째로 찍어 준다.

**외울 필요가 없다.** `assertThat(값).`까지 치고 자동완성을 열면
그 타입에 쓸 수 있는 검증이 전부 뜬다. 그것이 이 라이브러리의 설계 의도다.

## 실무 주력 넷

| 기능 | 언제 |
|---|---|
| `extracting` | 컬렉션에서 필드만 뽑아 비교. for 문이 사라진다 |
| `filteredOn` | 조건에 맞는 것만 골라서 검증. `extracting`과 조합이 많다 |
| `usingRecursiveComparison` | equals 없는 객체를 필드 단위로 비교. `ignoringFields`로 ID·시각 제외 |
| `SoftAssertions` | 실패를 모아서 보고. 응답 필드 여러 개를 볼 때 |

## 함정

🔥 **`contains` 계열 셋의 차이**
- `contains` — 들어 있기만 하면 됨 (순서·개수 무관)
- `containsExactly` — 순서까지 정확히
- `containsExactlyInAnyOrder` — 원소 집합만 같으면 됨

Set·Map처럼 순서가 보장되지 않는 자료구조에 `containsExactly`를 쓰면
지금은 통과하다가 나중에 깨진다.

🔥 **`SoftAssertions`는 `assertAll()`을 부르지 않으면 아무것도 검증하지 않는다.**
빠뜨리면 테스트가 **영원히 초록**이 된다. `@ExtendWith(SoftAssertionsExtension.class)` +
`@InjectSoftAssertions`를 쓰면 이 실수가 원천 차단된다.

⚠ **`SoftAssertions`는 AutoCloseable이 아니다.** try-with-resources에 넣으려면
`AutoCloseableSoftAssertions`를 써야 한다.

⚠ **`as()`는 단언 앞에 와야 한다.** 뒤에 쓰면 조용히 무시된다.

⚠ **부동소수점을 `isEqualTo`로 비교하지 말 것.** `0.1 + 0.2 != 0.3`이다.
`isCloseTo(0.3, within(0.0001))`을 쓴다.

⚠ **테스트 메서드 이름을 `assertThatThrownBy`처럼 짓지 말 것.**
같은 이름의 static import가 가려져서 컴파일이 안 된다
(자바는 클래스 멤버를 static import보다 먼저 본다).
