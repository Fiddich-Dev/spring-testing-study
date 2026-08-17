package io.study.testing.ch02_assertj;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.assertj.core.api.Assertions.entry;
import static org.assertj.core.api.Assertions.within;

/**
 * <h2>02장 ① — AssertJ 기본</h2>
 *
 * 📌 <b>규칙은 하나뿐이다</b>: {@code assertThat(실제값).검증(기대값)}.
 * 항상 실제값이 앞이라, JUnit 기본 단언에서 자주 나는 인자 순서 실수가 생기지 않는다.
 *
 * <p>🔥 <b>AssertJ 의 진짜 값어치는 실패 메시지에 있다.</b> {@code assertTrue(list.contains(x))}
 * 는 실패하면 "expected true but was false" 만 남기고 리스트에 뭐가 들었는지 안 알려준다.
 * AssertJ 는 실제 내용을 통째로 찍어 준다 — 디버깅이 한 판 줄어든다.
 *
 * <p>💡 IDE 에서 {@code assertThat(값).} 까지 치고 자동완성을 열면 그 타입에 쓸 수 있는
 * 검증이 전부 뜬다. 외울 필요가 없다는 것이 이 라이브러리의 설계 의도다.
 */
@DisplayName("02장 ① AssertJ 기본")
class AssertJBasicsTest {

    @Nested
    @DisplayName("객체")
    class Objects {

        @Test
        void 기본() {
            String value = "hello";

            assertThat(value)
                    .isNotNull()
                    .isEqualTo("hello")
                    .isNotEqualTo("world")
                    .isInstanceOf(String.class);
        }

        @Test
        void isEqualTo_와_isSameAs_는_다르다() {
            String a = new String("hello");
            String b = new String("hello");

            assertThat(a).isEqualTo(b);      // equals 비교 → 통과
            assertThat(a).isNotSameAs(b);    // 참조 비교 → 다른 객체
        }

        @Test
        void isIn_과_isNotIn() {
            assertThat("SPRING").isIn("SPRING", "SUMMER");
            assertThat("SPRING").isNotIn("AUTUMN", "WINTER");
        }
    }

    @Nested
    @DisplayName("문자열")
    class Strings {

        @Test
        void 자주_쓰는_것들() {
            String value = "Spring Testing Study";

            assertThat(value)
                    .startsWith("Spring")
                    .endsWith("Study")
                    .contains("Testing")
                    .doesNotContain("JUnit4")
                    .hasSize(20)
                    .matches("Spring .+ Study")
                    .isEqualToIgnoringCase("SPRING TESTING STUDY");
        }

        @Test
        void 빈값_계열_셋을_구별한다() {
            // 🔥 이 셋의 차이가 버그를 가른다.
            assertThat("").isEmpty();          // 길이 0
            assertThat("   ").isNotEmpty();    // 공백도 문자다 — isEmpty 는 거짓
            assertThat("   ").isBlank();       // 공백만 있으면 blank
            assertThat("").isBlank();          // 빈 것도 blank

            assertThat((String) null).isNullOrEmpty();
        }

        @Test
        void 여러_조각을_한번에() {
            String log = "2026-08-17 ERROR 결제 실패 orderId=1024";

            assertThat(log)
                    .containsSubsequence("ERROR", "결제", "orderId")  // 이 **순서로** 나타난다
                    .containsPattern("orderId=\\d+");
        }
    }

    @Nested
    @DisplayName("숫자")
    class Numbers {

        @Test
        void 기본() {
            assertThat(42)
                    .isPositive()
                    .isGreaterThan(40)
                    .isLessThanOrEqualTo(42)
                    .isBetween(40, 50);

            assertThat(0).isZero();
            assertThat(-1).isNegative();
        }

        @Test
        void 실수는_isCloseTo_로_비교한다() {
            double result = 0.1 + 0.2;

            // 🔥 assertThat(result).isEqualTo(0.3) 은 **실패한다**. 0.30000000000000004 다.
            //    부동소수점을 isEqualTo 로 비교하는 것은 거의 항상 버그다.
            assertThat(result).isCloseTo(0.3, within(0.0001));
        }
    }

    @Nested
    @DisplayName("컬렉션")
    class Collections {

        private final List<String> teams = List.of("백엔드", "프론트엔드", "인프라");

        @Test
        void 크기와_포함() {
            assertThat(teams)
                    .hasSize(3)
                    .isNotEmpty()
                    .contains("인프라")
                    .doesNotContain("디자인");
        }

        /**
         * 🔥 <b>가장 자주 틀리는 자리.</b> 셋의 차이를 확실히 해 둔다.
         */
        @Test
        void contains_계열_셋의_차이() {
            // contains — 들어 있기만 하면 된다. 순서·개수 무관, 다른 원소가 있어도 통과
            assertThat(teams).contains("인프라", "백엔드");

            // containsExactly — 순서까지 정확히 일치. 하나라도 순서가 다르면 실패
            assertThat(teams).containsExactly("백엔드", "프론트엔드", "인프라");

            // containsExactlyInAnyOrder — 원소 집합은 같고 순서만 무관
            assertThat(teams).containsExactlyInAnyOrder("인프라", "백엔드", "프론트엔드");

            // ⚠ Set·Map 처럼 순서가 보장되지 않는 자료구조에 containsExactly 를 쓰면
            //    지금은 통과하다가 JDK 버전이나 원소가 바뀌면 깨진다. InAnyOrder 를 쓴다.
            assertThat(java.util.Set.of("a", "b")).containsExactlyInAnyOrder("b", "a");
        }

        @Test
        void 모두_어떤_조건을_만족하는지() {
            assertThat(teams)
                    .allMatch(name -> !name.isBlank())
                    .anyMatch(name -> name.startsWith("백"))
                    .noneMatch(name -> name.contains("디자인"));
        }

        @Test
        void 원소마다_여러_조건을_보려면_satisfies() {
            assertThat(teams).allSatisfy(name -> {
                assertThat(name).isNotBlank();
                assertThat(name).hasSizeLessThan(10);
            });

            // 정확히 하나만 조건을 만족하는지
            assertThat(teams).satisfiesOnlyOnce(name -> assertThat(name).isEqualTo("인프라"));
        }
    }

    @Nested
    @DisplayName("Map")
    class Maps {

        private final Map<String, Integer> scores = Map.of("국어", 90, "수학", 100);

        @Test
        void 기본() {
            assertThat(scores)
                    .hasSize(2)
                    .containsKey("수학")
                    .containsValue(100)
                    .doesNotContainKey("영어")
                    .containsEntry("국어", 90)
                    .contains(entry("수학", 100));
        }

        @Test
        void 값만_꺼내_보기() {
            assertThat(scores).extractingByKey("수학").isEqualTo(100);
            assertThat(scores.values()).allMatch(score -> score >= 90);
        }
    }

    @Nested
    @DisplayName("Optional")
    class Optionals {

        @Test
        void 기본() {
            Optional<String> present = Optional.of("값");
            Optional<String> empty = Optional.empty();

            assertThat(present)
                    .isPresent()
                    .contains("값")               // 내용까지 확인
                    .hasValueSatisfying(v -> assertThat(v).hasSize(1));

            assertThat(empty).isEmpty();

            // ⚠ present.get() 을 꺼내 비교해도 되지만, Optional 그대로 넘기면
            //    비어 있을 때 NoSuchElementException 대신 읽을 수 있는 실패 메시지가 나온다.
        }
    }

    @Nested
    @DisplayName("예외")
    class Exceptions {

        /**
         * ⚠ <b>이 테스트 메서드의 이름을 {@code assertThatThrownBy} 로 짓지 말 것.</b>
         * 같은 이름의 static import 가 <b>가려져서</b> 컴파일이 안 된다
         * ("required: no arguments"). 실제로 이 레포를 만들 때 겪은 실수다 —
         * 자바의 이름 해석이 <b>클래스 멤버를 static import 보다 먼저</b> 보기 때문이다.
         */
        @Test
        void 기본_형태() {
            assertThatThrownBy(() -> { throw new IllegalArgumentException("나이는 음수일 수 없다"); })
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("나이는 음수일 수 없다")
                    .hasMessageContaining("음수");
        }

        @Test
        void 타입별_전용_메서드도_있다() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> { throw new IllegalArgumentException("bad"); })
                    .withMessage("bad");

            assertThatExceptionOfType(IllegalStateException.class)
                    .isThrownBy(() -> { throw new IllegalStateException("나쁜 상태"); })
                    .withMessageContaining("상태");
        }

        @Test
        void 원인까지_확인한다() {
            Exception cause = new IllegalArgumentException("근본 원인");
            assertThatThrownBy(() -> { throw new RuntimeException("겉면", cause); })
                    .hasCauseInstanceOf(IllegalArgumentException.class)
                    .hasRootCauseMessage("근본 원인");
        }

        /**
         * 🔥 <b>가장 흔한 함정</b>: 예외가 <b>안</b> 터지는 것을 검증하려 할 때.
         *
         * <p>{@code assertThatThrownBy} 는 람다가 예외를 안 던지면 "예외를 기대했는데 없다" 로
         * <b>실패</b>한다. "안 터진다"를 확인하려면 {@code assertThatCode(...).doesNotThrowAnyException()}
         * 이나 {@code catchThrowable} 을 쓴다.
         */
        @Test
        void 예외가_안_나는_것을_검증할_때() {
            Throwable thrown = catchThrowable(() -> Integer.parseInt("42"));
            assertThat(thrown).isNull();

            org.assertj.core.api.Assertions
                    .assertThatCode(() -> Integer.parseInt("42"))
                    .doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("날짜")
    class Dates {

        @Test
        void 기본() {
            LocalDate date = LocalDate.of(2026, 8, 17);

            assertThat(date)
                    .isAfter(LocalDate.of(2026, 1, 1))
                    .isBefore(LocalDate.of(2027, 1, 1))
                    .isEqualTo("2026-08-17")   // 문자열로도 비교된다
                    .hasYear(2026)
                    .hasMonthValue(8);
        }
    }
}
