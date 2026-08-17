# Spring 테스트 라이브러리 학습 레포

Spring Boot 프로젝트에서 쓰는 테스트 라이브러리를 **직접 돌려보며** 익히는 연습장입니다.
읽기만 하는 문서가 아니라, 통과하는 예제 133개와 직접 풀어야 하는 연습문제 28개로 되어 있습니다.

```
Java 21 · Spring Boot 3.4.7 · Gradle 8.14.3 (Kotlin DSL)
```

---

## 시작하기

```bash
git clone https://github.com/Fiddich-Dev/spring-testing-study.git
cd spring-testing-study

./gradlew test        # 완성본 133개 — 전부 초록이어야 한다
./gradlew exercise    # 연습문제 28개 — 처음에는 26개가 빨간불이 정상이다
```

Docker도 DB도 필요 없습니다. JDK 21만 있으면 됩니다.
(Gradle은 wrapper가 알아서 받습니다.)

### 학습 루프

1. 한 장의 완성본을 읽는다 (`src/test/java/io/study/testing/ch0N_*/`)
2. 같은 장의 연습문제를 푼다 (`.../ch0N_*/exercise/`)
3. 초록이 될 때까지 돌린다

```bash
./gradlew exercise --tests '*JUnit5Exercise'   # 한 장만
./gradlew test --tests '*AssertJBasicsTest'    # 완성본 한 파일만
```

> **왜 태스크가 둘인가**
> 연습문제는 일부러 실패합니다. 그걸 `test`에 섞으면 CI가 영원히 빨간불이 되고,
> "깨진 게 정상"인 상태에 익숙해집니다 — 그러면 진짜 회귀가 났을 때 아무도 알아채지 못합니다.
> 그래서 `@Tag("exercise")`로 갈라 두었습니다.

---

## 이론편

실습 전에 읽을 문서가 있습니다 — **[docs/theory.html](docs/theory.html)** (브라우저로 열기: `open docs/theory.html`)

- **1부 토대** — 테스트는 무엇을 사는가 · 테스트 피라미드와 그 반론 · 테스트 더블 5종 ·
  FIRST · AAA/Given-When-Then · TDD 사이클 · 테스트 가능한 설계 · 테스트하지 말아야 할 것
- **2부 Spring 내부 동작** — TestContext Framework · 슬라이스가 빈을 고르는 법 ·
  컨텍스트 캐싱 · MockMvc가 서버 없이 도는 원리 · `@MockitoBean`의 빈 교체 · 트랜잭션 롤백의 구멍
- **3부** 이론 ↔ 이 저장소 파일 대응표, **4부** 이해도 확인 8문항 (인터랙티브)

권하는 순서는 이론 1부 → 1~3장 실습 → 이론 2.2·2.4 → 4~5장 실습 → 이론 2.3·2.6 → 퀴즈입니다.

---

## 목차

| 장 | 주제 | 배우는 것 |
|---|---|---|
| **01** | [JUnit 5](src/test/java/io/study/testing/ch01_junit5/) | 라이프사이클, `@Nested`, `@ParameterizedTest`, `assertAll`, assume vs assert |
| **02** | [AssertJ](src/test/java/io/study/testing/ch02_assertj/) | `extracting`, `filteredOn`, `usingRecursiveComparison`, `SoftAssertions` |
| **03** | [Mockito](src/test/java/io/study/testing/ch03_mockito/) | stubbing, `verify`, `ArgumentCaptor`, `InOrder`, spy, Strictness |
| **04** | [Spring 슬라이스](src/test/java/io/study/testing/ch04_springslice/) | `@WebMvcTest`, MockMvc, `@MockitoBean`, `@SpringBootTest`와의 차이 |
| **05** | [JSON](src/test/java/io/study/testing/ch05_json/) | `@JsonTest`, JacksonTester, JSONassert, JsonPath |

각 장 폴더에 README가 있습니다.

---

## 의존성은 이 네 줄이 전부입니다

```kotlin
testImplementation("org.springframework.boot:spring-boot-starter-test")
testRuntimeOnly("org.junit.platform:junit-platform-launcher")
```

`spring-boot-starter-test` **한 줄**이 아래를 전부 끌고 옵니다.
01~05장에서 쓰는 라이브러리 중 따로 추가한 것은 **하나도 없습니다**.

| 라이브러리 | 역할 | 다루는 장 |
|---|---|---|
| **JUnit 5 (Jupiter)** | 테스트 실행 엔진 | 01 |
| **AssertJ** | fluent 단언 — Spring 팀 권장 | 02 |
| **Mockito** | 목 객체, 상호작용 검증 | 03 |
| **Spring Test** | `MockMvc`, 슬라이스 어노테이션, `@MockitoBean` | 04 |
| **JSONassert** | JSON 문자열 전체 비교 (LENIENT / STRICT) | 05 |
| **JsonPath** | JSON에서 값 추출 — `jsonPath()`가 쓰는 것 | 04, 05 |
| **Hamcrest** | 매처 (AssertJ로 대체하는 추세) | 04에 잠깐 |
| **XMLUnit** | XML 비교 | 이 레포에서는 안 씀 |
| **Awaitility** | 비동기 대기 | 이 레포에서는 안 씀 |

---

## 이 레포에 없는 것

핵심 5장만 다룹니다. 실무에서 그다음으로 필요한 것들:

| 라이브러리 | 언제 필요한가 |
|---|---|
| **Testcontainers** | 실제 Postgres/Redis로 통합 테스트. Boot 3.1+의 `@ServiceConnection` |
| **ArchUnit** | 패키지 의존 방향·순환을 테스트로 강제 |
| **WireMock** | 외부 HTTP API 스텁 |
| **Awaitility** | 비동기·이벤트 결과 대기 |
| **REST Assured** | E2E API 테스트를 BDD 스타일로 |
| **JaCoCo** | 커버리지 측정 |

---

## 알아 둘 것

**`@MockBean`은 deprecated입니다.** Spring Boot 3.4 / Framework 6.2부터
`@MockitoBean`으로 바뀌었습니다. 패키지도 다릅니다
(`org.springframework.test.context.bean.override.mockito`).
인터넷의 예전 예제 대부분이 `@MockBean`을 쓰고 있으니 주의하세요.

**Boot 버전을 3.4.7로 고정했습니다.** Spring Initializr는 이제 4.1.x만 주지만,
실무 코드베이스와 맞추기 위해 의도적으로 내렸습니다.
버전을 올리려면 `build.gradle.kts`와 `gradle/wrapper/gradle-wrapper.properties`를
함께 고쳐야 합니다 — Boot 3.4는 Gradle 9를 지원하지 않습니다.

---

## 참고 문서

- [Spring Boot Testing](https://docs.spring.io/spring-boot/reference/testing/index.html)
- [AssertJ 가이드](https://assertj.github.io/doc/)
- [Mockito 문서](https://javadoc.io/doc/org.mockito/mockito-core/latest/org/mockito/Mockito.html)
- [JUnit 5 User Guide](https://junit.org/junit5/docs/current/user-guide/)
