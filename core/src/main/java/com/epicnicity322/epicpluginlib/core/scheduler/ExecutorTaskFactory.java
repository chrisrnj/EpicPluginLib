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

import java.util.UUID;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * A {@link TaskFactory} provider that uses {@link ScheduledExecutorService}s to schedule tasks.
 * <p>
 * <b>Available Schedulers:</b>
 * <ul>
 * <li>{@link #global()} — Executes tasks using {@link ScheduledExecutorService} and scheduleWithFixedDelay for repeating.</li>
 * <li>{@link #async()} — Executes tasks using {@link ScheduledExecutorService} and scheduleAtFixedRate for repeating.</li>
 * </ul>
 * Every other task factory will behave the same as {@link #global()}. Tasks are not be guaranteed to run on the main
 * server thread.
 */
public final class ExecutorTaskFactory implements TaskFactoryProvider<Object, Object>
{
    public static final long MILLIS_PER_TICK = 50;
    private final @NotNull ScheduledExecutorService executor;

    public ExecutorTaskFactory(@NotNull ScheduledExecutorService executor)
    {
        this.executor = executor;
    }

    @Override
    public @NotNull TaskFactory.Global global()
    {
        return new TaskFactory.Global()
        {
            @Override
            public @NotNull Scheduled delayed(long delay, @NotNull Consumer<Scheduled> runnable)
            {
                ExecutorScheduled sch = new ExecutorScheduled();
                Runnable command = () -> runnable.accept(sch);

                if (delay <= 0) {
                    sch.scheduled = executor.submit(command);
                } else {
                    sch.scheduled = executor.schedule(command, delay * MILLIS_PER_TICK, TimeUnit.MILLISECONDS);
                }

                return sch;
            }

            @Override
            public @NotNull Scheduled repeating(long delay, long repeat, @NotNull Consumer<Scheduled> runnable)
            {
                if (delay < 0) delay = 0;
                if (repeat < 0) repeat = 0;

                ExecutorScheduled sch = new ExecutorScheduled();
                Runnable command = () -> runnable.accept(sch);

                sch.scheduled = executor.scheduleWithFixedDelay(command, delay * MILLIS_PER_TICK, repeat * MILLIS_PER_TICK, TimeUnit.MILLISECONDS);
                return sch;
            }
        };
    }

    @Override
    public @NotNull TaskFactory.Async async()
    {
        TaskFactory.Global global = global();

        return new TaskFactory.Async()
        {
            @Override
            public @NotNull Scheduled delayed(long delay, @NotNull Consumer<Scheduled> runnable)
            {
                return global.delayed(delay, runnable);
            }

            @Override
            public @NotNull Scheduled repeating(long delay, long repeat, @NotNull Consumer<Scheduled> runnable)
            {
                if (delay < 0) delay = 0;
                if (repeat < 0) repeat = 0;

                ExecutorScheduled sch = new ExecutorScheduled();
                Runnable command = () -> runnable.accept(sch);

                sch.scheduled = executor.scheduleAtFixedRate(command, delay * MILLIS_PER_TICK, repeat * MILLIS_PER_TICK, TimeUnit.MILLISECONDS);
                return sch;
            }
        };
    }

    @Override
    public @NotNull TaskFactory.Local<Object> local()
    {
        TaskFactory.Global global = global();

        return new TaskFactory.Local<Object>()
        {
            @Override
            public @NotNull Scheduled delayed(@NotNull Object world, int chunkX, int chunkZ, long delay, @NotNull Consumer<Scheduled> runnable)
            {
                return global.delayed(delay, runnable);
            }

            @Override
            public @NotNull Scheduled repeating(@NotNull Object world, int chunkX, int chunkZ, long delay, long repeat, @NotNull Consumer<Scheduled> runnable)
            {
                return global.repeating(delay, repeat, runnable);
            }
        };
    }

    @Override
    public @NotNull TaskFactory.Entity<Object> entity()
    {
        TaskFactory.Global global = global();

        return new TaskFactory.Entity<Object>()
        {
            @Override
            public @NotNull Scheduled delayed(@NotNull Object entity, long delay, @NotNull Consumer<Scheduled> runnable, @Nullable Runnable retired)
            {
                return global.delayed(delay, runnable);
            }

            @Override
            public @NotNull Scheduled repeating(@NotNull Object entity, long delay, long repeat, @NotNull Consumer<Scheduled> runnable, @Nullable Runnable retired)
            {
                return global.repeating(delay, repeat, runnable);
            }
        };
    }

    private static final class ExecutorScheduled implements Scheduled
    {
        private @Nullable Future<?> scheduled;

        @Override
        public void cancel()
        {
            if (scheduled != null) scheduled.cancel(false);
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
            return scheduled != null && scheduled.isDone();
        }
    }
}
