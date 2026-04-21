plugins {
    id("java")
}

group = "ru.itmo.tpo"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.junit.jupiter:junit-jupiter-params")
    testImplementation("org.seleniumhq.selenium:selenium-java:4.25.0")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
    systemProperty("browsers", System.getProperty("browsers", "chrome,firefox"))
    systemProperty("selenium.remoteUrl", System.getProperty("selenium.remoteUrl", ""))
    systemProperty("headless", System.getProperty("headless", "true"))
    systemProperty("baseUrl", System.getProperty("baseUrl", "https://mirtesen.ru/"))
}