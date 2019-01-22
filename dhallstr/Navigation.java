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

        Best best = new Best();

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
                    if (collected > mined) {
                        curr.extra -= mined / 2;// small penalty for inspiration in  2p
                    }
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
                curr.score = goal.rateTile(game, curr, s, plan);
                if (curr.score > best.score) {
                    best.endCell = curr;
                    best.score = curr.score;
                    best.actualDist = curr.actualDist;
                    best.depth = curr.depth;
                }
            }


            if (curr.depth > goal.getMaxTurns() || (curr.depth >= best.depth + 17 && curr.depth > 27 && Strategy.PREVENT_TIMEOUT_MODE)) {
                return finishSearch(s, map, best);
            }



            for (Direction d: goal.sort(game.gameMap, curr, Direction.ALL_CARDINALS)) {
                MapCell m = map.offset(curr, d);
                if (!m.visited && (plan.isSafe(game, m, s, curr.actualDist + 1, curr.actualDist <= 3) || goal.overrideUnsafe(m))){
                    queue.add(m);
                    m.visited = true;
                    m.path = d;
                    m.dist = curr.actualDist + 1;
                    m.depth = curr.depth + 1;
                    m.actualDist = m.dist;
                    m.lost = curr.lost + curr.moveCost();
                    m.extra = curr.extra;
                    m.gained = curr.gained;
                }
                else if (m.visited && curr.path != d.invertDirection() && m.path != Direction.STILL && curr.depth <= m.depth && (!m.processed || m.dist == curr.actualDist + 1)) {
                    // maybe change the path to be from curr to m
                    Direction prevDir = m.path;
                    int prevADist = m.actualDist, prevDDist = m.dist, prevDepth = m.depth, prevLost = m.lost, prevGained = m.gained, prevExtra = m.extra;
                    int prevScore = m.score == Integer.MIN_VALUE ? goal.rateTile(game, m, s, plan) : m.score;
                    m.path = d;
                    m.actualDist += curr.actualDist + 1  - prevDDist;
                    m.dist = curr.actualDist + 1;
                    m.depth = curr.depth + 1;
                    m.lost = curr.lost + curr.moveCost();
                    m.extra = curr.extra;
                    m.gained = curr.gained + m.haliteCollectedAfterXTurns(plan.getProjectedHalite(map, m, m.dist), s.halite + curr.gained - m.lost, prevADist - prevDDist);
                    m.score = goal.rateTile(game, m, s, plan);
                    if (!(m.score > prevScore && m.dist == prevDDist) && m.score <= prevScore + (m.dist - prevDDist) * game.gameMap.percentileHalite / (m.actualDist * Constants.EXTRACT_RATIO)) {
                        m.path = prevDir;
                        m.actualDist = prevADist;
                        m.dist = prevDDist;
                        m.depth = prevDepth;
                        m.lost = prevLost;
                        m.gained = prevGained;
                        m.score = prevScore;
                        m.extra = prevExtra;
                    }
                    else {
                        modifiedPaths++;
                        if (best.score <= m.score || m.equals(best.endCell)) {
                            best.endCell = m;
                            best.score = m.score;
                            best.actualDist = m.actualDist;
                            best.depth = m.depth;
                        }
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
        return finishSearch(s, map, best);
    }

    private static Direction[] finishSearch(Ship s, GameMap map, Best best) {
        if (best.endCell != null) {
            best.endCell.actualDist = best.actualDist;
            Log.log("Best goal at " + best.toString() + " with score " + best.score);
            return extractPath(map, s, best.endCell);
        }
        return new Direction[] { Direction.STILL};
    }

    private static Direction[] extractPath(GameMap map, Ship s, MapCell curr) {
        Direction[] directions = new Direction[(curr.actualDist == 0) ? 1 : curr.actualDist];
        for (int i = 0; i < directions.length; i++) {
            directions[i] = Direction.STILL;
        }

        while (curr.dist != 0) {
            directions[curr.dist - 1] = curr.path;
            curr = map.offset(curr, curr.path.invertDirection());
        }
         return directions;
    }

    private static class Best {
        MapCell endCell = null;
        int score = Integer.MIN_VALUE;
        int actualDist = 0;
        int depth = 0;
    }

}
