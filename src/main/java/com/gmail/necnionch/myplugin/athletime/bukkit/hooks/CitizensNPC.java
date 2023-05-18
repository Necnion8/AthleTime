package com.gmail.necnionch.myplugin.athletime.bukkit.hooks;

import com.gmail.necnionch.myplugin.athletime.bukkit.AthleTimePlugin;
import com.gmail.necnionch.myplugin.athletime.bukkit.parkour.Parkour;
import com.gmail.necnionch.myplugin.athletime.bukkit.parkour.ParkourContainer;
import com.gmail.necnionch.myplugin.athletime.bukkit.parkour.ParkourRankNPC;
import com.gmail.necnionch.myplugin.athletime.bukkit.record.Record;
import com.gmail.necnionch.myplugin.athletime.bukkit.record.RecordContainer;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.event.NPCRemoveByCommandSenderEvent;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.npc.NPCRegistry;
import net.citizensnpcs.trait.HologramTrait;
import net.citizensnpcs.trait.SkinTrait;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


public class CitizensNPC {
    private final AthleTimePlugin plugin;
    private final ParkourContainer container;
    private final RecordContainer records;
    private NPCRegistry registry;
    private boolean hooked;

    public CitizensNPC(AthleTimePlugin plugin, ParkourContainer container, RecordContainer records) {
        this.plugin = plugin;
        this.container = container;
        this.records = records;
    }


    public boolean hook(Plugin plugin) {
        hooked = false;

        if (plugin == null || !plugin.isEnabled())
            return false;
        try {
            Class.forName("net.citizensnpcs.api.CitizensAPI");
        } catch (ClassNotFoundException e) {
            return false;
        }
        registry = CitizensAPI.getNPCRegistry();
        plugin.getServer().getPluginManager().registerEvents(new NPCListener(), plugin);
        hooked = true;
        return true;
    }

    public boolean isHooked() {
        return hooked;
    }


    public NPC getById(int id) {
        if (!isHooked())
            throw new IllegalStateException("Citizens not hooked!");
        return registry.getById(id);
    }

    public NPC getByEntity(Entity entity) {
        if (!isHooked())
            throw new IllegalStateException("Citizens not hooked!");

        return registry.getNPC(entity);
    }

    public Iterable<NPC> getNPCs() {
        if (!isHooked())
            throw new IllegalStateException("Citizens not hooked!");
        return registry;
    }


    public boolean updateNPC(Parkour parkour) {
        if (!isHooked())
            return false;

        List<Record> records = this.records.getSortedRecords(parkour.getName());

        boolean changedSetting = false;

        List<ParkourRankNPC> npcs = parkour.rankNPCs().entrySet().stream()
                .sorted(Comparator.comparingInt(Map.Entry::getKey))
                .map(Map.Entry::getValue).collect(Collectors.toList());

        int rank = 1;
        for (ParkourRankNPC npc : npcs) {
            if (updateNPC(npc, (records.isEmpty()) ? null : records.remove(0), rank++))
                changedSetting = true;
        }
        return changedSetting;
    }

    public boolean updateNPC(ParkourRankNPC rankNPC, Record record, int rank) {
        if (!isHooked())
            throw new IllegalStateException("Citizens not hooked!");

        NPC npc = getById(rankNPC.getNPCId());
        if (npc == null)
            return false;

        SkinTrait skin = npc.getTraitNullable(SkinTrait.class);
        if (skin != null) {
            if (record != null) {
                skin.setSkinName(record.getPlayerName(), true);
            } else {
                skin.clearTexture();
            }
        }

        HologramTrait hologram = npc.getTraitNullable(HologramTrait.class);
        if (hologram != null) {
            List<String> npcHologramLines = plugin.getMainConfig().npcHologramLines();

            String lines = (1 <= rank && rank <= npcHologramLines.size()) ? npcHologramLines.get(rank - 1)
                    .replaceAll("\\{time}", (record != null) ? record.getFormattedTime() : "§7N/A")
                    .replaceAll("\\{player}", (record != null) ? record.getPlayerName() : "§7N/A") : null;

            // 遅延をいれないと見えなくなる。内部的に行われるNPCRemoveとタイミングを少しずらさないといけない？
            plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> {
                hologram.clear();
                if (lines != null) {
                    String[] split = lines.split("\n");
                    for (int i = 0; i < split.length; i++) {
                        hologram.addLine(split[split.length - i - 1]);
                    }
                }

                hologram.onDespawn();
                hologram.onSpawn();
            }, 1);
        }

        return true;
    }


    public class NPCListener implements Listener {
        @EventHandler(ignoreCancelled = true)
        public void onRemove(NPCRemoveByCommandSenderEvent event) {
            boolean modified = false;
            for (Parkour parkour : container.getAll()) {
                if (parkour.rankNPCs().remove(event.getNPC().getId()) != null)
                    modified = true;
            }
            if (modified)
                container.saveAll();
        }
    }

}
