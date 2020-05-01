package com.epicnicity322.epicpluginlib.bukkit.command;

import org.bukkit.command.PluginCommand;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashSet;

public final class CommandManager
{
    private CommandManager()
    {
    }

    /**
     * Register a {@link PluginCommand} executor and tab completer and handles this command's arguments by using
     * {@link Command}.
     *
     * @param mainCommand The command saved in your plugin.yml that you wanna register.
     * @param subCommands The arguments to be called when the sender uses them.
     */
    public static void registerCommand(@NotNull PluginCommand mainCommand, @NotNull HashSet<Command> subCommands)
    {
        registerCommand(mainCommand, subCommands, null, null);
    }

    /**
     * Register a {@link PluginCommand} executor and tab completer and handles this command's arguments by using
     * {@link Command}.
     *
     * @param mainCommand            The command saved in your plugin.yml that you wanna register.
     * @param subCommands            The arguments to be called when the sender uses them.
     * @param descriptionRunnable    When the command has 0 arguments, this will run.
     * @param unknownCommandRunnable When the command could not be found, this will run.
     * @throws IllegalArgumentException If @param subCommands is empty.
     */
    public static void registerCommand(@NotNull PluginCommand mainCommand, @NotNull Collection<Command> subCommands,
                                       @Nullable CommandRunnable descriptionRunnable,
                                       @Nullable CommandRunnable unknownCommandRunnable)
    {
        if (subCommands.isEmpty())
            throw new IllegalArgumentException("This command has no arguments");

        CommandHandler handler = new CommandHandler(subCommands, descriptionRunnable, unknownCommandRunnable);

        mainCommand.setExecutor(handler);
        mainCommand.setTabCompleter(handler);
    }
}
