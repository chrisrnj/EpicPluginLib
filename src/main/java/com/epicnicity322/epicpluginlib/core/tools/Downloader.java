/*
 * Copyright (c) 2020 Christiano Rangel
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.epicnicity322.epicpluginlib.core.tools;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.*;

public class Downloader implements Runnable
{
    private final @NotNull URL url;
    private final @NotNull OutputStream out;
    private Result result;

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
        InputStream is = conn.getInputStream();
        is.close();

        if (conn.getResponseCode() == HttpURLConnection.HTTP_MOVED_PERM ||
                conn.getResponseCode() == HttpURLConnection.HTTP_MOVED_TEMP) {
            return new URL(url, conn.getHeaderField("Location"));
        }

        return url;
    }

    /**
     * The result from the transferring.
     *
     * @return The result or null if the download didn't start yet.
     * @see Result
     */
    public @Nullable Result getResult()
    {
        return result;
    }

    @Override
    public void run()
    {
        try {
            URLConnection conn = getRedirect(url).openConnection();

            conn.setRequestProperty("User-Agent", "Plugin Updater");
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(10000);

            try (InputStream is = conn.getInputStream()) {
                byte[] buffer = new byte[8192];
                int bytesRead;

                while ((bytesRead = is.read(buffer)) > 0) {
                    out.write(buffer, 0, bytesRead);
                }

                result = Result.SUCCESS;
            }
        } catch (SocketTimeoutException e) {
            result = Result.TIMEOUT;
        } catch (UnknownHostException e) {
            result = Result.OFFLINE;
        } catch (IOException e) {
            e.printStackTrace();
            result = Result.UNEXPECTED_ERROR;
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
