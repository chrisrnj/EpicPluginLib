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

package com.epicnicity322.epicpluginlib.sponge.scheduler;


import com.epicnicity322.epicpluginlib.core.scheduler.Scheduled;
import com.epicnicity322.epicpluginlib.core.scheduler.TaskFactory;
import com.epicnicity322.epicpluginlib.core.scheduler.TaskFactoryProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.api.Game;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.scheduler.ScheduledTask;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.util.Ticks;
import org.spongepowered.api.world.World;
import org.spongepowered.plugin.PluginContainer;

import java.util.UUID;
import java.util.function.Consumer;

/**
 * A Sponge-specific implementation of a task factory.
 * <p>
 * <b>Available Schedulers:</b>
 * <ul>
 * <li>{@link #global()} — Executes tasks on the primary server thread.</li>
 * <li>{@link #async()} — Executes tasks asynchronously from the primary thread.</li>
 * </ul>
 * {@link #local()} and {@link #entity()} will wrap around {@link #global()}.
 */
public final class SpongeTaskFactory implements TaskFactoryProvider<World<?, ?>, Entity>
{
    public final @NotNull PluginContainer plugin;

    public SpongeTaskFactory(@NotNull PluginContainer plugin)
    {
        this.plugin = plugin;
    }

    @Override
    public @NotNull TaskFactory.Global global()
    {
        return new TaskFactory.Global()
        {
            /**
             * {@inheritDoc}
             * @throws IllegalStateException If {@link Game#server()} is not currently available.
             */
            @Override
            public @NotNull Scheduled delayed(long delay, @NotNull Consumer<Scheduled> runnable)
            {
                if (delay < 0) delay = 0;
                SpongeScheduled scheduled = new SpongeScheduled();

                scheduled.task = Sponge.server().scheduler().submit(Task.builder().plugin(plugin).delay(Ticks.of(delay)).execute(task -> {
                    try {
                        runnable.accept(scheduled);
                    } finally {
                        scheduled.done = true;
                    }
                }).build());

                return scheduled;
            }

            /**
             * {@inheritDoc}
             * @throws IllegalStateException If {@link Game#server()} is not currently available.
             */
            @Override
            public @NotNull Scheduled repeating(long delay, long repeat, @NotNull Consumer<Scheduled> runnable)
            {
                SpongeScheduled scheduled = new SpongeScheduled();

                scheduled.task = Sponge.server().scheduler().submit(Task.builder().plugin(plugin).delay(Ticks.of(delay)).interval(Ticks.of(repeat)).execute(task -> runnable.accept(scheduled)).build());

                return scheduled;
            }
        };
    }

    /**
     * A task scheduler for tasks run async from the global main thread.
     */
    @Override
    public @NotNull TaskFactory.Async async()
    {
        return new TaskFactory.Async()
        {
            /**
             * {@inheritDoc}
             * @throws IllegalStateException If {@link Sponge#game()} is not currently available.
             */
            @Override
            public @NotNull Scheduled delayed(long delay, @NotNull Consumer<Scheduled> runnable)
            {
                if (delay < 0) delay = 0;
                SpongeScheduled scheduled = new SpongeScheduled();

                scheduled.task = Sponge.asyncScheduler().submit(Task.builder().plugin(plugin).delay(Ticks.of(delay)).execute(task -> {
                    try {
                        runnable.accept(scheduled);
                    } finally {
                        scheduled.done = true;
                    }
                }).build());

                return scheduled;
            }

            /**
             * {@inheritDoc}
             * @throws IllegalStateException If {@link Sponge#game()} is not currently available.
             */
            @Override
            public @NotNull Scheduled repeating(long delay, long repeat, @NotNull Consumer<Scheduled> runnable)
            {
                SpongeScheduled scheduled = new SpongeScheduled();

                scheduled.task = Sponge.asyncScheduler().submit(Task.builder().plugin(plugin).delay(Ticks.of(delay)).interval(Ticks.of(repeat)).execute(task -> runnable.accept(scheduled)).build());

                return scheduled;
            }
        };
    }

    /**
     * Obtains a local task factory that delegates execution to the global main thread scheduler.
     * <p>
     * The world and chunk parameters are ignored; tasks are scheduled globally rather than relative to the location.
     *
     * @return A local task factory backed by the global main thread scheduler.
     */
    @Override
    public @NotNull TaskFactory.Local<World<?, ?>> local()
    {
        TaskFactory.Global global = global();

        return new TaskFactory.Local<World<?, ?>>()
        {
            @Override
            public @NotNull Scheduled delayed(@NotNull World<?, ?> world, int chunkX, int chunkZ, long delay, @NotNull Consumer<Scheduled> runnable)
            {
                return global.delayed(delay, runnable);
            }

            @Override
            public @NotNull Scheduled repeating(@NotNull World<?, ?> world, int chunkX, int chunkZ, long delay, long repeat, @NotNull Consumer<Scheduled> runnable)
            {
                return global.repeating(delay, repeat, runnable);
            }
        };
    }

    /**
     * Obtains an entity task factory that delegates execution to the global main thread scheduler.
     * <p>
     * The entity parameter is ignored; tasks are scheduled globally rather than relative to the entity.
     *
     * @return An entity task factory backed by the global main thread scheduler.
     */
    @Override
    public @NotNull TaskFactory.Entity<Entity> entity()
    {
        TaskFactory.Global global = global();

        return new TaskFactory.Entity<Entity>()
        {
            @Override
            public @NotNull Scheduled delayed(@NotNull org.spongepowered.api.entity.Entity entity, long delay, @NotNull Consumer<Scheduled> runnable, @Nullable Runnable retired)
            {
                return global.delayed(delay, runnable);
            }

            @Override
            public @NotNull Scheduled repeating(@NotNull org.spongepowered.api.entity.Entity entity, long delay, long repeat, @NotNull Consumer<Scheduled> runnable, @Nullable Runnable retired)
            {
                return global.repeating(delay, repeat, runnable);
            }
        };
    }

    private static final class SpongeScheduled implements Scheduled
    {
        private @Nullable ScheduledTask task;
        private volatile boolean done;

        @Override
        public void cancel()
        {
            if (task != null) task.cancel();
        }

        @Override
        public int bukkitId()
        {
            return 0;
        }

        @Override
        public UUID spongeId()
        {
            return task == null ? null : task.uniqueId();
        }

        @Override
        public boolean done()
        {
            return done || (task != null && task.isCancelled());
        }
    }
}
