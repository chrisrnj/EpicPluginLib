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

package com.epicnicity322.epicpluginlib.core.networking;

import com.epicnicity322.epicpluginlib.core.EpicPluginLib;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

public class Downloader implements Callable<DownloadResult>
{
    private static final int MAX_REDIRECTS = 10;
    private static final @NotNull String USER_AGENT = "chrisrnj/EpicPluginLib/" + EpicPluginLib.VERSION_STRING;
    private final @NotNull URL url;
    private final @NotNull OutputStream out;

    /**
     * Creates an instance of {@link Downloader}. Downloads data from an http {@link URL}.
     *
     * @param url The {@link URL} to download the data.
     * @param out The {@link OutputStream} to write the data.
     */
    public Downloader(@NotNull URL url, @NotNull OutputStream out)
    {
        this.url = url;
        this.out = out;
    }

    private static boolean isRedirect(int code)
    {
        return code == HttpURLConnection.HTTP_MOVED_PERM   // 301
                || code == HttpURLConnection.HTTP_MOVED_TEMP   // 302
                || code == HttpURLConnection.HTTP_SEE_OTHER    // 303
                || code == 307                                 // TEMP_REDIRECT
                || code == 308;                                // PERM_REDIRECT
    }

    private static URL resolveRecursive(URL url, Map<String, String> headers, int depth, Set<String> visited) throws IOException
    {
        if (depth > MAX_REDIRECTS) throw new IOException("Too many redirects");
        if (!visited.add(url.toString())) throw new IOException("Redirect loop detected");

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        try {
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);
            conn.setInstanceFollowRedirects(false);

            if (headers != null) for (Map.Entry<String, String> entry : headers.entrySet()) {
                conn.setRequestProperty(entry.getKey(), entry.getValue());
            }

            if (headers == null || !headers.containsKey("User-Agent"))
                conn.setRequestProperty("User-Agent", USER_AGENT);

            conn.connect();

            int code = conn.getResponseCode();

            if (isRedirect(code)) {
                String location = conn.getHeaderField("Location");

                if (location == null) throw new IOException("Redirect response missing Location header");

                URL newUrl = new URL(url, location);
                return resolveRecursive(newUrl, headers, depth + 1, visited);
            }

            return url;
        } finally {
            conn.disconnect();
        }
    }

    private static URL resolve(URL url) throws IOException
    {
        return resolveRecursive(url, null, 0, new HashSet<>());
    }

    @Override
    public @NotNull DownloadResult call()
    {
        HttpURLConnection conn = null;

        try {
            URL resolved = resolve(url);

            conn = (HttpURLConnection) resolved.openConnection();

            conn.setConnectTimeout(10000);
            conn.setReadTimeout(10000);
            conn.setInstanceFollowRedirects(false);
            conn.setRequestProperty("User-Agent", USER_AGENT);

            int code = conn.getResponseCode();

            if (code >= 400) {
                return new DownloadResult(HttpStatus.UNEXPECTED_ERROR, new IOException("HTTP error: " + code));
            }

            try (BufferedInputStream is = new BufferedInputStream(conn.getInputStream())) {
                byte[] buffer = new byte[8192];
                int bytesRead;

                while ((bytesRead = is.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                }

                out.flush();

                return new DownloadResult(HttpStatus.SUCCESS, null);
            }
        } catch (SocketTimeoutException e) {
            return new DownloadResult(HttpStatus.TIMEOUT, e);
        } catch (UnknownHostException e) {
            return new DownloadResult(HttpStatus.OFFLINE, e);
        } catch (IOException e) {
            return new DownloadResult(HttpStatus.UNEXPECTED_ERROR, e);
        } finally {
            if (conn != null) conn.disconnect();
        }
    }

    public enum HttpStatus
    {
        /**
         * Unable to connect to url.
         */
        OFFLINE,
        /**
         * Operation performed successfully.
         */
        SUCCESS,
        /**
         * Connection timed out.
         */
        TIMEOUT,
        /**
         * Something went wrong in the connection.
         */
        UNEXPECTED_ERROR
    }
}
