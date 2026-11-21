import type {
  DeviceInfo,
  KmpDevicePlugin,
  KmpMotionPlugin,
  MotionAvailability,
  MotionPermission,
  MotionSession,
  MotionSummary,
  StartMotionOptions,
} from './definitions';
// Synced from capacitor-kotlin-multi-platform by `npm run kmp:build` (no runtime dependency on the base).
import { KmpWebPlugin } from './generated/kmp-web';

// Both plugins load the SAME Kotlin/JS module (one copy of the Kotlin runtime); the browser/bundler
// evaluates it once and each plugin gets its own bridge from its factory.
const umbrella = () => import('./generated/kmp/kmp-umbrella.mjs');

export class KmpDeviceWeb extends KmpWebPlugin implements KmpDevicePlugin {
  constructor() {
    super(() => umbrella().then((m) => m.createKmpDeviceBridge()));
  }

  getDeviceInfo(): Promise<DeviceInfo> {
    return this.invoke('getDeviceInfo');
  }
}

export class KmpMotionWeb extends KmpWebPlugin implements KmpMotionPlugin {
  constructor() {
    super(() => umbrella().then((m) => m.createKmpMotionBridge()));
  }

  isMotionAvailable(): Promise<MotionAvailability> {
    return this.invoke('isMotionAvailable');
  }

  requestMotionPermission(): Promise<MotionPermission> {
    return this.invoke('requestMotionPermission');
  }

  startMotionUpdates(options: StartMotionOptions = {}): Promise<MotionSession> {
    return this.invoke('startMotionUpdates', options);
  }

  stopMotionUpdates(): Promise<MotionSummary> {
    return this.invoke('stopMotionUpdates');
  }
}
