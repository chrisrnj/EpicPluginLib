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

package com.epicnicity322.epicpluginlib.core.config;

import com.epicnicity322.epicpluginlib.core.tools.Version;
import com.epicnicity322.epicpluginlib.core.util.PathUtils;
import com.epicnicity322.yamlhandler.Configuration;
import com.epicnicity322.yamlhandler.YamlConfigurationLoader;
import com.epicnicity322.yamlhandler.exceptions.InvalidConfigurationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.yaml.snakeyaml.DumperOptions;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class ConfigurationLoader
{
    static final @NotNull YamlConfigurationLoader loader = new YamlConfigurationLoader('.', 2, DumperOptions.FlowStyle.BLOCK);
    private final @NotNull HashMap<ConfigurationHolder, Version[]> configurations = new HashMap<>();

    /**
     * Registers a {@link ConfigurationHolder} to have its {@link ConfigurationHolder#getConfiguration()} value updated
     * on {@link #loadConfigurations()}.
     *
     * @param configuration The configuration to be updated on {@link #loadConfigurations()}.
     * @see #registerConfiguration(ConfigurationHolder, Version, Version)
     */
    public void registerConfiguration(@NotNull ConfigurationHolder configuration)
    {
        synchronized (configurations) {
            configurations.put(configuration, null);
        }
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
        Version[] minMaxVersion;

        if (minimumVersion == null && maximumVersion == null) {
            minMaxVersion = null;
        } else {
            minMaxVersion = new Version[2];
            minMaxVersion[0] = minimumVersion;
            minMaxVersion[1] = maximumVersion;
        }

        synchronized (configurations) {
            configurations.put(configuration, minMaxVersion);
        }
    }

    /**
     * Removes a {@link ConfigurationHolder} from this loader, so the {@link ConfigurationHolder#getConfiguration()}
     * value is not updated anymore on {@link #loadConfigurations()}.
     *
     * @param configuration The configuration to remove from being updated on {@link #loadConfigurations()}.
     */
    public void unregisterConfiguration(@NotNull ConfigurationHolder configuration)
    {
        synchronized (configurations) {
            configurations.remove(configuration);
        }
    }

    /**
     * An unmodifiable set of {@link ConfigurationHolder}s that have their {@link ConfigurationHolder#getConfiguration()}
     * value updated on {@link #loadConfigurations()}.
     *
     * @return A set of {@link ConfigurationHolder}s.
     */
    public @NotNull Set<ConfigurationHolder> getConfigurations()
    {
        synchronized (configurations) {
            return Collections.unmodifiableSet(configurations.keySet());
        }
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
        synchronized (configurations) {
            HashMap<ConfigurationHolder, Exception> exceptions = new HashMap<>(configurations.size());

            for (Map.Entry<ConfigurationHolder, Version[]> configurationEntry : configurations.entrySet()) {
                ConfigurationHolder configurationHolder = configurationEntry.getKey();
                Version[] minAndMaxVersions = configurationEntry.getValue();

                try {
                    Configuration configuration = null;
                    Path path = configurationHolder.getPath();
                    boolean save = false;

                    if (Files.exists(path)) {
                        if (minAndMaxVersions != null) {
                            Version version = null;

                            try {
                                configuration = loader.load(path);
                                Optional<String> versionOptional = configuration.getString("Version");

                                if (versionOptional.isPresent())
                                    version = new Version(versionOptional.get());
                            } catch (InvalidConfigurationException | IllegalArgumentException | IOException ignored) {
                            }

                            if (version == null || ((minAndMaxVersions[0] != null && version.compareTo(minAndMaxVersions[0]) < 0) || (minAndMaxVersions[1] != null && version.compareTo(minAndMaxVersions[1]) > 0))) {
                                Files.move(path, PathUtils.getUniquePath(path.getParent().resolve("outdated " + path.getFileName().toString())));
                                save = true;
                            }
                        }
                    } else if (Files.notExists(path)) {
                        save = true;
                    }

                    if (save) {
                        PathUtils.write(configurationHolder.getContents(), path);
                    }

                    if (configuration != null && !save) {
                        configurationHolder.setConfiguration(configuration);
                    } else {
                        configurationHolder.setConfiguration(loader.load(path));
                    }
                } catch (InvalidConfigurationException | IOException e) {
                    exceptions.put(configurationHolder, e);
                }
            }

            return exceptions;
        }
    }
}
