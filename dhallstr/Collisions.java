package dhallstr;

import hlt.*;

public class Collisions {
    public static int AVOID = 0;
    public static int ALLOW = 1;
    public static int COLLIDE = 2;

    public static int shouldCollide(Ship mine, Ship enemy, Game game) {
        if (!Strategy.IS_TWO_PLAYER || game.getEnemyShips() > game.me.ships.size()) {
            return AVOID;
        }
        if (mine.halite > enemy.halite * 2) return AVOID;
        return ALLOW;
    }
}
