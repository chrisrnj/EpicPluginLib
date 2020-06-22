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

import com.epicnicity322.epicpluginlib.core.util.PathUtils;
import com.epicnicity322.yamlhandler.Configuration;
import com.epicnicity322.yamlhandler.YamlConfigurationLoader;
import org.jetbrains.annotations.NotNull;
import org.yaml.snakeyaml.DumperOptions;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.regex.Pattern;

public class PluginConfig
{
    protected static final @NotNull YamlConfigurationLoader loader = YamlConfigurationLoader.build(2, DumperOptions.FlowStyle.BLOCK, '.');
    private final @NotNull LinkedList<Object> elements = new LinkedList<>();
    private final @NotNull Path path;
    private boolean loaded;
    private @NotNull String name;
    private @NotNull Configuration configuration;

    public PluginConfig(@NotNull Path path)
    {
        if (Files.isDirectory(path))
            throw new IllegalArgumentException("Path is pointing to a directory.");

        this.path = path;

        String fileName = path.getFileName().toString();

        if (fileName.contains("."))
            name = fileName.substring(0, fileName.lastIndexOf("."));
        else
            name = fileName;

        configuration = new Configuration(loader);
    }

    public void addDefaultComment(@NotNull String comment)
    {
        elements.add(comment);
    }

    public void addDefault(@NotNull String path, @NotNull Object value)
    {
        Object last = elements.isEmpty() ? null : elements.getLast();

        if (last instanceof Configuration) {
            Configuration lastConfig = (Configuration) last;

            if (lastConfig.contains(path.split(Pattern.quote(Character.toString(lastConfig.getSectionSeparator())))[0])) {
                lastConfig.set(path, value);
                return;
            }
        }

        Configuration newConfig = new Configuration(loader);

        newConfig.set(path, value);
        elements.add(newConfig);

        if (!isLoaded())
            configuration.set(path, value);
    }

    public boolean isLoaded()
    {
        return loaded;
    }

    protected final void setLoaded()
    {
        this.loaded = true;
    }

    public @NotNull Path getPath()
    {
        return path;
    }

    public @NotNull String getName()
    {
        return name;
    }

    protected final void setName(@NotNull String name)
    {
        this.name = name;
    }

    public @NotNull Configuration getConfiguration()
    {
        return configuration;
    }

    protected final void setConfiguration(@NotNull Configuration configuration)
    {
        this.configuration = configuration;
    }

    public void saveDefault() throws IOException
    {
        StringBuilder builder = new StringBuilder();

        for (Object object : elements)
            if (object instanceof String)
                builder.append("#").append(object.toString()).append("\n");
            else
                builder.append(((Configuration) object).dump()).append("\n");

        PathUtils.write(builder.toString(), path);
    }
}
