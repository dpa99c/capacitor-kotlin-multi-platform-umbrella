package io.github.capacitorkmp.umbrella

import io.github.capacitorkmp.core.JsKmpBridge
import io.github.capacitorkmp.core.toJsBridge
import io.github.capacitorkmp.umbrella.device.DeviceModule
import io.github.capacitorkmp.umbrella.motion.MotionModule

// One factory per Capacitor plugin – all in the same Kotlin/JS module (one copy of the runtime).

/** Used by `KmpDevice`'s web implementation. */
@JsExport
fun createKmpDeviceBridge(): JsKmpBridge = DeviceModule().toJsBridge()

/** Used by `KmpMotion`'s web implementation. */
@JsExport
fun createKmpMotionBridge(): JsKmpBridge = MotionModule().toJsBridge()
