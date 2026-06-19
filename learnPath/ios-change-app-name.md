# How to change the app name (display name) on iOS

> Context: KMP project with a separate Xcode app in `iosApp/`. The shared Compose
> UI is platform-agnostic; the app's home-screen name is configured natively.

## The situation in this project

The iOS app has **no display name set**, so the label under the icon falls back to
`PRODUCT_NAME` = `composedemo` (from `iosApp/Configuration/Config.xcconfig`).

- `iosApp/iosApp/Info.plist` has no `CFBundleDisplayName`.
- The Xcode project uses `GENERATE_INFOPLIST_FILE = YES` and sets no
  `INFOPLIST_KEY_CFBundleDisplayName`.

The fix is to set **`CFBundleDisplayName`** — the iOS home-screen label.

## Option A — Xcode GUI (easiest)

1. Open `iosApp/iosApp.xcodeproj` in Xcode.
2. Select the **iosApp** target → **General** tab.
3. Under **Identity**, set **Display Name** to e.g. `Compose Study Demo`.
4. Build & run. Xcode writes this as `INFOPLIST_KEY_CFBundleDisplayName` in the
   project build settings.

## Option B — edit `Info.plist` directly (no Xcode needed)

Add a `CFBundleDisplayName` key to `iosApp/iosApp/Info.plist`:

```xml
<key>CFBundleDisplayName</key>
<string>Compose Study Demo</string>
```

Because `GENERATE_INFOPLIST_FILE = YES`, Xcode merges this entry into the generated
plist, and `CFBundleDisplayName` overrides the `PRODUCT_NAME` fallback for the
visible label.

## ⚠️ Don't change `PRODUCT_NAME` in `Config.xcconfig`

It's tempting (it's where `composedemo` lives), but `PRODUCT_NAME` is also the
**executable/product name** and is woven into `PRODUCT_BUNDLE_IDENTIFIER`
(`dev.flyingpigs.composedemo.composedemo$(TEAM_ID)`). Changing it can break the
bundle ID and framework linking. `CFBundleDisplayName` changes *only* the visible
name — exactly what's wanted.

## The KMP angle (vs Flutter / Android)

The app name is **native, per-platform config**, separate from shared Compose code.
To keep the name consistent across platforms, set each one natively:

| Platform | Where |
|---|---|
| Android  | `androidApp/.../res/values/strings.xml` → `app_name` (currently `Compose Study Demo`) |
| iOS      | `Info.plist` → `CFBundleDisplayName` (or Xcode General → Display Name) |

(Flutter analog: you'd edit `AndroidManifest.xml` `android:label` and iOS
`CFBundleDisplayName` — same native files, just without the Flutter layer on top.)
