package io.github.capacitorkmp.umbrella.motion

/** Accelerometer from Android `SensorManager`, iOS `CMMotionManager`, or the W3C `devicemotion` event. */
expect fun platformAccelerometer(): AccelerometerSource
