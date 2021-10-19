package com.gmail.necnionch.myplugin.athletime.bukkit.events;

import com.gmail.necnionch.myplugin.athletime.bukkit.parkour.Parkour;
import com.gmail.necnionch.myplugin.athletime.bukkit.parkour.ParkourPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;


public class PlayerParkourCancelEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();

    private final ParkourPlayer player;


    public PlayerParkourCancelEvent(ParkourPlayer player) {
        this.player = player;
    }

    public Player getPlayer() {
        return player.getPlayer();
    }

    public ParkourPlayer getParkourPlayer() {
        return player;
    }

    public Parkour getParkour() {
        return player.getParkour();
    }


    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

}
