package dhallstr;

import hlt.*;

import java.util.LinkedList;

public class Navigation {

    static Direction[] bfs(Game game, Ship s, Goal goal, PlannedLocations plan) {

        GameMap map = game.gameMap;

        map.setAllUnvisited();

        LinkedList<MapCell> queue = new LinkedList<>();
        queue.add(map.at(s.position));
        map.at(s.position).visited = true;

        MapCell best = null;
        int bestScore = Integer.MIN_VALUE;

        while (!queue.isEmpty()) {
            MapCell curr = queue.poll();
            if (curr == null) continue;

            if (goal.meetsGoal(curr)) {
                int score = goal.rateTile(game, curr, s, plan);
                if (score > bestScore) {
                    best = curr;
                    bestScore = score;
                }
            }

            int prevDist = curr.actualDist;
            int numStays = goal.getNumberStays(s, curr, plan, map);
            if (numStays > 0) {
                curr.actualDist+= numStays;
                int halite = plan.getProjectedHalite(map, curr.position, curr.dist);
                for (int i = 0; i < numStays; i++) {

                    if ((!plan.isSafe(map, curr.position, s, curr.dist + 1 + i, false ) && !goal.overrideUnsafe(curr))) {
                        curr.actualDist += -numStays + i;
                        break;
                    }
                    int mined = Math.min(curr.minedAmount(halite), Constants.MAX_HALITE - s.halite + curr.lost - curr.gained);
                    int collected = Math.min(curr.collectAmount(halite), Constants.MAX_HALITE - s.halite + curr.lost - curr.gained);
                    curr.gained += collected;
                    halite -= mined;
                }
            }
            if (curr.actualDist == prevDist && s.halite - curr.lost + curr.gained < curr.moveCost(curr.halite)) {
                curr.actualDist++;
                curr.gained += Math.min(curr.collectAmount(plan.getProjectedHalite(map, curr.position, curr.dist)), Constants.MAX_HALITE - s.halite + curr.lost - curr.gained);
            }


            if (curr.actualDist > goal.getMaxTurns()) {
                return finishSearch(game, s, goal, plan, map, best, bestScore);
            }



            for (Direction d: goal.sort(game.gameMap, curr, Direction.ALL_CARDINALS)) {
                MapCell m = map.offset(curr, d);
                if (!m.visited && (plan.isSafe(map, m.position, s, curr.actualDist + 1, curr.actualDist <= 1 && Strategy.COLLISIONS_DISABLED) || goal.overrideUnsafe(m))){
                    queue.add(m);
                    m.visited = true;
                    m.path = d;
                    m.dist = curr.actualDist + 1;
                    m.actualDist = m.dist;
                    m.lost = curr.lost + curr.moveCost();
                    m.gained = curr.gained;
                }
            }
            if ((plan.isSafe(map, curr.position, s, curr.actualDist + 1, false) || goal.overrideUnsafe(curr)) && curr.actualDist - curr.dist < 2 &&
                    !curr.hasStructure()) {
                queue.add(curr);
                curr.actualDist++;
                curr.gained += curr.collectAmount(plan.getProjectedHalite(map, curr.position, curr.dist));
            }
        }
        return finishSearch(game, s, goal, plan, map, best, bestScore);
    }

    private static Direction[] finishSearch(Game game, Ship s, Goal goal, PlannedLocations plan, GameMap map, MapCell best, int bestScore) {
        if (best != null) {
            Log.log("Best goal at " + best.position.toString() + " with score " + bestScore);
            return extractPath(map, plan, s, best);
        }
        return new Direction[] { Direction.STILL};
    }

    private static Direction[] extractPath(GameMap map, PlannedLocations plan, Ship s, MapCell curr) {
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

            int halite = curr.halite;// plan.getProjectedHalite(map, curr.position, curr.dist);
            for (int i = 0; i < prevDist - curr.dist - 1; i++) {
                int mined = Math.min(curr.minedAmount(halite), Constants.MAX_HALITE - s.halite + cost);
                int collected = Math.min(curr.collectAmount(halite), Constants.MAX_HALITE - s.halite + cost);
                cost -= collected;
                halite -= mined;
            }
            prevDist = curr.dist;
        }

        if (cost <= s.halite)
            return directions;

        cost = curr.moveCost();
        if (cost > s.halite) {
            return new Direction[] {Direction.STILL};
        }

        Log.log("Uh oh, it seems you've reached code that should be dead (Navigation.java, line ~134)");
        Log.log("Safety code is included so that a crash doesn't occur, but it is not at all ideal.");

        int halite = curr.halite;// not using projected halite because this code is just a safety net (also there were problems when I tried)
        for (int i = 0; i < directions.length; i++) {
            curr = map.offset(curr, directions[i]);
            if (directions[i] != Direction.STILL) {
                cost += curr.moveCost();
                halite = curr.halite;
            }
            else {
                int mined = Math.min(curr.minedAmount(halite), Constants.MAX_HALITE - s.halite + cost);
                int collected = Math.min(curr.collectAmount(halite), Constants.MAX_HALITE - s.halite + cost);
                cost -= collected;
                halite -= mined;
            }

            if (cost > s.halite) {
                Direction[] dirs = new Direction[i+2];
                for (int j = 0; j < i + 1; j++) {
                    dirs[j] = directions[j];
                }
                dirs[i+1] = Direction.STILL;
                return dirs;
            }
        }
        if (!Log.DISABLE_LOGS)
            throw new RuntimeException("This code should never have been reached.");
        return directions;
    }

}
