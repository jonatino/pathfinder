import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.4.0"
    id("me.champeau.gradle.jmh") version "0.5.2"
}

group = "org.rsmod"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
    jmh("com.fasterxml.jackson.module:jackson-module-kotlin:2.11.3")
    testImplementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.11.3")
    testImplementation("org.junit.jupiter:junit-jupiter:5.7.0")
}

kotlin {
    explicitApi()
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        jvmTarget = "11"
        freeCompilerArgs = listOf("-XXLanguage:+InlineClasses")
    }
}
