package com.gmail.necnionch.myplugin.athletime.common;

import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

public class Util {
    private static boolean legacy1_8;

    static {
        try {
            PlayerInteractEvent.class.getMethod("getHand");
            legacy1_8 = false;
        } catch (NoSuchMethodException e) {
            legacy1_8 = true;
        }

    }


    public static boolean isLegacy_v1_8() {
        return legacy1_8;
    }

    public static boolean isMainHand(PlayerInteractEvent event) {
        return legacy1_8 || EquipmentSlot.HAND.equals(event.getHand());
    }


}
