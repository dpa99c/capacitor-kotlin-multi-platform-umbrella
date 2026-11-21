package io.github.capacitorkmp.umbrella.motion

import io.github.capacitorkmp.umbrella.motion.MotionNormalizer.STANDARD_GRAVITY
import kotlinx.serialization.Serializable
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.roundToInt
import kotlin.math.sqrt

/** Tuning parameters of [MotionProcessor]. */
@Serializable
data class MotionConfig(
    /** Time constant of the low-pass filter separating gravity from linear acceleration. */
    val gravityTimeConstantMs: Double = 800.0,
    /** Linear acceleration (m/s²) a peak must exceed to count towards a shake. */
    val shakeThreshold: Double = 13.0,
    /** Number of peaks within [shakeWindowMs] that make a shake. */
    val shakeMinPeaks: Int = 3,
    val shakeWindowMs: Double = 1000.0,
    val shakeCooldownMs: Double = 1000.0,
    /** Smoothed `|a| - g` (m/s²) that must be exceeded for a step, and fallen below to re-arm. */
    val stepThreshold: Double = 1.2,
    val stepResetThreshold: Double = 0.4,
    val minStepIntervalMs: Double = 250.0,
    val stepSmoothingTimeConstantMs: Double = 50.0,
    val cadenceWindowMs: Double = 10_000.0,
)

/** Payload of the `motion` event. */
@Serializable
data class MotionSample(
    /** Acceleration including gravity, m/s², Android/W3C axes. */
    val x: Double,
    val y: Double,
    val z: Double,
    /** Magnitude of the linear (gravity-free) acceleration, m/s². */
    val linearMagnitude: Double,
    /** Device tilt derived from the gravity vector, in degrees. */
    val pitch: Double,
    val roll: Double,
    val steps: Int,
    val shakes: Int,
    val timestamp: Double,
)

/** Payload of the `shake` event. */
@Serializable
data class ShakeEvent(val count: Int, val intensity: Double, val timestamp: Double)

/** Payload of the `step` event. */
@Serializable
data class StepEvent(val count: Int, val cadence: Double?, val timestamp: Double)

/** Result of processing one sample. */
data class MotionUpdate(val sample: MotionSample, val shake: ShakeEvent? = null, val step: StepEvent? = null)

/**
 * Platform-independent motion analysis – the "shared logic" of the demo:
 *
 * - separates gravity from linear acceleration with an exponential low-pass filter,
 * - derives pitch/roll from gravity,
 * - detects shakes (several strong linear-acceleration peaks in a short window),
 * - counts steps with a hysteresis peak detector on the smoothed `|a| - g` signal and computes cadence.
 *
 * It is a plain stateful class with no platform dependencies, so it is unit tested once in
 * `commonTest` and behaves identically on Android, iOS and the Web.
 */
class MotionProcessor(private val config: MotionConfig = MotionConfig()) {
    private var gravity: Vec3? = null
    private var lastTimestamp: Double? = null
    private var smoothedDynamic = 0.0
    private var stepArmed = true
    private var lastStepAt = Double.NEGATIVE_INFINITY
    private var suppressStepsUntil = Double.NEGATIVE_INFINITY
    private val stepTimes = ArrayDeque<Double>()
    private val shakePeaks = ArrayDeque<Pair<Double, Double>>() // (timestamp, magnitude)
    private var lastShakeAt = Double.NEGATIVE_INFINITY

    var stepCount: Int = 0
        private set
    var shakeCount: Int = 0
        private set
    var sampleCount: Int = 0
        private set

    fun process(sample: AccelerationSample): MotionUpdate {
        val t = sample.timestampMs
        val a = Vec3(sample.x, sample.y, sample.z)
        val dt = lastTimestamp?.let { (t - it).coerceIn(1.0, 1000.0) } ?: 20.0
        lastTimestamp = t
        sampleCount++

        // 1. Gravity / linear acceleration split.
        val alpha = config.gravityTimeConstantMs / (config.gravityTimeConstantMs + dt)
        val g = gravity?.let { it * alpha + a * (1 - alpha) } ?: a
        gravity = g
        val linear = a - g
        val linearMagnitude = linear.magnitude

        // 2. Shake detection.
        var shake: ShakeEvent? = null
        var newPeak = false
        if (linearMagnitude > config.shakeThreshold) {
            suppressStepsUntil = t + 500.0
            if (shakePeaks.isEmpty() || t - shakePeaks.last().first >= 60.0) {
                shakePeaks.addLast(t to linearMagnitude)
                newPeak = true
            }
        }
        while (shakePeaks.isNotEmpty() && t - shakePeaks.first().first > config.shakeWindowMs) shakePeaks.removeFirst()
        // Only a fresh peak can complete a shake (stale peaks must not fire once the cooldown ends).
        if (newPeak && shakePeaks.size >= config.shakeMinPeaks && t - lastShakeAt >= config.shakeCooldownMs) {
            shakeCount++
            lastShakeAt = t
            shake = ShakeEvent(shakeCount, shakePeaks.maxOf { it.second }.round(2), t)
            shakePeaks.clear()
        }

        // 3. Step detection (orientation independent: uses |a| - g).
        val beta = dt / (config.stepSmoothingTimeConstantMs + dt)
        smoothedDynamic += (a.magnitude - STANDARD_GRAVITY - smoothedDynamic) * beta
        var step: StepEvent? = null
        if (stepArmed && smoothedDynamic > config.stepThreshold && t >= suppressStepsUntil &&
            t - lastStepAt >= config.minStepIntervalMs
        ) {
            stepArmed = false
            lastStepAt = t
            stepCount++
            stepTimes.addLast(t)
            while (stepTimes.isNotEmpty() && t - stepTimes.first() > config.cadenceWindowMs) stepTimes.removeFirst()
            step = StepEvent(stepCount, cadence(), t)
        } else if (smoothedDynamic < config.stepResetThreshold) {
            stepArmed = true
        }

        // 4. Orientation from gravity.
        val pitch = atan2(-g.x, sqrt(g.y * g.y + g.z * g.z)).toDegrees()
        val roll = atan2(g.y, g.z).toDegrees()

        return MotionUpdate(
            MotionSample(
                x = sample.x.round(3),
                y = sample.y.round(3),
                z = sample.z.round(3),
                linearMagnitude = linearMagnitude.round(3),
                pitch = pitch.round(1),
                roll = roll.round(1),
                steps = stepCount,
                shakes = shakeCount,
                timestamp = t,
            ),
            shake,
            step,
        )
    }

    /** Steps per minute over the cadence window, or null with fewer than two steps. */
    private fun cadence(): Double? {
        if (stepTimes.size < 2) return null
        val span = stepTimes.last() - stepTimes.first()
        return if (span <= 0) null else ((stepTimes.size - 1) * 60_000.0 / span).round(1)
    }

    private data class Vec3(val x: Double, val y: Double, val z: Double) {
        operator fun plus(o: Vec3) = Vec3(x + o.x, y + o.y, z + o.z)
        operator fun minus(o: Vec3) = Vec3(x - o.x, y - o.y, z - o.z)
        operator fun times(k: Double) = Vec3(x * k, y * k, z * k)
        val magnitude: Double get() = sqrt(x * x + y * y + z * z)
    }
}

private fun Double.toDegrees(): Double = this * 180.0 / PI
private fun Double.round(decimals: Int): Double {
    var factor = 1.0
    repeat(decimals) { factor *= 10 }
    return (this * factor).roundToInt() / factor
}

