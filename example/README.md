# capacitor-kotlin-multi-platform-umbrella – example app

Demonstrates the umbrella plugin: `KmpDevice` (normalised device info) and `KmpMotion` (accelerometer
streaming with shared Kotlin step counting and shake detection) – two Capacitor plugins backed by one
Kotlin framework.

```bash
(cd ../../capacitor-kotlin-multi-platform && npm install && npm run build)   # base (build-time only)
(cd .. && npm install && npm run build)                                      # umbrella plugin
npm install
npm run build && npx cap sync
npx cap run android                                                          # or: npx cap run ios  |  npm start
```

Note that the app depends **only** on `capacitor-kotlin-multi-platform-umbrella` – not on the base plugin –
so the iOS app embeds a single Kotlin framework (`KmpUmbrella.framework`).
