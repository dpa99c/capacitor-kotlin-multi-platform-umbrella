// KMP build of capacitor-kotlin-multi-platform-umbrella.
//
// Umbrella approach: every feature is a Gradle module of THIS build, and the `:umbrella` module links
// them (plus the base `core`) into ONE iOS framework / ONE JS module – i.e. one Kotlin runtime per app.
//
// The base plugin (capacitor-kotlin-multi-platform) is only a build-time dependency (devDependency):
//   - kmp/build-logic               → the `io.github.capacitorkmp.*` convention plugins
//   - kmp/gradle/libs.versions.toml → shared Kotlin / kotlinx / AGP versions
//   - kmp-repo                      → the prebuilt `core` library (all targets)

pluginManagement {
    includeBuild("../node_modules/capacitor-kotlin-multi-platform/kmp/build-logic")
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        maven {
            name = "capacitorKotlinMultiPlatform"
            url = uri("../node_modules/capacitor-kotlin-multi-platform/kmp-repo")
            content { includeGroup("io.github.capacitorkmp") }
        }
        google()
        mavenCentral()
    }
    versionCatalogs {
        create("libs") {
            from(files("../node_modules/capacitor-kotlin-multi-platform/kmp/gradle/libs.versions.toml"))
        }
    }
}

rootProject.name = "capacitor-kotlin-multi-platform-umbrella-kmp"

include(":features:device", ":features:motion", ":umbrella")
