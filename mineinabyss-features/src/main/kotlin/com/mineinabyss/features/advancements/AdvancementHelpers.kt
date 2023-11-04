package com.mineinabyss.features.advancements

import com.mineinabyss.components.advancements.CustomAdvancement
import com.mineinabyss.features.helpers.di.Features
import com.mineinabyss.idofront.messaging.logSuccess
import eu.endercentral.crazy_advancements.NameKey
import eu.endercentral.crazy_advancements.advancement.Advancement
import eu.endercentral.crazy_advancements.advancement.AdvancementDisplay
import eu.endercentral.crazy_advancements.manager.AdvancementManager

fun AdvancementManager.updateAdvancements() {
    Features.advancements.config.advancements.forEach {
        it.value.toAdvancement()?.let { a ->
            updateAdvancement(a)
            players.forEach { p ->
                updateTab(p, a.tab)
                updateVisibility(p)
            }
        }
    }
}

fun createAdvancements() = Features.advancements.config.advancements.forEach { (id, advancement) ->
    Features.advancements.advancementManager.addAdvancement(*createAdvancementTree(id, advancement))
}
fun createAdvancementTree(id: String, advancement: CustomAdvancement): Array<Advancement> {
    val display = AdvancementDisplay(advancement.icon?.toItemStack(), advancement.title, advancement.description, advancement.frame, advancement.visibility)
    display.backgroundTexture = advancement.backgroundTexture
    advancement.position?.let {
        display.positionOrigin = NameKey(it.originId).toAdvancement()
        display.x = it.offsetX
        display.y = it.offsetY
    }

    val rootAdvancement = Advancement(NameKey("mineinabyss", id), display, *advancement.flags)
    val children = advancement.children.map { createChildAdvancements(rootAdvancement, it.key, it.value) }.flatten().toSet()
    logSuccess("Created advancement $id")
    return (setOf(rootAdvancement) + children).toTypedArray()
}

fun createChildAdvancements(parent: Advancement, id: String, advancement: CustomAdvancement): Set<Advancement> {
    val display = AdvancementDisplay(advancement.icon?.toItemStack(), advancement.title, advancement.description, advancement.frame, advancement.visibility)
    display.backgroundTexture = advancement.backgroundTexture
    advancement.position?.let {
        display.positionOrigin = parent
        display.x = it.offsetX
        display.y = it.offsetY
    }
    val currentAdvancement = Advancement(parent, NameKey("mineinabyss", id), display, *advancement.flags)
    val children = advancement.children.map { createChildAdvancements(currentAdvancement, it.key, it.value) }.flatten().toSet()
    return setOf(currentAdvancement) + children
}
