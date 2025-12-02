group = "ru.kami"
version = "0.0.1-SNAPSHOT"
description = "MDC parameters auto injector"

repositories {
	mavenCentral()
}

plugins {
    kotlin("jvm") version "1.9.25"
    kotlin("plugin.spring") version "1.9.25"
    id("org.springframework.boot") version "3.5.7"
    id("io.spring.dependency-management") version "1.1.7"
}