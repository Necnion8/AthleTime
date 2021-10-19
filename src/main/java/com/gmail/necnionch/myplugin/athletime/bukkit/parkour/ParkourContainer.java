package com.gmail.necnionch.myplugin.athletime.bukkit.parkour;

import com.gmail.necnionch.myplugin.athletime.common.BukkitConfigDriver;
import com.google.common.collect.Maps;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Map;


public class ParkourContainer extends BukkitConfigDriver {
    private final Map<String, Parkour> parkours = Maps.newHashMap();
    private final Map<String, ParkourPoint> cachedLocations = Maps.newHashMap();

    public ParkourContainer(JavaPlugin plugin) {
        super(plugin, "parkours.yml", "empty.yml");
    }


    @Override
    public boolean onLoaded(FileConfiguration config) {
        if (super.onLoaded(config)) {
            parkours.clear();
            cachedLocations.clear();

            for (String name : config.getKeys(false)) {
                ConfigurationSection section = config.getConfigurationSection(name);
                if (section == null)
                    continue;

                try {
                    Parkour parkour = Parkour.fromConfig(name, section);

                    if (parkour.getWorldName() == null)
                        continue;

                    parkours.put(name, parkour);
                    putLocationCache(parkour);

                } catch (Throwable e) {
                    getLogger().warning("Failed to load: " + name);
                    e.printStackTrace();
                }
            }

            return true;
        }
        return false;
    }




    public void put(Parkour parkour) {
        if (parkours.containsKey(parkour.getName())) {
            removeLocationCache(parkours.get(parkour.getName()));
        }

        parkours.put(parkour.getName(), parkour);
        config.set(parkour.getName(), parkour.toConfig());
        save();
        putLocationCache(parkour);
    }

    public Parkour remove(String name) {
        Parkour parkour = parkours.remove(name);
        config.set(name, null);
        save();

        if (parkour != null)
            removeLocationCache(parkour);

        return parkour;
    }

    public Parkour get(String name) {
        return parkours.get(name);
    }

    public Parkour[] getAll() {
        return parkours.values().toArray(new Parkour[0]);
    }

    public void saveAll() {
        parkours.values().forEach(p -> config.set(p.getName(), p.toConfig()));
        save();
    }


    public ParkourPoint getByLocation(String world, int x, int y, int z) {
        return cachedLocations.get(x + "," + y + "," + z + "," + world);
    }

    private void putLocationCache(Parkour parkour) {
        cachedLocations.putAll(parkour.getPoints());
    }

    private void removeLocationCache(Parkour parkour) {
        cachedLocations.entrySet()
                .removeIf(p -> parkour.equals(p.getValue().getParkour()));
    }

}
