package com.gmail.necnionch.myplugin.athletime.bukkit;

import com.gmail.necnionch.myplugin.athletime.bukkit.command.CommandBukkit;
import com.gmail.necnionch.myplugin.athletime.bukkit.commands.MainCommand;
import com.gmail.necnionch.myplugin.athletime.bukkit.hooks.CitizensNPC;
import com.gmail.necnionch.myplugin.athletime.bukkit.listeners.PlayerListener;
import com.gmail.necnionch.myplugin.athletime.bukkit.parkour.Parkour;
import com.gmail.necnionch.myplugin.athletime.bukkit.parkour.ParkourContainer;
import com.gmail.necnionch.myplugin.athletime.bukkit.record.RecordContainer;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;


public final class AthleTimePlugin extends JavaPlugin {
    public static final BaseComponent[] PREFIX = TextComponent.fromLegacyText("§7[§6Athle§7] ");

    private final ParkourContainer container = new ParkourContainer(this);
    private final RecordContainer records = new RecordContainer(this);
    private final CitizensNPC citizens = new CitizensNPC(this, container, records);
    private final PlayerListener listener = new PlayerListener(this, container, records, citizens);

    @Override
    public void onEnable() {
        PluginManager mgr = getServer().getPluginManager();

        container.load();
        records.load();

        mgr.registerEvents(listener, this);

        CommandBukkit.register(new MainCommand(this, container), Objects.requireNonNull(getCommand("athletime")));

        if (citizens.hook(mgr.getPlugin("Citizens"))) {
            getLogger().info("Citizens hooked!");

            try {
                for (Parkour parkour : container.getAll()) {
                    citizens.updateNPC(parkour);
                }
            } catch (Throwable e) {
                e.printStackTrace();
            }

        }

    }

    @Override
    public void onDisable() {
        listener.cancelAll();
    }

    @Override
    public boolean onCommand(CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        sender.sendMessage(ChatColor.RED + "Plugin is not enabled!");
        return true;
    }


    public ParkourContainer getContainer() {
        return container;
    }

    public RecordContainer getRecords() {
        return records;
    }

    public CitizensNPC getCitizens() {
        return citizens;
    }

    public ParkourPlayerAPI getParkourPlayer() {
        return listener;
    }


    public static BaseComponent[] makeMessage(net.md_5.bungee.api.ChatColor color, String message) {
        ComponentBuilder builder = new ComponentBuilder("");
        builder.append(AthleTimePlugin.PREFIX);
        builder.append(message).color(color);
        return builder.create();
    }

    public static boolean isPressurePlate(Material material) {
        switch (material) {
            case STONE_PRESSURE_PLATE:
            case OAK_PRESSURE_PLATE:
            case ACACIA_PRESSURE_PLATE:
            case BIRCH_PRESSURE_PLATE:
            case DARK_OAK_PRESSURE_PLATE:
            case HEAVY_WEIGHTED_PRESSURE_PLATE:
            case LIGHT_WEIGHTED_PRESSURE_PLATE:
            case JUNGLE_PRESSURE_PLATE:
            case SPRUCE_PRESSURE_PLATE:
                return true;
        }
        return false;
    }

}
