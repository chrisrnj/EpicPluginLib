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

import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public final class CommandHandler implements CommandExecutor, TabCompleter
{
    private final Collection<Command> subCommands;
    private final CommandRunnable onDescription;
    private final CommandRunnable onUnknownCommand;

    protected CommandHandler(@NotNull Collection<Command> subCommands, @Nullable CommandRunnable onDescription,
                             @Nullable CommandRunnable onUnknownCommand)
    {
        this.subCommands = subCommands;
        this.onDescription = onDescription;
        this.onUnknownCommand = onUnknownCommand;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull org.bukkit.command.Command command,
                             @NotNull String label, String[] args)
    {
        if (args.length == 0) {
            if (onDescription != null)
                onDescription.run(label, sender, args);

            return true;
        }

        Command libCommand = findCommand(args[0]);

        if (libCommand == null) {
            if (onUnknownCommand != null)
                onUnknownCommand.run(label, sender, args);
        } else {
            String permission = libCommand.getPermission();

            if (permission == null || sender.hasPermission(permission)) {
                if (args.length >= libCommand.getMinArgsAmount()) {
                    libCommand.run(label, sender, args);
                } else {
                    CommandRunnable notEnoughArgsRunnable = libCommand.getNotEnoughArgsRunnable();

                    if (notEnoughArgsRunnable != null)
                        notEnoughArgsRunnable.run(label, sender, args);
                }
            } else {
                CommandRunnable noPermissionRunnable = libCommand.getNoPermissionRunnable();

                if (noPermissionRunnable != null)
                    noPermissionRunnable.run(label, sender, args);
            }
        }

        return true;
    }

    /**
     * Loops through all sub commands and checks if the argument is equal to an alias of a command.
     *
     * @param arg The argument to check.
     * @return The command if the argument matches. Null if a command with this name was not found.
     */
    private Command findCommand(@NotNull String arg)
    {
        for (Command command : subCommands) {
            String name = command.getName();
            boolean caseSensitive = command.isCaseSensitive();

            if (caseSensitive ? arg.equals(name) : arg.equalsIgnoreCase(name)) {
                return command;
            } else {
                String[] aliases = command.getAliases();

                if (aliases != null)
                    for (String alias : aliases)
                        if (alias != null)
                            if (caseSensitive ? arg.equals(alias) : arg.equalsIgnoreCase(alias))
                                return command;
            }
        }

        return null;
    }

    @Override
    public @NotNull List<String> onTabComplete(@NotNull CommandSender sender,
                                               @NotNull org.bukkit.command.Command command, @NotNull String label,
                                               @NotNull String[] args)
    {
        List<String> list = new ArrayList<>();

        if (args.length == 1) {
            for (Command libCommand : subCommands) {
                String permission = libCommand.getPermission();

                if (permission == null || sender.hasPermission(permission)) {
                    String name = libCommand.getName();

                    if (name.startsWith(args[0]))
                        list.add(name);

                    String[] aliases = libCommand.getAliases();

                    if (aliases != null)
                        for (String alias : libCommand.getAliases())
                            if (alias != null)
                                if (alias.startsWith(args[0]))
                                    list.add(alias);
                }
            }
        } else if (args.length != 0) {
            Command libCommand = findCommand(args[0]);

            if (libCommand != null) {
                String permission = libCommand.getPermission();

                if (permission == null || sender.hasPermission(permission)) {
                    CommandRunnable tabCompleteRunnable = libCommand.getTabCompleteRunnable();

                    if (tabCompleteRunnable != null)
                        tabCompleteRunnable.run(label, sender, args);
                }
            }
        }

        return list;
    }
}
