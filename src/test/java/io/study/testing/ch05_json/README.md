# 05장 — JSON 직렬화 테스트

레포 루트에서: `./gradlew test --tests '*ch05_json*'`

## 파일

| 파일 | 내용 |
|---|---|
| `JsonSerializationTest` | `@JsonTest`, JacksonTester, 직렬화/역직렬화, JSONassert, JsonPath |
| `exercise/JsonExercise` | 연습문제 6개 |

## 왜 따로 테스트하나

🔥 **JSON 계약이 깨지는 사고는 컴파일 타임에 안 잡힌다.**
필드명을 리팩터링하거나 `@JsonProperty`를 지워도 자바는 아무 말이 없고,
프론트만 조용히 깨진다.

📌 **`@JsonTest`가 세우는 것**: `ObjectMapper`와 Jackson 설정
(`@JsonComponent`, 모듈, `spring.jackson.*`)만. 웹도 서비스도 없다.
`@WebMvcTest`보다도 가볍다.

## 가장 값어치 있는 테스트 셋

1. **비밀번호가 응답에 없는지** — `@JsonIgnore`를 누군가 지우거나,
   DTO 대신 엔티티를 그대로 반환하도록 바꾸는 순간 빨간불이 된다
2. **`@JsonProperty` 키 이름** — 있는 것만이 아니라 **없어야 할 키가 없는지**도 본다.
   앞만 확인하면 두 키가 **둘 다** 나가는 상황을 못 잡는다
3. **null 필드 처리** — 설정 한 줄로 전역이 바뀌는 값이라 못박아 둘 값어치가 있다

## Spring Boot의 기본값

| 설정 | 기본 | 뜻 |
|---|---|---|
| `default-property-inclusion` | `always` | **null 필드도 키째 나간다** (`"nickname": null`) |
| `write-dates-as-timestamps` | `false` | 날짜가 ISO 문자열로 (Boot가 꺼 준다) |
| `fail-on-unknown-properties` | `false` | **모르는 키는 무시된다** (터지지 않는다) |

⚠ 셋 다 "당연히 그럴 것 같은" 값과 어긋나기 쉽다.
특히 첫 번째 — 프론트가 "키가 없으면 미설정"으로 구현하면 계약이 어긋난다.

⚠ **`@JsonFormat` 없이는 `LocalDateTime`이 배열로 나갈 수 있다** (`[2026,8,17,10,30]`).

📌 **`@JsonIgnore`는 읽기·쓰기 양쪽을 막는다.** JSON에 값이 있어도 필드는 null로 남는다 —
비밀번호가 외부에서 주입되지 않는다는 뜻이라 보안상 바람직하다.

## JSONassert 비교 모드

| 모드 | 필드 순서 | 추가 필드 | 배열 순서 |
|---|---|---|---|
| `LENIENT` (권장) | 무시 | **허용** | 무시 |
| `STRICT` | 무시 | **거부** | 본다 |

🔥 `STRICT`는 계약을 못박을 때 강력하지만, 필드를 하나 추가할 때마다
테스트를 고쳐야 한다. **응답 스펙을 고정해야 하는 공개 API** 정도에 쓴다.

## JsonPath 문법

MockMvc의 `jsonPath(...)`가 쓰는 것과 **같은 라이브러리**다.

```
$.id                        루트의 id
$.address.city              중첩
$.roles[0]                  배열 인덱스
$.items[-1]                 마지막 원소
$.roles.length()            길이
$..price                    깊이 무관하게 모든 price
$.items[?(@.price > 900)]   조건 필터
```
