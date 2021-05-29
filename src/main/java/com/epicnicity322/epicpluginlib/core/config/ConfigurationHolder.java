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

package com.epicnicity322.epicpluginlib.core.config;

import com.epicnicity322.yamlhandler.Configuration;
import com.epicnicity322.yamlhandler.YamlConfigurationLoader;
import com.epicnicity322.yamlhandler.exceptions.InvalidConfigurationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.Objects;

/**
 * A holder for your {@link Configuration} default and updated instance.
 *
 * @see ConfigurationLoader
 */
public class ConfigurationHolder
{
    private static final @NotNull YamlConfigurationLoader loader = new YamlConfigurationLoader();
    private final @NotNull Path path;
    private final @NotNull String contents;
    private final @NotNull Configuration defaultConfiguration;
    private @NotNull Configuration configuration;

    public ConfigurationHolder(@NotNull Path path, @NotNull String contents)
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

    public @NotNull Path getPath()
    {
        return path;
    }

    public @NotNull String getContents()
    {
        return contents;
    }

    public @NotNull Configuration getDefaultConfiguration()
    {
        return defaultConfiguration;
    }

    public synchronized @NotNull Configuration getConfiguration()
    {
        return configuration;
    }

    synchronized final void setConfiguration(@NotNull Configuration configuration)
    {
        this.configuration = configuration;
    }

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