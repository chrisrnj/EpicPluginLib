/*
 * EpicPluginLib - Library with basic utilities for bukkit plugins.
 * Copyright (C) 2022  Christiano Rangel
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
import com.epicnicity322.epicpluginlib.bukkit.metrics.Metrics;
import com.epicnicity322.epicpluginlib.core.EpicPluginLib;
import com.epicnicity322.epicpluginlib.core.config.ConfigurationHolder;
import com.epicnicity322.epicpluginlib.core.config.ConfigurationLoader;
import com.epicnicity322.epicpluginlib.core.logger.ConsoleLogger;
import com.epicnicity322.epicpluginlib.core.tools.SpigotUpdateChecker;
import com.epicnicity322.yamlhandler.YamlConfigurationLoader;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Paths;
import java.util.List;

public final class EpicPluginLibBukkit extends JavaPlugin
{
    private static final @NotNull Logger logger = new Logger("[EpicPluginLib] ");
    private static @Nullable EpicPluginLibBukkit instance;

    public EpicPluginLibBukkit()
    {
        instance = this;
        logger.setLogger(getLogger());
    }

    public static @Nullable EpicPluginLibBukkit getInstance()
    {
        return instance;
    }

    @Override
    public void onEnable()
    {
        int dependingPlugins = 0;

        for (Plugin plugin : getServer().getPluginManager().getPlugins()) {
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
            logger.log("Something went wrong while loading main config, using default values.", ConsoleLogger.Level.WARN);
        }

        if (dependingPlugins == 0) {
            logger.log("Lib enabled but no dependencies found.", ConsoleLogger.Level.WARN);
        } else {
            logger.log("Lib enabled successfully.");
        }

        // Checking for updates:
        if (mainConfig.getConfiguration().getBoolean("Check for updates").orElse(true)) {
            SpigotUpdateChecker updateChecker = new SpigotUpdateChecker(80448, EpicPluginLib.version);

            updateChecker.check((available, version) -> {
                if (!available) return;

                // Update available alerting task.
                getServer().getScheduler().runTaskTimerAsynchronously(this, task -> {
                    configLoader.loadConfigurations();
                    if (!mainConfig.getConfiguration().getBoolean("Check for updates").orElse(true)) {
                        task.cancel();
                    }
                    logger.log("EpicPluginLib v" + version + " is available. Please update.");
                }, 0, 36000);
            });
        }

        Metrics metrics = new Metrics(this, 8337);
        boolean bStats = false;

        try {
            bStats = new YamlConfigurationLoader().load(Paths.get("plugins/bStats/config.yml")).getBoolean("enabled").orElse(false);
        } catch (Exception ignored) {
        }

        if (bStats) logger.log("EpicPluginLib is using bStats as metrics collector.");
    }
}