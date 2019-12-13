package com.epicnicity322.epicpluginapi.reflection.type;

import com.epicnicity322.epicpluginapi.reflection.ReflectionUtil;
import org.bukkit.Bukkit;

public enum PackageType
{
    MINECRAFT_SERVER("net.minecraft.server." + ReflectionUtil.NMS_VERSION),
    CRAFTBUKKIT(Bukkit.getServer().getClass().getPackage().getName());

    private String name;

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
