plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.freesky1102.deps)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.kapt)
    alias(libs.plugins.ksp)
}
android {
    
    setupAndroidBasicConfigs()
    namespace = "freesky1102.xyz.mediapicker"

    defaultConfig {
        versionCode  = 1
        versionName  = "1.0"
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
        }
    }
}

dependencies {
    api(fileTree("dir" to "libs", "include" to listOf("*.jar")))
    api(libs.kotlin.stdlib)
    implementation(libs.glide.glide)
    ksp(libs.glide.compiler)
    implementation("com.vanniktech:android-image-cropper:4.6.0")
}

kotlin { autoConfig() }
