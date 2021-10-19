package com.gmail.necnionch.myplugin.athletime.bukkit.parkour;

import org.bukkit.Location;


public class ParkourPoint {
    private final Parkour parkour;
    private final Type type;
    private final Location location;

    public ParkourPoint(Parkour parkour, Type type, Location location) {
        this.parkour = parkour;
        this.type = type;
        this.location = location;
    }

    public Parkour getParkour() {
        return parkour;
    }

    public Type getType() {
        return type;
    }

    public Location getLocation() {
        return location;
    }


    public enum Type {
        START("開始ポイント"), CHECK("中間ポイント"), END("最終ポイント");

        public final String name;

        Type(String name) {
            this.name = name;
        }
    }

}
