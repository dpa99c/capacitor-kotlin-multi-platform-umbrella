package io.github.capacitorkmp.umbrella.device

import android.content.Context
import android.os.Build
import io.github.capacitorkmp.core.KmpAndroid
import java.util.Locale

// The Android application Context is provided by the base plugin (KmpCapacitorPlugin calls
// KmpAndroid.init(context) in load()).

actual fun platformDeviceInfoSource(): DeviceInfoSource = DeviceInfoSource {
    val metrics = KmpAndroid.applicationContext.resources.displayMetrics
    RawDeviceInfo(
        platform = "android",
        manufacturer = Build.MANUFACTURER,
        model = Build.MODEL,
        osName = "Android",
        osVersion = Build.VERSION.RELEASE,
        locale = Locale.getDefault().toLanguageTag(),
        screenWidth = metrics.widthPixels / metrics.density.toDouble(),
        screenHeight = metrics.heightPixels / metrics.density.toDouble(),
        screenScale = metrics.density.toDouble(),
        isVirtual = isEmulator(),
    )
}

private fun isEmulator(): Boolean =
    Build.FINGERPRINT.startsWith("generic") || Build.FINGERPRINT.contains("emulator") ||
        Build.HARDWARE.contains("ranchu") || Build.HARDWARE.contains("goldfish") ||
        Build.PRODUCT.contains("sdk") || Build.MODEL.contains("Emulator")
