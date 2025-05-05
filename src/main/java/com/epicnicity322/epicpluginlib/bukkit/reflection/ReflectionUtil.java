/*
 * EpicPluginLib - Library with basic utilities for bukkit plugins.
 * Copyright (C) 2025  Christiano Rangel
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
import com.epicnicity322.epicpluginlib.core.EpicPluginLib;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.*;
import java.util.Objects;

public final class ReflectionUtil
{
    private static final Method method_CraftPlayer_getHandle;
    private static final Method method_PlayerConnection_sendPacket;
    private static final Field field_EntityPlayer_playerConnection;

    static {
        // Finding equivalents of PlayerConnection#sendPacket on the current version.
        Method method_CraftPlayer_getHandle1 = null;
        Method method_PlayerConnection_sendPacket1 = null;
        Field field_EntityPlayer_playerConnection1 = null;
        try {
            Class<?> class_CraftPlayer = Objects.requireNonNull(getClass("CraftPlayer", SubPackageType.ENTITY));

            Class<?> class_EntityPlayer = getClass("EntityPlayer", PackageType.MINECRAFT_SERVER);
            if (class_EntityPlayer == null)
                class_EntityPlayer = Class.forName("net.minecraft.server.level.EntityPlayer");

            Class<?> class_Packet = getClass("Packet", PackageType.MINECRAFT_SERVER);
            if (class_Packet == null) class_Packet = Class.forName("net.minecraft.network.protocol.Packet");

            Class<?> class_PlayerConnection = getClass("PlayerConnection", PackageType.MINECRAFT_SERVER);
            if (class_PlayerConnection == null)
                class_PlayerConnection = Class.forName("net.minecraft.server.network.PlayerConnection");

            method_CraftPlayer_getHandle1 = Objects.requireNonNull(getMethod(class_CraftPlayer, "getHandle"));
            field_EntityPlayer_playerConnection1 = Objects.requireNonNull(findFieldByType(class_EntityPlayer, class_PlayerConnection));

            // Finding send packet method.
            for (Method m : class_PlayerConnection.getMethods()) {
                if (m.getReturnType() == Void.TYPE && DataType.equalsArray(m.getParameterTypes(), new Class[]{class_Packet})) {
                    method_PlayerConnection_sendPacket1 = m;
                    break;
                }
            }
        } catch (Exception e) {
            new Exception("Could not find equivalent of PlayerConnection#sendPacket. Does the server have NMS?", e).printStackTrace();
        }
        method_CraftPlayer_getHandle = method_CraftPlayer_getHandle1;
        method_PlayerConnection_sendPacket = method_PlayerConnection_sendPacket1;
        field_EntityPlayer_playerConnection = field_EntityPlayer_playerConnection1;
    }

    private ReflectionUtil()
    {
    }

    /**
     * Sends a net.minecraft.server packet to a player. In order to be compatible with all versions, reflection is used
     * to find the equivalent of PlayerConnection#sendPacket method in the current version.
     *
     * @param player The player to send the packet.
     * @param packet The packet to send to the player.
     * @throws UnsupportedOperationException If PlayerConnection#sendPacket could not be found in the running server.
     * @throws RuntimeException              If there was an issue invoking the methods or getting the playerConnection field.
     */
    public static void sendPacket(@NotNull Player player, @NotNull Object packet)
    {
        if (method_CraftPlayer_getHandle == null) {
            throw new UnsupportedOperationException("Packet could not be sent because the equivalent of PlayerConnection#sendPacket could not be found.");
        }

        try {
            Object entityPlayer = method_CraftPlayer_getHandle.invoke(player);
            Object playerConnection = field_EntityPlayer_playerConnection.get(entityPlayer);

            method_PlayerConnection_sendPacket.invoke(playerConnection, packet);
        } catch (InvocationTargetException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Gets the version suffix of the Minecraft Server package.
     * <p>
     * The suffix might be blank depending on the server version the lib is running. For example: in versions newer or
     * equal to 1.17 there is no suffix, so this is subject to change in any version. Use {@link EpicPluginLib.Platform#getVersion()}
     * if you're looking for the running server version.
     *
     * @return The suffix of net.minecraft.server package, blank if there isn't one.
     */
    public static @NotNull String getNmsVersion()
    {
        return NMSVersion.NMS_VERSION;
    }

    /**
     * Gets the version suffix of the CraftBukkit package.
     * <p>
     * The suffix might be blank depending on the version of CraftBukkit the lib is running. For example: in versions
     * older than 1.4.5 there was no suffix, so this is subject to change in any version. Use {@link EpicPluginLib.Platform#getVersion()}
     * if you're looking for the running server version.
     *
     * @return The suffix of org.bukkit.craftbukkit package, blank if there isn't one.
     */
    public static @NotNull String getCraftBukkitVersion()
    {
        return NMSVersion.CRAFTBUKKIT_VERSION;
    }

    /**
     * Gets the class with the specified name or null if this class was not found.
     *
     * @param name The name of the class.
     * @return The class or null if not found.
     */
    public static @Nullable Class<?> getClass(@NotNull String name)
    {
        return NMSVersion.getClass(name);
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

        for (Constructor<?> c : clazz.getDeclaredConstructors())
            if (DataType.equalsArray(DataType.convertToPrimitive(c.getParameterTypes()), p)) {
                trySetAccessible(c);
                return c;
            }

        return null;
    }

    /**
     * Gets a method of a class, regardless of its access modifier and class variable. If a method with the same name
     * and parameters is found, it is automatically set to accessible.
     * <p>
     * Primitive and reference parameter types are considered the same when looking for the method.
     *
     * @param clazz          The class to look for the method.
     * @param name           The name of the method.
     * @param parameterTypes The parameter types of the method.
     * @return A method with the specified parameters and name, if found.
     */
    public static @Nullable Method getMethod(@NotNull Class<?> clazz, @NotNull String name, @NotNull Class<?>... parameterTypes)
    {
        Class<?>[] p = DataType.convertToPrimitive(parameterTypes);

        for (Method m : clazz.getDeclaredMethods())
            if (m.getName().equals(name) && DataType.equalsArray(DataType.convertToPrimitive(m.getParameterTypes()), p)) {
                trySetAccessible(m);
                return m;
            }

        return null;
    }

    /**
     * Finds the first {@link Method} of a class that returns the specified {@link Class} type, this is for both static
     * and non-static methods.
     *
     * @param clazz      The class to look for the method.
     * @param methodType The class type the method returns.
     * @return A method returning the specified type, if found.
     * @see #findMethodByType(Class, Class, boolean)
     */
    public static @Nullable Method findMethodByType(@NotNull Class<?> clazz, @NotNull Class<?> methodType)
    {
        for (Method method : clazz.getDeclaredMethods())
            if (method.getReturnType() == methodType) {
                trySetAccessible(method);
                return method;
            }

        return null;
    }

    /**
     * Finds the first {@link Method} of a class that returns the specified {@link Class} type.
     *
     * @param clazz      The class to look for the method.
     * @param methodType The class type the method returns.
     * @param isStatic   true to look only for static methods, false otherwise.
     * @return A method returning the specified type, if found.
     */
    public static @Nullable Method findMethodByType(@NotNull Class<?> clazz, @NotNull Class<?> methodType, boolean isStatic)
    {
        for (Method method : clazz.getDeclaredMethods()) {
            if (method.getReturnType() != methodType) continue;
            if (isStatic == Modifier.isStatic(method.getModifiers())) {
                trySetAccessible(method);
                return method;
            }
        }

        return null;
    }

    /**
     * Finds the first {@link Method} of a class that has the matching parameter types, this is for both static and
     * non-static methods.
     *
     * @param clazz          The class to look for the method.
     * @param parameterTypes The parameter types of the method.
     * @return A method with the specified parameters.
     * @see #findMethodByParameterTypes(Class, boolean, Class[])
     */
    public static @Nullable Method findMethodByParameterTypes(@NotNull Class<?> clazz, @Nullable Class<?>... parameterTypes)
    {
        Class<?>[] p = DataType.convertToPrimitive(parameterTypes);

        for (Method method : clazz.getDeclaredMethods()) {
            if (DataType.equalsArray(DataType.convertToPrimitive(method.getParameterTypes()), p)) {
                trySetAccessible(method);
                return method;
            }
        }

        return null;
    }

    /**
     * Finds the first {@link Method} of a class that has the matching parameter types.
     *
     * @param clazz          The class to look for the method.
     * @param isStatic       true to look only for static methods, false otherwise.
     * @param parameterTypes The parameter types of the method.
     * @return A method with the specified parameters.
     */
    public static @Nullable Method findMethodByParameterTypes(@NotNull Class<?> clazz, boolean isStatic, @Nullable Class<?>... parameterTypes)
    {
        Class<?>[] p = DataType.convertToPrimitive(parameterTypes);

        for (Method method : clazz.getDeclaredMethods()) {
            if (!DataType.equalsArray(DataType.convertToPrimitive(method.getParameterTypes()), p)) continue;
            if (isStatic == Modifier.isStatic(method.getModifiers())) {
                trySetAccessible(method);
                return method;
            }
        }

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
        for (Field field : clazz.getDeclaredFields())
            if (field.getName().equals(name)) {
                trySetAccessible(field);
                return field;
            }

        return null;
    }

    /**
     * Finds the first {@link Field} of a class that has the specified {@link Class} type, this is for both static and
     * non-static fields.
     *
     * @param clazz     The class to look for the field.
     * @param fieldType The class type the field is.
     * @return A field with the specified type, if found.
     * @see #findFieldByType(Class, Class, boolean)
     */
    public static @Nullable Field findFieldByType(@NotNull Class<?> clazz, @NotNull Class<?> fieldType)
    {
        for (Field field : clazz.getDeclaredFields())
            if (field.getType() == fieldType) {
                trySetAccessible(field);
                return field;
            }

        return null;
    }

    /**
     * Finds the first {@link Field} of a class that has the specified {@link Class} type.
     *
     * @param clazz     The class to look for the field.
     * @param fieldType The class type the field is.
     * @param isStatic  true to look only for static fields, false otherwise.
     * @return A field with the specified type, if found.
     */
    public static @Nullable Field findFieldByType(@NotNull Class<?> clazz, @NotNull Class<?> fieldType, boolean isStatic)
    {
        for (Field field : clazz.getDeclaredFields()) {
            if (field.getType() != fieldType) continue;
            if (isStatic == Modifier.isStatic(field.getModifiers())) {
                trySetAccessible(field);
                return field;
            }
        }

        return null;
    }

    private static void trySetAccessible(@NotNull AccessibleObject object)
    {
        try {
            object.setAccessible(true);
        } catch (Exception ignored) {
            // Alternative of AccessibleObject#trySetAccessible for Java 8
        }
    }
}
