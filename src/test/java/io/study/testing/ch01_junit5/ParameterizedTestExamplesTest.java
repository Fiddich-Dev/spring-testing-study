package io.study.testing.ch01_junit5;

import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;

/**
 * <h2>01장 ② — 같은 검증을 여러 입력으로</h2>
 *
 * 📌 <b>언제 쓰나</b>: 검증 <b>논리는 같고</b> 입력만 다를 때. 논리가 갈리면
 * (성공 케이스 / 예외 케이스) 파라미터로 묶지 말고 테스트를 나눈다 — 한 메서드 안에
 * {@code if} 가 생기는 순간 무엇을 검증하는지 읽히지 않는다.
 *
 * <p>🔥 <b>가장 큰 이득은 실패했을 때다.</b> for 문을 돌면 어느 입력에서 깨졌는지
 * 스택만 보고는 모르지만, 파라미터 테스트는 실패한 케이스가 이름째 리포트에 뜬다.
 */
@DisplayName("01장 ② 파라미터 테스트")
class ParameterizedTestExamplesTest {

    private Calculator calculator;

    @BeforeEach
    void setUp() {
        calculator = new Calculator();
    }

    /**
     * 가장 단순한 형태. 값 하나짜리 목록.
     *
     * <p>⚠ {@code @ValueSource} 는 <b>기본형·String·Class 만</b> 받는다.
     * 객체를 넘기려면 {@code @MethodSource} 로 간다.
     */
    @ParameterizedTest(name = "{0} 은 짝수다")
    @ValueSource(ints = {2, 4, 100, 0, -2})
    void 짝수_판정(int value) {
        assertThat(calculator.isEven(value)).isTrue();
    }

    /**
     * 입력이 여러 개일 때. {@code {0}}·{@code {1}} 로 리포트 이름에 값을 박을 수 있다.
     *
     * <p>⚠ 문자열에 쉼표가 들어가야 하면 {@code delimiter} 를 바꾸거나 작은따옴표로 감싼다.
     */
    @ParameterizedTest(name = "{0} + {1} = {2}")
    @CsvSource({
            "1, 2, 3",
            "0, 0, 0",
            "-1, 1, 0",
            "2147483647, 0, 2147483647"
    })
    void 덧셈(int a, int b, int expected) {
        assertThat(calculator.add(a, b)).isEqualTo(expected);
    }

    /**
     * {@code @CsvSource} 는 빈 문자열과 null 을 구분해서 표기해야 한다.
     * 아무것도 안 쓰면 {@code null}, 작은따옴표 두 개면 빈 문자열이다.
     */
    @ParameterizedTest(name = "[{index}] 입력=\"{0}\" → 비었나={1}")
    @CsvSource(value = {
            "hello, false",
            "'', true",
            "NIL, true"
    }, nullValues = "NIL")
    void 빈값_판정(String input, boolean expectedBlank) {
        boolean actual = input == null || input.isBlank();
        assertThat(actual).isEqualTo(expectedBlank);
    }

    /**
     * 📌 <b>{@code @MethodSource} 가 가장 자주 쓰인다</b> — 객체를 넘길 수 있어서다.
     *
     * <p>⚠ 팩터리 메서드는 <b>static</b> 이어야 한다(클래스에 {@code @TestInstance(PER_CLASS)}
     * 를 걸면 인스턴스 메서드도 된다 — 01장 ① 참고). 이름을 생략하면 테스트 메서드와
     * <b>같은 이름</b>의 메서드를 찾는다.
     */
    @ParameterizedTest(name = "{0}")
    @MethodSource("나눗셈_케이스")
    void 나눗셈(String 설명, int a, int b, int expected) {
        assertThat(calculator.divide(a, b)).isEqualTo(expected);
    }

    static Stream<Arguments> 나눗셈_케이스() {
        return Stream.of(
                arguments("나누어떨어짐", 10, 2, 5),
                arguments("몫만 남음", 7, 2, 3),
                arguments("음수", -9, 3, -3),
                arguments("0을 나눔", 0, 5, 0));
    }

    /**
     * enum 을 전부 훑는다. <b>새 상수가 추가되면 자동으로 케이스가 늘어난다</b> —
     * 상태 머신·응답 코드처럼 "빠짐없이" 가 중요한 곳에서 값어치가 크다.
     */
    @ParameterizedTest
    @EnumSource(Season.class)
    void 모든_계절은_이름이_있다(Season season) {
        assertThat(season.name()).isNotBlank();
    }

    @ParameterizedTest
    @EnumSource(value = Season.class, names = {"SPRING", "AUTUMN"})
    void 일부만_고른다(Season season) {
        assertThat(season).isIn(Season.SPRING, Season.AUTUMN);
    }

    @ParameterizedTest
    @EnumSource(value = Season.class, mode = EnumSource.Mode.EXCLUDE, names = "WINTER")
    void 하나만_뺀다(Season season) {
        assertThat(season).isNotEqualTo(Season.WINTER);
    }

    /**
     * 📌 <b>경계값을 빠뜨리지 않게 해 주는 어노테이션 셋.</b>
     * null 과 빈 문자열은 실무에서 가장 자주 터지는 입력인데 가장 자주 잊힌다.
     */
    @ParameterizedTest(name = "입력=[{0}]")
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\t", "\n"})
    void 공백_취급되는_입력들(String input) {
        boolean blank = input == null || input.isBlank();
        assertThat(blank).isTrue();
    }

    @ParameterizedTest
    @NullSource
    void null만(String input) {
        assertThat(input).isNull();
    }

    @ParameterizedTest
    @EmptySource
    void 빈것만(String input) {
        assertThat(input).isEmpty();
    }

    enum Season {
        SPRING, SUMMER, AUTUMN, WINTER
    }
}
