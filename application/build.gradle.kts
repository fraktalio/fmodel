@Suppress("DSL_SCOPE_VIOLATION") // https://youtrack.jetbrains.com/issue/KTIJ-19369
plugins {
    id(libs.plugins.kotlin.multiplatform.get().pluginId)
    id(libs.plugins.dokka.get().pluginId)
    id("maven-publish")
    id("signing")
}

kotlin {
    jvmToolchain(17)
    jvm()

    js(IR) {
        browser()
        nodejs()
    }

    linuxX64()

    mingwX64()

    macosX64()
    macosArm64()

    tvosSimulatorArm64()

//    watchosArm32()
//    watchosArm64()
    watchosX64()
    watchosSimulatorArm64()

    iosX64()
//    iosArm64()
    iosSimulatorArm64()

    sourceSets {
        commonMain {
            dependencies {
                implementation(libs.coroutines.core)
                api(projects.domain)
            }
        }
    }

    // Dokka
    val dokkaHtml by tasks.getting(org.jetbrains.dokka.gradle.DokkaTask::class)

    val javadocJar: TaskProvider<Jar> by tasks.registering(Jar::class) {
        dependsOn(dokkaHtml)
        archiveClassifier.set("javadoc")
        from(dokkaHtml.outputDirectory)
    }

    signing {
        useInMemoryPgpKeys(
            System.getenv("GPG_PRIVATE_KEY"),
            System.getenv("GPG_PRIVATE_PASSWORD")
        )
        sign(publishing.publications)
    }

    val signingTasks = tasks.withType<Sign>()
    val testTasks = tasks.withType<AbstractTestTask>()

    tasks.withType<AbstractPublishToMaven>().configureEach {
        dependsOn(signingTasks)
    }

    signingTasks.configureEach {
        dependsOn(testTasks)
    }

    // Publishing
    publishing {

        repositories {
            maven {
                name = "ossrh"
                setUrl {
                    if (version.toString().endsWith("SNAPSHOT"))
                        "https://s01.oss.sonatype.org/content/repositories/snapshots"
                    else "https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/"
                }
                credentials {
                    username = System.getenv("MAVEN_USERNAME")
                    password = System.getenv("MAVEN_PASSWORD")
                }
            }
        }

        publications {
            withType<MavenPublication> {
                artifact(javadocJar)
                pom {
                    name.set("fmodel-application")
                    description.set("Fmodel provides just enough tactical Domain-Driven Design patterns, optimised for Event Sourcing and CQRS. The application library orchestrates the execution of the logic by loading state, executing domain components and storing new state.")
                    url.set("https://github.com/fraktalio/fmodel")
                    licenses {
                        license {
                            name.set("The Apache License, Version 2.0")
                            url.set("http://www.apache.org/licenses/LICENSE-2.0")
                        }
                    }
                    inceptionYear.set("2021")
                    issueManagement {
                        system.set("Github")
                        url.set("https://github.com/fraktalio/fmodel/issues")
                    }
                    developers {
                        developer {
                            id.set("idugalic")
                            name.set("Ivan Dugalic")
                            email.set("ivan.dugalic@fraktalio.com")
                        }
                    }
                    scm {
                        connection.set("scm:git:git://github.com/fraktalio/fmodel.git")
                        developerConnection.set("scm:git:git@github.com:fraktalio/fmodel.git")
                        url.set("https://github.com/fraktalio/fmodel.git")
                    }
                }
            }
        }

    }

}

