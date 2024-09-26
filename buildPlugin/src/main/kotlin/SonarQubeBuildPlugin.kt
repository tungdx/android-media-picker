import com.android.build.gradle.BaseExtension
import com.android.builder.model.SourceProvider
import io.gitlab.arturbosch.detekt.extensions.DetektExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.get
import org.gradle.testing.jacoco.plugins.JacocoPluginExtension
import org.gradle.testing.jacoco.tasks.JacocoReport
import org.sonarqube.gradle.SonarExtension
import java.io.File

class SonarQubeBuildPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        project.plugins.apply("org.sonarqube")
        project.plugins.apply("jacoco")
        project.plugins.apply("io.gitlab.arturbosch.detekt")

        project.dependencies {
            applyDetektPlugins()
        }
    }
}

fun Project.setupSonarQube(clojure: MySonarQubeExt.() -> Unit) {
    val helper = SonarQubeHelper(
        this, MySonarQubeExt().apply(clojure)
    )
    helper.setup()
}

private fun String.capFirstChar(): String {
    val stringBuilder = StringBuilder(this)
    if (this.isEmpty()) return this
    val firstChar = get(0)
    val upperCaseChar = firstChar.uppercaseChar()
    stringBuilder.setCharAt(0, upperCaseChar)
    return stringBuilder.toString()
}

private class SonarQubeHelper(
    private val project: Project,
    private val config: MySonarQubeExt
) {

    private val productFlavorFirstCap = config.productFlavor.capFirstChar()

    private val buildTypeFirstCap = config.buildType.capFirstChar()

    fun getVariant(): String {
        if (productFlavorFirstCap.isEmpty()) return config.buildType
        return "${config.productFlavor}$buildTypeFirstCap"
    }

    fun setup() {
        val sonarQubeExt = project.extensions.getByType(SonarExtension::class.java)
        setupSonarQube().invoke(sonarQubeExt)
        val jacocoExt = project.extensions.getByType(JacocoPluginExtension::class.java)
        setupJacoco().invoke(jacocoExt)
        val androidExt = project.extensions.getByType(BaseExtension::class.java)
        setupAppAndroid().invoke(androidExt)
        val detektExt = project.extensions.getByType(DetektExtension::class.java)
        setupDetekt(project).invoke(detektExt)
    }

    private fun setupAppAndroid(): BaseExtension.() -> Unit = {
        testOptions {
            unitTests {
                isIncludeAndroidResources = true
                isReturnDefaultValues = true
            }
        }
    }

    private fun setupJacoco(): JacocoPluginExtension.() -> Unit = {
        val fileFilter = listOf(
            "**/*Parcel.class",
            "**/*\$CREATOR.class",
            "**/*Test*.*",
            "**/AutoValue_*.*",
            "**/*JavascriptBridge.class",
            "**/R.class",
            "**/R$*.class",
            "**/Manifest*.*",
            "android/**/*.*",
            "**/BuildConfig.*",
            "**/*\$ViewBinder*.*",
            "**/*\$ViewInjector*.*",
            "**/Lambda$*.class",
            "**/Lambda.class",
            "**/*Lambda.class",
            "**/*Lambda*.class",
            "**/*\$InjectAdapter.class",
            "**/*\$ModuleAdapter.class",
            "**/*\$ViewInjector*.class",
            "**/*_MembersInjector.class", //Dagger2 generated code
            "*/*_MembersInjector*.*", //Dagger2 generated code
            "**/*_*Factory*.*", //Dagger2 generated code
            "**/*Component*.*", //Dagger2 generated code
            "**/*Module*.*" //Dagger2 generated code
        )
        val buildDir = project.layout.buildDirectory

        val debugTree = project.fileTree(
            mapOf(
                "dir" to "${buildDir}/intermediates/javac/${getVariant()}/classes",
                "excludes" to fileFilter
            )
        )

        val allJavaSource = project.getAllJavaSourceSets()
        val kotlinDebugTree = project.fileTree(
            mapOf(
                "dir" to "${buildDir}/tmp/kotlin-classes/${getVariant()}",
                "excludes" to fileFilter
            )
        )

        toolVersion = versionCatalog.requiredVersion("jacoco")
        project.tasks.register("jacocoTestReport", JacocoReport::class.java) {
            dependsOn("test$productFlavorFirstCap${buildTypeFirstCap}UnitTest")
            group = "Reporting"
            description = "Generating Jacoco coverage reports"

            reports {
                xml.required.set(true)
                html.required.set(true)
                csv.required.set(true)
                xml.outputLocation.set(project.file("${buildDir}/reports/jacocoTestReport.xml"))
                html.outputLocation.set(project.file("${buildDir}/reports/jacoco"))
                csv.outputLocation.set(project.file("${buildDir}/reports/jacocoTestReport.csv"))
            }

            sourceDirectories.setFrom(project.files(*allJavaSource.toTypedArray()))
            classDirectories.setFrom(project.files(debugTree, kotlinDebugTree))
            executionData.setFrom(
                project.fileTree(
                    mapOf(
                        "dir" to buildDir,
                        "includes" to listOf("**/*.exec", " **/*.ec")
                    )
                )
            )
        }
    }

    private fun setupSonarQube(): SonarExtension.() -> Unit = {
        val dependTasks = mutableListOf(
            "lint$productFlavorFirstCap$buildTypeFirstCap"
        )
        for (module in config.modules) {
            dependTasks.add(":$module:lint$buildTypeFirstCap")
        }

        project.tasks["sonar"].dependsOn(*dependTasks.toTypedArray())

        setAndroidVariant(getVariant())

        properties {
            val allModulePrj = config.modules.map {
                project.project(":$it")
            }

            val prjBuildDir = project.layout.buildDirectory.toString()

            val mainAndroidExt = project.getAndroidExt()
            val compileSDKVersion = mainAndroidExt.compileSdkVersion
            val androidSDK =
                mainAndroidExt.sdkDirectory.path + "/platforms/$compileSDKVersion/android.jar"

            val allJavaSource = allModulePrj.map { it.getAllJavaSourceSets() }
            val allResSource = allModulePrj.map { it.geAllResSourceSets() }
            val allManifestFiles = allModulePrj.map { it.geAllManifestFiles() }

            val allSources = mutableListOf<String>().apply {
                // other modules
                allJavaSource.forEach { addAll(it) }
                allResSource.forEach { addAll(it) }
                allManifestFiles.forEach { addAll(it) }
                allModulePrj.forEach {
                    val buildPath = it.layout.buildDirectory.toString()
                    add(buildPath + "/generated/source/kapt/${config.buildType}")
                    add(buildPath + "/generated/ksp/${config.buildType}")
                }
                // main project
                addAll(project.getAllJavaSourceSets())
                addAll(project.geAllResSourceSets())
                addAll(project.geAllManifestFiles())
                add(prjBuildDir + "/generated/source/kapt/${getVariant()}")
                add(prjBuildDir + "/generated/ksp/${getVariant()}")

                distinct()
            }.removeWrapperString()

            val allLintFiles = allModulePrj.map {
                val buildPath = it.layout.buildDirectory.toString()
                "$buildPath/reports/lint-results-${config.buildType}.xml"
            } + let {
                "$prjBuildDir/reports/lint-results-${getVariant()}.xml"
            }

            val allTestJavaSource = mainAndroidExt.getSourceSets("test") { getJavaKotlinSrc() }

            val hasTestSource = allTestJavaSource.isNotEmpty()
            if (hasTestSource) {
                property("sonar.tests", allTestJavaSource.joinToString(","))
                property(
                    "sonar.coverage.jacoco.xmlReportPaths",
                    "$prjBuildDir/reports/jacocoTestReport.xml"
                )
            }

            property("sonar.libraries", androidSDK)

            property("sonar.verbose", true)

            property("sonar.issuesReport.html.enable", "true")
            property("sonar.issuesReport.console.enable", "true")

            property("sonar.kotlin.detekt.reportPaths", "$prjBuildDir/reports/detekt/detekt.xml")

            val finalSonarSources = allSources.filterExisted()
            property("sonar.sources", finalSonarSources)
            property("sonar.exclusions", "**/*.png,**/*.jpg,**/*.gif,**/*.mp3,**/*.wav")

            property("sonar.java.coveragePlugin", "jacoco")
            property(
                "sonar.java.binaries",
                "$prjBuildDir/intermediates/javac/${getVariant()}/classes," + "$prjBuildDir/tmp/kotlin-classes/${getVariant()}"
            )
            property(
                "sonar.projectBaseDir", project.rootProject.projectDir.toString()
            )

            property(
                "sonar.androidLint.reportPaths",
                allLintFiles.joinToString(",")
            )

            property("sonar.java.source", globalJavaVersion)
            property("sonar.java.target", globalJavaVersion)
            val jdkHome = System.getenv("JAVA_HOME")
            property("sonar.java.jdkHome", jdkHome)
            property("sonar.sourceEncoding", "UTF-8")
            property("sonar.import_unknown_files", true)
        }
    }

    private fun BaseExtension.getSourceSets(
        vararg types: String,
        clojure: SourceProvider.() -> List<String>
    ): List<String> {
        val allSourceSets = mutableListOf<String>()

        for (type in types) {
            if (type.isEmpty()) continue

            val sourceSet = try {
                val sourceSet = sourceSets.getByName(type)
                if (sourceSet is SourceProvider) {
                    clojure.invoke(sourceSet)
                } else null
            } catch (e: Exception) {
                null
            } ?: continue

            allSourceSets.addAll(sourceSet)
        }

        return allSourceSets.distinct().filter { it.isNotEmpty() }
    }

    private fun Project.getAllJavaSourceSets(): List<String> {
        return getAndroidExt().getSourceSets(*getAllSourceSetText()) { getJavaKotlinSrc() }
    }

    private fun Project.geAllResSourceSets(): List<String> {
        return getAndroidExt().getSourceSets(*getAllSourceSetText()) { resDirectories.getAllDirs() }
    }

    private fun Project.geAllManifestFiles(): List<String> {
        return getAndroidExt().getSourceSets(*getAllSourceSetText()) {
            val manifestFile = manifestFile
            if (manifestFile.exists()) listOf(manifestFile.parent) else listOf("")
        }
    }

    private fun SourceProvider.getJavaKotlinSrc(): List<String> {
        val javaSrcs = javaDirectories.getAllDirs().toMutableList()
        val kotlinSrcs = kotlinDirectories.getAllDirs()
        javaSrcs += kotlinSrcs
        javaSrcs.distinct()
        return javaSrcs
    }

    fun Collection<File>.getAllDirs(): List<String> {
        val results = mutableListOf<String>()
        this.forEach {
            if (it.exists()) results.add(it.absolutePath)
        }
        return results
    }

    private fun getAllSourceSetText() = arrayOf(
        "main",
        config.buildType,
        config.productFlavor,
        getVariant()
    )

    private fun List<String>.removeWrapperString(): List<String> {
        val childItems = filter { it.isAWrapperStringOfAny(this) }
        return toMutableList().apply { removeAll(childItems) }
    }

    private fun List<String>.filterExisted(): List<String> {
        return filter { File(it).run { isDirectory && exists() } }
    }

    private fun String.isAWrapperStringOfAny(list: List<String>): Boolean {
        for (item in list) {
            if (item != this && this.contains(item, true)) return true
        }

        return false
    }

    private fun Project.getAndroidExt(): BaseExtension {
        return extensions.getByType(BaseExtension::class.java)
    }

    private fun setupDetekt(mainProject: Project): DetektExtension.() -> Unit = {
        toolVersion = detektVersion
        ignoredBuildTypes = listOf("release")
        source.from(mainProject.files(mainProject.rootProject.projectDir))
        val rootProject = mainProject.rootProject
        val configFile = rootProject.file("buildPlugin/config/detekt-config.yml")
        config.from(rootProject.files(configFile))
        autoCorrect = false
        debug = false
        buildUponDefaultConfig = false
        allRules = false
    }
}