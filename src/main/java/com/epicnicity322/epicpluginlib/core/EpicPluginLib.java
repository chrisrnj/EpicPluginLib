package com.epicnicity322.epicpluginlib.core;

import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;

public interface EpicPluginLib
{
    /**
     * The folder where temp files will be stored.
     *
     * @return The data folder of {@link EpicPluginLib}
     */
    @NotNull Path getFolder();

    /**
     * The path to the jar of {@link EpicPluginLib}.
     *
     * @return The jar of {@link EpicPluginLib}
     */
    @NotNull Path getPath();
}
