/*
 * EpicPluginLib - Library with basic utilities for Minecraft plugins.
 * Copyright (C) 2023-2026 Christiano Rangel
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

package com.epicnicity322.epicpluginlib.core.updater;

import com.epicnicity322.epicpluginlib.core.networking.DownloadResult;
import com.epicnicity322.epicpluginlib.core.networking.Downloader;
import com.epicnicity322.epicpluginlib.core.scheduler.TaskFactory;
import org.apache.maven.artifact.versioning.ComparableVersion;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayOutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * A class to check for updates on spigotmc.org.
 */
public class SpigotUpdateChecker
{
    private final int id;
    private final @NotNull ComparableVersion currentVersion;
    private final @NotNull URL url;
    private final @NotNull TaskFactory.Global taskFactory;

    /**
     * UpdateChecker constructor.
     *
     * @param id             The id of your plugin page on spigotmc.org.
     * @param currentVersion The current version of your plugin.
     * @param taskFactory    The scheduler to be used when performing update lookups.
     */
    public SpigotUpdateChecker(int id, @NotNull ComparableVersion currentVersion, @NotNull TaskFactory.Global taskFactory)
    {
        this.id = id;
        this.currentVersion = currentVersion;
        this.taskFactory = taskFactory;

        try {
            url = new URL("https://api.spigotmc.org/legacy/update.php?resource=" + id);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @return The id of this plugin's spigotmc.org page.
     */
    public int id()
    {
        return id;
    }

    /**
     * @return The current version the plugin is at.
     */
    public @NotNull ComparableVersion currentVersion()
    {
        return currentVersion;
    }

    /**
     * Performs an HTTP request to the spigotmc.org API to retrieve the project's latest released tag.
     * <p>
     * The lookup is executed using the {@link TaskFactory} scheduler, and the provided consumer is invoked when the
     * operation completes.
     * <p>
     * If an {@link Downloader.HttpStatus} of UNEXPECTED_ERROR is
     * returned, the exception's stack trace is printed.
     *
     * @param onCheck A consumer invoked when the lookup completes. The first argument indicates
     *                whether the latest tag represents a newer version than the current project
     *                version, and the second argument contains the retrieved version.
     */
    public void check(@NotNull BiConsumer<Boolean, ComparableVersion> onCheck)
    {
        check(onCheck, result -> {
            if (result.status() == Downloader.HttpStatus.UNEXPECTED_ERROR && result.exception() != null) {
                result.exception().printStackTrace();
            }
        });
    }

    /**
     * Performs an HTTP request to the spigotmc.org API to retrieve the project's latest released tag.
     * <p>
     * The lookup is executed using the {@link TaskFactory} scheduler, and the provided consumers are
     * invoked when the operation completes.
     *
     * @param onCheck A consumer invoked when the lookup completes. The first argument indicates
     *                whether the latest tag represents a newer version than the current project
     *                version, and the second argument contains the retrieved version.
     * @param onError A consumer invoked if an error occurs during the request.
     */
    public void check(@NotNull BiConsumer<Boolean, ComparableVersion> onCheck, @Nullable Consumer<DownloadResult> onError)
    {
        taskFactory.delayed(0, task -> {
            try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                DownloadResult result = new Downloader(url, baos).call();

                if (result.status() == Downloader.HttpStatus.SUCCESS) {
                    ComparableVersion latestVersion = new ComparableVersion(baos.toString("UTF-8"));

                    onCheck.accept(latestVersion.compareTo(currentVersion) > 0, latestVersion);
                } else if (onError != null) {
                    onError.accept(result);
                }
            } catch (Throwable t) {
                if (onError != null) onError.accept(new DownloadResult(Downloader.HttpStatus.UNEXPECTED_ERROR, t));
            }
        });
    }
}
