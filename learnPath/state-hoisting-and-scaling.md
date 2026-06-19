# State hoisting (FAB → HomeScreen toggle) — and when it stops scaling

Two parts:
1. The hoisting guide (current implementation the user is playing with).
2. Why it breaks at scale, and the ViewModel fix (future Phase 1 lesson).

---

## Part 1 — The problem & the hoist

**Goal:** the global FAB toggles a `Text` on `HomeScreen`.

**Why the naive way fails:** the FAB (`AppFloatingActionButton`) and `HomeScreen`
are *siblings* under the `Scaffold` in `MainScreen`. State declared **inside** the
FAB is private to it — `HomeScreen` can't see it. (The FAB's old
`var visible by remember { mutableStateOf(true) }` toggled a boolean nobody read.)

**The rule:** when two composables share state, hoist it to their **nearest common
parent** and pass it down. Here that parent is `MainScreen`.

Pattern = **state flows down, events flow up** (unidirectional data flow).

### Step 1 — `MainScreen.kt` (owns the state)

```kotlin
// imports
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

// inside MainScreen(), under rememberNavController():
var contentVisible by remember { mutableStateOf(true) }

// pass the EVENT up:
floatingActionButton = {
    AppFloatingActionButton(onToggle = { contentVisible = !contentVisible })
},

// pass the VALUE down:
composable<Home> { HomeScreen(contentVisible = contentVisible) }
```

### Step 2 — `AppFloatingActionButton.kt` (event up, owns nothing)

```kotlin
fun AppFloatingActionButton(onToggle: () -> Unit) {
    // DELETE: var visible by remember { mutableStateOf(true) }
    // in onClick: replace `visible = !visible` with:
    onToggle()
    // in dialog onDismiss: remove the dead `visible = false`
}
// getValue / setValue imports become unused — remove them.
```

### Step 3 — `HomeScreen.kt` (reads the state)

```kotlin
fun HomeScreen(contentVisible: Boolean) {
    // ...inside the Column, e.g. after Text("Hello $submittedName"):
    AnimatedVisibility(contentVisible) {
        Text("👋 This text is toggled by the FAB")
    }
}
```

### Trace the round trip
tap FAB → `onToggle()` fires **up** to `MainScreen` → flips `contentVisible` →
Compose recomposes the `composable<Home>` lambda → new value passed **down** →
`HomeScreen`'s `AnimatedVisibility` reacts.

### Scope insight
The state lives in `MainScreen` (which wraps all tabs), so the toggle **persists
across tab switches**. *Where* you hoist defines the scope — hoist into `HomeScreen`
instead and it'd be Home-only.

---

## Part 2 — Will this be a problem as the app grows? (Yes.)

Hoisting into a parent `remember` is correct for **small, local, shared UI state**
(a toggle, selected tab, expanded flag). It starts to hurt when any of these appear:

| Problem | Looks like | Why it bites |
|---|---|---|
| **Prop drilling** | passing `value` + `onX` through layers that don't use them | every signature grows; refactors ripple |
| **God composable** | `MainScreen` accumulates every feature's state | one function owns everything; hard to test/reuse |
| **No lifecycle durability** | `remember` survives recomposition but NOT rotation / process death | rotate → state resets (`rememberSaveable` only patches small cases) |
| **Logic in UI** | toggle is trivial, but soon "fetch/validate/persist" sits in a composable | can't unit-test without rendering UI |

### The fix is *scoping* + ViewModel — not "hoist higher"

1. **Hoist only as high as the lowest common ancestor that needs it.** Don't dump
   unrelated state into one parent.
2. **Replace the `remember` owner with a ViewModel** once state outlives the screen,
   holds logic, or needs testing. A ViewModel is **scoped** (screen / nav-graph / app),
   **survives config changes**, keeps **logic testable**, and screens **read from it**
   instead of receiving everything via params → kills prop drilling.

```kotlin
class HomeViewModel : ViewModel() {
    private val _contentVisible = MutableStateFlow(true)
    val contentVisible = _contentVisible.asStateFlow()
    fun toggle() { _contentVisible.value = !_contentVisible.value }
}
```

### For this specific FAB→Home case
Ask the design question: should a global FAB control one screen's content?
- **Home-only toggle:** FAB belongs to Home; state lives in `HomeViewModel`; the
  shared-parent hoist disappears.
- **Genuinely cross-cutting:** a scoped ViewModel (shared across the nav graph) or a
  DI-provided holder (Koin) owns it; FAB and Home both read it — no prop drilling.

### Honest summary
- Current hoist: correct for learning, fine for small apps.
- At scale the problem isn't *hoisting* — it's hoisting into a `remember` in a
  growing parent. Answer = **ViewModel + correct scoping** = **Phase 1**.
- Flutter parallel: same reason you reach for Provider/Riverpod/Bloc instead of
  threading callbacks through `StatefulWidget`s.

### Next lesson (when ready)
Redo this exact FAB/Home toggle with a `HomeViewModel` — feel the prop drilling and
rotation-reset problems disappear. See [README](README.md) → Phase 1.
