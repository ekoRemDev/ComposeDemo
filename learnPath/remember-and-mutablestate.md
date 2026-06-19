# `remember` & `mutableStateOf` — Compose's state primitive

The example that packs in three concepts:

```kotlin
var count by remember { mutableStateOf(0) }

Button(onClick = { count++ }) {
    Text("Clicked $count times")
}
```

## There are actually 3 things in that one line

### 1. `mutableStateOf(0)` — observable state
Creates a `MutableState<Int>` holding `0`. The point isn't the value — it's that
the object is **observable**. When a `@Composable` reads `.value`, Compose records
"this composable depends on this state." When `.value` changes, Compose knows
exactly which composables to re-run.

> Flutter analog: like a `ValueNotifier<int>` that *auto-wires* its listeners.
> In Flutter you'd wrap the reader in `ValueListenableBuilder`; in Compose just
> reading the value subscribes you.

### 2. `remember { ... }` — survive recomposition
Composables run many times (every recomposition). Without `remember`,
`mutableStateOf(0)` would re-run every time and reset state to `0` on every frame,
so the counter could never increase. `remember` runs the lambda **once**, caches
the result, and returns the same object on later recompositions.

> Flutter analog: why a `StatefulWidget` keeps its `State` in a separate class —
> rebuilding the widget tree doesn't wipe your fields. `remember` gives a plain
> function that same durability.

### 3. `by` — property delegate (syntactic sugar)
`remember { mutableStateOf(0) }` returns a `MutableState<Int>` whose value is
normally `.value`. `by` delegates so you read/write `count` directly:

```kotlin
// with `by`                    // without `by`
var count by remember {                val count = remember {
    mutableStateOf(0)                      mutableStateOf(0)
}                                      }
count++                                count.value++
Text("$count")                         Text("${count.value}")
```

Needs:
```kotlin
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
```

## What happens on click

1. First composition: `count` is `0`, button shows "Clicked 0 times". The `Text`
   read `count`, so Compose subscribed it.
2. Tap → `onClick` runs `count++` → the `MutableState` changes `0 → 1`.
3. Compose recomposes **only** what read the state (the `Text`), now "Clicked 1 times".
4. `remember` returns the same `MutableState`, so the count is `1`, not reset.

The whole Compose model in miniature: **state changes → targeted recomposition →
UI reflects new state.** Reading state *is* the subscription; no `setState()`.

## Gotchas

**A. `remember` survives recomposition, NOT configuration changes / process death.**
Rotate on Android and `count` resets to `0`. Use `rememberSaveable` to survive that:
```kotlin
var count by rememberSaveable { mutableStateOf(0) }
```
This is also *why ViewModels exist* (Phase 1) — they outlive config changes entirely.

**B. You need BOTH.** Forget `remember` → state resets every recomposition (stuck at 0).
Use a plain `var` instead of `mutableStateOf` → value changes but no recomposition,
so the UI never updates. `mutableStateOf` = "notify"; `remember` = "persist".

**C. Compose only reacts to reads of observable state.** Mutating a plain variable
won't redraw anything.

## Where this lives in this project
- `HomeScreen.kt`: `var showContent by remember { mutableStateOf(false) }`,
  `var name by remember { mutableStateOf("") }`
- `SettingsScreen.kt`: `notificationsEnabled`, `darkModeEnabled`

That's the "state lives inside the widget" approach — fine for local, throwaway UI
state (text field contents, a toggle). When state must outlive the screen, be
shared, or hold business logic, you **hoist** it into a `ViewModel` → **Phase 1**.
This `remember`/`mutableStateOf` knowledge is the foundation ViewModels build on.
