/*
 * EpicPluginLib - Library with basic utilities for bukkit plugins.
 * Copyright (C) 2023  Christiano Rangel
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

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayOutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.function.BiConsumer;

/**
 * A class that allows you to check for new updates using your project's GitHub repository.
 */
public class GitHubUpdateChecker
{
    private final @NotNull Version currentVersion;
    private final @NotNull URL url;

    public GitHubUpdateChecker(@NotNull String repository, @NotNull Version currentVersion)
    {
        this.currentVersion = currentVersion;

        try {
            url = new URL("https://api.github.com/repos/" + repository + "/releases/latest");
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @return The current version the plugin is at.
     */
    public @NotNull Version getCurrentVersion()
    {
        return currentVersion;
    }

    /**
     * Checks if an update is available on GitHub.
     *
     * @param onCheck A {@link BiConsumer} that is applied when the latest version is fetched, with {@link Boolean} as if the update is available, and {@link Version} as the latest version.
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
     * Checks if an update is available on GitHub.
     *
     * @param onCheck A {@link BiConsumer} that is applied when the latest version is fetched, with {@link Boolean} as if the update is available, and {@link Version} as the latest version.
     * @param onError A {@link BiConsumer} that is applied when an error happens, as {@link com.epicnicity322.epicpluginlib.core.tools.Downloader.Result} as the result, and {@link Exception} as the exception that was thrown.
     */
    public void check(@NotNull BiConsumer<Boolean, Version> onCheck, @Nullable BiConsumer<Downloader.Result, Exception> onError)
    {
        new Thread(() -> {
            try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                Downloader downloader = new Downloader(url, baos);

                downloader.run();

                if (downloader.getResult() == Downloader.Result.SUCCESS) {
                    JsonObject json = JsonParser.parseString(baos.toString("UTF-8")).getAsJsonObject();
                    Version latestVersion = new Version(json.get("tag_name").getAsString());

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
