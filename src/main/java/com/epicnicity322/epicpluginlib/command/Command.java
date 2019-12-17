package com.epicnicity322.epicpluginlib.command;

import org.bukkit.command.CommandSender;

public interface Command
{
    /**
     * @return The name of the command.
     */
    String getName();

    /**
     * The minimum of arguments this command need to be executed.
     *
     * @return The minimum of arguments.
     */
    default int minArgsAmount()
    {
        return 1;
    }

    /**
     * @return If this command is case sensitive.
     */
    default boolean isCaseSensitive()
    {
        return false;
    }

    /**
     * When the command is successfully sent by the user.
     *
     * @param label  The name of the main command.
     * @param sender The user.
     * @param args   The arguments sent by the user.
     */
    void onCommand(String label, CommandSender sender, String[] args);

    /**
     * When the command is sent by the user, but it does not match the number of arguments required.
     *
     * @param label  The name of the main command.
     * @param sender The user.
     * @param args   The arguments sent by the user.
     */
    default void onNotEnoughArgs(String label, CommandSender sender, String[] args)
    {
    }
}
