package io.study.testing.ch05_json.exercise;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;

import io.study.testing.ch05_json.UserProfile;

/**
 * <h2>05장 연습문제</h2>
 *
 * <pre>
 *   ./gradlew exercise --tests '*JsonExercise'
 * </pre>
 *
 * 답은 {@code ch05_json/JsonSerializationTest} 에 있다.
 */
@Tag("exercise")
@JsonTest
@DisplayName("05장 연습문제")
class JsonExercise {

    @Autowired
    private JacksonTester<UserProfile> json;

    private UserProfile sample() {
        return new UserProfile(
                1L, "철수", "chulsoo@example.com", "해시된비밀번호",
                LocalDateTime.of(2026, 8, 17, 10, 30, 0),
                LocalDate.of(1995, 3, 15),
                null,
                new UserProfile.Address("서울", "테헤란로 1", "06234"),
                List.of("USER", "ADMIN"));
    }

    /**
     * 문제 1. {@code displayName} 이 JSON 에서 <b>{@code display_name}</b> 키로 나가는지,
     * 그리고 <b>{@code displayName} 키는 없는지</b> 둘 다 검증하라.
     *
     * <p>🔥 뒤쪽(없음 검증)이 진짜 방어선이다. 있는 것만 확인하면
     * 두 키가 <b>둘 다</b> 나가는 상황을 못 잡는다.
     */
    @Test
    void 문제1_snake_case_키() throws Exception {
        // TODO: hasJsonPath / doesNotHaveJsonPath
        throw new AssertionError("TODO: 문제 1 을 풀어라");
    }

    /**
     * 문제 2. {@code passwordHash} 가 응답에 <b>어떤 형태로도</b> 나가지 않는지 검증하라.
     *
     * <p>키 이름 두 가지({@code passwordHash}, {@code password_hash})와
     * <b>값 문자열 자체</b>가 JSON 어디에도 없는지 확인한다.
     */
    @Test
    void 문제2_비밀번호_유출_방지() throws Exception {
        // TODO
        throw new AssertionError("TODO: 문제 2 를 풀어라");
    }

    /**
     * 문제 3. {@code createdAt} 이 {@code "2026-08-17T10:30:00"} 문자열로 나가는지 검증하라.
     *
     * <p>💡 다 푼 뒤 {@code UserProfile} 의 {@code @JsonFormat} 을 지워 보고
     * 무엇이 나오는지 직접 확인할 것 — 그것이 이 문제의 진짜 목적이다. 확인 후 원복한다.
     */
    @Test
    void 문제3_날짜_형식() throws Exception {
        // TODO
        throw new AssertionError("TODO: 문제 3 을 풀어라");
    }

    /**
     * 문제 4. {@code nickname} 이 null 일 때 <b>키가 남는지</b> 검증하라.
     *
     * <p>💡 그 다음 {@code application.properties} 의
     * {@code spring.jackson.default-property-inclusion} 을 {@code non_null} 로 바꿔 보고
     * 이 테스트가 깨지는 것을 확인할 것. 확인 후 원복한다.
     */
    @Test
    void 문제4_null_필드_처리() throws Exception {
        // TODO
        throw new AssertionError("TODO: 문제 4 를 풀어라");
    }

    /**
     * 문제 5. 아래 JSON 문자열을 {@code UserProfile} 로 역직렬화하고
     * {@code displayName}, {@code address.zipCode}, {@code roles} 를 검증하라.
     */
    @Test
    void 문제5_역직렬화() throws Exception {
        String content = """
                {
                  "id": 7,
                  "display_name": "영희",
                  "email": "younghee@example.com",
                  "createdAt": "2026-01-01T00:00:00",
                  "birthday": "2000-12-25",
                  "address": {"city": "부산", "street": "해운대로 2", "zip_code": "48094"},
                  "roles": ["USER"]
                }
                """;

        // TODO: json.parseObject(content) 로 파싱하고 필드를 검증하라.
        throw new AssertionError("TODO: 문제 5 를 풀어라");
    }

    /**
     * 문제 6. 위 JSON 에 {@code "모르는키": "값"} 을 추가해도
     * 파싱이 <b>실패하지 않는지</b> 검증하라.
     *
     * <p>💡 Spring Boot 의 기본값이 무엇인지 확인하는 문제다.
     */
    @Test
    void 문제6_모르는_키() throws Exception {
        // TODO
        throw new AssertionError("TODO: 문제 6 을 풀어라");
    }
}
