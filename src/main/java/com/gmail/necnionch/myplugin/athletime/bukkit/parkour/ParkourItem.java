package com.gmail.necnionch.myplugin.athletime.bukkit.parkour;

import org.bukkit.inventory.ItemStack;


public class ParkourItem {
    private final int slotId;
    private final ItemStack itemStack;
    private final ItemAction action;
    private boolean focus;

    public ParkourItem(int slotId, ItemStack itemStack, ItemAction action) {
        this.slotId = slotId;
        this.itemStack = itemStack;
        this.action = action;
    }

    public ParkourItem focus(boolean focus) {
        if (!(0 <= slotId && slotId < 9))
            throw new IllegalArgumentException("not equals : 0 <= slotId < 0");
        this.focus = focus;
        return this;
    }

    public boolean isFocus() {
        return focus;
    }


    public int getSlotId() {
        return slotId;
    }

    public ItemStack getItemStack() {
        return itemStack;
    }

    public ItemAction getAction() {
        return action;
    }


    public interface ItemAction {
        void call(ParkourPlayer parkourPlayer);
    }

}
