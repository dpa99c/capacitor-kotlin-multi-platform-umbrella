package io.github.capacitorkmp.umbrella.motion

import kotlinx.coroutines.flow.Flow
import kotlin.math.sqrt

/** Units in which a platform reports acceleration. */
enum class AccelerationUnit { METERS_PER_SECOND_SQUARED, STANDARD_GRAVITY }

/**
 * Axis/sign conventions of the platform APIs. All of them use x → right, y → up, z → out of the
 * screen, but CoreMotion reports the *reaction* to gravity with the opposite sign.
 */
enum class AxisConvention {
    /** Android `TYPE_ACCELEROMETER` and W3C `devicemotion` — flat, face up: z ≈ +9.81 m/s². */
    ANDROID_W3C,

    /** iOS CoreMotion `CMAccelerometerData` — flat, face up: z ≈ -1 g. */
    CORE_MOTION,
}

/** One accelerometer reading exactly as delivered by the platform (gravity included). */
data class RawAcceleration(
    val x: Double,
    val y: Double,
    val z: Double,
    /** Monotonic timestamp in milliseconds (only differences are meaningful). */
    val timestampMs: Double,
    val unit: AccelerationUnit,
    val convention: AxisConvention,
)

/** A normalised reading: m/s², Android/W3C axis convention. */
data class AccelerationSample(val x: Double, val y: Double, val z: Double, val timestampMs: Double) {
    val magnitude: Double get() = sqrt(x * x + y * y + z * z)
}

/**
 * Accelerometer access implemented per platform (`expect`/`actual`):
 * Android `SensorManager`, iOS `CMMotionManager`, Web `devicemotion` events.
 */
interface AccelerometerSource {
    /** Whether the hardware/API is present. */
    val isAvailable: Boolean

    /** Requests permission where the platform needs it (iOS Safari); returns whether access is granted. */
    suspend fun requestPermission(): Boolean

    /** Cold stream of raw readings, requested at roughly [intervalMs]. Collection stops the sensor. */
    fun samples(intervalMs: Int): Flow<RawAcceleration>
}

/** Converts any [RawAcceleration] into a common [AccelerationSample]. */
object MotionNormalizer {
    const val STANDARD_GRAVITY: Double = 9.80665

    fun normalize(raw: RawAcceleration): AccelerationSample {
        val scale = when (raw.unit) {
            AccelerationUnit.METERS_PER_SECOND_SQUARED -> 1.0
            AccelerationUnit.STANDARD_GRAVITY -> STANDARD_GRAVITY
        }
        val sign = when (raw.convention) {
            AxisConvention.ANDROID_W3C -> 1.0
            AxisConvention.CORE_MOTION -> -1.0
        }
        val k = scale * sign
        return AccelerationSample(raw.x * k, raw.y * k, raw.z * k, raw.timestampMs)
    }
}
