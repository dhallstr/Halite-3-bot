package hlt;

import java.util.ArrayList;
import java.util.Arrays;

public enum Direction {
    NORTH('n'),
    EAST('e'),
    SOUTH('s'),
    WEST('w'),
    STILL('o');

    public final char charValue;

    public static ArrayList<Direction> ALL_CARDINALS = new ArrayList<>();
    static {
        ALL_CARDINALS.add(NORTH);
        ALL_CARDINALS.add(SOUTH);
        ALL_CARDINALS.add(EAST);
        ALL_CARDINALS.add(WEST);
    }

    public Direction invertDirection() {
        switch (this) {
            case NORTH: return SOUTH;
            case EAST: return WEST;
            case SOUTH: return NORTH;
            case WEST: return EAST;
            case STILL: return STILL;
            default: throw new IllegalStateException("Unknown direction " + this);
        }
    }

    public static void setAllCardinals(int playerId) {
        switch(playerId) {
            case 0:
                ALL_CARDINALS = new ArrayList<>(Arrays.asList(new Direction[] {NORTH, EAST, SOUTH, WEST}));
                break;
            case 1:
                ALL_CARDINALS = new ArrayList<>(Arrays.asList(new Direction[] {NORTH, WEST, SOUTH, EAST}));
                break;
            case 2:
                ALL_CARDINALS = new ArrayList<>(Arrays.asList(new Direction[] {SOUTH, EAST, NORTH, WEST}));
                break;
            case 3:
                ALL_CARDINALS = new ArrayList<>(Arrays.asList(new Direction[] {SOUTH, WEST, NORTH, EAST}));
                break;
            default: throw new IllegalArgumentException("playerId must be in the range [0-3]. Instead, " + playerId + " was given");
        }
    }

    Direction(final char charValue) {
        this.charValue = charValue;
    }
}
