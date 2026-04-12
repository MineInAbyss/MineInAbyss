package com.mineinabyss.features.core

import com.mineinabyss.dependencies.get
import com.mineinabyss.dependencies.module
import com.mineinabyss.dependencies.new
import com.mineinabyss.features.AbyssFeatureConfig
import com.mineinabyss.idofront.features.listeners
import com.mineinabyss.idofront.plugin.Plugins
import com.mineinabyss.idofront.textcomponents.miniMsg
import com.nisovin.shopkeepers.api.ShopkeepersAPI
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import org.bukkit.Bukkit
import java.net.URI

val CoreFeature = module("core") {
    val config = get<AbyssFeatureConfig>().core
    require(config.enabled) { "Core feature is disabled" }

    listeners(new(::CoreListener), new(::PreventSignEditListener))
    if (Plugins.isEnabled("Shopkeepers")) {
        listeners(new(::ShopkeepersHookListener))
        ShopkeepersAPI.updateItems()
    }

    config.serverLinks.forEach {
        Bukkit.getServerLinks().addLink(it.displayName, it.url)
    }
}

@Serializable
data class CoreFeatureConfig(
    val enabled: Boolean = false,
    val waterfallDamageMultiplier: Double = 0.5,
    val waterfallMoveMultiplier: Double = 0.15,
    val bubbleColumnDamageMultiplier: Double = 2.0,
    val bubbleColumnBreathMultiplier: Int = 2,
    val serverLinks: List<ServerLink> = emptyList(),
) {
    @Serializable
    data class ServerLink(@SerialName("displayName") private val _displayName: String, @SerialName("url") private val _url: String) {
        @Transient
        val url: URI = URI.create(_url)

        @Transient
        val displayName = _displayName.miniMsg()
    }
}
