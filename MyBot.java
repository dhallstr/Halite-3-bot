// This Java API uses camelCase instead of the snake_case as documented in the API docs.
//     Otherwise the names of methods are consistent.

import dhallstr.*;
import hlt.*;

import java.util.ArrayList;
import java.util.Arrays;

public class MyBot {
    public static void main(final String[] args) {

        Game game = new Game();
        // At this point "game" variable is populated with initial map data.
        // This is a good place to do computationally expensive start-up pre-processing.
        // As soon as you call "ready" function below, the 2 second per turn timer will start

        PlannedLocations plan = new PlannedLocations(game.gameMap.width, game.gameMap.height, game.me.id);
        Strategy.IS_TWO_PLAYER = game.players.size() <= 2;
        Magic.updateConstants(Strategy.IS_TWO_PLAYER, game.gameMap.width, game.gameMap.height);
        Direction.setAllCardinals(game.myId.id);
        Magic.commandLineParams(args);

        game.ready(Magic.BOT_NAME);

        Log.log("Successfully created bot! My Player ID is " + game.myId + ".");

        for (;;) {
            long startTurn = System.currentTimeMillis();
            game.updateFrame();

            final Player me = game.me;
            final GameMap gameMap = game.gameMap;
            Navigation.modifiedPaths = 0;
            Strategy.LOW_ON_TIME = false;

            ArrayList<Command> commandQueue = new ArrayList<>();
            Ship[] ships = new Ship[me.ships.values().size()];
            me.ships.values().toArray(ships);
            Arrays.sort(ships, (s1, s2) -> (s1.halite - s2.halite));
            Strategy.adjustDropoffGoal(game);

            for (final Ship ship: ships) {
                if (System.currentTimeMillis() - startTurn > 1950) {
                    break;
                }
                else if (System.currentTimeMillis() - startTurn > 1500) {
                    Strategy.PREVENT_TIMEOUT_MODE = true;
                    Strategy.LOW_ON_TIME = true;
                }
                Command newCommand = Strategy.evaluateMove(game, ship, plan, commandQueue);
                if (newCommand != null)
                    commandQueue.add(newCommand);
            }

            if (Strategy.shouldSpawn(game, plan, me, gameMap, commandQueue))
            {
                me.halite -= Constants.SHIP_COST;
                commandQueue.add(me.shipyard.spawn());
            }

            Log.log("Modified paths: " + Navigation.modifiedPaths);
            if (System.currentTimeMillis() - startTurn < 1000) Strategy.PREVENT_TIMEOUT_MODE = false;
            game.endTurn(commandQueue);
            plan.updateTurn(game.turnNumber);
        }
    }
}
