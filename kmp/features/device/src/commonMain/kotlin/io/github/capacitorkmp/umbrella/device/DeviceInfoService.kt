package io.github.capacitorkmp.umbrella.device

import kotlin.math.min
import kotlin.math.roundToInt

/**
 * Shared business logic on top of the raw platform data: normalisation, classification and policy
 * decisions that should behave identically on Android, iOS and the Web.
 */
class DeviceInfoService(private val source: DeviceInfoSource) {

    suspend fun getDeviceInfo(): DeviceInfo = normalize(source.read())

    companion object {
        /** Minimum OS versions supported by this (fictional) product, enforced in one place. */
        val MINIMUM_OS_VERSIONS: Map<String, OsVersion> = mapOf(
            "Android" to OsVersion(7, 0), // API 24, Capacitor 8's minSdk
            "iOS" to OsVersion(15, 0),
            "iPadOS" to OsVersion(15, 0),
        )

        /** Smallest logical screen width from which a touch device is considered a tablet (Android sw600dp). */
        const val TABLET_MIN_WIDTH: Double = 600.0

        fun normalize(raw: RawDeviceInfo): DeviceInfo {
            val ua = raw.userAgent?.let { UserAgentParser.parse(it, raw.maxTouchPoints) }

            val os = ua?.osName ?: raw.osName?.let(::canonicalOsName) ?: "Unknown"
            val osVersion = (ua?.osVersion ?: raw.osVersion)?.let { OsVersion.parse(it)?.toString() ?: it } ?: "unknown"
            val manufacturer = raw.manufacturer?.let(::canonicalManufacturer)
                ?: if (os in setOf("iOS", "iPadOS", "macOS")) "Apple" else "Unknown"
            val model = raw.model?.takeIf { it.isNotBlank() } ?: ua?.deviceModel ?: ua?.browserName ?: "Unknown"
            val locale = normalizeLocale(raw.locale)
            val minimum = MINIMUM_OS_VERSIONS[os]
            val parsedVersion = OsVersion.parse(osVersion)
            val supported = minimum == null || (parsedVersion != null && parsedVersion >= minimum)
            val formFactor = classify(raw, os, ua)
            val screen = ScreenInfo(
                width = raw.screenWidth.roundToInt(),
                height = raw.screenHeight.roundToInt(),
                scale = (raw.screenScale * 100).roundToInt() / 100.0,
                physicalWidth = (raw.screenWidth * raw.screenScale).roundToInt(),
                physicalHeight = (raw.screenHeight * raw.screenScale).roundToInt(),
            )
            val browser = ua?.browserName?.let { name -> ua.browserVersion?.let { "$name ${it.substringBefore('.')}" } ?: name }

            val summary = buildList {
                add(if (model.startsWith(manufacturer, ignoreCase = true)) model else "$manufacturer $model")
                add("$os $osVersion")
                browser?.let(::add)
                add(locale.tag)
                if (raw.isVirtual) add("virtual")
            }.joinToString(" · ")

            return DeviceInfo(
                platform = raw.platform,
                manufacturer = manufacturer,
                model = model,
                operatingSystem = os,
                osVersion = osVersion,
                locale = locale.tag,
                language = locale.language,
                region = locale.region,
                formFactor = formFactor,
                screen = screen,
                isVirtual = raw.isVirtual,
                browser = browser,
                minimumOsVersion = minimum?.toString(),
                isOsSupported = supported,
                summary = summary,
            )
        }

        private fun classify(raw: RawDeviceInfo, os: String, ua: UserAgentInfo?): FormFactor {
            val smallestWidth = min(raw.screenWidth, raw.screenHeight)
            return when {
                os == "iPadOS" || raw.model?.startsWith("iPad") == true -> FormFactor.TABLET
                raw.platform == "web" && raw.maxTouchPoints == 0 && ua?.isMobile != true -> FormFactor.DESKTOP
                smallestWidth >= TABLET_MIN_WIDTH -> FormFactor.TABLET
                else -> FormFactor.PHONE
            }
        }

        private fun canonicalOsName(name: String): String = when (name.lowercase()) {
            "android" -> "Android"
            "ios", "iphone os" -> "iOS"
            "ipados" -> "iPadOS"
            else -> name
        }

        private fun canonicalManufacturer(name: String): String = when (val lower = name.trim().lowercase()) {
            "" -> "Unknown"
            "google" -> "Google"
            "samsung" -> "Samsung"
            "apple" -> "Apple"
            else -> lower.replaceFirstChar { it.uppercase() }
        }

        data class Locale(val tag: String, val language: String, val region: String?)

        /** `en_GB`, `en-gb`, `en_GB@calendar=…` → `en-GB`. */
        fun normalizeLocale(raw: String?): Locale {
            val parts = raw.orEmpty().substringBefore('@').substringBefore('.').split('_', '-').filter { it.isNotBlank() }
            val language = parts.firstOrNull()?.lowercase() ?: "und"
            val region = parts.drop(1).firstOrNull { it.length == 2 || (it.length == 3 && it.all(Char::isDigit)) }?.uppercase()
            val script = parts.drop(1).firstOrNull { it.length == 4 }?.lowercase()?.replaceFirstChar { it.uppercase() }
            return Locale(listOfNotNull(language, script, region).joinToString("-"), language, region)
        }
    }
}
