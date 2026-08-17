package io.study.testing.ch04_springslice;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * <h2>04장 ① — {@code @WebMvcTest} 슬라이스</h2>
 *
 * 📌 <b>슬라이스 테스트란</b>: 애플리케이션 <b>전체</b>가 아니라 한 계층만 세우는 것.
 * {@code @WebMvcTest} 는 웹 계층만 세운다 — 빠르고, 실패 원인이 좁다.
 *
 * <p><b>세워지는 것</b>: {@code @Controller}·{@code @RestController},
 * {@code @ControllerAdvice}, {@code Filter}, {@code WebMvcConfigurer},
 * {@code Converter}, Jackson 설정, 검증(Validation).
 *
 * <p><b>세워지지 않는 것</b>: {@code @Service}·{@code @Repository}·{@code @Component},
 * JPA, DataSource, {@code @Configuration} 일반.
 *
 * <p>🔥 <b>그래서 서비스 자리는 비어 있다.</b> {@code @MockitoBean} 으로 채우지 않으면
 * "필요한 빈이 없다" 며 컨텍스트가 뜨지 못한다. 이것이 이 장의 핵심 감각이다.
 *
 * <p>⚠ <b>{@code @MockitoBean} vs {@code @MockBean}</b> — {@code @MockBean} 은 Spring Boot 3.4 /
 * Framework 6.2 에서 <b>deprecated</b> 됐다. 새 코드는 {@code @MockitoBean} 을 쓴다.
 * 패키지도 다르다 ({@code org.springframework.test.context.bean.override.mockito}).
 */
@WebMvcTest(ArticleController.class)   // ← 컨트롤러를 지정하지 않으면 **모든** 컨트롤러를 세운다
@DisplayName("04장 ① @WebMvcTest")
class ArticleControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 🔥 이 목이 없으면 테스트가 <b>한 줄도 실행되기 전에</b>
     * {@code NoSuchBeanDefinitionException} 으로 죽는다. 슬라이스가 서비스를 안 세우기 때문이다.
     */
    @MockitoBean
    private ArticleService articleService;

    @Nested
    @DisplayName("GET /api/articles/{id}")
    class GetOne {

        @Test
        void 있으면_200과_본문을_준다() throws Exception {
            given(articleService.findById(1L)).willReturn(Article.of(1L, "테스트 입문", "김철수"));

            mockMvc.perform(get("/api/articles/{id}", 1L))
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.title").value("테스트 입문"))
                    .andExpect(jsonPath("$.author").value("김철수"))
                    // 📌 $ 는 루트. 존재 여부만 볼 때는 exists()/doesNotExist() 를 쓴다.
                    .andExpect(jsonPath("$.createdAt").exists())
                    .andExpect(jsonPath("$.nonexistent").doesNotExist());

            then(articleService).should().findById(1L);
        }

        /**
         * 📌 <b>{@code @RestControllerAdvice} 는 슬라이스에 포함된다.</b>
         * 그래서 목이 예외를 던지면 <b>실제 예외 처리 경로</b>가 그대로 돈다 —
         * 404 응답 본문의 모양까지 여기서 검증할 수 있다.
         */
        @Test
        void 없으면_404와_에러본문을_준다() throws Exception {
            given(articleService.findById(999L)).willThrow(new ArticleNotFoundException(999L));

            mockMvc.perform(get("/api/articles/{id}", 999L))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.code").value("ARTICLE_NOT_FOUND"))
                    .andExpect(jsonPath("$.message").value("글을 찾을 수 없다: 999"));
        }

        /**
         * ⚠ 타입이 안 맞는 경로 변수는 컨트롤러에 <b>닿기도 전에</b> 400 이 된다.
         * 서비스는 불리지 않는다.
         */
        @Test
        void 숫자가_아니면_400() throws Exception {
            mockMvc.perform(get("/api/articles/{id}", "abc"))
                    .andExpect(status().isBadRequest());

            then(articleService).shouldHaveNoInteractions();
        }
    }

    @Nested
    @DisplayName("GET /api/articles")
    class GetList {

        @Test
        void 배열_검증() throws Exception {
            given(articleService.findAll()).willReturn(List.of(
                    Article.of(1L, "첫 글", "김철수"),
                    Article.of(2L, "둘째 글", "이영희")));

            mockMvc.perform(get("/api/articles"))
                    .andExpect(status().isOk())
                    // 📌 배열 검증에 자주 쓰는 세 가지
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$.length()").value(2))
                    .andExpect(jsonPath("$[0].title").value("첫 글"))
                    // $..title → 깊이 상관없이 모든 title 을 모은다
                    .andExpect(jsonPath("$..title").value(org.hamcrest.Matchers.hasItems("첫 글", "둘째 글")));
        }

        @Test
        void 쿼리_파라미터() throws Exception {
            given(articleService.findAll()).willReturn(List.of(
                    Article.of(1L, "첫 글", "김철수"),
                    Article.of(2L, "둘째 글", "이영희")));

            mockMvc.perform(get("/api/articles").param("size", "1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(1));
        }
    }

    @Nested
    @DisplayName("POST /api/articles")
    class Create {

        @Test
        void 성공하면_201과_Location() throws Exception {
            ArticleCreateRequest request = new ArticleCreateRequest("새 글", "본문", "김철수");
            given(articleService.create(any())).willReturn(Article.of(42L, "새 글", "김철수"));

            mockMvc.perform(post("/api/articles")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(header().string("Location", "/api/articles/42"))
                    .andExpect(jsonPath("$.id").value(42));
        }

        /**
         * 🔥 <b>검증 실패 경로를 테스트하지 않는 것이 가장 흔한 구멍이다.</b>
         * 성공 케이스만 있는 컨트롤러 테스트는 "잘못된 입력이 어떻게 되는지" 를 아무도 모르게 둔다.
         *
         * <p>여기서는 셋 다 비어 있으므로 {@code fields} 에 세 항목이 담긴다.
         */
        @Test
        void 검증_실패하면_400과_필드별_메시지() throws Exception {
            ArticleCreateRequest invalid = new ArticleCreateRequest("", "", "");

            mockMvc.perform(post("/api/articles")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalid)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                    .andExpect(jsonPath("$.fields.title").value("제목은 비어 있을 수 없다"))
                    .andExpect(jsonPath("$.fields.content").value("본문은 비어 있을 수 없다"))
                    .andExpect(jsonPath("$.fields.author").value("작성자는 비어 있을 수 없다"));

            // 📌 검증에서 걸렸으니 서비스는 **불리지 않아야** 한다.
            then(articleService).should(never()).create(any());
        }

        /**
         * ⚠ {@code Content-Type} 을 안 주면 415 다. 400 이 아니다 —
         * 이 둘을 헷갈리면 프론트와 원인 진단이 어긋난다.
         */
        @Test
        void ContentType_없으면_415() throws Exception {
            mockMvc.perform(post("/api/articles").content("{}"))
                    .andExpect(status().isUnsupportedMediaType());
        }

        @Test
        void 망가진_JSON은_400() throws Exception {
            mockMvc.perform(post("/api/articles")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{ 이건 JSON 이 아니다 }"))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("DELETE /api/articles/{id}")
    class Delete {

        @Test
        void 성공하면_204_본문없음() throws Exception {
            mockMvc.perform(delete("/api/articles/{id}", 1L))
                    .andExpect(status().isNoContent())
                    .andExpect(content().string(""));

            then(articleService).should().delete(1L);
        }

        @Test
        void 없으면_404() throws Exception {
            doThrow(new ArticleNotFoundException(999L)).when(articleService).delete(999L);

            mockMvc.perform(delete("/api/articles/{id}", 999L))
                    .andExpect(status().isNotFound());
        }
    }

    /**
     * 💡 <b>실패했을 때 가장 먼저 할 일</b>: {@code andDo(print())} 를 붙여
     * 요청·응답 전체를 찍어 보는 것. 상태 코드만 보고 추측하는 것보다 훨씬 빠르다.
     */
    @Test
    @DisplayName("응답 전체를 찍어 보고 싶을 때")
    void 디버깅_요령() throws Exception {
        given(articleService.findById(1L)).willReturn(Article.of(1L, "제목", "작성자"));

        String body = mockMvc.perform(get("/api/articles/1"))
                .andDo(org.springframework.test.web.servlet.result.MockMvcResultHandlers.print())
                .andReturn()
                .getResponse()
                // ⚠ getContentAsString() 은 ISO-8859-1 로 읽어 한글이 깨진다.
                //    UTF-8 을 명시하거나, 애초에 jsonPath 로 검증하는 편이 낫다.
                .getContentAsString(java.nio.charset.StandardCharsets.UTF_8);

        assertThat(body).contains("제목");
    }
}
