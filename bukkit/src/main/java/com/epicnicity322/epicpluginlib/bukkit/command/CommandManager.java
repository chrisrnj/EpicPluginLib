/*
 * EpicPluginLib - Library with basic utilities for bukkit plugins.
 * Copyright (C) 2025  Christiano Rangel
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.epicnicity322.epicpluginlib.bukkit.command;

import org.bukkit.command.PluginCommand;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public final class CommandManager
{
    private CommandManager()
    {
    }

    /**
     * Register a {@link PluginCommand} executor and tab completer and handles this command's arguments by using
     * {@link Command}.
     *
     * @param mainCommand The command saved in your plugin.yml that you want to register.
     * @param subCommands The arguments to be called when the sender uses them.
     */
    public static void registerCommand(@NotNull PluginCommand mainCommand, @NotNull Collection<? extends Command> subCommands)
    {
        registerCommand(mainCommand, subCommands, null, null);
    }

    /**
     * Register a {@link PluginCommand} executor and tab completer and handles this command's arguments by using
     * {@link Command}.
     *
     * @param mainCommand            The command saved in your plugin.yml that you want to register.
     * @param subCommands            The arguments to be called when the sender uses them.
     * @param descriptionRunnable    When the command has 0 arguments, this will run.
     * @param unknownCommandRunnable When the command could not be found, this will run.
     * @throws IllegalArgumentException If @param subCommands is empty.
     */
    public static void registerCommand(@NotNull PluginCommand mainCommand, @NotNull Collection<? extends Command> subCommands,
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
