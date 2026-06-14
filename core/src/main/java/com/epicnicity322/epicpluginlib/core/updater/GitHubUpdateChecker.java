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
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.maven.artifact.versioning.ComparableVersion;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayOutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * A class that allows you to check for new updates using your project's GitHub repository.
 */
public class GitHubUpdateChecker
{
    private final @NotNull ComparableVersion currentVersion;
    private final @NotNull URL url;
    private final @NotNull TaskFactory.Global taskFactory;

    public GitHubUpdateChecker(@NotNull String repository, @NotNull ComparableVersion currentVersion, @NotNull TaskFactory.Global taskFactory)
    {
        this.currentVersion = currentVersion;
        this.taskFactory = taskFactory;

        try {
            url = new URL("https://api.github.com/repos/" + repository + "/releases/latest");
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @return The current version the plugin is at.
     */
    public @NotNull ComparableVersion currentVersion()
    {
        return currentVersion;
    }

    /**
     * Performs an HTTP request to the project's GitHub repository to retrieve the latest released tag.
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
        check(onCheck, error -> {
            if (error.status() == Downloader.HttpStatus.UNEXPECTED_ERROR && error.exception() != null) {
                error.exception().printStackTrace();
            }
        });
    }

    /**
     * Performs an HTTP request to the project's GitHub repository to retrieve the latest released tag.
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
                    JsonObject json = JsonParser.parseString(baos.toString("UTF-8")).getAsJsonObject();
                    ComparableVersion latestVersion = new ComparableVersion(json.get("tag_name").getAsString());

                    onCheck.accept(latestVersion.compareTo(currentVersion) > 0, latestVersion);
                } else if (onError != null) {
                    onError.accept(result);
                }
            } catch (Throwable t) {
                if (onError != null) {
                    onError.accept(new DownloadResult(Downloader.HttpStatus.UNEXPECTED_ERROR, t));
                }
            }
        });
    }
}
