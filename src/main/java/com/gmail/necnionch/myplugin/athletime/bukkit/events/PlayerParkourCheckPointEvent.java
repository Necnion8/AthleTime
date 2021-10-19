package com.gmail.necnionch.myplugin.athletime.bukkit.events;

import com.gmail.necnionch.myplugin.athletime.bukkit.parkour.ParkourPlayer;
import com.gmail.necnionch.myplugin.athletime.bukkit.parkour.ParkourPoint;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerInteractEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


public class PlayerParkourCheckPointEvent extends ParkourPointEvent implements Cancellable {
    private static final HandlerList HANDLERS = new HandlerList();

    private final ParkourPlayer player;
    private final PlayerInteractEvent baseEvent;

    private boolean cancelled;

    public PlayerParkourCheckPointEvent(ParkourPlayer player, ParkourPoint point, @Nullable PlayerInteractEvent baseEvent) {
        super(player.getParkour(), point);
        this.player = player;
        this.baseEvent = baseEvent;
    }

    public Player getPlayer() {
        return player.getPlayer();
    }

    public ParkourPlayer getParkourPlayer() {
        return player;
    }

    public @Nullable PlayerInteractEvent getBaseEvent() {
        return baseEvent;
    }


    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
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
