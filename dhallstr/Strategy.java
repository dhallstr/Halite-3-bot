package dhallstr;

import hlt.*;

import java.util.ArrayList;
import java.util.Arrays;

public class Strategy {
    public static boolean IS_TWO_PLAYER = true, PREVENT_TIMEOUT_MODE = false, LOW_ON_TIME = false;
    private static int lastDropoffBuilt = 0;

    private static DropoffCreation nextDropoff = null;

    public static Command evaluateMove(Game game, Ship ship, PlannedLocations plan, ArrayList<Command> commands) {

        Log.log("moving ship " + ship.id.toString());
        if (ship.processed) return null;
        ship.processed = true;
        Direction plannedMove = plan.getNextStep(game.gameMap, ship, 0);
        Intent intent = plan.shipPlans.get(ship.id);
        Intent nextIntent = plan.getIntent(game.gameMap, ship, 0);

        if (nextIntent != null && nextIntent != Intent.NONE && intent != nextIntent) {
            Log.log("Setting the next intent.");
            plan.shipPlans.put(ship.id, nextIntent);
        }

        Log.log("D: " + plannedMove);


        // *** RETURN HOME END GAME ***
        if (intent == Intent.CRASH_HOME || game.turnNumber + 5 + game.gameMap.calculateDistanceToDropoff(game.me, ship) * 1.2 + 2 * game.me.ships.size() / 24 > Constants.MAX_TURNS) {
            if (intent != Intent.CRASH_HOME || plannedMove == null || (plannedMove != Direction.STILL && ship.halite < game.gameMap.at(ship).moveCost()) ||
                    !plan.isSafe(game, ship.directionalOffset(plannedMove), ship, 1, false)) {
                plan.cancelPlan(game.gameMap, ship, 1);
                return returnHome(game, ship, plan, commands, plannedMove == null);
            }
            else if (game.gameMap.at(ship).hasStructure() && ship.owner.equals(game.gameMap.at(ship).structure.owner)) {
                return ship.stayStill();
            }
            else
                return ship.move(plannedMove);
        }



        // *** BUILD DROPOFFS ***
        if (nextDropoff != null && nextDropoff.builder.id == ship.id.id) {
            if (nextDropoff.equals(ship) && lastDropoffBuilt != game.turnNumber && game.me.halite >= Constants.DROPOFF_COST - ship.halite - game.gameMap.at(ship).halite) {
                game.me.halite -= Constants.DROPOFF_COST - ship.halite - game.gameMap.at(ship).halite;
                lastDropoffBuilt = game.turnNumber;
                nextDropoff = null;
                return ship.makeDropoff();
            }
            else if (nextDropoff.equals(ship)) {
                resolveCancelledMove(game, ship, plan, commands, new ArrayList<>());
                return ship.stayStill();
            }
        }



        // *** use the move that was planned ahead ***
        if (LOW_ON_TIME && plannedMove != null && plan.isSafe(game, ship.directionalOffset(plannedMove), ship, 1, true) &&
                !(game.gameMap.at(ship).hasStructure() && game.gameMap.at(ship).structure.owner == plan.me && plannedMove == Direction.STILL) &&
                (ship.halite >= game.gameMap.at(ship).moveCost() || plannedMove == Direction.STILL) &&
                !(intent == Intent.GATHER && plannedMove == Direction.STILL && ship.halite == Constants.MAX_HALITE)) {
            return ship.move(plannedMove);
        }


        // *** create a new goal ***

        Goal g = null;
        if (nextDropoff != null && nextDropoff.builder.id == ship.id.id) {
            g = new BuildDropoffGoal(nextDropoff);
        }
        else if (ship.halite > Magic.START_DELIVER_HALITE ||
                (intent == Intent.DROPOFF && ship.halite > Magic.MIN_HALITE_FOR_DELIVER) ||
                (game.gameMap.haliteOnMap < Magic.END_GAME_HALITE  * game.gameMap.width * game.gameMap.height && ship.halite > Magic.END_GAME_DELIVER_HALITE)) {
            g = new DropoffGoal(plan.me, false);
            Log.log("Dropping off now!");
        }
        else if (ship.halite >= game.gameMap.at(ship).moveCost()) {
            g = new TerrainGoal(10, Magic.SEARCH_DEPTH);
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
        ArrayList<Ship> needToRecalculate = new ArrayList<>();
        if (path[0] == Direction.STILL && plannedMove != Direction.STILL) {
            resolveCancelledMove(game, ship, plan, commands, needToRecalculate);
            ship.processed = true;
        }
        plan.addPlan(game.gameMap, ship, path, g == null ? Intent.NONE : g.getIntent());

        for (Ship s: needToRecalculate) {
            if (!s.processed) {
                Command c = Strategy.evaluateMove(game, s, plan, commands);
                if (c != null) commands.add(c);
            }
        }

        nextIntent = plan.getIntent(game.gameMap, ship, 0);
        if (nextIntent != null && nextIntent != Intent.NONE && intent != nextIntent && nextIntent != Intent.BUILD_DROPOFF) {
            Log.log("Setting the next intent.");
            plan.shipPlans.put(ship.id, nextIntent);
        }

        return ship.move(path[0]);
    }

    private static void resolveCancelledMove(Game game, Ship ship, PlannedLocations plan, ArrayList<Command> commands, ArrayList<Ship> needToRecalculate) {
        Log.log("resolving " + ship.id + "...");
        EntityId here = plan.get(game.gameMap, ship, 1);
        plan.cancelPlan(game.gameMap, ship, 0);
        plan.addPlan(game.gameMap, ship, new Direction[] {Direction.STILL}, Intent.NONE);
        if (here == null || ship.id.id == here.id) return;


        if (here != ship.id) {
            // Need to cancel this ship's move, if it was made already
            Ship newShip = game.me.ships.get(here);
            if (newShip != null && newShip.processed) {
                Command.cancelCommand(commands, here);
                newShip.processed = false;
                resolveCancelledMove(game, newShip, plan, commands, needToRecalculate);
                needToRecalculate.add(newShip);
            }
        }

    }


    public static boolean shouldSpawn(Game game, PlannedLocations plan, Player me, GameMap gameMap, ArrayList<Command> commandQueue) {
        return (game.turnNumber <= (Strategy.IS_TWO_PLAYER ? Constants.MAX_TURNS - 85 : Constants.MAX_TURNS - 85) &&
                (((game.gameMap.haliteOnMap - (game.gameMap.width * game.gameMap.height) *(IS_TWO_PLAYER ? 6 : 3))) / (1000 + (IS_TWO_PLAYER ? 10 : 7) * game.totalShips) > game.me.ships.size())) &&
                me.halite >= Constants.SHIP_COST &&
                (nextDropoff == null || game.me.halite >= Constants.DROPOFF_COST - game.gameMap.at(nextDropoff).halite + Constants.SHIP_COST) &&
                isSpawnSafe(game, me, plan, commandQueue);
    }

    private static boolean isSpawnSafe(Game game, Player me, PlannedLocations plan, ArrayList<Command> commands) {
        return plan.isSafe(game, me.shipyard, new Ship(me.id, EntityId.NONE, me.shipyard.x, me.shipyard.y, 0), 1, false)&&
                (game.gameMap.at(me.shipyard).ship == null || !game.gameMap.at(me.shipyard).ship.owner.equals(me.id) ||
                        (shipIsMoving(game.gameMap.at(me.shipyard).ship.id, commands)));
    }

    public static void adjustDropoffGoal(Game game) {
        nextDropoff = DropoffCreation.findBestPosition(game);
        if (nextDropoff != null) {
            nextDropoff.findBestShip(game);
            Log.log("Started Dropoff, requesting ship " + nextDropoff.builder.id);
        }
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

    private static Command returnHome(Game game, Ship ship, PlannedLocations plan, ArrayList<Command> commands, boolean cancelIfStill) {
        Direction[] path = Navigation.bfs(game, ship, new DropoffGoal(plan.me, true), plan);
        if (path == null || path.length == 0) {
            path = new Direction[] {Direction.STILL};
            resolveCancelledMove(game, ship, plan, commands, new ArrayList<Ship>());
        }
       else if (path[0] == Direction.STILL && cancelIfStill)
            resolveCancelledMove(game, ship, plan, commands, new ArrayList<Ship>());
        plan.addPlan(game.gameMap, ship, path, Intent.CRASH_HOME);
        return ship.move(path[0]);
    }
}
