pluginManagement {
    includeBuild("buildPlugin")
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
        maven { setUrl("https://jitpack.io") }
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
        maven { setUrl("https://jitpack.io") }
    }
    versionCatalogs {
        create("libs") {
            from(files("./buildPlugin/libs.versions.toml"))
        }
    }
}

include("app", "mediapicker")
