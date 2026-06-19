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

import com.epicnicity322.epicpluginlib.core.EpicPluginLib;
import com.epicnicity322.epicpluginlib.core.scheduler.Scheduled;
import com.epicnicity322.epicpluginlib.core.scheduler.TaskFactory;
import com.epicnicity322.epicpluginlib.core.scheduler.TaskFactoryProvider;
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
 * Folia servers. You can check if the server supports the new scheduler using the method
 * {@link EpicPluginLib.Platform#hasThreadedRegions()}.
 * <p>
 * <b>Available Schedulers:</b>
 * <ul>
 * <li>{@link #global()} — Executes tasks on the primary server thread.</li>
 * <li>{@link #async()} — Executes tasks asynchronously from the primary thread.</li>
 * <li>{@link #local()} — Executes tasks on the specified location thread.</li>
 * <li>{@link #entity()} — Executes tasks on the specified entity thread.</li>
 * </ul>
 */
public final class FoliaTaskFactory implements TaskFactoryProvider<World, Entity>
{
    private static final long millisPerTick = 50;
    private final @NotNull Plugin plugin;

    public FoliaTaskFactory(@NotNull Plugin plugin)
    {
        this.plugin = plugin;
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

    @Override
    public @NotNull TaskFactory.Global global()
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

    @Override
    public @NotNull TaskFactory.Async async()
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
                return fromScheduledTask(plugin.getServer().getAsyncScheduler().runAtFixedRate(plugin, taskConsumer(runnable), delay * millisPerTick, repeat * millisPerTick, TimeUnit.MILLISECONDS));
            }
        };
    }

    @Override
    public @NotNull TaskFactory.Local<World> local()
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

    @Override
    public @NotNull TaskFactory.Entity<Entity> entity()
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
}
