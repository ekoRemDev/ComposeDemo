# Feature implementation guide (anatomy of a feature + how to add one)

> **As of: 2026-06-23.** Uses the `login` feature as the worked example.
> Clean Architecture (data/domain/presentation) + MVI + Koin DI.
> See also `clean-architecture-and-koin-di.md` and `architecture-overview.md`.

A **feature** is a self-contained vertical slice — one folder holding everything
for one piece of functionality, split into three layers. Nothing outside reaches
into it except two wiring points: **DI** and **navigation**.

---

## 1. Graphical explanation

### a) The three layers + the dependency rule

```
        ┌──────────────────────────────────────────────────────────┐
        │                     feature/login                         │
        │                                                            │
        │   ┌──────────────────┐                                     │
        │   │   PRESENTATION   │   LoginScreen (View)                │
        │   │   (Compose UI)   │   LoginViewModel                    │
        │   │                  │   LoginUiState · LoginEvent         │
        │   └────────┬─────────┘                                     │
        │            │ depends on                                    │
        │            ▼                                               │
        │   ┌──────────────────┐                                     │
        │   │     DOMAIN       │   LoginRepository (interface)       │
        │   │  (pure Kotlin)   │   model/AuthSession                 │
        │   └────────▲─────────┘   ← knows NO framework (no Ktor)    │
        │            │ implements                                    │
        │            │                                               │
        │   ┌────────┴─────────┐                                     │
        │   │      DATA        │   LoginRepositoryImpl               │
        │   │  (Ktor + JSON)   │   remote/AuthApi · remote/AuthDtos  │
        │   └──────────────────┘                                     │
        └──────────────────────────────────────────────────────────┘

   RULE: arrows point INWARD. presentation ─▶ domain ◀─ data
         domain is the stable center and depends on nothing framework-y.
```

Why it matters: the ViewModel only ever sees `LoginRepository` + `AuthSession`.
Swap Ktor for something else, change the JSON shape, or drop in a fake for tests
— the presentation layer doesn't change.

### b) Folder map (what lives where)

```
feature/login/
├─ domain/                         the "WHAT"  (contracts + models)
│  ├─ LoginRepository.kt              interface: suspend login(): DataResult<AuthSession>
│  └─ model/
│     └─ AuthSession.kt               domain model (token, username)
├─ data/                           the "HOW"   (network + mapping)
│  ├─ remote/
│  │  ├─ AuthApi.kt                   Ktor POST → LoginResponse
│  │  └─ AuthDtos.kt                  @Serializable LoginRequest / LoginResponse
│  └─ LoginRepositoryImpl.kt          implements interface; DTO→domain; try/catch→DataResult
└─ presentation/                   the "SHOW"  (UI + state)
   ├─ LoginScreen.kt                  @Composable View (koinViewModel())
   ├─ LoginViewModel.kt               holds state, runs work in viewModelScope
   ├─ LoginUiState.kt                 sealed: Idle / Loading / Success / Error
   └─ LoginEvent.kt                   sealed: UsernameChanged / PasswordChanged / Submit
```

### c) Runtime flow — one "Login" tap

```
 USER taps Login
   │
   ▼
 LoginScreen ──onEvent(LoginEvent.Submit)──▶ LoginViewModel
                                              │  uiState = Loading
                                              ▼
                                   viewModelScope.launch {           (coroutine)
                                        repository.login(u, p)
                                              │
                                              ▼
                                   LoginRepositoryImpl
                                        │ AuthApi.login()  ──HTTP POST──▶  dummyjson.com
                                        │        ◀── LoginResponse (JSON DTO) ──
                                        │ map DTO → AuthSession
                                        │ wrap → DataResult.Success / Failure
                                              │
                                              ▼
                                   when(result) {
                                     Success → uiState = Success(token)
                                     Failure → uiState = Error(message)
                                   }
                                   }
   ┌──────────────────────────────────────────┘
   ▼
 LoginScreen recomposes (reads uiState)
   • Loading → spinner    • Error → red text    • Success →
        LaunchedEffect(Success) ──▶ onContinue() ──▶ navigate to Main

   STATE flows DOWN (uiState → UI)   ·   EVENTS flow UP (onEvent → ViewModel)
   The UI NEVER sees a raw exception or a JSON DTO — the repository removes both.
```

### d) The two wiring points (a feature isn't live without these)

```
   core/di/AppModule.kt                         core/navigation + App.kt
   ─────────────────────                        ────────────────────────
   single { createHttpClient() } ─┐             @Serializable object Login
   single { AuthApi(get()) } ◀────┘             composable<Login> {
   single<LoginRepository> {                        LoginScreen(onContinue = { … })
       LoginRepositoryImpl(get()) }              }
   viewModelOf(::LoginViewModel)                       │
            │                                          │ LoginScreen asks the graph:
            └───────── builds the graph ───────────────▶  koinViewModel()
```

`single` = one shared instance · `get()` = resolve a dependency from the graph ·
binding `single<LoginRepository> { LoginRepositoryImpl(...) }` is what lets the UI
depend on the interface while Koin supplies the impl.

> Flutter analogies: layers = the `data/domain/presentation` split · `LoginEvent`
> ≈ Bloc events · `DataResult` ≈ `Either<Failure,T>` · Koin ≈ `get_it` ·
> `koinViewModel()` ≈ reading a provider · `viewModelScope` cancellation ≈ avoiding
> setState-after-dispose.

---

## 2. Component reference

| File | Layer | Purpose | Used by |
|---|---|---|---|
| `AuthSession` | domain | app's session model, server-shape-independent | ViewModel, repo impl |
| `LoginRepository` | domain | the contract the UI depends on | ViewModel, impl, DI |
| `AuthDtos` | data | exact JSON shapes | `AuthApi` only |
| `AuthApi` | data | the actual Ktor HTTP call | `LoginRepositoryImpl` |
| `LoginRepositoryImpl` | data | try/catch→`DataResult`, DTO→domain | DI |
| `LoginUiState` | presentation | every visual state (exhaustive) | ViewModel writes, Screen reads |
| `LoginEvent` | presentation | every user intent (MVI) | Screen sends, ViewModel handles |
| `LoginViewModel` | presentation | state holder + coroutine runner | Screen via `koinViewModel()` |
| `LoginScreen` | presentation | renders state, emits events, navigates | `App.kt` NavHost |

---

## 3. Step-by-step: add a NEW feature

Worked example: a **`profile`** feature that fetches the logged-in user. Same 7
steps every time.

### 1) Folders
```
feature/profile/{domain/model, data/remote, presentation}
```

### 2) domain — model + contract (start here, the stable center)
```kotlin
// domain/model/UserProfile.kt
data class UserProfile(val name: String, val email: String)

// domain/ProfileRepository.kt
interface ProfileRepository {
    suspend fun getProfile(): DataResult<UserProfile>
}
```

### 3) data — DTO, API, impl
```kotlin
// data/remote/ProfileDtos.kt
@Serializable data class ProfileDto(val firstName: String, val email: String /* … */)

// data/remote/ProfileApi.kt
class ProfileApi(private val client: HttpClient) {
    suspend fun fetch(): ProfileDto = client.get("https://dummyjson.com/users/1").body()
}

// data/ProfileRepositoryImpl.kt
class ProfileRepositoryImpl(private val api: ProfileApi) : ProfileRepository {
    override suspend fun getProfile(): DataResult<UserProfile> = try {
        val dto = api.fetch()
        DataResult.Success(UserProfile(name = dto.firstName, email = dto.email))
    } catch (e: Exception) { DataResult.Failure(e.message ?: "Failed") }
}
```

### 4) presentation — state, events, ViewModel, screen
```kotlin
// presentation/ProfileUiState.kt
sealed interface ProfileUiState {
    data object Loading : ProfileUiState
    data class Success(val profile: UserProfile) : ProfileUiState
    data class Error(val message: String) : ProfileUiState
}

// presentation/ProfileEvent.kt
sealed interface ProfileEvent { data object Load : ProfileEvent; data object Retry : ProfileEvent }

// presentation/ProfileViewModel.kt
class ProfileViewModel(private val repo: ProfileRepository) : ViewModel() {
    var uiState by mutableStateOf<ProfileUiState>(ProfileUiState.Loading); private set
    fun onEvent(e: ProfileEvent) { when (e) { ProfileEvent.Load, ProfileEvent.Retry -> load() } }
    private fun load() {
        uiState = ProfileUiState.Loading
        viewModelScope.launch {
            uiState = when (val r = repo.getProfile()) {
                is DataResult.Success -> ProfileUiState.Success(r.data)
                is DataResult.Failure -> ProfileUiState.Error(r.message)
            }
        }
    }
}

// presentation/ProfileScreen.kt
@Composable fun ProfileScreen(viewModel: ProfileViewModel = koinViewModel()) {
    LaunchedEffect(Unit) { viewModel.onEvent(ProfileEvent.Load) }   // load once on open
    when (val s = viewModel.uiState) { /* render Loading / Success / Error */ }
}
```

### 5) Register in DI — `core/di/AppModule.kt`
```kotlin
single { ProfileApi(get()) }
single<ProfileRepository> { ProfileRepositoryImpl(get()) }
viewModelOf(::ProfileViewModel)
```

### 6) Wire navigation — `core/navigation/Routes.kt` + `App.kt`/`MainScreen.kt`
```kotlin
@Serializable object Profile                       // route (may already exist)
composable<Profile> { ProfileScreen() }            // in the right NavHost
```

### 7) Compile + run
`:shared:compileKotlinJvm` (fast), then iOS/Wasm/Android, **then run** — a missing
Koin binding fails at RUNTIME, not at compile time.

### Checklist
- [ ] `domain` first (model + interface)
- [ ] `data` (DTO ≠ domain model · map in impl · try/catch → `DataResult`)
- [ ] `presentation` (sealed `UiState` · sealed `Event` · `viewModelScope` · `koinViewModel()` · `LaunchedEffect` for one-shots)
- [ ] register API + repo (bind the interface) + `viewModelOf` in `AppModule`
- [ ] add route + `composable<>{}`
- [ ] compile all targets, then run

> **Static screen shortcut** (no network, like Home/Search today): skip `data` and
> `domain` — just `feature/<name>/presentation/<Name>Screen.kt`. Add the other
> layers later, only when real logic appears.

---

## Progress log
- **2026-06-23** — Wrote this guide (feature anatomy + diagrams + new-feature recipe).
