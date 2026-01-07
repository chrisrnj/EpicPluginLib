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
import com.epicnicity322.epicpluginlib.core.util.PathLocker;
import com.epicnicity322.epicpluginlib.core.util.PathUtils;
import com.epicnicity322.yamlhandler.Configuration;
import com.epicnicity322.yamlhandler.exceptions.InvalidConfigurationException;
import com.epicnicity322.yamlhandler.loaders.YamlConfigurationLoader;
import com.epicnicity322.yamlhandler.serializers.CustomSerializer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A class used for loading configurations into {@link ConfigurationHolder} instances.
 */
public class ConfigurationManager
{
    private static final @NotNull Version[] dummyMinMaxVersion = new Version[0];
    final @NotNull YamlConfigurationLoader loader;
    private final @NotNull Map<ConfigurationHolder, Version[]> configurations = new ConcurrentHashMap<>();

    public ConfigurationManager(@NotNull CustomSerializer<?> @Nullable ... customSerializers)
    {
        loader = new YamlConfigurationLoader(customSerializers);
    }

    /**
     * Registers a {@link ConfigurationHolder} to have its {@link ConfigurationHolder#config()} value updated
     * on {@link #loadConfigurations()}.
     *
     * @param configuration The configuration to be updated on {@link #loadConfigurations()}.
     * @see #registerConfiguration(ConfigurationHolder, Version, Version)
     */
    public void registerConfiguration(@NotNull ConfigurationHolder configuration)
    {
        configurations.put(configuration, dummyMinMaxVersion);
    }

    /**
     * Registers a {@link ConfigurationHolder} to have its {@link ConfigurationHolder#config()} value updated
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
            minMaxVersion = dummyMinMaxVersion;
        } else {
            minMaxVersion = new Version[]{minimumVersion, maximumVersion};
        }

        configurations.put(configuration, minMaxVersion);
    }

    /**
     * Removes a {@link ConfigurationHolder} from this manager, so the {@link ConfigurationHolder#config()}
     * value is not updated anymore on {@link #loadConfigurations()}.
     *
     * @param configuration The configuration to remove from being updated on {@link #loadConfigurations()}.
     */
    public void unregisterConfiguration(@NotNull ConfigurationHolder configuration)
    {
        configurations.remove(configuration);
    }

    /**
     * An unmodifiable set of {@link ConfigurationHolder}s that have their {@link ConfigurationHolder#config()}
     * value updated on {@link #loadConfigurations()}.
     *
     * @return A set of {@link ConfigurationHolder}s.
     */
    public @NotNull Set<ConfigurationHolder> configurations()
    {
        return Collections.unmodifiableSet(configurations.keySet());
    }

    /**
     * Saves the default {@link Configuration} of registered {@link ConfigurationHolder}s if they are outdated or
     * inexistent.
     * <p>
     * This method also updates the value from the method {@link ConfigurationHolder#config()} to the current
     * {@link Configuration} values that were set by the user on the {@link Path} specified on {@link ConfigurationHolder#path()}.
     *
     * @return A map with thrown exceptions.
     */
    public @NotNull Map<ConfigurationHolder, Exception> loadConfigurations()
    {
        Map<ConfigurationHolder, Exception> exceptions = new ConcurrentHashMap<>((int) (configurations.size() / .75f) + 1);

        configurations.entrySet().stream().parallel().forEach(configurationEntry -> {
            ConfigurationHolder config = configurationEntry.getKey();
            Path path = config.path();
            Version[] minAndMaxVersions = configurationEntry.getValue();

            try (PathLocker.LockToken ignored = PathLocker.lock(path)) {
                Configuration configuration = null;
                boolean save = false;

                if (Files.exists(path)) {
                    if (minAndMaxVersions != dummyMinMaxVersion) {
                        Version version = null;

                        try {
                            configuration = loader.load(path);
                            Optional<Object> versionOptional = configuration.getObject("Version");

                            if (versionOptional.isPresent()) version = new Version(versionOptional.get().toString());
                        } catch (InvalidConfigurationException | IllegalArgumentException | IOException ignored1) {
                        }

                        // Set save to true if Version is not within accepted range.
                        if (version == null || ((minAndMaxVersions[0] != null && version.compareTo(minAndMaxVersions[0]) < 0) || (minAndMaxVersions[1] != null && version.compareTo(minAndMaxVersions[1]) > 0))) {
                            Files.move(path, PathUtils.getUniquePath(path.getParent().resolve("outdated " + path.getFileName().toString())));
                            save = true;
                        }
                    }
                } else if (Files.notExists(path)) save = true;

                if (save) PathUtils.write(config.contents(), path);

                if (configuration != null && !save) {
                    config.setConfig(configuration);
                } else {
                    config.setConfig(loader.load(path));
                }
            } catch (InvalidConfigurationException | IOException e) {
                exceptions.put(config, e);
            }
        });

        return exceptions;
    }
}
