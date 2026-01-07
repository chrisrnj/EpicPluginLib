/*
 * EpicPluginLib - Library with basic utilities for Minecraft plugins.
 * Copyright (C) 2022-2026 Christiano Rangel
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

package com.epicnicity322.epicpluginlib.core.config;

import com.epicnicity322.yamlhandler.Configuration;
import com.epicnicity322.yamlhandler.exceptions.InvalidConfigurationException;
import com.epicnicity322.yamlhandler.loaders.YamlConfigurationLoader;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.Objects;

/**
 * A class that holds the default configuration and a changing configuration instance that is loaded by
 * {@link ConfigurationManager}.
 *
 * @see ConfigurationManager
 * @see #ConfigurationHolder(Path, ConfigurationManager, String)
 */
public class ConfigurationHolder
{
    private final @NotNull Path path;
    private final @NotNull String contents;
    private final @NotNull Configuration defaultConfiguration;
    private @NotNull Configuration configuration;

    /**
     * Creates a new ConfigurationHolder, and loads the default config from the specified contents using a new
     * {@link YamlConfigurationLoader}.
     *
     * @param path     The path of the configuration.
     * @param contents The contents of this config, used for the default config.
     * @see #ConfigurationHolder(Path, ConfigurationManager, String)
     */
    public ConfigurationHolder(@NotNull Path path, @NotNull String contents)
    {
        this(path, new YamlConfigurationLoader(), contents);
    }

    /**
     * Creates a ConfigurationHolder that loads the default config using the {@link YamlConfigurationLoader} from the
     * specified {@link ConfigurationManager}.
     *
     * @param path     The path of the configuration.
     * @param manager  The manager to get the {@link YamlConfigurationLoader} from.
     * @param contents The contents of this config, used for the default config.
     * @see #ConfigurationHolder(Path, YamlConfigurationLoader, String)
     */
    public ConfigurationHolder(@NotNull Path path, @NotNull ConfigurationManager manager, @NotNull String contents)
    {
        this(path, manager.loader, contents);
    }

    /**
     * Creates a ConfigurationHolder that uses a specific {@link YamlConfigurationLoader} for the default config.
     *
     * @param path     The path of the configuration.
     * @param loader   The specific loader used for loading the default config.
     * @param contents The contents of this config, used for the default config.
     */
    public ConfigurationHolder(@NotNull Path path, @NotNull YamlConfigurationLoader loader, @NotNull String contents)
    {
        try {
            this.defaultConfiguration = loader.load(contents);
        } catch (InvalidConfigurationException e) {
            throw new RuntimeException(e);
        }

        this.contents = contents;
        this.path = path;
        configuration = defaultConfiguration;
    }

    public @NotNull Path path()
    {
        return path;
    }

    public @NotNull String contents()
    {
        return contents;
    }

    public @NotNull Configuration defaultConfig()
    {
        return defaultConfiguration;
    }

    public synchronized @NotNull Configuration config()
    {
        return configuration;
    }

    synchronized final void setConfig(@NotNull Configuration configuration)
    {
        this.configuration = configuration;
    }

    /**
     * Tests whether the specified object is a {@link ConfigurationHolder} and that it has the same {@link #path()}
     * as this one.
     *
     * @param other the reference object with which to compare.
     * @return Whether the object is a {@link ConfigurationHolder} with same path.
     */
    @Override
    public boolean equals(@Nullable Object other)
    {
        if (this == other) return true;
        if (other == null || getClass() != other.getClass()) return false;

        ConfigurationHolder that = (ConfigurationHolder) other;

        return path.equals(that.path);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(path);
    }
}