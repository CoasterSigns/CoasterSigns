package dev.masp005.coastersigns.rides;

import java.io.File;

import org.bukkit.configuration.file.YamlConfiguration;

public class Ride {
    private static final int NEWEST_FORMAT = 1;
    protected String name;

    public String getName() {
        return name;
    }

    public static Ride fromConfig(YamlConfiguration config, File source) throws IllegalArgumentException {
        Ride ride = new Ride();
        if (config.getInt("format") != NEWEST_FORMAT)
            config = upgradeConfiguration(config, source);

        return ride;
    }

    private static YamlConfiguration upgradeConfiguration(YamlConfiguration old, File file)
            throws IllegalArgumentException {
        return upgradeConfiguration(old, file, NEWEST_FORMAT);
    }

    private static YamlConfiguration upgradeConfiguration(YamlConfiguration old, File file, int target)
            throws IllegalArgumentException {
        int oldFormat = old.getInt("format");
        if (target > NEWEST_FORMAT || target <= 0)
            throw new IllegalArgumentException("invalid target");
        if (oldFormat >= target)
            return old;
        while (oldFormat < target - 1) {
            old = upgradeConfiguration(old, file, oldFormat + 1);
            oldFormat++;
        }
        // do operations, which... well.
        return old;
    }
}
