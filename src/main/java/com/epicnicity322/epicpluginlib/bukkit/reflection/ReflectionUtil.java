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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public final class ReflectionUtil
{
    private static final String NMS_VERSION = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
    private static Method player_getHandle_method;
    private static Method playerConnection_sendPacket_method;
    private static Field entityPlayer_playerConnection_field;

    static {
        try {
            Class<?> craftPlayer_class = getClass("CraftPlayer", PackageType.CRAFTBUKKIT);
            Class<?> entityPlayer_class = getClass("EntityPlayer", PackageType.MINECRAFT_SERVER);
            Class<?> packet_class = getClass("Packet", PackageType.MINECRAFT_SERVER);
            Class<?> playerConnection_class = getClass("PlayerConnection", PackageType.MINECRAFT_SERVER);

            playerConnection_sendPacket_method = getMethod(playerConnection_class, "sendPacket", packet_class);
            player_getHandle_method = getMethod(craftPlayer_class, "getHandle");
            entityPlayer_playerConnection_field = getField(entityPlayer_class, "playerConnection");

            //I'm pretty sure these classes exist so the exception is ignored.
        } catch (ClassNotFoundException ignored) {
        }
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
    public static void sendPacket(@NotNull Player player, @NotNull Object packet) throws InvocationTargetException,
            IllegalAccessException
    {
        Object entityPlayer = player_getHandle_method.invoke(player);
        Object playerConnection = entityPlayer_playerConnection_field.get(entityPlayer);

        playerConnection_sendPacket_method.invoke(playerConnection, packet);
    }

    /**
     * Gets the version of minecraft server the bukkit server is running.
     *
     * @return The version of NMS the server is running.
     */
    public static @NotNull String getNmsVersion()
    {
        return NMS_VERSION;
    }

    /**
     * Gets a class in net.minecraft.server or org.bukkit.craftbukkit by name.
     *
     * @param name The name of the class to get.
     * @param type The type of package this class is in.
     * @return The class with this name.
     * @throws ClassNotFoundException If a class with this name was not found in this package.
     */
    public static @NotNull Class<?> getClass(@NotNull String name, @NotNull PackageType type) throws
            ClassNotFoundException
    {
        return Class.forName(type.getName() + "." + name);
    }

    /**
     * Gets a class in sub package inside org.bukkit.craftbukkit by name.
     *
     * @param name The name of the class to get.
     * @param type The type of package this class is in.
     * @return The class with this name.
     * @throws ClassNotFoundException If a class with this name was not found in this package.
     */
    public static @NotNull Class<?> getClass(@NotNull String name, @NotNull SubPackageType type) throws
            ClassNotFoundException
    {
        return Class.forName(type.getName() + "." + name);
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
