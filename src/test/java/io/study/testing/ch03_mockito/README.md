# 03장 — Mockito

레포 루트에서: `./gradlew test --tests '*ch03_mockito*'`

## 파일

| 파일 | 내용 |
|---|---|
| `MockitoBasicsTest` | `@Mock`/`@InjectMocks`, stubbing, 매처, `verify`, BDDMockito, Strictness |
| `MockitoAdvancedTest` | `ArgumentCaptor`, `InOrder`, spy, `Answer` |
| `exercise/MockitoExercise` | 연습문제 5개 |

## 핵심

**목이 필요한 이유는 하나다** — 검증하고 싶은 것이 **반환값이 아니라 상호작용**일 때.

> "결제사를 정확히 한 번만 불렀나", "실패했을 때 FAILED로 저장했나"

이건 리턴값을 아무리 봐도 알 수 없다. 오직 `verify`만이 잡는다.

**반대로, 반환값만 보면 되는 것에는 목을 쓰지 않는다.** 가짜를 끼우는 순간
테스트가 "구현이 이 순서로 이걸 부른다"에 묶여, 리팩터링할 때마다 깨진다.

## 용어

| | 관심사 |
|---|---|
| **Stub** | 정해진 값을 돌려주는 가짜. "무엇을 돌려줄까"만 |
| **Mock** | 여기에 "어떻게 불렸나" 검증이 붙은 것 |
| **Spy** | **진짜 객체**를 감싸서 일부만 가로챈 것 |

## 함정

🔥 **스터빙 안 한 메서드는 예외가 아니라 기본값을 돌려준다.**
객체→`null`, `int`→`0`, `Optional`→`empty()`, `List`→빈 컬렉션.
그래서 스터빙을 빠뜨리면 NPE가 엉뚱한 곳에서 터진다.
실패 원인을 찾을 때 "이 목이 뭘 돌려주고 있지?"를 먼저 의심할 것.

🔥 **매처와 실제 값을 섞어 쓸 수 없다.**
```java
when(gateway.charge("CUST-1", any()))       // ✗ InvalidUseOfMatchersException
when(gateway.charge(eq("CUST-1"), any()))   // ✓ 하나라도 매처면 전부 매처
```

🔥 **스파이는 `doReturn(...).when(spy).method()` 형태를 쓴다.**
`when(spy.get(0))`은 스터빙하기 **전에 진짜 메서드가 먼저 실행되어** 그 자리에서 터진다.

🔥 **`@InjectMocks`는 생성자 주입일 때 잘 동작한다.** 필드 주입이면 조용히 null이 남을 수 있다.
테스트하기 쉬운 설계가 대개 좋은 설계인 이유의 한 사례다.

⚠ **`MockitoExtension`의 기본은 `STRICT_STUBS`다.** 쓰이지 않은 스터빙이 있으면 실패한다.
성가시지만, 그건 대개 **테스트가 의도한 경로를 안 타고 있다**는 뜻이다.
`LENIENT`로 끄기 전에 "왜 안 불렸지?"를 먼저 볼 것.

⚠ **`verifyNoMoreInteractions`는 남용하지 않는다.** 구현이 무해한 호출을 하나
추가하는 순간 깨져서, 리팩터링을 방해한다. 도메인 규칙일 때만 쓴다.

⚠ **`InOrder`도 순서가 도메인 규칙일 때만.** 상관없는 호출에 걸면
구현을 정리할 때마다 애먼 테스트가 깨진다.

⚠ **중첩 클래스 이름을 `Captor`로 짓지 말 것.** `@Captor` 어노테이션을 가린다.
