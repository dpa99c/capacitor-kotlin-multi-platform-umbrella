package io.github.capacitorkmp.umbrella

import com.getcapacitor.PluginCall
import com.getcapacitor.PluginMethod
import com.getcapacitor.annotation.CapacitorPlugin
import io.github.capacitorkmp.core.KmpCapacitorPlugin
import io.github.capacitorkmp.core.KmpModule
import io.github.capacitorkmp.umbrella.device.DeviceModule

/**
 * `KmpDevice` – backed by the shared [DeviceModule] (kmp/features/device).
 *
 * One Capacitor plugin class per source file: the Capacitor CLI registers only the first
 * `@CapacitorPlugin` class it finds in each file.
 */
@CapacitorPlugin(name = "KmpDevice")
class KmpDevicePlugin : KmpCapacitorPlugin() {

    override fun createModule(): KmpModule = DeviceModule()

    @PluginMethod
    fun getDeviceInfo(call: PluginCall) = forward(call)
}
