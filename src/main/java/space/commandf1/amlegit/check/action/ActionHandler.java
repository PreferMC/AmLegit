package space.commandf1.amlegit.check.action;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

public class ActionHandler {

    private final String[] commands;
    private final CommandSender commandSender;

    public ActionHandler(String[] commands, CommandSender commandSender) {
        this.commands = commands;
        this.commandSender = commandSender;
    }

    public void execute(Function<? super String, ? extends String> mapper) {
        Arrays.stream(commands).map(mapper).forEach(command -> Bukkit.dispatchCommand(this.commandSender, command));
    }

    public static class Builder {
        private final List<String> commands = new ArrayList<>();
        private CommandSender commandSender = null;

        public Builder executor(CommandSender sender) {
            this.commandSender = sender;
            return this;
        }

        public Builder commands(String... commands) {
            Arrays.stream(commands).forEach(this::command);
            return this;
        }

        public Builder commands(List<String> commands) {
            commands.forEach(this::command);
            return this;
        }

        @SuppressWarnings("UnusedReturnValue")
        public Builder command(String command) {
            commands.add(command);
            return this;
        }

        public ActionHandler build() {
            return new ActionHandler(commands.toArray(new String[0]),  commandSender);
        }
    }
}
