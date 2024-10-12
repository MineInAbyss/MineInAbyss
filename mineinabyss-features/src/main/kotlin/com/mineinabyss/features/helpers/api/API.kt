package com.mineinabyss.features.helpers.api

import github.scarsz.discordsrv.DiscordSRV
import org.bukkit.Bukkit
import org.bukkit.plugin.Plugin
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

object API {
    val DiscordSRV: APIDiscordSRV? by requiresPlugin("DiscordSRV") { plugin ->
        APIDiscordSRV(plugin as DiscordSRV)
    }

    inline fun <T> requiresPlugin(
        name: String,
        crossinline check: (plugin: Plugin) -> Any = { it.isEnabled },
        crossinline load: (plugin: Plugin) -> T,
    ) = object : PluginDelegate<T>(name) {
        override fun wrap(plugin: Plugin) = load(plugin)
        override fun check(plugin: Plugin) = check(plugin)
    }

    abstract class PluginDelegate<T>(
        val name: String,
    ) : ReadOnlyProperty<Any, T?> {
        abstract fun wrap(plugin: Plugin): T
        abstract fun check(plugin: Plugin): Any

        fun checkPassed(plugin: Plugin) = runCatching { check(plugin) != false }.getOrDefault(false)

        val plugin by lazy { Bukkit.getPluginManager().getPlugin(name)?.takeIf { checkPassed(it) } }
        val wrapped by lazy { plugin?.let { wrap(it) } }

        override fun getValue(thisRef: Any, property: KProperty<*>): T? {
            val plugin = plugin ?: return null
            return if (checkPassed(plugin)) wrapped else null
        }
    }
}
