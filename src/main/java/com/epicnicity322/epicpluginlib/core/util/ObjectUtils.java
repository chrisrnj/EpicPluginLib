/*
 * EpicPluginLib - Library with basic utilities for bukkit plugins.
 * Copyright (C) 2023  Christiano Rangel
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

package com.epicnicity322.epicpluginlib.core.util;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

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
     * @return false if value is null or isn't numeric.
     */
    public static boolean isNumeric(@Nullable Object value)
    {
        if (value == null) return false;
        if (value instanceof Number) return true;

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

    /**
     * Splits a collection into a {@link HashMap} having the key as the page number, and value as a list that have the
     * size of maxPerPage.
     * <p>
     * If you use an empty collection or set maxPerPage to a value lower than or equal to 0, a map with one page will be
     * returned, but this page will have no entries.
     *
     * @param collection The collection to split.
     * @param maxPerPage The amount you want each page to have.
     * @param <T>        The type of the collection to split.
     * @return The map consisting of the pages.
     */
    public static <T> @NotNull HashMap<Integer, ArrayList<T>> splitIntoPages(@NotNull Collection<T> collection, int maxPerPage)
    {
        if (collection.isEmpty() || maxPerPage <= 0) {
            // Return 1 page with no entries.
            HashMap<Integer, ArrayList<T>> emptyPage = new HashMap<>(1);
            emptyPage.put(1, new ArrayList<>(0));
            return emptyPage;
        }

        // pageAmount must always round up.
        int pageAmount = (int) Math.ceil(collection.size() / (double) maxPerPage);
        HashMap<Integer, ArrayList<T>> pages = new HashMap<>(pageAmount);

        int count = 0;
        int page = 1;
        ArrayList<T> list = new ArrayList<>(maxPerPage);

        for (T t : collection) {
            list.add(t);

            if (++count == maxPerPage) {
                pages.put(page++, list);
                list = new ArrayList<>(maxPerPage);
                count = 0;
            }
        }

        if (!list.isEmpty()) pages.put(page, list);

        return pages;
    }
}
