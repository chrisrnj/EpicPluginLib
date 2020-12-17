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

import java.io.ByteArrayOutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.function.BiConsumer;

/**
 * A class to check for updates on spigotmc.org.
 */
public class SpigotUpdateChecker
{
    private final int id;
    private final @NotNull Version currentVersion;
    private final @NotNull URL url;

    /**
     * UpdateChecker constructor.
     *
     * @param id             The id of your plugin page on spigotmc.org.
     * @param currentVersion The current version of your plugin.
     */
    public SpigotUpdateChecker(int id, @NotNull Version currentVersion)
    {
        this.id = id;
        this.currentVersion = currentVersion;

        try {
            url = new URL("https://api.spigotmc.org/legacy/update.php?resource=" + id);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @return The id of this plugin's spigotmc.org page.
     */
    public int getId()
    {
        return id;
    }

    /**
     * @return The current version the plugin is at.
     */
    public @NotNull Version getCurrentVersion()
    {
        return currentVersion;
    }

    /**
     * Checks if an update is available at spigotmc.org.
     *
     * @param onCheck A {@link BiConsumer} to run when check for updates, with {@link Boolean} as if the update is available, and {@link Version} as the latest version.
     * @see #check(BiConsumer onCheck, BiConsumer onError)
     */
    public void check(@NotNull BiConsumer<Boolean, Version> onCheck)
    {
        check(onCheck, ((result, exception) -> {
            if (result == Downloader.Result.UNEXPECTED_ERROR) {
                exception.printStackTrace();
            }
        }));
    }

    /**
     * Checks if an update is available at spigotmc.org.
     *
     * @param onCheck A {@link BiConsumer} to run when check for updates, with {@link Boolean} as if the update is available, and {@link Version} as the latest version.
     * @param onError A {@link BiConsumer} to run when something went wrong while connecting to spigotmc.org, as {@link com.epicnicity322.epicpluginlib.core.tools.Downloader.Result} as the result, and {@link Exception} as  the exception that was thrown.
     */
    public void check(@NotNull BiConsumer<Boolean, Version> onCheck, @Nullable BiConsumer<Downloader.Result, Exception> onError)
    {
        new Thread(() -> {
            try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                Downloader downloader = new Downloader(url, baos);

                downloader.run();

                if (downloader.getResult() == Downloader.Result.SUCCESS) {
                    Version latestVersion = new Version(baos.toString("UTF-8"));

                    onCheck.accept(latestVersion.compareTo(currentVersion) > 0, latestVersion);
                } else if (onError != null) {
                    onError.accept(downloader.getResult(), downloader.getException());
                }
            } catch (Exception e) {
                if (onError != null) {
                    onError.accept(Downloader.Result.UNEXPECTED_ERROR, e);
                }
            }
        }, "EPL Update Checker").start();
    }
}
