package com.mineinabyss.features.guilds.ui

import androidx.compose.runtime.Composable
import com.mineinabyss.guiy.components.button.Button
import com.mineinabyss.guiy.components.items.Text
import com.mineinabyss.guiy.modifiers.Modifier
import com.mineinabyss.guiy.navigation.LocalBackGestureDispatcher

@Composable
fun BackButton(modifier: Modifier = Modifier.Companion, content: @Composable () -> Unit = { Text("<red><b>Back") }) {
    val backHandler = LocalBackGestureDispatcher.current ?: return
    Button(onClick = { backHandler.onBack() }, modifier = modifier) {
        content()
    }
}
