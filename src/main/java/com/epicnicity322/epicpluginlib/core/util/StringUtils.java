/*
 * EpicPluginLib - Library with basic utilities for bukkit plugins.
 * Copyright (C) 2024  Christiano Rangel
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

public final class StringUtils
{
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
            if (string.charAt(i) == toCount) ++count;

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
        if (value == null) return false;

        int length = value.length();

        if (length == 0) return false;

        int i = 0;

        if (value.charAt(0) == '-') {
            if (length == 1) return false;

            i = 1;
        }

        for (; i < length; ++i) {
            char c = value.charAt(i);

            if (c < '0' || c > '9') return false;
        }

        return true;
    }

    /**
     * Breaks lines automatically of a text to make it fit in an item's lore. This is useful to make so the lore doesn't
     * clip through the edge of the player's screen.
     * <p>
     * If a line's length is greater than the maxCharactersPerLine, the line breaks.
     * <p>
     * Here's an example of how the final lore will look like if the maxCharactersPerLine limit is 35 and maxLines is 5:
     * <blockquote>
     * Lorem ipsum dolor sit amet,<br>
     * consectetur adipiscing elit, sed do<br>
     * eiusmod tempor incididunt ut labore<br>
     * et dolore magna aliqua. Ut enim ad<br>
     * minim veniam, quis nostrud...
     * </blockquote>
     *
     * @param lore                     The text to break lines.
     * @param maxCharactersPerLine     The max characters each line of the text is allowed to have. Usually the lore can have up to 40 characters without clipping through the screen.
     * @param maxLines                 The max lines the text will have, a "..." will be appended to the end if the text has more than this limit. Use -1 for no limit.
     * @param lengthAlreadyInFirstLine If you want to place this text after something already in the lore, specify the length of the line here to format properly.
     * @param lineBreak                What to use at the end of every line.
     * @return The formatted lore text.
     */
    public static @NotNull String breakLore(@NotNull String lore, int maxCharactersPerLine, int maxLines, int lengthAlreadyInFirstLine, @NotNull String lineBreak)
    {
        // Removing double spaces.
        lore = lore.replaceAll("  +", " ");

        String[] words = lore.split(" ");
        if (words.length == 0) return lore;

        StringBuilder builder = new StringBuilder();
        int currentLineLength = lengthAlreadyInFirstLine;
        int lineCount = 1;

        for (String word : words) {
            if (currentLineLength + word.length() > maxCharactersPerLine) {
                if (maxLines != -1 && ++lineCount > maxLines) {
                    // Removing space and putting "..." at the end.
                    switch (builder.charAt(builder.length() - 2)) {
                        case ',':
                        case '.':
                        case '!':
                        case '?':
                        case ':':
                        case ';':
                            break;
                        default:
                            builder.deleteCharAt(builder.length() - 1);
                    }

                    builder.append("...").append(' ');
                    break;
                }

                builder.append(lineBreak);
                currentLineLength = 0;
            }

            currentLineLength += word.length() + 1;
            builder.append(word).append(' ');
        }

        return builder.substring(0, builder.length() - 1);
    }
}
