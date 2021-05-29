/*
 * EpicPluginLib - Library with basic utilities for bukkit plugins.
 * Copyright (C) 2021  Christiano Rangel
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

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.*;

public class Downloader implements Runnable
{
    private final @NotNull URL url;
    private final @NotNull OutputStream out;
    private Result result;
    private Exception exception;

    /**
     * Creates an instance of {@link Downloader}. Downloads data from a http {@link URL}.
     *
     * @param url The {@link URL} to download the data.
     * @param out The {@link OutputStream} to write the data.
     */
    public Downloader(@NotNull URL url, @NotNull OutputStream out)
    {
        this.url = url;
        this.out = out;
    }

    private static URL getRedirect(URL url) throws IOException
    {
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        conn.setConnectTimeout(5000);
        conn.setReadTimeout(5000);
        conn.setInstanceFollowRedirects(false);
        conn.connect();

        if (conn.getResponseCode() == HttpURLConnection.HTTP_MOVED_PERM || conn.getResponseCode() == HttpURLConnection.HTTP_MOVED_TEMP)
            return getRedirect(new URL(conn.getHeaderField("Location")));

        return url;
    }

    /**
     * The result from the transferring.
     *
     * @return The result or null if {@link Downloader} wasn't run yet.
     * @see Result
     */
    public synchronized Result getResult()
    {
        return result;
    }

    /**
     * The exception thrown if {@link #getResult()} is not {@link Result#SUCCESS}.
     *
     * @return The exception thrown or null if {@link Downloader} wasn't run yet or was successful.
     * @see #getResult()
     */
    public synchronized Exception getException()
    {
        return exception;
    }

    @Override
    public void run()
    {
        try {
            URLConnection conn = getRedirect(url).openConnection();

            conn.setRequestProperty("User-Agent", "Plugin Downloader");
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(10000);

            try (InputStream is = conn.getInputStream()) {
                byte[] buffer = new byte[4096];
                int bytesRead;

                while ((bytesRead = is.read(buffer)) > 0) {
                    out.write(buffer, 0, bytesRead);
                }

                synchronized (this) {
                    result = Result.SUCCESS;
                }
            }
        } catch (SocketTimeoutException e) {
            synchronized (this) {
                exception = e;
                result = Result.TIMEOUT;
            }
        } catch (UnknownHostException e) {
            synchronized (this) {
                exception = e;
                result = Result.OFFLINE;
            }
        } catch (IOException e) {
            synchronized (this) {
                exception = e;
                result = Result.UNEXPECTED_ERROR;
            }
        }
    }

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
