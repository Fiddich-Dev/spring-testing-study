# 01장 — JUnit 5

레포 루트에서: `./gradlew test --tests '*ch01_junit5*'`

## 파일

| 파일 | 내용 |
|---|---|
| `JUnit5BasicsTest` | 라이프사이클, `@Nested`, `assertAll`, 예외, assume |
| `ParameterizedTestExamplesTest` | `@ValueSource`, `@CsvSource`, `@MethodSource`, `@EnumSource` |
| `exercise/JUnit5Exercise` | 연습문제 5개 |

## 핵심

**JUnit 5는 세 조각이다.** Jupiter(우리가 쓰는 API) · Platform(실행 엔진) · Vintage(JUnit 4 어댑터).
`build.gradle.kts`에 `junit-platform-launcher`가 있는 이유가 두 번째 조각 때문이다.

**기본 생명주기는 PER_METHOD다.** 테스트 메서드마다 인스턴스를 새로 만든다.
그래서 필드에 남긴 상태가 다음 테스트로 넘어가지 않고, 테스트 격리가 공짜로 온다.

**`assertAll`은 실패를 모아서 보고한다.** 그냥 나열하면 첫 실패에서 멈춰서,
고치고 → 돌리고 → 또 다른 실패를 보는 일을 반복하게 된다.

**`@ParameterizedTest`의 진짜 이득은 실패했을 때다.** for 문을 돌면 어느 입력에서
깨졌는지 모르지만, 파라미터 테스트는 실패한 케이스가 이름째 리포트에 뜬다.

## 함정

⚠ **assume은 실패가 아니라 건너뜀이다.** 조건이 영영 거짓이면 아무도 모르게 계속 건너뛴다.
"CI는 초록인데 실제로는 한 번도 안 돈 테스트"가 이렇게 생긴다.

⚠ **`@ValueSource`는 기본형·String·Class만 받는다.** 객체를 넘기려면 `@MethodSource`.

⚠ **`@MethodSource`의 팩터리 메서드는 static이어야 한다.**
(`@TestInstance(PER_CLASS)`를 걸면 인스턴스 메서드도 가능)

⚠ **`Assertions.assertEquals`는 기대값이 앞이다.** 순서를 바꿔도 통과하지만
실패 메시지가 뒤집혀 나온다. AssertJ는 한 방향뿐이라 이 실수가 없다.
