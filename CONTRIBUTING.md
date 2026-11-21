# Contributing

This guide provides instructions for contributing to this Capacitor plugin.

## Developing

### Local Setup

1. Fork and clone the repo.
1. Install the dependencies.

    ```shell
    npm install
    ```

1. Make sure you have a JDK 17+ (21 recommended), Xcode with the iOS SDK and the Android SDK. Gradle,
   Kotlin and Kotlin/Native are downloaded by the Gradle wrapper in `kmp/` on the first build.
1. The base plugin must be checked out next to this repository (`../capacitor-kotlin-multi-platform`)
   and built once (`npm install && npm run build` there); it is a build-time `devDependency` linked via `file:`.
1. Install SwiftLint if you're on macOS.

    ```shell
    brew install swiftlint
    ```

### Scripts

#### `npm run kmp:build`

Builds all KMP modules in `kmp/` (`./gradlew capacitorExport`): every module is published to `kmp-repo/`,
and the `:umbrella` module exports the only XCFramework (`ios/Frameworks/KmpUmbrella.xcframework`) and JS
module (`src/generated/kmp/`). Then `scripts/sync-from-base.mjs` copies the base's `core` artifacts and
Swift/TypeScript helpers into this package. All of these are git-ignored but published to npm.

#### `npm run kmp:test`

Runs the shared Kotlin tests of all feature modules on the JVM (Android host tests), the iOS simulator and Node.js.

#### `npm run build`

Run `kmp:build`, then build the plugin web assets and generate plugin API documentation using [`@capacitor/docgen`](https://github.com/ionic-team/capacitor-docgen).

It will compile the TypeScript code from `src/` into ESM JavaScript in `dist/esm/`. These files are used in apps with bundlers when your plugin is imported.

Then, Rollup will bundle the code into a single file at `dist/plugin.js`. This file is used in apps without bundlers by including it as a script in `index.html`.

#### `npm run verify`

Build and validate the web and native projects.

This is useful to run in CI to verify that the plugin builds for all platforms.

#### `npm run lint` / `npm run fmt`

Check formatting and code quality, autoformat/autofix if possible.

This template is integrated with ESLint, Prettier, and SwiftLint. Using these tools is completely optional, but the [Capacitor Community](https://github.com/capacitor-community/) strives to have consistent code style and structure for easier cooperation.

## Publishing

There is a `prepublishOnly` hook in `package.json` which prepares the plugin before publishing, so all you need to do is run:

```shell
npm publish
```

> **Note**: The [`files`](https://docs.npmjs.com/cli/v7/configuring-npm/package-json#files) array in `package.json` specifies which files get published. If you rename files/directories or add files elsewhere, you may need to update it.
