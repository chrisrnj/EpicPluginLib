package com.epicnicity322.epicpluginlib.core.config;

import com.timvisee.yamlwrapper.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * Reloads YamlConfiguration instances from {@link PluginConfig}.
 */
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
     *
     * @param configurations A map with the configurations and the allowed versions this configuration can have before
     *                       getting outdated.
     */
    public ConfigLoader(@NotNull Map<@NotNull PluginConfig, @NotNull Collection<String>> configurations)
    {
        Set<PluginConfig> configSet = configurations.keySet();

        for (PluginConfig config : configSet) {
            if (config == null || configurations.get(config) == null) {
                throw new NullPointerException();
            }

            for (PluginConfig config1 : configSet) {
                if (config != config1 && config.getName().equals(config1.getName())) {
                    Path parent = config.getPath().getParent();
                    Path parent1 = config1.getPath().getParent();

                    if (parent1.startsWith(parent)) {
                        config1.name = parent1.getFileName().toString() + System.getProperty("file.separator") +
                                config1.getName();
                    } else {
                        config.name = parent.getFileName().toString() + System.getProperty("file.separator") +
                                config.getName();
                    }
                }
            }
        }

        this.configurations = configurations;
    }

    /**
     * Creates a config loader to save, load, and get configurations. This instantiation makes so the versions of the
     * configurations in this collection will not be checked.
     *
     * @param configurations The configurations you want to save, load and get.
     */
    public ConfigLoader(@NotNull Collection<PluginConfig> configurations)
    {
        this.configurations = new HashMap<>();

        configurations.forEach((pluginConfig) -> {
            if (pluginConfig != null)
                this.configurations.put(pluginConfig, Collections.emptySet());
        });
    }

    /**
     * Gets a {@link PluginConfig} registered in this {@link ConfigLoader} instance by name.
     *
     * @param name The name of the {@link PluginConfig}
     * @return The {@link PluginConfig} with this name or null if no {@link PluginConfig} with that name was found.
     */
    public @Nullable PluginConfig getConfiguration(@NotNull String name)
    {
        for (PluginConfig config : configurations.keySet()) {
            if (config.getName().equals(name)) {
                return config;
            }
        }

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
     *
     * @param config              The configuration to be added.
     * @param nonOutdatedVersions The versions this configuration can have before getting outdated.
     */
    public void registerConfiguration(@NotNull PluginConfig config, @Nullable String... nonOutdatedVersions)
    {
        HashSet<String> newSet = new HashSet<>();

        Collections.addAll(newSet, nonOutdatedVersions);
        configurations.put(config, newSet);
    }

    /**
     * Adds a configuration to be loaded and saved by this {@link ConfigLoader}.
     *
     * @param config              The configuration to be added.
     * @param nonOutdatedVersions The versions this configuration can have before getting outdated.
     */
    public void registerConfiguration(@NotNull PluginConfig config, @NotNull Collection<String> nonOutdatedVersions)
    {
        configurations.put(config, nonOutdatedVersions);
    }

    /**
     * Removes a configuration from being loaded and saved by this {@link ConfigLoader}.
     *
     * @param config The registered config in this {@link ConfigLoader} that will be removed.
     */
    public void unregisterConfiguration(@NotNull PluginConfig config)
    {
        configurations.remove(config);
    }

    /**
     * Loads {@link YamlConfiguration} instances of {@link PluginConfig} that you can get through
     * {@link PluginConfig#getYamlConfiguration()}.
     *
     * @throws IOException If it was not possible to save this config or update it.
     */
    public void loadConfigurations() throws IOException
    {
        for (PluginConfig config : configurations.keySet()) {
            Path path = config.getPath();
            boolean save = false;

            if (Files.exists(path)) {
                YamlConfiguration yamlConfiguration = YamlConfiguration.loadFromFile(path.toFile());
                String version = yamlConfiguration.getString("Version", null);

                if (version != null && !configurations.get(config).contains(version)) {
                    Files.move(path, path.getParent().resolve("outdated " + path.getFileName().toString()));
                    save = true;
                }
            } else {
                save = true;
            }

            if (save)
                config.save();

            config.yamlConfiguration = YamlConfiguration.loadFromFile(path.toFile());
        }
    }
}
