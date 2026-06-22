# App architecture overview (snapshot)

> **As of: 2026-06-22**
> A point-in-time map of how ComposeDemo is structured. Update the snapshot date
> and add a line to the **Progress log** at the bottom whenever the architecture
> meaningfully changes — that's the running history of the learning/dev process.

## Big picture: one shared brain, four thin shells

A **Compose Multiplatform** app. Almost everything — UI, navigation, state,
networking — lives in `:shared/commonMain` and is written **once**. Each platform
is a thin entry point that boots the shared `App()` composable.

```
androidApp/   MainActivity.kt        ┐
desktopApp/   main.kt                ├─▶  :shared  App()  ◀─ all real code lives here
webApp/       main.kt (JS + Wasm)    │
iosApp/       iOSApp.swift           ┘
```

`expect/actual` covers the small platform-specific bits: `Platform.kt` (common
`expect`) with `.android/.ios/.jvm/.js/.wasmJs` actuals.

> **Flutter analogy:** `:shared` is your `lib/`; the four app modules are the
> auto-generated `android/`, `ios/`, `web/` runners.

## Structure: package-by-feature, Clean layers + MVI

Code is grouped **by feature**; each feature is split into Clean layers
(`data` / `domain` / `presentation`). Cross-feature infrastructure lives in
`core/`; shared widgets stay in `ui/`. Koin wires the object graph.

```
dev/flyingpigs/composedemo/
├─ App.kt                      ← KoinApplication + root NavHost (Splash→Welcome→Login→Main)
├─ MainScreen.kt              ← Scaffold shell + nested tab NavHost
├─ Platform.kt               ← expect/actual platform info
├─ core/                     ← cross-feature infrastructure
│  ├─ di/AppModule.kt            ← Koin module (the object graph)
│  ├─ network/HttpClientFactory  ← shared Ktor client
│  ├─ navigation/Routes.kt       ← @Serializable type-safe routes
│  └─ util/DataResult.kt         ← success/failure wrapper (no exceptions to UI)
├─ feature/
│  ├─ login/                 ← the one feature with real layers
│  │  ├─ data/
│  │  │  ├─ remote/AuthApi.kt        (Ktor source)
│  │  │  ├─ remote/AuthDtos.kt       (@Serializable DTOs)
│  │  │  └─ LoginRepositoryImpl.kt   (maps DTO→domain, wraps DataResult)
│  │  ├─ domain/
│  │  │  ├─ LoginRepository.kt       (interface)
│  │  │  └─ model/AuthSession.kt     (domain model)
│  │  └─ presentation/
│  │     ├─ LoginViewModel.kt
│  │     ├─ LoginUiState.kt          (sealed state)
│  │     ├─ LoginEvent.kt            (sealed user intents — MVI)
│  │     └─ LoginScreen.kt           (koinViewModel())
│  ├─ splash/ welcome/ home/ search/ favorites/ profile/ settings/
│  │     └─ presentation/<Name>Screen.kt   ← static screens, presentation-only
├─ ui/
│  ├─ components/            ← shared widgets (AppTopBar, AppBottomBar, FAB, LogoutDialog…)
│  └─ samples/               ← learning sandboxes (AlertDialog, LazyRow)
└─ domain/                   ← Greeting / GreetingUtil (thin, legacy)
```

> Static screens are **presentation-only** — no empty `data/`/`domain/` folders.
> Add those layers per-feature only when a screen grows real logic.

The login feature's layers and the dependency direction (UI → domain ← data):

| Layer | Where (login) | Role |
|---|---|---|
| **Presentation** | `presentation/` | `LoginScreen` (View) + `LoginViewModel`; sends `LoginEvent`s, observes `LoginUiState`; depends only on the domain interface |
| **Domain** | `domain/` | `LoginRepository` interface + `AuthSession` model — pure, no framework deps |
| **Data** | `data/` | `LoginRepositoryImpl` (maps DTO→domain, returns `DataResult`) → `remote/AuthApi` + `AuthDtos` |
| **Infra / DI** | `core/` | Ktor client, `DataResult`, routes, and the Koin `appModule` |

## Two key structural patterns

**1. Nested navigation, two NavControllers.** The root `NavHost` in `App.kt`
drives full-screen, chrome-less destinations (Splash/Welcome/Login/Main). `Main`
is itself `MainScreen`, which owns a **second** `NavController` for the 5
bottom-bar tabs inside a `Scaffold`. So the top/bottom bar only exist inside
`Main`. See `navigation-screen-order.md`.

**2. State hoisting + callbacks-up / data-down.** Screens are mostly stateless
and take callbacks (`onContinue`, `onLogout`, `onFavoriteClick`). Navigation
logic lives where the controller lives (in `App.kt`), passed down as lambdas —
e.g. logout threads `App → MainScreen → AppTopBar → LogoutDialog`. See
`state-hoisting-and-scaling.md`.

## State management — mid-transition

Two styles coexist right now:
- **Local UI state**: `remember { mutableStateOf(...) }` directly in composables
  (e.g. `AppTopBar`'s dialog flag, `MainScreen`'s FAB visibility). See
  `remember-and-mutablestate.md`.
- **ViewModel state**: only `LoginViewModel` so far — `mutableStateOf`-backed
  sealed `LoginUiState` (`Idle/Loading/Success/Error`), network call run in
  `viewModelScope.launch`. See `api-calls-and-coroutines.md`.

## Tech stack

- **UI**: Compose Multiplatform + Material 3
- **Navigation**: `navigation-compose` (type-safe `@Serializable` routes)
- **Async**: Kotlin coroutines (`viewModelScope`)
- **Networking**: Ktor (OkHttp / CIO / Darwin / JS engines per target)
- **Serialization**: kotlinx.serialization
- **Build**: Gradle version catalog; app version single-sourced from
  `gradle.properties` via a generated `BuildConfig.kt` (see `app-versioning.md`)

## Honest gaps (next learning phases)

- **`StateFlow` not used yet** — state is `mutableStateOf`; the idiomatic
  `StateFlow` + `collectAsStateWithLifecycle` is still ahead.
- **No persistence** — the login token is in-memory only; no `data/local/`
  source yet (e.g. multiplatform-settings/DataStore for the token).
- **No UseCases** — `LoginViewModel` calls `LoginRepository` directly. Fine:
  add `domain/usecase/` per-feature only when real business logic appears.
- **DI module is one flat `appModule`** — fine now; split into per-feature Koin
  modules when there are several features.

✅ Resolved: DI (Koin), repository abstraction, domain model, `DataResult`,
MVI events, full feature-grouping — see the progress log.

## Target architecture (where we're heading)

```
UI (Composable)
   ↕  state down / events up
ViewModel  ──uses──▶  Repository (interface)
                          │  implemented by
                          ▼
                      AuthApi (Ktor)  +  local storage (token)
   ▲
   └─ everything wired by a DI container (Koin)
```

---

## Progress log

Newest first. One line per meaningful architectural change — this is the running
history of the learning/dev journey.

- **2026-06-22** — Refactored to **Clean Architecture + MVI + Koin DI**: split
  login into `data`(remote/repo-impl) / `domain`(interface + `AuthSession` model) /
  `presentation`(VM + `LoginUiState` + `LoginEvent`); added `core/util/DataResult`
  (repo returns success/failure, no exceptions to UI); moved `Routes` →
  `core/navigation`; introduced Koin (`core/di/AppModule`, `KoinApplication` in
  `App.kt`, `koinViewModel()` in the screen) and removed all constructor-default
  "fake DI"; migrated every screen to `feature/<name>/presentation/`. All 4
  targets compile.
- **2026-06-22** — Restructured to **package-by-feature MVVM**: moved login UI +
  ViewModel + data into `feature/login/`, extracted the shared Ktor client to
  `core/network/HttpClientFactory`, and introduced an `AuthRepository` interface
  (+ `AuthRepositoryImpl`) so the ViewModel depends on an abstraction. All 4
  targets compile.
- **2026-06-22** — Added real API login: Ktor multiplatform client (4 engines),
  `@Serializable` DTOs (`data/`), `LoginViewModel` with coroutines +
  sealed `LoginUiState`, interactive `LoginScreen`. Introduced first ViewModel
  and first `data/` layer. Added `INTERNET` permission (Android).
- **2026-06-22** — Logout flow: dedicated stateless `LogoutDialog`, callback
  threaded `App → MainScreen → AppTopBar`; `Login` route added to root NavHost.
- **(earlier)** — Established baseline: shared Compose UI across 4 platforms,
  nested navigation (root + tab NavHosts), state hoisting, single-source app
  versioning. Foundational notes in `learnPath/`.
