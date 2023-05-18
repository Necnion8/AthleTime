package com.gmail.necnionch.myplugin.athletime.common;

import org.bukkit.Material;

public enum LegacyItemTypes {
    GOLD_PLATE("LIGHT_WEIGHTED_PRESSURE_PLATE", "GOLD_PLATE"),
    OAK_DOOR("OAK_DOOR", "WOOD_DOOR"),
    RED_BED("RED_BED", "BED");

    private final String[] names;
    private final Material type;

    LegacyItemTypes(String... names) {
        this.names = names;

        Material type = null;
        for (String name : names) {
            try {
                type = Material.valueOf(name);
            } catch (IllegalArgumentException ignored) {
            }
        }

        if (type == null)
            throw new IllegalArgumentException("Unknown item type: " + name());
        this.type = type;
    }

    public String[] getNames() {
        return names;
    }

    public Material getType() {
        return type;
    }

}
