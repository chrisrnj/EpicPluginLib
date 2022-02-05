/*
 * EpicPluginLib - Library with basic utilities for bukkit plugins.
 * Copyright (C) 2021  Christiano Rangel
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

import com.epicnicity322.epicpluginlib.core.tools.Version;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public final class CommandHandler implements CommandExecutor, TabCompleter
{
    //Tab completions are already sorted in 1.13+
    private static final boolean sortCompletions = new Version(Bukkit.getBukkitVersion().substring(0, Bukkit.getBukkitVersion().indexOf("-"))).compareTo(new Version("1.13")) < 0;
    private final @NotNull Collection<Command> subCommands;
    private final @Nullable CommandRunnable onDescription;
    private final @Nullable CommandRunnable onUnknownCommand;

    CommandHandler(@NotNull Collection<Command> subCommands, @Nullable CommandRunnable onDescription, @Nullable CommandRunnable onUnknownCommand)
    {
        this.subCommands = subCommands;
        this.onDescription = onDescription;
        this.onUnknownCommand = onUnknownCommand;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull org.bukkit.command.Command command, @NotNull String label, String[] args)
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
    public @NotNull List<String> onTabComplete(@NotNull CommandSender sender, @NotNull org.bukkit.command.Command command, @NotNull String label, @NotNull String[] args)
    {
        ArrayList<String> list = new ArrayList<>();

        if (args.length == 1) {
            for (Command subCommand : subCommands) {
                String permission = subCommand.getPermission();

                if (permission == null || sender.hasPermission(permission)) {
                    String name = subCommand.getName();

                    if (name.startsWith(args[0])) list.add(name);

                    String[] aliases = subCommand.getAliases();

                    if (aliases != null) {
                        for (String alias : subCommand.getAliases()) {
                            if (alias != null && alias.startsWith(args[0])) {
                                list.add(alias);
                            }
                        }
                    }
                }
            }
        } else {
            Command libCommand = findCommand(args[0]);

            if (libCommand != null) {
                String permission = libCommand.getPermission();

                if (permission == null || sender.hasPermission(permission)) {
                    TabCompleteRunnable tabCompleteRunnable = libCommand.getTabCompleteRunnable();

                    if (tabCompleteRunnable != null)
                        tabCompleteRunnable.run(list, label, sender, args);
                }
            }
        }

        if (sortCompletions) Collections.sort(list);
        return list;
    }
}
