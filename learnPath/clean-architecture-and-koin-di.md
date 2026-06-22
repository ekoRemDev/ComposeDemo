# Clean Architecture + MVI + Koin DI (vs Flutter)

> **As of: 2026-06-22.** Refactored the `login` feature from flat MVVM into
> Clean layers, added a `DataResult` wrapper, MVI events, and Koin for DI.
> See `architecture-overview.md` for the full tree.

## The three layers (per feature)

```
presentation/  ──depends on──▶  domain/  ◀──implemented by──  data/
   (UI + VM)                 (interfaces,                  (Ktor, DTOs,
                              models)                       repo impl)
```

The golden rule: **dependencies point inward**. `presentation` and `data` both
depend on `domain`; `domain` depends on nothing. So the ViewModel knows only
`LoginRepository` (an interface) and `AuthSession` (a plain model) — never Ktor,
never the JSON DTO.

| Layer | Files | Knows about |
|---|---|---|
| `domain` | `LoginRepository` (interface), `model/AuthSession` | nothing framework-y |
| `data` | `LoginRepositoryImpl`, `remote/AuthApi`, `remote/AuthDtos` | Ktor, JSON |
| `presentation` | `LoginViewModel`, `LoginUiState`, `LoginEvent`, `LoginScreen` | Compose, the domain interface |

> Flutter analogy: identical to the `data/domain/presentation` split you'd do
> with Bloc + a `Repository` interface + `freezed` models.

## `DataResult` — no exceptions cross the layer boundary

```kotlin
sealed interface DataResult<out T> {
    data class Success<T>(val data: T) : DataResult<T>
    data class Failure(val message: String) : DataResult<Nothing>
}
```

The **repository** catches the exception and returns `DataResult`, so the
ViewModel just does an exhaustive `when` — no `try/catch` in the UI layer, and a
network error can never crash a composable. (Flutter: same idea as returning
`Either<Failure, T>` from a repository.)

## MVI: events flow up through ONE entry point

Instead of the screen calling `viewModel.onUsernameChange(...)`,
`viewModel.login()`, etc., it sends **intents**:

```kotlin
sealed interface LoginEvent {
    data class UsernameChanged(val value: String) : LoginEvent
    data class PasswordChanged(val value: String) : LoginEvent
    data object Submit : LoginEvent
}

// screen:
onValueChange = { viewModel.onEvent(LoginEvent.UsernameChanged(it)) }
onClick       = { viewModel.onEvent(LoginEvent.Submit) }

// viewModel:
fun onEvent(event: LoginEvent) = when (event) {
    is LoginEvent.UsernameChanged -> username = event.value
    is LoginEvent.PasswordChanged -> password = event.value
    LoginEvent.Submit             -> login()
}
```

One funnel for everything the user does → easy to log/test, and the View stays
dumb. (Flutter: `LoginEvent` ≈ Bloc events you `add()`.)

## Koin — dependency injection

Before: every class created its own dependencies (`AuthApi()` defaulting its own
`HttpClient`). That's not real DI — it's hard-wired and un-fakeable. Koin builds
the graph in one place:

```kotlin
val appModule = module {
    single { createHttpClient() }                         // one shared instance
    single { AuthApi(get()) }                             // get() = resolve from graph
    single<LoginRepository> { LoginRepositoryImpl(get()) }// bind INTERFACE → impl
    viewModelOf(::LoginViewModel)                         // VM, params auto-resolved
}
```

Wired once at the top, common to all platforms:

```kotlin
@Composable fun App() {
    KoinApplication(application = { modules(appModule) }) { /* NavHost… */ }
}
```

And the screen asks for its ViewModel from the graph:

```kotlin
viewModel: LoginViewModel = koinViewModel()
```

Because of this, the constructors no longer have default values — Koin supplies
every dependency. To test, swap the module: `single<LoginRepository> { FakeRepo() }`.

> Flutter analogy: Koin ≈ `get_it` / `injectable` (or Riverpod providers).
> `single` ≈ a lazily-created singleton; `get()` ≈ `getIt<T>()`.

## Dependencies added

`libs.versions.toml` → `koin = "4.1.0"`; `koin-core`, `koin-compose`,
`koin-compose-viewmodel` in `commonMain`.

Key imports (these were the fiddly part):
- `org.koin.compose.KoinApplication`
- `org.koin.compose.viewmodel.koinViewModel`
- `org.koin.core.module.dsl.viewModelOf`
- `org.koin.dsl.module`

## Verify

Compiles on all four targets. Runtime check still worth doing: launch
`desktopApp`, log in with `emilys` / `emilyspass` — if Koin is missing a
definition you'd get a runtime "No definition found" error (we don't, the graph
is complete).
