/*
 * EpicPluginLib - Library with basic utilities for Minecraft plugins.
 * Copyright (C) 2022-2026 Christiano Rangel
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

package com.epicnicity322.epicpluginlib.core.tools;

import org.apache.maven.artifact.versioning.ComparableVersion;
import org.jetbrains.annotations.NotNull;

/**
 * @deprecated In favour of {@link ComparableVersion}. Removal scheduled for 3.0.
 */
@Deprecated
public class Version implements Comparable<Version>
{
    private final @NotNull ComparableVersion version;

    public Version(@NotNull String version)
    {
        this.version = new ComparableVersion(version);
    }

    public final @NotNull String getVersion()
    {
        return version.toString();
    }

    @Override
    public int compareTo(@NotNull Version version)
    {
        return this.version.compareTo(version.version);
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (!(o instanceof Version)) return false;

        Version that = (Version) o;
        return version.equals(that.version);
    }

    @Override
    public @NotNull String toString()
    {
        return version.toString();
    }
}
