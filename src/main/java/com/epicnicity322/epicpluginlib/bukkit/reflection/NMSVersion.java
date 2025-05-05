/*
 * EpicPluginLib - Library with basic utilities for bukkit plugins.
 * Copyright (C) 2025  Christiano Rangel
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

package com.epicnicity322.epicpluginlib.bukkit.reflection;

import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class NMSVersion
{
    public static final @NotNull String CRAFTBUKKIT_VERSION;
    public static final @NotNull String NMS_VERSION;

    static {
        // Checking if this version contains version suffix on the package.
        if (getClass("org.bukkit.craftbukkit.CraftServer") == null) {
            CRAFTBUKKIT_VERSION = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];

            if (getClass("net.minecraft.server.MinecraftServer") == null) {
                NMS_VERSION = CRAFTBUKKIT_VERSION;
            } else {
                NMS_VERSION = "";
            }
        } else {
            CRAFTBUKKIT_VERSION = "";
            NMS_VERSION = "";
        }
    }

    private NMSVersion()
    {
    }

    static @Nullable Class<?> getClass(@NotNull String name)
    {
        try {
            return Class.forName(name);
        } catch (ClassNotFoundException e) {
            return null;
        }
    }
}
