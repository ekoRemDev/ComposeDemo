# KMP practice quiz (self-test / spaced repetition)

> How to use: cover the **Answer key** at the bottom, read a question, say the
> answer out loud (or write it), then check. Or just tell Claude **"quiz me"**
> and it'll pick random ones, wait for your answer, and grade you.
>
> Questions are tagged by topic and difficulty: 🟢 recall · 🟡 understand · 🔴 apply.

---

## A. Navigation

1. 🟢 What does `popUpTo(X) { inclusive = true }` do differently from `inclusive = false`?
2. 🟡 After `navigate(Main) { popUpTo(Welcome) { inclusive = true } }`, what happens when the user presses the system back button on `Main`? Why?
3. 🟡 Why does this app have **two** `NavController`s (one in `App.kt`, one in `MainScreen.kt`)?
4. 🔴 You want `Splash → Welcome → Login → Main`, and after login the user must NOT be able to go back to Login. Write the `navigate` call for the login button.
5. 🟢 What does `composable<Login> { ... }` (the `<Login>` generic) give you over a string route?

## B. State & composition

6. 🟢 What's the difference between `remember { }` and `mutableStateOf()`? Do you need both?
7. 🟡 Why must navigation/side effects go in `LaunchedEffect(key)` instead of directly in the composable body?
8. 🟡 What is "state hoisting"? State flows ___ and events flow ___.
9. 🔴 A child dialog has a `showDialog: Boolean` parameter and gates itself with `if (showDialog)`. Why is it usually better for the *parent* to own that flag and decide whether to call the dialog at all?
10. 🟢 What does the `private set` on `var uiState by mutableStateOf(...)` accomplish?

## C. Coroutines & API

11. 🟢 What does the `suspend` keyword mean? What's the rule about where you can call a `suspend` function?
12. 🟡 Compare `launch` vs `async`. When do you use each?
13. 🟡 What is `viewModelScope` and what does "structured concurrency" buy you here? (Name the Flutter bug it prevents.)
14. 🔴 Why did we deliberately NOT wrap the Ktor call in `withContext(Dispatchers.IO)`? Give both reasons.
15. 🟢 Which Ktor plugin turns request/response bodies into JSON automatically, and what does `ignoreUnknownKeys = true` protect against?
16. 🟡 The login feature is written once in `commonMain`, but each platform needs something different. What is it, and name two of the four engines.
17. 🔴 Walk the data path: user taps Login → ... → screen navigates. Name each layer the call passes through.

## D. Multiplatform & tooling

18. 🟢 Why does an Android API call fail without a one-line manifest change? What's the line?
19. 🟡 In KMP, where does the app version live (vs Flutter's single `pubspec.yaml`)? Why can Android and iOS drift?
20. 🟢 Which single Ktor artifact serves BOTH the JS and the WasmJS targets?

## E. Clean Architecture, MVI & DI

21. 🟢 Name the three per-feature layers and the one rule about which way dependencies point.
22. 🟡 Why does `LoginRepository` live in `domain/` as an *interface*, while `LoginRepositoryImpl` lives in `data/`?
23. 🟡 What problem does `DataResult` solve, and which layer is responsible for turning an exception into a `DataResult.Failure`?
24. 🟢 Why is `AuthSession` (domain model) separate from `LoginResponse` (DTO)? Which layer maps one to the other?
25. 🟡 In MVI, what does the screen send instead of calling `viewModel.login()` directly, and what's the single ViewModel method that receives it?
26. 🔴 In the Koin module, explain `single { AuthApi(get()) }` — what do `single` and `get()` each do?
27. 🟡 Why did the classes lose their constructor default values (e.g. `repository: LoginRepository = LoginRepositoryImpl()`) after Koin was added?
28. 🟢 What does `koinViewModel()` do in `LoginScreen`, and where is the graph it resolves from started?
29. 🔴 You want to unit-test `LoginViewModel` with no real network. What's the one line you change, and where?
30. 🟡 A missing Koin binding — does it fail at compile time or run time? What does that imply about verifying a DI refactor?

---

## Answer key

1. `inclusive = true` pops `X` **itself** off the back stack too; `false` keeps `X` and only removes everything above it.
2. The app **exits**. The forward steps all use `inclusive = true`, so by the time you reach `Main` the stack is just `[Main]` — there's nothing underneath to go back to.
3. Separation of chrome: the root controller drives full-screen destinations (Splash/Welcome/Login/Main) with no bottom bar; `MainScreen`'s own controller drives the 5 tabs *inside* the Scaffold. The tab controller can't even see root destinations like `Login`.
4. `rootNavController.navigate(Main) { popUpTo(Login) { inclusive = true } }` — goes to Main and removes Login from the stack.
5. Type-safe navigation: arguments are real typed properties (e.g. `FavoriteDetail(name)`), checked at compile time, instead of string paths you parse by hand.
6. `mutableStateOf` creates observable state that triggers recomposition when it changes; `remember` keeps a value alive across recompositions. You need both together (`remember { mutableStateOf(...) }`) so the state isn't recreated every recomposition. (A ViewModel is an alternative home for the state.)
7. The composable body re-runs on every recomposition, so a direct call would fire repeatedly. `LaunchedEffect(key)` runs its block only when `key` changes — the correct place for one-shot effects (navigate, snackbar).
8. Lifting state up to a common ancestor. State flows **down** (as parameters), events flow **up** (as callbacks).
9. A small UI component shouldn't carry a "should I exist?" flag — whether it's on screen is the parent's concern. The component just renders and reports events (`onConfirm`/`onDismiss`). Cleaner, more reusable.
10. The property is readable from anywhere but only the ViewModel can change it — the UI can't mutate state directly, it must go through functions like `login()`.
11. `suspend` = the function can pause (e.g. during a network call) without blocking its thread, then resume. You can only call it from another `suspend` function or from inside a coroutine.
12. `launch` = fire-and-forget, returns a `Job`, no result value. `async` = returns a `Deferred<T>` you `.await()` for the result, used for concurrent work you need a value from. Single sequential call → `launch`.
13. A `CoroutineScope` tied to the ViewModel's lifetime; when the ViewModel is cleared, its coroutines are cancelled automatically. Prevents the "setState after dispose" class of bug (updating UI that's already gone).
14. (a) Ktor's suspend calls are already non-blocking, so there's nothing to move off the UI thread; (b) `Dispatchers.IO` doesn't exist on JS/Wasm, so using it would break common code.
15. `ContentNegotiation` (with the `json(...)` config). `ignoreUnknownKeys = true` stops the parser from throwing when the server returns fields you didn't model.
16. Each platform needs its own HTTP **engine**. Four: OkHttp (Android), CIO (desktop/JVM), Darwin (iOS), JS (web).
17. `LoginScreen` (tap) → `onEvent(LoginEvent.Submit)` → `LoginViewModel.login()` → `viewModelScope.launch` → `LoginRepository.login()` → `LoginRepositoryImpl` → `AuthApi.login()` (suspend, Ktor POST) → `LoginResponse` DTO mapped to `AuthSession`, wrapped in `DataResult.Success` → ViewModel sets `uiState = Success(token)` → `LaunchedEffect` sees Success → `onContinue()` → root nav to Main.
18. Android needs the `INTERNET` permission. Line: `<uses-permission android:name="android.permission.INTERNET"/>`.
19. Natively, per platform: Android in `gradle.properties` → `androidApp/build.gradle.kts` (`versionName`/`versionCode`); iOS in `Config.xcconfig` (`MARKETING_VERSION`/`CURRENT_PROJECT_VERSION`). They drift because there's no single source of truth — you must keep them in sync manually.
20. `io.ktor:ktor-client-js`.
21. `presentation`, `domain`, `data`. Rule: dependencies point **inward** — `presentation` and `data` both depend on `domain`; `domain` depends on nothing framework-y.
22. So the presentation layer depends on an abstraction it can't break and didn't have to know the details of. The interface (the contract) belongs in the inner `domain` layer; the Ktor/JSON implementation belongs in the outer `data` layer. This lets you swap/fake the impl and keeps `domain` free of framework code.
23. It keeps raw exceptions (and DTOs) out of the UI layer — the ViewModel just does an exhaustive `when` over `Success`/`Failure`, and a network error can't crash a composable. The **data layer** (`LoginRepositoryImpl`) catches the exception and returns `DataResult.Failure`.
24. The DTO mirrors the server's JSON shape; the domain model is what the app actually needs, independent of the wire format — so the JSON shape can change without touching the UI. The **data layer** (`LoginRepositoryImpl`) maps `LoginResponse` → `AuthSession`.
25. It sends a `LoginEvent` (e.g. `LoginEvent.Submit`, `UsernameChanged(value)`) to the single `onEvent(event)` method, which `when`s over the event and decides what to do.
26. `single { ... }` registers a **single shared instance** for the app's lifetime (created lazily, reused). `get()` **resolves a dependency from the graph** — here it supplies the `HttpClient` argument that `AuthApi`'s constructor needs, so you don't pass it by hand.
27. Because Koin now supplies every dependency. The defaults were a temporary "fake DI"; with a real container they're dead code, and removing them makes the dependencies explicit (you can't accidentally construct a class with its hidden default).
28. `koinViewModel()` asks Koin for the `LoginViewModel` (built via `viewModelOf(::LoginViewModel)`, with its `LoginRepository` auto-resolved) and scopes it to the composable. The graph is started by `KoinApplication(application = { modules(appModule) })` wrapped around the NavHost in `App.kt`.
29. Override the binding in the Koin module: `single<LoginRepository> { FakeLoginRepository() }` (or construct `LoginViewModel(FakeRepo())` directly in the test). One line, in the DI module / test setup — nothing in the ViewModel changes.
30. **Run time** — Koin resolves the graph at runtime, so a missing binding throws "No definition found" only when the screen is shown, not at compile time. Implication: compiling is necessary but not sufficient — you must actually run the flow (or write a Koin `checkModules`/verify test) to confirm a DI refactor.
