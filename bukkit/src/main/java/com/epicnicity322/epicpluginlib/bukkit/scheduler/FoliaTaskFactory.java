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
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * A class containing implementations of TaskFactory for Folia API.
 * <p>
 * Although this is intended for folia, use for Paper is encouraged as well, as it will work fine either on Paper or
 * Folia servers.
 * <p>
 * <b>Available Schedulers:</b>
 * <ul>
 * <li>{@link #global(Plugin)} — Executes tasks on the primary server thread.</li>
 * <li>{@link #async(Plugin)} — Executes tasks asynchronously from the primary thread.</li>
 * <li>{@link #local(Plugin)} — Executes tasks on the specified location thread.</li>
 * <li>{@link #entity(Plugin)} — Executes tasks on the specified entity thread.</li>
 * </ul>
 */
public final class FoliaTaskFactory
{
    private static final long millisPerTick = 50;

    /**
     * A task scheduler for tasks run in the global main thread.
     */
    public static @NotNull TaskFactory.Global global(@NotNull Plugin plugin)
    {
        return new TaskFactory.Global()
        {
            @Override
            public @NotNull Scheduled delayed(long delay, @NotNull Consumer<Scheduled> runnable)
            {
                if (delay <= 0) delay = 1;
                return fromScheduledTask(plugin.getServer().getGlobalRegionScheduler().runDelayed(plugin, taskConsumer(runnable), delay));
            }

            @Override
            public @NotNull Scheduled repeating(long delay, long repeat, @NotNull Consumer<Scheduled> runnable)
            {
                if (delay <= 0) delay = 1;
                if (repeat <= 0) repeat = 1;
                return fromScheduledTask(plugin.getServer().getGlobalRegionScheduler().runAtFixedRate(plugin, taskConsumer(runnable), delay, repeat));
            }
        };
    }

    /**
     * A task scheduler for tasks run async from the global main thread.
     */
    public static @NotNull TaskFactory.Async async(@NotNull Plugin plugin)
    {
        return new TaskFactory.Async()
        {
            @Override
            public @NotNull Scheduled delayed(long delay, @NotNull Consumer<Scheduled> runnable)
            {
                // Folia Async Scheduler actually accepts delay = 0, but we're gonna use the runNow.
                if (delay <= 0) {
                    return fromScheduledTask(plugin.getServer().getAsyncScheduler().runNow(plugin, taskConsumer(runnable)));
                } else {
                    return fromScheduledTask(plugin.getServer().getAsyncScheduler().runDelayed(plugin, taskConsumer(runnable), delay * millisPerTick, TimeUnit.MILLISECONDS));
                }
            }

            @Override
            public @NotNull Scheduled repeating(long delay, long repeat, @NotNull Consumer<Scheduled> runnable)
            {
                if (delay < 0) delay = 0;
                if (repeat <= 0) repeat = 1;
                return fromScheduledTask(plugin.getServer().getAsyncScheduler().runAtFixedRate(plugin, taskConsumer(runnable), delay * millisPerTick, repeat, TimeUnit.MILLISECONDS));
            }
        };
    }

    /**
     * A task scheduler for tasks run in a thread specific to a location in a world.
     */
    public static @NotNull TaskFactory.Local<World> local(@NotNull Plugin plugin)
    {
        return new TaskFactory.Local<World>()
        {
            @Override
            public @NotNull Scheduled delayed(@NotNull World world, int chunkX, int chunkZ, long delay, @NotNull Consumer<Scheduled> runnable)
            {
                if (delay <= 0) delay = 1;
                return fromScheduledTask(plugin.getServer().getRegionScheduler().runDelayed(plugin, world, chunkX, chunkZ, taskConsumer(runnable), delay));
            }

            @Override
            public @NotNull Scheduled repeating(@NotNull World world, int chunkX, int chunkZ, long delay, long repeat, @NotNull Consumer<Scheduled> runnable)
            {
                if (delay <= 0) delay = 1;
                if (repeat <= 0) repeat = 1;
                return fromScheduledTask(plugin.getServer().getRegionScheduler().runAtFixedRate(plugin, world, chunkX, chunkZ, taskConsumer(runnable), delay, repeat));
            }
        };
    }

    /**
     * A task scheduler for tasks run in a thread specific for an entity.
     */
    public static @NotNull TaskFactory.Entity<Entity> entity(@NotNull Plugin plugin)
    {
        return new TaskFactory.Entity<Entity>()
        {
            @Override
            public @NotNull Scheduled delayed(@NotNull org.bukkit.entity.Entity entity, long delay, @NotNull Consumer<Scheduled> runnable, @Nullable Runnable retired)
            {
                if (delay <= 0) delay = 1;
                return fromScheduledTask(entity.getScheduler().runDelayed(plugin, taskConsumer(runnable), retired, delay));
            }

            @Override
            public @NotNull Scheduled repeating(@NotNull org.bukkit.entity.Entity entity, long delay, long repeat, @NotNull Consumer<Scheduled> runnable, @Nullable Runnable retired)
            {
                if (delay <= 0) delay = 1;
                if (repeat <= 0) repeat = 1;
                return fromScheduledTask(entity.getScheduler().runAtFixedRate(plugin, taskConsumer(runnable), retired, delay, repeat));
            }
        };
    }

    private static @NotNull Consumer<ScheduledTask> taskConsumer(@NotNull Consumer<Scheduled> runnable)
    {
        return t -> runnable.accept(fromScheduledTask(t));
    }

    private static @NotNull Scheduled fromScheduledTask(@Nullable ScheduledTask task)
    {
        return new Scheduled()
        {
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
                return null;
            }

            @Override
            public boolean done()
            {
                return task == null || task.getExecutionState() == ScheduledTask.ExecutionState.CANCELLED || task.getExecutionState() == ScheduledTask.ExecutionState.FINISHED;
            }
        };
    }
}
