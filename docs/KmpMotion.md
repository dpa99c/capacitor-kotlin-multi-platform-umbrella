# KmpMotion API

<docgen-index>

* [`isMotionAvailable()`](#ismotionavailable)
* [`requestMotionPermission()`](#requestmotionpermission)
* [`startMotionUpdates(...)`](#startmotionupdates)
* [`stopMotionUpdates()`](#stopmotionupdates)
* [`addListener('motion', ...)`](#addlistenermotion-)
* [`addListener('step', ...)`](#addlistenerstep-)
* [`addListener('shake', ...)`](#addlistenershake-)
* [`addListener('motionError', ...)`](#addlistenermotionerror-)
* [`removeAllListeners()`](#removealllisteners)
* [Interfaces](#interfaces)
* [Type Aliases](#type-aliases)

</docgen-index>

<docgen-api>
<!--Update the source file JSDoc comments and rerun docgen to update the docs below-->

Accelerometer streaming (Android `SensorManager`, iOS `CMMotionManager`, W3C `devicemotion`) with
shared Kotlin step counting and shake detection.

### isMotionAvailable()

```typescript
isMotionAvailable() => Promise<MotionAvailability>
```

Whether a hardware accelerometer is available, and which source to use. Uses the device feature
(in the same Kotlin runtime) to recommend the simulated source on emulators and simulators.

**Returns:** <code>Promise&lt;<a href="#motionavailability">MotionAvailability</a>&gt;</code>

**Since:** 0.1.0

--------------------


### requestMotionPermission()

```typescript
requestMotionPermission() => Promise<MotionPermission>
```

Requests access to motion data where required (iOS Safari). Call from a user gesture on the web.

**Returns:** <code>Promise&lt;<a href="#motionpermission">MotionPermission</a>&gt;</code>

**Since:** 0.1.0

--------------------


### startMotionUpdates(...)

```typescript
startMotionUpdates(options?: StartMotionOptions | undefined) => Promise<MotionSession>
```

Starts streaming accelerometer data through the shared Kotlin `MotionProcessor`, which emits
`motion`, `step` and `shake` events. Restarts the stream if already running.

Rejects with code `UNAVAILABLE` when `source` is `'device'` and there is no accelerometer.

| Param         | Type                                                              |
| ------------- | ----------------------------------------------------------------- |
| **`options`** | <code><a href="#startmotionoptions">StartMotionOptions</a></code> |

**Returns:** <code>Promise&lt;<a href="#motionsession">MotionSession</a>&gt;</code>

**Since:** 0.1.0

--------------------


### stopMotionUpdates()

```typescript
stopMotionUpdates() => Promise<MotionSummary>
```

Stops the motion stream and returns a summary of the session.

**Returns:** <code>Promise&lt;<a href="#motionsummary">MotionSummary</a>&gt;</code>

**Since:** 0.1.0

--------------------


### addListener('motion', ...)

```typescript
addListener(eventName: 'motion', listenerFunc: (sample: MotionSample) => void) => Promise<PluginListenerHandle>
```

Processed accelerometer samples, at most every `intervalMs`.

| Param              | Type                                                                       |
| ------------------ | -------------------------------------------------------------------------- |
| **`eventName`**    | <code>'motion'</code>                                                      |
| **`listenerFunc`** | <code>(sample: <a href="#motionsample">MotionSample</a>) =&gt; void</code> |

**Returns:** <code>Promise&lt;<a href="#pluginlistenerhandle">PluginListenerHandle</a>&gt;</code>

**Since:** 0.1.0

--------------------


### addListener('step', ...)

```typescript
addListener(eventName: 'step', listenerFunc: (event: StepEvent) => void) => Promise<PluginListenerHandle>
```

A step detected by the shared step counter.

| Param              | Type                                                                |
| ------------------ | ------------------------------------------------------------------- |
| **`eventName`**    | <code>'step'</code>                                                 |
| **`listenerFunc`** | <code>(event: <a href="#stepevent">StepEvent</a>) =&gt; void</code> |

**Returns:** <code>Promise&lt;<a href="#pluginlistenerhandle">PluginListenerHandle</a>&gt;</code>

**Since:** 0.1.0

--------------------


### addListener('shake', ...)

```typescript
addListener(eventName: 'shake', listenerFunc: (event: ShakeEvent) => void) => Promise<PluginListenerHandle>
```

A shake gesture detected by the shared shake detector.

| Param              | Type                                                                  |
| ------------------ | --------------------------------------------------------------------- |
| **`eventName`**    | <code>'shake'</code>                                                  |
| **`listenerFunc`** | <code>(event: <a href="#shakeevent">ShakeEvent</a>) =&gt; void</code> |

**Returns:** <code>Promise&lt;<a href="#pluginlistenerhandle">PluginListenerHandle</a>&gt;</code>

**Since:** 0.1.0

--------------------


### addListener('motionError', ...)

```typescript
addListener(eventName: 'motionError', listenerFunc: (error: MotionError) => void) => Promise<PluginListenerHandle>
```

The motion stream failed after it was started (e.g. the sensor became unavailable).

| Param              | Type                                                                    |
| ------------------ | ----------------------------------------------------------------------- |
| **`eventName`**    | <code>'motionError'</code>                                              |
| **`listenerFunc`** | <code>(error: <a href="#motionerror">MotionError</a>) =&gt; void</code> |

**Returns:** <code>Promise&lt;<a href="#pluginlistenerhandle">PluginListenerHandle</a>&gt;</code>

**Since:** 0.1.0

--------------------


### removeAllListeners()

```typescript
removeAllListeners() => Promise<void>
```

Removes all listeners for this plugin.

**Since:** 0.1.0

--------------------


### Interfaces


#### MotionAvailability

| Prop                    | Type                                                  | Description                                                                                     |
| ----------------------- | ----------------------------------------------------- | ----------------------------------------------------------------------------------------------- |
| **`device`**            | <code>boolean</code>                                  | A hardware accelerometer (or, on the web, the DeviceMotion API) is available.                   |
| **`simulated`**         | <code>boolean</code>                                  | The simulated source (pure Kotlin) is always available.                                         |
| **`isVirtualDevice`**   | <code>boolean</code>                                  | Running on an emulator / simulator – determined by the *device* feature (a direct Kotlin call). |
| **`recommendedSource`** | <code><a href="#motionsource">MotionSource</a></code> | `'device'` on real hardware with an accelerometer, otherwise `'simulated'`.                     |


#### MotionPermission

| Prop          | Type                 |
| ------------- | -------------------- |
| **`granted`** | <code>boolean</code> |


#### MotionSession

| Prop             | Type                                                  |
| ---------------- | ----------------------------------------------------- |
| **`source`**     | <code><a href="#motionsource">MotionSource</a></code> |
| **`intervalMs`** | <code>number</code>                                   |


#### StartMotionOptions

| Prop                 | Type                                                  | Description                                                                                                                                                         | Default               |
| -------------------- | ----------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------- | --------------------- |
| **`intervalMs`**     | <code>number</code>                                   | Requested sampling / event interval in milliseconds (16–1000).                                                                                                      | <code>50</code>       |
| **`source`**         | <code><a href="#motionsource">MotionSource</a></code> | `'device'` uses the platform accelerometer; `'simulated'` uses a pure-Kotlin signal generator (walking + periodic shake), handy on simulators and desktop browsers. | <code>'device'</code> |
| **`shakeThreshold`** | <code>number</code>                                   | Linear acceleration (m/s²) a peak must exceed to count towards a shake.                                                                                             | <code>13</code>       |


#### MotionSummary

| Prop          | Type                 | Description                                |
| ------------- | -------------------- | ------------------------------------------ |
| **`running`** | <code>boolean</code> | Whether a stream was running when stopped. |
| **`samples`** | <code>number</code>  |                                            |
| **`steps`**   | <code>number</code>  |                                            |
| **`shakes`**  | <code>number</code>  |                                            |


#### PluginListenerHandle

| Prop         | Type                                      |
| ------------ | ----------------------------------------- |
| **`remove`** | <code>() =&gt; Promise&lt;void&gt;</code> |


#### MotionSample

| Prop                  | Type                | Description                                                                  |
| --------------------- | ------------------- | ---------------------------------------------------------------------------- |
| **`x`**               | <code>number</code> | Acceleration including gravity in m/s² (Android/W3C axes on every platform). |
| **`y`**               | <code>number</code> |                                                                              |
| **`z`**               | <code>number</code> |                                                                              |
| **`linearMagnitude`** | <code>number</code> | Magnitude of the gravity-free acceleration in m/s².                          |
| **`pitch`**           | <code>number</code> | Tilt derived from the gravity vector, in degrees.                            |
| **`roll`**            | <code>number</code> |                                                                              |
| **`steps`**           | <code>number</code> |                                                                              |
| **`shakes`**          | <code>number</code> |                                                                              |
| **`timestamp`**       | <code>number</code> | Monotonic timestamp in milliseconds.                                         |


#### StepEvent

| Prop            | Type                        | Description                                                              |
| --------------- | --------------------------- | ------------------------------------------------------------------------ |
| **`count`**     | <code>number</code>         |                                                                          |
| **`cadence`**   | <code>number \| null</code> | Steps per minute over the last 10 s (null until two steps are detected). |
| **`timestamp`** | <code>number</code>         |                                                                          |


#### ShakeEvent

| Prop            | Type                | Description                                    |
| --------------- | ------------------- | ---------------------------------------------- |
| **`count`**     | <code>number</code> |                                                |
| **`intensity`** | <code>number</code> | Peak linear acceleration of the shake in m/s². |
| **`timestamp`** | <code>number</code> |                                                |


#### MotionError

| Prop          | Type                |
| ------------- | ------------------- |
| **`code`**    | <code>string</code> |
| **`message`** | <code>string</code> |


### Type Aliases


#### MotionSource

<code>'device' | 'simulated'</code>

</docgen-api>
