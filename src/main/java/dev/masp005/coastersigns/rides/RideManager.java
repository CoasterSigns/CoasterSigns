package dev.masp005.coastersigns.rides;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.bukkit.entity.Player;

import dev.masp005.coastersigns.CoasterSigns;
import dev.masp005.coastersigns.util.Util;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;

public class RideManager {
    private static final String DIRECTORY_NAME = "rides";
    private static final String debugName = "rideMngr";
    // subfeatures: read, io
    private static String helpLink = "ridemanager.html";
    private final CoasterSigns plugin;
    private final Map<String, Ride> rides = new HashMap<>();

    public RideManager(CoasterSigns plugin) {
        this.plugin = plugin;
        File rideDir = new File(plugin.getDataFolder(), DIRECTORY_NAME);
        if (!rideDir.exists() && rideDir.mkdir())
            plugin.logInfo("Created Ride Folder.", "setup");
        else {
            for (String file : rideDir.list()) {
                try {
                    rides.putIfAbsent(
                            Util.removeFileExtension(file),
                            new Ride(new File(rideDir, file)));
                } catch (Exception err) {
                    plugin.logError(String.format("Error reading Ride %s: %s", file, err.getMessage()),
                            debugName + ".read");
                }
            }
        }
        plugin.logInfo("Ride manager initialized.", "setup");
    }

    /**
     * Gets a ride.
     * 
     * @param id The ID of the ride to get.
     * @return The ride associated with the ID or {@code null} if it does not exist.
     */
    public Ride getRide(String id) {
        return rides.get(id);
    }

    /**
     * Creates a ride.
     * 
     * @param id   The ID this ride should be give.
     * @param name The display name of the ride.
     * @return Whether the creation was successful.
     */
    public boolean createRide(String id, String name) {
        try {
            Ride ride = new Ride(
                    new File(new File(plugin.getDataFolder(), DIRECTORY_NAME), id.toLowerCase() + ".yml"));
            ride.name = name;
            rides.put(id, ride);
            ride.save();
            return true;
        } catch (IOException err) {
            plugin.logError(err.getMessage(), debugName + ".io");
            return false;
        }
    }

    /**
     * Lists ride IDs.
     * 
     * @return A list of all existing ride IDs.
     */
    public List<String> listRides() {
        List<String> list = new LinkedList<>();
        for (String key : rides.keySet()) {
            list.add(key);
        }
        return list;
    }

    /**
     * Changes the ID of a ride.
     * 
     * @param from The ID of the ride as it is currently stored.
     * @param to   The ID the ride should be referred to in the future.
     * @return Whether the change was successful.
     */
    public boolean changeId(String from, String to) {
        Ride ride = rides.get(from);
        if (ride == null)
            return false;
        ride.file.delete();
        ride.file = new File(new File(plugin.getDataFolder(), DIRECTORY_NAME), to.toLowerCase() + ".yml");
        ride.save();
        rides.put(to, ride);
        rides.remove(from);
        return true;
    }

    public boolean modifyMenu(Player player, String ride) {
        Ride rideObj = getRide(ride);
        if (rideObj == null)
            return false;
        rideObj.modifyMenu(player);
        return true;
    }

    /**
     * Creates the message that should be displayed for {@code /coastersigns rides}
     * 
     * @return The message.
     */
    public BaseComponent[] overviewMessage() {
        // TODO: Ride list
        ComponentBuilder builder = new ComponentBuilder();
        boolean first = true;
        for (String ride : listRides()) {
            if (!first)
                builder.append(", ").reset();
            builder.append(ride).color(ChatColor.GREEN);
            first = false;
        }
        builder.append("\nHelp").color(ChatColor.GOLD).bold(true)
                .event(new ClickEvent(ClickEvent.Action.OPEN_URL, plugin.baseDocURL + helpLink));
        return builder.create();
    }
}
