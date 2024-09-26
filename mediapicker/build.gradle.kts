plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.freesky1102.deps)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ksp)
    id("com.vanniktech.maven.publish")
    id("com.gradleup.nmcp")
}

android {

    setupAndroidBasicConfigs()
    namespace = "vn.tungdx.mediapicker"

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
    api(libs.androidx.appCompat)
    api(libs.glide.glide)
    ksp(libs.glide.compiler)
    implementation("com.vanniktech:android-image-cropper:4.6.0")
}

kotlin { autoConfig() }
