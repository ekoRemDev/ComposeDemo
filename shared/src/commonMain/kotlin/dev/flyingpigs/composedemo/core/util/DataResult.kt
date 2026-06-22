package dev.flyingpigs.composedemo.core.util

/**
 * A tiny success/failure wrapper for data-layer results.
 *
 * The repository returns this instead of throwing, so the layers above never
 * deal with raw exceptions or know that the failure came from Ktor/JSON/etc.
 * The ViewModel just does an exhaustive `when` over it.
 *
 * (Flutter analogy: like returning an `Either<Failure, T>` or a sealed
 * `Result` from your repository instead of letting exceptions bubble into the
 * UI layer.)
 */
sealed interface DataResult<out T> {
    data class Success<T>(val data: T) : DataResult<T>
    data class Failure(val message: String) : DataResult<Nothing>
}
