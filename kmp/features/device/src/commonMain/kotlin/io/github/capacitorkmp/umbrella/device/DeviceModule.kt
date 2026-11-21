package io.github.capacitorkmp.umbrella.device

import io.github.capacitorkmp.core.KmpModule

/**
 * Shared implementation of the `KmpDevice` Capacitor plugin.
 * Method: `getDeviceInfo` – platform data normalised by [DeviceInfoService].
 */
class DeviceModule(source: DeviceInfoSource) : KmpModule() {

    /** Uses the platform implementation. (Also the constructor visible from Swift.) */
    constructor() : this(platformDeviceInfoSource())

    private val service = DeviceInfoService(source)

    init {
        method("getDeviceInfo") { service.getDeviceInfo() }
    }
}
