package io.github.capacitorkmp.umbrella.motion

import io.github.capacitorkmp.umbrella.motion.AccelerationSample
import io.github.capacitorkmp.umbrella.motion.AccelerationUnit
import io.github.capacitorkmp.umbrella.motion.AxisConvention
import io.github.capacitorkmp.umbrella.motion.MotionNormalizer
import io.github.capacitorkmp.umbrella.motion.MotionNormalizer.STANDARD_GRAVITY
import io.github.capacitorkmp.umbrella.motion.MotionProcessor
import io.github.capacitorkmp.umbrella.motion.RawAcceleration
import io.github.capacitorkmp.umbrella.motion.SimulatedAccelerometer
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.sin
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MotionNormalizerTest {
    @Test
    fun coreMotionGravityUnitsAreConvertedAndFlipped() {
        // iPhone lying flat, face up: CoreMotion reports z = -1 g.
        val sample = MotionNormalizer.normalize(RawAcceleration(0.0, 0.0, -1.0, 0.0, AccelerationUnit.STANDARD_GRAVITY, AxisConvention.CORE_MOTION))
        assertEquals(STANDARD_GRAVITY, sample.z, 1e-9)
    }

    @Test
    fun androidValuesPassThrough() {
        val sample = MotionNormalizer.normalize(RawAcceleration(0.1, 9.7, 0.3, 5.0, AccelerationUnit.METERS_PER_SECOND_SQUARED, AxisConvention.ANDROID_W3C))
        assertEquals(AccelerationSample(0.1, 9.7, 0.3, 5.0), sample)
    }
}

class MotionProcessorTest {
    private val intervalMs = 20.0
    private val random = Random(1)

    private fun MotionProcessor.feed(seconds: Double, startMs: Double = 0.0, signal: (t: Double) -> Triple<Double, Double, Double>) =
        (0 until (seconds * 1000 / intervalMs).toInt()).map { i ->
            val t = startMs + i * intervalMs
            val (x, y, z) = signal(t / 1000)
            process(AccelerationSample(x, y, z, t))
        }

    private fun noise() = (random.nextDouble() - 0.5) * 0.1

    @Test
    fun stillDeviceProducesNoStepsOrShakes() {
        val processor = MotionProcessor()
        val updates = processor.feed(10.0) { Triple(noise(), noise(), STANDARD_GRAVITY + noise()) }
        assertEquals(0, processor.stepCount)
        assertEquals(0, processor.shakeCount)
        // Flat on a table: no pitch, no roll.
        assertTrue(abs(updates.last().sample.pitch) < 2 && abs(updates.last().sample.roll) < 2)
    }

    @Test
    fun countsStepsAndCadenceWhileWalking() {
        val processor = MotionProcessor()
        val updates = processor.feed(10.0) { t -> Triple(noise(), STANDARD_GRAVITY + 3.0 * sin(2 * PI * 2.0 * t), noise()) }
        // 2 steps/s for 10 s.
        assertTrue(processor.stepCount in 19..21, "steps=${processor.stepCount}")
        val cadence = updates.mapNotNull { it.step?.cadence }.last()
        assertEquals(120.0, cadence, 3.0)
        assertEquals(0, processor.shakeCount)
        // Upright phone: pitch ≈ 0, roll ≈ 90°.
        assertEquals(90.0, updates.last().sample.roll, 5.0)
    }

    @Test
    fun detectsOneShakePerBurstAndSuppressesSteps() {
        val processor = MotionProcessor()
        processor.feed(2.0) { Triple(0.0, 0.0, STANDARD_GRAVITY) }
        val burst = processor.feed(0.6, startMs = 2000.0) { t -> Triple(25 * sin(2 * PI * 8 * t), 0.0, STANDARD_GRAVITY) }
        val after = processor.feed(2.0, startMs = 2600.0) { Triple(0.0, 0.0, STANDARD_GRAVITY) }
        assertEquals(1, processor.shakeCount, "shakes at ${(burst + after).mapNotNull { it.shake }}")
        assertTrue(burst.mapNotNull { it.shake }.single().intensity > 13.0)
        assertEquals(0, processor.stepCount)
    }

    @Test
    fun simulatedAccelerometerWalksAndShakes() {
        val simulator = SimulatedAccelerometer(stepsPerSecond = 1.8, shakeEverySeconds = 10.0)
        val processor = MotionProcessor()
        val random = Random(7)
        (0 until 1000).forEach { i -> processor.process(MotionNormalizer.normalize(simulator.sampleAt(i * 0.02, random))) }
        // 20 s of walking at 1.8 steps/s (minus steps suppressed during the two shakes) and 2 shakes.
        assertEquals(2, processor.shakeCount)
        assertTrue(processor.stepCount in 30..37, "steps=${processor.stepCount}")
    }
}
