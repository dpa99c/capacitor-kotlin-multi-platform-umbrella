package io.github.capacitorkmp.umbrella.motion

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlin.math.PI
import kotlin.math.sin
import kotlin.random.Random

/**
 * A pure-Kotlin [AccelerometerSource] that synthesises a person walking with a phone held upright
 * (≈1.8 steps/s), gently swaying, plus a vigorous shake every [shakeEverySeconds].
 *
 * Useful on simulators/desktop browsers without an accelerometer – and a nice demonstration that
 * shared code can provide complete implementations, not just logic.
 */
class SimulatedAccelerometer(
    private val stepsPerSecond: Double = 1.8,
    private val shakeEverySeconds: Double = 10.0,
    private val seed: Int = 42,
) : AccelerometerSource {
    override val isAvailable: Boolean = true

    override suspend fun requestPermission(): Boolean = true

    override fun samples(intervalMs: Int): Flow<RawAcceleration> = flow {
        val random = Random(seed)
        var index = 0L
        while (true) {
            val t = index * intervalMs / 1000.0
            emit(sampleAt(t, random))
            index++
            delay(intervalMs.toLong())
        }
    }

    internal fun sampleAt(t: Double, random: Random = Random(seed)): RawAcceleration {
        val g = MotionNormalizer.STANDARD_GRAVITY
        val noise = { (random.nextDouble() - 0.5) * 0.2 }
        val sway = 1.5 * sin(2 * PI * 0.1 * t)
        val bounce = 3.0 * sin(2 * PI * stepsPerSecond * t)
        // A 0.6 s burst of 8 Hz side-to-side shaking, half-way through every period.
        val shakeStart = shakeEverySeconds / 2
        val shake = if (t % shakeEverySeconds in shakeStart..(shakeStart + 0.6)) 25.0 * sin(2 * PI * 8 * t) else 0.0
        return RawAcceleration(
            x = sway + shake + noise(),
            y = g + bounce + noise(),
            z = 0.8 + noise(),
            timestampMs = t * 1000,
            unit = AccelerationUnit.METERS_PER_SECOND_SQUARED,
            convention = AxisConvention.ANDROID_W3C,
        )
    }
}
