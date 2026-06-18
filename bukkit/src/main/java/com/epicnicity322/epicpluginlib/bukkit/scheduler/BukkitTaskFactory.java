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

package com.epicnicity322.epicpluginlib.bukkit.scheduler;

import com.epicnicity322.epicpluginlib.core.scheduler.Scheduled;
import com.epicnicity322.epicpluginlib.core.scheduler.TaskFactory;
import com.epicnicity322.epicpluginlib.core.scheduler.TaskFactoryProvider;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.function.Consumer;

/**
 * A Spigot-specific implementation of a task factory.
 * <p>
 * This task factory uses the {@link BukkitRunnable} approach to schedule tasks, as it allows for a way to provide a
 * {@link Scheduled} instance to both the consumer parameter and return value of {@link TaskFactory.Global} methods.
 * <p>
 * <b>Available Schedulers:</b>
 * <ul>
 * <li>{@link #global()} — Executes tasks on the primary server thread.</li>
 * <li>{@link #async()} — Executes tasks asynchronously from the primary thread.</li>
 * </ul>
 * {@link #local()} and {@link #entity()} will wrap around {@link #global()}.
 *
 * @apiNote If you are developing plugins specifically for Paper (or its forks), prefer using {@link FoliaTaskFactory}.
 * Paper will internally handle schedulers to provide the same functionality as if you were running Folia.
 */
public final class BukkitTaskFactory implements TaskFactoryProvider<World, Entity>
{
    private final @NotNull Plugin plugin;

    public BukkitTaskFactory(@NotNull Plugin plugin)
    {
        this.plugin = plugin;
    }

    @Override
    public @NotNull TaskFactory.Global global()
    {
        return new TaskFactory.Global()
        {
            @Override
            public @NotNull Scheduled delayed(long delay, @NotNull Consumer<Scheduled> runnable)
            {
                BukkitRunnableWrapped bukkitRunnable = new BukkitRunnableWrapped()
                {
                    @Override
                    public void run()
                    {
                        try {
                            runnable.accept(scheduled);
                        } finally {
                            done = true;
                        }
                    }
                };
                bukkitRunnable.runTaskLater(plugin, delay);
                return bukkitRunnable.scheduled;
            }

            @Override
            public @NotNull Scheduled repeating(long delay, long repeat, @NotNull Consumer<Scheduled> runnable)
            {
                BukkitRunnableWrapped bukkitRunnable = new BukkitRunnableWrapped()
                {
                    @Override
                    public void run()
                    {
                        runnable.accept(scheduled);
                    }
                };
                bukkitRunnable.runTaskTimer(plugin, delay, repeat);
                return bukkitRunnable.scheduled;
            }
        };
    }

    @Override
    public @NotNull TaskFactory.Async async()
    {
        return new TaskFactory.Async()
        {
            @Override
            public @NotNull Scheduled delayed(long delay, @NotNull Consumer<Scheduled> runnable)
            {
                BukkitRunnableWrapped bukkitRunnable = new BukkitRunnableWrapped()
                {
                    @Override
                    public void run()
                    {
                        try {
                            runnable.accept(scheduled);
                        } finally {
                            done = true;
                        }
                    }
                };
                bukkitRunnable.runTaskLaterAsynchronously(plugin, delay);
                return bukkitRunnable.scheduled;
            }

            @Override
            public @NotNull Scheduled repeating(long delay, long repeat, @NotNull Consumer<Scheduled> runnable)
            {
                BukkitRunnableWrapped bukkitRunnable = new BukkitRunnableWrapped()
                {
                    @Override
                    public void run()
                    {
                        runnable.accept(scheduled);
                    }
                };
                bukkitRunnable.runTaskTimerAsynchronously(plugin, delay, repeat);
                return bukkitRunnable.scheduled;
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
    public @NotNull TaskFactory.Local<World> local()
    {
        TaskFactory.Global global = global();

        return new TaskFactory.Local<World>()
        {
            @Override
            public @NotNull Scheduled delayed(@NotNull World world, int chunkX, int chunkZ, long delay, @NotNull Consumer<Scheduled> runnable)
            {
                return global.delayed(delay, runnable);
            }

            @Override
            public @NotNull Scheduled repeating(@NotNull World world, int chunkX, int chunkZ, long delay, long repeat, @NotNull Consumer<Scheduled> runnable)
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
            public @NotNull Scheduled delayed(@NotNull org.bukkit.entity.Entity entity, long delay, @NotNull Consumer<Scheduled> runnable, @Nullable Runnable retired)
            {
                return global.delayed(delay, runnable);
            }

            @Override
            public @NotNull Scheduled repeating(@NotNull org.bukkit.entity.Entity entity, long delay, long repeat, @NotNull Consumer<Scheduled> runnable, @Nullable Runnable retired)
            {
                return global.repeating(delay, repeat, runnable);
            }
        };
    }

    private static abstract class BukkitRunnableWrapped extends BukkitRunnable
    {
        protected volatile boolean done = false;
        protected final @NotNull Scheduled scheduled = new Scheduled()
        {
            @Override
            public void cancel()
            {
                BukkitRunnableWrapped.this.cancel();
            }

            @Override
            public int bukkitId()
            {
                return getTaskId();
            }

            @Override
            public UUID spongeId()
            {
                return null;
            }

            @Override
            public boolean done()
            {
                return done || isCancelled();
            }
        };
    }
}
