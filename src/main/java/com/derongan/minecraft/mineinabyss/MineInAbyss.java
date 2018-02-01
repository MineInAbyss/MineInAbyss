package com.derongan.minecraft.mineinabyss;

import com.derongan.minecraft.mineinabyss.Ascension.AscensionCommandExecutor;
import com.derongan.minecraft.mineinabyss.Ascension.AscensionListener;
import com.derongan.minecraft.mineinabyss.Ascension.AscensionTask;
import com.derongan.minecraft.mineinabyss.Configuration.ConfigurationManager;
import com.derongan.minecraft.mineinabyss.Layer.Layer;
import com.derongan.minecraft.mineinabyss.Relic.Loading.RelicLoader;
import com.derongan.minecraft.mineinabyss.Relic.RelicCommandExecutor;
import com.derongan.minecraft.mineinabyss.Relic.RelicDecayTask;
import com.derongan.minecraft.mineinabyss.Relic.RelicUseListener;
import org.apache.commons.dbutils.DbUtils;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public final class MineInAbyss extends JavaPlugin {
    private final int TICKS_BETWEEN = 5;
    AbyssContext context;

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

        for (Map layerData : getConfig().getMapList("layers")) {
            Layer layer = new Layer((String) layerData.get("name"), context);

            List<List<Integer>> sections = (List<List<Integer>>) layerData.get("sectionOffsets");

            if(sections != null){
                context.getLogger().info("Section data found");

            } else {
                context.getLogger().info("No section data");
                sections = new ArrayList<>();
            }

            layer.setSectionsOnLayer(sections);
            layer.setEffectsOnLayer((Collection<Map>) layerData.get("effects"));
            layer.setDeathMessage((String) layerData.getOrDefault("abyssDeathMessage", null));
            layer.setOffset((int)layerData.getOrDefault("offset", 50));
            context.getLayerMap().put(layer.getName(), layer);
        }

        Runnable mainTask = new AscensionTask(context, TICKS_BETWEEN);
        getServer().getScheduler().scheduleSyncRepeatingTask(this, mainTask, TICKS_BETWEEN, TICKS_BETWEEN);

        Runnable decayTask = new RelicDecayTask(TICKS_BETWEEN);
        getServer().getScheduler().scheduleSyncRepeatingTask(this, decayTask, TICKS_BETWEEN, TICKS_BETWEEN);

//        Runnable lootTask = new DistributionTask(context);
//        getServer().getScheduler().scheduleSyncRepeatingTask(this, lootTask, TICKS_BETWEEN, TickUtils.milisecondsToTicks(20000));

        getServer().getPluginManager().registerEvents(new AscensionListener(context), this);
        getServer().getPluginManager().registerEvents(new RelicUseListener(), this);


        RelicCommandExecutor relicCommandExecutor = new RelicCommandExecutor(context);
        this.getCommand("relic").setExecutor(relicCommandExecutor);
        this.getCommand("relicreload").setExecutor(relicCommandExecutor);

        AscensionCommandExecutor ascensionCommandExecutor = new AscensionCommandExecutor(context);
        this.getCommand("sectionon").setExecutor(ascensionCommandExecutor);
        this.getCommand("sectionoff").setExecutor(ascensionCommandExecutor);

        RelicLoader.loadAllRelics(context);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        getLogger().info("onDisable has been invoked!");
        DbUtils.closeQuietly(context.getConnection());//todo this is bad cus the connection might just be created then trashed here
    }
}
