package com.mineinabyss.components.advancements

import com.mineinabyss.idofront.serialization.SerializableItemStack
import com.mineinabyss.idofront.serialization.toSerializable
import eu.endercentral.crazy_advancements.NameKey
import eu.endercentral.crazy_advancements.advancement.AdvancementDisplay.AdvancementFrame
import eu.endercentral.crazy_advancements.advancement.AdvancementFlag
import eu.endercentral.crazy_advancements.advancement.AdvancementVisibility
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bukkit.Material
import org.bukkit.inventory.ItemStack

@Serializable
@SerialName("mineinabyss:custom_advancement")
open class CustomAdvancement(
    val title: String = "",
    val description: String = "",
    val icon: SerializableItemStack? = ItemStack(Material.STONE).toSerializable(),
    val frame: AdvancementFrame = AdvancementFrame.TASK,
    @SerialName("visibility") val _visibility: Visibility = Visibility.ALWAYS,
    val backgroundTexture: String? = null,
    val position: AdvancementPosition? = null,
    val flags: Array<AdvancementFlag> = AdvancementFlag.TOAST_AND_MESSAGE,
    val children: Map<String, CustomAdvancement> = emptyMap()
) {
    val visibility get() = AdvancementVisibility.parseVisibility(_visibility.name)
    enum class Visibility {
        ALWAYS, PARENTS, CHILDREN, HIDDEN
    }

    @Serializable
    data class AdvancementPosition(
        @SerialName("origin") val _origin: String,
        val offsetX: Float = 0.0f,
        val offsetY: Float = 0.0f,
    ) {
        val originId = NameKey(_origin).let { if (it.namespace == "mineinabyss") it.toString() else NameKey("mineinabyss", _origin).toString() }
    }
}
