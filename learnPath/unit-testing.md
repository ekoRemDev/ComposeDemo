# Unit testing in KMP (vs Flutter's test/ + mocktail)

> **As of: 2026-06-23.** First real unit tests: `LoginViewModel` (fake repo) and
> `LoginRepositoryImpl` (Ktor MockEngine). 8 tests pass via `:shared:jvmTest`.

## Where tests live

```
shared/src/
├─ commonTest/   ← write tests HERE — they run on every platform
├─ jvmTest/      ← platform-specific tests (rarely needed)
├─ iosTest/
└─ androidHostTest/
```

Put tests in **`commonTest`**: one test, runs on JVM, iOS, Android, JS/Wasm.
Mirror the main package path (`feature/login/presentation/…`).

> Flutter analogy: `commonTest` ≈ `test/`. `kotlin.test` ≈ `package:test`
> (`@Test`/`assertEquals` ≈ `test()`/`expect()`).

## Dependencies (commonTest)

```kotlin
commonTest.dependencies {
    implementation(libs.kotlin.test)             // @Test, assertEquals, assertTrue…
    implementation(libs.kotlinx.coroutines.test) // runTest, TestDispatcher, setMain
    implementation(libs.ktor.client.mock)        // MockEngine — fake HTTP
}
```

## Technique 1 — fake an interface (ViewModel test)

Because `LoginViewModel` depends on the `LoginRepository` **interface**, you
hand-write a fake — no mocking library needed:

```kotlin
private class FakeLoginRepository(private val result: DataResult<AuthSession>) : LoginRepository {
    override suspend fun login(username: String, password: String) = result
}
```

Then drive the ViewModel and assert state:

```kotlin
val vm = LoginViewModel(FakeLoginRepository(DataResult.Success(AuthSession("abc", "emilys"))))
vm.onEvent(LoginEvent.Submit)
assertTrue(vm.uiState is LoginUiState.Success)
```

> Flutter analogy: a hand-written fake instead of `mocktail`'s `MockX`. Kotlin
> mocking libs exist (MockK), but for interfaces a fake is simpler and KMP-safe.

### The coroutine gotcha — `viewModelScope` needs a Main dispatcher

`viewModelScope.launch` dispatches on `Dispatchers.Main`, which doesn't exist in
a unit test → "Module with Main dispatcher is missing". Fix per test class:

```kotlin
@BeforeTest fun setUp() { Dispatchers.setMain(UnconfinedTestDispatcher()) }
@AfterTest  fun tearDown() { Dispatchers.resetMain() }
```

`UnconfinedTestDispatcher` runs launched coroutines **eagerly**, so the state has
already reached `Success`/`Error` by the time `onEvent()` returns — no waiting.
Wrap the test body in `runTest { }` (the multiplatform coroutine test builder).

> Flutter analogy: `setMain` ≈ controlling time with `fakeAsync`; `runTest` ≈
> the async test zone. Forgetting `setMain` is the KMP version of "pump the
> widget tree".

## Technique 2 — fake the HTTP engine (repository test)

Don't hit the network — swap Ktor's engine for `MockEngine`, which returns canned
responses. This tests the repo + AuthApi together (JSON → DTO → domain mapping
and error handling):

```kotlin
val engine = MockEngine { respond(
    content = """{ "id":1,"username":"emilys", … ,"accessToken":"tok_123","refreshToken":"r" }""",
    status = HttpStatusCode.OK,
    headers = headersOf(HttpHeaders.ContentType, "application/json"),
) }
val client = HttpClient(engine) { install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) } }
val repo = LoginRepositoryImpl(AuthApi(client))

val result = repo.login("emilys", "emilyspass")
assertTrue(result is DataResult.Success)
assertEquals("tok_123", result.data.token)   // accessToken mapped → token
```

A non-2xx / unparseable response → the repo's `try/catch` → `DataResult.Failure`.

> Flutter analogy: `MockEngine` ≈ `MockClient` from `package:http/testing.dart`.

## Running tests

```bash
./gradlew :shared:jvmTest         # fastest — use this while developing
./gradlew :shared:allTests        # every platform (slow)
./gradlew :shared:iosSimulatorArm64Test
```
HTML report: `shared/build/reports/tests/jvmTest/index.html`.

## What to test at each layer (rule of thumb)

| Layer | Test with | Assert |
|---|---|---|
| ViewModel | fake repository | state transitions for each event (Idle→Loading→Success/Error) |
| Repository | `MockEngine` | DTO→domain mapping, failures become `DataResult.Failure` |
| Domain | plain JUnit | any pure logic (none yet — models are dumb data) |
| UI (Compose) | `compose-ui-test` | (not set up yet — later phase) |

## Known follow-up

`AuthApi` relies on the JSON failing to parse to surface HTTP errors (Ktor's
`expectSuccess` defaults to `false`, so a 400 doesn't throw by itself). For
robust error handling, set `expectSuccess = true` on the client (or check
`response.status`). Worth a dedicated test once added.

## Progress log
- **2026-06-23** — Added coroutines-test + ktor-client-mock; wrote ViewModel
  (fake repo) and repository (MockEngine) tests. 8 tests green on JVM.
