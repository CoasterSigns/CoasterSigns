package dev.masp005.coastersigns;

import dev.masp005.coastersigns.signs.CSBaseSignAction;
import dev.masp005.coastersigns.signs.SignActionAttachment;
import dev.masp005.coastersigns.signs.SignActionTimedScript;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import javax.annotation.Nullable;
import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class CoasterSigns extends JavaPlugin {
    private final Map<String, Boolean> featureWatchCache = new HashMap<>();
    private List<CSBaseSignAction> signs;
    private FileConfiguration config;
    private int verbosity;
    private Logger logger;
    private List<String> featureWatch;

    //<editor-fold desc="Logging Methods" defaultstate="collapsed">
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
    //</editor-fold>
    public void onEnable() {
        logger = getLogger();

        if (!getDataFolder().exists() && getDataFolder().mkdir())
            logInfo("Created Config File Folder.", "startup");
        if (new File(getDataFolder(), "attachments").mkdir())
            logInfo("Created Attachment Config File Folder.", "startup");
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

        signs = new LinkedList<>();
        signs.add(new SignActionAttachment(this));
        signs.add(new SignActionTimedScript(this));

        new CSCommand(this);
    }
    public void onDisable() {
    }

    /**
     * Reads the YAML configuration at (dir)/(name).yml and returns it as a parsed YamlConfiguration
     *
     * @param dir  The name of the subdirectory from the plugin's config directory
     * @param name The name of the file, excluding the file extension
     * @return The parsed config as a YamlConfiguration, or null if it could not be found
     */
    @Nullable
    public YamlConfiguration readFile(@Nullable String dir, String name) {
        File file = new File(dir == null ? getDataFolder() : new File(getDataFolder(), dir), name + ".yml");
        if (!file.exists()) return null;
        return YamlConfiguration.loadConfiguration(file);
    }

    public List<CSBaseSignAction> getSigns() {
        return signs;
    }
}
