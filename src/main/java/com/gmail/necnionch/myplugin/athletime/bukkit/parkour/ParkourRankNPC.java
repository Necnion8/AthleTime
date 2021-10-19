package com.gmail.necnionch.myplugin.athletime.bukkit.parkour;

import com.google.common.collect.Maps;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class ParkourRankNPC {

    private final int npcId;
    private String hologramLines;

    public ParkourRankNPC(int npcId) {
        this.npcId = npcId;
    }

    public int getNPCId() {
        return npcId;
    }

    public void setHologramLines(String[] hologramLines) {
        this.hologramLines = (hologramLines != null) ? String.join("\n", hologramLines) : null;
    }

    public String[] getHologramLines() {
        return (hologramLines != null) ? hologramLines.split("\n") : null;
    }


    public static ParkourRankNPC fromConfig(Map<?, ?> config) throws ClassCastException{
        ParkourRankNPC npc = new ParkourRankNPC((Integer) config.get("npc-id"));

        if (config.containsKey("hologram-lines")) {
            //noinspection unchecked
            npc.setHologramLines(((List<String>) config.get("hologram-lines")).toArray(new String[0]));
        }

        return npc;
    }

    public Map<String, Object> toConfig() {
        Map<String, Object> map = Maps.newHashMap();
        map.put("npc-id", npcId);
        if (hologramLines != null)
            map.put("hologram-lines", Arrays.asList(getHologramLines()));
        return map;
    }

}
