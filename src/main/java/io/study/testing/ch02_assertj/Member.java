package io.study.testing.ch02_assertj;

import java.time.LocalDate;
import java.util.Objects;

/**
 * 02장 테스트 대상.
 *
 * <p><b>일부러 record 가 아니라 class 로 두고 equals 를 만들지 않았다.</b>
 * AssertJ 의 {@code usingRecursiveComparison()} 이 왜 필요한지를 보려면
 * "equals 가 없어서 isEqualTo 가 실패하는 객체"가 하나 있어야 한다.
 */
public class Member {

    private final String name;
    private final int age;
    private final String team;
    private final LocalDate joinedAt;

    public Member(String name, int age, String team, LocalDate joinedAt) {
        this.name = name;
        this.age = age;
        this.team = team;
        this.joinedAt = joinedAt;
    }

    public static Member of(String name, int age, String team) {
        return new Member(name, age, team, LocalDate.of(2026, 1, 1));
    }

    public String getName() {
        return name;
    }

    public int getAge() {
        return age;
    }

    public String getTeam() {
        return team;
    }

    public LocalDate getJoinedAt() {
        return joinedAt;
    }

    public boolean isAdult() {
        return age >= 19;
    }

    @Override
    public String toString() {
        return "Member{name='" + name + "', age=" + age + ", team='" + team + "'}";
    }

    /**
     * equals 를 <b>일부러 만들지 않았다.</b> Objects.equals 는 여기서 참조 비교가 된다.
     * 02장의 {@code usingRecursiveComparison} 절이 이 사실 위에 서 있다.
     */
    @SuppressWarnings("unused")
    private boolean sameIdentity(Member other) {
        return Objects.equals(this.name, other.name);
    }
}
