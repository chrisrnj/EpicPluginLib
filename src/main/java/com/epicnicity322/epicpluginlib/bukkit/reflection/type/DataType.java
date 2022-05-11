/*
 * EpicPluginLib - Library with basic utilities for bukkit plugins.
 * Copyright (C) 2022  Christiano Rangel
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

package com.epicnicity322.epicpluginlib.bukkit.reflection.type;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;

public enum DataType
{
    BYTE(byte.class, Byte.class),
    SHORT(short.class, Short.class),
    INTEGER(int.class, Integer.class),
    LONG(long.class, Long.class),
    CHARACTER(char.class, Character.class),
    FLOAT(float.class, Float.class),
    DOUBLE(double.class, Double.class),
    BOOLEAN(boolean.class, Boolean.class);

    private static final @NotNull HashMap<Class<?>, DataType> CLASS_MAP = new HashMap<>();

    static {
        for (DataType t : values()) {
            CLASS_MAP.put(t.primitive, t);
            CLASS_MAP.put(t.reference, t);
        }
    }

    private final @NotNull Class<?> primitive;
    private final @NotNull Class<?> reference;

    DataType(@NotNull Class<?> primitive, @NotNull Class<?> reference)
    {
        this.primitive = primitive;
        this.reference = reference;
    }

    /**
     * Gets a {@link DataType} based on reference or primitive stated in c parameter.
     *
     * @param c The reference or primitive.
     * @return The {@link DataType} or null if c parameter isn't a reference or primitive class.
     */
    public static @Nullable DataType fromClass(@NotNull Class<?> c)
    {
        return CLASS_MAP.get(c);
    }

    /**
     * Converts the reference object to primitive class.
     *
     * @param c The object to convert to primitive.
     * @return The primitive of the reference in the parameter c or the class you put if it isn't a primitive.
     */
    public static @NotNull Class<?> getPrimitive(@NotNull Class<?> c)
    {
        DataType t = fromClass(c);

        return t == null ? c : t.getPrimitive();
    }

    /**
     * Converts the primitive class to reference object.
     *
     * @param c The class to convert to reference.
     * @return The reference of the primitive in the parameter c or the class you put if it isn't a primitive.
     */
    public static @NotNull Class<?> getReference(@NotNull Class<?> c)
    {
        DataType t = fromClass(c);

        return t == null ? c : t.getReference();
    }

    /**
     * Converts references to primitive.
     *
     * @param classes The references to convert.
     * @return An array with the primitives in the same order as the classes parameter array or an empty array if
     * classes parameter is null.
     */
    public static @NotNull Class<?>[] convertToPrimitive(Class<?>... classes)
    {
        int length = classes == null ? 0 : classes.length;
        Class<?>[] types = new Class<?>[length];

        for (int i = 0; i < length; ++i)
            types[i] = getPrimitive(classes[i]);

        return types;
    }

    /**
     * Converts references to primitive.
     *
     * @param objects The references to convert.
     * @return An array with the primitives in the same order as the objects parameter array or an empty array if
     * objects parameter is null.
     */
    public static @NotNull Class<?>[] convertToPrimitive(Object... objects)
    {
        int length = objects == null ? 0 : objects.length;
        Class<?>[] types = new Class<?>[length];

        for (int i = 0; i < length; ++i)
            types[i] = getPrimitive(objects[i].getClass());

        return types;
    }

    /**
     * Checks if two {@link Class} arrays have the same classes or similar classes.
     *
     * @param a1 The first array.
     * @param a2 The second array.
     * @return If the arrays are the same or have similar classes.
     */
    public static boolean equalsArray(@Nullable Class<?>[] a1, @Nullable Class<?>[] a2)
    {
        if (a1 == a2) return true;
        if (a1 == null || a2 == null || a1.length != a2.length) return false;

        for (int i = 0; i < a1.length; ++i) {
            Class<?> c1 = a1[i];
            Class<?> c2 = a2[i];

            if (c1 != c2 && (c1 == null || c2 == null || !c1.isAssignableFrom(c2))) return false;
        }

        return true;
    }

    /**
     * Gets the primitive class of this {@link DataType}
     *
     * @return A primitive class.
     */
    public @NotNull Class<?> getPrimitive()
    {
        return primitive;
    }

    /**
     * Gets the reference class of this {@link DataType}
     *
     * @return A reference class.
     */
    public @NotNull Class<?> getReference()
    {
        return reference;
    }
}
