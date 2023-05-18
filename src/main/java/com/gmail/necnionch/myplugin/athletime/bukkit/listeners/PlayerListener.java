package com.gmail.necnionch.myplugin.athletime.bukkit.listeners;

import com.gmail.necnionch.myplugin.athletime.bukkit.AthleTimePlugin;
import com.gmail.necnionch.myplugin.athletime.bukkit.ParkourPlayerAPI;
import com.gmail.necnionch.myplugin.athletime.bukkit.events.PlayerParkourCancelEvent;
import com.gmail.necnionch.myplugin.athletime.bukkit.events.PlayerParkourCheckPointEvent;
import com.gmail.necnionch.myplugin.athletime.bukkit.events.PlayerParkourEndEvent;
import com.gmail.necnionch.myplugin.athletime.bukkit.events.PlayerParkourStartEvent;
import com.gmail.necnionch.myplugin.athletime.bukkit.hooks.CitizensNPC;
import com.gmail.necnionch.myplugin.athletime.bukkit.parkour.*;
import com.gmail.necnionch.myplugin.athletime.bukkit.record.Record;
import com.gmail.necnionch.myplugin.athletime.bukkit.record.RecordContainer;
import com.gmail.necnionch.myplugin.athletime.common.LegacySounds;
import com.gmail.necnionch.myplugin.athletime.common.Util;
import com.google.common.collect.Maps;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityInteractEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Map;

import static com.gmail.necnionch.myplugin.athletime.bukkit.AthleTimePlugin.makeMessage;


public class PlayerListener implements Listener, ParkourPlayerAPI {
    private final AthleTimePlugin owner;
    private Listener_v1_9 v1_9Listener = null;
    private final ParkourContainer container;
    private final RecordContainer records;
    private final CitizensNPC citizens;

    private final Map<Player, ParkourPlayer> players = Maps.newHashMap();

    public PlayerListener(AthleTimePlugin owner, ParkourContainer container, RecordContainer records, CitizensNPC citizens) {
        this.owner = owner;
        this.container = container;
        this.records = records;
        this.citizens = citizens;
    }

    public Listener_v1_9 init_v1_9_listener() {
        if (v1_9Listener == null)
            v1_9Listener = new Listener_v1_9();
        return v1_9Listener;
    }

    // parkour

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onPress(EntityInteractEvent event) {
        Block block = event.getBlock();
        if (!AthleTimePlugin.isPressurePlate(block.getType()))
            return;

        ParkourPoint point = container.getByLocation(block.getWorld().getName(), block.getX(), block.getY(), block.getZ());
        if (point == null)
            return;

        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onPress(PlayerInteractEvent event) {
        if (!Action.PHYSICAL.equals(event.getAction()))
            return;

        Block block = event.getClickedBlock();
        if (block == null || !AthleTimePlugin.isPressurePlate(block.getType()))
            return;

        ParkourPoint point = container.getByLocation(block.getWorld().getName(), block.getX(), block.getY(), block.getZ());
        if (point == null)
            return;

        event.setCancelled(true);

        Player p = event.getPlayer();
        ParkourPlayer pPlayer = players.get(p);

        if (pPlayer == null) {
            if (ParkourPoint.Type.START.equals(point.getType())) {
                onStartPoint(p, point, event);
            }
        } else {
            if (pPlayer.getParkour().equals(point.getParkour())) {
                if (ParkourPoint.Type.CHECK.equals(point.getType()) && !point.equals(pPlayer.getLastPoint())) {
                    onCheckPoint(pPlayer, p, point, event);

                } else if (ParkourPoint.Type.END.equals(point.getType())) {
                    onEndPoint(pPlayer, p, point, event);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onTeleport(PlayerTeleportEvent event) {
        if (PlayerTeleportEvent.TeleportCause.COMMAND.equals(event.getCause()))
            return;

        ParkourPlayer parkourPlayer = players.get(event.getPlayer());
        if (parkourPlayer != null && event.getTo() != null && !event.getTo().equals(parkourPlayer.getSafeTeleportLocation())) {
            cancelParkour(parkourPlayer, true);
            event.setCancelled(true);
        }
    }

    // inventory items

    @EventHandler(priority = EventPriority.LOW)
    public void onRightClick(PlayerInteractEvent event) {
        if (!Util.isMainHand(event))
            return;
        switch (event.getAction()) {
            case RIGHT_CLICK_BLOCK:
            case RIGHT_CLICK_AIR: {
                break;
            }
            default:
                return;
        }

        ParkourPlayer pPlayer = players.get(event.getPlayer());
        ItemStack item = event.getItem();
        if (pPlayer == null || item == null || Material.AIR.equals(item.getType()))
            return;

        ParkourItem parkourItem = null;
        for (ParkourItem pItem : pPlayer.parkourItems()) {
            if (item.equals(pItem.getItemStack()))
                parkourItem = pItem;
        }
        if (parkourItem == null)
            return;

        event.setCancelled(true);
        parkourItem.getAction().call(pPlayer);
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onDrop(PlayerDropItemEvent event) {
        ParkourPlayer pPlayer = players.get(event.getPlayer());
        ItemStack item = event.getItemDrop().getItemStack();
        if (pPlayer == null || Material.AIR.equals(item.getType()))
            return;

        ParkourItem parkourItem = null;
        for (ParkourItem pItem : pPlayer.parkourItems()) {
            if (item.equals(pItem.getItemStack()))
                parkourItem = pItem;
        }
        if (parkourItem == null)
            return;

        event.setCancelled(true);
        parkourItem.getAction().call(pPlayer);
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onInvClick(InventoryClickEvent event) {  // TODO: cloneできてしまう
        if (!(event.getInventory().getHolder() instanceof Player))
            return;
        Player player = (Player) event.getInventory().getHolder();

        ParkourPlayer pPlayer = players.get(player);
        if (pPlayer == null)
            return;

        ItemStack currentItem = event.getCurrentItem();
        ItemStack cursor = event.getCursor();

        for (ParkourItem pItem : pPlayer.parkourItems()) {
            if ((currentItem != null && currentItem.equals(pItem.getItemStack()))
                    || (cursor != null && cursor.equals(pItem.getItemStack()))) {
//                event.setResult(Event.Result.DENY);
                event.setCancelled(true);
                return;
            }
        }
    }

    //

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        ParkourPlayer parkourPlayer = players.get(event.getPlayer());
        if (parkourPlayer != null)
            cancelParkour(parkourPlayer, true);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onDeath(PlayerDeathEvent event) {
        ParkourPlayer parkourPlayer = players.get(event.getEntity());
        if (parkourPlayer != null) {
            event.setKeepLevel(true);
            event.setKeepInventory(true);
            event.setDroppedExp(0);
            event.setDeathMessage(null);
            event.getDrops().clear();
            cancelParkour(parkourPlayer, false);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player))
            return;
        ParkourPlayer parkourPlayer = players.get(((Player) event.getEntity()));
        if (parkourPlayer != null && parkourPlayer.getPlayer().getHealth() - event.getFinalDamage() <= 0) {
            parkourPlayer.safeTeleport(parkourPlayer.getLastPoint());
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onVoid(PlayerMoveEvent event) {
        if (event.getTo() == null || 0 < event.getTo().getY())
            return;
        ParkourPlayer parkourPlayer = players.get(event.getPlayer());
        if (parkourPlayer != null) {
            parkourPlayer.safeTeleport(parkourPlayer.getLastPoint());
            parkourPlayer.getPlayer().playSound(parkourPlayer.getPlayer().getLocation(), LegacySounds.ENTITY_ENDERMAN_TELEPORT.getType(), .5f, 1.5f);
        }
    }

    private <T extends Event> boolean callEvent(T event) {
        owner.getServer().getPluginManager().callEvent(event);
        return event instanceof Cancellable && ((Cancellable) event).isCancelled();
    }

    //

    private void onStartPoint(Player player, ParkourPoint point, PlayerInteractEvent event) {
        startParkour(point.getParkour(), player, false, point, event);
    }

    private void onCheckPoint(ParkourPlayer pPlayer, Player player, ParkourPoint point, PlayerInteractEvent event) {
        if (callEvent(new PlayerParkourCheckPointEvent(pPlayer, point, event)))
            return;

        pPlayer.setLastPoint(point);
        player.playSound(player.getLocation(), LegacySounds.ENTITY_ITEM_PICKUP.getType(), 1, 1);
        player.spigot().sendMessage(makeMessage(ChatColor.YELLOW, "チェックポイント！"));
    }

    private void onEndPoint(ParkourPlayer pPlayer, Player player, ParkourPoint point, PlayerInteractEvent event) {
        if (callEvent(new PlayerParkourEndEvent(pPlayer, point, event)))
            return;

        player.playSound(player.getLocation(), LegacySounds.ENTITY_PLAYER_LEVELUP.getType(), 1, 2);
        stopParkour(pPlayer);

        Record record = new Record(player.getUniqueId(), player.getName(), pPlayer.getCurrentTime());
        boolean newRecord = records.put(pPlayer.getParkour().getName(), record);

        BaseComponent[] message;
        if (newRecord) {
            message = makeMessage(ChatColor.GOLD, "タイムは " + record.getFormattedTime() + " でした。新記録おめでとう！");
        } else {
            message = makeMessage(ChatColor.GOLD, record.getFormattedTime() + " でクリアしました！");
        }
        player.spigot().sendMessage(message);


        if (newRecord && citizens.updateNPC(pPlayer.getParkour()))
            container.put(pPlayer.getParkour());
    }

    //

    private void stopParkour(ParkourPlayer player) {
        if (players.remove(player.getPlayer()) != null) {
            player.removeParkourItems();
            player.restoreInventoryItems();
            player.getPlayer().removePotionEffect(PotionEffectType.NIGHT_VISION);
        }
    }

    @Override
    public ParkourPlayer get(Player player) {
        return players.get(player);
    }

    @Override
    public ParkourPlayer[] getAll() {
        return players.values().toArray(new ParkourPlayer[0]);
    }

    @Override
    public void cancelAll() {
        for (ParkourPlayer parkourPlayer : getAll()) {
            cancelParkour(parkourPlayer, true);
        }
    }

    @Override
    public void cancelParkour(ParkourPlayer player, boolean teleport) {
        if (players.containsKey(player.getPlayer())) {
            callEvent(new PlayerParkourCancelEvent(player));
            stopParkour(player);

            if (teleport)
                player.safeTeleport(player.getStartPoint(), 1.5);

            player.getPlayer().playSound(player.getPlayer().getLocation(), LegacySounds.ENTITY_ITEM_PICKUP.getType(), 1, 0);
            player.getPlayer().spigot().sendMessage(makeMessage(ChatColor.RED, "パルクールを中止します"));
        }
    }

    @Override
    public ParkourPlayer startParkour(Parkour parkour, Player player, boolean teleport, ParkourPoint startPoint, @Nullable PlayerInteractEvent baseEvent) {
        if (players.containsKey(player))
            throw new IllegalStateException("already started parkour");

        ParkourPlayer parkourPlayer = new ParkourPlayer(player, parkour, startPoint);
        parkourPlayer.parkourItems().addAll(Arrays.asList(ParkourPlayer.makeParkourItems(this)));


        if (callEvent(new PlayerParkourStartEvent(parkourPlayer, parkour, startPoint, baseEvent)))
            return null;

        if (teleport) {
            World world = Bukkit.getWorld(parkour.getWorldName());
            if (world == null)
                world = player.getWorld();

            Location loc = startPoint.getLocation().clone();
            loc.setWorld(world);
            player.teleport(loc);
        }
        parkourPlayer.storeInventoryItems(true);
        parkourPlayer.placeParkourItems();
        players.put(player, parkourPlayer);


        player.playSound(player.getLocation(), LegacySounds.ENTITY_EXPERIENCE_ORB_PICKUP.getType(), 1, 2);
        player.spigot().sendMessage(makeMessage(ChatColor.GOLD, "パルクールスタート！"));
        player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, Integer.MAX_VALUE, 0, false, false), true);

        return parkourPlayer;
    }


    public class Listener_v1_9 implements Listener {
        @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
        public void onSwap(PlayerSwapHandItemsEvent event) {
            ParkourPlayer pPlayer = players.get(event.getPlayer());
            if (pPlayer == null)
                return;

            ItemStack main = event.getMainHandItem();
            ItemStack off = event.getOffHandItem();

            for (ParkourItem pItem : pPlayer.parkourItems()) {
                if ((main != null && main.equals(pItem.getItemStack()))
                        || (off != null && off.equals(pItem.getItemStack()))) {
                    event.setCancelled(true);
                    return;
                }
            }
        }
    }

}
