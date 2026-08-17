package io.study.testing.ch04_springslice;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 검증 실패가 어떤 응답이 되는지를 보려고 제약을 걸어 두었다.
 * 04장의 "400 응답 본문 검증" 절이 이 어노테이션들 위에 서 있다.
 */
public record ArticleCreateRequest(

        @NotBlank(message = "제목은 비어 있을 수 없다")
        @Size(max = 100, message = "제목은 100자를 넘을 수 없다")
        String title,

        @NotBlank(message = "본문은 비어 있을 수 없다")
        String content,

        @NotBlank(message = "작성자는 비어 있을 수 없다")
        String author) {
}
