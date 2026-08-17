package io.study.testing.ch05_json;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.json.JSONException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * <h2>05장 — JSON 직렬화 테스트</h2>
 *
 * 🔥 <b>왜 따로 테스트하나.</b> JSON 계약이 깨지는 사고는 <b>컴파일 타임에 안 잡힌다.</b>
 * 필드명을 리팩터링하거나 {@code @JsonProperty} 를 지워도 자바는 아무 말이 없고,
 * 프론트만 조용히 깨진다. 그 어긋남을 잡는 것이 이 장의 테스트다.
 *
 * <p>📌 <b>{@code @JsonTest} 가 세우는 것</b>: {@code ObjectMapper} 와 Jackson 설정
 * ({@code @JsonComponent}, 모듈, {@code spring.jackson.*} 프로퍼티)만. 웹도 서비스도 없다.
 * {@code @WebMvcTest} 보다도 가볍다.
 */
@JsonTest
@DisplayName("05장 JSON 직렬화")
class JsonSerializationTest {

    /**
     * 📌 {@code JacksonTester} 는 {@code @JsonTest} 가 자동으로 주입해 준다.
     * 제네릭 타입까지 알고 있어서 {@code ObjectMapper} 를 직접 쓰는 것보다 단언이 풍부하다.
     */
    @Autowired
    private JacksonTester<UserProfile> json;

    @Autowired
    private ObjectMapper objectMapper;

    private UserProfile sample() {
        return new UserProfile(
                1L,
                "철수",
                "chulsoo@example.com",
                "해시된비밀번호",
                LocalDateTime.of(2026, 8, 17, 10, 30, 0),
                LocalDate.of(1995, 3, 15),
                null,                                   // nickname 이 null 이다
                new UserProfile.Address("서울", "테헤란로 1", "06234"),
                List.of("USER", "ADMIN"));
    }

    @Nested
    @DisplayName("직렬화 (객체 → JSON)")
    class Serialize {

        @Test
        void JacksonTester로_필드를_짚는다() throws Exception {
            var result = json.write(sample());

            assertThat(result)
                    .hasJsonPathNumberValue("$.id")
                    .extractingJsonPathNumberValue("$.id").isEqualTo(1);

            assertThat(result).extractingJsonPathStringValue("$.email")
                    .isEqualTo("chulsoo@example.com");
        }

        /**
         * 🔥 <b>{@code @JsonProperty} 를 테스트로 못박는 이유.</b>
         *
         * <p>자바 필드는 {@code displayName} 인데 JSON 키는 {@code display_name} 이다.
         * 이 어노테이션을 실수로 지우면 키가 {@code displayName} 으로 바뀌어 나가고,
         * <b>자바 쪽은 아무 문제 없이 컴파일된다.</b> 프론트만 깨진다.
         */
        @Test
        void snake_case_키를_확인한다() throws Exception {
            var result = json.write(sample());

            assertThat(result).hasJsonPath("$.display_name");
            assertThat(result).doesNotHaveJsonPath("$.displayName");   // ← 이 줄이 진짜 방어선
            assertThat(result).extractingJsonPathStringValue("$.display_name").isEqualTo("철수");

            assertThat(result).hasJsonPath("$.address.zip_code");
            assertThat(result).doesNotHaveJsonPath("$.address.zipCode");
        }

        /**
         * 🔥 <b>가장 값어치 있는 테스트 하나를 고르라면 이것이다.</b>
         * 비밀번호·주민번호처럼 <b>나가면 안 되는</b> 필드가 응답에 없는지 확인한다.
         *
         * <p>{@code @JsonIgnore} 를 누군가 지우거나, DTO 대신 엔티티를 그대로 반환하도록
         * 바꾸는 순간 이 테스트가 빨간불이 된다.
         */
        @Test
        void 비밀번호는_응답에_없다() throws Exception {
            var result = json.write(sample());

            assertThat(result).doesNotHaveJsonPath("$.passwordHash");
            assertThat(result).doesNotHaveJsonPath("$.password_hash");
            // 원문이 어딘가에 섞여 나가는 것도 막는다
            assertThat(result.getJson()).doesNotContain("해시된비밀번호");
        }

        /**
         * ⚠ <b>날짜는 설정 없이는 배열로 나간다.</b>
         * {@code [2026,8,17,10,30]} 같은 모양이다. {@code @JsonFormat} 이나
         * {@code write-dates-as-timestamps=false} 가 그것을 막는다.
         */
        @Test
        void 날짜는_ISO_문자열이다() throws Exception {
            var result = json.write(sample());

            assertThat(result).extractingJsonPathStringValue("$.createdAt")
                    .isEqualTo("2026-08-17T10:30:00");
            assertThat(result).extractingJsonPathStringValue("$.birthday")
                    .isEqualTo("1995-03-15");
        }

        /**
         * 🔥 <b>Boot 의 기본은 {@code NON_NULL} 이 아니다.</b>
         * null 필드도 키째 나간다 ({@code "nickname": null}).
         *
         * <p>이걸 모르고 프론트가 "키가 없으면 미설정" 으로 구현하면 어긋난다.
         * 어느 쪽이든 <b>테스트로 못박아 두는 것</b>이 중요하다 — 설정 한 줄로 전역이 바뀌는 값이라서다.
         */
        @Test
        void null_필드도_키가_남는다() throws Exception {
            var result = json.write(sample());

            assertThat(result).hasJsonPath("$.nickname");
            assertThat(result.getJson()).contains("\"nickname\":null");
        }

        @Test
        void 중첩과_배열() throws Exception {
            var result = json.write(sample());

            assertThat(result).extractingJsonPathStringValue("$.address.city").isEqualTo("서울");
            assertThat(result).extractingJsonPathArrayValue("$.roles")
                    .containsExactly("USER", "ADMIN");
            assertThat(result).extractingJsonPathNumberValue("$.roles.length()").isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("역직렬화 (JSON → 객체)")
    class Deserialize {

        @Test
        void 기본() throws Exception {
            String content = """
                    {
                      "id": 1,
                      "display_name": "철수",
                      "email": "chulsoo@example.com",
                      "createdAt": "2026-08-17T10:30:00",
                      "birthday": "1995-03-15",
                      "nickname": null,
                      "address": {"city": "서울", "street": "테헤란로 1", "zip_code": "06234"},
                      "roles": ["USER", "ADMIN"]
                    }
                    """;

            UserProfile parsed = json.parseObject(content);

            assertThat(parsed.id()).isEqualTo(1L);
            assertThat(parsed.displayName()).isEqualTo("철수");
            assertThat(parsed.createdAt()).isEqualTo(LocalDateTime.of(2026, 8, 17, 10, 30));
            assertThat(parsed.address().zipCode()).isEqualTo("06234");
            assertThat(parsed.roles()).containsExactly("USER", "ADMIN");
        }

        /**
         * ⚠ <b>모르는 키가 오면 기본은 "무시" 가 아니다 — 터진다.</b>
         * Spring Boot 는 {@code FAIL_ON_UNKNOWN_PROPERTIES} 를 꺼 두므로 <b>무시된다.</b>
         *
         * <p>🔥 이 기본값을 아는 것이 중요하다. 엄격하게 가려면 켜야 하고
         * ({@code spring.jackson.deserialization.fail-on-unknown-properties=true}),
         * 그러면 클라이언트가 필드를 하나 더 보내는 순간 400 이 된다. 트레이드오프다.
         */
        @Test
        void 모르는_키는_기본적으로_무시된다() throws Exception {
            String content = """
                    {"id": 1, "display_name": "철수", "이런키는없다": "무시된다"}
                    """;

            UserProfile parsed = json.parseObject(content);

            assertThat(parsed.id()).isEqualTo(1L);
            assertThat(parsed.displayName()).isEqualTo("철수");
        }

        /**
         * 📌 {@code @JsonIgnore} 는 <b>읽기·쓰기 양쪽</b>을 막는다.
         * 그래서 JSON 에 값이 있어도 필드는 null 로 남는다 — 비밀번호가 외부에서
         * 주입되지 않는다는 뜻이라 보안상 바람직하다.
         */
        @Test
        void JsonIgnore_필드는_읽지도_않는다() throws Exception {
            String content = """
                    {"id": 1, "passwordHash": "외부에서주입시도"}
                    """;

            UserProfile parsed = json.parseObject(content);

            assertThat(parsed.passwordHash()).isNull();
        }
    }

    @Nested
    @DisplayName("JSONassert — 문자열 전체를 비교")
    class JsonAssertUsage {

        /**
         * 📌 <b>{@code LENIENT} 가 기본이자 권장</b> — 필드 순서를 무시하고,
         * 실제 JSON 에 <b>추가 필드가 있어도</b> 통과한다.
         *
         * <p>🔥 {@code STRICT} 는 "정확히 이 필드들만" 을 요구한다. 계약을 못박을 때는
         * 강력하지만, 필드를 하나 추가할 때마다 테스트를 고쳐야 해서 관리 비용이 크다.
         * <b>응답 스펙을 고정해야 하는 공개 API</b> 정도에 쓴다.
         */
        @Test
        void LENIENT는_추가_필드를_허용한다() throws JSONException {
            String actual = """
                    {"id": 1, "name": "철수", "extra": "추가된 필드"}
                    """;
            String expected = """
                    {"name": "철수", "id": 1}
                    """;

            JSONAssert.assertEquals(expected, actual, JSONCompareMode.LENIENT);
        }

        @Test
        void STRICT는_추가_필드를_거부한다() {
            String actual = """
                    {"id": 1, "name": "철수", "extra": "추가된 필드"}
                    """;
            String expected = """
                    {"id": 1, "name": "철수"}
                    """;

            org.assertj.core.api.Assertions
                    .assertThatThrownBy(() -> JSONAssert.assertEquals(expected, actual, JSONCompareMode.STRICT))
                    .isInstanceOf(AssertionError.class);
        }

        @Test
        void 배열_순서도_모드에_따라_다르다() throws JSONException {
            String actual = """
                    {"roles": ["ADMIN", "USER"]}
                    """;
            String expected = """
                    {"roles": ["USER", "ADMIN"]}
                    """;

            // LENIENT 는 배열 순서를 무시한다
            JSONAssert.assertEquals(expected, actual, JSONCompareMode.LENIENT);

            // STRICT_ORDER 는 순서까지 본다
            org.assertj.core.api.Assertions
                    .assertThatThrownBy(() -> JSONAssert.assertEquals(expected, actual, JSONCompareMode.STRICT))
                    .isInstanceOf(AssertionError.class);
        }
    }

    @Nested
    @DisplayName("JsonPath — 문자열에서 값만 꺼내기")
    class JsonPathUsage {

        /**
         * 📌 MockMvc 의 {@code jsonPath(...)} 가 쓰는 것과 <b>같은 라이브러리</b>다.
         * 문자열만 있고 타입이 없을 때(외부 API 응답 등) 유용하다.
         */
        @Test
        void 기본_문법() throws Exception {
            String content = objectMapper.writeValueAsString(sample());

            assertThat((int) JsonPath.read(content, "$.id")).isEqualTo(1);
            assertThat((String) JsonPath.read(content, "$.display_name")).isEqualTo("철수");
            assertThat((String) JsonPath.read(content, "$.address.city")).isEqualTo("서울");
            assertThat((List<String>) JsonPath.read(content, "$.roles"))
                    .containsExactly("USER", "ADMIN");
            assertThat((int) JsonPath.read(content, "$.roles.length()")).isEqualTo(2);
        }

        @Test
        void 배열_필터링() {
            String content = """
                    {"items": [
                      {"name": "A", "price": 1000},
                      {"name": "B", "price": 3000},
                      {"name": "C", "price": 500}
                    ]}
                    """;

            // $..price      → 깊이 무관하게 모든 price
            // $.items[?(@.price > 900)]  → 조건 필터
            assertThat((List<Integer>) JsonPath.read(content, "$..price"))
                    .containsExactly(1000, 3000, 500);
            assertThat((List<String>) JsonPath.read(content, "$.items[?(@.price > 900)].name"))
                    .containsExactly("A", "B");
            assertThat((String) JsonPath.read(content, "$.items[-1].name")).isEqualTo("C");
        }
    }
}
