plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.freesky1102.deps)
    alias(libs.plugins.freesky1102.sonar)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.kapt)
    alias(libs.plugins.kotlin.parcelize)
    alias(libs.plugins.ksp)
}

android {
    
    setupAndroidBasicConfigs()
    namespace = "vn.tungdx.mediapickersample"
    defaultConfig {
        versionCode = 1
        versionName = "1.0"
    }
    
    buildTypes {

        getByName("debug") {
            isMinifyEnabled = false
        }

        getByName("release") {
            isMinifyEnabled = true
            isShrinkResources = false
        }
    }
}

dependencies {
    implementation(fileTree("dir" to "libs", "include" to listOf("*.jar")))
    implementation(project(":mediapicker"))
}
