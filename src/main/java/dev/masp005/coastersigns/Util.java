package dev.masp005.coastersigns;

import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;

public class Util {
    public static final Map<Character, BlockFace> cartesianDirectionCharMap = new HashMap<>();
    private static final Map<BlockFace, Integer> yawMap = new HashMap<>();

    static {
        cartesianDirectionCharMap.put('n', BlockFace.NORTH);
        cartesianDirectionCharMap.put('s', BlockFace.SOUTH);
        cartesianDirectionCharMap.put('w', BlockFace.WEST);
        cartesianDirectionCharMap.put('e', BlockFace.EAST);
        cartesianDirectionCharMap.put('u', BlockFace.UP);
        cartesianDirectionCharMap.put('d', BlockFace.DOWN);

        BlockFace[] faces = {
                BlockFace.SOUTH, BlockFace.SOUTH_SOUTH_WEST, BlockFace.SOUTH_WEST, BlockFace.WEST_SOUTH_WEST,
                BlockFace.WEST, BlockFace.WEST_NORTH_WEST, BlockFace.NORTH_WEST, BlockFace.NORTH_NORTH_WEST,
                BlockFace.NORTH, BlockFace.NORTH_NORTH_EAST, BlockFace.NORTH_EAST, BlockFace.EAST_NORTH_EAST,
                BlockFace.EAST, BlockFace.EAST_SOUTH_EAST, BlockFace.SOUTH_EAST, BlockFace.SOUTH_SOUTH_EAST
        };

        for (int i = 0; i < faces.length; i++)
            yawMap.put(faces[i], i * 360 / faces.length);
        yawMap.put(BlockFace.UP, 0);
        yawMap.put(BlockFace.DOWN, 0);
    }

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
     * @return Coordinates in the format "X, Y, Z".
     */
    public static String blockCoordinates(Block block) {
        return blockCoordinates(block, ", ");
    }

    /**
     * Turns a block into human-readable coordinates with a custom seperator between values.
     *
     * @param block The block.
     * @return Coordinates in the format "X(seperator)Y(seperator)Z".
     */
    public static String blockCoordinates(Block block, String seperator) {
        return block.getX() + seperator +
                block.getY() + seperator +
                block.getZ();
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

    /**
     * Returns the nearest cartesian BlockFace. Prioritises X∓-facing directions over Y∓-facing over Z∓-facing.
     *
     * @param direction The BlockFace to work off of.
     * @return The nearest cartesian direction.
     */
    public static BlockFace nearestCartesianDirection(BlockFace direction) {
        if (direction.isCartesian()) return direction;
        return nearestCartesianDirection(direction.getDirection());
    }

    /**
     * Returns the nearest cartesian BlockFace. Prioritises X∓-facing directions over Y∓-facing over Z∓-facing.
     *
     * @param direction The Vector direction to work off of.
     * @return The nearest cartesian direction.
     */
    public static BlockFace nearestCartesianDirection(Vector direction) {
        direction = direction.normalize();
        int mostPotentSize = largestAbsoluteIndex(new double[]{direction.getX(), direction.getY(), direction.getZ()});
        switch (mostPotentSize) {
            case 0:
                return direction.getX() > 0 ? BlockFace.EAST : BlockFace.WEST;
            case 1:
                return direction.getY() > 0 ? BlockFace.UP : BlockFace.DOWN;
            case 2:
                return direction.getZ() > 0 ? BlockFace.SOUTH : BlockFace.NORTH;
        }
        return BlockFace.NORTH; // never reachable but compilers won't shut up otherwise.
    }

    public static int blockFaceYaw(BlockFace face) {
        return yawMap.get(face);
    }

    /**
     * Calculates the index of the highest absolute value.
     * Inputting an array containing 5,-7,2 would return 1, as -7 (absolute 7) is the highest value present and its index is 1.
     *
     * @param values The value array to work with.
     * @return The index of the highest absolute value.
     */
    public static int largestAbsoluteIndex(double[] values) {
        int maxIdx = -1;
        double maxVal = 0;
        for (int i = 0; i < values.length; i++) {
            double value = Math.abs(values[i]);
            if (value <= maxVal) continue;
            maxIdx = i;
            maxVal = value;
        }
        return maxIdx;
    }
}
