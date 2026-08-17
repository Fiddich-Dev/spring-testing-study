package io.study.testing.ch04_springslice;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.stereotype.Service;

/**
 * 04장 <b>슬라이스에서 잘려 나가는</b> 쪽.
 *
 * <p>{@code @WebMvcTest} 는 이 빈을 만들지 않는다. 그래서 테스트가
 * {@code @MockitoBean} 으로 자리를 채워 준다 — 그 사실을 눈으로 보는 것이 이 장의 절반이다.
 *
 * <p>저장소를 안 쓰고 메모리 맵으로 둔 이유: 04장의 관심은 <b>웹 계층</b>이라
 * JPA·DB 를 끌어들이면 배울 것이 흐려진다. 실제 DB 는 07장(Testcontainers)에서 붙인다.
 */
@Service
public class ArticleService {

    private final Map<Long, Article> store = new ConcurrentHashMap<>();
    private final AtomicLong sequence = new AtomicLong();

    public Article findById(Long id) {
        Article found = store.get(id);
        if (found == null) {
            throw new ArticleNotFoundException(id);
        }
        return found;
    }

    public List<Article> findAll() {
        return List.copyOf(store.values());
    }

    public Article create(ArticleCreateRequest request) {
        long id = sequence.incrementAndGet();
        Article article = Article.of(id, request.title(), request.author());
        store.put(id, article);
        return article;
    }

    public void delete(Long id) {
        if (store.remove(id) == null) {
            throw new ArticleNotFoundException(id);
        }
    }
}
