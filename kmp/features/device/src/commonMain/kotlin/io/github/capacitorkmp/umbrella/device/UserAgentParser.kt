package io.github.capacitorkmp.umbrella.device

/** Operating system and browser details extracted from a browser user agent string. */
data class UserAgentInfo(
    val osName: String,
    val osVersion: String?,
    val browserName: String?,
    val browserVersion: String?,
    val isMobile: Boolean,
    val deviceModel: String?,
)

/**
 * Small, dependency-free user agent parser. It lives in shared code (and is unit tested once) even
 * though only the Web implementation needs it today.
 */
object UserAgentParser {
    private val ANDROID = Regex("""Android (\d+(?:\.\d+)*)(?:; ([^;)]+))?""")
    private val IOS = Regex("""(iPhone|iPad|iPod)(?:.*?)OS (\d+(?:_\d+)*)""")
    private val MAC = Regex("""Mac OS X (\d+(?:[_.]\d+)*)""")
    private val WINDOWS = Regex("""Windows NT (\d+\.\d+)""")
    private val CROS = Regex("""CrOS \S+ (\d+(?:\.\d+)*)""")
    private val SAFARI_VERSION = Regex("""Version/(\d+(?:\.\d+)*)""")

    private val BROWSERS = listOf(
        "Edge" to Regex("""Edg(?:e|A|iOS)?/(\d+(?:\.\d+)*)"""),
        "Opera" to Regex("""OPR/(\d+(?:\.\d+)*)"""),
        "Samsung Internet" to Regex("""SamsungBrowser/(\d+(?:\.\d+)*)"""),
        "Firefox" to Regex("""(?:Firefox|FxiOS)/(\d+(?:\.\d+)*)"""),
        "Chrome" to Regex("""(?:Chrome|CriOS)/(\d+(?:\.\d+)*)"""),
        "Safari" to Regex("""Version/(\d+(?:\.\d+)*).*Safari/"""),
    )

    /**
     * @param maxTouchPoints `navigator.maxTouchPoints`; iPadOS reports a desktop (Mac) user agent, and
     * touch support is the usual way to tell them apart.
     */
    fun parse(userAgent: String, maxTouchPoints: Int = 0): UserAgentInfo {
        var model: String? = null
        val (os, version) = when {
            IOS.containsMatchIn(userAgent) -> IOS.find(userAgent)!!.destructured.let { (device, v) ->
                model = device
                (if (device == "iPad") "iPadOS" else "iOS") to v.replace('_', '.')
            }
            ANDROID.containsMatchIn(userAgent) -> ANDROID.find(userAgent)!!.groupValues.let { g ->
                model = g[2].trim().removeSuffix(" Build").substringBefore(" Build/").takeIf { it.isNotEmpty() && it != "K" }
                "Android" to g[1]
            }
            MAC.containsMatchIn(userAgent) && maxTouchPoints > 1 ->
                // iPadOS 13+ "Request desktop website" UA: the OS version is only in Version/x.
                "iPadOS".also { model = "iPad" } to SAFARI_VERSION.find(userAgent)?.groupValues?.get(1)
            MAC.containsMatchIn(userAgent) -> "macOS" to MAC.find(userAgent)!!.groupValues[1].replace('_', '.')
            WINDOWS.containsMatchIn(userAgent) -> "Windows" to windowsVersion(WINDOWS.find(userAgent)!!.groupValues[1])
            CROS.containsMatchIn(userAgent) -> "ChromeOS" to CROS.find(userAgent)!!.groupValues[1]
            "Linux" in userAgent -> "Linux" to null
            else -> "Unknown" to null
        }
        val browser = BROWSERS.firstNotNullOfOrNull { (name, regex) -> regex.find(userAgent)?.let { name to it.groupValues[1] } }
        val mobile = "Mobi" in userAgent || os in setOf("iOS", "Android") && "Tablet" !in userAgent
        return UserAgentInfo(os, version, browser?.first, browser?.second, mobile, model)
    }

    private fun windowsVersion(nt: String): String = when (nt) {
        "10.0" -> "10/11" // Windows 11 still reports NT 10.0
        "6.3" -> "8.1"
        "6.2" -> "8"
        "6.1" -> "7"
        else -> "NT $nt"
    }
}
