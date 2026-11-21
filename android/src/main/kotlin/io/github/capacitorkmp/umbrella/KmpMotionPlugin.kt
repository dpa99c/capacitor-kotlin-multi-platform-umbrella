package io.github.capacitorkmp.umbrella

import com.getcapacitor.PluginCall
import com.getcapacitor.PluginMethod
import com.getcapacitor.annotation.CapacitorPlugin
import io.github.capacitorkmp.core.KmpCapacitorPlugin
import io.github.capacitorkmp.core.KmpModule
import io.github.capacitorkmp.umbrella.motion.MotionModule

/** `KmpMotion` – backed by the shared [MotionModule] (kmp/features/motion). */
@CapacitorPlugin(name = "KmpMotion")
class KmpMotionPlugin : KmpCapacitorPlugin() {

    override fun createModule(): KmpModule = MotionModule()

    @PluginMethod
    fun isMotionAvailable(call: PluginCall) = forward(call)

    @PluginMethod
    fun requestMotionPermission(call: PluginCall) = forward(call)

    @PluginMethod
    fun startMotionUpdates(call: PluginCall) = forward(call)

    @PluginMethod
    fun stopMotionUpdates(call: PluginCall) = forward(call)
}
