package io.github.capacitorkmp.umbrella.motion

import io.github.capacitorkmp.core.KmpBridge
import io.github.capacitorkmp.core.KmpErrorCode
import io.github.capacitorkmp.core.KmpException
import io.github.capacitorkmp.umbrella.device.DeviceInfoSource
import io.github.capacitorkmp.umbrella.device.RawDeviceInfo
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class MotionModuleTest {
    private fun device(virtual: Boolean) = DeviceInfoSource { RawDeviceInfo(platform = "android", osName = "Android", osVersion = "16", isVirtual = virtual) }
    private fun accelerometer(available: Boolean) = object : AccelerometerSource {
        override val isAvailable = available
        override suspend fun requestPermission() = available
        override fun samples(intervalMs: Int): Flow<RawAcceleration> = emptyFlow()
    }

    private fun String.json() = Json.parseToJsonElement(this).jsonObject

    @Test
    fun recommendsSourceUsingTheDeviceFeature() = runTest {
        val real = KmpBridge(MotionModule(accelerometer(true), device(virtual = false)), StandardTestDispatcher(testScheduler))
        assertEquals("device", real.execute("isMotionAvailable", null).json()["recommendedSource"]!!.jsonPrimitive.content)
        val emulator = KmpBridge(MotionModule(accelerometer(true), device(virtual = true)), StandardTestDispatcher(testScheduler))
        val availability = emulator.execute("isMotionAvailable", null).json()
        assertEquals("simulated", availability["recommendedSource"]!!.jsonPrimitive.content)
        assertTrue(availability["isVirtualDevice"]!!.jsonPrimitive.boolean)
        real.close(); emulator.close()
    }

    @Test
    fun deviceSourceUnavailableIsReported() = runTest {
        val bridge = KmpBridge(MotionModule(accelerometer(false), device(false)), StandardTestDispatcher(testScheduler))
        val error = assertFailsWith<KmpException> { bridge.execute("startMotionUpdates", """{"source":"device"}""") }
        assertEquals(KmpErrorCode.UNAVAILABLE, error.code)
        bridge.close()
    }

    @Test
    fun simulatedMotionStreamsEvents() = runTest {
        val bridge = KmpBridge(MotionModule(accelerometer(false), device(true)), StandardTestDispatcher(testScheduler))
        val events = mutableListOf<String>()
        bridge.setEventListener { event, _ -> events += event }

        bridge.execute("startMotionUpdates", """{"source":"simulated","intervalMs":20}""")
        advanceTimeBy(12_000)
        runCurrent()
        val summary = bridge.execute("stopMotionUpdates", null).json()

        assertTrue(events.count { it == "motion" } in 550..610, "motion=${events.count { it == "motion" }}")
        assertEquals(1, events.count { it == "shake" })
        assertEquals(events.count { it == "step" }, summary["steps"]!!.jsonPrimitive.int)
        val before = events.size
        advanceTimeBy(1_000)
        assertEquals(before, events.size)
        bridge.close()
    }
}
