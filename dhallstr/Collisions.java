package dhallstr;

import hlt.*;

public class Collisions {
    public static int AVOID = 0;
    public static int ALLOW = 1;
    public static int COLLIDE = 2;

    public static int shouldCollide(Ship mine, Ship enemy, Game game) {
        int support = game.gameMap.at(mine).friendlyShipsNearby;
        int opposition = game.gameMap.at(enemy).enemyShipsNearby;
        if (enemy.halite > Constants.MAX_HALITE * 0.6 && support > opposition * 2 + 2 && game.turnNumber + 60 > Constants.MAX_TURNS) {
            return COLLIDE;
        }
        if (!Strategy.IS_TWO_PLAYER) {
            if (mine.halite < enemy.halite + 30 && support > opposition - 1 && support > 3) return ALLOW;
            else if (mine.halite < Constants.MAX_HALITE * 0.6 && support > opposition * 1.3 + 3 && support > 3) return ALLOW;
            else return AVOID;
        }
        else {
            if (mine.halite < Constants.MAX_HALITE * 0.7 && enemy.halite > Constants.MAX_HALITE * 0.5 && support > opposition * 1.8) return COLLIDE;
            else if (mine.halite < enemy.halite + 30 && support > opposition && support > 2) return ALLOW;
            else return AVOID;
        }
    }
}
