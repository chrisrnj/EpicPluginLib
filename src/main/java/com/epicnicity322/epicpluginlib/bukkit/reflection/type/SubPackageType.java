package com.epicnicity322.epicpluginlib.bukkit.reflection.type;

public enum SubPackageType
{
    ADVANCEMENT,
    ATTRIBUTE,
    BLOCK,
    BOSS,
    CHUNKIO,
    COMMAND,
    CONVERSATIONS,
    ENCHANTMENTS,
    ENTITY,
    EVENT,
    GENERATOR,
    HELP,
    INVENTORY,
    MAP,
    METADATA,
    PERSISTENCE,
    POTION,
    PROJECTILES,
    SCHEDULER,
    SCOREBOARD,
    TAG,
    UPDATER,
    UTIL;

    private final String name;

    SubPackageType()
    {
        name = PackageType.CRAFTBUKKIT + "." + name().toLowerCase();
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
