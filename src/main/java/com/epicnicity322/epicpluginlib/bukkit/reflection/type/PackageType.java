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

package com.epicnicity322.epicpluginlib.bukkit.reflection.type;

import com.epicnicity322.epicpluginlib.bukkit.reflection.NMSVersion;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

public enum PackageType
{
    // Some versions didn't have nms version suffix.
    MINECRAFT_SERVER("net.minecraft.server" + (NMSVersion.NMS_VERSION.isEmpty() ? "" : '.' + NMSVersion.NMS_VERSION)),
    CRAFTBUKKIT(Bukkit.getServer().getClass().getPackage().getName());

    private final @NotNull String name;

    PackageType(@NotNull String name)
    {
        this.name = name;
    }

    public @NotNull String getName()
    {
        return name;
    }

    @Override
    public @NotNull String toString()
    {
        return name;
    }
}
