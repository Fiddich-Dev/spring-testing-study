package io.study.testing.ch02_assertj;

import java.time.LocalDate;
import java.util.List;

import org.assertj.core.api.AutoCloseableSoftAssertions;
import org.assertj.core.api.SoftAssertions;
import org.assertj.core.api.junit.jupiter.InjectSoftAssertions;
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

/**
 * <h2>02장 ② — 실무에서 차이가 나는 기능들</h2>
 *
 * 여기 있는 넷을 알면 테스트 코드 길이가 눈에 띄게 줄어든다.
 * {@code extracting} · {@code filteredOn} · {@code usingRecursiveComparison} · {@code SoftAssertions}.
 */
@DisplayName("02장 ② AssertJ 심화")
class AssertJAdvancedTest {

    private final List<Member> members = List.of(
            Member.of("김철수", 30, "백엔드"),
            Member.of("이영희", 25, "프론트엔드"),
            Member.of("박민수", 17, "백엔드"),
            Member.of("최지은", 41, "인프라"));

    @Nested
    @DisplayName("extracting — 필드만 뽑아서 비교")
    class Extracting {

        /**
         * 🔥 <b>이것 하나로 for 문이 사라진다.</b>
         *
         * <p>없으면 이렇게 쓰게 된다:
         * <pre>
         *   List&lt;String&gt; names = new ArrayList&lt;&gt;();
         *   for (Member m : members) names.add(m.getName());
         *   assertThat(names).contains("김철수");
         * </pre>
         * 게다가 실패하면 이름 목록만 보이고 <b>어떤 Member 였는지</b>는 안 보인다.
         */
        @Test
        void 필드_하나() {
            assertThat(members)
                    .extracting(Member::getName)
                    .containsExactly("김철수", "이영희", "박민수", "최지은");
        }

        @Test
        void 필드_여럿은_tuple_로() {
            assertThat(members)
                    .extracting(Member::getName, Member::getAge)
                    .contains(
                            tuple("김철수", 30),
                            tuple("박민수", 17));
        }

        /**
         * 문자열로도 뽑을 수 있다(리플렉션). ⚠ 하지만 <b>메서드 참조를 권한다</b> —
         * 문자열은 필드명을 바꿔도 컴파일러가 안 잡아 준다. 런타임에야 깨진다.
         */
        @Test
        void 문자열_추출은_타입_안전하지_않다() {
            assertThat(members)
                    .extracting("team", String.class)
                    .contains("인프라");
        }

        @Test
        void 단일_객체에서도_쓴다() {
            Member member = members.get(0);

            assertThat(member)
                    .extracting(Member::getName, Member::getTeam)
                    .containsExactly("김철수", "백엔드");
        }
    }

    @Nested
    @DisplayName("filteredOn — 거르고 나서 검증")
    class Filtering {

        @Test
        void 조건으로_거른다() {
            assertThat(members)
                    .filteredOn(Member::isAdult)
                    .hasSize(3)
                    .extracting(Member::getName)
                    .doesNotContain("박민수");
        }

        @Test
        void 필드값으로_거른다() {
            assertThat(members)
                    .filteredOn("team", "백엔드")
                    .extracting(Member::getName)
                    .containsExactly("김철수", "박민수");
        }

        /**
         * 📌 <b>{@code filteredOn} + {@code extracting} 조합이 실무 주력이다.</b>
         * "특정 조건의 것들만 골라서, 그중 이 필드가 이러이러한가" 가 검증의 흔한 모양이라서다.
         */
        @Test
        void 조합() {
            assertThat(members)
                    .filteredOn(m -> m.getAge() >= 30)
                    .extracting(Member::getName, Member::getTeam)
                    .containsExactlyInAnyOrder(
                            tuple("김철수", "백엔드"),
                            tuple("최지은", "인프라"));
        }
    }

    @Nested
    @DisplayName("usingRecursiveComparison — equals 없이 객체 비교")
    class RecursiveComparison {

        /**
         * 🔥 {@code Member} 는 <b>equals 를 만들지 않았다</b>(일부러). 그래서 참조 비교가 된다.
         */
        @Test
        void equals가_없으면_isEqualTo가_실패한다() {
            Member a = new Member("김철수", 30, "백엔드", LocalDate.of(2026, 1, 1));
            Member b = new Member("김철수", 30, "백엔드", LocalDate.of(2026, 1, 1));

            assertThat(a).isNotEqualTo(b);   // 내용은 같은데 다른 객체다
        }

        /**
         * 📌 <b>필드를 하나씩 재귀적으로 비교한다.</b> 테스트만을 위해 프로덕션 코드에
         * equals 를 만드는 것은 본말전도다 — equals 는 도메인 동일성 규칙이지 테스트 도구가 아니다.
         */
        @Test
        void 재귀_비교는_통과한다() {
            Member a = new Member("김철수", 30, "백엔드", LocalDate.of(2026, 1, 1));
            Member b = new Member("김철수", 30, "백엔드", LocalDate.of(2026, 1, 1));

            assertThat(a).usingRecursiveComparison().isEqualTo(b);
        }

        /**
         * 📌 <b>가장 자주 쓰는 형태</b>: 생성 시각·자동 채번 ID 처럼
         * <b>미리 알 수 없는 필드</b>만 빼고 비교한다.
         */
        @Test
        void 일부_필드를_뺀다() {
            Member expected = new Member("김철수", 30, "백엔드", null);
            Member actual = new Member("김철수", 30, "백엔드", LocalDate.now());

            assertThat(actual)
                    .usingRecursiveComparison()
                    .ignoringFields("joinedAt")
                    .isEqualTo(expected);
        }

        @Test
        void 컬렉션에도_쓴다() {
            List<Member> expected = List.of(
                    new Member("김철수", 30, "백엔드", null),
                    new Member("이영희", 25, "프론트엔드", null));

            assertThat(members.subList(0, 2))
                    .usingRecursiveFieldByFieldElementComparatorIgnoringFields("joinedAt")
                    .containsExactlyElementsOf(expected);
        }
    }

    @Nested
    @DisplayName("SoftAssertions — 실패를 모아서 보고")
    class Soft {

        /**
         * 🔥 <b>왜 필요한가</b>: 일반 단언은 첫 실패에서 멈춘다. 응답 객체의 필드 열 개를
         * 검증하는데 첫 필드가 틀리면, 나머지 아홉 개도 틀렸는지는 <b>고치고 다시 돌려야</b> 안다.
         * 그게 다섯 번 반복되면 다섯 판이다.
         *
         * <p>⚠ <b>{@code assertAll()} 을 부르지 않으면 아무것도 검증되지 않는다.</b>
         * 이걸 빠뜨리면 테스트가 <b>영원히 초록</b>이 된다 — 가장 위험한 종류의 실수다.
         * 아래 {@code @ExtendWith(SoftAssertionsExtension.class)} 방식이 이 실수를 원천 차단한다.
         */
        @Test
        void 수동_방식() {
            Member member = members.get(0);

            SoftAssertions softly = new SoftAssertions();
            softly.assertThat(member.getName()).isEqualTo("김철수");
            softly.assertThat(member.getAge()).isEqualTo(30);
            softly.assertThat(member.getTeam()).isEqualTo("백엔드");
            softly.assertThat(member.isAdult()).isTrue();
            softly.assertAll();   // ⚠ 이 줄이 없으면 위 넷은 아무 일도 하지 않는다
        }

        /**
         * ⚠ <b>{@code SoftAssertions} 자체는 {@code AutoCloseable} 이 아니다.</b>
         * try-with-resources 에 넣으려면 <b>{@code AutoCloseableSoftAssertions}</b> 를 써야 한다.
         * 이름이 비슷해 헷갈리기 쉽다.
         */
        @Test
        void try_with_resources_로도_된다() {
            Member member = members.get(0);

            // 블록을 벗어날 때 assertAll 이 자동으로 불린다.
            try (AutoCloseableSoftAssertions softly = new AutoCloseableSoftAssertions()) {
                softly.assertThat(member.getName()).isEqualTo("김철수");
                softly.assertThat(member.getAge()).isEqualTo(30);
            }
        }
    }

    /**
     * 📌 <b>가장 안전한 SoftAssertions</b> — 확장이 {@code assertAll()} 을 대신 불러 준다.
     * 부르는 것을 잊을 수가 없다.
     */
    @Nested
    @ExtendWith(SoftAssertionsExtension.class)
    @DisplayName("SoftAssertions (확장 주입 방식)")
    class SoftWithExtension {

        @InjectSoftAssertions
        private SoftAssertions softly;

        @Test
        void assertAll을_부를_필요가_없다() {
            Member member = members.get(0);

            softly.assertThat(member.getName()).isEqualTo("김철수");
            softly.assertThat(member.getAge()).isEqualTo(30);
            softly.assertThat(member.getTeam()).isEqualTo("백엔드");
        }
    }

    @Nested
    @DisplayName("as / describedAs — 실패 메시지에 맥락 붙이기")
    class Describing {

        /**
         * 📌 <b>{@code as()} 는 반드시 단언 <u>앞</u>에 와야 한다.</b>
         * 뒤에 쓰면 이미 검증이 끝난 뒤라 아무 효과가 없다 — 조용히 무시되는 종류의 실수다.
         *
         * <p>루프 안에서 같은 단언을 돌릴 때 특히 값어치가 있다. 어느 반복에서 깨졌는지
         * 메시지만 보고 알 수 있다.
         */
        @Test
        void 맥락을_붙인다() {
            for (Member member : members) {
                assertThat(member.getName())
                        .as("회원 [%s] 의 이름", member.getName())   // ← 단언 앞
                        .isNotBlank();
            }
        }
    }
}
