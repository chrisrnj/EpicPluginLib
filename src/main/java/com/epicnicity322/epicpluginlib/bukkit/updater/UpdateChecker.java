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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayOutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.function.BiConsumer;

/**
 * A thread that when executed checks for updates of your plugin on spigotmc.org.
 */
public class UpdateChecker extends Thread
{
    private URL url;
    private final @NotNull Version currentVersion;
    private @Nullable BiConsumer<CheckResult, Version> onUpdateCheck;

    /**
     * UpdateChecker constructor.
     *
     * @param id             The id of your plugin page on spigotmc.org.
     * @param currentVersion The current version of your plugin.
     */
    public UpdateChecker(int id, @NotNull Version currentVersion)
    {
        super("Update Checker");
        this.currentVersion = currentVersion;

        try {
            url = new URL("https://api.spigotmc.org/legacy/update.php?resource=" + id);
        } catch (MalformedURLException ignored) {
            // Never going to happen.
        }
    }

    @Override
    public synchronized void start()
    {
        if (onUpdateCheck != null)
            super.start();
    }

    @Override
    public void run()
    {
        @Nullable Version latestVersion = null;
        @NotNull CheckResult checkResult;

        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Downloader downloader = new Downloader(url, baos);

            downloader.run();

            if (downloader.getResult() == Downloader.Result.SUCCESS) {
                latestVersion = new Version(new String(baos.toByteArray(), StandardCharsets.UTF_8));

                if (latestVersion.compareTo(currentVersion) > 0)
                    checkResult = CheckResult.AVAILABLE;
                else
                    checkResult = CheckResult.NOT_AVAILABLE;
            } else {
                checkResult = CheckResult.valueOf(downloader.getResult().toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
            checkResult = CheckResult.UNEXPECTED_ERROR;
        }

        if (onUpdateCheck != null)
            onUpdateCheck.accept(checkResult, latestVersion);
    }

    /**
     * Sets a consumer to run when an update is checked.
     *
     * @param onUpdateCheck A consumer of {@link CheckResult} as the result and {@link Version} as the latest version
     *                      found. {@link Version} can be null.
     */
    public synchronized void setOnUpdateCheck(@NotNull BiConsumer<@NotNull CheckResult, @Nullable Version> onUpdateCheck)
    {
        this.onUpdateCheck = onUpdateCheck;
    }


    public @NotNull Version getCurrentVersion()
    {
        return currentVersion;
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
