package com.gmail.necnionch.myplugin.athletime.bukkit;

import com.gmail.necnionch.myplugin.athletime.common.BukkitConfigDriver;
import com.google.common.collect.Lists;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public class MainConfig extends BukkitConfigDriver {
    private final List<String> npcHologramLines = Lists.newArrayList();

    public MainConfig(JavaPlugin plugin) {
        super(plugin);
    }

    @Override
    public boolean onLoaded(FileConfiguration config) {
        if (super.onLoaded(config)) {
            npcHologramLines.clear();
            npcHologramLines.addAll(config.getStringList("npc-hologram-lines"));
            return true;
        }
        return false;
    }

    public List<String> npcHologramLines() {
        return npcHologramLines;
    }

}
