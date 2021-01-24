import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val ossrhUsername: String? by ext
val ossrhPassword: String? by ext

group = "org.rsmod"
version = "1.0.0"
description = "A breadth-first search path finder"

plugins {
    `maven-publish`
    signing
    kotlin("jvm") version "1.4.0"
    id("me.champeau.gradle.jmh") version "0.5.2"
    id("net.researchgate.release") version "2.8.1"
}

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

jmh {
    profilers = listOf("stack")
}

java {
    withJavadocJar()
    withSourcesJar()
}

publishing {
    repositories {
        maven {
            if (version.toString().endsWith("SNAPSHOT")) {
                setUrl("https://oss.sonatype.org/content/repositories/snapshots")
            } else {
                setUrl("https://oss.sonatype.org/service/local/staging/deploy/maven2")
            }
            credentials {
                username = ossrhUsername
                password = ossrhPassword
            }
        }
    }

    publications.withType<MavenPublication> {
        pom {
            name.set("RSMod PathFinder")
            description.set(project.description)
            url.set("https://github.com/rsmod/pathfinder")

            licenses {
                license {
                    name.set("ISC License")
                    url.set("https://opensource.org/licenses/ISC")
                }
            }

            scm {
                connection.set("scm:git:git://github.com/rsmod/pathfinder.git")
                developerConnection.set("scm:git:git@github.com:github.com/rsmod/pathfinder.git")
                url.set("https://github.com/rsmod/pathfinder")
            }

            developers {
                developer {
                    id.set("tom")
                    name.set("Tomm")
                }
            }
        }
    }
}

signing {
    useGpgCmd()
    sign(publishing.publications)
}

tasks.afterReleaseBuild {
    dependsOn(tasks.publish)
}
