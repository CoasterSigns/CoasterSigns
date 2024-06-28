package dev.masp005.coastersigns.rides;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.ConversationFactory;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.StringPrompt;
import org.bukkit.entity.Player;

import dev.masp005.coastersigns.CoasterSigns;
import dev.masp005.coastersigns.util.InteractiveInventory;
import dev.masp005.coastersigns.util.Util;

// CONSIDER: implement Serializable
public class Ride {
    public static CoasterSigns plugin;
    private static final int NEWEST_FORMAT = 1;
    private static final String debugName = "rideMngr";
    protected String name;
    protected File file;

    public Ride(File source) throws IOException {
        file = source;
        name = Util.removeFileExtension(source.getName());
        if (file.exists())
            fromConfig();
    }

    /**
     * Saves the ride.
     */
    public void save() {
        try {
            YamlConfiguration config = new YamlConfiguration();
            config.set("format", NEWEST_FORMAT);
            config.set("name", name);

            file.createNewFile(); // silently fails if file already exists
            FileWriter writer = new FileWriter(file, false);
            writer.write(config.saveToString());
            writer.close();
        } catch (IOException e) {
            plugin.logError("Renaming failed", debugName + ".io");
        }
    }

    /**
     * Gets the ride's display name.
     * 
     * @return The ride's display name.
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the ride's display name.
     * 
     * @param name The ride's new display name.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Opens a modification GUI for the specified player.
     * 
     * @param player The player the GUI should be opened for.
     */
    public void modifyMenu(Player player) {
        new InteractiveInventory(6)
                .setItem(0, Material.NAME_TAG).setUniversalListener(event -> {
                    event.setCancelled(true);
                    player.sendMessage("hi");
                    Bukkit.getScheduler().runTask(plugin, () -> {
                        player.closeInventory();
                        new ConversationFactory(plugin).withEscapeSequence("-").withFirstPrompt(new StringPrompt() {
                            public String getPromptText(ConversationContext context) {
                                return String.format("§3Enter the new name of ride §b%s§3:", getName());
                            }

                            public Prompt acceptInput(ConversationContext context, String input) {
                                setName(input);
                                save();
                                player.sendMessage("§3Saved new name.");
                                modifyMenu(player);
                                return END_OF_CONVERSATION;
                            }
                        }).buildConversation(player).begin();
                    });
                }).finish()
                .open(player, getName());
    }

    /**
     * Loads this ride from its dedicated config file.
     * 
     * @throws IllegalArgumentException If the config is malformed.
     */
    private void fromConfig() throws IllegalArgumentException {
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        if (config.getInt("format") < NEWEST_FORMAT)
            config = upgradeConfiguration(file);
        name = config.getString("name");
    }

    /**
     * Upgrades a config file.
     * 
     * @param file The file that should be updated.
     * @return The upgraded config file.
     * @throws IllegalArgumentException If the config is malformed.
     */
    private static YamlConfiguration upgradeConfiguration(File file)
            throws IllegalArgumentException {
        return upgradeConfiguration(file, NEWEST_FORMAT);
    }

    /**
     * Upgrades a config file.
     * 
     * @param file   The file that should be updated.
     * @param target The target version it should be updated to.
     * @return The upgraded config file.
     * @throws IllegalArgumentException If the config is malformed or an invalid
     *                                  {@code target} was supplied.
     */
    private static YamlConfiguration upgradeConfiguration(File file, int target)
            throws IllegalArgumentException {
        YamlConfiguration old = YamlConfiguration.loadConfiguration(file);
        int oldFormat = old.getInt("format");

        // invalid version
        if (target > NEWEST_FORMAT || target <= 0)
            throw new IllegalArgumentException("invalid target");

        // No need to upgrade.
        if (oldFormat >= target)
            return old;

        // Upgrade format to format, one at a time.
        while (oldFormat < target - 1) {
            old = upgradeConfiguration(file, oldFormat + 1);
            oldFormat++;
        }

        // do operations, which... well.
        return old;
    }
}
