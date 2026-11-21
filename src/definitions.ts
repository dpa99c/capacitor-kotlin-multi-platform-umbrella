import type { PluginListenerHandle } from '@capacitor/core';

// Two Capacitor plugins from ONE package and ONE Kotlin framework (the umbrella approach):
//   KmpDevice – kmp/features/device
//   KmpMotion – kmp/features/motion

/**
 * Device information read with platform APIs (Android `Build`, iOS `UIDevice`, browser `navigator`)
 * and normalised by shared Kotlin code.
 */
export interface KmpDevicePlugin {
  /**
   * Returns normalised device information (OS version and user-agent parsing, locale normalisation,
   * form-factor classification, minimum-OS policy – all shared Kotlin).
   *
   * @since 0.1.0
   */
  getDeviceInfo(): Promise<DeviceInfo>;
}

/**
 * Accelerometer streaming (Android `SensorManager`, iOS `CMMotionManager`, W3C `devicemotion`) with
 * shared Kotlin step counting and shake detection.
 */
export interface KmpMotionPlugin {
  /**
   * Whether a hardware accelerometer is available, and which source to use. Uses the device feature
   * (in the same Kotlin runtime) to recommend the simulated source on emulators and simulators.
   *
   * @since 0.1.0
   */
  isMotionAvailable(): Promise<MotionAvailability>;

  /**
   * Requests access to motion data where required (iOS Safari). Call from a user gesture on the web.
   *
   * @since 0.1.0
   */
  requestMotionPermission(): Promise<MotionPermission>;

  /**
   * Starts streaming accelerometer data through the shared Kotlin `MotionProcessor`, which emits
   * `motion`, `step` and `shake` events. Restarts the stream if already running.
   *
   * Rejects with code `UNAVAILABLE` when `source` is `'device'` and there is no accelerometer.
   *
   * @since 0.1.0
   */
  startMotionUpdates(options?: StartMotionOptions): Promise<MotionSession>;

  /**
   * Stops the motion stream and returns a summary of the session.
   *
   * @since 0.1.0
   */
  stopMotionUpdates(): Promise<MotionSummary>;

  /**
   * Processed accelerometer samples, at most every `intervalMs`.
   *
   * @since 0.1.0
   */
  addListener(eventName: 'motion', listenerFunc: (sample: MotionSample) => void): Promise<PluginListenerHandle>;

  /**
   * A step detected by the shared step counter.
   *
   * @since 0.1.0
   */
  addListener(eventName: 'step', listenerFunc: (event: StepEvent) => void): Promise<PluginListenerHandle>;

  /**
   * A shake gesture detected by the shared shake detector.
   *
   * @since 0.1.0
   */
  addListener(eventName: 'shake', listenerFunc: (event: ShakeEvent) => void): Promise<PluginListenerHandle>;

  /**
   * The motion stream failed after it was started (e.g. the sensor became unavailable).
   *
   * @since 0.1.0
   */
  addListener(eventName: 'motionError', listenerFunc: (error: MotionError) => void): Promise<PluginListenerHandle>;

  /**
   * Removes all listeners for this plugin.
   *
   * @since 0.1.0
   */
  removeAllListeners(): Promise<void>;
}

export type FormFactor = 'phone' | 'tablet' | 'desktop';

export interface ScreenInfo {
  /** Logical width (dp / points / CSS pixels). */
  width: number;
  /** Logical height (dp / points / CSS pixels). */
  height: number;
  /** Physical pixels per logical pixel. */
  scale: number;
  physicalWidth: number;
  physicalHeight: number;
}

export interface DeviceInfo {
  platform: 'android' | 'ios' | 'web';
  manufacturer: string;
  model: string;
  /** `Android`, `iOS`, `iPadOS`, `macOS`, `Windows`, … */
  operatingSystem: string;
  osVersion: string;
  /** BCP-47 locale tag, e.g. `en-GB`. */
  locale: string;
  language: string;
  region?: string;
  formFactor: FormFactor;
  screen: ScreenInfo;
  /** Emulator / simulator. */
  isVirtual: boolean;
  /** Browser name and major version (web only). */
  browser?: string;
  /** Minimum OS version required by the (fictional) product policy implemented in Kotlin. */
  minimumOsVersion?: string;
  isOsSupported: boolean;
  /** Human readable one-liner, e.g. `Google Pixel 9 Pro · Android 16.0 · en-GB`. */
  summary: string;
}

export interface MotionAvailability {
  /** A hardware accelerometer (or, on the web, the DeviceMotion API) is available. */
  device: boolean;
  /** The simulated source (pure Kotlin) is always available. */
  simulated: boolean;
  /** Running on an emulator / simulator – determined by the *device* feature (a direct Kotlin call). */
  isVirtualDevice: boolean;
  /** `'device'` on real hardware with an accelerometer, otherwise `'simulated'`. */
  recommendedSource: MotionSource;
}

export interface MotionPermission {
  granted: boolean;
}

export type MotionSource = 'device' | 'simulated';

export interface StartMotionOptions {
  /**
   * Requested sampling / event interval in milliseconds (16–1000).
   *
   * @default 50
   */
  intervalMs?: number;
  /**
   * `'device'` uses the platform accelerometer; `'simulated'` uses a pure-Kotlin signal generator
   * (walking + periodic shake), handy on simulators and desktop browsers.
   *
   * @default 'device'
   */
  source?: MotionSource;
  /**
   * Linear acceleration (m/s²) a peak must exceed to count towards a shake.
   *
   * @default 13
   */
  shakeThreshold?: number;
}

export interface MotionSession {
  source: MotionSource;
  intervalMs: number;
}

export interface MotionSummary {
  /** Whether a stream was running when stopped. */
  running: boolean;
  samples: number;
  steps: number;
  shakes: number;
}

export interface MotionSample {
  /** Acceleration including gravity in m/s² (Android/W3C axes on every platform). */
  x: number;
  y: number;
  z: number;
  /** Magnitude of the gravity-free acceleration in m/s². */
  linearMagnitude: number;
  /** Tilt derived from the gravity vector, in degrees. */
  pitch: number;
  roll: number;
  steps: number;
  shakes: number;
  /** Monotonic timestamp in milliseconds. */
  timestamp: number;
}

export interface StepEvent {
  count: number;
  /** Steps per minute over the last 10 s (null until two steps are detected). */
  cadence: number | null;
  timestamp: number;
}

export interface ShakeEvent {
  count: number;
  /** Peak linear acceleration of the shake in m/s². */
  intensity: number;
  timestamp: number;
}

export interface MotionError {
  code: string;
  message: string;
}
