package io.study.testing.ch04_springslice;

public class ArticleNotFoundException extends RuntimeException {

    public ArticleNotFoundException(Long id) {
        super("글을 찾을 수 없다: " + id);
    }
}
