package dhallstr;

import hlt.*;

public class DropoffCreation extends Position {

    EntityId builder = null;
    private DropoffCreation(int x, int y) {
        super(x, y);
    }

    public Ship findBestShip(Game game) {
        Ship best = null;
        int bestDist = Integer.MAX_VALUE;
        for (Ship s: game.me.ships.values()) {
            int dist = game.gameMap.calculateDistance(s, this);
            if (dist < bestDist || (dist == bestDist && s.halite > best.halite)) {
                bestDist = dist;
                best = s;
            }
        }
        if (best != null)
            builder = best.id;
        return best;
    }

    public static DropoffCreation findBestPosition(Game game) {
        if (game.me.dropoffs.values().size() >= Magic.MAX_DROPOFFS || game.me.dropoffs.values().size() * Magic.SHIPS_PER_DROPOFF > game.me.ships.values().size()) return null;
        int bestx = -1, besty = -1;
        int bestScore = Integer.MIN_VALUE;
        for (int y = 0; y < game.gameMap.height; y++) {
            for (int x = 0; x < game.gameMap.width; x++) {
                int score = scoreLoc(game, game.gameMap.at(new Position(x, y)));
                if (score > bestScore) {
                    bestx = x;
                    besty = y;
                    bestScore = score;
                }
            }
        }
        if (bestScore < Magic.MIN_SCORE_FOR_DROPOFF) {
            return null;
        }
        return new DropoffCreation(bestx, besty);
    }

    private static int scoreLoc(Game game, MapCell loc) {
        if (game.gameMap.calculateDistanceToDropoff(game.me, loc) < Magic.MIN_DIST_FOR_BUILD || loc.friendlyShipsNearby < 2) return 0;
        return loc.haliteNearby + (5 - Math.max(5, loc.friendlyShipsNearby)) * -100 - loc.enemyShipsNearby * 100;
    }
}
