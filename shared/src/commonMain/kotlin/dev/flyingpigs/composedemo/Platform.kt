package dev.flyingpigs.composedemo

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform