package io.study.testing.ch04_springslice;

import java.time.LocalDateTime;

public record Article(Long id, String title, String content, String author, LocalDateTime createdAt) {

    public static Article of(Long id, String title, String author) {
        return new Article(id, title, title + " 의 본문", author, LocalDateTime.of(2026, 8, 17, 10, 0));
    }
}
