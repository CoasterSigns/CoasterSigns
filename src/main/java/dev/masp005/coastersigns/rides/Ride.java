package dev.masp005.coastersigns.rides;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.bukkit.configuration.file.YamlConfiguration;

import dev.masp005.coastersigns.util.Util;

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
        config.set("format", NEWEST_FORMAT);
        config.set("name", name);

        file.createNewFile(); // silently fails if file already exists
        FileWriter writer = new FileWriter(file, false);
        writer.write(config.saveToString());
        writer.close();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    private void fromConfig() throws IllegalArgumentException {
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        if (config.getInt("format") < NEWEST_FORMAT)
            config = upgradeConfiguration(file);
        name = config.getString("name");
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
