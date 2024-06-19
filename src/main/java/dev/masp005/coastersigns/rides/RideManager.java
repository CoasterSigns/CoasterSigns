package dev.masp005.coastersigns.rides;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import dev.masp005.coastersigns.CoasterSigns;
import dev.masp005.coastersigns.Util;

public class RideManager {
    private static final String DIRECTORY_NAME = "rides";
    private static String debugName = "attchMod";
    // private static String helpLink = "ridemanager.html";
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
                            Ride.fromConfig(plugin.readConfig(DIRECTORY_NAME, file), new File(rideDir, file)));
                } catch (Exception err) {
                    plugin.logError(String.format("Error reading Ride %s: %s", file, err.getMessage()),
                            debugName + ".read");
                }
            }
        }
        plugin.logInfo("Ride manager initialized.", "setup");
    }

    public Ride getRide(String name) {
        return rides.get(name);
    }

    public void createRide(String name) {
        Ride ride = new Ride();
        ride.name = name;
        rides.put(name, ride);
    }

    public List<String> listRides() {
        List<String> list = new LinkedList<>();
        for (String key : rides.keySet()) {
            list.add(key);
        }
        return list;
    }
}
