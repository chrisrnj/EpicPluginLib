package com.epicnicity322.epicpluginlib.bukkit.reflection.type;

import com.epicnicity322.epicpluginlib.bukkit.reflection.ReflectionUtil;
import org.bukkit.Bukkit;

public enum PackageType
{
    MINECRAFT_SERVER("net.minecraft.server." + ReflectionUtil.getNmsVersion()),
    CRAFTBUKKIT(Bukkit.getServer().getClass().getPackage().getName());

    private final String name;

    PackageType(String name)
    {
        this.name = name;
    }

    public String getName()
    {
        return name;
    }

    @Override
    public String toString()
    {
        return name;
    }
}
