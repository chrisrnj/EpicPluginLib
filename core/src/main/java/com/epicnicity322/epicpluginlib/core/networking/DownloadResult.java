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

package com.epicnicity322.epicpluginlib.core.networking;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public final class DownloadResult
{
    private final @NotNull Downloader.HttpStatus status;
    private final @Nullable Throwable exception;

    public DownloadResult(@NotNull Downloader.HttpStatus status, @Nullable Throwable exception)
    {
        this.status = status;
        this.exception = exception;
    }

    public @NotNull Downloader.HttpStatus status()
    {
        return status;
    }

    public @Nullable Throwable exception()
    {
        return exception;
    }

    @Override
    public boolean equals(Object o)
    {
        if (o == null || getClass() != o.getClass()) return false;
        DownloadResult that = (DownloadResult) o;
        return status == that.status && Objects.equals(exception, that.exception);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(status, exception);
    }

    @Override
    public String toString()
    {
        return exception == null ? status.toString() : status + " - " + exception;
    }
}
