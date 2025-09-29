plugins {
    base
    id("org.jetbrains.kotlinx.binary-compatibility-validator") version "0.18.1"
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.kotest) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.vanniktech.mavenPublish) apply false
    alias(libs.plugins.dokka)
}

allprojects {
    group = "com.fraktalio.fmodel"
    version = "3.6.1"

    repositories {
        mavenCentral()
        mavenLocal()
    }
}

