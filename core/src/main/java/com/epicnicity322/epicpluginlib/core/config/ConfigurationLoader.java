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

import com.epicnicity322.epicpluginlib.core.tools.Version;
import com.epicnicity322.yamlhandler.Configuration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Set;

/**
 * @deprecated Use {@link ConfigurationManager}
 */
@Deprecated
public class ConfigurationLoader
{
    private final @NotNull ConfigurationManager manager = new ConfigurationManager();

    /**
     * Registers a {@link ConfigurationHolder} to have its {@link ConfigurationHolder#getConfiguration()} value updated
     * on {@link #loadConfigurations()}.
     *
     * @param configuration The configuration to be updated on {@link #loadConfigurations()}.
     * @see #registerConfiguration(ConfigurationHolder, Version, Version)
     */
    public void registerConfiguration(@NotNull ConfigurationHolder configuration)
    {
        manager.registerConfiguration(configuration);
    }

    /**
     * Registers a {@link ConfigurationHolder} to have its {@link ConfigurationHolder#getConfiguration()} value updated
     * on {@link #loadConfigurations()}.
     * <p>
     * Configurations can have minimum or maximum versions. Configurations that don't have "Version" key or is out of
     * the specified range will be restored to their default values.
     *
     * @param configuration  The configuration to be updated on {@link #loadConfigurations()}.
     * @param minimumVersion The minimum version this configuration can have to not be restored.
     * @param maximumVersion The maximum version this configuration can have to not be restored.
     * @see #loadConfigurations()
     */
    public void registerConfiguration(@NotNull ConfigurationHolder configuration, @Nullable Version minimumVersion, @Nullable Version maximumVersion)
    {
        manager.registerConfiguration(configuration, minimumVersion, maximumVersion);
    }

    /**
     * Removes a {@link ConfigurationHolder} from this loader, so the {@link ConfigurationHolder#getConfiguration()}
     * value is not updated anymore on {@link #loadConfigurations()}.
     *
     * @param configuration The configuration to remove from being updated on {@link #loadConfigurations()}.
     */
    public void unregisterConfiguration(@NotNull ConfigurationHolder configuration)
    {
        manager.unregisterConfiguration(configuration);
    }

    /**
     * An unmodifiable set of {@link ConfigurationHolder}s that have their {@link ConfigurationHolder#getConfiguration()}
     * value updated on {@link #loadConfigurations()}.
     *
     * @return A set of {@link ConfigurationHolder}s.
     */
    public @NotNull Set<ConfigurationHolder> getConfigurations()
    {
        return manager.configurations();
    }

    /**
     * Saves the default {@link Configuration} of registered {@link ConfigurationHolder}s if they are outdated or
     * inexistent.
     * <p>
     * This method also updates the value from the method {@link ConfigurationHolder#getConfiguration()} to the current
     * {@link Configuration} values that were set by the user on the {@link Path} specified on {@link ConfigurationHolder#getPath()}.
     *
     * @return A map with thrown exceptions.
     */
    public @NotNull HashMap<ConfigurationHolder, Exception> loadConfigurations()
    {
        return new HashMap<>(manager.loadConfigurations());
    }
}