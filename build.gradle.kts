import org.jetbrains.kotlin.gradle.targets.js.dsl.ExperimentalWasmDsl

plugins {
    kotlin("multiplatform") version "1.9.0"
    id("org.jetbrains.dokka") version "1.9.0"
    `maven-publish`
    signing
}

group = "io.github.devngho"
version = "1.0.1"

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
    jvmToolchain(19)

    jvm {
        withJava()
        tasks.withType<Test>().configureEach {
            useJUnitPlatform()
        }
    }

    @OptIn(ExperimentalWasmDsl::class)
    wasm {
        binaries.executable()
        browser {}
        applyBinaryen()
    }

    sourceSets {
        val commonMain by getting
        val commonTest by getting
        val jvmMain by getting {
            dependencies {
                implementation("io.github.devngho:kirok-binding:1.0")
                implementation(kotlin("reflect"))
            }
        }
        val jvmTest by getting {
            dependencies {
                val kotestVersion = "5.7.2"
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
            maven("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/") {
                name = "sonatypeReleaseRepository"
                credentials(PasswordCredentials::class)
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
    getByName("signKotlinMultiplatformPublication") {
        dependsOn(
            "publishJvmPublicationToSonatypeReleaseRepositoryRepository",
            "publishJvmPublicationToMavenLocalRepository"
        )
//        dependsOn("publishJvmPublicationToMavenLocalRepository")
    }

    getByName("signWasmPublication") {
        dependsOn(
            "publishJvmPublicationToSonatypeReleaseRepositoryRepository",
            "publishKotlinMultiplatformPublicationToSonatypeReleaseRepositoryRepository",
            "publishKotlinMultiplatformPublicationToMavenLocalRepository",
        )
//        dependsOn(
//            "publishKotlinMultiplatformPublicationToMavenLocalRepository",
//        )
    }
}