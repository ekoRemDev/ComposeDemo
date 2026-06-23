package dev.flyingpigs.composedemo.feature.profile.presentation

sealed interface ProfileEvent {
    data object Load : ProfileEvent; data object ReTry : ProfileEvent
}