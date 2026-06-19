# KMP Learning Path — Flutter → Kotlin Multiplatform

A living plan for transitioning from Flutter to KMP / Compose Multiplatform, tracked
against the **ComposeDemo** project. Update the checkboxes as we go.

> Legend: ✅ done · 🔶 partial · ⬜ not started · 🎯 recommended next

Last updated: 2026-06-18 (consolidated all learning notes under `learnPath/`)

---

## Topic notes (deep dives)

This README is the index/roadmap; each file below is a focused deep dive captured
as questions come up.

- [navigation-screen-order.md](navigation-screen-order.md) — what defines which screen shows, and in which order (`startDestination` vs `composable<>` vs `navigate()`).
- [ios-change-app-name.md](ios-change-app-name.md) — change the iOS home-screen app name via `CFBundleDisplayName` (and why not to touch `PRODUCT_NAME`).
- [app-versioning.md](app-versioning.md) — how versioning works in KMP vs Flutter's pubspec.yaml; `gradle.properties` single-source setup for Android.
- [show-version-in-shared-ui.md](show-version-in-shared-ui.md) — display the app version in shared Compose UI via a Gradle-generated `BuildConfig.kt`.
- [remember-and-mutablestate.md](remember-and-mutablestate.md) — Compose's state primitive: `mutableStateOf` (observable) + `remember` (survives recomposition) + the `by` delegate; recomposition model and gotchas.
- [state-hoisting-and-scaling.md](state-hoisting-and-scaling.md) — hoisting shared state to a common parent (FAB → HomeScreen toggle guide), and why it stops scaling → ViewModel + scoping (Phase 1).

---

## Flutter → KMP mental map

| Flutter | KMP / Compose Multiplatform | Status in this project |
|---|---|---|
| Widget | `@Composable` function | ✅ used everywhere |
| `StatefulWidget` + `setState` | `remember { mutableStateOf() }` | ✅ used (HomeScreen) |
| Bloc / Riverpod / Provider | `ViewModel` + `StateFlow` + state hoisting | ⬜ **gap** |
| `Navigator` / go_router | Navigation Compose (`@Serializable` routes) | ✅ solid |
| `dio` / `http` | Ktor client + kotlinx.serialization | ⬜ |
| `shared_preferences` / Hive | Multiplatform DataStore | ⬜ |
| `sqflite` / Drift | SQLDelight / Room KMP | ⬜ |
| `get_it` / `injectable` (DI) | Koin | ⬜ |
| `cached_network_image` | Coil 3 (multiplatform) | ⬜ |
| Platform channels | `expect` / `actual` | 🔶 only the generated `Platform` stub |
| `Future` / `async`-`await` | `suspend` / Coroutines + Flow | 🔶 implicit only |
| `flutter_test` | `kotlin.test` + Compose UI test | 🔶 stubs exist, not exercised |

---

## What's already built ✅

- **Multiplatform targets**: Android, iOS (arm64 + sim), JVM/Desktop, JS, wasmJs — all configured in `shared/build.gradle.kts`.
- **Type-safe Navigation Compose**: `@Serializable` route objects in `ui/navigation/Routes.kt`, nested tab graphs (`navigation<HomeTab>{...}`), arguments via `toRoute<FavoriteDetail>()`.
- **Scaffold app shell**: top bar, bottom nav (5 tabs), FAB, directional slide transitions based on tab index (`App.kt`).
- **Compose UI fundamentals**: `Column`/`Row`/`LazyColumn`/`LazyRow`, `Modifier` chains, `AnimatedVisibility`, `OutlinedTextField`, gesture handling (`detectTapGestures`), focus management.
- **expect/actual basics**: `Platform.kt` interface + per-platform `actual` implementations.
- **Component extraction**: reusable `AppTopBar`, `AppBottomBar`, `Logo`, `GreetingLabel`, etc.
- **Nested navigation / app-launch flow**: root NavHost (`Splash → Welcome → Main`) with the tabbed Scaffold isolated inside `Main` (the "ShellRoute" pattern). Splash auto-advances via `LaunchedEffect` + `delay` (first taste of coroutines). Forward steps use `popUpTo(inclusive = true)` so back doesn't return to splash/welcome.
- **Test source sets** present for common/iOS/JVM/androidHost.

---

## Roadmap (prioritized)

### Phase 1 — State management: ViewModel + StateFlow 🎯 ⬜
*The single biggest gap. Direct analog to the Bloc/Riverpod knowledge you already have.*
Dependencies are **already present** (`lifecycle-viewmodel-compose`, `lifecycle-runtime-compose`).

- [ ] Create `HomeViewModel : ViewModel()` holding a `MutableStateFlow<HomeUiState>`.
- [ ] Model `HomeUiState` as an immutable data class (name, submittedName, showContent).
- [ ] Move the `Button` enable / submit logic out of the composable into VM functions (`onNameChange`, `onSubmit`).
- [ ] Collect state with `collectAsStateWithLifecycle()`; obtain the VM via `viewModel { }`.
- [ ] Understand the difference: state **hoisting** (stateless composable + caller owns state) vs ViewModel ownership.
- [ ] Show why `remember` survives recomposition but ViewModel survives configuration changes / nav.

### Phase 2 — Coroutines & Flow fundamentals ⬜
*Underpins everything async. Worth a focused pass before networking.*

- [ ] `suspend` functions, `viewModelScope.launch`, structured concurrency.
- [ ] `Flow` vs `StateFlow` vs `SharedFlow`; cold vs hot.
- [ ] Loading/Error/Success UI state with a sealed interface.

### Phase 3 — Networking: Ktor + serialization ⬜
- [ ] Add Ktor client + per-platform engines (OkHttp/Darwin/JS) to `shared/build.gradle.kts`.
- [ ] `@Serializable` DTOs, `ContentNegotiation { json() }`.
- [ ] A `Repository` exposing `suspend` calls, consumed by a ViewModel.
- [ ] Replace the hardcoded `FavoritesScreen` list with fetched data.

### Phase 4 — Dependency injection: Koin ⬜
- [ ] Define modules; inject Repository → ViewModel.
- [ ] `koinViewModel()` in Compose; start Koin per platform entry point.

### Phase 5 — Persistence: DataStore ⬜
- [ ] Multiplatform DataStore for settings (e.g. theme, last name).
- [ ] Wire into `SettingsScreen`; survive app restart.

### Phase 6 — Local database: SQLDelight or Room KMP ⬜
- [ ] Persist favorites locally; observe as `Flow`.

### Phase 7 — Deeper expect/actual ⬜
- [ ] A real platform feature beyond the stub (e.g. `openUrl`, share sheet, or battery/info).
- [ ] Understand `expect class`, `expect fun`, and when to prefer interface + DI instead.

### Phase 8 — Theming & design system ⬜
- [ ] Replace hardcoded `Color(187,134,252)` with a proper `MaterialTheme` color scheme.
- [ ] Light/dark theme + dynamic color; typography & shape tokens.

### Phase 9 — Resources & assets ⬜
- [ ] `compose.components.resources` for shared strings/images/fonts (already a dependency).
- [ ] Localization.

### Phase 10 — Testing ⬜
- [ ] Unit-test a ViewModel (state transitions) in `commonTest`.
- [ ] Compose UI test for a screen.
- [ ] Run the existing iOS/JVM/androidHost test stubs and understand each source set.

---

## Known cleanups / debt to revisit
- `App.kt` hardcodes `Color(187,134,252)` in two places → fold into theme (Phase 8).
- `HomeScreen` mixes UI + logic → refactor in Phase 1.
- `FavoritesScreen` list is hardcoded → becomes the Ktor demo target (Phase 3).
- Test files exist but appear to be stubs → flesh out in Phase 10.

---

## How we'll work
- One phase at a time; each lands a small working change plus a short "why it differs from Flutter" note.
- This file is the source of truth — check items off and bump *Last updated* as we progress.
