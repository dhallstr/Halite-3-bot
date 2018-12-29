package dhallstr;

import hlt.*;

public class Collisions {
    public static int AVOID = 0;
    public static int ALLOW = 1;
    public static int COLLIDE = 2;

    public static int shouldCollide(Ship mine, Ship enemy, Game game) {
        if (!Strategy.IS_TWO_PLAYER) {
            // Never COLLIDE, but sometimes allow or avoid
            // for now, I'm going to allow collisions if I have less halite
            if (mine.halite < enemy.halite) return ALLOW;
            return AVOID;
        }
        else {
            if (game.getEnemyShips() > game.me.ships.size()) {
                return AVOID;
            }
            if (mine.halite > enemy.halite * 2) return AVOID;
            // also consider enemy dropoff dist. vs mine, and enemy ships nearby vs. mine
            return ALLOW;
        }
    }
}
