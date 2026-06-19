# App versioning in KMP (vs Flutter's pubspec.yaml)

## The core difference

Flutter keeps one line in `pubspec.yaml` (`version: 1.0.0+1`) and generates the
native version fields for you. **KMP has no single version file by default** —
versioning is handled **natively per platform**, so the two halves of Flutter's
version string live in separate files.

| Flutter `pubspec.yaml` | Android (`androidApp/build.gradle.kts`) | iOS (`iosApp/Configuration/Config.xcconfig`) |
|---|---|---|
| `1.0.0` (name)  | `versionName` | `MARKETING_VERSION` |
| `+1` (build no.) | `versionCode` | `CURRENT_PROJECT_VERSION` |

The shared/Compose module has **no version** — it's a library. Only the app shells
(`androidApp`, `iosApp`) are versioned.

**Gotcha:** without a single source of truth, Android and iOS versions can drift
(bump one, forget the other). Flutter prevented this; in KMP you build it yourself.

---

## Chosen approach: Option 1 — `gradle.properties` single source (Android side)

> iOS `Config.xcconfig` can't read Gradle, so it's kept in sync **manually** for now.
> (A later upgrade: generate the xcconfig from these properties via a Gradle task.)

### Step 1 — declare the version in `gradle.properties`

```properties
#App version — single source of truth (keep iOS Config.xcconfig in sync manually)
app.versionName=1.0
app.versionCode=1
```

### Step 2 — read them in `androidApp/build.gradle.kts`

```kotlin
defaultConfig {
    // ...
    versionCode = providers.gradleProperty("app.versionCode").get().toInt()
    versionName = providers.gradleProperty("app.versionName").get()
}
```

`providers.gradleProperty(...)` is the configuration-cache-friendly way to read a
Gradle property (this project has `org.gradle.configuration-cache=true`).

### Step 3 — verify

```
./gradlew :androidApp:processDebugManifest
```

Then check the merged manifest contains the values:
```
versionCode="1"
versionName="1.0"
```

---

## Day-to-day: how to bump the version

1. Edit `app.versionName` / `app.versionCode` in `gradle.properties` (one place for Android).
2. Mirror the same values in `iosApp/Configuration/Config.xcconfig`
   (`MARKETING_VERSION` = versionName, `CURRENT_PROJECT_VERSION` = versionCode).

That second manual step is the only gap vs Flutter — automatable later by generating
the xcconfig from the Gradle properties.
