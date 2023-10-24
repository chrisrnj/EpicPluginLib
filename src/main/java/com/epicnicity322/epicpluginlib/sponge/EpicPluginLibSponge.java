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

package com.epicnicity322.epicpluginlib.sponge;

import com.epicnicity322.epicpluginlib.core.EpicPluginLib;
import com.epicnicity322.epicpluginlib.core.config.ConfigurationHolder;
import com.epicnicity322.epicpluginlib.core.config.ConfigurationLoader;
import com.epicnicity322.epicpluginlib.core.logger.ConsoleLogger;
import com.epicnicity322.epicpluginlib.core.tools.GitHubUpdateChecker;
import com.epicnicity322.epicpluginlib.sponge.logger.Logger;
import com.epicnicity322.epicpluginlib.sponge.metrics.Metrics;
import com.google.inject.Inject;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.lifecycle.ConstructPluginEvent;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.util.Ticks;
import org.spongepowered.api.util.Tristate;
import org.spongepowered.api.util.metric.MetricsConfigManager;
import org.spongepowered.plugin.PluginContainer;
import org.spongepowered.plugin.builtin.jvm.Plugin;

import java.nio.file.Path;

@Plugin("epicpluginlib")
public final class EpicPluginLibSponge
{
    private final @NotNull Logger logger;
    private final @NotNull ConfigurationHolder mainConfig;
    private final @NotNull MetricsConfigManager metricsConfigManager;

    @Inject
    public EpicPluginLibSponge(@NotNull org.apache.logging.log4j.Logger l4jLogger,
                               @ConfigDir(sharedRoot = false) @NotNull Path dataFolder,
                               @NotNull Metrics.Factory metricsFactory,
                               @NotNull MetricsConfigManager metricsConfigManager)
    {
        this.logger = new Logger("[EpicPluginLib] ", l4jLogger);
        this.mainConfig = new ConfigurationHolder(dataFolder.resolve("config.yml"), "Check for updates: true");
        this.metricsConfigManager = metricsConfigManager;
        metricsFactory.make(8342);
    }

    @Listener
    public void onConstructPlugin(ConstructPluginEvent event)
    {
        int dependingPlugins = 0;

        for (PluginContainer plugin : Sponge.pluginManager().plugins()) {
            if (plugin.metadata().dependency("epicpluginlib").isPresent()) {
                logger.log("Dependency found: " + plugin.metadata().id() + ".");
                ++dependingPlugins;
            }
        }

        ConfigurationLoader configLoader = new ConfigurationLoader();

        configLoader.registerConfiguration(mainConfig);
        if (!configLoader.loadConfigurations().isEmpty()) {
            logger.log("Something went wrong while loading main config, using default values.", ConsoleLogger.Level.WARN);
        }

        if (dependingPlugins == 0)
            logger.log("Lib enabled but no dependencies found.", ConsoleLogger.Level.WARN);
        else
            logger.log("Lib enabled successfully.");

        // Checking for updates:
        if (mainConfig.getConfiguration().getBoolean("Check for updates").orElse(true)) {
            GitHubUpdateChecker updateChecker = new GitHubUpdateChecker("chrisrnj/EpicPluginLib", EpicPluginLib.version);

            updateChecker.check((available, version) -> {
                if (!available) return;

                // Update available alerting task.
                Sponge.asyncScheduler().submit(Task.builder()
                        .plugin(event.plugin())
                        .interval(Ticks.of(36000))
                        .execute(() -> logger.log("EpicPluginLib v" + version + " is available. Please update."))
                        .build());
            });
        }

        if (metricsConfigManager.collectionState(event.plugin()) == Tristate.TRUE)
            logger.log("EpicPluginLib is using bStats as metrics collector.");
    }
}
