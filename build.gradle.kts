group = "ru.kami"
version = "0.0.1-SNAPSHOT"
description = "MDC parameters auto injector"

repositories {
	mavenCentral()
}

plugins {
    kotlin("jvm") version "2.3.0"
    kotlin("plugin.spring") version "2.3.0"
    id("org.springframework.boot") version "3.5.7"
    id("io.spring.dependency-management") version "1.1.7"
}

tasks.jar {
    enabled = true
}

tasks.bootJar {
    enabled = false
}
