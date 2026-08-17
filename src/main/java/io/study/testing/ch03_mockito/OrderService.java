package io.study.testing.ch03_mockito;

import java.util.NoSuchElementException;

/**
 * 03장 테스트 대상.
 *
 * <p>검증하고 싶은 규칙은 셋이다.
 * <ul>
 *   <li>결제가 성공하면 주문이 PAID 로 <b>저장</b>된다</li>
 *   <li>결제가 거절되면 FAILED 로 저장되고 <b>예외가 다시 던져진다</b></li>
 *   <li>결제사는 <b>정확히 한 번만</b> 불린다 (중복 청구는 사고다)</li>
 * </ul>
 * 셋 다 "무엇을 반환했나"가 아니라 <b>"누구를 어떻게 불렀나"</b>에 대한 질문이라,
 * 목 없이는 확인할 수 없다. 이것이 Mockito 를 쓰는 이유다.
 */
public class OrderService {

    private final OrderRepository orderRepository;
    private final PaymentGateway paymentGateway;

    public OrderService(OrderRepository orderRepository, PaymentGateway paymentGateway) {
        this.orderRepository = orderRepository;
        this.paymentGateway = paymentGateway;
    }

    public Order pay(String orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NoSuchElementException("주문을 찾을 수 없다: " + orderId));

        if (order.status() != Order.Status.PENDING) {
            throw new IllegalStateException("이미 처리된 주문이다: " + order.status());
        }

        try {
            paymentGateway.charge(order.customerId(), order.amount());
        } catch (PaymentDeclinedException exc) {
            orderRepository.save(order.failed());
            throw exc;
        }

        return orderRepository.save(order.paid());
    }
}
