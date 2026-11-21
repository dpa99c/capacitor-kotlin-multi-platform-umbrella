// Makes this umbrella plugin self-contained: apps install ONLY capacitor-kotlin-multi-platform-umbrella,
// never the base plugin. (If an app installed the base too, Capacitor would link the base's own
// CapacitorKmpCore.xcframework – a second Kotlin runtime – which is exactly what the umbrella avoids.)
//
// Copied from the base package (a devDependency):
//   kmp-repo/io/github/capacitorkmp/core*          → kmp-repo/…           (Android resolves `core` transitively)
//   ios/Sources/CapacitorKmpSupport/KmpCapacitor.swift → ios/Sources/KmpUmbrellaPlugin/Generated/
//   src/kmp-web.ts                                  → src/generated/kmp-web.ts
import { cpSync, existsSync, mkdirSync, readdirSync, rmSync } from 'node:fs';
import { join } from 'node:path';

const base = 'node_modules/capacitor-kotlin-multi-platform';
if (!existsSync(join(base, 'kmp-repo'))) {
  console.error(`[sync-from-base] ${base}/kmp-repo not found – build the base plugin first (npm run build there).`);
  process.exit(1);
}

// 1. Prebuilt `core` library (all targets) for Android dependency resolution in apps.
const group = 'io/github/capacitorkmp';
mkdirSync(join('kmp-repo', group), { recursive: true });
for (const artifact of readdirSync(join(base, 'kmp-repo', group))) {
  if (artifact !== 'core' && !artifact.startsWith('core-')) continue;
  rmSync(join('kmp-repo', group, artifact), { recursive: true, force: true });
  cpSync(join(base, 'kmp-repo', group, artifact), join('kmp-repo', group, artifact), { recursive: true });
}

// 2. Swift glue helper (compiled into this plugin's own Swift target – no dependency on the base pod/package).
mkdirSync('ios/Sources/KmpUmbrellaPlugin/Generated', { recursive: true });
cpSync(
  join(base, 'ios/Sources/CapacitorKmpSupport/KmpCapacitor.swift'),
  'ios/Sources/KmpUmbrellaPlugin/Generated/KmpCapacitor.swift',
);

// 3. TypeScript web helper (KmpWebPlugin).
mkdirSync('src/generated', { recursive: true });
cpSync(join(base, 'src/kmp-web.ts'), 'src/generated/kmp-web.ts');

console.log('[sync-from-base] synced core artifacts, KmpCapacitor.swift and kmp-web.ts from', base);
