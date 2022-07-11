package com.mineinabyss.helpers

import androidx.compose.runtime.Composable
import com.combimagnetron.imageloader.Gif
import com.combimagnetron.imageloader.Image
import com.github.shynixn.mccoroutine.bukkit.launch
import com.mineinabyss.guiy.components.canvases.Chest
import com.mineinabyss.guiy.inventory.GuiyOwner
import com.mineinabyss.guiy.modifiers.Modifier
import com.mineinabyss.guiy.modifiers.height
import com.mineinabyss.mineinabyss.core.mineInAbyss
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.format.Style
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import org.bukkit.entity.Player
import java.nio.file.Path

var guildMenu: Component = Component.text("")
var gifComponent = Component.text("")

@Composable
fun GuiyOwner.test(viewer: Player, title: Component) {

    Chest(
        setOf(viewer),
        title,
        Modifier.height(4),
        onClose = { viewer.closeInventory() }) {

    }
}

fun generateImages() {
    mineInAbyss.launch { generateGuildMenu() }
}

fun generateGifs() {
    generateGif()
}

private fun generateGuildMenu() {
    //val image = convertImageUrlToImageString()
    val image = convertFileToImageString("/images/guild_disband_or_leave_menu.png")
    guildMenu = convertToImageComponent(image, Key.key("minecraft", "cubes"))

}

fun generateGif(): Gif? {
    //val image = convertImageUrlToImageString()
    return convertFileToGif("/images/test.gif")
}

private fun convertFileToGif(fileName: String, ascent: Int = 8, colorType: Image.ColorType = Image.ColorType.LEGACY): Gif? {
    val path = Path.of(mineInAbyss.dataFolder.absolutePath + fileName)
    return Gif.builder().image(path).colorType(colorType).ascent(ascent).fps(20).build()

}

private fun convertFileToImageString(fileName: String, ascent: Int = 8, colorType: Image.ColorType = Image.ColorType.LEGACY): String {
    val path = Path.of(mineInAbyss.dataFolder.absolutePath + fileName)
    return Image.builder().image(path).colorType(colorType).ascent(ascent).build().generate()
}

private fun convertImageUrlToImageString(url: String, ascent: Int = 8, coloType: Image.ColorType = Image.ColorType.LEGACY): String {
    return Image.builder().image(url).colorType(coloType).ascent(ascent).build().generate()
}

fun convertToImageComponent(image: String, font: Key): TextComponent {
    return LegacyComponentSerializer.builder().useUnusualXRepeatedCharacterHexFormat().hexColors().build().deserialize(image).style(
        Style.style().font(font).build())

}
