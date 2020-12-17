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