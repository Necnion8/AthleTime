package com.gmail.necnionch.myplugin.athletime.bukkit.record;

import com.gmail.necnionch.myplugin.athletime.common.BukkitConfigDriver;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class RecordContainer extends BukkitConfigDriver {
    private final Multimap<String, Record> records = ArrayListMultimap.create();

    public RecordContainer(JavaPlugin plugin) {
        super(plugin, "records.yml", "empty.yml");
    }


    @Override
    public boolean onLoaded(FileConfiguration config) {
        if (super.onLoaded(config)) {
            records.clear();

            for (String parkourName : config.getKeys(false)) {
                for (Map<?, ?> map : config.getMapList(parkourName)) {
                    try {
                        records.put(parkourName, Record.fromConfig(map));
                    } catch (NullPointerException | ClassCastException ignored) {
                    }
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean save() {
        FileConfiguration config = new YamlConfiguration();
        for (Map.Entry<String, Collection<Record>> e : records.asMap().entrySet()) {
            config.set(e.getKey(), e.getValue().stream().map(Record::toConfig).collect(Collectors.toList()));
        }
        this.config = config;
        return super.save();
    }

    public boolean put(String parkourName, Record record) {
        Collection<Record> records = this.records.get(parkourName);

        if (records.stream().anyMatch(r -> record.getPlayer().equals(r.getPlayer()) && r.getTime() <= record.getTime()))
            return false;

        this.records.get(parkourName).removeIf(r -> record.getPlayer().equals(r.getPlayer()));
        this.records.put(parkourName, record);
        save();
        return true;
    }

    public void removeAll(String parkourName) {
        records.removeAll(parkourName);
        save();
    }


    public List<Record> getSortedRecords(String parkourName) {
        List<Record> records = Lists.newArrayList(this.records.get(parkourName));
        records.sort(Comparator.comparingDouble(Record::getTime));
        return records;
    }



}
