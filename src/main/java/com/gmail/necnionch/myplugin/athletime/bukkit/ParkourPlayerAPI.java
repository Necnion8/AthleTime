package com.gmail.necnionch.myplugin.athletime.bukkit;

import com.gmail.necnionch.myplugin.athletime.bukkit.parkour.Parkour;
import com.gmail.necnionch.myplugin.athletime.bukkit.parkour.ParkourPlayer;
import com.gmail.necnionch.myplugin.athletime.bukkit.parkour.ParkourPoint;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.jetbrains.annotations.Nullable;


public interface ParkourPlayerAPI {
    ParkourPlayer get(Player player);

    ParkourPlayer[] getAll();

    void cancelAll();

    void cancelParkour(ParkourPlayer player, boolean teleport);

    ParkourPlayer startParkour(Parkour parkour, Player player, boolean teleport, ParkourPoint startPoint, @Nullable PlayerInteractEvent baseEvent);

}
