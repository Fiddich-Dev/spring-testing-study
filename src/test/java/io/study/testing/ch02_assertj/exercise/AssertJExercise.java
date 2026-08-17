package io.study.testing.ch02_assertj.exercise;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import io.study.testing.ch02_assertj.Member;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * <h2>02장 연습문제</h2>
 *
 * <pre>
 *   ./gradlew exercise --tests '*AssertJExercise'
 * </pre>
 *
 * 답은 {@code ch02_assertj/AssertJBasicsTest}, {@code AssertJAdvancedTest} 에 있다.
 */
@Tag("exercise")
@DisplayName("02장 연습문제")
class AssertJExercise {

    private final List<Member> members = List.of(
            Member.of("김철수", 30, "백엔드"),
            Member.of("이영희", 25, "프론트엔드"),
            Member.of("박민수", 17, "백엔드"),
            Member.of("최지은", 41, "인프라"));

    /**
     * 문제 1. 아래 반복문을 {@code extracting} 한 줄로 바꿔라.
     *
     * <p>검증 내용: 회원 이름이 정확히 이 순서로 넷이다.
     */
    @Test
    void 문제1_extracting_으로_바꿔라() {
        // TODO: extracting(Member::getName).containsExactly(...) 로 대체하라.
        for (Member member : members) {
            assertThat(member.getName()).isNotBlank();
        }
        throw new AssertionError("TODO: 문제 1 을 풀어라");
    }

    /**
     * 문제 2. "백엔드 팀 회원의 이름은 김철수와 박민수다" 를 검증하라.
     *
     * <p>{@code filteredOn} + {@code extracting} 을 조합한다.
     */
    @Test
    void 문제2_filteredOn_과_extracting_을_조합하라() {
        // TODO
        throw new AssertionError("TODO: 문제 2 를 풀어라");
    }

    /**
     * 문제 3. 아래 두 객체가 <b>내용상 같음</b>을 검증하라.
     *
     * <p>⚠ {@code Member} 에는 equals 가 없다. {@code isEqualTo} 로는 실패한다.
     */
    @Test
    void 문제3_equals_없이_비교하라() {
        Member a = new Member("김철수", 30, "백엔드", LocalDate.of(2026, 1, 1));
        Member b = new Member("김철수", 30, "백엔드", LocalDate.of(2026, 1, 1));

        // TODO: usingRecursiveComparison 을 써라.
        throw new AssertionError("TODO: 문제 3 을 풀어라");
    }

    /**
     * 문제 4. {@code joinedAt} 만 다른 두 객체를 "그 필드를 빼고" 비교하라.
     */
    @Test
    void 문제4_일부_필드를_빼고_비교하라() {
        Member expected = new Member("이영희", 25, "프론트엔드", null);
        Member actual = new Member("이영희", 25, "프론트엔드", LocalDate.now());

        // TODO: ignoringFields 를 써라.
        throw new AssertionError("TODO: 문제 4 를 풀어라");
    }

    /**
     * 문제 5. 아래 네 단언이 <b>전부 실행되어</b> 실패를 한 번에 보고하도록 고쳐라.
     *
     * <p>{@code SoftAssertions} 를 쓴다. 다 고친 뒤, 일부러 값을 하나 틀리게 바꿔
     * <b>실패 메시지에 몇 개가 뜨는지</b> 확인할 것 — 그것이 이 문제의 핵심이다.
     */
    @Test
    void 문제5_SoftAssertions_로_바꿔라() {
        Member member = members.get(0);

        assertThat(member.getName()).isEqualTo("김철수");
        assertThat(member.getAge()).isEqualTo(30);
        assertThat(member.getTeam()).isEqualTo("백엔드");
        assertThat(member.isAdult()).isTrue();

        // TODO: 위 넷을 SoftAssertions 로 묶고 assertAll() 을 부르라.
        throw new AssertionError("TODO: 문제 5 를 풀어라");
    }

    /**
     * 문제 6. {@code 0.1 + 0.2} 가 {@code 0.3} 인지 검증하라.
     *
     * <p>🔥 {@code isEqualTo(0.3)} 은 실패한다. 왜 실패하는지 실행해서 메시지를 직접 볼 것.
     */
    @Test
    void 문제6_부동소수점을_비교하라() {
        double result = 0.1 + 0.2;

        // TODO: isCloseTo + within 을 써라.
        throw new AssertionError("TODO: 문제 6 을 풀어라");
    }
}
