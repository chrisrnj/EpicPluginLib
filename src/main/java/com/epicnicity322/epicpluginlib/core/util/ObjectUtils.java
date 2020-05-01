package com.epicnicity322.epicpluginlib.core.util;

import org.jetbrains.annotations.Nullable;

public final class ObjectUtils
{
    private ObjectUtils()
    {
    }

    /**
     * Returns the default value in case the value is null.
     *
     * @param value        The value that can be null.
     * @param defaultValue The default value to be returned if value parameter is null.
     * @return The value or the default value. Null if both parameters are null.
     */
    public static <T> T getOrDefault(@Nullable T value, @Nullable T defaultValue)
    {
        return value == null ? defaultValue : value;
    }

    /**
     * Converts object to string and checks if it's a number. This doesn't check for number length so this will return
     * true even if the number is greater than {@link Long#MAX_VALUE} and lower than {@link Long#MIN_VALUE}.
     *
     * @param value The object to check if is numeric.
     * @return false if value is null or isn't  numeric.
     */
    public static boolean isNumeric(@Nullable Object value)
    {
        if (value == null)
            return false;

        String string = value.toString();

        int length = string.length();

        if (length == 0)
            return false;

        int i = 0;

        if (string.charAt(0) == '-') {
            if (length == 1)
                return false;

            i = 1;
        }

        for (; i < length; ++i) {
            char c = string.charAt(i);

            if (c < '0' || c > '9')
                return false;
        }

        return true;
    }
}
