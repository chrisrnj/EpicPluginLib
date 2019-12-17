package com.epicnicity322.epicpluginlib.command;

import com.sun.istack.internal.Nullable;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.HashMap;
import java.util.HashSet;

public class CommandHandler implements CommandExecutor
{
    private HashMap<String, Command> subCommands;
    private Command description;

    private CommandHandler(HashSet<Command> subCommands, Command description)
    {
        this.subCommands = new HashMap<>();

        for (Command s : subCommands) {
            this.subCommands.put(s.getName(), s);
        }

        this.description = description;
    }

    public static CommandHandler getInstance(HashSet<Command> subCommands)
    {
        return new CommandHandler(subCommands, null);
    }

    public static CommandHandler getInstance(HashSet<Command> subCommands, @Nullable Command description)
    {
        return new CommandHandler(subCommands, description);
    }

    @Override
    public boolean onCommand(CommandSender commandSender, org.bukkit.command.Command command, String s, String[] strings)
    {
        if (strings.length == 0) {
            if (description != null) {
                description.onCommand(s, commandSender, strings);
            }
            return true;
        }

        Command command1 = findSubCommand(strings[0]);

        if (command1 != null) {
            if (strings.length >= command1.minArgsAmount()) {
                command1.onCommand(s, commandSender, strings);
            } else {
                command1.onNotEnoughArgs(s, commandSender, strings);
            }

            return true;
        }

        return false;
    }

    /**
     * Loops through all sub commands and checks if one has the same name as the argument. If so, then it checks if the
     * sub command is case sensitive and checks again if the argument matches case.
     *
     * @param arg The argument to check.
     * @return The command if the argument matches.
     */
    private Command findSubCommand(String arg)
    {
        for (String cmdName : subCommands.keySet()) {
            Command command = subCommands.get(cmdName);

            if (cmdName.equalsIgnoreCase(arg)) {
                if (command.isCaseSensitive()) {
                    if (cmdName.equals(arg)) {
                        return command;
                    }
                } else {
                    return command;
                }
            }
        }

        return null;
    }
}
