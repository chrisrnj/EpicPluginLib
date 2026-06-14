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
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public interface TaskFactory
{
    /**
     * A task scheduler for tasks run in the global main thread.
     */
    interface Global extends TaskFactory
    {
        /**
         * Runs the task after the delay in ticks has passed.
         *
         * @param delay    The delay in ticks to wait.
         * @param runnable The task to run.
         * @return A scheduled instance depicting the characteristics of this task.
         */
        @NotNull Scheduled delayed(long delay, @NotNull Consumer<Scheduled> runnable);

        /**
         * Runs the task infinitely in a loop until cancelled.
         *
         * @param delay    The delay in ticks to wait before the first call.
         * @param repeat   The time in ticks to wait between calls.
         * @param runnable The task to run.
         * @return A scheduled instance depicting the characteristics of this task.
         */
        @NotNull Scheduled repeating(long delay, long repeat, @NotNull Consumer<Scheduled> runnable);
    }

    /**
     * A task scheduler for tasks run async from the global main thread.
     */
    interface Async extends Global
    {
        @NotNull Scheduled delayed(long delay, @NotNull Consumer<Scheduled> runnable);
    }

    /**
     * A task scheduler for tasks run in a thread specific to a location in a world.
     *
     * @param <W> The world type.
     */
    interface Local<W> extends TaskFactory
    {
        /**
         * Runs the task after the delay in ticks has passed in the thread of the specified location.
         *
         * @param world    The world.
         * @param chunkX   The X coordinate of the chunk.
         * @param chunkZ   The Z coordinate of the chunk.
         * @param delay    The delay in ticks to wait.
         * @param runnable The task to run.
         * @return A scheduled instance depicting the characteristics of this task.
         */
        @NotNull Scheduled delayed(@NotNull W world, int chunkX, int chunkZ, long delay, @NotNull Consumer<Scheduled> runnable);

        /**
         * Runs the task in the thread of the specified location infinitely in a loop until cancelled.
         *
         * @param world    The world.
         * @param chunkX   The X coordinate of the chunk.
         * @param chunkZ   The Z coordinate of the chunk.
         * @param delay    The delay in ticks to wait before the first call.
         * @param repeat   The time in ticks to wait between calls.
         * @param runnable The task to run.
         * @return A scheduled instance depicting the characteristics of this task.
         */
        @NotNull Scheduled repeating(@NotNull W world, int chunkX, int chunkZ, long delay, long repeat, @NotNull Consumer<Scheduled> runnable);
    }

    /**
     * A task scheduler for tasks run in a thread specific for an entity.
     *
     * @param <E> The entity type.
     */
    interface Entity<E> extends TaskFactory
    {
        /**
         * Runs the task after the delay in ticks has passed in the thread of the specified entity.
         *
         * @param entity   The entity to obtain the thread.
         * @param delay    The delay in ticks to wait.
         * @param runnable The task to run.
         * @param retired  A runnable to run if the entity dies before the task is executed.
         * @return A scheduled instance depicting the characteristics of this task.
         */
        @NotNull Scheduled delayed(@NotNull E entity, long delay, @NotNull Consumer<Scheduled> runnable, @Nullable Runnable retired);

        /**
         * Runs the task in the thread of the specified entity infinitely in a loop until cancelled.
         *
         * @param entity   The entity to obtain the thread.
         * @param delay    The delay in ticks to wait before the first call.
         * @param repeat   The time in ticks to wait between calls.
         * @param runnable The task to run.
         * @param retired  A runnable to run if the entity dies before the task is executed.
         * @return A scheduled instance depicting the characteristics of this task.
         */
        @NotNull Scheduled repeating(@NotNull E entity, long delay, long repeat, @NotNull Consumer<Scheduled> runnable, @Nullable Runnable retired);
    }
}
