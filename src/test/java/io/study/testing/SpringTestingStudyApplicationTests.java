package io.study.testing;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Spring Initializr 가 만들어 주는 그 테스트다. 지우지 않고 남겨 둔다.
 *
 * <p>📌 <b>한 줄도 없는데 값어치가 있다.</b> 이 테스트가 검증하는 것은
 * "애플리케이션 컨텍스트가 뜬다" 는 사실 하나다 — 빈 순환 참조, 없는 프로퍼티,
 * 조건부 빈이 만든 구멍처럼 <b>기동 시점에만</b> 드러나는 문제를 여기서 잡는다.
 *
 * <p>🔥 실무에서 이 테스트가 깨지면 대개 배포도 깨진다. 가장 싸게 사는 안전망이다.
 */
@SpringBootTest
@DisplayName("애플리케이션 컨텍스트가 뜬다")
class SpringTestingStudyApplicationTests {

    @Test
    void contextLoads() {
        // 본문이 비어 있는 것이 맞다. 컨텍스트 기동 자체가 검증이다.
    }
}
