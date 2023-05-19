package com.gmail.necnionch.myplugin.athletime.bukkit.parkour;

import com.gmail.necnionch.myplugin.athletime.bukkit.ParkourPlayerAPI;
import com.gmail.necnionch.myplugin.athletime.common.LegacyItemTypes;
import com.gmail.necnionch.myplugin.athletime.common.LegacySounds;
import com.google.common.collect.Lists;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;


public class ParkourPlayer {
    private final long startTime = System.currentTimeMillis();
    private final Player player;
    private final Parkour parkour;
    private final ParkourPoint startPoint;
    private ParkourPoint lastPoint;

    private final List<ParkourItem> parkourItems = Lists.newArrayList();

    private int inventoryCursor;
    private ItemStack[] inventoryItems;
    private Collection<PotionEffect> activePotionEffects;

    private Location safeTeleportLocation;
    private List<PotionEffect> potionEffects;


    public ParkourPlayer(Player player, Parkour parkour, ParkourPoint first) {
        this.player = player;
        this.parkour = parkour;
        lastPoint = first;
        startPoint = first;
    }

    public Player getPlayer() {
        return player;
    }

    public Parkour getParkour() {
        return parkour;
    }

    public ParkourPoint getStartPoint() {
        return startPoint;
    }

    public ParkourPoint getLastPoint() {
        return lastPoint;
    }

    public void setLastPoint(ParkourPoint lastPoint) {
        this.lastPoint = lastPoint;
    }

    public long getStartTime() {
        return startTime;
    }

    public long getCurrentTime() {
        return System.currentTimeMillis() - startTime;
    }


    public List<ParkourItem> parkourItems() {
        return parkourItems;
    }

    public void placeParkourItems() {
        PlayerInventory inv = player.getInventory();
        for (ParkourItem pItem : parkourItems) {
            inv.setItem(pItem.getSlotId(), pItem.getItemStack());
            if (pItem.isFocus() && 0 <= pItem.getSlotId() && pItem.getSlotId() < 9)
                player.getInventory().setHeldItemSlot(pItem.getSlotId());
        }
    }

    public void removeParkourItems() {
        PlayerInventory inv = player.getInventory();
        for (ParkourItem pItem : parkourItems) {
            inv.remove(pItem.getItemStack());
        }
    }


    public void storeInventoryItems(boolean clear) {
        inventoryCursor = player.getInventory().getHeldItemSlot();
        inventoryItems = player.getInventory().getContents();
        activePotionEffects = player.getActivePotionEffects();
        if (clear) {
            player.getInventory().clear();
            List<PotionEffectType> effects = activePotionEffects.stream()
                    .map(PotionEffect::getType)
                    .collect(Collectors.toList());
            effects.forEach(player::removePotionEffect);
        }
    }

    public void restoreInventoryItems() {
        if (inventoryItems != null) {
            player.getInventory().clear();
            player.getInventory().setContents(inventoryItems);
            player.getInventory().setHeldItemSlot(inventoryCursor);
            inventoryItems = null;
        }
        if (activePotionEffects != null) {
            List<PotionEffectType> effects = activePotionEffects.stream()
                    .map(PotionEffect::getType)
                    .collect(Collectors.toList());
            effects.forEach(player::removePotionEffect);
            player.addPotionEffects(activePotionEffects);
        }
    }


    public void startPotionEffect() {
        potionEffects = player.getActivePotionEffects().stream()
                .map(pot -> new PotionEffect(pot.serialize()))
                .collect(Collectors.toList());

        player.getActivePotionEffects().stream()
                .map(PotionEffect::getType)
                .forEach(player::removePotionEffect);
        player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, Integer.MAX_VALUE, 0, false, false), true);
    }

    public void stopPotionEffect() {
        player.removePotionEffect(PotionEffectType.NIGHT_VISION);
        if (potionEffects != null) {
            potionEffects.forEach(pot -> player.addPotionEffect(pot, true));
            potionEffects = null;
        }
    }


    public void safeTeleport(ParkourPoint point) {
        safeTeleport(point, 1.5);
    }

    public void safeTeleport(ParkourPoint point, double backSize) {
        World world = Bukkit.getWorld(point.getParkour().getWorldName());
        if (world == null)
            world = player.getWorld();

        Location loc = point.getLocation().clone().add(0.5, 0, 0.5);
        loc.setWorld(world);

        float yaw = loc.getYaw();
        if (yaw < 0.0F)
            yaw += 360.0F;
        yaw %= 360.0F;
        int i = (int)((yaw + 45.0F) / 90.0F);
        if (i == 1) {
            loc.add(backSize, 0, 0);
        } else if (i == 2) {
            loc.add(0, 0, backSize);
        } else if (i == 3) {
            loc.add(-backSize, 0, 0);
        } else {
            loc.add(0, 0, -backSize);
        }

        safeTeleportLocation = loc;
        player.teleport(loc);
    }

    public Location getSafeTeleportLocation() {
        return safeTeleportLocation;
    }

    public static ParkourItem[] makeParkourItems(ParkourPlayerAPI players) {
        return new ParkourItem[] {
                new ParkourItem(3, makeItem(LegacyItemTypes.GOLD_PLATE.getType(), ChatColor.GREEN + "チェックポイントへ戻る"), (pPlayer) -> {
                    pPlayer.safeTeleport(pPlayer.lastPoint, 1.5);
                    pPlayer.player.playSound(pPlayer.getPlayer().getLocation(), LegacySounds.ENTITY_ENDERMAN_TELEPORT.getType(), .5f, 1.5f);
                }).focus(true),
                new ParkourItem(4, makeItem(LegacyItemTypes.OAK_DOOR.getType(), ChatColor.GOLD + "スタート地点へ戻る"), (pPlayer) -> {
                    pPlayer.safeTeleport(pPlayer.startPoint, 1.5);
                    pPlayer.player.playSound(pPlayer.getPlayer().getLocation(), LegacySounds.ENTITY_ENDERMAN_TELEPORT.getType(), .5f, 1);
                }),
                new ParkourItem(5, makeItem(LegacyItemTypes.RED_BED.getType(), ChatColor.RED + "キャンセル"), (pPlayer) -> {
                    players.cancelParkour(pPlayer, true);
                    pPlayer.player.playSound(pPlayer.getPlayer().getLocation(), LegacySounds.ENTITY_ARROW_HIT_PLAYER.getType(), 1, 2);
                })
        };
    }

    private static ItemStack makeItem(Material material, String name) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        item.setItemMeta(meta);
        return item;
    }

}
