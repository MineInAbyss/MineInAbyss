package com.derongan.minecraft.mineinabyss.world;

import com.derongan.minecraft.mineinabyss.AbyssContext;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;

public class EntityChunkListener implements Listener {
    private EntityChunkManager manager;
    private AbyssContext context;

    public EntityChunkListener(AbyssContext context) {
        this.manager = context.getEntityChunkManager();
        this.context = context;
    }

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
        manager.loadChunk(event.getChunk());
    }

    @EventHandler
    public void onChunkUnload(ChunkUnloadEvent event) {
        manager.unloadChunk(event.getChunk());
    }
}
