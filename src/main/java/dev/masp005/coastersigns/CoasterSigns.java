package dev.masp005.coastersigns;

import com.bergerkiller.bukkit.tc.signactions.SignAction;
import dev.masp005.coastersigns.signs.SignActionAttachment;
import dev.masp005.coastersigns.signs.SignActionTimedScript;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public final class CoasterSigns extends JavaPlugin {

    @Override
    public void onEnable() {
        if (!getDataFolder().exists() && getDataFolder().mkdir())
            getLogger().info("Created Config File Folder.");

        SignAction.register(new SignActionAttachment(this));
        getLogger().info("TrainCarts Attachment Switcher Sign has been registered.");
        SignAction.register(new SignActionTimedScript(this));
        getLogger().info("TrainCarts TimedScripts Executor has been registered.");
    }

    @Override
    public void onDisable() {
    }

    public YamlConfiguration readFile(String name) {
        File file = new File(getDataFolder(), name + ".yml");
        if (!file.exists()) return null;
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        if (!config.isSet("modifications")) return null;
        return config;
    }
}
