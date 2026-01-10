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

package com.epicnicity322.epicpluginlib.core.util;

import org.jetbrains.annotations.NotNull;

import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * A class that provides global locks for paths.
 * <p>
 * When a lock is created by this class, it is saved in a map, which will allow for providing the same lock for a
 * {@link Path}. The lock is automatically removed from the map when it is unlocked.
 *
 * @see LockToken
 */
public final class PathLocker
{
    private static final @NotNull ConcurrentHashMap<Path, ReentrantLock> locks = new ConcurrentHashMap<>();

    private PathLocker()
    {
    }

    /**
     * Creates and locks a new lock for the specified path, if one doesn't already exist. Otherwise, obtains the global
     * lock for this path, locks it, and returns a new {@link LockToken}.
     * <p>
     * The provided path is guaranteed to return the same lock as other Path instances, as it uses
     * {@link Path#toRealPath(LinkOption...)} when possible, otherwise {@link Path#toAbsolutePath()}.
     *
     * @param path The path to obtain a global lock for.
     * @return The global lock for this path.
     */
    public static @NotNull LockToken lock(@NotNull Path path)
    {
        Path key = canonicalPath(path);
        ReentrantLock lock = locks.computeIfAbsent(key, k -> new ReentrantLock());
        lock.lock();
        return new LockToken(key, lock);
    }

    private static @NotNull Path canonicalPath(@NotNull Path path)
    {
        try {
            return path.toRealPath(); // resolves symlinks
        } catch (Exception e) {
            return path.toAbsolutePath().normalize();
        }
    }

    /**
     * A token that holds the lock and the path that uses it. Close it to unlock the path's lock.
     */
    public static final class LockToken implements AutoCloseable
    {
        private final @NotNull Path key;
        private final @NotNull ReentrantLock lock;
        private boolean closed = false;

        private LockToken(@NotNull Path key, @NotNull ReentrantLock lock)
        {
            this.key = key;
            this.lock = lock;
        }

        @Override
        public void close()
        {
            if (closed) return;
            closed = true;
            try {
                lock.unlock();
            } finally {
                if (!lock.isLocked() && !lock.hasQueuedThreads()) {
                    locks.remove(key, lock);
                }
            }
        }
    }
}

