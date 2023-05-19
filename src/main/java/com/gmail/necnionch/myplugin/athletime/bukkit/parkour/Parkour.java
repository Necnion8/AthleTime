package com.gmail.necnionch.myplugin.athletime.bukkit.parkour;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Parkour {
    private final String name;
    private String worldName;
    private final List<Location> startPoints;
    private final List<Location> checkPoints;
    private final List<Location> endPoints;
    private final Map<Integer, ParkourRankNPC> rankNPCs;
    private @Nullable Location finishTeleportPosition;


    public Parkour(String name, String worldName, List<Location> startPoints, List<Location> checkPoints, List<Location> endPoints, Map<Integer, ParkourRankNPC> rankNPCs) {
        this.name = name;
        this.worldName = worldName;
        this.startPoints = startPoints;
        this.checkPoints = checkPoints;
        this.endPoints = endPoints;
        this.rankNPCs = rankNPCs;
    }

    public Parkour(String name) {
        this(name, null, Lists.newArrayList(), Lists.newArrayList(), Lists.newArrayList(), Maps.newHashMap());
    }

    public String getName() {
        return name;
    }

    public String getWorldName() {
        return worldName;
    }

    public void setWorldName(String worldName) {
        this.worldName = worldName;
    }

    public List<Location> startPoints() {
        return startPoints;
    }

    public List<Location> checkPoints() {
        return checkPoints;
    }

    public List<Location> endPoints() {
        return endPoints;
    }

    public Map<Integer, ParkourRankNPC> rankNPCs() {
        return rankNPCs;
    }

    public ParkourRankNPC newRankNPC(int npcId) {
        if (rankNPCs.containsKey(npcId))
            throw new IllegalArgumentException("already added npc id");
        ParkourRankNPC npc = new ParkourRankNPC(npcId);
        rankNPCs.put(npcId, npc);
        return npc;
    }

    public @Nullable Location getFinishTeleportPosition() {
        return finishTeleportPosition;
    }

    public void setFinishTeleportPosition(@Nullable Location location) {
        this.finishTeleportPosition = location;
    }

    public static Parkour fromConfig(String name, ConfigurationSection config) {
        Parkour parkour = new Parkour(name);
        parkour.worldName = config.getString("world");

        for (String point : config.getStringList("start-points")) {
            String[] split = point.split(",", 4);
            int x = Integer.parseInt(split[0]);
            int y = Integer.parseInt(split[1]);
            int z = Integer.parseInt(split[2]);
            float direction = Float.parseFloat(split[3]);

            parkour.startPoints().add(new Location(null, x, y, z, direction, 0));
        }

        for (String point : config.getStringList("check-points")) {
            String[] split = point.split(",", 4);
            int x = Integer.parseInt(split[0]);
            int y = Integer.parseInt(split[1]);
            int z = Integer.parseInt(split[2]);
            float direction = Float.parseFloat(split[3]);

            parkour.checkPoints().add(new Location(null, x, y, z, direction, 0));
        }

        for (String point : config.getStringList("end-points")) {
            String[] split = point.split(",", 4);
            int x = Integer.parseInt(split[0]);
            int y = Integer.parseInt(split[1]);
            int z = Integer.parseInt(split[2]);
            float direction = Float.parseFloat(split[3]);

            parkour.endPoints().add(new Location(null, x, y, z, direction, 0));
        }

        for (Map<?, ?> map : config.getMapList("rank-npc")) {
            ParkourRankNPC npc;
            try {
                npc = ParkourRankNPC.fromConfig(map);
            } catch (ClassCastException ignored) {
                continue;
            }
            parkour.rankNPCs.put(npc.getNPCId(), npc);
        }

        String finishToTeleportPosition = config.getString("finish-to-teleport");
        if (finishToTeleportPosition != null) {
            String[] split = finishToTeleportPosition.split(",", 4);
            int x = Integer.parseInt(split[0]);
            int y = Integer.parseInt(split[1]);
            int z = Integer.parseInt(split[2]);
            float direction = Float.parseFloat(split[3]);
            parkour.finishTeleportPosition = new Location(null, x, y, z, direction, 0);
        }
        return parkour;
    }

    public Map<String, Object> toConfig() {
        HashMap<String, Object> config = Maps.newHashMap();
        config.put("world", worldName);
        config.put("start-points", startPoints.stream().map(l -> l.getBlockX() + "," + l.getBlockY() + "," + l.getBlockZ() + "," + l.getYaw()).collect(Collectors.toList()));
        config.put("check-points", checkPoints.stream().map(l -> l.getBlockX() + "," + l.getBlockY() + "," + l.getBlockZ() + "," + l.getYaw()).collect(Collectors.toList()));
        config.put("end-points", endPoints.stream().map(l -> l.getBlockX() + "," + l.getBlockY() + "," + l.getBlockZ() + "," + l.getYaw()).collect(Collectors.toList()));
        config.put("rank-npc", rankNPCs.values().stream().map(ParkourRankNPC::toConfig).collect(Collectors.toList()));

        if (finishTeleportPosition != null) {
            config.put("finish-to-teleport", finishTeleportPosition.getBlockX() + "," +
                    finishTeleportPosition.getBlockY() + "," +
                    finishTeleportPosition.getBlockZ() + "," +
                    finishTeleportPosition.getYaw());
        }
        return config;
    }


    public Map<String, ParkourPoint> getPoints() {
        if (worldName == null)
            return Collections.emptyMap();

        Map<String, ParkourPoint> points = startPoints.stream()
                .collect(Collectors.toMap(loc -> transLocationKey(worldName, loc), loc -> new ParkourPoint(this, ParkourPoint.Type.START, loc)));
        points.putAll(checkPoints.stream()
                .collect(Collectors.toMap(loc -> transLocationKey(worldName, loc), loc -> new ParkourPoint(this, ParkourPoint.Type.CHECK, loc))));
        points.putAll(endPoints.stream()
                .collect(Collectors.toMap(loc -> transLocationKey(worldName, loc), loc -> new ParkourPoint(this, ParkourPoint.Type.END, loc))));
        return points;
    }

    public static String transLocationKey(World world, Location location) {
        return location.getBlockX() + "," + location.getBlockY() + "," + location.getBlockZ() + "," + world.getName();
    }

    public static String transLocationKey(String world, Location location) {
        return location.getBlockX() + "," + location.getBlockY() + "," + location.getBlockZ() + "," + world;
    }


}
