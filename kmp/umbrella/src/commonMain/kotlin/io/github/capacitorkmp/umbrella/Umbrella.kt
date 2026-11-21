package io.github.capacitorkmp.umbrella

/**
 * The umbrella module has no logic of its own: it links `core` and every feature module into one iOS
 * framework (`KmpUmbrella`) and one JS module (`kmp-umbrella`). Kotlin requires at least one source
 * file so that every target produces (and publishes) a klib.
 */
internal object Umbrella
