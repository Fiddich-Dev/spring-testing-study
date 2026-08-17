package io.study.testing.ch04_springslice.exercise;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.study.testing.ch04_springslice.ArticleController;

/**
 * <h2>04장 연습문제</h2>
 *
 * <pre>
 *   ./gradlew exercise --tests '*WebMvcExercise'
 * </pre>
 *
 * 🔥 <b>문제 0 을 먼저 풀어야 나머지가 실행된다.</b> {@code @MockitoBean} 이 없으면
 * 컨텍스트가 뜨지 못해 <b>모든</b> 테스트가 같은 이유로 죽는다 — 그 실패 메시지를
 * 직접 보는 것이 이 장의 첫 교훈이다.
 *
 * <p>답은 {@code ch04_springslice/ArticleControllerWebMvcTest} 에 있다.
 */
@Tag("exercise")
@WebMvcTest(ArticleController.class)
@DisplayName("04장 연습문제")
class WebMvcExercise {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // TODO: 문제 0. @MockitoBean 으로 ArticleService 자리를 채워라.
    //       (지우고 돌려 보면 NoSuchBeanDefinitionException 을 볼 수 있다)

    /**
     * 문제 1. {@code GET /api/articles/1} 이 200 을 주고
     * {@code $.title} 이 "테스트 입문" 인지 검증하라.
     *
     * <p>목이 {@code Article.of(1L, "테스트 입문", "김철수")} 를 돌려주도록 스터빙한다.
     */
    @Test
    void 문제1_단건_조회() throws Exception {
        // TODO
        throw new AssertionError("TODO: 문제 1 을 풀어라");
    }

    /**
     * 문제 2. 서비스가 {@code ArticleNotFoundException} 을 던질 때
     * <b>404</b> 와 {@code $.code == "ARTICLE_NOT_FOUND"} 가 나오는지 검증하라.
     *
     * <p>💡 예외 처리기는 슬라이스에 <b>포함</b>된다. 따로 등록할 필요가 없다.
     */
    @Test
    void 문제2_없는_글은_404() throws Exception {
        // TODO
        throw new AssertionError("TODO: 문제 2 를 풀어라");
    }

    /**
     * 문제 3. 빈 제목으로 {@code POST /api/articles} 를 보내면
     * <b>400</b> 이 나오고 {@code $.fields.title} 에 메시지가 담기는지 검증하라.
     *
     * <p>그리고 <b>서비스가 불리지 않았음</b>도 함께 확인하라.
     */
    @Test
    void 문제3_검증_실패() throws Exception {
        // TODO
        throw new AssertionError("TODO: 문제 3 을 풀어라");
    }

    /**
     * 문제 4. 글 생성이 성공하면 <b>201</b> 과 {@code Location} 헤더가
     * {@code /api/articles/42} 인지 검증하라.
     */
    @Test
    void 문제4_생성_응답() throws Exception {
        // TODO
        throw new AssertionError("TODO: 문제 4 를 풀어라");
    }

    /**
     * 문제 5. {@code DELETE /api/articles/1} 이 <b>204</b> 를 주고
     * 본문이 <b>비어 있는지</b> 검증하라. 서비스의 {@code delete(1L)} 이 불렸는지도 확인한다.
     */
    @Test
    void 문제5_삭제() throws Exception {
        // TODO
        throw new AssertionError("TODO: 문제 5 를 풀어라");
    }
}
