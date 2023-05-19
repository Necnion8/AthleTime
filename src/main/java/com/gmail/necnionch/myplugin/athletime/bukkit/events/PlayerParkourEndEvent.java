package com.gmail.necnionch.myplugin.athletime.bukkit.events;

import com.gmail.necnionch.myplugin.athletime.bukkit.parkour.ParkourPlayer;
import com.gmail.necnionch.myplugin.athletime.bukkit.parkour.ParkourPoint;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerInteractEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


public class PlayerParkourEndEvent extends ParkourPointEvent implements Cancellable {
    private static final HandlerList HANDLERS = new HandlerList();

    private final ParkourPlayer player;
    private final PlayerInteractEvent baseEvent;
    private @Nullable Location moveLocation;

    private boolean cancelled;

    public PlayerParkourEndEvent(ParkourPlayer player, ParkourPoint point, @Nullable PlayerInteractEvent baseEvent, @Nullable Location moveLocation) {
        super(player.getParkour(), point);
        this.player = player;
        this.baseEvent = baseEvent;
        this.moveLocation = moveLocation;
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

    public @Nullable Location getMoveLocation() {
        return moveLocation;
    }

    public void setMoveLocation(@Nullable Location location) {
        this.moveLocation = location;
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
