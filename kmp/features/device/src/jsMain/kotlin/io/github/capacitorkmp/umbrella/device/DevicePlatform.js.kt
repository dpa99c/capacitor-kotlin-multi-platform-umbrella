package io.github.capacitorkmp.umbrella.device

private val hasWindow: Boolean get() = js("typeof window !== 'undefined'") as Boolean

actual fun platformDeviceInfoSource(): DeviceInfoSource = DeviceInfoSource {
    if (!hasWindow) return@DeviceInfoSource RawDeviceInfo(platform = "web", userAgent = "", locale = "en")
    val navigator: dynamic = js("window.navigator")
    val screen: dynamic = js("window.screen")
    RawDeviceInfo(
        platform = "web",
        // Browsers don't expose the manufacturer; the model is parsed from the user agent in shared code.
        locale = navigator.language as String?,
        screenWidth = (screen.width as Number).toDouble(),
        screenHeight = (screen.height as Number).toDouble(),
        screenScale = ((js("window.devicePixelRatio") as Number?) ?: 1).toDouble(),
        userAgent = navigator.userAgent as String,
        maxTouchPoints = ((navigator.maxTouchPoints as Number?) ?: 0).toInt(),
    )
}
