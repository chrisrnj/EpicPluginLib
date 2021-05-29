/*
 * EpicPluginLib - Library with basic utilities for bukkit plugins.
 * Copyright (C) 2021  Christiano Rangel
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

package com.epicnicity322.epicpluginlib.bukkit;

import com.epicnicity322.epicpluginlib.bukkit.logger.Logger;
import com.epicnicity322.epicpluginlib.core.config.ConfigurationHolder;
import com.epicnicity322.epicpluginlib.core.config.ConfigurationLoader;
import com.epicnicity322.epicpluginlib.core.logger.ConsoleLogger;
import com.epicnicity322.epicpluginlib.core.tools.SpigotUpdateChecker;
import com.epicnicity322.epicpluginlib.core.tools.Version;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.List;

public final class EpicPluginLib extends JavaPlugin implements com.epicnicity322.epicpluginlib.core.EpicPluginLib
{
    private static @Nullable EpicPluginLib epicPluginLib;

    public EpicPluginLib()
    {
        epicPluginLib = this;
    }

    /**
     * Gets an instance of {@link EpicPluginLib}.
     *
     * @return The instance or null if the lib wasn't loaded by bukkit yet.
     */
    public static @Nullable EpicPluginLib getEpicPluginLib()
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
            List<String> depend = desc.getDepend();
            List<String> softDepend = desc.getSoftDepend();

            // On older versions of bukkit PluginDescriptionFile#getDepend and PluginDescriptionFile#getSoftDepend could be null if empty.
            if (depend == null ? softDepend != null && softDepend.contains("EpicPluginLib") : depend.contains("EpicPluginLib")) {
                logger.log("Dependency found: " + plugin.getName() + ".");
                ++dependingPlugins;
            }
        }

        ConfigurationHolder mainConfig = new ConfigurationHolder(getDataFolder().toPath().resolve("config.yml"), "Check for updates: true");
        ConfigurationLoader configLoader = new ConfigurationLoader();

        configLoader.registerConfiguration(mainConfig);

        if (!configLoader.loadConfigurations().isEmpty()) {
            logger.log("Something went wrong while loading main config, using default values.");
        }

        if (dependingPlugins == 0) {
            logger.log("Lib enabled but no dependencies found.", ConsoleLogger.Level.WARN);
        } else {
            logger.log("Lib enabled successfully.");
        }

        // Checking for updates:
        if (mainConfig.getConfiguration().getBoolean("Check for updates").orElse(true)) {
            SpigotUpdateChecker updateChecker = new SpigotUpdateChecker(80448, new Version(getDescription().getVersion()));

            updateChecker.check((available, version) -> {
                if (available)
                    Bukkit.getScheduler().runTaskTimerAsynchronously(epicPluginLib, () -> logger.log("EpicPluginLib v" + version + " is available. Please update."), 0, 36000);
            });
        }

        try {
            // bStats libraries were added in 1.8.3, testing if metrics should run.
            Class.forName("com.google.gson.JsonElement");
            Metrics metrics = new Metrics(this, 8337);

            logger.log("EpicPluginLib is using bStats as metrics collector.");
        } catch (ClassNotFoundException ignored) {
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