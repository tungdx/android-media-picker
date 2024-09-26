import com.android.build.api.dsl.CommonExtension
import com.android.build.api.dsl.VariantDimension
import com.android.build.gradle.BaseExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.plugins.ExtensionAware
import org.gradle.kotlin.dsl.DependencyHandlerScope
import org.gradle.kotlin.dsl.fileTree
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinAndroidProjectExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmCompilerOptions
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.Properties
import java.util.TimeZone

lateinit var globalJavaVersion: String

lateinit var detektVersion: String

lateinit var versionCatalog: VersionCatalog

class VersionBuildPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        versionCatalog = target.findVersionCatalog("libs")
        versionCatalog.run {
            globalJavaVersion = requiredVersion("java")
            detektVersion = requiredVersion("detekt")
        }
    }

    companion object {

        fun getBuildTimeStamp(): String {
            val df = SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z", Locale.ENGLISH).apply {
                timeZone = TimeZone.getTimeZone("Asia/Ho_Chi_Minh")
            }

            return df.format(Date())
        }
    }
}


object AndroidBuildDeps {

    const val MIN_SDK = 26

    const val TARGET_SDK = 35

    const val COMPILE_SDK = 35

    const val BUILD_TOOLS = "35.0.0"

}

object SingleLibs {

    const val ADVANCED_RECYCLERVIEW =
        "com.h6ah4i.android.widget.advrecyclerview:advrecyclerview:1.0.0@aar"
}

fun BaseExtension.setupAndroidBasicConfigs() {

    compileSdkVersion(AndroidBuildDeps.COMPILE_SDK)
    buildToolsVersion(AndroidBuildDeps.BUILD_TOOLS)

    defaultConfig {
        minSdk = AndroidBuildDeps.MIN_SDK
        targetSdk = AndroidBuildDeps.TARGET_SDK
    }

    compileOptions {
        sourceCompatibility = JavaVersion.toVersion(globalJavaVersion)
        targetCompatibility = JavaVersion.toVersion(globalJavaVersion)
    }

    if (this is CommonExtension<*, *, *, *, *, *>) {
        buildFeatures {
            viewBinding = true
        }

        lint {
            abortOnError = false
            checkGeneratedSources = true
            checkDependencies = true
            ignoreTestSources = true
        }
    }

    customKotlinOptions {
        jvmTarget.value(JvmTarget.fromTarget(globalJavaVersion))
    }
}

fun BaseExtension.setupConsumeProguardFiles(project: Project) {

    buildTypes {

        val pgFiles =
            project.fileTree("dir" to "proguards", "include" to "*.pro").files.toTypedArray()

        getByName("release") {
            isMinifyEnabled = false
            consumerProguardFiles(*pgFiles)
            proguardFiles(*pgFiles)
        }
    }
}

fun BaseExtension.customKotlinOptions(configure: KotlinJvmCompilerOptions.() -> Unit) {
    val kotlinOptions = (this as ExtensionAware).extensions.findByName("compilerOptions")
    if (kotlinOptions !is KotlinJvmCompilerOptions) return
    configure.invoke(kotlinOptions)
}

fun DependencyHandlerScope.applyDetektPlugins() {
    "detektPlugins".invoke(versionCatalog.takeLib("detekt.formatting"))
    "detektPlugins".invoke(versionCatalog.takeLib("compose.rules.detekt"))
}

fun KotlinAndroidProjectExtension.autoConfig() {
    jvmToolchain(globalJavaVersion.toInt())
}

fun Project.findVersionCatalog(name: String): VersionCatalog {
    return extensions.findByType(VersionCatalogsExtension::class.java)!!.find(name).get()
}

fun VersionCatalog.requiredVersion(name: String): String {
    return findVersion(name).get().requiredVersion
}

fun VersionCatalog.takeLib(name: String): String {
    return try {
        findLibrary(name).get().get().toString()
    } catch (e: NoSuchElementException) {
        throw IllegalArgumentException("Library $name not found in version catalog")
    }
}

fun Project.getPropertiesFile(path: String): Properties? {
    val localPropertiesFile: File = file(path)
    return if (localPropertiesFile.exists()) {
        val stream = localPropertiesFile.inputStream()
        val temp = Properties().apply { load(stream) }
        stream.close()
        temp
    } else null
}

fun VariantDimension.addStringConstant(key: String, value: String) {
    val finalValue = if (value.startsWith("\"")) {
        value.substringAfter("\"").substringAfterLast("\"")
    } else value
    addManifestPlaceholders(mapOf(key to finalValue))
    resValue("string", key, "\"$finalValue\"")
}

fun VariantDimension.addStringConfigField(key: String, value: String?) {
    val tempValue = value ?: ""
    val finalValue = if (tempValue.startsWith("\"")) {
        tempValue.substringAfter("\"").substringAfterLast("\"")
    } else tempValue
    resValue("string", key, "\"$finalValue\"")
}

fun BaseExtension.enableCompose() {

    if (this is CommonExtension<*, *, *, *, *, *>) {
        buildFeatures {
            compose = true
        }
    }
}

fun DependencyHandlerScope.jetpackCompose() {
    val composeBom = platform(versionCatalog.takeLib("androidx.compose.bom"))
    "implementation"(composeBom)
    "androidTestImplementation"(composeBom)
    "implementation"("androidx.compose.material3:material3")
    "implementation"("androidx.compose.ui:ui-tooling-preview")
    "implementation"("androidx.compose.ui:ui-viewbinding")
    "debugImplementation"("androidx.compose.ui:ui-tooling")
    "androidTestImplementation"("androidx.compose.ui:ui-test-junit4")
    "debugImplementation"("androidx.compose.ui:ui-test-manifest")
    "implementation"("androidx.compose.runtime:runtime-livedata")
    listOf(
        "accompanist.swipeRefresh",
        "androidx.navigation.compose",
        "accompanist.appCompatTheme",
        "coil.kt.compose",
        "androidx.compose.constraintlayout",
        "androidx.compose.foundation",
        "androidx.activity.compose",
        "androidx.lifecycle.viewModelCompose",
        "androidx.lifecycle.runtimeCompose",
        "accompanist.systemConfig",
    ).forEach {
        "implementation"(versionCatalog.takeLib(it))
    }
}
