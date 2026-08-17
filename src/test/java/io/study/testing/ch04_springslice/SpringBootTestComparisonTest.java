package io.study.testing.ch04_springslice;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * <h2>04장 ② — 슬라이스 vs 전체 컨텍스트</h2>
 *
 * <table border="1">
 *   <caption>어떤 걸 언제 쓰나</caption>
 *   <tr><th></th><th>{@code @WebMvcTest}</th><th>{@code @SpringBootTest}</th></tr>
 *   <tr><td>세우는 빈</td><td>웹 계층만</td><td>전부</td></tr>
 *   <tr><td>속도</td><td>빠르다</td><td>느리다</td></tr>
 *   <tr><td>서비스</td><td>목으로 채운다</td><td>진짜가 돈다</td></tr>
 *   <tr><td>서버</td><td>안 뜬다 (MockMvc 가 흉내)</td><td>{@code RANDOM_PORT} 면 진짜로 뜬다</td></tr>
 *   <tr><td>잡는 버그</td><td>매핑·검증·직렬화·예외 처리</td><td>배선(빈 조립) 문제, 전 구간 흐름</td></tr>
 * </table>
 *
 * <p>🔥 <b>둘 중 하나만 고르는 문제가 아니다.</b> 컨트롤러 케이스는 슬라이스로 촘촘하게 깔고,
 * 전체 컨텍스트는 <b>대표 경로 몇 개</b>만 둔다. 전부 {@code @SpringBootTest} 로 짜면
 * 테스트 스위트가 몇 분씩 걸려 TDD 사이클이 죽는다.
 *
 * <p>💡 <b>컨텍스트 캐싱</b> — Spring 은 <b>설정이 같으면</b> 컨텍스트를 재사용한다.
 * {@code @MockitoBean} 을 하나 추가하거나 {@code @ActiveProfiles} 를 바꾸면 설정이 달라져
 * <b>새 컨텍스트가 뜬다</b>. 테스트가 느려질 때 가장 먼저 의심할 자리다.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@DisplayName("04장 ② @SpringBootTest 와의 차이")
class SpringBootTestComparisonTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 📌 <b>여기서는 {@code ArticleService} 가 진짜다.</b> 목을 끼우지 않았다.
     * 그래서 POST 로 만든 글을 GET 으로 다시 읽을 수 있다 — 슬라이스에서는 못 하는 검증이다.
     */
    @Test
    void 진짜_서비스가_동작한다() throws Exception {
        String body = objectMapper.writeValueAsString(
                new ArticleCreateRequest("전체 컨텍스트 글", "본문", "김철수"));

        String created = mockMvc.perform(post("/api/articles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("전체 컨텍스트 글"))
                .andReturn()
                .getResponse()
                .getContentAsString(java.nio.charset.StandardCharsets.UTF_8);

        assertThat(created).contains("전체 컨텍스트 글");
    }

    /**
     * 📌 <b>{@code TestRestTemplate} 은 진짜 HTTP 를 탄다.</b>
     * 그래서 {@code MockMvc} 가 못 보는 것을 본다 — 서블릿 컨테이너 설정, 실제 인코딩,
     * 커넥션 수준의 문제.
     *
     * <p>⚠ 대신 느리고, 실패했을 때 원인이 넓다. 필요한 곳에만 쓴다.
     */
    @Test
    void TestRestTemplate은_진짜_HTTP다() {
        ResponseEntity<String> response = restTemplate.getForEntity(
                "http://localhost:" + port + "/api/articles", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(port).isPositive();
    }

    /**
     * ⚠ <b>{@code @SpringBootTest} 에서도 {@code MockMvc} 를 쓸 수 있다.</b>
     * {@code @AutoConfigureMockMvc} 를 붙이면 된다.
     *
     * <p>이 조합이 실무에서 흔하다 — 진짜 빈을 쓰면서도 HTTP 오버헤드 없이 빠르게 돈다.
     * (acttub-platform 의 {@code *IT} 테스트들이 정확히 이 형태다)
     */
    @Test
    void 없는_글을_찾으면_404() throws Exception {
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .get("/api/articles/{id}", 999_999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("ARTICLE_NOT_FOUND"));
    }
}
