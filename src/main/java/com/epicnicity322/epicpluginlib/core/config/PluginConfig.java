package com.epicnicity322.epicpluginlib.core.config;

import com.epicnicity322.epicpluginlib.core.util.ObjectUtils;
import com.epicnicity322.epicpluginlib.core.util.PathUtils;
import com.epicnicity322.epicpluginlib.core.util.StringUtils;
import com.timvisee.yamlwrapper.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.regex.Pattern;

/**
 * Saves and loads yaml configurations.
 *
 * @see ConfigLoader
 */
public class PluginConfig
{
    private static final @NotNull Pattern allowedKeyCharsRegex = Pattern.compile("^[A-Za-z0-9_'. -]+$");
    private static final @NotNull Pattern sectionSeparatorRegex = Pattern.compile("\\.");
    private static final @NotNull Pattern lineSeparatorRegex = Pattern.compile(ObjectUtils.getOrDefault(
            System.getProperty("line.separator"), "\n"));
    private final @NotNull Path path;
    protected @NotNull String name;
    protected YamlConfiguration yamlConfiguration;
    private @NotNull String content;
    private LinkedList<LinkedHashMap<String, Object>> elements;

    /**
     * Creates an instance of {@link PluginConfig} from path. If path exists then the default values are loaded from the
     * file in this path, if doesn't exists, you need to add your own defaults.
     *
     * @param path The path this configuration is/will be located.
     * @throws IOException If path exists and it wasn't possible to get the default values.
     */
    public PluginConfig(@NotNull Path path) throws IOException
    {
        if (Files.isDirectory(path))
            throw new IllegalArgumentException("path is pointing to a directory.");

        String fileName = path.getFileName().toString();

        this.path = path;
        name = fileName.substring(0, fileName.lastIndexOf("."));

        String content = PathUtils.read(path);

        // PathUtils#read returns null if file doesn't exists or is a directory.
        if (content == null) {
            elements = new LinkedList<>();
            yamlConfiguration = new YamlConfiguration();
            this.content = "";
        } else {
            this.content = content;
            updateElements();
            updateYaml();
        }
    }

    /**
     * Adds a comment into your yaml. The # are automatically added.
     *
     * @param comment The string you want to be commented.
     */
    public void addComment(@NotNull String comment)
    {
        LinkedHashMap<String, Object> newMap = new LinkedHashMap<>();

        newMap.put(comment, null);
        elements.add(newMap);
        updateContent();
    }

    /**
     * Adds a key with a value to your yaml and updates {@link #getYamlConfiguration()} instance.
     *
     * @param key   The key with sections separated by dots orIllegalArgumentException a comment.
     * @param value The value this key will have.
     * @throws UnsupportedOperationException If a char in key parameter is not supported.
     * @throws IllegalArgumentException      If a section has no chars or a section is not quoted correctly.
     */
    public void add(@NotNull String key, @NotNull Object value)
    {
        if (!allowedKeyCharsRegex.matcher(key).matches())
            throw new UnsupportedOperationException("Illegal chars in key parameter");

        String[] nodes = sectionSeparatorRegex.split(key);
        StringBuilder builder = new StringBuilder();
        int nodeAmount = nodes.length;

        for (String node : nodes) {
            String fixedNode = node.trim();

            if (fixedNode.equals(""))
                throw new IllegalArgumentException("key can not have sections with no chars.");

            if (fixedNode.contains("'"))
                if (StringUtils.count(fixedNode, '\'') > 2 || fixedNode.indexOf("'") != 0
                        || fixedNode.lastIndexOf("'") + 1 != fixedNode.length())
                    throw new IllegalArgumentException("Single quotes may only be in the start and end of a section.");

            builder.append(fixedNode).append(".");
        }

        key = builder.toString();
        key = key.substring(0, key.length() - 1);

        if (!elements.isEmpty()) {
            // If key has a section
            if (nodeAmount > 1) {
                LinkedHashMap<String, Object> lastElement = elements.getLast();

                // Checking if lastElement is not a comment.
                if (!lastElement.containsValue(null)) {
                    String firstSection = nodes[0].trim();

                    for (String keys : lastElement.keySet()) {
                        // Checking if any key of lastElement has the same mother section as firstSection.
                        if (keys.substring(0, keys.indexOf(".")).equals(firstSection)) {
                            lastElement.put(key, value);
                            updateContent();
                            updateYaml();
                            return;
                        }
                    }
                }
            }
        }

        LinkedHashMap<String, Object> newMap = new LinkedHashMap<>();

        newMap.put(key, value);
        elements.add(newMap);
        updateContent();
        updateYaml();
    }

    /**
     * Removes a key from this configuration and updates {@link #getYamlConfiguration()} instance.
     *
     * @param key The key to remove.
     */
    public void remove(@NotNull String key)
    {
        for (LinkedHashMap<String, Object> element : elements) {
            // Checking if element is not a comment and if contains the key.
            if (!element.containsValue(null) && element.containsKey(key)) {
                element.remove(key);
                break;
            }
        }

        updateContent();
        updateYaml();
    }

    /**
     * Saves a Yaml Configuration as .yml in this config path deleting the one that already exists.
     *
     * @throws IOException If write access was denied for this path.
     */
    public void save() throws IOException
    {
        Files.deleteIfExists(path);
        PathUtils.write(content, path);
    }

    /**
     * The path where this configuration will be saved.
     *
     * @return The path of this configuration.
     */
    public @NotNull Path getPath()
    {
        return path;
    }

    /**
     * The name of this configuration is the file in the path without extension. E.g.: The name of the config
     * "MyServer/plugins/MyPlugin/myCustomConfig.yml" is "myCustomConfig".
     *
     * @return The name of this configuration.
     */
    public @NotNull String getName()
    {
        return name;
    }

    /**
     * The {@link YamlConfiguration} instance of this {@link PluginConfig} with the same keys and values.
     *
     * @return A {@link YamlConfiguration} version of this class.
     * @see ConfigLoader
     */
    public @NotNull YamlConfiguration getYamlConfiguration()
    {
        return yamlConfiguration;
    }

    private void updateYaml()
    {
        yamlConfiguration = YamlConfiguration.loadFromStream(
                new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8)));
    }

    private void updateElements()
    {
        elements = new LinkedList<>();

        StringBuilder sections = new StringBuilder();

        for (String line : lineSeparatorRegex.split(content)) {
            String trimmed = line.trim();

            if (trimmed.startsWith("#")) {
                if (sections.length() > 0) {
                    YamlConfiguration config = YamlConfiguration.loadFromStream(
                            new ByteArrayInputStream(sections.toString().getBytes(StandardCharsets.UTF_8)));

                    for (String key : config.getKeys())
                        add(key, config.get(key));

                    sections = new StringBuilder();
                }

                addComment(trimmed.substring(1));
            } else {
                sections.append(line).append("\n");
            }
        }
    }

    private void updateContent()
    {
        StringBuilder builder = new StringBuilder();
        int i = 1;
        int contentsSize = elements.size();

        for (LinkedHashMap<String, Object> element : elements)
            try {
                if (element.values().iterator().next() == null) {
                    builder.append("#").append(element.keySet().iterator().next()).append(i == contentsSize ? "" : "\n");
                } else {
                    YamlConfiguration yaml = new YamlConfiguration();

                    for (String key : element.keySet())
                        yaml.set(key, element.get(key));

                    builder.append(yaml.saveToString()).append(i == contentsSize ? "" : "\n");
                }
            } finally {
                ++i;
            }

        content = builder.toString();
    }
}
