package io.github.capacitorkmp.umbrella.motion

import io.github.capacitorkmp.core.KmpErrorCode
import io.github.capacitorkmp.core.KmpException
import kotlinx.cinterop.useContents
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.callbackFlow
import platform.CoreMotion.CMMotionManager
import platform.Foundation.NSOperationQueue

actual fun platformAccelerometer(): AccelerometerSource = IosAccelerometer()

private class IosAccelerometer : AccelerometerSource {
    private val manager = CMMotionManager()

    override val isAvailable: Boolean get() = manager.accelerometerAvailable

    // Raw accelerometer data needs no permission prompt (unlike CMPedometer / motion activity).
    override suspend fun requestPermission(): Boolean = isAvailable

    override fun samples(intervalMs: Int): Flow<RawAcceleration> = callbackFlow {
        if (!manager.accelerometerAvailable) {
            throw KmpException(KmpErrorCode.UNAVAILABLE, "This device has no accelerometer (the iOS Simulator has none)")
        }
        manager.accelerometerUpdateInterval = intervalMs / 1000.0
        manager.startAccelerometerUpdatesToQueue(NSOperationQueue()) { data, _ ->
            if (data != null) {
                data.acceleration.useContents {
                    trySend(
                        RawAcceleration(
                            x = x,
                            y = y,
                            z = z,
                            timestampMs = data.timestamp * 1000.0,
                            unit = AccelerationUnit.STANDARD_GRAVITY,
                            convention = AxisConvention.CORE_MOTION,
                        ),
                    )
                }
            }
        }
        awaitClose { manager.stopAccelerometerUpdates() }
    }.buffer(capacity = 64, onBufferOverflow = BufferOverflow.DROP_OLDEST)
}
