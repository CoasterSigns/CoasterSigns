package dev.masp005.coastersigns.rides;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.bukkit.configuration.file.YamlConfiguration;

import dev.masp005.coastersigns.Util;

// CONSIDER: implement Serializable
public class Ride {
    private static final int NEWEST_FORMAT = 1;
    protected String name;
    private File file;

    public Ride(File source) throws IOException {
        file = source;
        name = Util.removeFileExtension(source.getName());
        if (file.exists())
            fromConfig();
    }

    public void save() throws IOException {
        YamlConfiguration config = new YamlConfiguration();
        if (file.exists() && !file.delete())
            throw new IOException();
        file.createNewFile();
        config.set("format", NEWEST_FORMAT);
        config.set("name", name);
        FileWriter writer = new FileWriter(file);
        writer.write(config.saveToString());
        writer.close();
    }

    public String getName() {
        return name;
    }

    private void fromConfig() throws IllegalArgumentException {
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        if (config.getInt("format") < NEWEST_FORMAT)
            config = upgradeConfiguration(file);
    }

    private static YamlConfiguration upgradeConfiguration(File file)
            throws IllegalArgumentException {
        return upgradeConfiguration(file, NEWEST_FORMAT);
    }

    private static YamlConfiguration upgradeConfiguration(File file, int target)
            throws IllegalArgumentException {
        YamlConfiguration old = YamlConfiguration.loadConfiguration(file);
        int oldFormat = old.getInt("format");
        if (target > NEWEST_FORMAT || target <= 0)
            throw new IllegalArgumentException("invalid target");
        if (oldFormat >= target)
            return old;
        while (oldFormat < target - 1) {
            old = upgradeConfiguration(file, oldFormat + 1);
            oldFormat++;
        }
        // do operations, which... well.
        return old;
    }
}
