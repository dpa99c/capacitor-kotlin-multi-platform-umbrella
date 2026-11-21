package io.github.capacitorkmp.umbrella.device

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Raw, platform-specific device data as read by a [DeviceInfoSource] `actual` implementation.
 * Screen sizes are in logical pixels (Android dp, iOS points, CSS pixels).
 */
data class RawDeviceInfo(
    val platform: String,
    val manufacturer: String? = null,
    val model: String? = null,
    val osName: String? = null,
    val osVersion: String? = null,
    val locale: String? = null,
    val screenWidth: Double = 0.0,
    val screenHeight: Double = 0.0,
    val screenScale: Double = 1.0,
    val isVirtual: Boolean = false,
    val userAgent: String? = null,
    val maxTouchPoints: Int = 0,
)

/** Reads [RawDeviceInfo] with platform APIs. See `platformDeviceInfoSource()` in Platform.kt for the `actual`s. */
fun interface DeviceInfoSource {
    suspend fun read(): RawDeviceInfo
}

@Serializable
enum class FormFactor {
    @SerialName("phone") PHONE,
    @SerialName("tablet") TABLET,
    @SerialName("desktop") DESKTOP,
}

@Serializable
data class ScreenInfo(
    /** Logical size (dp / points / CSS px). */
    val width: Int,
    val height: Int,
    /** Physical pixels per logical pixel. */
    val scale: Double,
    val physicalWidth: Int,
    val physicalHeight: Int,
)

/** Normalised device information returned to JavaScript by `getDeviceInfo()`. */
@Serializable
data class DeviceInfo(
    val platform: String,
    val manufacturer: String,
    val model: String,
    val operatingSystem: String,
    val osVersion: String,
    val locale: String,
    val language: String,
    val region: String? = null,
    val formFactor: FormFactor,
    val screen: ScreenInfo,
    val isVirtual: Boolean,
    val browser: String? = null,
    val minimumOsVersion: String? = null,
    val isOsSupported: Boolean,
    /** One-line human readable description, e.g. `Google Pixel 9 Pro · Android 16 · en-GB`. */
    val summary: String,
)
