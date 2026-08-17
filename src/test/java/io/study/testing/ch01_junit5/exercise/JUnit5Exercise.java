package io.study.testing.ch01_junit5.exercise;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import io.study.testing.ch01_junit5.Calculator;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * <h2>01장 연습문제</h2>
 *
 * <b>이 파일은 처음에 실패한다. 그것이 정상이다.</b>
 *
 * <pre>
 *   ./gradlew exercise --tests '*JUnit5Exercise'
 * </pre>
 *
 * TODO 를 채워 초록으로 만든다. 막히면 같은 장의 완성본
 * ({@code ch01_junit5/JUnit5BasicsTest}, {@code ParameterizedTestExamplesTest})에 답이 있다.
 *
 * <p>{@code @Tag("exercise")} 때문에 {@code ./gradlew test} 에서는 제외된다 —
 * 연습문제가 CI 를 빨갛게 만들면 "깨진 게 정상"인 상태에 익숙해지기 때문이다.
 */
@Tag("exercise")
@DisplayName("01장 연습문제")
class JUnit5Exercise {

    private final Calculator calculator = new Calculator();

    /**
     * 문제 1. 각 테스트마다 {@code calculator} 를 새로 만들도록 고쳐라.
     *
     * <p>지금은 필드 초기화라 PER_METHOD 생명주기 덕에 우연히 동작한다.
     * 명시적인 {@code @BeforeEach} 로 바꾸고, 아래 두 테스트가 <b>순서에 상관없이</b>
     * 통과하는지 확인하라.
     */
    @Test
    void 문제1_상태가_남지_않는다_하나() {
        calculator.remember(7);
        assertThat(calculator.memory()).isEqualTo(7);
    }

    @Test
    void 문제1_상태가_남지_않는다_둘() {
        assertThat(calculator.memory()).isZero();
    }

    /**
     * 문제 2. {@code divide(1, 0)} 이 {@code ArithmeticException} 을 던지고
     * 메시지가 "나눌 수 없다" 를 포함하는지 검증하라.
     *
     * <p>{@code assertThatThrownBy} 또는 {@code assertThrows} 중 하나를 쓴다.
     */
    @Test
    void 문제2_예외_검증() {
        // TODO: 여기에 예외 검증을 작성하라.
        throw new AssertionError("TODO: 문제 2 를 풀어라");
    }

    /**
     * 문제 3. 아래 세 단언이 <b>모두</b> 실행되어 실패를 한 번에 보고하도록 고쳐라.
     *
     * <p>지금은 첫 줄에서 멈춘다. {@code assertAll} 을 쓴다.
     * (지금 상태로는 통과하지만, 일부러 값을 틀리게 바꿔 보고 차이를 눈으로 확인할 것)
     */
    @Test
    void 문제3_실패를_모아_보고하라() {
        assertThat(calculator.add(1, 2)).isEqualTo(3);
        assertThat(calculator.divide(9, 3)).isEqualTo(3);
        assertThat(calculator.isEven(3)).isFalse();
        // TODO: 위 세 줄을 assertAll 로 묶어라.
        throw new AssertionError("TODO: 문제 3 을 풀어라");
    }

    /**
     * 문제 4. 이 테스트를 {@code @ParameterizedTest} 로 바꿔라.
     *
     * <p>입력 {@code 2, 4, 0, -2} 에 대해 {@code isEven} 이 참인지 검증한다.
     * {@code @ValueSource(ints = {...})} 를 쓰고, 메서드가 {@code int} 파라미터를 받도록 고친다.
     */
    @Test
    void 문제4_파라미터_테스트로_바꿔라() {
        // TODO: @Test 를 @ParameterizedTest + @ValueSource 로 바꾸고
        //       아래 반복문을 파라미터 하나짜리 단언으로 만들어라.
        for (int value : new int[] {2, 4, 0, -2}) {
            assertThat(calculator.isEven(value)).isTrue();
        }
        throw new AssertionError("TODO: 문제 4 를 풀어라");
    }

    /**
     * 문제 5. "0 으로 나누면 예외" 라는 전제를 {@code @Nested} 클래스로 묶고,
     * {@code @DisplayName} 으로 "0 으로 나누면" 이라고 표시하라.
     *
     * <p>중첩 클래스 안에 테스트를 최소 하나 두어야 한다.
     */
    @Test
    void 문제5_Nested_로_묶어라() {
        // TODO: @Nested 중첩 클래스를 만들고 이 테스트를 그 안으로 옮겨라.
        throw new AssertionError("TODO: 문제 5 를 풀어라");
    }
}
