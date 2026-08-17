package io.study.testing.ch03_mockito;

import java.math.BigDecimal;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

/**
 * <h2>03장 ① — Mockito 기본</h2>
 *
 * 📌 <b>목이 필요한 이유는 하나다</b>: 검증하고 싶은 것이 <b>반환값이 아니라 상호작용</b>일 때.
 * "결제사를 정확히 한 번만 불렀나", "실패했을 때 FAILED 로 저장했나" 는 리턴값을 아무리 봐도
 * 알 수 없다.
 *
 * <p>⚠ <b>반대로, 반환값만 보면 되는 것에는 목을 쓰지 않는다.</b> 가짜 객체를 끼우는 순간
 * 테스트는 "구현이 이 순서로 이걸 부른다" 에 묶여, 리팩터링할 때마다 깨진다.
 * 값 계산은 그냥 진짜 객체로 테스트하는 것이 낫다.
 *
 * <p>💡 <b>Mock vs Stub vs Spy</b>
 * <ul>
 *   <li><b>Stub</b> — 정해진 값을 돌려주는 가짜. "무엇을 돌려줄까" 만 관심</li>
 *   <li><b>Mock</b> — 여기에 "어떻게 불렸나" 검증이 붙은 것. Mockito 의 {@code verify}</li>
 *   <li><b>Spy</b> — <b>진짜 객체</b>를 감싸서 일부만 가로챈 것</li>
 * </ul>
 */
@ExtendWith(MockitoExtension.class)   // ← 이게 있어야 @Mock/@InjectMocks 이 채워진다
@DisplayName("03장 ① Mockito 기본")
class MockitoBasicsTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private PaymentGateway paymentGateway;

    /**
     * 📌 {@code @InjectMocks} 는 위 {@code @Mock} 들을 생성자로 밀어넣어 <b>진짜</b>
     * {@code OrderService} 를 만든다. 테스트 대상은 목이 아니라 실제 객체다.
     *
     * <p>⚠ 생성자 주입이면 잘 동작하지만, 필드 주입이면 조용히 null 이 남을 수 있다.
     * <b>그래서 프로덕션 코드도 생성자 주입을 쓰는 것이 좋다</b> — 테스트하기 쉬운 설계가
     * 대개 좋은 설계인 이유의 한 사례다.
     */
    @InjectMocks
    private OrderService orderService;

    @Nested
    @DisplayName("스터빙")
    class Stubbing {

        @Test
        void when_thenReturn() {
            Order pending = Order.pending("ORD-1", "CUST-1", 10_000);
            when(orderRepository.findById("ORD-1")).thenReturn(Optional.of(pending));
            when(paymentGateway.charge("CUST-1", BigDecimal.valueOf(10_000))).thenReturn("APPROVED-1");
            when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

            Order result = orderService.pay("ORD-1");

            assertThat(result.status()).isEqualTo(Order.Status.PAID);
        }

        /**
         * 📌 <b>스터빙하지 않은 메서드는 "기본값"을 돌려준다.</b> 예외가 아니다.
         * <ul>
         *   <li>객체 → {@code null}</li>
         *   <li>{@code int}·{@code long} → {@code 0}, {@code boolean} → {@code false}</li>
         *   <li>{@code Optional} → {@code Optional.empty()} (RETURNS_SMART_NULLS 아니어도)</li>
         *   <li>{@code List}·{@code Set} → 빈 컬렉션</li>
         * </ul>
         * 🔥 그래서 스터빙을 빠뜨리면 <b>NullPointerException 이 엉뚱한 곳에서</b> 터진다.
         * 실패 원인을 찾을 때 "이 목이 뭘 돌려주고 있지?" 를 먼저 의심할 것.
         */
        @Test
        void 스터빙_안하면_기본값이_나온다() {
            assertThat(orderRepository.findById("없는주문")).isEmpty();

            // Optional.empty() 가 나오므로 서비스는 NoSuchElementException 을 던진다.
            assertThatThrownBy(() -> orderService.pay("없는주문"))
                    .isInstanceOf(NoSuchElementException.class)
                    .hasMessageContaining("없는주문");
        }

        /**
         * 🔥 <b>void 메서드는 {@code when()} 으로 스터빙할 수 없다.</b>
         * {@code when(mock.voidMethod())} 는 애초에 컴파일이 안 된다.
         * {@code doThrow}/{@code doNothing}/{@code doAnswer} 를 쓴다.
         */
        @Test
        void void_메서드는_do계열로() {
            // OrderRepository.save 는 void 가 아니지만, 형태를 보이기 위해 do 계열을 쓴다.
            doThrow(new IllegalStateException("DB 장애"))
                    .when(orderRepository).save(any(Order.class));

            Order pending = Order.pending("ORD-2", "CUST-2", 5_000);
            when(orderRepository.findById("ORD-2")).thenReturn(Optional.of(pending));
            when(paymentGateway.charge(anyString(), any())).thenReturn("APPROVED-2");

            assertThatThrownBy(() -> orderService.pay("ORD-2"))
                    .isInstanceOf(IllegalStateException.class);
        }

        /**
         * 📌 <b>호출할 때마다 다른 값</b>을 돌려주려면 이어 쓴다.
         * 재시도 로직을 테스트할 때 이 형태를 쓴다.
         */
        @Test
        void 연속_호출마다_다른_값() {
            when(paymentGateway.charge(anyString(), any()))
                    .thenThrow(new PaymentDeclinedException("일시 오류"))
                    .thenReturn("APPROVED-3");

            assertThatThrownBy(() -> paymentGateway.charge("CUST", BigDecimal.ONE))
                    .isInstanceOf(PaymentDeclinedException.class);
            assertThat(paymentGateway.charge("CUST", BigDecimal.ONE)).isEqualTo("APPROVED-3");
        }
    }

    @Nested
    @DisplayName("인자 매처")
    class Matchers {

        /**
         * 🔥 <b>가장 흔한 컴파일/런타임 사고</b>: 매처와 실제 값을 <b>섞어 쓰는</b> 것.
         *
         * <pre>
         *   // ✗ InvalidUseOfMatchersException
         *   when(gateway.charge("CUST-1", any())).thenReturn("OK");
         *
         *   // ✓ 하나라도 매처를 쓰면 **전부** 매처여야 한다
         *   when(gateway.charge(eq("CUST-1"), any())).thenReturn("OK");
         * </pre>
         */
        @Test
        void 매처를_쓰면_전부_매처여야_한다() {
            Order pending = Order.pending("ORD-4", "CUST-4", 3_000);
            when(orderRepository.findById(eq("ORD-4"))).thenReturn(Optional.of(pending));
            when(paymentGateway.charge(eq("CUST-4"), any(BigDecimal.class))).thenReturn("APPROVED-4");
            when(orderRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            assertThat(orderService.pay("ORD-4").status()).isEqualTo(Order.Status.PAID);
        }
    }

    @Nested
    @DisplayName("검증(verify)")
    class Verification {

        @Test
        void 호출_횟수를_센다() {
            Order pending = Order.pending("ORD-5", "CUST-5", 7_000);
            when(orderRepository.findById("ORD-5")).thenReturn(Optional.of(pending));
            when(paymentGateway.charge(anyString(), any())).thenReturn("APPROVED-5");
            when(orderRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            orderService.pay("ORD-5");

            // 🔥 결제는 정확히 한 번. 두 번 불리면 중복 청구 사고다 —
            //    반환값만 봐서는 절대 알 수 없고, 오직 verify 만이 잡는다.
            verify(paymentGateway, times(1)).charge("CUST-5", BigDecimal.valueOf(7_000));
            verify(orderRepository).save(any(Order.class));   // times(1) 이 기본값
        }

        @Test
        void 부르지_않았음을_검증한다() {
            assertThatThrownBy(() -> orderService.pay("없는주문"))
                    .isInstanceOf(NoSuchElementException.class);

            // 📌 주문을 못 찾았으면 결제사는 **건드리지도 않아야** 한다.
            verify(paymentGateway, never()).charge(anyString(), any());
            verifyNoInteractions(paymentGateway);   // 이 목에 아무 호출도 없었다
        }

        @Test
        void 결제_실패시_FAILED로_저장한다() {
            Order pending = Order.pending("ORD-6", "CUST-6", 9_000);
            when(orderRepository.findById("ORD-6")).thenReturn(Optional.of(pending));
            when(paymentGateway.charge(anyString(), any()))
                    .thenThrow(new PaymentDeclinedException("한도 초과"));

            assertThatThrownBy(() -> orderService.pay("ORD-6"))
                    .isInstanceOf(PaymentDeclinedException.class)
                    .hasMessage("한도 초과");

            verify(orderRepository).save(pending.failed());
        }

        /**
         * ⚠ {@code verifyNoMoreInteractions} 는 <b>남용하지 않는다.</b>
         * 구현이 무해한 호출을 하나 추가하는 순간 깨져서, 리팩터링을 방해하는 쪽으로 기운다.
         * "이건 절대 더 불리면 안 된다"가 <b>도메인 규칙</b>일 때만 쓴다.
         */
        @Test
        void 더_이상의_상호작용이_없음() {
            Order pending = Order.pending("ORD-7", "CUST-7", 1_000);
            when(orderRepository.findById("ORD-7")).thenReturn(Optional.of(pending));
            when(paymentGateway.charge(anyString(), any())).thenReturn("APPROVED-7");
            when(orderRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            orderService.pay("ORD-7");

            verify(paymentGateway).charge(anyString(), any());
            verifyNoMoreInteractions(paymentGateway);
        }
    }

    /**
     * 📌 <b>BDDMockito</b> — 같은 기능에 given/when/then 이름을 붙인 것.
     * {@code when} 이 "스터빙"과 "실행 단계" 두 뜻으로 쓰여 헷갈리는 문제를 없앤다.
     * 기능 차이는 없고 <b>읽는 방식</b>의 차이다.
     */
    @Nested
    @DisplayName("BDDMockito 스타일")
    class Bdd {

        @Test
        void given_when_then() {
            // given
            Order pending = Order.pending("ORD-8", "CUST-8", 12_000);
            given(orderRepository.findById("ORD-8")).willReturn(Optional.of(pending));
            given(paymentGateway.charge(anyString(), any())).willReturn("APPROVED-8");
            given(orderRepository.save(any())).willAnswer(inv -> inv.getArgument(0));

            // when
            Order result = orderService.pay("ORD-8");

            // then
            assertThat(result.status()).isEqualTo(Order.Status.PAID);
            then(paymentGateway).should().charge("CUST-8", BigDecimal.valueOf(12_000));
            then(orderRepository).should().save(pending.paid());
        }
    }

    /**
     * 🔥 <b>Strictness 를 알아 둬야 하는 이유.</b>
     *
     * <p>{@code MockitoExtension} 의 기본은 {@code STRICT_STUBS} 라, <b>쓰이지 않은 스터빙이
     * 있으면 테스트가 실패한다</b>({@code UnnecessaryStubbingException}).
     * 처음에는 성가시지만 이 검사가 잡아 주는 것이 크다 —
     * 스터빙한 메서드가 실제로는 안 불린다는 것은 대개 <b>테스트가 의도한 경로를 안 타고 있다</b>는 뜻이다.
     *
     * <p>⚠ {@code LENIENT} 로 끄기 전에 "왜 안 불렸지?" 를 먼저 볼 것.
     * 정말 필요하면 개별 스터빙에 {@code lenient()} 를 붙이는 쪽이 낫다.
     */
    @Nested
    @ExtendWith(MockitoExtension.class)
    @MockitoSettings(strictness = Strictness.LENIENT)
    @DisplayName("Strictness")
    class StrictnessDemo {

        @Mock
        private PaymentGateway gateway;

        @Test
        void 안_쓰인_스터빙이_있어도_통과한다() {
            // STRICT_STUBS 였다면 이 줄 때문에 UnnecessaryStubbingException 이 난다.
            when(gateway.charge(anyString(), any())).thenReturn("사용되지 않음");

            assertThat(true).isTrue();
        }
    }
}
