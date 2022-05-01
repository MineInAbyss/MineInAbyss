package com.mineinabyss.helpers.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import com.github.shynixn.mccoroutine.bukkit.launch
import com.mineinabyss.guiy.guiyPlugin
import org.bukkit.entity.Player

@Deprecated("Moved to guiy")
class Navigator<T>(val default: () -> T) {
    private val screen: T? get() = screens.lastOrNull()
    private val screens = mutableStateListOf<T>()
    private val universal = mutableStateListOf<UniversalScreens>()

    init {
        open(default())
    }

    fun back() = universal.removeLastOrNull() ?: screens.removeLastOrNull()
    fun open(screen: T) = screens.add(screen)
    fun open(screen: UniversalScreens) = universal.add(screen)

    fun reset() {
        screens.clear()
        open(default())
    }

    /**
     * Entrypoint for handling composition based on screen.
     *
     * Includes universal defaults like an Anvil screen.
     */
    @Composable
    fun withScreen(players: Set<Player>, onEmpty: () -> Unit, run: @Composable (T) -> Unit) {
        if (universal.isNotEmpty()) {
            when (val screen = universal.first()) {
                is UniversalScreens.Anvil -> LaunchedEffect(screen) {
                    guiyPlugin.launch {
                        screen.builder.open(players.first()).inventory
                    }
                }
            }
        } else screen?.let { run(it) } ?: onEmpty()
    }
}

@Composable
fun <T> rememberNavigation(default: () -> T) = remember {
    Navigator(default)
}
