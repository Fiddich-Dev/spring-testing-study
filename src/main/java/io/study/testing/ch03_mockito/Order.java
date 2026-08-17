package io.study.testing.ch03_mockito;

import java.math.BigDecimal;

/** 03장 테스트 대상 도메인. */
public record Order(String id, String customerId, BigDecimal amount, Status status) {

    public enum Status {
        PENDING, PAID, FAILED
    }

    public static Order pending(String id, String customerId, long amount) {
        return new Order(id, customerId, BigDecimal.valueOf(amount), Status.PENDING);
    }

    public Order paid() {
        return new Order(id, customerId, amount, Status.PAID);
    }

    public Order failed() {
        return new Order(id, customerId, amount, Status.FAILED);
    }
}
