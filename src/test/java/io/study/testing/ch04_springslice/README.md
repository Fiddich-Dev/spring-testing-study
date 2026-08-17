# 04장 — Spring 슬라이스 테스트

레포 루트에서: `./gradlew test --tests '*ch04_springslice*'`

## 파일

| 파일 | 내용 |
|---|---|
| `ArticleControllerWebMvcTest` | `@WebMvcTest` + MockMvc + `@MockitoBean`, 200/400/404/415/204 |
| `SpringBootTestComparisonTest` | `@SpringBootTest`와의 차이, `TestRestTemplate` |
| `exercise/WebMvcExercise` | 연습문제 5개 (문제 0을 먼저 풀어야 나머지가 실행된다) |

## `@WebMvcTest`가 세우는 것 / 안 세우는 것

| 세운다 | 안 세운다 |
|---|---|
| `@Controller`, `@RestController` | `@Service` |
| **`@ControllerAdvice`** | `@Repository` |
| `Filter`, `WebMvcConfigurer` | `@Component` |
| `Converter`, Jackson 설정 | JPA, DataSource |
| 검증(Validation) | 일반 `@Configuration` |

🔥 **그래서 서비스 자리는 비어 있다.** `@MockitoBean`으로 채우지 않으면
테스트가 한 줄도 실행되기 전에 `NoSuchBeanDefinitionException`으로 죽는다.
이 실패 메시지를 직접 보는 것이 이 장의 첫 교훈이다.

📌 **반대로 `@RestControllerAdvice`는 포함된다.** 그래서 목이 예외를 던지면
**실제 예외 처리 경로가 그대로 돈다** — 404 응답 본문의 모양까지 검증할 수 있다.

## `@WebMvcTest` vs `@SpringBootTest`

| | `@WebMvcTest` | `@SpringBootTest` |
|---|---|---|
| 세우는 빈 | 웹 계층만 | 전부 |
| 속도 | 빠르다 | 느리다 |
| 서비스 | 목으로 채운다 | 진짜가 돈다 |
| 서버 | 안 뜬다 (MockMvc가 흉내) | `RANDOM_PORT`면 진짜로 뜬다 |
| 잡는 버그 | 매핑·검증·직렬화·예외 처리 | 배선(빈 조립) 문제, 전 구간 흐름 |

🔥 **둘 중 하나를 고르는 문제가 아니다.** 컨트롤러 케이스는 슬라이스로 촘촘하게 깔고,
전체 컨텍스트는 **대표 경로 몇 개**만 둔다. 전부 `@SpringBootTest`로 짜면
스위트가 몇 분씩 걸려 TDD 사이클이 죽는다.

📌 **`@SpringBootTest` + `@AutoConfigureMockMvc` 조합이 실무에서 흔하다.**
진짜 빈을 쓰면서도 HTTP 오버헤드 없이 빠르게 돈다.

## 함정

⚠ **`@MockBean`은 deprecated다** (Boot 3.4 / Framework 6.2부터).
`@MockitoBean`을 쓴다. 패키지도 다르다
(`org.springframework.test.context.bean.override.mockito`).

⚠ **Content-Type을 안 주면 400이 아니라 415다.** 이 둘을 헷갈리면
프론트와 원인 진단이 어긋난다.

⚠ **`getContentAsString()`은 ISO-8859-1로 읽어 한글이 깨진다.**
`getContentAsString(StandardCharsets.UTF_8)`을 쓰거나, 애초에 `jsonPath`로 검증한다.

⚠ **컨텍스트 캐싱** — Spring은 설정이 같으면 컨텍스트를 재사용한다.
`@MockitoBean`을 하나 추가하거나 `@ActiveProfiles`를 바꾸면 **새 컨텍스트가 뜬다**.
테스트가 느려질 때 가장 먼저 의심할 자리다.

💡 **실패하면 `andDo(print())`를 먼저 붙일 것.** 요청·응답 전체를 찍어 보는 것이
상태 코드만 보고 추측하는 것보다 훨씬 빠르다.

## 놓치기 쉬운 검증

성공 케이스만 있는 컨트롤러 테스트는 절반이다. 함께 볼 것:

- 검증 실패(400)와 그 **본문 모양**
- 없는 리소스(404)
- Content-Type 누락(415), 망가진 JSON(400)
- 타입이 안 맞는 경로 변수 → 컨트롤러에 **닿기도 전에** 400
- 실패 경로에서 **서비스가 불리지 않았는지**
