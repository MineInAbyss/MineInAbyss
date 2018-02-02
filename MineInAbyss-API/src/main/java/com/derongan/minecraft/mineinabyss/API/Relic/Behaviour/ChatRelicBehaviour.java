package com.derongan.minecraft.mineinabyss.API.Relic.Behaviour;

import org.bukkit.event.player.AsyncPlayerChatEvent;

public interface ChatRelicBehaviour extends RelicBehaviour{
    void onChat(AsyncPlayerChatEvent event);
}
