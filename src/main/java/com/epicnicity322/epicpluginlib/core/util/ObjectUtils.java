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
