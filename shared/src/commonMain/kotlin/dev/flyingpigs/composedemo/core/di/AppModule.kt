package dev.flyingpigs.composedemo.core.di

import dev.flyingpigs.composedemo.core.network.createHttpClient
import dev.flyingpigs.composedemo.feature.login.data.LoginRepositoryImpl
import dev.flyingpigs.composedemo.feature.login.data.remote.AuthApi
import dev.flyingpigs.composedemo.feature.login.domain.LoginRepository
import dev.flyingpigs.composedemo.feature.login.presentation.LoginViewModel
import dev.flyingpigs.composedemo.feature.profile.data.ProfileRepositoryImpl
import dev.flyingpigs.composedemo.feature.profile.data.remote.ProfileApi
import dev.flyingpigs.composedemo.feature.profile.domain.ProfileRepository
import dev.flyingpigs.composedemo.feature.profile.presentation.ProfileViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

/**
 * The single Koin module that wires the app's object graph.
 *
 *  • single { }  → one shared instance for the app's lifetime (the HttpClient,
 *                  the api, the repository).
 *  • get()       → "resolve this dependency from the graph" — Koin supplies the
 *                  constructor argument, which is why the classes no longer need
 *                  default values.
 *  • viewModelOf → registers a ViewModel; `::LoginViewModel` means "call this
 *                  constructor, resolving each parameter via get()".
 *
 * Binding the INTERFACE (`single<LoginRepository> { LoginRepositoryImpl(...) }`)
 * is what lets callers depend on `LoginRepository` while Koin hands them the impl.
 */
val appModule = module {
    single { createHttpClient() }
    single { AuthApi(get()) }
    single<LoginRepository> { LoginRepositoryImpl(get()) }
    single { ProfileApi(get()) }
    single<ProfileRepository>{ ProfileRepositoryImpl(get()) }
    viewModelOf(::LoginViewModel)
    viewModelOf(::ProfileViewModel)
}
