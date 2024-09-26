import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    `java-gradle-plugin`
    `kotlin-dsl`
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
}

group = "com.freesky1102.dependencies"
version = "SNAPSHOT"

dependencies {
    compileOnly(gradleApi())
    compileOnly(libs.android.gradlePlugin)
    compileOnly(libs.android.tools.common)
    compileOnly(libs.sonarqube.gradlePlugin)
    compileOnly(libs.kotlin.gradlePlugin)
    compileOnly(libs.detekt.gradlePlugin)
    implementation(libs.kotlinx.serialization.json)
}

gradlePlugin {
    plugins {

        create("simplePlugin") {
            id = "freesky1102.dependencies"
            implementationClass = "VersionBuildPlugin"
        }

        create("sonarQubeCommon") {
            id = "freesky1102.sonarqube.common"
            implementationClass = "SonarQubeBuildPlugin"
        }
    }
}

tasks.withType<KotlinCompile> {
    compilerOptions {
        jvmTarget.set(JvmTarget.fromTarget(libs.versions.java.get()))
    }
}
