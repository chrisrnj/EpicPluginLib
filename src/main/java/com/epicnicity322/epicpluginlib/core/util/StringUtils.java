package com.epicnicity322.epicpluginlib.core.util;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.regex.Pattern;

public final class StringUtils {
    private StringUtils() {
    }

    /**
     * Returns the default value in case the string is null or is empty.
     *
     * @param value        The string to get.
     * @param defaultValue The string to be returned if the value is null or empty.
     * @return The string or the default string. Null only if defaultValue is null and value didn't pass the test.
     */
    public static @Nullable String getOrDefault(@Nullable String value, @Nullable String defaultValue)
    {
        return value == null || value.isEmpty() ? defaultValue : value;
    }

    /**
     * Counts the amount of a char in a string.
     *
     * @param string  The string to count how many chars.
     * @param toCount The char you want to count in the string.
     * @return The amount of this char in the string.
     */
    public static int count(String string, char toCount)
    {
        int count = 0, length = string.length();

        for (int i = 0; i < length; ++i)
            if (string.charAt(i) == toCount)
                ++count;

        return count;
    }

    /**
     * Checks if a string is a number. This will return true doesn't matter the number length so you might not want to
     * use this if you want to fail when number is greater than {@link Integer#MAX_VALUE} or lower than
     * {@link Integer#MIN_VALUE}. This was not written by me, you can find the original code made by Jonas Klemming on
     * Stack Overflow.
     *
     * @param value The string you want to check if is a number.
     * @return true if the string is a number.
     * @see <a href="https://stackoverflow.com/a/237204">StackOverFlow original answer</a>
     */
    public static boolean isNumeric(@Nullable String value)
    {
        if (value == null)
            return false;

        int length = value.length();

        if (length == 0)
            return false;

        int i = 0;

        if (value.charAt(0) == '-') {
            if (length == 1)
                return false;

            i = 1;
        }

        for (; i < length; ++i) {
            char c = value.charAt(i);

            if (c < '0' || c > '9')
                return false;
        }

        return true;
    }

    private static final @NotNull Pattern versionSeparatorRegex = Pattern.compile("\\.");

    /**
     * Checks if version is greater than greaterVersion.
     *
     * @param version        The version to check if is greater.
     * @param greaterVersion The version to check if version parameter is greater.
     * @return true if version is greater than greaterVersion.
     */
    public static boolean isVersionGreater(@NotNull String version, @NotNull String greaterVersion) {
        String[] versionNodes = versionSeparatorRegex.split(version);
        String[] greaterNodes = versionSeparatorRegex.split(greaterVersion);

        int length = Math.max(versionNodes.length, greaterNodes.length);

        for (int i = 0; i < length; ++i) {
            int versionNode = i < versionNodes.length ? Integer.parseInt(versionNodes[i]) : 0;
            int greaterNode = i < greaterNodes.length ? Integer.parseInt(greaterNodes[i]) : 0;

            if (versionNode < greaterNode)
                return false;
            if (versionNode > greaterNode)
                return true;
        }

        return false;
    }
}
