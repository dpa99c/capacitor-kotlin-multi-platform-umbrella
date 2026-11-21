package io.github.capacitorkmp.umbrella.device

import io.github.capacitorkmp.core.KmpBridge
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals

class DeviceModuleTest {
    @Test
    fun deviceInfoIsNormalised() = runTest {
        val source = DeviceInfoSource {
            RawDeviceInfo(platform = "ios", manufacturer = "Apple", model = "iPhone", osName = "iOS", osVersion = "18.2", locale = "en_US", screenWidth = 393.0, screenHeight = 852.0, screenScale = 3.0)
        }
        val bridge = KmpBridge(DeviceModule(source), StandardTestDispatcher(testScheduler))
        val info = Json.parseToJsonElement(bridge.execute("getDeviceInfo", null)).jsonObject
        assertEquals("en-US", info["locale"]!!.jsonPrimitive.content)
        assertEquals("phone", info["formFactor"]!!.jsonPrimitive.content)
        assertEquals(1179, info["screen"]!!.jsonObject["physicalWidth"]!!.jsonPrimitive.int)
        bridge.close()
    }
}
