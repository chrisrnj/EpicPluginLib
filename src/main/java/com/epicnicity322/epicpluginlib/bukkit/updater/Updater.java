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

package com.epicnicity322.epicpluginlib.bukkit.updater;

import com.epicnicity322.epicpluginlib.core.tools.Downloader;
import com.epicnicity322.epicpluginlib.core.tools.Version;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * Downloads and checks for updates through spiget.org
 */
public class Updater
{
    private final @NotNull File jar;
    private final @NotNull Version currentVersion;
    private Version latestVersion;
    private boolean hasUpdate = false;
    private URL VERSION_URL;
    private URL DOWNLOAD_URL;

    /**
     * Creates an Updater that can check and download updates on spigotmc.org.
     *
     * @param jar            Your plugin's jar, you can get it by JavaPlugin#getFile().
     * @param currentVersion The current version of your plugin.
     * @param id             The id of your plugin in spigotmc.org.
     */
    public Updater(@NotNull File jar, @NotNull Version currentVersion, int id)
    {
        this.jar = jar;
        this.currentVersion = currentVersion;

        try {
            VERSION_URL = new URL("https://api.spigotmc.org/legacy/update.php?resource=" + id);
            DOWNLOAD_URL = new URL("https://api.spiget.org/v2/resources/" + id + "/download");
        } catch (MalformedURLException ignored) {
        }
    }

    public @NotNull CheckResult check()
    {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Downloader downloader = new Downloader(VERSION_URL, baos);
            Thread thread = new Thread(downloader, "Update Checker");

            thread.start();

            if (thread.isAlive())
                thread.join();

            if (downloader.getResult() != Downloader.Result.SUCCESS)
                return CheckResult.valueOf(downloader.getResult().toString());

            latestVersion = new Version(new String(baos.toByteArray(), StandardCharsets.UTF_8));

            if (latestVersion.compareTo(currentVersion) > 0) {
                hasUpdate = true;
                return CheckResult.AVAILABLE;
            } else {
                return CheckResult.NOT_AVAILABLE;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return CheckResult.UNEXPECTED_ERROR;
        }
    }

    public @NotNull Downloader.Result download()
    {
        try {
            File update = Bukkit.getUpdateFolderFile();

            if (update.mkdirs()) {
                Downloader downloader = new Downloader(DOWNLOAD_URL, new FileOutputStream(new File(update,
                        jar.getName())));
                Thread thread = new Thread(downloader, "Update Downloader");

                thread.start();

                if (thread.isAlive())
                    thread.join();

                return downloader.getResult();
            } else {
                return Downloader.Result.UNEXPECTED_ERROR;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return Downloader.Result.UNEXPECTED_ERROR;
    }

    public @Nullable Version getLatestVersion()
    {
        return latestVersion;
    }

    public @NotNull Version getCurrentVersion()
    {
        return currentVersion;
    }

    public boolean hasUpdate()
    {
        return hasUpdate;
    }

    public enum CheckResult
    {
        /**
         * There is a update available.
         */
        AVAILABLE,
        /**
         * Latest version is installed.
         */
        NOT_AVAILABLE,
        /**
         * Unable to connect to api.spigotmc.org.
         */
        OFFLINE,
        /**
         * Connection timed out.
         */
        TIMEOUT,
        /**
         * Something went wrong while checking for updates.
         */
        UNEXPECTED_ERROR
    }
}
