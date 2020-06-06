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
