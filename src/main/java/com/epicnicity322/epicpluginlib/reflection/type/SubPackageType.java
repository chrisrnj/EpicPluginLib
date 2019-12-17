package com.epicnicity322.epicpluginlib.reflection.type;

public enum SubPackageType
{
    BLOCK, CHUNKIO, COMMAND, CONVERSATIONS, ENCHANTMENS, ENTITY, EVENT, GENERATOR, HELP, INVENTORY, MAP, METADATA,
    POTION, PROJECTILES, SCHEDULER, SCOREBOARD, UPDATER, UTIL;

    private String name;

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
