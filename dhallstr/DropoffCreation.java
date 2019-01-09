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

    public static DropoffCreation findBestPosition(Game game, Position initial, int dist) {
        int bestx = -1, besty = -1;
        int bestScore = Integer.MIN_VALUE;
        for (int i = -dist; i <= dist; i++) {
            for (int j = Math.abs(i) - dist; j <= dist - Math.abs(i); j++) {
                MapCell test = game.gameMap.at(new Position(initial.x + i, initial.y + j));
                int score = scoreLoc(game, test);
                if (score > bestScore) {
                    bestScore = score;
                    bestx = test.x;
                    besty = test.y;
                }
            }
        }
        if (bestScore == Integer.MIN_VALUE) {
            return null;
        }
        return new DropoffCreation(bestx, besty);
    }

    private static int scoreLoc(Game game, MapCell loc) {
        if (game.gameMap.calculateDistanceToDropoff(game.me, loc) < Magic.MIN_DIST_FOR_BUILD - 5) return 0;
        return game.gameMap.numHaliteWithin(loc, Magic.BUILD_DROPOFF_RADIUS) + loc.friendlyShipsNearby * 100 - loc.enemyShipsNearby * 150;
    }
}
