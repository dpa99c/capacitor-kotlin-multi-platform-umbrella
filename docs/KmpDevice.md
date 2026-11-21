# KmpDevice API

<docgen-index>

* [`getDeviceInfo()`](#getdeviceinfo)
* [Interfaces](#interfaces)
* [Type Aliases](#type-aliases)

</docgen-index>

<docgen-api>
<!--Update the source file JSDoc comments and rerun docgen to update the docs below-->

Device information read with platform APIs (Android `Build`, iOS `UIDevice`, browser `navigator`)
and normalised by shared Kotlin code.

### getDeviceInfo()

```typescript
getDeviceInfo() => Promise<DeviceInfo>
```

Returns normalised device information (OS version and user-agent parsing, locale normalisation,
form-factor classification, minimum-OS policy – all shared Kotlin).

**Returns:** <code>Promise&lt;<a href="#deviceinfo">DeviceInfo</a>&gt;</code>

**Since:** 0.1.0

--------------------


### Interfaces


#### DeviceInfo

| Prop                   | Type                                              | Description                                                                          |
| ---------------------- | ------------------------------------------------- | ------------------------------------------------------------------------------------ |
| **`platform`**         | <code>'android' \| 'ios' \| 'web'</code>          |                                                                                      |
| **`manufacturer`**     | <code>string</code>                               |                                                                                      |
| **`model`**            | <code>string</code>                               |                                                                                      |
| **`operatingSystem`**  | <code>string</code>                               | `Android`, `iOS`, `iPadOS`, `macOS`, `Windows`, …                                    |
| **`osVersion`**        | <code>string</code>                               |                                                                                      |
| **`locale`**           | <code>string</code>                               | BCP-47 locale tag, e.g. `en-GB`.                                                     |
| **`language`**         | <code>string</code>                               |                                                                                      |
| **`region`**           | <code>string</code>                               |                                                                                      |
| **`formFactor`**       | <code><a href="#formfactor">FormFactor</a></code> |                                                                                      |
| **`screen`**           | <code><a href="#screeninfo">ScreenInfo</a></code> |                                                                                      |
| **`isVirtual`**        | <code>boolean</code>                              | Emulator / simulator.                                                                |
| **`browser`**          | <code>string</code>                               | Browser name and major version (web only).                                           |
| **`minimumOsVersion`** | <code>string</code>                               | Minimum OS version required by the (fictional) product policy implemented in Kotlin. |
| **`isOsSupported`**    | <code>boolean</code>                              |                                                                                      |
| **`summary`**          | <code>string</code>                               | Human readable one-liner, e.g. `Google Pixel 9 Pro · Android 16.0 · en-GB`.          |


#### ScreenInfo

| Prop                 | Type                | Description                                |
| -------------------- | ------------------- | ------------------------------------------ |
| **`width`**          | <code>number</code> | Logical width (dp / points / CSS pixels).  |
| **`height`**         | <code>number</code> | Logical height (dp / points / CSS pixels). |
| **`scale`**          | <code>number</code> | Physical pixels per logical pixel.         |
| **`physicalWidth`**  | <code>number</code> |                                            |
| **`physicalHeight`** | <code>number</code> |                                            |


### Type Aliases


#### FormFactor

<code>'phone' | 'tablet' | 'desktop'</code>

</docgen-api>
