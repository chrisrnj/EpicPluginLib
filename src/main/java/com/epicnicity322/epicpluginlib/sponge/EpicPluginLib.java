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

package com.epicnicity322.epicpluginlib.sponge;

import com.epicnicity322.epicpluginlib.core.logger.ConsoleLogger;
import com.epicnicity322.epicpluginlib.sponge.logger.Logger;
import com.google.inject.Inject;
import org.bstats.sponge.Metrics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.util.Tristate;
import org.spongepowered.api.util.metric.MetricsConfigManager;

import java.nio.file.Path;

@Plugin(
        id = "epicpluginlib",
        name = "EpicPluginLib",
        version = com.epicnicity322.epicpluginlib.core.EpicPluginLib.versionString,
        description = "Allows plugins to extract configurations and languages, handle commands, send messages, report" +
                " errors, and check for updates more easily.")
public final class EpicPluginLib implements com.epicnicity322.epicpluginlib.core.EpicPluginLib
{
    private static com.epicnicity322.epicpluginlib.core.EpicPluginLib epicPluginLib;

    @Inject
    private org.slf4j.Logger l4jLogger;

    @Inject
    @ConfigDir(sharedRoot = false)
    private Path folder;

    @Inject
    private PluginContainer container;

    @Inject
    private MetricsConfigManager metricsConfigManager;

    @Inject
    public EpicPluginLib(Metrics.Factory metricsFactory)
    {
        epicPluginLib = this;
        metricsFactory.make(8342);
    }

    /**
     * Gets an instance of {@link com.epicnicity322.epicpluginlib.core.EpicPluginLib}.
     *
     * @return The instance or null if the lib wasn't loaded by sponge yet.
     */
    public static @Nullable com.epicnicity322.epicpluginlib.core.EpicPluginLib getEpicPluginLib()
    {
        return epicPluginLib;
    }

    @Listener
    public void onGameInitialization(GameInitializationEvent event)
    {
        Logger logger = new Logger("[EpicPluginLib] ", l4jLogger);
        int dependingPlugins = 0;

        for (PluginContainer plugin : Sponge.getPluginManager().getPlugins()) {
            if (plugin.getDependency("epicpluginlib").isPresent()) {
                logger.log("Dependency found: " + plugin.getId() + ".");
                ++dependingPlugins;
            }
        }

        if (dependingPlugins == 0)
            logger.log("Lib enabled but no dependencies found.", ConsoleLogger.Level.WARN);
        else
            logger.log("Lib enabled successfully.");

        if (metricsConfigManager.getCollectionState(container) == Tristate.TRUE)
            logger.log("EpicPluginLib is using bStats as metrics collector.");
    }

    @Override
    public @NotNull Path getFolder()
    {
        return folder;
    }

    @Override
    public @NotNull Path getPath()
    {
        return container.getSource().orElseThrow(NullPointerException::new);
    }
}
