import org.gradle.api.tasks.testing.logging.TestLogEvent

plugins {
    java
    id("org.springframework.boot") version "3.2.5"
    id("io.spring.dependency-management") version "1.1.5"
}

group = "com.recipemanager"
version = "1.0.0"

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

repositories {
    mavenCentral()
}

val jooqVersion = "3.19.8"
extra["jooq.version"] = jooqVersion
val jooqCodegen: Configuration by configurations.creating

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-jooq")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.flywaydb:flyway-core")
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.5.0")
    implementation("io.micrometer:micrometer-registry-prometheus")

    implementation("io.jsonwebtoken:jjwt-api:0.12.5")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.12.5")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.12.5")
    runtimeOnly("org.postgresql:postgresql")

    jooqCodegen("org.jooq:jooq-codegen:$jooqVersion")
    jooqCodegen("org.jooq:jooq-meta-extensions:$jooqVersion")
    jooqCodegen("com.h2database:h2:2.2.224")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.security:spring-security-test")

    testRuntimeOnly("com.h2database:h2")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

val generateJooq = tasks.register<JavaExec>("generateJooq") {
    mainClass.set("org.jooq.codegen.GenerationTool")
    classpath = jooqCodegen
    args = listOf(file("jooq-config.xml").absolutePath)
    inputs.files(fileTree("src/main/resources/db/migration") { include("*.sql") })
    inputs.file("jooq-config.xml")
    outputs.dir(layout.buildDirectory.dir("generated-sources/jooq"))
}

tasks.named<JavaCompile>("compileJava") {
    dependsOn(generateJooq)
}

sourceSets.named("main") {
    java.srcDir(layout.buildDirectory.dir("generated-sources/jooq"))
}

tasks.withType<Test> {
    useJUnitPlatform()
    environment("TESTCONTAINERS_RYUK_DISABLED", "true")
    testLogging {
        events(TestLogEvent.FAILED, TestLogEvent.SKIPPED)
    }
}
