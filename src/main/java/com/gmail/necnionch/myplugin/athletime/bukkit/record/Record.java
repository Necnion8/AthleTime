package com.gmail.necnionch.myplugin.athletime.bukkit.record;

import com.google.common.collect.Maps;
import org.bukkit.configuration.ConfigurationSection;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class Record {

    private final UUID player;
    private final String playerName;
    private final long time;

    public Record(UUID player, String playerName, long time) {
        this.player = player;
        this.playerName = playerName;
        this.time = time;
    }

    public UUID getPlayer() {
        return player;
    }

    public String getPlayerName() {
        return playerName;
    }

    public long getTime() {
        return time;
    }

    public String getFormattedTime() {
        long minutes = TimeUnit.MILLISECONDS.toMinutes(time);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(time - TimeUnit.MINUTES.toMillis(minutes));
        long millis = time - TimeUnit.MINUTES.toMillis(minutes) - TimeUnit.SECONDS.toMillis(seconds);

        return String.format("%02d:%02d.%02d", minutes, seconds, Math.round(millis / 10f));
    }


    public static Record fromConfig(ConfigurationSection config) {
        return new Record(
                UUID.fromString(Objects.requireNonNull(config.getString("player-uuid"))),
                config.getString("player-name", ""),
                config.getLong("time", -1)
        );
    }

    public static Record fromConfig(Map<?, ?> config) {
        Object tmp = Objects.requireNonNull(config.get("time"));
        long time = (tmp instanceof Integer) ? (Integer) tmp : (Long) tmp;

        return new Record(
                UUID.fromString((String) Objects.requireNonNull(config.get("player-uuid"))),
                        (String) Objects.requireNonNull(config.get("player-name")),
                        time
        );
    }

    public Map<String, Object> toConfig() {
        Map<String, Object> config = Maps.newHashMap();

        config.put("player-uuid", player.toString());
        config.put("player-name", playerName);
        config.put("time", time);

        return config;
    }

}
