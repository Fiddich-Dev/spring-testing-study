package io.study.testing.ch03_mockito;

import java.math.BigDecimal;

/**
 * 협력 객체 ②. 외부 결제사다 — 테스트에서 <b>진짜로 부르면 안 되는</b> 종류의 의존이고,
 * 그래서 목이 가장 정당화되는 자리다.
 */
public interface PaymentGateway {

    /**
     * @return 결제 승인 번호
     * @throws PaymentDeclinedException 승인이 거절된 경우
     */
    String charge(String customerId, BigDecimal amount);
}
