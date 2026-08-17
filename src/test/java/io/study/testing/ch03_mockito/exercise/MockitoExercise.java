package io.study.testing.ch03_mockito.exercise;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import io.study.testing.ch03_mockito.OrderRepository;
import io.study.testing.ch03_mockito.OrderService;
import io.study.testing.ch03_mockito.PaymentGateway;

/**
 * <h2>03장 연습문제</h2>
 *
 * <pre>
 *   ./gradlew exercise --tests '*MockitoExercise'
 * </pre>
 *
 * 답은 {@code ch03_mockito/MockitoBasicsTest}, {@code MockitoAdvancedTest} 에 있다.
 */
@Tag("exercise")
@ExtendWith(MockitoExtension.class)
@DisplayName("03장 연습문제")
class MockitoExercise {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private PaymentGateway paymentGateway;

    // TODO: 문제 0. @InjectMocks 를 붙여 orderService 를 만들어라.
    private OrderService orderService;

    /**
     * 문제 1. 주문 "ORD-1"(고객 "CUST-1", 10,000원)이 결제되면
     * 결과 상태가 {@code PAID} 인지 검증하라.
     *
     * <p>스터빙할 것 셋:
     * <ul>
     *   <li>{@code orderRepository.findById("ORD-1")} → 그 주문</li>
     *   <li>{@code paymentGateway.charge(...)} → 승인번호 문자열</li>
     *   <li>{@code orderRepository.save(...)} → 넘어온 인자를 그대로 (thenAnswer)</li>
     * </ul>
     */
    @Test
    void 문제1_결제_성공_경로() {
        // TODO
        throw new AssertionError("TODO: 문제 1 을 풀어라");
    }

    /**
     * 문제 2. 주문을 찾지 못하면 <b>결제사를 한 번도 부르지 않는지</b> 검증하라.
     *
     * <p>💡 {@code verify(mock, never())} 또는 {@code verifyNoInteractions(mock)}.
     * 🔥 반환값만 봐서는 절대 알 수 없는 종류의 검증이다 — 이것이 목을 쓰는 이유다.
     */
    @Test
    void 문제2_주문이_없으면_결제하지_않는다() {
        // TODO
        throw new AssertionError("TODO: 문제 2 를 풀어라");
    }

    /**
     * 문제 3. 결제가 {@code PaymentDeclinedException} 으로 거절되면
     * 주문이 <b>FAILED 상태로 저장</b>되는지 {@code ArgumentCaptor} 로 확인하라.
     *
     * <p>예외가 다시 던져지는 것도 함께 검증한다.
     */
    @Test
    void 문제3_결제_거절시_FAILED로_저장한다() {
        // TODO
        throw new AssertionError("TODO: 문제 3 을 풀어라");
    }

    /**
     * 문제 4. 결제({@code charge})가 저장({@code save})보다 <b>먼저</b> 불리는지
     * {@code InOrder} 로 검증하라.
     *
     * <p>🔥 순서가 뒤집히면 "결제 안 됐는데 PAID 로 저장된 주문"이 생긴다. 진짜 도메인 규칙이다.
     */
    @Test
    void 문제4_호출_순서를_검증하라() {
        // TODO
        throw new AssertionError("TODO: 문제 4 를 풀어라");
    }

    /**
     * 문제 5. 결제사가 <b>정확히 한 번만</b> 불리는지 검증하라.
     *
     * <p>다 푼 뒤, {@code OrderService.pay} 에 {@code charge} 호출을 한 줄 더 추가해 보고
     * 이 테스트가 실제로 잡아내는지 확인할 것 — 확인한 뒤 원복한다.
     */
    @Test
    void 문제5_중복_청구를_막는다() {
        // TODO
        throw new AssertionError("TODO: 문제 5 를 풀어라");
    }
}
