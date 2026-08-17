package io.study.testing.ch04_springslice;

import java.net.URI;
import java.util.List;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 04장 테스트 대상. {@code @WebMvcTest} 가 <b>실제로 세우는</b> 쪽이다.
 */
@RestController
@RequestMapping("/api/articles")
public class ArticleController {

    private final ArticleService articleService;

    public ArticleController(ArticleService articleService) {
        this.articleService = articleService;
    }

    @GetMapping("/{id}")
    public Article get(@PathVariable Long id) {
        return articleService.findById(id);
    }

    @GetMapping
    public List<Article> list(@RequestParam(defaultValue = "10") int size) {
        return articleService.findAll().stream().limit(size).toList();
    }

    @PostMapping
    public ResponseEntity<Article> create(@Valid @RequestBody ArticleCreateRequest request) {
        Article created = articleService.create(request);
        return ResponseEntity.created(URI.create("/api/articles/" + created.id())).body(created);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        articleService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
