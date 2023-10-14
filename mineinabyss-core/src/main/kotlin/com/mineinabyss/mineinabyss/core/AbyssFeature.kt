package com.mineinabyss.mineinabyss.core

import com.mineinabyss.idofront.di.DI
import kotlin.reflect.KClass

interface AbyssFeature {
    val dependsOn: Set<String> get() = setOf()

    fun MineInAbyssPlugin.loadFeature() {}

    fun MineInAbyssPlugin.enableFeature() {}

    fun MineInAbyssPlugin.disableFeature() {}

}

fun AbyssFeature.load(plugin: MineInAbyssPlugin) = plugin.loadFeature()
fun AbyssFeature.enable(plugin: MineInAbyssPlugin) = plugin.enableFeature()
fun AbyssFeature.disable(plugin: MineInAbyssPlugin) = plugin.disableFeature()

abstract class AbyssFeatureWithContext<T: Any>(val contextClass: KClass<T>): AbyssFeature {
    val context: T by DI.observe(contextClass)

    abstract fun createContext(): T

    fun createAndInjectContext(): T {
        val context = createContext()
        DI.remove(contextClass)
        DI.add(contextClass, context)
        return context
    }
}
