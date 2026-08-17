plugins {
    java
    // Boot 버전은 acttub-platform(apps/api-java)과 같은 3.4.7 로 맞춰 둔다.
    // 여기서 익힌 것을 그대로 옮길 수 있어야 하기 때문이다. Initializr 는 이제
    // 4.1.x 만 주므로, 스켈레톤을 받은 뒤 이 버전과 Gradle 배포판을 함께 내렸다.
    id("org.springframework.boot") version "3.4.7"
    id("io.spring.dependency-management") version "1.1.7"
}

group = "io.study"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-validation")

    // 이 한 줄이 JUnit 5 · AssertJ · Mockito · Spring Test · JSONassert · JsonPath 를
    // 전부 끌고 온다. 01~05 장에서 쓰는 라이브러리 중 따로 추가한 것은 하나도 없다.
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    // 이게 없으면 @ParameterizedTest 의 파라미터 이름이 arg0 으로 나오고,
    // Spring MVC 도 @RequestParam 의 이름을 못 찾아 400 을 낸다.
    options.compilerArgs.add("-parameters")
}

/**
 * 공통 설정. `test` 와 `exercise` 두 태스크가 같은 것을 쓴다.
 */
fun Test.commonSetup() {
    useJUnitPlatform {
        // 태그 필터는 각 태스크가 따로 건다.
    }
    systemProperty("file.encoding", "UTF-8")
    // Java 21 + Mockito 조합에서 나오는 "dynamic agent loading" 경고를 끈다.
    // 경고일 뿐이지만, 매 실행마다 스택이 섞여 나오면 정작 볼 것을 놓친다.
    jvmArgs("-XX:+EnableDynamicAgentLoading")
    testLogging {
        events("passed", "skipped", "failed")
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
    }
}

/**
 * `./gradlew test` — 완성본만 돌린다. **항상 초록이어야 한다.**
 *
 * 연습문제(@Tag("exercise"))는 일부러 실패하도록 두었으므로 여기서 제외한다.
 * 안 그러면 CI 가 영원히 빨간불이라 "깨진 게 정상"인 상태에 익숙해진다 —
 * 그러면 진짜 회귀가 났을 때 아무도 알아채지 못한다.
 */
tasks.named<Test>("test") {
    commonSetup()
    useJUnitPlatform {
        excludeTags("exercise")
    }
}

/**
 * `./gradlew exercise` — 연습문제만 돌린다. **처음에는 빨간불이 정상이다.**
 *
 * 각 장의 `exercise` 패키지에 TODO 가 박힌 테스트가 있다. 그것을 채워서
 * 이 태스크를 초록으로 만드는 것이 학습 루프다. 답은 같은 장의 완성본에 있다.
 */
tasks.register<Test>("exercise") {
    description = "연습문제만 돌린다. 처음에는 실패하는 것이 정상이다."
    group = "verification"
    testClassesDirs = sourceSets["test"].output.classesDirs
    classpath = sourceSets["test"].runtimeClasspath
    commonSetup()
    useJUnitPlatform {
        includeTags("exercise")
    }
    // 연습문제는 실패해도 빌드를 죽이지 않는다. 몇 개가 남았는지 보는 것이 목적이다.
    ignoreFailures = true
    outputs.upToDateWhen { false }
}
