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
    version = "3.2.1-SNAPSHOT"

    repositories {
        mavenCentral()
        mavenLocal()
        maven("https://oss.sonatype.org/content/repositories/snapshots")
    }
}

//  see the issue: https://youtrack.jetbrains.com/issue/KT-49109#focus=Comments-27-5381158.0-0
rootProject.plugins.withType<org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsRootPlugin> {
    rootProject.the<org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsRootExtension>().nodeVersion = "16.13.1"
}
