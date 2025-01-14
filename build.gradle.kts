@file:OptIn(ExperimentalWasmDsl::class)

import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    kotlin("multiplatform") version "2.1.0"
    id("org.jetbrains.dokka") version "2.0.0"
    `maven-publish`
    signing
}

group = "io.github.devngho"
version = "1.1.4"

repositories {
    mavenCentral()
    mavenLocal()
}

val dokkaHtml by tasks.getting(org.jetbrains.dokka.gradle.DokkaTask::class)

val javadocJar: TaskProvider<Jar> by tasks.registering(Jar::class) {
    dependsOn(dokkaHtml)
    archiveClassifier.set("javadoc")
    from(dokkaHtml.outputDirectory)
}

kotlin {
    withSourcesJar(true)
    jvmToolchain(21)

    jvm {
        withJava()
        tasks.withType<Test>().configureEach {
            useJUnitPlatform()
        }
    }

    wasmJs {
        binaries.executable()
        browser {}
    }

    sourceSets {
        val commonMain by getting
        val commonTest by getting
        val jvmMain by getting {
            dependencies {
                implementation("io.github.devngho:kirok-binding:1.1.3")
                implementation(kotlin("reflect"))
            }
        }
        val jvmTest by getting {
            dependencies {
                val kotestVersion = "5.9.1"
                implementation("io.kotest:kotest-runner-junit5:$kotestVersion")
                implementation("io.kotest:kotest-assertions-core:$kotestVersion")
            }
        }
    }
}

publishing {
    signing {
        sign(publishing.publications)
    }

    repositories {
        mavenLocal()
        if (!version.toString().endsWith("SNAPSHOT")) {
            val id: String =
                if (project.hasProperty("repoUsername")) project.property("repoUsername") as String
                else System.getenv("repoUsername")
            val pw: String =
                if (project.hasProperty("repoPassword")) project.property("repoPassword") as String
                else System.getenv("repoPassword")
            if (!version.toString().endsWith("SNAPSHOT")) {
                maven("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/") {
                    name = "sonatypeReleaseRepository"
                    credentials {
                        username = id
                        password = pw
                    }
                }
            }
        }
    }

    publications.withType(MavenPublication::class) {
        groupId = project.group as String?
        version = project.version as String?

        artifact(javadocJar)

        pom {
            name.set("kirok-svelte-binding")
            description.set("kirok의 Svelte 바인딩")
            url.set("https://github.com/devngho/kirok-svelte-binding")


            licenses {
                license {
                    name.set("MIT License")
                    url.set("https://github.com/devngho/kirok-svelte-binding/blob/master/LICENSE")
                }
            }
            developers {
                developer {
                    id.set("devngho")
                    name.set("devngho")
                    email.set("yjh135908@gmail.com")
                }
            }
            scm {
                connection.set("https://github.com/devngho/kirok-svelte-binding.git")
                developerConnection.set("https://github.com/devngho/kirok-svelte-binding.git")
                url.set("https://github.com/devngho/kirok-svelte-binding")
            }
        }
    }
}

tasks {
    val taskList = this.toList().map { it.name }
    getByName("signKotlinMultiplatformPublication") {
        if (taskList.contains("publishJvmPublicationToSonatypeReleaseRepositoryRepository"))
            dependsOn(
                "publishJvmPublicationToSonatypeReleaseRepositoryRepository",
                "publishJvmPublicationToMavenLocalRepository",
                "publishJvmPublicationToMavenLocal"
            )
        else dependsOn("publishJvmPublicationToMavenLocalRepository", "publishJvmPublicationToMavenLocal")
    }
    getByName("signWasmJsPublication") {
        if (taskList.contains("publishJvmPublicationToSonatypeReleaseRepositoryRepository"))
            dependsOn(
                "publishJvmPublicationToSonatypeReleaseRepositoryRepository",
                "publishKotlinMultiplatformPublicationToSonatypeReleaseRepositoryRepository",
                "publishJvmPublicationToMavenLocal",
                "publishJvmPublicationToMavenLocalRepository",
                "publishKotlinMultiplatformPublicationToMavenLocalRepository"
            )
        else
            dependsOn(
                "publishJvmPublicationToMavenLocal",
                "publishKotlinMultiplatformPublicationToMavenLocal",
                "publishKotlinMultiplatformPublicationToMavenLocalRepository"
            )
    }
}