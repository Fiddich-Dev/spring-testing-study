package io.study.testing.ch01_junit5;

import java.time.Duration;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTimeout;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * <h2>01장 ① — JUnit 5 의 실행 규칙</h2>
 *
 * JUnit 5 는 세 조각이 합쳐진 이름이다. 셋을 구별해 두면 의존성 문제에서 덜 헤맨다.
 * <ul>
 *   <li><b>Jupiter</b> — 우리가 쓰는 {@code @Test}·{@code @Nested} 등의 API</li>
 *   <li><b>Platform</b> — 테스트를 실제로 찾아 실행하는 엔진 (Gradle·IDE 가 이걸 부른다)</li>
 *   <li><b>Vintage</b> — JUnit 4 테스트를 같은 엔진에서 돌려 주는 어댑터</li>
 * </ul>
 * {@code testRuntimeOnly("org.junit.platform:junit-platform-launcher")} 가
 * build.gradle.kts 에 있는 이유가 두 번째 조각 때문이다.
 */
@DisplayName("01장 ① JUnit 5 기본")
class JUnit5BasicsTest {

    private Calculator calculator;

    /**
     * ⚠ <b>기본 생명주기는 "테스트 메서드마다 인스턴스를 새로 만든다"(PER_METHOD)</b> 이다.
     *
     * <p>그래서 {@code @BeforeAll} 은 정적이어야 하고, 필드에 남긴 상태는 다음 테스트로
     * 넘어가지 않는다. 이 규칙이 테스트 간 격리를 <b>공짜로</b> 준다 — 아래
     * {@code 상태는_테스트마다_초기화된다} 가 그 사실을 직접 확인한다.
     */
    @BeforeAll
    static void beforeAll() {
        System.out.println("[BeforeAll] 클래스 전체에서 한 번");
    }

    @BeforeEach
    void setUp() {
        calculator = new Calculator();
    }

    @AfterEach
    void tearDown() {
        // 테스트마다 정리할 것이 있으면 여기. 파일·커넥션처럼 닫아야 하는 자원이 대표적이다.
    }

    @AfterAll
    static void afterAll() {
        System.out.println("[AfterAll] 클래스 전체에서 한 번");
    }

    @Test
    @DisplayName("@DisplayName 을 쓰면 리포트에 한국어 문장이 그대로 뜬다")
    void displayName_예시() {
        assertThat(calculator.add(2, 3)).isEqualTo(5);
    }

    @Test
    void 상태는_테스트마다_초기화된다_그하나() {
        calculator.remember(10);
        assertThat(calculator.memory()).isEqualTo(10);
    }

    @Test
    void 상태는_테스트마다_초기화된다_그둘() {
        // 앞 테스트가 10 을 넣었지만 여기서는 0 이다. 인스턴스가 새로 만들어졌기 때문이다.
        // 실행 순서에 상관없이 통과한다 — 그것이 요점이다.
        assertThat(calculator.memory()).isZero();
    }

    /**
     * 📌 <b>assertAll 은 "여러 단언을 끝까지 다 해보고" 실패를 모아서 보고한다.</b>
     *
     * <p>그냥 나열하면 첫 단언에서 멈춰서, 두 번째도 틀렸다는 사실을 다음 실행에나 알게 된다.
     * 고치고 → 돌리고 → 또 다른 실패를 보고 를 반복하게 되는 것이 그래서다.
     * AssertJ 에도 같은 목적의 {@code SoftAssertions} 가 있다 (02장).
     */
    @Test
    void assertAll_은_실패를_모아_보고한다() {
        assertAll("계산기",
                () -> assertThat(calculator.add(1, 2)).isEqualTo(3),
                () -> assertThat(calculator.divide(10, 2)).isEqualTo(5),
                () -> assertThat(calculator.isEven(4)).isTrue());
    }

    @Test
    void 예외는_assertThrows_로_잡는다() {
        ArithmeticException exc =
                assertThrows(ArithmeticException.class, () -> calculator.divide(1, 0));

        // assertThrows 는 잡은 예외를 **돌려준다**. 메시지까지 확인하는 것이 좋다 —
        // 타입만 보면 엉뚱한 이유로 같은 타입이 터져도 통과한다.
        assertThat(exc).hasMessage("0 으로 나눌 수 없다");
    }

    @Test
    void 같은_것을_AssertJ_로_쓰면_이렇게_된다() {
        // 📌 취향이 아니라 정보량의 차이다. AssertJ 쪽은 타입·메시지·원인을
        // 한 체인에 이어 쓸 수 있고, 실패 메시지에 "무엇을 기대했는지"가 더 자세히 나온다.
        assertThatThrownBy(() -> calculator.divide(1, 0))
                .isInstanceOf(ArithmeticException.class)
                .hasMessageContaining("나눌 수 없다");
    }

    @Test
    void assertTimeout_은_느린_코드를_잡는다() {
        assertTimeout(Duration.ofMillis(500), () -> calculator.add(1, 1));
    }

    @RepeatedTest(value = 3, name = "{displayName} — {currentRepetition}/{totalRepetitions}")
    void 반복_실행() {
        assertThat(calculator.isEven(2)).isTrue();
    }

    @Test
    @Disabled("@Disabled 는 이유를 반드시 적는다. 이유 없는 비활성 테스트는 영원히 죽어 있는다")
    void 꺼진_테스트() {
        throw new AssertionError("실행되지 않는다");
    }

    @Test
    void assumeTrue_는_조건이_아니면_건너뛴다() {
        // ⚠ assertion 과 헷갈리기 쉽다.
        //   - assertX 가 틀리면 → **실패**(빨간불)
        //   - assumeX 가 틀리면 → **건너뜀**(회색). 실패가 아니다.
        // 환경에 따라 못 도는 테스트(OS·자격증명·도커)에 쓴다.
        //
        // 🔥 그래서 위험하기도 하다 — 조건이 영영 거짓이면 아무도 모르게 계속 건너뛴다.
        //    "CI 는 초록인데 실제로는 한 번도 안 돈 테스트"가 이렇게 생긴다.
        assumeTrue(System.getProperty("java.version") != null, "자바가 없으면 건너뛴다");
        assertThat(calculator.add(1, 1)).isEqualTo(2);
    }

    /**
     * 📌 <b>{@code @Nested} 는 "같은 전제를 공유하는 테스트"를 묶는다.</b>
     *
     * <p>바깥의 {@code @BeforeEach} 가 <b>먼저</b> 돌고 안쪽 것이 뒤따른다. 그래서
     * "로그인한 사용자가 / 관리자일 때 / 삭제하면" 같은 계층을 코드 구조로 표현할 수 있다.
     */
    @Nested
    @DisplayName("나눗셈은")
    class Divide {

        @Test
        @DisplayName("몫을 돌려준다")
        void 몫() {
            // 바깥 setUp() 이 이미 돌아서 calculator 가 준비돼 있다.
            assertThat(calculator.divide(7, 2)).isEqualTo(3);
        }

        @Nested
        @DisplayName("0 으로 나누면")
        class ByZero {

            @Test
            @DisplayName("ArithmeticException 을 던진다")
            void 예외() {
                assertThatThrownBy(() -> calculator.divide(1, 0))
                        .isInstanceOf(ArithmeticException.class);
            }
        }
    }

    /**
     * 📌 <b>{@code @TestInstance(PER_CLASS)}</b> 를 쓰면 인스턴스를 하나만 만든다.
     *
     * <p>그러면 {@code @BeforeAll} 이 <b>정적이 아니어도</b> 되고,
     * {@code @MethodSource} 의 팩터리 메서드도 인스턴스 메서드로 둘 수 있다(02장에서 다시 나온다).
     *
     * <p>⚠ 대신 테스트 간 상태가 <b>공유된다</b>. 아래처럼 실행 순서에 의존하는 테스트가
     * 만들어지기 쉬워, 얻는 편의보다 잃는 격리가 클 때가 많다. 기본(PER_METHOD)을 권한다.
     */
    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @DisplayName("PER_CLASS 생명주기")
    class PerClass {

        private final Calculator shared = new Calculator();

        @Test
        void 상태가_공유된다는_사실만_확인한다() {
            shared.remember(5);
            // 다른 테스트가 이 값을 이어받는다. 편해 보이지만 실행 순서에 묶이는 순간
            // "혼자 돌리면 통과, 전체로 돌리면 실패"가 시작된다.
            assertThat(shared.memory()).isPositive();
        }
    }

    /**
     * ⚠ <b>JUnit 4 습관 하나</b>: {@code Assertions.assertEquals(expected, actual)} 는
     * 기대값이 <b>앞</b>이다. 순서를 바꿔 써도 통과하지만 실패 메시지가 뒤집혀 나와
     * "expected: 5 but was: 3" 을 거꾸로 읽게 된다.
     *
     * <p>📌 AssertJ 는 {@code assertThat(actual).isEqualTo(expected)} 한 방향뿐이라
     * 이 실수 자체가 생기지 않는다. 이 레포가 AssertJ 를 기본으로 쓰는 이유 중 하나다.
     */
    @Test
    void JUnit_기본_단언과_AssertJ_의_인자_순서() {
        Assertions.assertEquals(5, calculator.add(2, 3));   // 기대값이 앞
        assertThat(calculator.add(2, 3)).isEqualTo(5);      // 실제값이 앞
    }
}
