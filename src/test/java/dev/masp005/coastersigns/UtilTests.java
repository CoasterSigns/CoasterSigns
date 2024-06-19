package dev.masp005.coastersigns;

import static org.junit.jupiter.api.Assertions.*;

import org.bukkit.block.BlockFace;
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
    // first place, you're doing something wrong.
    })
    public void nearestCartesianDirection(String direction, String expect) {
        assertEquals(BlockFace.valueOf(expect), Util.nearestCartesianDirection(BlockFace.valueOf(direction)));
    }

    @ParameterizedTest(name = "{0} | {1} | {2} => {3}")
    @CsvSource({
            "0,    1,  0, UP",
            "-1,  .5, .2, WEST",
            ".1, -.5, .2, DOWN",
            "6,   -3, 17, SOUTH"
    // do not expect certain values for 45° BlockFaces. If you're using them in the
    // first place, you're doing something wrong.
    })
    public void nearestCartesianDirection(float x, float y, float z, String expect) {
        assertEquals(BlockFace.valueOf(expect), Util.nearestCartesianDirection(new Vector(x, y, z)));
    }

    @ParameterizedTest(name = "{0} => {1}")
    @CsvSource({
            "UP,         0",
            "SOUTH,      0",
            "NORTH,      180",
            "WEST,       90",
            "NORTH_WEST, 135",
            "NORTH_EAST, 225"
    })
    public void blockFaceYaw(String face, int expect) {
        assertEquals(expect, Util.blockFaceYaw(BlockFace.valueOf(face)));
    }

    @Test
    public void largestAbsoluteIndex() {
        assertEquals(2, Util.largestAbsoluteIndex(new double[] { 1, 2, 3 }));
        assertEquals(3, Util.largestAbsoluteIndex(new double[] { 1, 2, 3, -7 }));
        assertEquals(0, Util.largestAbsoluteIndex(new double[] { -8, -1, 0, 7 }));
        assertEquals(1, Util.largestAbsoluteIndex(new double[] { 1, 2, 1, 2 }));
        assertEquals(0, Util.largestAbsoluteIndex(new double[] { 0, 0, 0, 0 }));
        assertEquals(0, Util.largestAbsoluteIndex(new double[] { 8 }));
        assertEquals(0, Util.largestAbsoluteIndex(new double[] {}));
    }

    @ParameterizedTest(name = "{0} => {1}")
    @CsvSource({
            "file.yml,                         file",
            "file.test.lol,                    file.test",
            "file,                             file",
            "this.is.a.very.long.filename.mp4, this.is.a.very.long.filename"
    })
    public void removeFileExtension(String file, String expect) {
        assertEquals(expect, Util.removeFileExtension(file));
    }

}
