# What defines which screen shows, and in which order?

> Context: the app's launch flow is `Splash → Welcome → Main`, defined in `App.kt`
> (root NavHost) with the tabbed shell nested inside `Main` (`MainScreen.kt`).

The order isn't defined in one place. It's the combination of **three things**.

## 1. Which screen shows *first* → `startDestination`

In `App.kt`, the root NavHost declares where it begins:

```kotlin
NavHost(
    navController = rootNavController,
    startDestination = Splash,   // ← THIS is the entry point
)
```

Change `Splash` to `Welcome` and the app skips the splash on every launch.
`startDestination` is the only thing that decides what renders at position one.

## 2. What screens *exist* → the `composable<…>` blocks

Inside that NavHost, each `composable<Route>` registers a destination.
**Listing order does NOT matter** — these are registrations in a map
("route `Splash` → this UI"), not a sequence:

```kotlin
composable<Splash>  { SplashScreen(...) }
composable<Welcome> { WelcomeScreen(...) }
composable<Main>    { MainScreen() }
```

You could reorder these three blocks and nothing changes.

## 3. What comes *next* → the `navigate(...)` calls

The actual order is defined by the callbacks — each screen says where to go next:

```kotlin
SplashScreen(onTimeout = {
    rootNavController.navigate(Welcome) { popUpTo(Splash) { inclusive = true } }
    //                        ^^^^^^^ Splash → Welcome
})
WelcomeScreen(onContinue = {
    rootNavController.navigate(Main) { popUpTo(Welcome) { inclusive = true } }
    //                        ^^^^ Welcome → Main
})
```

So the chain `Splash → Welcome → Main` is **emergent**, not declared in one spot:
start at `Splash`, splash navigates to `Welcome`, welcome navigates to `Main`.

## The mental model (vs Flutter)

Think of it as a **graph**, not a list:

- `startDestination` = the entry node.
- `composable<>` blocks = the nodes that exist.
- `navigate()` calls = the edges between them.

This is exactly `go_router`: `composable<Welcome>` ≈ `GoRoute(path: '/welcome')`,
and `navigate(Welcome)` ≈ `context.go('/welcome')`. The route *table* and the
*flow* are deliberately separate so any screen can reach any other.

## Two subtleties already in this code

- **`popUpTo(...) { inclusive = true }`** does NOT change what shows next — it edits
  the **back stack**. After `Splash → Welcome` it removes `Splash`, so pressing back
  from Welcome won't return to the splash. It controls *backward* order; `navigate()`
  controls *forward* order.

- **Nested NavHost = nested ordering.** `Main` is one node in the root graph, but it
  contains its *own* NavHost (`MainScreen.kt`) with its own `startDestination = HomeTab`.
  "Which tab shows first inside Main" is a separate decision from "which screen shows
  first in the app." Two independent graphs, two `startDestination`s.

## Diagram

```
Native system splash (Android theme, ~instant)
        │
        ▼
Root NavHost (App.kt)         startDestination = Splash
  ┌──────────────────────────────────────────────┐
  │  Splash ──onTimeout──▶ Welcome ──onContinue──▶ Main
  │   (1.5s delay)          (button)               │
  └────────────────────────────────────────────┬──┘
                                                │
                            Main hosts its OWN NavHost (MainScreen.kt)
                            startDestination = HomeTab
                              Home · Search · Favorites · Profile · Settings
```
