import { registerPlugin } from '@capacitor/core';

import type { KmpDevicePlugin, KmpMotionPlugin } from './definitions';
import type { KmpDeviceWeb, KmpMotionWeb } from './web';

// Web factories are memoised: Capacitor may call them several times when the first plugin calls happen
// concurrently, and every web instance would otherwise host its own Kotlin module (losing events).
let deviceWeb: Promise<KmpDeviceWeb> | undefined;
let motionWeb: Promise<KmpMotionWeb> | undefined;

const KmpDevice = registerPlugin<KmpDevicePlugin>('KmpDevice', {
  web: () => (deviceWeb ??= import('./web').then((m) => new m.KmpDeviceWeb())),
});

const KmpMotion = registerPlugin<KmpMotionPlugin>('KmpMotion', {
  web: () => (motionWeb ??= import('./web').then((m) => new m.KmpMotionWeb())),
});

export * from './definitions';
export { KmpDevice, KmpMotion };
