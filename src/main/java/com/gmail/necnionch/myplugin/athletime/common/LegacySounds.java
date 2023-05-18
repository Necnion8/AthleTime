package com.gmail.necnionch.myplugin.athletime.common;

import org.bukkit.Sound;

public enum LegacySounds {
    ENTITY_ARROW_HIT_PLAYER("ENTITY_ARROW_HIT_PLAYER", "ARROW_HIT"),
    ENTITY_PLAYER_LEVELUP("ENTITY_PLAYER_LEVELUP", "LEVEL_UP"),
    ENTITY_EXPERIENCE_ORB_PICKUP("ENTITY_EXPERIENCE_ORB_PICKUP", "ORB_PICKUP"),
    ENTITY_ENDERMAN_TELEPORT("ENTITY_ENDERMAN_TELEPORT", "ENDERMAN_TELEPORT"),
    ENTITY_ITEM_PICKUP("ENTITY_ITEM_PICKUP", "ITEM_PICKUP");

    private final String[] names;
    private final Sound type;

    LegacySounds(String... names) {
        this.names = names;

        Sound type = null;
        for (String name : names) {
            try {
                type = Sound.valueOf(name);
            } catch (IllegalArgumentException ignored) {
            }
        }

        if (type == null)
            throw new IllegalArgumentException("Unknown sound type: " + name());
        this.type = type;
    }

    public String[] getNames() {
        return names;
    }

    public Sound getType() {
        return type;
    }

}
