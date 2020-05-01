package com.epicnicity322.epicpluginlib.sponge;

import com.epicnicity322.epicpluginlib.sponge.logger.Logger;
import com.google.inject.Inject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.event.Level;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;

import java.nio.file.Path;

@Plugin(
        id = "epicpluginlib",
        name = "EpicPluginLib",
        version = "1.4",
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

    public EpicPluginLib()
    {
        epicPluginLib = this;
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
    public void onGameStartedServer(GameStartedServerEvent event)
    {
        Logger logger = new Logger("[EpicPluginLib] ", l4jLogger);
        int dependingPlugins = 0;

        for (PluginContainer plugin : Sponge.getPluginManager().getPlugins()) {
            if (plugin.getDependency("epicpluginlib").isPresent()) {
                logger.log("Dependency found: " + plugin + ".");
                ++dependingPlugins;
            }
        }

        if (dependingPlugins == 0)
            logger.log("Lib enabled but no dependencies found.", Level.WARN);
        else
            logger.log("Lib enabled successfully.");
    }

    @Override
    public @NotNull Path getFolder()
    {
        return folder;
    }

    @Override
    public @NotNull Path getPath()
    {
        return container.getSource().get();
    }
}
