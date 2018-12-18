package hlt;

import java.util.ArrayList;

public class Command {
    public final String command;

    public final EntityId id;

    public static Command spawnShip() {
        return new Command("g", null);
    }

    public static Command transformShipIntoDropoffSite(final EntityId id) {
        return new Command("c " + id, id);
    }

    public static Command move(final EntityId id, final Direction direction) {
        return new Command("m " + id + ' ' + direction.charValue, id);
    }
    public static void cancelCommand(ArrayList<Command> commands, EntityId id) {
        for (int i = commands.size() - 1; i >= 0; i--) {
            if (id.equals(commands.get(i).id)) {
                commands.remove(i);
            }
        }
    }

    public boolean isMovingAway() {
        return command.startsWith("m") && !command.endsWith("o");
    }


    private Command(final String command, EntityId id) {
        this.command = command;
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Command command1 = (Command) o;

        return command.equals(command1.command);
    }

    @Override
    public int hashCode() {
        return command.hashCode();
    }
}
