package io.github.capacitorkmp.umbrella.device

/** Device data from Android `Build`/`DisplayMetrics`, iOS `UIDevice`/`UIScreen`, or the browser `navigator`. */
expect fun platformDeviceInfoSource(): DeviceInfoSource
