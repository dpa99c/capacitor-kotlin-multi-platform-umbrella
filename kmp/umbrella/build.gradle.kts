plugins {
    id("io.github.capacitorkmp.plugin")
}

group = "io.github.capacitorkmp.umbrella"

kotlin {
    sourceSets.commonMain.dependencies {
        api(project(":features:device"))
        api(project(":features:motion"))
    }
}

capacitorKmp {
    android(namespace = "io.github.capacitorkmp.umbrella.all")
    // The ONLY framework of this plugin: core + every feature, one Kotlin/Native runtime.
    iosFramework("KmpUmbrella") {
        export(project(":features:device"))
        export(project(":features:motion"))
    }
    jsModule("kmp-umbrella")
}
