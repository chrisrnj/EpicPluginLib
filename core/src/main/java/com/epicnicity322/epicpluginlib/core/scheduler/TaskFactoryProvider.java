/*
 * EpicPluginLib - Library with basic utilities for Minecraft plugins.
 * Copyright (C) 2026 Christiano Rangel
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

package com.epicnicity322.epicpluginlib.core.scheduler;

import org.jetbrains.annotations.NotNull;

/**
 * An interface used to obtain different {@link TaskFactory}.
 *
 * @param <W> Platform's World class
 * @param <E> Platform's Entity class
 */
public interface TaskFactoryProvider<W, E>
{
    /**
     * Obtains a global task factory, which will run tasks on the server's main thread.
     *
     * @return A global task factory.
     * @see TaskFactory.Global
     */
    @NotNull TaskFactory.Global global();

    /**
     * Obtains an async task factory, which will run tasks on asynchronous from the server's main thread.
     *
     * @return An async task factory.
     * @see TaskFactory.Async
     */
    @NotNull TaskFactory.Async async();

    /**
     * Obtains a local task factory, which will run tasks on a specific chunk's thread.
     *
     * @return A local task factory.
     * @see TaskFactory.Local
     */
    @NotNull TaskFactory.Local<W> local();

    /**
     * Obtains an entity task factory, which will run tasks on an entity's designated thread.
     *
     * @return An entity task factory.
     * @see TaskFactory.Entity
     */
    @NotNull TaskFactory.Entity<E> entity();
}
