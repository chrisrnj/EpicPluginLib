package com.epicnicity322.epicpluginlib.updater.tools;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.*;

public class Downloader implements Runnable
{
    private URL url;
    private OutputStream out;
    private Result result;

    public Downloader(URL url, OutputStream out)
    {
        this.url = url;
        this.out = out;
    }

    public Result getResult()
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

    private URL getRedirect(URL url) throws IOException
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
     * OFFLINE = Unable to connect to url.
     * SUCCESS = Update downloaded successfully.
     * TIMEOUT = Connection timed out.
     * UNEXPECTED_ERROR = Something went wrong while downloading.
     */
    public enum Result
    {
        OFFLINE,
        SUCCESS,
        TIMEOUT,
        UNEXPECTED_ERROR
    }
}
