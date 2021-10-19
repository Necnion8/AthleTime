package com.gmail.necnionch.myplugin.athletime.bukkit.events;

import com.gmail.necnionch.myplugin.athletime.bukkit.parkour.Parkour;
import com.gmail.necnionch.myplugin.athletime.bukkit.parkour.ParkourPoint;
import org.bukkit.event.Event;

public abstract class ParkourPointEvent extends Event {
    private final Parkour parkour;
    private final ParkourPoint point;

    public ParkourPointEvent(Parkour parkour, ParkourPoint point) {
        this.parkour = parkour;
        this.point = point;
    }

    public Parkour getParkour() {
        return parkour;
    }

    public ParkourPoint getPoint() {
        return point;
    }

}
