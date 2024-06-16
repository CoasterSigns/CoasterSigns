package dev.masp005.coastersigns;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.bukkit.block.BlockFace;
import org.bukkit.event.block.BlockCanBuildEvent;
import org.bukkit.util.Vector;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

public class UtilTests {

    @ParameterizedTest(name = "{0} <{1} => [{2}, {3}]")
    @CsvSource({
            "..,   7, 0, 7",
            "6,    8, 6, 6",
            "5..,  9, 5, 9",
            "..3,  4, 0, 3",
            "0..4, 3, 0, 3",
            "4..,  3, 3, 3",
            "9,    4, 4, 4",
            ",     2, 0, 2",
            "-5,   2, 0, 0"
    })
    public void evaluateRange(Object rangeRaw, int max, int expectmin, int expectmax) {
        assertArrayEquals(new int[] { expectmin, expectmax }, Util.evaluateRange(rangeRaw, max));
    }

    @ParameterizedTest(name = "{0} <{1} => Error")
    @CsvSource({
            "ten",
            "m"
    })
    public void evaluateRangeErrors(Object rangeRaw) {
        assertThrows(IllegalArgumentException.class, () -> Util.evaluateRange(rangeRaw, 9));
    }

    @ParameterizedTest(name = "{0} => {1}")
    @CsvSource({
            "NORTH_NORTH_EAST, NORTH",
            "SOUTH_SOUTH_EAST, SOUTH",
            "UP, UP",
            "NORTH, NORTH",
            "WEST_NORTH_WEST, WEST"
    // do not expect certain values for 45° BlockFaces. If you're using them in the
    // first place, something's wrong.
    })
    public void /* BlockFace */ nearestCartesianDirection(String direction, String expect) {
        assertEquals(BlockFace.valueOf(expect), Util.nearestCartesianDirection(BlockFace.valueOf(direction)));
    }

    @ParameterizedTest(name = "{0} | {1} | {2} => {3}")
    @CsvSource({
            "0, 1, 0, UP",
    // do not expect certain values for 45° BlockFaces. If you're using them in the
    // first place, something's wrong.
    })
    public void /* BlockFace */ nearestCartesianDirection(float x, float y, float z, String expect) {
        assertEquals(BlockFace.valueOf(expect), Util.nearestCartesianDirection(new Vector(x, y, z)));
    }

    // @Test
    public void /* int */ blockFaceYaw(BlockFace face) {

    }

    // @Test
    public void /* int */ largestAbsoluteIndex(double[] values) {

    }

}
