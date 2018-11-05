package com.derongan.minecraft.mineinabyss.relic.behaviour;

import org.bukkit.event.player.AsyncPlayerChatEvent;

public interface ChatRelicBehaviour extends RelicBehaviour{
    void onChat(AsyncPlayerChatEvent event);
}
