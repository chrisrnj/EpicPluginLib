package com.epicnicity322.epicpluginlib.config.type;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashSet;

public class ConfigType implements Type
{
    private String name;
    private String defaults;
    private Path parent;
    private HashSet<String> acceptableVersions;

    public ConfigType(String name, String defaults, Path parent, String... acceptableVersions)
    {
        this.name = name;
        this.defaults = defaults;
        this.parent = parent;
        this.acceptableVersions = new HashSet<>(Arrays.asList(acceptableVersions));
    }

    public String getName()
    {
        return name;
    }

    public String getDefaults()
    {
        return defaults;
    }

    public Path getFolder()
    {
        return parent;
    }

    public HashSet<String> getAcceptableVersions()
    {
        return acceptableVersions;
    }
}
