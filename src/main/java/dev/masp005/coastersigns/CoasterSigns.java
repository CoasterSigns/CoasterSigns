package dev.masp005.coastersigns;

import dev.masp005.coastersigns.rides.Ride;
import dev.masp005.coastersigns.rides.RideManager;
import dev.masp005.coastersigns.signs.CSBaseSignAction;
import dev.masp005.coastersigns.signs.SignActionAttachment;
import dev.masp005.coastersigns.signs.SignActionTimedScript;
import dev.masp005.coastersigns.util.InteractiveInventory;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;

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
    protected List<CSBaseSignAction> signs;
    protected RideManager rideManager;
    private FileConfiguration config;

    /**
     * Level of logging verbosity.
     * 1: only fatal events
     * 2: Errors and fatalities
     * 3: Warnings and worse.
     * 4: Everything.
     */
    private int verbosity;
    private Logger logger;
    private List<String> featureWatch;

    public String baseDocURL;
    public BaseComponent noPermsMessage;

    {
        // TODO: add permission documentation and add a link here.
        noPermsMessage = new ComponentBuilder("No Permissions. ").bold(true).color(ChatColor.GOLD)
                .append("You do not have permission to perform this action.").reset().color(ChatColor.RED)
                .append("\nIf you belive this is a mistake, please contact a server administrator or check your permissions.")
                .color(ChatColor.GRAY).italic(true).build();
    }

    // <editor-fold desc="Logging Methods" defaultstate="collapsed">

    /**
     * Checks if a specific feature String (accounting for any level of detail) is
     * watched by config.yml
     * 
     * @param feature A feature String of the format
     *                feature.subfeature.functionality
     * @return true if it is and should always be logged, false if it is not and
     *         should only be watched if the event severity matches the verbosity
     *         determined in config.yml
     */
    private boolean checkFeatureWatch(String feature) {
        if (featureWatchCache.containsKey(feature))
            return featureWatchCache.get(feature);
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

    // </editor-fold>

    public void onEnable() {
        logger = getLogger();

        if (!getDataFolder().exists() && getDataFolder().mkdir())
            logInfo("Created Config File Folder.", "setup");
        if (new File(getDataFolder(), "attachments").mkdir())
            logInfo("Created Attachment Config File Folder.", "setup");
        saveDefaultConfig();
        config = getConfig();

        baseDocURL = getDescription().getWebsite();

        String verbosityStr = config.getString("verbosity");
        if (verbosityStr == null)
            verbosity = 3; // This should usually never be reached but just in case
        else {
            verbosityStr = verbosityStr.toLowerCase();
            verbosity = verbosityStr.equals("fatal") ? 1
                    : verbosityStr.equals("error") ? 2 : verbosityStr.equals("all") ? 4 : 3;
        }
        featureWatch = config.getStringList("watchFeatures");

        signs = new LinkedList<>();
        signs.add(new SignActionAttachment(this));
        signs.add(new SignActionTimedScript(this));

        new CSCommand(this);
        Ride.plugin = this;
        rideManager = new RideManager(this);

        InteractiveInventory.handler = new InteractiveInventory.InteractiveInventoryHandler(this);
    }

    public void onDisable() {
    }

    /**
     * Reads the YAML configuration at (dir)/(name).yml and returns it as a parsed
     * YamlConfiguration
     *
     * @param dir  The name of the subdirectory from the plugin's config directory
     *             (optional)
     * @param name The name of the file, excluding the file extension
     * @return The parsed config as a YamlConfiguration, or null if it could not be
     *         found
     */
    @Nullable
    public YamlConfiguration readConfig(@Nullable String dir, String name) {
        File directory = dir == null ? getDataFolder() : new File(getDataFolder(), dir);
        File file = new File(directory, name + ".yml");
        if (!file.exists())
            return null;
        return YamlConfiguration.loadConfiguration(file);
    }
}
