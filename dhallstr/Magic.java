package dhallstr;

import hlt.*;

import java.util.ArrayList;

public class Magic {
    public static final String BOT_NAME = "Kraken";

    // Dropoff constants
    public static final int MIN_DIST_FOR_BUILD = 9;
    public static final int BUILD_DROPOFF_RADIUS = 8;
    private static final int NUM_IN_BUILD_RADIUS = 2*BUILD_DROPOFF_RADIUS*(BUILD_DROPOFF_RADIUS+1)+1;
    public static final int MIN_SCORE_FOR_DROPOFF = 9000;
    public static final int SHIPS_PER_DROPOFF = 15;
    public static int MAX_DROPOFFS = 1;// includes shipyard


    // Mining constants
    // Tiles are mined down to COLLECTION_INT + COLLECTION_SLOPE * (FIND_PERCENTILE percentile of halite "near" a friendly dropoff, i.e. within SEARCH_DEPTH)
    public static double FIND_PERCENTILE = 0.75, NEAR_FIND_PERCENTILE = 0.5;
    public static double COLLECTION_INT = 23,
                        COLLECTION_SLOPE = 0.27;
    public static double END_GAME_FIND_PERCENTILE = 0.9;
    public static int COLLECTION_END_GAME_HALITE = 27;
    public static double END_GAME_COLLECTION_INT = 12,
                        END_GAME_COLLECTION_SLOPE = 0.2;
    public static double MINING_WEIGHT = 0;

    public static int COLLECT_DOWN_TO;
    public static int START_DELIVER_HALITE, MIN_HALITE_FOR_DELIVER;
    public static int END_GAME_DELIVER_HALITE, END_GAME_HALITE;

    public static int SEARCH_DEPTH;
    public static int HALITE_SEARCH_DEPTH = 30;//only used for gameMap.halitePercentile
    public static int NEAR_DROPOFF_SEARCH_DIST = 8;
    public static int NEAR_SHIP_DIST = 7;// how far away a ship can be from another to be considered "near"


    public static ArrayList<int[]> INSPIRE_OFFSET, NEARBY_SHIP_OFFSET;

    public static void updateConstants(boolean isTwoPlayer, int width, int height) {
        COLLECT_DOWN_TO = Constants.MAX_HALITE / 14;
        START_DELIVER_HALITE = (int)(Constants.MAX_HALITE * 0.85);
        MIN_HALITE_FOR_DELIVER = (int)(Constants.MAX_HALITE * 0.3);
        END_GAME_DELIVER_HALITE = (int)(Constants.MAX_HALITE * 0.5);

        INSPIRE_OFFSET = new ArrayList<>(2*Constants.INSPIRATION_RADIUS*(Constants.INSPIRATION_RADIUS+1)+1);
        for (int i = - Constants.INSPIRATION_RADIUS; i <= Constants.INSPIRATION_RADIUS; i++) {
            for (int j = Math.abs(i) - Constants.INSPIRATION_RADIUS; j <= Constants.INSPIRATION_RADIUS - Math.abs(i); j++) {
                INSPIRE_OFFSET.add(new int[] {i, j});
            }
        }

        NEARBY_SHIP_OFFSET = new ArrayList<>(2*NEAR_SHIP_DIST*(NEAR_SHIP_DIST+1)+1);
        for (int i = - NEAR_SHIP_DIST; i <= NEAR_SHIP_DIST; i++) {
            for (int j = Math.abs(i)- NEAR_SHIP_DIST; j <= NEAR_SHIP_DIST - Math.abs(i); j++) {
                NEARBY_SHIP_OFFSET.add(new int[] {i, j});
            }
        }

        int size = (width + height) / 2; // in case it is a rectangle
        END_GAME_HALITE += (int)((size - 32) / 32.0 * (45 - END_GAME_HALITE));
        if (isTwoPlayer) {
            MAX_DROPOFFS = size / 8;
            SEARCH_DEPTH = 75;
            END_GAME_HALITE = 15;

            // CLOP adjustments
            COLLECTION_INT = 18.7964;
            COLLECTION_SLOPE = 1.12599;
            FIND_PERCENTILE = 0.461508;
            END_GAME_HALITE = 25;
            END_GAME_FIND_PERCENTILE = 0.335921;
            END_GAME_COLLECTION_INT = 20.511;
            END_GAME_COLLECTION_SLOPE = 1.13451;
        }
        else {
            END_GAME_DELIVER_HALITE = (int) (Constants.MAX_HALITE * 0.4);
            END_GAME_HALITE = 25;
            MAX_DROPOFFS = size / 11;
            SEARCH_DEPTH = 60;
        }
    }

    public static void commandLineParams(String[] args) {
        if (args.length % 2 == 1 || args.length == 0) return;
        for (int i = 0; i < args.length; i += 2) {
            switch(args[i]) {
                case "COLLECTION_INT":
                    COLLECTION_INT = Double.parseDouble(args[i + 1]);
                    break;
                case "COLLECTION_SLOPE":
                    COLLECTION_SLOPE = Double.parseDouble(args[i+1]);
                    break;
                case "FIND_PERCENTILE":
                    FIND_PERCENTILE = Double.parseDouble(args[i+1]);
                    break;
                case "END_GAME_FIND_PERCENTILE":
                    END_GAME_FIND_PERCENTILE = Double.parseDouble(args[i+1]);
                    break;
                case "COLLECTION_END_GAME_HALITE":
                    COLLECTION_END_GAME_HALITE = Integer.parseInt(args[i+1]);
                    break;
                case "END_GAME_COLLECTION_INT":
                    END_GAME_COLLECTION_INT = Double.parseDouble(args[i+1]);
                    break;
                case "END_GAME_COLLECTION_SLOPE":
                    END_GAME_COLLECTION_SLOPE = Double.parseDouble(args[i+1]);
                    break;
            }
        }
    }

    static int getCollectDownTo(GameMap game, MapCell loc, int shipHalite) {
        boolean prevEndGame = FIND_PERCENTILE == END_GAME_FIND_PERCENTILE;
        if (game.percentileHalite > COLLECTION_END_GAME_HALITE) FIND_PERCENTILE = END_GAME_FIND_PERCENTILE; // will take effect next turn
        double weight = MINING_WEIGHT;
        if (shipHalite < 60) weight = 0.6;
        return (int)((prevEndGame ? (COLLECTION_INT + COLLECTION_SLOPE * game.percentileHalite) : (END_GAME_COLLECTION_INT + END_GAME_COLLECTION_SLOPE * game.percentileHalite)) * (1 - weight) + weight * loc.haliteNearby / NUM_IN_BUILD_RADIUS);
    }

    static int getMinHaliteMined(GameMap map, MapCell loc, int shipHalite) {
        return getCollectDownTo(map, loc, shipHalite) / (Constants.EXTRACT_RATIO * 3);
    }
}
