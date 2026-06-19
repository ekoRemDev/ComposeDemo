# Showing the app version in shared Compose UI (generated BuildConfig)

## The problem

`WelcomeScreen` lives in `commonMain` (shared Compose), but the version lives in
`gradle.properties` (a Gradle/native concern). Shared Kotlin code **cannot read
Gradle properties at runtime**. So how do we display the version in shared UI
without hardcoding it (which would break the single source of truth from
[app-versioning.md](app-versioning.md))?

## The solution

Have **Gradle generate a Kotlin constant** from `app.versionName` /
`app.versionCode` into the shared module. One edit in `gradle.properties` then
flows to *both* the Android `versionName`/`versionCode` *and* this constant.

```
gradle.properties (app.versionName / app.versionCode)
        │
        ├──▶ androidApp/build.gradle.kts  → versionName / versionCode
        └──▶ shared/build.gradle.kts task → generated BuildConfig.kt → shared UI
```

This is the KMP equivalent of Android's `BuildConfig` — but hand-rolled, because
the multiplatform library plugin doesn't generate one across all targets.

## Implementation

### Step 1 — generator task in `shared/build.gradle.kts` (after the `plugins {}` block)

```kotlin
val appVersionName = providers.gradleProperty("app.versionName")
val appVersionCode = providers.gradleProperty("app.versionCode")

val generateBuildConfig by tasks.registering {
    val outputDir = layout.buildDirectory.dir("generated/buildConfig/kotlin")
    val versionName = appVersionName
    val versionCode = appVersionCode
    outputs.dir(outputDir)
    // Re-run whenever the version values change.
    inputs.property("versionName", appVersionName.get())
    inputs.property("versionCode", appVersionCode.get())
    doLast {
        val pkgDir = outputDir.get().asFile.resolve("dev/flyingpigs/composedemo")
        pkgDir.mkdirs()
        pkgDir.resolve("BuildConfig.kt").writeText(
            """
            |package dev.flyingpigs.composedemo
            |
            |/** Generated from gradle.properties — do not edit by hand. */
            |object BuildConfig {
            |    const val VERSION_NAME: String = "${versionName.get()}"
            |    const val VERSION_CODE: Int = ${versionCode.get()}
            |}
            |
            """.trimMargin()
        )
    }
}
```

### Step 2 — register the generated dir as a `commonMain` source

Inside `kotlin { sourceSets { ... } }`:

```kotlin
commonMain {
    kotlin.srcDir(generateBuildConfig)   // passing the task wires the build dependency
}
```

Passing the **TaskProvider** (not just the dir) makes Kotlin compilation depend on
the task automatically, so the file is regenerated before compile.

### Step 3 — use it in `WelcomeScreen.kt` (commonMain)

```kotlin
import dev.flyingpigs.composedemo.BuildConfig

Text(
    text = "Version ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})",
    style = MaterialTheme.typography.bodySmall,
)
```

### Step 4 — verify

```
./gradlew :shared:compileKotlinJvm
cat shared/build/generated/buildConfig/kotlin/dev/flyingpigs/composedemo/BuildConfig.kt
```

Generated output:
```kotlin
object BuildConfig {
    const val VERSION_NAME: String = "1.0"
    const val VERSION_CODE: Int = 1
}
```

## Usage / day-to-day

- Bump the version in **`gradle.properties`** only — the constant regenerates on the
  next build (the `inputs.property` lines ensure the task re-runs on change).
- `BuildConfig` is in package `dev.flyingpigs.composedemo` and available from **all**
  targets (Android, iOS, JVM, JS, wasm), since it's generated into `commonMain`.
- Still mirror the version into `iosApp/Configuration/Config.xcconfig` for the iOS
  store metadata (the manual step noted in [app-versioning.md](app-versioning.md)).

## Config-cache notes (why the code looks like this)

- `providers.gradleProperty(...)` instead of `project.property(...)` — configuration
  cache safe (this project sets `org.gradle.configuration-cache=true`).
- Provider values are resolved with `.get()` at execution/config time, never holding
  a reference to `project` inside `doLast`.

## Alternative considered: expect/actual

Instead of generating a constant, you could `expect fun appVersion(): String` and
read the *actually installed* version per platform (Android `PackageManager`, iOS
`Bundle.main` `CFBundleShortVersionString`). More "correct" (reflects the installed
build), but needs an `actual` per target and has no meaning on JVM/JS/wasm. The
generated constant is simpler and keeps one source of truth — good enough here.
A good Phase 7 (expect/actual) exercise to revisit later.
