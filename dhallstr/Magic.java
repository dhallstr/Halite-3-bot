package dhallstr;

import hlt.Constants;
import hlt.GameMap;

import java.util.ArrayList;

public class Magic {
    public static final String BOT_NAME = "Xenon";

    // Dropoff constants
    public static final int MIN_DIST_FOR_BUILD = 15;
    public static final int BUILD_DROPOFF_RADIUS = 6;
    public static final int MIN_HALITE_FOR_BUILD = 9000;
    public static final int BUILD_BUFFER_HALITE = 0;
    public static final int SHIPS_PER_DROPOFF = 15;
    public static final int MIN_TURNS_LEFT_FOR_DROPOFF = 70;
    public static final int DROPOFF_FRIENDLY_SHIP_RADIUS = 10;
    public static final int MIN_FRIENDLY_AROUND_FOR_DROPOFF = 7;
    public static int MAX_DROPOFFS = 1;// includes shipyard


    // Mining constants
    // Tiles are mined down to COLLECTION_INT + COLLECTION_SLOPE * (FIND_PERCENTILE percentile of halite "near" a friendly dropoff, i.e. within SEARCH_DEPTH)
    public static double FIND_PERCENTILE = 0.75;
    public static double COLLECTION_INT = 25,
                        COLLECTION_SLOPE = 0.27;
    public static double END_GAME_FIND_PERCENTILE = 0.9;
    public static int COLLECTION_END_GAME_HALITE = 30;
    public static double END_GAME_COLLECTION_INT = 12,
                        END_GAME_COLLECTION_SLOPE = 0.2;

    public static int COLLECT_DOWN_TO;
    public static int START_DELIVER_HALITE;
    public static int END_GAME_DELIVER_HALITE, END_GAME_HALITE = 35;

    public static int SEARCH_DEPTH;


    public static ArrayList<int[]> INSPIRE_OFFSET;

    public static void updateConstants(boolean isTwoPlayer, int width, int height) {
        COLLECT_DOWN_TO = Constants.MAX_HALITE / 14;
        START_DELIVER_HALITE = (int)(Constants.MAX_HALITE * 0.92);
        END_GAME_DELIVER_HALITE = (int)(Constants.MAX_HALITE * 0.6);

        INSPIRE_OFFSET = new ArrayList<>(2*Constants.INSPIRATION_RADIUS*(Constants.INSPIRATION_RADIUS+1)+1);
        for (int i = - Constants.INSPIRATION_RADIUS; i <= Constants.INSPIRATION_RADIUS; i++) {
            for (int j = - Constants.INSPIRATION_RADIUS; j <= Constants.INSPIRATION_RADIUS; j++) {
                if (Math.abs(i) + Math.abs(j) <= Constants.INSPIRATION_RADIUS) {
                    INSPIRE_OFFSET.add(new int[] {i, j});
                }
            }
        }

        int size = (width + height) / 2; // in case it is a rectangle
        END_GAME_HALITE += (int)((size - 32) / 32.0 * (45 - END_GAME_HALITE));
        if (isTwoPlayer) {
            MAX_DROPOFFS = (int)(size / 11);
            SEARCH_DEPTH = 65;
        }
        else {
            END_GAME_DELIVER_HALITE = (int) (Constants.MAX_HALITE * 0.4);
            END_GAME_HALITE = 25;
            MAX_DROPOFFS = (int)(size / 11);
            SEARCH_DEPTH = 45;
        }
    }

    public static int getCollectDownTo(GameMap game) {
        boolean isEndGame =game.percentileHaliteNearMyDropoffs > COLLECTION_END_GAME_HALITE;
        if (isEndGame) FIND_PERCENTILE = END_GAME_FIND_PERCENTILE; // will take effect next turn
        return (int)(isEndGame ? (COLLECTION_INT + COLLECTION_SLOPE * game.percentileHaliteNearMyDropoffs) : (END_GAME_COLLECTION_INT + END_GAME_COLLECTION_SLOPE * game.percentileHaliteNearMyDropoffs));
    }
}
