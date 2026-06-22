# API calls & coroutines in KMP (vs Flutter's http/async-await)

> Goal: log in against a real API (`dummyjson.com/auth/login`) and understand
> coroutines along the way. Test creds: `emilys` / `emilyspass`.

## The cast of characters (and their Flutter equivalents)

| KMP | Flutter | Role |
|---|---|---|
| **Ktor** client | `dio` / `http` | makes the HTTP request |
| **kotlinx.serialization** `@Serializable` | `json_serializable` | JSON ⇄ data class |
| **coroutines** (`suspend`, `launch`) | `async` / `await` / `Future` | run the call without freezing the UI |
| **ViewModel** + `viewModelScope` | Bloc/Cubit/ChangeNotifier | hold state, own the async work |

The whole feature is written **once in `commonMain`** — only the HTTP *engine*
differs per platform (OkHttp on Android, CIO on desktop, Darwin on iOS, JS on
web). Those are one-line entries in `shared/build.gradle.kts`.

---

## Coroutines, the part that actually matters

### `suspend` = "can pause without blocking the thread"

```kotlin
suspend fun login(username: String, password: String): LoginResponse { ... }
```

A `suspend` function is like a Dart `async` function — but with a twist:
when it hits a network call it **suspends** (frees the thread to do other work)
and **resumes** on the same logical flow when the response arrives. You write it
as straight-line code:

```kotlin
val response = authApi.login(username, password)  // pauses here…
LoginUiState.Success(response.accessToken)         // …resumes here, no .then()
```

Dart needs `await` on every async hop. Kotlin marks the *function* `suspend`
instead, and the compiler wires up the pause/resume. **Rule:** a `suspend`
function can only be called from another `suspend` function or a coroutine.

### `launch` = start a coroutine (fire-and-forget)

```kotlin
viewModelScope.launch {
    uiState = try { LoginUiState.Success(authApi.login(...).accessToken) }
              catch (e: Exception) { LoginUiState.Error(e.message ?: "Failed") }
}
```

- `launch` starts a coroutine and returns a `Job` (no result value). Think of it
  as kicking off an un-awaited `Future` — but one that's *owned* by a scope.
- Need the returned value concurrently? Use `async { }` + `.await()`. For a
  single sequential call, `launch` is right.

### `viewModelScope` = structured concurrency = no leaks

Every coroutine belongs to a **scope**. `viewModelScope` is tied to the
ViewModel's lifetime: when the ViewModel is cleared, all its coroutines are
**cancelled automatically**. That kills the classic Flutter bug of calling
`setState` after `dispose` on a screen that's already gone — here the in-flight
request just gets cancelled with the screen.

### Threads / Dispatchers — what we deliberately did NOT do

In Android-only code you'd often write `withContext(Dispatchers.IO) { ... }`.
We don't, for two reasons:
1. **Ktor's `suspend` calls are already non-blocking** — they don't hold a
   thread while waiting, so there's nothing to move off the UI thread.
2. `Dispatchers.IO` doesn't even exist on JS/Wasm. Avoiding it keeps the code
   truly common. (Flutter's single-isolate model means you rarely think about
   this; KMP gives you the control but Ktor hides it here.)

---

## The layers (where each piece lives)

```
LoginScreen (Compose)         feature/login/LoginScreen.kt
  └─ observes uiState, renders fields / spinner / error
LoginViewModel                feature/login/LoginViewModel.kt
  └─ holds state, runs login() in viewModelScope.launch { }; depends on AuthRepository
AuthRepository (interface)    feature/login/data/AuthRepository.kt
  └─ the abstraction the ViewModel depends on (+ AuthRepositoryImpl)
AuthApi (remote source)       feature/login/data/AuthApi.kt
  └─ suspend fun login(), uses the shared Ktor client
LoginRequest / LoginResponse  feature/login/data/AuthModels.kt
  └─ @Serializable DTOs
HttpClientFactory             core/network/HttpClientFactory.kt
  └─ the one shared Ktor HttpClient
```

> Structure note: this is package-by-feature — see `architecture-overview.md`.

UI never imports Ktor. State flows **down** (`uiState`), events flow **up**
(`viewModel::login`) — the same hoisting pattern as the rest of this app.

### One-shot navigation on success: `LaunchedEffect`

```kotlin
LaunchedEffect(state) {
    if (state is LoginUiState.Success) onContinue()
}
```

Composables can recompose many times, so you can't just call `onContinue()` in
the body — it'd fire repeatedly. `LaunchedEffect(key)` runs its block only when
`key` changes, which is the correct home for one-time side effects (navigate,
show a snackbar). Flutter analogy: doing it in a listener/`addPostFrameCallback`
rather than in `build()`.

---

## Gotchas hit while wiring this up

- **`INTERNET` permission (Android):** add `<uses-permission
  android:name="android.permission.INTERNET"/>` to `androidApp/.../AndroidManifest.xml`
  or every request throws. iOS/desktop/web need nothing.
- **`ignoreUnknownKeys = true`:** the API returns more fields than we model;
  without this flag the JSON parse throws. (Flutter just ignores extra keys by
  default — kotlinx.serialization is strict unless told otherwise.)
- **One `HttpClient` for the app**, not one per call — same as reusing a single
  `Dio` instance.
- **Wasm engine:** `io.ktor:ktor-client-js` serves both the JS and the WasmJS
  targets — there's no separate `ktor-client-wasm` artifact.

## Try it

Run any target (`desktopApp` is fastest), tap into Login, hit **Login** with the
pre-filled `emilys` / `emilyspass` → spinner → navigates on success. Change the
password to something wrong → red error message instead.
