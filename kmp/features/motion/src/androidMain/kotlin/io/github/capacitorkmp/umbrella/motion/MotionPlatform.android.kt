package io.github.capacitorkmp.umbrella.motion

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import io.github.capacitorkmp.core.KmpAndroid
import io.github.capacitorkmp.core.KmpErrorCode
import io.github.capacitorkmp.core.KmpException
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.callbackFlow

actual fun platformAccelerometer(): AccelerometerSource = AndroidAccelerometer()

private class AndroidAccelerometer : AccelerometerSource {
    private val sensorManager: SensorManager
        get() = KmpAndroid.applicationContext.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    override val isAvailable: Boolean
        get() = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null

    // Accelerometer access needs no runtime permission on Android.
    override suspend fun requestPermission(): Boolean = true

    override fun samples(intervalMs: Int): Flow<RawAcceleration> = callbackFlow {
        val manager = sensorManager
        val sensor = manager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
            ?: throw KmpException(KmpErrorCode.UNAVAILABLE, "This device has no accelerometer")
        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                trySend(
                    RawAcceleration(
                        x = event.values[0].toDouble(),
                        y = event.values[1].toDouble(),
                        z = event.values[2].toDouble(),
                        timestampMs = event.timestamp / 1_000_000.0,
                        unit = AccelerationUnit.METERS_PER_SECOND_SQUARED,
                        convention = AxisConvention.ANDROID_W3C,
                    ),
                )
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
        }
        manager.registerListener(listener, sensor, intervalMs * 1000)
        awaitClose { manager.unregisterListener(listener) }
    }.buffer(capacity = 64, onBufferOverflow = BufferOverflow.DROP_OLDEST)
}
