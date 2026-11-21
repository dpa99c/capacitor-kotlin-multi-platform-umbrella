plugins {
    // Library + `api(core)`. No iosFramework(): a feature is linked into the umbrella framework.
    id("io.github.capacitorkmp.plugin")
}

group = "io.github.capacitorkmp.umbrella"

capacitorKmp {
    android(namespace = "io.github.capacitorkmp.umbrella.motion")
}

kotlin {
    sourceSets.commonMain.dependencies {
        // Features in one umbrella can call each other directly (same Kotlin runtime).
        api(project(":features:device"))
    }
}
