package com.epicnicity322.epicpluginlib.command;

import com.sun.istack.internal.Nullable;
import org.bukkit.command.PluginCommand;

import java.util.HashSet;

public class CommandManager
{
    /**
     * Sets the command executor for a command and handles the arguments by calling methods in {@link Command}.
     *
     * @param mainCommand The command saved in your plugin.yml that you wanna register.
     * @param subCommands The arguments to be called when the user uses them.
     * @see CommandManager#registerCommand(PluginCommand, HashSet, Command)
     */
    public static void registerCommand(PluginCommand mainCommand, HashSet<Command> subCommands)
    {
        mainCommand.setExecutor(CommandHandler.getInstance(subCommands));
    }

    /**
     * Sets the command executor for a command and handles the arguments by calling methods in {@link Command}.
     *
     * @param mainCommand The command saved in your plugin.yml that you wanna register.
     * @param subCommands The arguments to be called when the user uses them.
     * @param description When the command has 0 arguments, this will be called.
     */
    public static void registerCommand(PluginCommand mainCommand, HashSet<Command> subCommands, @Nullable Command description)
    {
        mainCommand.setExecutor(CommandHandler.getInstance(subCommands, description));
    }
}
