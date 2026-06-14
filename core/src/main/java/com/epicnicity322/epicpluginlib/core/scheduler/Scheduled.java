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

import java.util.UUID;

public interface Scheduled
{
    /**
     * Cancel this task from running if it hasn't already ran.
     */
    void cancel();

    /**
     * An ID assigned to the task by the Bukkit scheduler.
     *
     * @return The ID assigned to this scheduled task.
     */
    int bukkitId();

    /**
     * A UUID assigned to the task by the Sponge scheduler.
     *
     * @return The UUID assigned to this scheduled task.
     */
    UUID spongeId();

    /**
     * Whether the task has already finished running or has been cancelled.
     *
     * @return If the task is no longer scheduled.
     */
    boolean done();
}
