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

package com.epicnicity322.epicpluginlib.core.util;

import com.epicnicity322.epicpluginlib.core.tools.VersionComparator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.regex.Pattern;

public final class StringUtils
{
    private static final @NotNull Pattern versionSeparatorRegex = Pattern.compile("\\.");

    private StringUtils()
    {
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

    /**
     * Checks if version is greater than greaterVersion.
     *
     * @param version        The version to check if is greater.
     * @param greaterVersion The version to check if version parameter is greater.
     * @return true if version is greater than greaterVersion.
     * @deprecated Use {@link com.epicnicity322.epicpluginlib.core.tools.VersionComparator} instead.
     */
    @Deprecated
    public static boolean isVersionGreater(@NotNull String version, @NotNull String greaterVersion)
    {
        return new VersionComparator(version).compareTo(new VersionComparator(greaterVersion)) > 0;
    }
}
