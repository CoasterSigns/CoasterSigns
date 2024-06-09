package dev.masp005.coastersigns;

import org.bukkit.block.Block;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.Map;

public class Util {
    /**
     * Turns a map of values into a YamlConfiguration
     *
     * @param values A map of values.
     * @return The generated YamlConfiguration.
     */
    public static YamlConfiguration makeConfig(Map<?, ?> values) {
        YamlConfiguration config = new YamlConfiguration();
        for (Map.Entry<?, ?> entry : values.entrySet()) {
            config.set((String) entry.getKey(), entry.getValue());
        }
        return config;
    }

    /**
     * Turns a block into human-readable coordinates
     *
     * @param block The block.
     * @return Coordinates in the format "(X, Y, Z)".
     */
    public static String blockCoordinates(Block block) {
        return "(" +
                block.getX() + ", " +
                block.getY() + ", " +
                block.getZ() + ")";
    }
}
