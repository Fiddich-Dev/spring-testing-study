package io.study.testing.ch01_junit5;

/**
 * 01장 테스트 대상. 일부러 아주 단순하게 두었다 —
 * 이 장에서 배울 것은 계산기가 아니라 <b>JUnit 5 의 실행 규칙</b>이기 때문이다.
 */
public class Calculator {

    private int memory;

    public int add(int a, int b) {
        return a + b;
    }

    public int divide(int a, int b) {
        if (b == 0) {
            throw new ArithmeticException("0 으로 나눌 수 없다");
        }
        return a / b;
    }

    public boolean isEven(int value) {
        return value % 2 == 0;
    }

    /** 라이프사이클 어노테이션의 효과를 눈으로 보기 위한 가변 상태. */
    public void remember(int value) {
        this.memory += value;
    }

    public int memory() {
        return memory;
    }
}
