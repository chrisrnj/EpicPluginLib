/*
 * EpicPluginLib - Library with basic utilities for Minecraft plugins.
 * Copyright (C) 2021-2026 Christiano Rangel
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

import com.epicnicity322.epicpluginlib.core.networking.DownloadResult;
import org.jetbrains.annotations.NotNull;

import java.io.OutputStream;
import java.net.URL;

@Deprecated
public class Downloader implements Runnable
{
    private final @NotNull com.epicnicity322.epicpluginlib.core.networking.Downloader downloader;
    private Result result;
    private Exception exception;

    /**
     * Creates an instance of {@link Downloader}. Downloads data from an http {@link URL}.
     *
     * @param url The {@link URL} to download the data.
     * @param out The {@link OutputStream} to write the data.
     */
    public Downloader(@NotNull URL url, @NotNull OutputStream out)
    {
        this.downloader = new com.epicnicity322.epicpluginlib.core.networking.Downloader(url, out);
    }

    /**
     * The result from the transferring.
     *
     * @return The result or null if {@link Downloader} wasn't run yet.
     * @see Result
     */
    public Result getResult()
    {
        return result;
    }

    /**
     * The exception thrown if {@link #getResult()} is not {@link Result#SUCCESS}.
     *
     * @return The exception thrown or null if {@link Downloader} wasn't run yet or was successful.
     * @see #getResult()
     */
    public Exception getException()
    {
        return exception;
    }

    @Override
    public void run()
    {
        DownloadResult downloadResult = downloader.call();
        exception = (Exception) downloadResult.exception();

        switch (downloadResult.status()) {
            case OFFLINE:
                result = Result.OFFLINE;
                break;
            case SUCCESS:
                result = Result.SUCCESS;
                break;
            case TIMEOUT:
                result = Result.TIMEOUT;
                break;
            case UNEXPECTED_ERROR:
                result = Result.UNEXPECTED_ERROR;
                break;
        }
    }

    @Deprecated
    public enum Result
    {
        /**
         * Unable to connect to url.
         */
        OFFLINE,
        /**
         * Update downloaded successfully.
         */
        SUCCESS,
        /**
         * Connection timed out.
         */
        TIMEOUT,
        /**
         * Something went wrong while downloading.
         */
        UNEXPECTED_ERROR
    }
}
