package dev.masp005.coastersigns;

import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.Map;

public class Util {
    public static YamlConfiguration makeConfig(Map<?, ?> values) {
        YamlConfiguration config = new YamlConfiguration();
        for (Map.Entry<?, ?> entry : values.entrySet()) {
            config.set((String) entry.getKey(), entry.getValue());
        }
        return config;
    }
}
