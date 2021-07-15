/*
 * EpicPluginLib - Library with basic utilities for bukkit plugins.
 * Copyright (C) 2021  Christiano Rangel
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.epicnicity322.epicpluginlib.bukkit.reflection;

import com.epicnicity322.epicpluginlib.bukkit.reflection.type.DataType;
import com.epicnicity322.epicpluginlib.bukkit.reflection.type.PackageType;
import com.epicnicity322.epicpluginlib.bukkit.reflection.type.SubPackageType;
import org.apache.commons.lang.ArrayUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public final class ReflectionUtil
{
    private static final String NMS_VERSION;
    private static final Method player_getHandle_method;
    private static final Method playerConnection_sendPacket_method;
    private static final Field entityPlayer_playerConnection_field;

    static {
        //Checking if version contains NMS_VERSION suffix.
        if (getClass("org.bukkit.craftbukkit.CraftServer") == null) {
            NMS_VERSION = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
        } else {
            NMS_VERSION = "";
        }

        Class<?> craftPlayer_class = getClass("CraftPlayer", SubPackageType.ENTITY);
        Class<?> entityPlayer_class = getClass("EntityPlayer", PackageType.MINECRAFT_SERVER);
        Class<?> packet_class = getClass("Packet", PackageType.MINECRAFT_SERVER);
        Class<?> playerConnection_class = getClass("PlayerConnection", PackageType.MINECRAFT_SERVER);

        playerConnection_sendPacket_method = getMethod(playerConnection_class, "sendPacket", packet_class);
        player_getHandle_method = getMethod(craftPlayer_class, "getHandle");
        entityPlayer_playerConnection_field = getField(entityPlayer_class, "playerConnection");
    }

    private ReflectionUtil()
    {
    }

    /**
     * Sends a net.minecraft.server packet to a player.
     *
     * @param player The player to send the packet.
     * @param packet The packet to send to the player.
     */
    public static void sendPacket(@NotNull Player player, @NotNull Object packet)
    {
        try {
            Object entityPlayer = player_getHandle_method.invoke(player);
            Object playerConnection = entityPlayer_playerConnection_field.get(entityPlayer);

            playerConnection_sendPacket_method.invoke(playerConnection, packet);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Gets the version of minecraft server the bukkit server is running. Empty if this version of bukkit does not have
     * the NMS version suffix.
     *
     * @return The version of NMS the server is running.
     */
    public static @NotNull String getNmsVersion()
    {
        return NMS_VERSION;
    }

    /**
     * Gets the class with the specified name or null if this class was not found.
     *
     * @param name The name of the class.
     * @return The class or null if not found.
     */
    public static @Nullable Class<?> getClass(@NotNull String name)
    {
        try {
            return Class.forName(name);
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

    /**
     * Gets a class in net.minecraft.server or org.bukkit.craftbukkit by name.
     *
     * @param name The name of the class to get.
     * @param type The type of package this class is in.
     * @return The class with this name.
     */
    public static @Nullable Class<?> getClass(@NotNull String name, @NotNull PackageType type)
    {
        return getClass(type.getName() + "." + name);
    }

    /**
     * Gets a class in sub package inside org.bukkit.craftbukkit by name.
     *
     * @param name The name of the class to get.
     * @param type The type of package this class is in.
     * @return The class with this name.
     */
    public static @Nullable Class<?> getClass(@NotNull String name, @NotNull SubPackageType type)
    {
        return getClass(type.getName() + "." + name);
    }

    /**
     * Gets a constructor declared or not of a class without differentiating whether its parameters are references or
     * primitives.
     *
     * @param clazz          The class to search for constructors.
     * @param parameterTypes The parameters of this constructor.
     * @return A constructor matching these parameters or null if not found.
     */
    public static @Nullable Constructor<?> getConstructor(@NotNull Class<?> clazz, @NotNull Class<?>... parameterTypes)
    {
        Class<?>[] p = DataType.convertToPrimitive(parameterTypes);

        for (Constructor<?> c : (Constructor<?>[]) ArrayUtils.addAll(clazz.getConstructors(),
                clazz.getDeclaredConstructors()))
            if (DataType.equalsArray(DataType.convertToPrimitive(c.getParameterTypes()), p))
                return c;

        return null;
    }

    /**
     * Gets a method declared or not of a class without differentiating whether its parameters are references or
     * primitives.
     *
     * @param clazz          The class where the methods are.
     * @param name           The name of the method.
     * @param parameterTypes The classes of parameters of this method.
     * @return The method matching these parameters and this name or null if not found.
     */
    public static @Nullable Method getMethod(@NotNull Class<?> clazz, @NotNull String name,
                                             @NotNull Class<?>... parameterTypes)
    {
        Class<?>[] p = DataType.convertToPrimitive(parameterTypes);

        for (Method m : (Method[]) ArrayUtils.addAll(clazz.getMethods(), clazz.getDeclaredMethods()))
            if (m.getName().equals(name) && DataType.equalsArray(DataType.convertToPrimitive(m.getParameterTypes()), p))
                return m;

        return null;
    }

    /**
     * Gets a field declared or not and sets it accessible.
     *
     * @param clazz The class where the field is.
     * @param name  The name of the field.
     * @return The field with this name or null if not found.
     */
    public static @Nullable Field getField(@NotNull Class<?> clazz, @NotNull String name)
    {
        for (Field field : (Field[]) ArrayUtils.addAll(clazz.getFields(), clazz.getDeclaredFields()))
            if (field.getName().equals(name)) {
                field.setAccessible(true);

                return field;
            }

        return null;
    }
}
