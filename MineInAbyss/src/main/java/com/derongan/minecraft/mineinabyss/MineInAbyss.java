package com.derongan.minecraft.mineinabyss;

import com.derongan.minecraft.mineinabyss.Configuration.ConfigurationManager;
import com.derongan.minecraft.mineinabyss.Layer.Configuration.LayerConfiguror;
import com.derongan.minecraft.mineinabyss.Player.PlayerListener;
import com.derongan.minecraft.mineinabyss.Relic.Loading.RelicLoader;
import com.derongan.minecraft.mineinabyss.Relic.RelicCommandExecutor;
import com.derongan.minecraft.mineinabyss.Relic.RelicDecayTask;
import com.derongan.minecraft.mineinabyss.Relic.RelicUseListener;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public final class MineInAbyss extends JavaPlugin {
    private final int TICKS_BETWEEN = 5;
    private AbyssContext context;

    @Override
    public void onEnable() {
        // Plugin startup logic
        getLogger().info("On enable has been called");
        ConfigurationManager.createConfig(this);

        context = new AbyssContext();
        context.setPlugin(this);
        context.setLogger(getLogger());
        context.setConfig(getConfig());
        context.setTickTime(TICKS_BETWEEN);

        LayerConfiguror configuror = new LayerConfiguror(getConfig(), context);

        context.setLayerMap(configuror.loadLayers());


        Runnable decayTask = new RelicDecayTask(TICKS_BETWEEN);
        getServer().getScheduler().scheduleSyncRepeatingTask(this, decayTask, TICKS_BETWEEN, TICKS_BETWEEN);

        getServer().getPluginManager().registerEvents(new RelicUseListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerListener(context), this);


        RelicCommandExecutor relicCommandExecutor = new RelicCommandExecutor(context);
        this.getCommand("relic").setExecutor(relicCommandExecutor);
        this.getCommand("relicreload").setExecutor(relicCommandExecutor);
        this.getCommand("relics").setExecutor(relicCommandExecutor);

        RelicLoader.loadAllRelics(context);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        getLogger().info("onDisable has been invoked!");
    }


    public static MineInAbyss getInstance(){
        Plugin plugin = Bukkit.getServer().getPluginManager().getPlugin("MineInAbyss");

        return (MineInAbyss)plugin;
    }
}
