package com.epicnicity322.epicpluginapi.reflection;

import com.epicnicity322.epicpluginapi.reflection.type.DataType;
import com.epicnicity322.epicpluginapi.reflection.type.PackageType;
import com.epicnicity322.epicpluginapi.reflection.type.SubPackageType;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class ReflectionUtil
{
    public static String NMS_VERSION = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];

    public static Class<?> getClass(String name, PackageType type) throws Exception
    {
        return Class.forName(type.getName() + "." + name);
    }

    public static Class<?> getClass(String name, SubPackageType type) throws Exception
    {
        return Class.forName(type.getName() + "." + name);
    }

    public static Constructor<?> getConstructor(Class<?> clazz, Class<?>... parameterTypes)
    {
        Class<?>[] p = DataType.convertToPrimitive(parameterTypes);

        for (Constructor<?> c : clazz.getConstructors()) {
            if (DataType.equalsArray(DataType.convertToPrimitive(c.getParameterTypes()), p)) {
                return c;
            }
        }
        return null;
    }

    public static Method getMethod(Class<?> clazz, String name, Class<?>... parameterTypes)
    {
        Class<?>[] p = DataType.convertToPrimitive(parameterTypes);

        for (Method m : clazz.getMethods()) {
            if (m.getName().equals(name) && DataType.equalsArray(DataType.convertToPrimitive(m.getParameterTypes()), p)) {
                return m;
            }
        }

        return null;
    }

    public static Field getField(Class<?> clazz, String name) throws Exception
    {
        Field f = clazz.getField(name);

        f.setAccessible(true);
        return f;
    }

    public static void sendPacket(Player p, Object packet) throws Exception
    {
        Object handle = p.getClass().getMethod("getHandle").invoke(p);
        Object playerConnection = handle.getClass().getField("playerConnection").get(handle);
        playerConnection.getClass().getMethod("sendPacket", getClass("Packet", PackageType.MINECRAFT_SERVER)).invoke(playerConnection, packet);
    }
}
