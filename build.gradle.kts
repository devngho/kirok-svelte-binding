plugins {
    kotlin("multiplatform") version "1.9.0"
    `maven-publish`
    signing
}

group = "io.github.devngho"
version = "1.0"

repositories {
    mavenCentral()
    mavenLocal()
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
                implementation("io.github.devngho:kirok-binding:1.0-SNAPSHOT")
            }
        }
        val jvmTest by getting
    }
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
        kirok()
    }
}

kotlin {
    publishing {
        kirok()
    }
}

java {
    withSourcesJar()
    withJavadocJar()
}