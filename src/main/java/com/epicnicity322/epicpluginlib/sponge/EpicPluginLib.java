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

package com.epicnicity322.epicpluginlib.sponge;

import com.epicnicity322.epicpluginlib.core.logger.ConsoleLogger;
import com.epicnicity322.epicpluginlib.sponge.logger.Logger;
import com.google.inject.Inject;
import org.bstats.sponge.MetricsLite2;
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
    public EpicPluginLib(MetricsLite2.Factory metricsFactory)
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

        if (metricsConfigManager.getGlobalCollectionState() == Tristate.TRUE)
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
