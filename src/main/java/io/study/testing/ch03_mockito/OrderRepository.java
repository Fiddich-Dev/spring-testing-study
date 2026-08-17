package io.study.testing.ch03_mockito;

import java.util.Optional;

/**
 * 협력 객체 ①. <b>인터페이스로 두는 이유</b>가 곧 03장의 주제다 —
 * 구현이 DB 든 HTTP 든, 서비스의 규칙을 검증할 때는 그 사정을 알 필요가 없다.
 */
public interface OrderRepository {

    Optional<Order> findById(String id);

    Order save(Order order);
}
