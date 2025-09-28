import com.vanniktech.maven.publish.JavadocJar
import com.vanniktech.maven.publish.KotlinMultiplatform

plugins {
    id(libs.plugins.kotlin.multiplatform.get().pluginId)
    id(libs.plugins.kotest.get().pluginId)
    id(libs.plugins.ksp.get().pluginId)
    id(libs.plugins.dokka.get().pluginId)
    id(libs.plugins.vanniktech.mavenPublish.get().pluginId)
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
    watchosX64()
    watchosSimulatorArm64()
    iosX64()
    iosSimulatorArm64()

    sourceSets {
        commonMain {
            dependencies {
                implementation(libs.coroutines.core)
                api(projects.application)
            }
        }
        commonTest {
            dependencies {
                implementation(libs.kotest.frameworkEngine)
                implementation(libs.kotest.assertionsCore)
            }
        }

        jvmTest {
            dependencies {
                runtimeOnly(libs.kotest.runnerJUnit5)
            }
        }
    }

    mavenPublishing {
        configure(
            KotlinMultiplatform(
                // configures the -javadoc artifact, possible values:
                // - `JavadocJar.None()` don't publish this artifact
                // - `JavadocJar.Empty()` publish an empty jar
                // - `JavadocJar.Dokka("dokkaHtml")` when using Kotlin with Dokka, where `dokkaHtml` is the name of the Dokka task that should be used as input
                javadocJar = JavadocJar.Dokka("dokkaHtml"),
                // whether to publish a sources jar
                sourcesJar = true,
            )
        )
        publishToMavenCentral()

        signAllPublications()


        coordinates(group.toString(), "application-vanilla", version.toString())
        pom {
            name.set("fmodel-application-vanilla")
            description.set("Fmodel provides just enough tactical Domain-Driven Design patterns, optimised for Event Sourcing and CQRS. The application library orchestrates the execution of the logic by loading state, executing domain components and storing new state. It is a vanilla, native Kotlin implementation.")
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

