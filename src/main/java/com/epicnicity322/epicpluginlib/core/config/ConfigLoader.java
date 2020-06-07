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
    private final @NotNull Map<PluginConfig, Collection<String>> configurations;

    /**
     * Create an empty config loader that you can use to register configurations later.
     */
    public ConfigLoader()
    {
        configurations = new HashMap<>();
    }

    /**
     * Creates a config loader to save, reload, and get configurations. This instantiation allows you to check for
     * outdated configurations.
     * <p>
     * Configurations with the paths with the same file name will have the prefix of the parent file added to their name.
     *
     * @param configurations A map with the configurations and the allowed versions this configuration can have before
     *                       getting outdated.
     */
    public ConfigLoader(@NotNull Map<@NotNull PluginConfig, @NotNull Collection<String>> configurations)
    {
        this.configurations = configurations;

        setUniqueNames(configurations.keySet());
    }

    /**
     * Creates a config loader to save, load, and get configurations. This instantiation makes so the versions of the
     * configurations in this collection will not be checked.
     * <p>
     * Configurations with the paths with the same file name will have the prefix of the parent file added to their name.
     *
     * @param configurations The configurations you want to save, load and get.
     */
    public ConfigLoader(@NotNull Collection<PluginConfig> configurations)
    {
        this.configurations = new HashMap<>();

        configurations.forEach(pluginConfig -> {
            if (pluginConfig != null)
                this.configurations.put(pluginConfig, Collections.emptySet());
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
     * If this configuration has the same name as others configurations on this loader the name of this configurations
     * or others on this loader may be changed, so beware.
     *
     * @param config              The configuration to be added.
     * @param nonOutdatedVersions The versions this configuration can have before getting outdated.
     */
    public void registerConfiguration(@NotNull PluginConfig config, @Nullable String... nonOutdatedVersions)
    {
        HashSet<String> newSet = new HashSet<>();

        Collections.addAll(newSet, nonOutdatedVersions);
        configurations.put(config, newSet);
        setUniqueNames(configurations.keySet());
    }

    /**
     * Adds a configuration to be loaded and saved by this {@link ConfigLoader}.
     * <p>
     * If this configuration has the same name as others configurations on this loader the name of this configurations
     * or others on this loader may be changed, so beware.
     *
     * @param config              The configuration to be added.
     * @param nonOutdatedVersions The versions this configuration can have before getting outdated.
     */
    public void registerConfiguration(@NotNull PluginConfig config, @NotNull Collection<String> nonOutdatedVersions)
    {
        configurations.put(config, nonOutdatedVersions);
        setUniqueNames(configurations.keySet());
    }

    /**
     * Removes a configuration from being loaded and saved by this {@link ConfigLoader}.
     * <p>
     * If this configuration had the same name as others configurations on this loader, the name of configurations on
     * this loader may be changed, so beware.
     *
     * @param config The registered config in this {@link ConfigLoader} that will be removed.
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
        for (PluginConfig config : configurations.keySet()) {
            Path path = config.getPath();
            boolean save = false;

            if (Files.exists(path)) {
                String version;

                try {
                    Configuration configuration = PluginConfig.loader.load(path);
                    version = configuration.getString("Version").orElse("");
                } catch (InvalidConfigurationException e) {
                    version = null;
                }

                if (version == null || (!version.equals("") && !configurations.get(config).contains(version))) {
                    Files.move(path, path.getParent().resolve("outdated " + path.getFileName().toString()));
                    save = true;
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
