package io.study.testing.ch05_json;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 05장 테스트 대상. JSON 계약에서 자주 사고가 나는 지점을 한 곳에 모아 두었다.
 *
 * <ul>
 *   <li>{@code @JsonProperty} 로 자바 필드명과 <b>다른</b> JSON 키</li>
 *   <li>{@code @JsonIgnore} 로 응답에서 빠지는 필드 — 비밀번호가 새는 사고가 여기서 난다</li>
 *   <li>{@code LocalDateTime} 직렬화 — 설정 없이는 <b>배열</b>로 나간다</li>
 *   <li>null 필드 — Boot 기본은 키를 <b>남긴다</b>(NON_NULL 이 아니다)</li>
 *   <li>중첩 객체와 리스트</li>
 * </ul>
 */
public record UserProfile(

        Long id,

        // 자바는 camelCase, JSON 은 snake_case 인 계약. 이 어긋남을 테스트로 못박지 않으면
        // 필드명을 리팩터링하는 순간 프론트가 조용히 깨진다.
        @JsonProperty("display_name")
        String displayName,

        String email,

        // 응답에 절대 나가면 안 되는 것. "안 나가는지"를 테스트로 확인하는 것이 05장의 요점 하나다.
        @JsonIgnore
        String passwordHash,

        // 이게 없으면 Boot 는 [2026,8,17,10,0] 같은 **배열**로 내보낸다.
        // JavaTimeModule 이 등록돼 있어도 WRITE_DATES_AS_TIMESTAMPS 가 켜져 있으면 그렇다.
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime createdAt,

        LocalDate birthday,

        // null 일 수 있는 필드. 기본 설정에서는 "nickname": null 로 키가 남는다.
        String nickname,

        Address address,

        List<String> roles) {

    public record Address(String city, String street, @JsonProperty("zip_code") String zipCode) {
    }
}
