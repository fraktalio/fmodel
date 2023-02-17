@Suppress("DSL_SCOPE_VIOLATION") // https://youtrack.jetbrains.com/issue/KTIJ-19369
plugins {
    base
    id("maven-publish")
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.kotest.multiplatform) apply false
    alias(libs.plugins.dokka)
}

allprojects {
    group = "com.fraktalio.fmodel"
    version = "3.3.1-SNAPSHOT"

    repositories {
        mavenCentral()
        mavenLocal()
    }
}

