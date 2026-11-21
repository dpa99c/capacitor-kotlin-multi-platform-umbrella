plugins {
    // Library + `api(core)`. No iosFramework(): a feature is linked into the umbrella framework.
    id("io.github.capacitorkmp.plugin")
}

group = "io.github.capacitorkmp.umbrella"

capacitorKmp {
    android(namespace = "io.github.capacitorkmp.umbrella.device")
}
