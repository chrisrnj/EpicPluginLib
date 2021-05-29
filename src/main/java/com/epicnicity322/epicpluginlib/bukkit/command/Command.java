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

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class Command implements CommandRunnable
{
    /**
     * @return The name of the command.
     */
    public abstract @NotNull String getName();

    /**
     * The alternative commands that can trigger this Command.
     *
     * @return The aliases of this command. Null if this command has no aliases.
     */
    public @Nullable String[] getAliases()
    {
        return null;
    }

    /**
     * The permission the sender needs to send or tab-complete the command.
     *
     * @return The permission required for this command. Null if this command has no permission.
     */
    public @Nullable String getPermission()
    {
        return null;
    }

    /**
     * The minimum of arguments this command need to be executed.
     *
     * @return The minimum of arguments this command has.
     */
    public int getMinArgsAmount()
    {
        return 1;
    }

    /**
     * If this command should have the same case in the name and aliases to be triggered.
     *
     * @return If this command is case sensitive.
     */
    public boolean isCaseSensitive()
    {
        return false;
    }

    /**
     * The runnable that will run when the sender sends this command and doesn't has the permission
     * {@link #getPermission()}.
     *
     * @return The runnable to run when sender doesn't has the permission. Null if nothing should happen.
     */
    protected @Nullable CommandRunnable getNoPermissionRunnable()
    {
        return null;
    }

    /**
     * The runnable that will run when the sender sends this command and the number of arguments is smaller than
     * {@link #getMinArgsAmount()}.
     *
     * @return The runnable to run when this command was sent with not enough arguments. Null if nothing should happen.
     */
    protected @Nullable CommandRunnable getNotEnoughArgsRunnable()
    {
        return null;
    }

    /**
     * The runnable that will run when the sender tries to auto complete the arguments of this sub-command, only the
     * arguments, not the command. The name in {@link #getName()} is auto completed automatically by this library.
     *
     * @return The runnable to run when the arguments of this command should be auto completed. Null if nothing should
     * happen.
     */
    protected @Nullable CommandRunnable getTabCompleteRunnable()
    {
        return null;
    }
}
