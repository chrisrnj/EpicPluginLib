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

package com.epicnicity322.epicpluginlib.core.config;

import com.epicnicity322.epicpluginlib.core.tools.Version;
import com.epicnicity322.yamlhandler.Configuration;
import com.epicnicity322.yamlhandler.exceptions.InvalidConfigurationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class ConfigLoader
{
    private final @NotNull Map<PluginConfig, Version[]> configurations;

    /**
     * Create an empty config loader that you can use to register configurations later.
     */
    public ConfigLoader()
    {
        configurations = new HashMap<>();
    }

    /**
     * Creates a config loader to save, load, and get configurations.
     * <p>
     * Configurations with the same file name will have the prefix of the parent file added to their name.
     *
     * @param configurations The configurations you want to save, load and get.
     */
    public ConfigLoader(@NotNull Collection<PluginConfig> configurations)
    {
        this.configurations = new HashMap<>();

        configurations.forEach(pluginConfig -> {
            if (pluginConfig != null)
                this.configurations.put(pluginConfig, null);
        });

        setUniqueNames(configurations);
    }

    private static void setUniqueNames(Collection<PluginConfig> configs)
    {
        // Sorting configs with the same name.
        HashMap<String, HashSet<PluginConfig>> sortedConfigs = new HashMap<>();

        for (PluginConfig config : configs) {
            String name = config.getPath().getFileName().toString();

            for (PluginConfig config1 : configs) {
                if (name.equals(config1.getPath().getFileName().toString())) {
                    HashSet<PluginConfig> configsWithTheSameName;

                    if (sortedConfigs.containsKey(name))
                        configsWithTheSameName = sortedConfigs.get(name);
                    else {
                        configsWithTheSameName = new HashSet<>();
                        sortedConfigs.put(name, configsWithTheSameName);
                    }

                    configsWithTheSameName.add(config1);
                }
            }
        }

        // Converting configs with the same name to string and adding the parent path to the name of the configs that have the same name.

        for (Map.Entry<String, HashSet<PluginConfig>> sortedConfigEntry : sortedConfigs.entrySet()) {
            HashSet<PluginConfig> pathsWithTheSameName = sortedConfigEntry.getValue();
            HashMap<String, PluginConfig> pathNames = new HashMap<>();

            if (pathsWithTheSameName.size() > 1) {
                pathsWithTheSameName.forEach(config -> pathNames.put(config.getPath().toAbsolutePath().toString(), config));

                whileSameFirstChar:
                while (!pathNames.containsKey(sortedConfigEntry.getKey()) && haveTheSameFirstChar(pathNames.keySet())) {
                    for (Map.Entry<String, PluginConfig> name : new HashSet<>(pathNames.entrySet())) {
                        if (name.getKey().length() > 1) {
                            pathNames.remove(name.getKey());
                            String newName = name.getKey().substring(1);
                            pathNames.put(newName, name.getValue());
                            name.getValue().setName(newName);
                        } else
                            break whileSameFirstChar;
                    }
                }
            } else {
                PluginConfig config = pathsWithTheSameName.iterator().next();
                config.setName(config.getPath().getFileName().toString());
            }
        }
    }

    private static boolean haveTheSameFirstChar(Set<String> strings)
    {
        Character c = null;

        for (String s : strings) {
            if (c == null) {
                if (s.isEmpty()) {
                    return false;
                }

                c = s.charAt(0);
            } else {
                if (!s.startsWith(c.toString())) {
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * Gets a {@link PluginConfig} registered in this {@link ConfigLoader} instance by name.
     *
     * @param name The name of the {@link PluginConfig}
     * @return The {@link PluginConfig} with this name or null if no {@link PluginConfig} with that name was found.
     */
    public @Nullable PluginConfig getConfiguration(@NotNull String name)
    {
        for (PluginConfig config : configurations.keySet())
            if (config.getName().equals(name))
                return config;

        return null;
    }

    /**
     * Gets all registered configurations.
     *
     * @return Set of registered configurations.
     */
    public @NotNull Set<PluginConfig> getConfigurations()
    {
        return Collections.unmodifiableSet(configurations.keySet());
    }

    /**
     * Adds a configuration to be loaded and saved by this {@link ConfigLoader}.
     * <p>
     * If this configuration has the same file name as other configuration registered on this loader, the parent file
     * name will be added as prefix to these configurations.
     *
     * @param config The configuration to be added.
     */
    public void registerConfiguration(@NotNull PluginConfig config)
    {
        configurations.put(config, null);
        setUniqueNames(configurations.keySet());
    }

    /**
     * Adds a configuration to be loaded and saved by this {@link ConfigLoader}.
     * <p>
     * If this configuration has the same file name as other configuration registered on this loader, the parent file
     * name will be added as prefix to these configurations.
     * <p>
     * If this configuration already exists and its Version node is lower or greater than the versions specified here,
     * the configuration will be restored.
     *
     * @param config     The configuration to be added.
     * @param minVersion The minimum version this configuration can have to not be restored.
     * @param maxVersion The maximum version this configuration can have to not be restored.
     */
    public void registerConfiguration(@NotNull PluginConfig config, @NotNull Version minVersion, @NotNull Version maxVersion)
    {
        configurations.put(config, new Version[]{minVersion, maxVersion});
        setUniqueNames(configurations.keySet());
    }

    /**
     * Removes a configuration from being loaded and saved by this {@link ConfigLoader}.
     * <p>
     * If this configuration had the same file name as others configurations registered on this loader, the name of
     * configurations on this loader may be changed.
     *
     * @param config The registered config on this loader to be removed.
     */
    public void unregisterConfiguration(@NotNull PluginConfig config)
    {
        configurations.remove(config);
        setUniqueNames(configurations.keySet());
    }

    /**
     * Loads {@link Configuration} instances of {@link PluginConfig} that you can get through
     * {@link PluginConfig#getConfiguration()}.
     *
     * @throws IOException If it was not possible to save this config.
     */
    public void loadConfigurations() throws IOException
    {
        for (Map.Entry<PluginConfig, Version[]> configEntry : configurations.entrySet()) {
            PluginConfig config = configEntry.getKey();
            Path path = config.getPath();
            boolean save = false;

            if (Files.exists(path)) {
                Version[] minAndMaxVersions = configEntry.getValue();

                if (minAndMaxVersions != null) {
                    Version version = null;

                    try {
                        Configuration configuration = PluginConfig.loader.load(path);
                        Optional<String> versionOptional = configuration.getString("Version");

                        if (versionOptional.isPresent())
                            version = new Version(versionOptional.get());
                    } catch (InvalidConfigurationException | IllegalArgumentException ignored) {
                    }

                    if (version == null || (version.compareTo(minAndMaxVersions[0]) < 0 || version.compareTo(minAndMaxVersions[1]) > 0)) {
                        Files.move(path, path.getParent().resolve("outdated " + path.getFileName().toString()));
                        save = true;
                    }
                }
            } else {
                save = true;
            }

            if (save)
                config.saveDefault();

            try {
                config.setLoaded();
                config.setConfiguration(PluginConfig.loader.load(path));
            } catch (Exception ignored) {
            }
        }
    }
}
