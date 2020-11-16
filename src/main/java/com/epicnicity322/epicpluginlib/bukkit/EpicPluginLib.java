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

package com.epicnicity322.epicpluginlib.bukkit;

import com.epicnicity322.epicpluginlib.bukkit.logger.Logger;
import com.epicnicity322.epicpluginlib.bukkit.updater.UpdateChecker;
import com.epicnicity322.epicpluginlib.core.config.ConfigLoader;
import com.epicnicity322.epicpluginlib.core.config.PluginConfig;
import com.epicnicity322.epicpluginlib.core.logger.ConsoleLogger;
import com.epicnicity322.epicpluginlib.core.tools.Version;
import org.bstats.bukkit.MetricsLite;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Path;

public final class EpicPluginLib extends JavaPlugin implements com.epicnicity322.epicpluginlib.core.EpicPluginLib
{
    private static @Nullable com.epicnicity322.epicpluginlib.core.EpicPluginLib epicPluginLib;

    public EpicPluginLib()
    {
        epicPluginLib = this;
    }

    /**
     * Gets an instance of {@link com.epicnicity322.epicpluginlib.core.EpicPluginLib}.
     *
     * @return The instance or null if the lib wasn't loaded by bukkit yet.
     */
    public static @Nullable com.epicnicity322.epicpluginlib.core.EpicPluginLib getEpicPluginLib()
    {
        return epicPluginLib;
    }

    @Override
    public void onEnable()
    {
        Logger logger = new Logger("[EpicPluginLib] ", getLogger());
        int dependingPlugins = 0;

        for (Plugin plugin : Bukkit.getPluginManager().getPlugins()) {
            PluginDescriptionFile desc = plugin.getDescription();

            if (desc.getDepend().contains("EpicPluginLib") || desc.getSoftDepend().contains("EpicPluginLib")) {
                logger.log("Dependency found: " + plugin.getName() + ".");
                ++dependingPlugins;
            }
        }

        PluginConfig mainConfig = new PluginConfig(getDataFolder().toPath().resolve("config.yml"));

        mainConfig.addDefault("Check for updates", true);

        ConfigLoader configLoader = new ConfigLoader();

        configLoader.registerConfiguration(mainConfig);

        try {
            configLoader.loadConfigurations();

            if (dependingPlugins == 0)
                logger.log("Lib enabled but no dependencies found.", ConsoleLogger.Level.WARN);
            else
                logger.log("Lib enabled successfully.");
        } catch (IOException e) {
            logger.log("Something went wrong while saving main config, using default values.");

            if (dependingPlugins == 0)
                logger.log("Lib enabled but no dependencies found.", ConsoleLogger.Level.WARN);
            else
                logger.log("Lib enabled.");
        }

        if (mainConfig.getConfiguration().getBoolean("Check for updates").orElse(true)) {
            EpicPluginLib plugin = this;

            // Checking for updates:
            new Thread(new UpdateChecker(80448, new Version(getDescription().getVersion()))
            {
                @Override
                public void onUpdateCheck(@NotNull CheckResult checkResult, @Nullable Version latestVersion)
                {
                    if (checkResult == UpdateChecker.CheckResult.AVAILABLE)
                        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, () -> logger.log("EpicPluginLib v" + latestVersion + " is available. Please update."), 0, 36000);
                }
            }, "Update Checker").start();
        }

        // bStats libraries were added in 1.8.3, testing if metrics should run.
        boolean supportsMetrics = false;

        try {
            Class.forName("com.google.gson.JsonElement");
            supportsMetrics = true;
        } catch (ClassNotFoundException ignored) {
        }

        if (supportsMetrics) {
            MetricsLite metrics = new MetricsLite(this, 8337);

            if (metrics.isEnabled())
                logger.log("EpicPluginLib is using bStats as metrics collector.");
        }
    }

    @Override
    public @NotNull Path getFolder()
    {
        return getDataFolder().toPath();
    }

    @Override
    public @NotNull Path getPath()
    {
        return getFile().toPath();
    }
}