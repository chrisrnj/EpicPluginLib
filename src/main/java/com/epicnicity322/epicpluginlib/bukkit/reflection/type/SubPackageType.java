/*
 * EpicPluginLib - Library with basic utilities for bukkit plugins.
 * Copyright (C) 2022  Christiano Rangel
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

import org.jetbrains.annotations.NotNull;

public enum SubPackageType
{
    ADVANCEMENT,
    ATTRIBUTE,
    BLOCK,
    BOSS,
    CHUNKIO,
    COMMAND,
    CONFIGURATION,
    CONVERSATIONS,
    ENCHANTMENTS,
    ENTITY,
    EVENT,
    GENERATOR,
    HELP,
    INVENTORY,
    LEGACY,
    MAP,
    METADATA,
    PERSISTENCE,
    POTION,
    PROFILE,
    PROJECTILES,
    SCHEDULER,
    SCOREBOARD,
    STRUCTURE,
    TAG,
    UPDATER,
    UTIL;

    private final @NotNull String name;

    SubPackageType()
    {
        name = PackageType.CRAFTBUKKIT + "." + name().toLowerCase();
    }

    public @NotNull String getName()
    {
        return name;
    }

    @Override
    public String toString()
    {
        return name;
    }
}
