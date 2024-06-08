package dev.masp005.coastersigns;

import com.bergerkiller.bukkit.tc.signactions.SignAction;
import dev.masp005.coastersigns.signs.SignActionAttachment;
import dev.masp005.coastersigns.signs.SignActionTimedScript;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class CoasterSigns extends JavaPlugin {
    private FileConfiguration config;
    private int verbosity;
    private Logger logger;
    private List<String> featureWatch;
    private Map<String, Boolean> featureWatchCache = new HashMap<>();

    private boolean checkFeatureWatch(String feature) {
        if (featureWatchCache.containsKey(feature)) return featureWatchCache.get(feature);
        boolean result = false;
        for (String watchedFeature : featureWatch) {
            if (feature.startsWith(watchedFeature) && // targets same (sub)feature
                    (feature.length() == watchedFeature.length() || // same level of detail
                            feature.charAt(watchedFeature.length()) == '.') // at subfeature level detail
            ) {
                result = true;
                break;
            }
        }
        featureWatchCache.put(feature, result);
        return result;
    }

    public void logFatal(String message, String feature) {
        logger.log(Level.SEVERE, feature + ": " + message);
    }

    public void logError(String message, String feature) {
        if (verbosity >= 2 || checkFeatureWatch(feature))
            logger.log(Level.SEVERE, feature + ": " + message);
    }

    public void logWarn(String message, String feature) {
        if (verbosity >= 3 || checkFeatureWatch(feature))
            logger.warning(feature + ": " + message);
    }

    public void logInfo(String message, String feature) {
        if (verbosity >= 4 || checkFeatureWatch(feature))
            logger.info(feature + ": " + message);
    }

    @Override
    public void onEnable() {
        if (!getDataFolder().exists() && getDataFolder().mkdir())
            logger.info("Created Config File Folder.");

        saveDefaultConfig();
        config = getConfig();
        String verbosityStr = config.getString("verbosity");
        if (verbosityStr == null) verbosity = 4;
        else {
            verbosityStr = verbosityStr.toLowerCase();
            verbosity = verbosityStr.equals("fatal") ? 1 :
                    verbosityStr.equals("error") ? 2 :
                            verbosityStr.equals("all") ? 4 : 3;
        }
        featureWatch = config.getStringList("watchFeatures");
        logger = getLogger();

        SignAction.register(new SignActionAttachment(this));
        SignAction.register(new SignActionTimedScript(this));
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
