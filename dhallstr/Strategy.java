package dhallstr;

import hlt.*;

import java.util.ArrayList;
import java.util.Arrays;

public class Strategy {
    public static boolean IS_TWO_PLAYER = true, COLLISIONS_DISABLED = false;
    public static int lastDropoffBuilt = 0;

    public static Command evaluateMove(Game game, Ship ship, PlannedLocations plan, ArrayList<Command> commands) {

        Log.log("moving ship " + ship.id.toString());
        Direction plannedMove = plan.getNextStep(game.gameMap, ship, 0);
        Intent intent = plan.shipPlans.get(ship.id);
        Log.log("D: " + plannedMove);


        // *** RETURN HOME END GAME ***
        if (intent == Intent.CRASH_HOME || game.turnNumber + 3 + game.gameMap.calculateDistanceToDropoff(game.me, ship.position) * 1.2 + 2 * game.me.ships.size() / 24 > Constants.MAX_TURNS) {
            if (intent != Intent.CRASH_HOME || plannedMove == null) {
                plan.cancelPlan(game.gameMap, ship, 1);
                return returnHome(game, ship, plan, commands, plannedMove == null);
            }
            else
                return ship.move(plannedMove);//returnHome(game, ship, plan, commands, plannedMove == null);//ship.move(plannedMove);
        }
        else if (intent == Intent.CRASH_HOME && game.gameMap.at(ship).hasStructure() && ship.owner.equals(game.gameMap.at(ship).structure.owner)) {
            return ship.stayStill();
        }


        // *** BUILD DROPOFFS ***
        if (intent == null || intent == Intent.NONE || intent == Intent.GATHER || intent == Intent.BUILD_DROPOFF) {
            if (game.gameMap.calculateDistanceToDropoff(game.me, ship.position) >= Magic.MIN_DIST_FOR_BUILD && game.gameMap.numHaliteWithin(ship.position, Magic.BUILD_DROPOFF_RADIUS) >= Magic.MIN_HALITE_FOR_BUILD &&
                    game.me.ships.size() >= game.me.dropoffs.size() * Magic.SHIPS_PER_DROPOFF && game.turnNumber + Magic.MIN_TURNS_LEFT_FOR_DROPOFF < Constants.MAX_TURNS &&
                    game.gameMap.getNumMyShipsWithin(ship.position, Magic.DROPOFF_FRIENDLY_SHIP_RADIUS, game.me.id) >= Magic.MIN_FRIENDLY_AROUND_FOR_DROPOFF &&
                    game.me.dropoffs.size() < Magic.MAX_DROPOFFS) {
                if (lastDropoffBuilt != game.turnNumber && game.me.halite >= Constants.DROPOFF_COST - ship.halite - game.gameMap.at(ship.position).halite + Magic.BUILD_BUFFER_HALITE) {
                    game.me.halite -= Constants.DROPOFF_COST - ship.halite - game.gameMap.at(ship.position).halite;
                    lastDropoffBuilt = game.turnNumber;
                    return ship.makeDropoff();
                }
            }
            else if (intent == Intent.BUILD_DROPOFF) {
                intent = Intent.NONE;
                plan.shipPlans.put(ship.id, intent);
            }
        }


        // *** use the move that was planned ahead ***
        if (plannedMove != null && plan.isSafe(game.gameMap, ship.position.directionalOffset(plannedMove), ship, 1, COLLISIONS_DISABLED) &&
                !(game.gameMap.at(ship).hasStructure() && game.gameMap.at(ship).structure.owner == plan.me) &&
                ship.halite >= game.gameMap.at(ship).moveCost()) {
            return ship.move(plannedMove);
        }
        else if (plannedMove != null && !plan.isSafe(game.gameMap, ship.position.directionalOffset(plannedMove), ship, 1, COLLISIONS_DISABLED)) {
            resolveCancelledMove(game, ship, plan, commands);
        }


        // *** create a new goal ***
        Goal g = null;
        if (ship.halite > Magic.START_DELIVER_HALITE && ship.halite + Magic.MIN_BACK_TO_DROPOFF_WAIT_HALITE < Constants.MAX_HALITE && game.gameMap.at(ship).halite > (Constants.MAX_HALITE - ship.halite - Magic.MIN_BACK_TO_DROPOFF_WAIT_HALITE) * Constants.EXTRACT_RATIO) {
            g = null;
        }
        else if (ship.halite > Magic.START_DELIVER_HALITE || (ship.halite > Magic.END_DELIVER_HALITE && intent == Intent.DROPOFF) ||
                  (game.gameMap.haliteOnMap < Magic.END_GAME_HALITE  * game.gameMap.width * game.gameMap.height && ship.halite > Magic.END_GAME_DELIVER_HALITE)) {
            g = new DropoffGoal(plan.me, false);
        }
        else if (game.gameMap.at(ship).halite < Magic.COLLECT_DOWN_TO || (game.gameMap.at(ship).hasStructure() && game.gameMap.at(ship).structure.owner == plan.me)) {
            g = new TerrainGoal(10, 30, ship);
        }

        // *** pathfind based on goal ***
        Direction[] path;
        if (g != null) {
            long time = System.currentTimeMillis();
            path = Navigation.bfs(game, ship, g, plan);
            time
                    = System.currentTimeMillis() - time;
            Log.log("Navigation time: " + time);
        }
        else {
            path = new Direction[] {Direction.STILL};
        }
        Log.log(Arrays.toString(path));
        if (path[0] == Direction.STILL && plannedMove == null) {
            resolveCancelledMove(game, ship, plan, commands);
        }
        plan.addPlan(game.gameMap, ship, path, g == null ? Intent.NONE : g.getIntent());

        return ship.move(path[0]);
    }

    private static void resolveCancelledMove(Game game, Ship ship, PlannedLocations plan, ArrayList<Command> commands) {
        Log.log("resolving " + ship.id + "...");
        EntityId here = plan.get(game.gameMap, ship.position, 1);
        plan.cancelPlan(game.gameMap, ship, 1);
        plan.addPlan(game.gameMap, ship, new Direction[] {Direction.STILL}, Intent.NONE);
        if (here == null || ship.id.equals(here)) return;


        if (here != ship.id) {
            // Need to cancel this ship's move, if it was made already
            Ship newShip = game.me.ships.get(here);
            if (newShip != null) {
                Command.cancelCommand(commands, here);
                resolveCancelledMove(game, newShip, plan, commands);
            }
        }

    }


    public static boolean shouldSpawn(Game game, PlannedLocations plan, Player me, GameMap gameMap, ArrayList<Command> commandQueue) {
        return (game.turnNumber <= (Strategy.IS_TWO_PLAYER ? Constants.MAX_TURNS - 150 : Constants.MAX_TURNS - 150) &&
                (((game.gameMap.haliteOnMap - (game.gameMap.width * game.gameMap.height) *(Strategy.IS_TWO_PLAYER ? 10 : 3))) / (1620 + 18 * game.totalShips) > game.me.ships.size())) &&
                me.halite >= Constants.SHIP_COST &&
                (game.turnNumber > 5 || me.ships.size() <= 4) &&
                isSpawnSafe(gameMap, me, plan, commandQueue);
    }

    public static boolean isSpawnSafe(GameMap gameMap, Player me, PlannedLocations plan, ArrayList<Command> commands) {
        return plan.isSafe(gameMap, me.shipyard.position, new Ship(me.id, EntityId.NONE, me.shipyard.position, 0), 1, false)&&
                (gameMap.at(me.shipyard).ship == null || !gameMap.at(me.shipyard).ship.owner.equals(me.id) ||
                        (shipIsMoving(gameMap.at(me.shipyard).ship.id, commands)));
    }

    private static boolean shipIsMoving(EntityId id, ArrayList<Command> commands) {
        if (id == null) return false;
        for (Command c: commands) {
            if (id.equals(c.id)) {
                return c.isMovingAway();
            }
        }
        return false;
    }

    public static boolean shouldDisableCollisions(Game game) {
        return !IS_TWO_PLAYER || (game.getEnemyShips() > game.me.ships.size());
    }

    private static Command returnHome(Game game, Ship ship, PlannedLocations plan, ArrayList<Command> commands, boolean cancelIfStill) {
        Direction[] path = Navigation.bfs(game, ship, new DropoffGoal(plan.me, true), plan);
        if (path == null || path.length == 0) {
            path = new Direction[] {Direction.STILL};
            resolveCancelledMove(game, ship, plan, commands);
        }
       /* else if (path[0] == Direction.STILL)
            resolveCancelledMove(game, ship, plan, commands);*/
        plan.addPlan(game.gameMap, ship, path, Intent.CRASH_HOME);
        return ship.move(path[0]);
    }
}
