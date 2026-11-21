package io.github.capacitorkmp.umbrella.device

import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.toKString
import kotlinx.cinterop.useContents
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import platform.Foundation.NSLocale
import platform.Foundation.NSProcessInfo
import platform.Foundation.currentLocale
import platform.Foundation.localeIdentifier
import platform.UIKit.UIDevice
import platform.UIKit.UIScreen
import platform.posix.uname
import platform.posix.utsname

actual fun platformDeviceInfoSource(): DeviceInfoSource = DeviceInfoSource {
    // UIKit must be used on the main thread; KmpBridge runs handlers on a background dispatcher.
    withContext(Dispatchers.Main) {
        val device = UIDevice.currentDevice
        val (width, height) = UIScreen.mainScreen.bounds.useContents { size.width to size.height }
        val environment = NSProcessInfo.processInfo.environment
        val simulatorModel = environment["SIMULATOR_MODEL_IDENTIFIER"] as String?
        RawDeviceInfo(
            platform = "ios",
            manufacturer = "Apple",
            // e.g. "iPhone" + machine identifier "iPhone17,1" (the simulator reports the simulated model)
            model = "${device.model} (${simulatorModel ?: machineIdentifier()})",
            osName = device.systemName,
            osVersion = device.systemVersion,
            locale = NSLocale.currentLocale.localeIdentifier,
            screenWidth = width,
            screenHeight = height,
            screenScale = UIScreen.mainScreen.scale,
            isVirtual = simulatorModel != null,
        )
    }
}

private fun machineIdentifier(): String = memScoped {
    val info = alloc<utsname>()
    uname(info.ptr)
    info.machine.toKString()
}
