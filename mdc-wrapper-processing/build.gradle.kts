plugins {
    kotlin("jvm")
    kotlin("plugin.spring")
    id("io.spring.dependency-management")
}

repositories {
    mavenCentral()
}

val springVersion = "6.2.12"

dependencies {
    api(project(":mdc-wrapper-api"))

    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.springframework:spring-aop:$springVersion")
    implementation("org.springframework:spring-expression:$springVersion")
    implementation("org.springframework:spring-context:$springVersion")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    implementation("io.github.microutils:kotlin-logging:3.0.5")
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(25)
}