/*
 * EpicPluginLib - Library with basic utilities for Minecraft plugins.
 * Copyright (C) 2025-2026 Christiano Rangel
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

import com.epicnicity322.epicpluginlib.common.BukkitVersion;
import com.epicnicity322.epicpluginlib.common.SpongeVersion;
import com.epicnicity322.epicpluginlib.core.tools.Version;
import org.apache.maven.artifact.versioning.ComparableVersion;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public final class EpicPluginLib
{
    /**
     * The version of EpicPluginLib as string.
     *
     * @deprecated Use {@link #VERSION_STRING}
     */
    @Deprecated
    public static final @NotNull String versionString = EpicPluginLibVersion.version;

    /**
     * The version of EpicPluginLib as a string.
     */
    public static final @NotNull String VERSION_STRING = EpicPluginLibVersion.version;

    /**
     * The version of EpicPluginLib.
     *
     * @deprecated Use {@link #VERSION}
     */
    @Deprecated
    public static final @NotNull Version version = new Version(VERSION_STRING);

    /**
     * The version of EpicPluginLib.
     */
    public static final @NotNull ComparableVersion VERSION = new ComparableVersion(VERSION_STRING);

    private EpicPluginLib()
    {
    }

    public static void main(String[] args)
    {
        System.out.println("I am a library, not an application!");

        JFrame frame = new JFrame("Sorry");
        JLabel textArea = new JLabel(" I am a library, not an application!");

        frame.add(textArea);
        frame.setSize(210, 60);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);

        try {
            Thread.sleep(7500);
        } catch (Exception ignored) {
        }

        System.exit(0);
    }

    public enum Platform
    {
        BUKKIT, SPONGE, UNKNOWN;

        private static final @NotNull Platform platform;
        private static final @NotNull ComparableVersion version;
        private static final boolean paper = getClass("com.destroystokyo.paper.ParticleBuilder") != null;
        private static final boolean folia = getClass("io.papermc.paper.threadedregions.RegionizedServer") != null;
        private static final boolean hasThreadedRegions = getClass("io.papermc.paper.threadedregions.scheduler.AsyncScheduler") != null;

        static {
            if (getClass("org.bukkit.Bukkit") != null) {
                platform = BUKKIT;
                version = new ComparableVersion(BukkitVersion.getVersion());
            } else if (getClass("org.spongepowered.api.Sponge") != null) {
                platform = SPONGE;
                version = new ComparableVersion(SpongeVersion.getVersion());
            } else {
                platform = UNKNOWN;
                version = new ComparableVersion("0.0");
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
         * Whether "com.destroystokyo.paper.ParticleBuilder" class was found.
         *
         * @return Whether this server is running a version of the Paper fork.
         */
        public static boolean isPaper()
        {
            return paper;
        }

        /**
         * Whether "io.papermc.paper.threadedregions.RegionizedServer" class was found.
         *
         * @return Whether this server is running a version of the Folia fork.
         */
        public static boolean isFolia()
        {
            return folia;
        }

        /**
         * Whether the server has an implementation of the new Paper scheduler.
         * <p>
         * This may be used to test whether Folia schedulers can be used interoperable between Paper and Folia servers.
         *
         * @return Whether "io.papermc.paper.threadedregions.scheduler.AsyncScheduler" class is found.
         */
        public static boolean hasThreadedRegions()
        {
            return hasThreadedRegions;
        }

        /**
         * Gets the current platform EpicPluginLib is running on.
         *
         * @return The enum referencing to the platform EpicPluginLib was initialized on.
         * @deprecated Use {@link #current()}
         */
        @Deprecated
        public static @NotNull Platform getPlatform()
        {
            return current();
        }

        /**
         * Gets the current platform EpicPluginLib is running on.
         *
         * @return The enum referencing to the platform EpicPluginLib was initialized on.
         */
        public static @NotNull Platform current()
        {
            return platform;
        }

        /**
         * Gets the version of the current platform EpicPluginLib is running on.
         *
         * @return The version of the platform EpicPluginLib is running, 0.0 in case platform is {@link #UNKNOWN}.
         * @deprecated Use {@link #version()}
         */
        @Deprecated
        public static @NotNull Version getVersion()
        {
            return new Version(version.toString());
        }

        /**
         * Gets the version of the current platform EpicPluginLib is running on.
         *
         * @return The version of the platform EpicPluginLib is running, 0.0 in case platform is {@link #UNKNOWN}.
         */
        public static @NotNull ComparableVersion version()
        {
            return version;
        }
    }
}