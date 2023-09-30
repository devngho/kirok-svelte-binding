plugins {
    kotlin("multiplatform") version "1.9.0"
    id("org.jetbrains.dokka") version "1.8.20"
    `maven-publish`
    signing
}

group = "io.github.devngho"
version = "1.0"

repositories {
    mavenCentral()
}

kotlin {
    jvmToolchain(19)

    jvm()
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
            }
        }
        val jvmTest by getting
    }
}

val dokkaHtml by tasks.getting(org.jetbrains.dokka.gradle.DokkaTask::class)

val javadocJar: TaskProvider<Jar> by tasks.registering(Jar::class) {
    dependsOn(dokkaHtml)
    archiveClassifier.set("javadoc")
    from(dokkaHtml.outputDirectory)
}

fun PublishingExtension.kirok() {
    signing {
        sign(publishing.publications)
    }

    repositories {
        if (version.toString().endsWith("SNAPSHOT")) {
            mavenLocal()
        } else {
            maven("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/") {
                name = "sonatypeReleaseRepository"
                credentials(PasswordCredentials::class)
            }
        }
    }

    fun MavenPublication.kirok() {
        pom {
            name.set(artifactId)
            description.set("kirok의 Svelte 바인딩")
            url.set("https://github.com/devngho/kirok-react-binding")


            licenses {
                license {
                    name.set("MIT License")
                    url.set("https://github.com/devngho/kirok-react-binding/blob/master/LICENSE")
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
                connection.set("https://github.com/devngho/kirok-react-binding.git")
                developerConnection.set("https://github.com/devngho/kirok-react-binding.git")
                url.set("https://github.com/devngho/kirok-react-binding")
            }
        }
    }

    publications.create<MavenPublication>("maven") {
        groupId = project.group as String?
        artifactId = "kirok-svelte-binding"
        version = project.version as String?

        artifact(tasks["javadocJar"])

        kirok()
    }
}

kotlin {
    publishing {
        kirok()
    }
}