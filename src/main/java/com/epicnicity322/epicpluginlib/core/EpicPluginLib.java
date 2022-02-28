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

package com.epicnicity322.epicpluginlib.core;

import com.epicnicity322.epicpluginlib.core.tools.Version;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class EpicPluginLib
{
    /**
     * The version of EpicPluginLib as string.
     */
    public static final @NotNull String versionString = EpicPluginLibVersion.version;
    /**
     * The version of EpicPluginLib.
     */
    public static final @NotNull Version version = new Version(versionString);

    private EpicPluginLib()
    {
    }

    public enum Platform
    {
        BUKKIT,
        SPONGE,
        UNKNOWN;

        private static final @NotNull Platform platform;
        private static final @NotNull Version version;

        static {
            if (getClass("org.bukkit.Bukkit") != null) {
                platform = BUKKIT;
                version = BukkitVersion.getVersion();
            } else if (getClass("org.spongepowered.api.Sponge") != null) {
                platform = SPONGE;
                version = SpongeVersion.getVersion();
            } else {
                platform = UNKNOWN;
                version = new Version("0.0");
            }
        }

        private static @Nullable Class<?> getClass(String name)
        {
            try {
                return Class.forName(name);
            } catch (ClassNotFoundException e) {
                return null;
            }
        }

        /**
         * Gets the current platform EpicPluginLib is running on.
         *
         * @return The enum referencing to the platform EpicPluginLib was initialized on.
         */
        public static @NotNull Platform getPlatform()
        {
            return platform;
        }

        /**
         * Gets the version of the current platform EpicPluginLib is running on.
         *
         * @return The version of the platform EpicPluginLib is running, 0.0 in case platform is {@link #UNKNOWN}.
         */
        public static @NotNull Version getVersion()
        {
            return version;
        }
    }
}