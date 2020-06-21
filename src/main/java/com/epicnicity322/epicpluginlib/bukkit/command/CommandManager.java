/*
 * Copyright (c) 2020 Christiano Rangel
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
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
     * @param mainCommand The command saved in your plugin.yml that you wanna register.
     * @param subCommands The arguments to be called when the sender uses them.
     */
    public static void registerCommand(@NotNull PluginCommand mainCommand, @NotNull Collection<Command> subCommands)
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
