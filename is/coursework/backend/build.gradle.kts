import org.gradle.api.tasks.testing.logging.TestLogEvent

plugins {
    id("java")
    id("war")
    id("io.spring.dependency-management") version "1.1.7"
    id("org.springframework.boot") version "3.5.4"
}

allprojects {
    group = "ru.itmo.se.is.cw"
    version = "1.0-SNAPSHOT"
    repositories {
        mavenCentral()
    }
}

subprojects {

    apply(plugin = "java")
    apply(plugin = "war")
    apply(plugin = "io.spring.dependency-management")
    apply(plugin = "org.springframework.boot")

    dependencyManagement {
        imports {
            mavenBom("org.springframework.cloud:spring-cloud-dependencies:2025.0.0")
        }
    }

    dependencies {
        implementation("org.springframework.boot:spring-boot-starter-web")
        implementation("org.springframework.boot:spring-boot-starter-aop")
        implementation("org.springframework.boot:spring-boot-starter-security")
        implementation("org.springframework.boot:spring-boot-starter-validation")

        implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.11")

        compileOnly("org.projectlombok:lombok")
        annotationProcessor("org.projectlombok:lombok")

        implementation("org.mapstruct:mapstruct:1.6.0.Beta1")
        annotationProcessor("org.mapstruct:mapstruct-processor:1.6.0.Beta1")

        testImplementation("org.springframework.boot:spring-boot-starter-test")
        testImplementation("org.springframework.security:spring-security-test")
        testImplementation("org.testcontainers:junit-jupiter")
        testImplementation("org.springframework.boot:spring-boot-testcontainers")
    }

    tasks.test {
        useJUnitPlatform()

        testLogging {
            events(
                TestLogEvent.STARTED,
                TestLogEvent.PASSED,
                TestLogEvent.SKIPPED,
                TestLogEvent.FAILED,
            )
            exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
        }
    }
}