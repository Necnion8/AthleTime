package com.gmail.necnionch.myplugin.athletime.bukkit.listeners;

import com.gmail.necnionch.myplugin.athletime.bukkit.AthleTimePlugin;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;


public abstract class SetupHandler {
    private final Player player;
    private final Handler handler;
    private final EventListener listener = new EventListener();


    public SetupHandler(AthleTimePlugin owner, Player player, Handler onDone) {
        this.player = player;
        this.handler = onDone;
        owner.getServer().getPluginManager().registerEvents(listener, owner);
    }

    public abstract void onInteract(PlayerInteractEvent event, Block block);


    public void exit(boolean result) {
        HandlerList.unregisterAll(listener);
        handler.call(result);
    }



    public interface Handler {
        void call(boolean result);
    }


    public class EventListener implements Listener {
        @org.bukkit.event.EventHandler(priority = EventPriority.LOW)
        public void onInteract(PlayerInteractEvent event) {
            if (!player.equals(event.getPlayer()))
                return;

            if (!EquipmentSlot.HAND.equals(event.getHand()))
                return;

            Block block = event.getClickedBlock();
            if (block == null || !AthleTimePlugin.isPressurePlate(block.getType()))
                return;

            event.setCancelled(true);
            SetupHandler.this.onInteract(event, block);
        }

        @org.bukkit.event.EventHandler(priority = EventPriority.LOWEST)
        public void onPreCommand(PlayerCommandPreprocessEvent event) {
            if (!player.equals(event.getPlayer()))
                return;
            event.setCancelled(true);
            exit(false);
        }

        @org.bukkit.event.EventHandler
        public void onQuit(PlayerQuitEvent event) {
            exit(false);
        }

        @org.bukkit.event.EventHandler
        public void onDeath(PlayerDeathEvent event) {
            exit(false);
        }

    }

}
