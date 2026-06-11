package dev.flyingpigs.composedemo.domain

import dev.flyingpigs.composedemo.getPlatform

class Greeting {
    private val platform = getPlatform()

    fun greet(): String {
        return sayHello(platform.name)
    }
}