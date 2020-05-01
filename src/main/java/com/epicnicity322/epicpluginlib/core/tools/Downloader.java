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
