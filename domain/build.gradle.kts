@Suppress("DSL_SCOPE_VIOLATION") // https://youtrack.jetbrains.com/issue/KTIJ-19369
plugins {
    id(libs.plugins.kotlin.multiplatform.get().pluginId)
    id(libs.plugins.kotest.multiplatform.get().pluginId)
    id(libs.plugins.dokka.get().pluginId)
    id("maven-publish")
    id("signing")
}

kotlin {
    jvm {
        compilations.all {
            kotlinOptions.jvmTarget = "1.8"
            kotlinOptions.verbose = true
        }
        //withJava()
        testRuns["test"].executionTask.configure {
            useJUnitPlatform()
            filter {
                isFailOnNoMatchingTests = false
            }
            testLogging {
                showExceptions = true
                showStandardStreams = true
                events = setOf(
                    org.gradle.api.tasks.testing.logging.TestLogEvent.FAILED,
                    org.gradle.api.tasks.testing.logging.TestLogEvent.PASSED
                )
                exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
            }
        }
    }
    js(IR) {
        compilations.all {
            kotlinOptions.verbose = true
            kotlinOptions.sourceMap = true
        }
        browser()
        nodejs()
    }
    val hostOs = System.getProperty("os.name")
    val arch = System.getProperty("os.arch")
    val isMingwX64 = hostOs.startsWith("Windows")
    val nativeTarget = when {
        hostOs == "Mac OS X" ->
            when {
                arch == "aarch64" || arch.startsWith("arm") -> macosArm64()
                else -> macosX64()
            }
        hostOs == "Linux" -> linuxX64()
        isMingwX64 -> mingwX64()
        else -> throw GradleException("Host OS is not supported in Kotlin/Native.")
    }
    nativeTarget.compilations.all {
        kotlinOptions.verbose = true
    }
    nativeTarget.binaries.all {
        freeCompilerArgs += "-Xlazy-ir-for-caches=disable"
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(libs.coroutines.core)
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(libs.kotest.frameworkEngine)
                implementation(libs.kotest.assertionsCore)
            }
        }
        //  val jvmMain by getting
        val jvmTest by getting {
            dependencies {
                runtimeOnly(libs.kotest.runnerJUnit5)
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
                    name.set("fmodel-domain")
                    description.set("Fmodel provides just enough tactical Domain-Driven Design patterns, optimised for Event Sourcing and CQRS. The domain model library is fully isolated from the application layer and API-related concerns. It represents a pure declaration of the program logic.")
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


