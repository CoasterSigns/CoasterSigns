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
        for (Map.Entry<?, ?> entry : values.entrySet())
            config.set((String) entry.getKey(), entry.getValue());
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

    /**
     * Evaluates an unknown-type input into a minimum and maximum range
     * based on the format n..m where both n and m can be omitted to reference the respective extremes.
     *
     * @param rangeRaw Unsanitized input to be evaluated
     * @param max      The maximum possible index
     * @return a length 2 array of integers representing minumum and maximum
     * @throws IllegalArgumentException if the input is not formatted correctly.
     */
    public static int[] evaluateRange(Object rangeRaw, int max) throws IllegalArgumentException {
        String range;

        if (rangeRaw instanceof Integer) range = String.valueOf(rangeRaw);
        else if (rangeRaw instanceof String) range = (String) rangeRaw;
        else if (rangeRaw == null) range = "..";
        else throw new IllegalArgumentException("Incorrectly formatted range.");

        try {
            return evaluateRange(range.trim(), max);
        } catch (NumberFormatException ignored) {
            throw new IllegalArgumentException("Incorrectly formatted range.");
        }
    }

    /**
     * Evaluates a String input into a minimum and maximum range
     * based on the format n..m where both n and m can be omitted to reference the respective extremes.
     *
     * @param range Sanitized input to be evaluated
     * @param max   The maximum possible index
     * @return a length 2 array of integers representing minumum and maximum
     */
    public static int[] evaluateRange(String range, int max) {
        int rangeMin;
        int rangeMax;

        if (range.equals("..") || range.equals("*")) {
            rangeMin = 0;
            rangeMax = max;
        } else if (range.startsWith("..")) { // ..n
            rangeMin = 0;
            rangeMax = Integer.parseInt(range.substring(2));
        } else if (range.endsWith("..")) { // n..
            rangeMin = Integer.parseInt(range.substring(0, range.indexOf('.')));
            rangeMax = max;
        } else if (range.contains("..")) { // n..m
            int delimiter = range.indexOf('.');
            rangeMin = Integer.parseInt(range.substring(0, delimiter));
            rangeMax = Integer.parseInt(range.substring(delimiter + 2));
        } else { // n
            rangeMin = rangeMax = Integer.parseInt(range);
        }
        rangeMin = Math.max(0, rangeMin);
        rangeMax = Math.min(max, rangeMax);

        return new int[]{rangeMin, rangeMax};
    }
}
