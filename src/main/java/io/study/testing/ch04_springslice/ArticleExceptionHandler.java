package io.study.testing.ch04_springslice;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * ⚠ <b>{@code @WebMvcTest} 는 {@code @RestControllerAdvice} 를 함께 세운다.</b>
 *
 * <p>이걸 모르면 "왜 목만 끼웠는데 404 응답 본문이 나오지?" 에서 헤맨다.
 * 슬라이스가 걷어 가는 것은 {@code @Service}·{@code @Repository}·{@code @Component} 이고,
 * 웹 계층으로 분류되는 것( {@code @Controller}·{@code @ControllerAdvice}·
 * {@code Filter}·{@code WebMvcConfigurer}·{@code Converter} )은 그대로 남는다.
 */
@RestControllerAdvice
public class ArticleExceptionHandler {

    @ExceptionHandler(ArticleNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(ArticleNotFoundException exc) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("code", "ARTICLE_NOT_FOUND");
        body.put("message", exc.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException exc) {
        // 필드 순서를 정렬해 둔다. 안 그러면 필드 순서가 실행마다 달라져
        // jsonPath 로 배열 인덱스를 짚는 테스트가 간헐적으로 깨진다.
        Map<String, String> fields = new TreeMap<>();
        for (FieldError error : exc.getBindingResult().getFieldErrors()) {
            fields.put(error.getField(), error.getDefaultMessage());
        }
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("code", "VALIDATION_FAILED");
        body.put("fields", fields);
        return ResponseEntity.badRequest().body(body);
    }
}
