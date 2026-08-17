package io.study.testing.ch03_mockito;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * <h2>03장 ② — ArgumentCaptor · InOrder · Spy</h2>
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("03장 ② Mockito 심화")
class MockitoAdvancedTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private PaymentGateway paymentGateway;

    /**
     * ⚠ 이 중첩 클래스를 {@code Captor} 라고 이름 짓지 말 것 —
     * {@code @Captor} 어노테이션을 가려서 "cannot be converted to Annotation" 이 난다.
     */
    @Nested
    @DisplayName("ArgumentCaptor — 넘어간 인자를 붙잡아 본다")
    class Capturing {

        /**
         * 🔥 <b>언제 필요한가</b>: 목에 넘어간 객체가 <b>테스트 안에서 만들어지지 않아</b>
         * 미리 비교 대상을 만들 수 없을 때. 서비스가 내부에서 조립한 객체가 대표적이다.
         *
         * <p>{@code verify(repo).save(어떤객체)} 는 그 "어떤객체"를 미리 알아야 쓸 수 있다.
         * 캡터는 <b>실제로 넘어간 것</b>을 꺼내서 열어 본다.
         */
        @Test
        void 넘어간_객체의_내부를_본다() {
            Order pending = Order.pending("ORD-1", "CUST-1", 10_000);
            when(orderRepository.findById("ORD-1")).thenReturn(Optional.of(pending));
            when(paymentGateway.charge(anyString(), any())).thenReturn("APPROVED");
            when(orderRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            new OrderService(orderRepository, paymentGateway).pay("ORD-1");

            ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
            verify(orderRepository).save(captor.capture());

            Order saved = captor.getValue();
            assertThat(saved.status()).isEqualTo(Order.Status.PAID);
            assertThat(saved.id()).isEqualTo("ORD-1");
            assertThat(saved.amount()).isEqualByComparingTo("10000");
        }

        /**
         * 📌 여러 번 불렸으면 {@code getAllValues()} 로 전부 꺼낸다. <b>순서대로</b> 담긴다.
         */
        @Test
        void 여러_번_불린_인자를_전부_꺼낸다() {
            orderRepository.save(Order.pending("A", "C", 1));
            orderRepository.save(Order.pending("B", "C", 2));

            ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
            verify(orderRepository, times(2)).save(captor.capture());

            assertThat(captor.getAllValues())
                    .extracting(Order::id)
                    .containsExactly("A", "B");
        }

        /**
         * ⚠ <b>{@code argThat} 과 언제를 가르나.</b>
         * <ul>
         *   <li>{@code argThat} — 조건에 <b>맞는 호출이 있었는지</b>만 보면 될 때. 짧다</li>
         *   <li>캡터 — 넘어간 값을 <b>여러 각도로</b> 뜯어봐야 할 때. 실패 메시지도 훨씬 친절하다</li>
         * </ul>
         * 🔥 {@code argThat} 이 실패하면 "매칭되는 호출이 없다" 만 나오고
         * <b>실제로 뭐가 넘어왔는지는 안 알려준다</b> — 그래서 디버깅이 어렵다.
         */
        @Test
        void argThat_과의_차이() {
            orderRepository.save(Order.pending("A", "CUST-9", 5_000));

            verify(orderRepository).save(argThat(order -> order.customerId().equals("CUST-9")));
        }

        /**
         * 📌 {@code @Captor} 어노테이션 방식. <b>제네릭 타입을 온전히 표현할 수 있다</b> —
         * {@code ArgumentCaptor.forClass(List.class)} 로는 {@code List<String>} 을 쓸 수 없고
         * 비검사 경고가 남는다.
         */
        @Captor
        private ArgumentCaptor<Order> orderCaptor;

        @Test
        void 어노테이션_방식() {
            orderRepository.save(Order.pending("X", "CUST-X", 1));

            verify(orderRepository).save(orderCaptor.capture());

            assertThat(orderCaptor.getValue().id()).isEqualTo("X");
        }
    }

    @Nested
    @DisplayName("InOrder — 호출 순서를 검증한다")
    class Ordering {

        /**
         * 📌 <b>순서가 도메인 규칙일 때만 쓴다.</b>
         *
         * <p>여기서는 진짜 규칙이다 — 결제를 <b>먼저</b> 하고 저장을 <b>나중에</b> 해야 한다.
         * 뒤집히면 "결제 안 됐는데 PAID 로 저장된 주문" 이 생긴다.
         *
         * <p>⚠ 반대로 순서가 상관없는 호출에 걸면, 구현을 정리할 때마다 애먼 테스트가 깨진다.
         */
        @Test
        void 결제가_저장보다_먼저다() {
            Order pending = Order.pending("ORD-2", "CUST-2", 20_000);
            when(orderRepository.findById("ORD-2")).thenReturn(Optional.of(pending));
            when(paymentGateway.charge(anyString(), any())).thenReturn("APPROVED");
            when(orderRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            new OrderService(orderRepository, paymentGateway).pay("ORD-2");

            // 목이 여럿이어도 한 InOrder 에 넣으면 **목을 가로질러** 순서를 본다.
            InOrder inOrder = inOrder(orderRepository, paymentGateway);
            inOrder.verify(orderRepository).findById("ORD-2");
            inOrder.verify(paymentGateway).charge("CUST-2", BigDecimal.valueOf(20_000));
            inOrder.verify(orderRepository).save(any(Order.class));
        }
    }

    @Nested
    @DisplayName("Spy — 진짜 객체를 감싸 일부만 가로챈다")
    class Spies {

        /**
         * 📌 <b>목과의 차이</b>: 스터빙하지 않은 메서드는 <b>진짜 구현이 실행된다</b>.
         * 레거시 코드처럼 통째로 목을 만들기 어려운 대상에 부분적으로 쓴다.
         *
         * <p>⚠ <b>스파이가 자주 필요하다면 설계 신호다.</b> "이 클래스의 이 메서드만 가짜였으면"
         * 은 대개 그 메서드가 별도 협력 객체로 나와야 한다는 뜻이다.
         */
        @Test
        void 스터빙_안한_메서드는_진짜로_동작한다() {
            List<String> realList = new ArrayList<>();
            List<String> spyList = spy(realList);

            spyList.add("실제로 들어간다");

            assertThat(spyList).hasSize(1).containsExactly("실제로 들어간다");
            verify(spyList).add("실제로 들어간다");   // 호출 검증도 된다
        }

        /**
         * 🔥 <b>스파이에서 가장 자주 나는 사고</b>: {@code when(spy.get(0))} 을 쓰면
         * 스터빙하기 <b>전에 진짜 메서드가 먼저 실행된다</b>.
         * 빈 리스트에서는 그 자리에서 {@code IndexOutOfBoundsException} 이 터진다.
         *
         * <p>그래서 스파이는 {@code doReturn(...).when(spy).method()} 형태를 쓴다.
         * 이 형태는 실제 메서드를 부르지 않는다.
         */
        @Test
        void 스파이는_doReturn_형태를_쓴다() {
            List<String> spyList = spy(new ArrayList<String>());

            // ✗ when(spyList.get(0)).thenReturn("가짜");  ← 여기서 IndexOutOfBounds 가 터진다
            assertThatThrownBy(() -> spyList.get(0))
                    .isInstanceOf(IndexOutOfBoundsException.class);

            // ✓ 실제 메서드를 부르지 않는 형태
            doReturn("가짜").when(spyList).get(0);
            assertThat(spyList.get(0)).isEqualTo("가짜");
        }
    }

    @Nested
    @DisplayName("Answer — 인자를 보고 응답을 만든다")
    class Answers {

        /**
         * 📌 <b>가장 자주 쓰는 형태</b>: 저장소가 "저장한 것을 그대로 돌려준다" 를 흉내내는 것.
         * {@code thenReturn} 으로는 표현할 수 없다 — 무엇이 넘어올지 미리 모르기 때문이다.
         */
        @Test
        void 넘어온_인자를_그대로_돌려준다() {
            when(orderRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

            Order order = Order.pending("ORD-3", "CUST-3", 3_000);
            assertThat(orderRepository.save(order)).isSameAs(order);
        }

        @Test
        void 인자에_따라_다르게_응답한다() {
            when(paymentGateway.charge(anyString(), any())).thenAnswer(invocation -> {
                BigDecimal amount = invocation.getArgument(1);
                if (amount.compareTo(BigDecimal.valueOf(100_000)) > 0) {
                    throw new PaymentDeclinedException("한도 초과");
                }
                return "APPROVED";
            });

            assertThat(paymentGateway.charge("C", BigDecimal.valueOf(1_000))).isEqualTo("APPROVED");
            assertThatThrownBy(() -> paymentGateway.charge("C", BigDecimal.valueOf(200_000)))
                    .isInstanceOf(PaymentDeclinedException.class);
        }
    }
}
