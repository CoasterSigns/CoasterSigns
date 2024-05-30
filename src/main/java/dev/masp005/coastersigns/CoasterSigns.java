package dev.masp005.coastersigns;

import org.bukkit.plugin.java.JavaPlugin;

public final class CoasterSigns extends JavaPlugin {

    @Override
    public void onEnable() {
        if (!getDataFolder().exists() && getDataFolder().mkdir())
            getLogger().info("Created Config File Folder.");
    }

    @Override
    public void onDisable() {
    }
}
