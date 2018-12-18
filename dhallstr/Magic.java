package dhallstr;

import hlt.Constants;

import java.util.ArrayList;

public class Magic {
    public static final String BOT_NAME = "MinGW";

    public static final int MIN_DIST_FOR_BUILD = 15;
    public static final int BUILD_DROPOFF_RADIUS = 6;
    public static final int MIN_HALITE_FOR_BUILD = 9000;
    public static final int BUILD_BUFFER_HALITE = 0;
    public static final int SHIPS_PER_DROPOFF = 15;
    public static final int MIN_TURNS_LEFT_FOR_DROPOFF = 70;
    public static final int DROPOFF_FRIENDLY_SHIP_RADIUS = 10;
    public static final int MIN_FRIENDLY_AROUND_FOR_DROPOFF = 7;
    public static int MAX_DROPOFFS = 1;// includes shipyard

    public static int COLLECT_DOWN_TO;
    public static int START_DELIVER_HALITE, END_DELIVER_HALITE;
    public static int END_GAME_DELIVER_HALITE, END_GAME_HALITE = 35;

    public static int MIN_BACK_TO_DROPOFF_WAIT_HALITE, MIN_GATHER_WAIT_HALITE;
    public static double GATHER_HALITE_RATIO;

    public static ArrayList<int[]> INSPIRE_OFFSET;

    public static void updateConstants() {
        COLLECT_DOWN_TO = Constants.MAX_HALITE / 14;
        START_DELIVER_HALITE = (int)(Constants.MAX_HALITE * 0.95);
        END_DELIVER_HALITE = (int)(Constants.MAX_HALITE * 0.75);

        END_GAME_DELIVER_HALITE = (int)(Constants.MAX_HALITE * 0.5);

        INSPIRE_OFFSET = new ArrayList<>(2*Constants.INSPIRATION_RADIUS*(Constants.INSPIRATION_RADIUS+1)+1);
        for (int i = - Constants.INSPIRATION_RADIUS; i <= Constants.INSPIRATION_RADIUS; i++) {
            for (int j = - Constants.INSPIRATION_RADIUS; j <= Constants.INSPIRATION_RADIUS; j++) {
                if (Math.abs(i) + Math.abs(j) <= Constants.INSPIRATION_RADIUS) {
                    INSPIRE_OFFSET.add(new int[] {i, j});
                }
            }
        }
    }

    public static void updateConstants(boolean isTwoPlayer, int width, int height) {
        int size = (width + height) / 2; // in case it is a rectangle
        END_GAME_HALITE += (int)((size - 32) / 32.0 * (45 - END_GAME_HALITE));
        if (isTwoPlayer) {
            MIN_BACK_TO_DROPOFF_WAIT_HALITE = (int)(Constants.MAX_HALITE * 0.018);
            MIN_GATHER_WAIT_HALITE = (int)(Constants.MAX_HALITE * (0.03 - 0.01 * ((64 - size)/32.0)));//0.04);
            GATHER_HALITE_RATIO = 2.0;
            MAX_DROPOFFS = (int)(size / 11);
        }
        else {
            MIN_BACK_TO_DROPOFF_WAIT_HALITE = (int)(Constants.MAX_HALITE * 0.018);
            //MIN_GATHER_WAIT_HALITE = (int)(Constants.MAX_HALITE * (0.022 + 0.01 * ((size - 32)/32.0)));//0.04);
            MIN_GATHER_WAIT_HALITE = (int)(Constants.MAX_HALITE * (0.03 - 0.01 * ((64 - size)/32.0)));
            GATHER_HALITE_RATIO = 2.8;
            END_GAME_DELIVER_HALITE = (int) (Constants.MAX_HALITE * 0.4);
            END_GAME_HALITE = 25;
            MAX_DROPOFFS = (int)(size / 11);
        }
    }
}
