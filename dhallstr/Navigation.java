package dhallstr;

import hlt.*;

import java.util.LinkedList;

public class Navigation {
    public static int modifiedPaths = 0;
    static Direction[] bfs(Game game, Ship s, Goal goal, PlannedLocations plan) {

        GameMap map = game.gameMap;

        map.setAllUnvisited();

        LinkedList<MapCell> queue = new LinkedList<>();
        queue.add(map.at(s));
        map.at(s).visited = true;

        MapCell best = null;
        int bestScore = Integer.MIN_VALUE;
        int bestActualDist = 0;

        while (!queue.isEmpty()) {
            MapCell curr = queue.poll();
            if (curr == null) continue;

            curr.processed = true;

            int prevDist = curr.actualDist;
            int numStays = goal.getNumberStays(s, curr, plan, map);
            int cellHalite = plan.getProjectedHalite(map, curr, curr.dist);
            if (numStays > 0) {
                curr.actualDist+= numStays;

                for (int i = 0; i < numStays; i++) {

                    if ((!plan.isSafe(game, curr, s, curr.dist + 1 + i, curr.dist + i < 3 ) && !goal.overrideUnsafe(curr))) {
                        curr.actualDist += -numStays + i;
                        break;
                    }
                    int mined = Math.min(curr.minedAmount(cellHalite), Constants.MAX_HALITE - s.halite + curr.lost - curr.gained);
                    int collected = Math.min(curr.collectAmount(cellHalite), Constants.MAX_HALITE - s.halite + curr.lost - curr.gained);
                    curr.gained += collected;
                    cellHalite -= mined;
                }
            }
            if (curr.actualDist == prevDist && s.halite - curr.lost + curr.gained < curr.halite / Constants.MOVE_COST_RATIO) {// not using curr.moveCost() because we don't want to count on inspiration
                curr.actualDist++;
                curr.gained += Math.min(curr.collectAmount(plan.getProjectedHalite(map, curr, curr.dist)), Constants.MAX_HALITE - s.halite + curr.lost - curr.gained);
                cellHalite -= Math.min(curr.collectAmount(cellHalite), Constants.MAX_HALITE - s.halite + curr.lost - curr.gained);
            }

            if (goal.meetsGoal(curr)) {
                int score = goal.rateTile(game, curr, s, plan);
                if (score > bestScore) {
                    best = curr;
                    bestScore = score;
                    bestActualDist = curr.actualDist;
                }
            }


            if (curr.actualDist > goal.getMaxTurns()) {
                return finishSearch(s, map, best, bestScore, bestActualDist);
            }



            for (Direction d: goal.sort(game.gameMap, curr, Direction.ALL_CARDINALS)) {
                MapCell m = map.offset(curr, d);
                if (!m.visited && (plan.isSafe(game, m, s, curr.actualDist + 1, curr.actualDist <= 3) || goal.overrideUnsafe(m))){
                    queue.add(m);
                    m.visited = true;
                    m.path = d;
                    m.dist = curr.actualDist + 1;
                    m.actualDist = m.dist;
                    m.lost = curr.lost + curr.moveCost();
                    m.gained = curr.gained;
                }
                else if (m.visited && (!m.processed || m.actualDist == curr.actualDist - 1)) {
                    // if this path is strictly better than the first one we found, change it
                    if (curr.gained - curr.lost - curr.moveCost() >= m.gained - m.lost && curr.actualDist + 1 <= m.actualDist) {
                        m.path = d;
                        m.dist = curr.actualDist + 1;
                        m.actualDist = m.dist;
                        m.lost = curr.lost - curr.moveCost();
                        m.gained = curr.gained;
                        modifiedPaths++;
                    }
                }
            }
            if ((plan.isSafe(game, curr, s, curr.actualDist + 1, curr.actualDist <= 3) || goal.overrideUnsafe(curr)) && curr.actualDist - curr.dist < 2 &&
                    !curr.hasStructure()) {
                queue.add(curr);
                curr.actualDist++;
                curr.gained += curr.collectAmount(cellHalite);
            }
        }
        return finishSearch(s, map, best, bestScore, bestActualDist);
    }

    private static Direction[] finishSearch(Ship s, GameMap map, MapCell best, int bestScore, int bestActualDist) {
        if (best != null) {
            best.actualDist = bestActualDist;
            Log.log("Best goal at " + best.toString() + " with score " + bestScore);
            return extractPath(map, s, best);
        }
        return new Direction[] { Direction.STILL};
    }

    private static Direction[] extractPath(GameMap map, Ship s, MapCell curr) {
        Direction[] directions = new Direction[(curr.actualDist == 0) ? 1 : curr.actualDist];
        for (int i = 0; i < directions.length; i++) {
            directions[i] = Direction.STILL;
        }
        int cost = 0;

        int prevDist = curr.dist;
        while (curr.dist != 0) {
            directions[curr.dist - 1] = curr.path;
            curr = map.offset(curr, curr.path.invertDirection());
            cost += curr.moveCost();

            int halite = curr.halite;// not doing projected halite to be conservative
            for (int i = 0; i < prevDist - curr.dist - 1; i++) {
                int mined = Math.min(curr.minedAmount(halite), Constants.MAX_HALITE - s.halite + cost);
                int collected = Math.min(curr.collectAmount(halite), Constants.MAX_HALITE - s.halite + cost);
                cost -= collected;
                halite -= mined;
            }
            prevDist = curr.dist;
        }
         return directions;
    }

}
