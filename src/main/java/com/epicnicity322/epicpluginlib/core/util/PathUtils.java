/*
 * EpicPluginLib - Library with basic utilities for bukkit plugins.
 * Copyright (C) 2021  Christiano Rangel
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

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Comparator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class PathUtils
{
    private static final String lineSeparator = ObjectUtils.getOrDefault(System.getProperty("line.separator"), "\n");
    private static final int lineSeparatorLength = lineSeparator.length();

    private PathUtils()
    {
    }

    /**
     * Gets the contents in the file in the path as string.
     *
     * @param destination The file to get the contents.
     * @return The contents of the file or null if file does not exists or is a directory.
     * @throws IOException If read access was denied for this path.
     */
    public static @Nullable String read(@NotNull Path destination) throws IOException
    {
        if (!Files.exists(destination) || Files.isDirectory(destination))
            return null;

        StringBuilder output = new StringBuilder();

        try (Stream<String> lines = Files.lines(destination, StandardCharsets.UTF_8)) {
            lines.forEach(line -> output.append(lineSeparator).append(line));
        }

        // Returning the string without the initial line separator that was added above.
        return output.length() <= lineSeparatorLength ? "" : output.substring(lineSeparatorLength);
    }

    /**
     * Writes bytes into a file. Creates a new file and parent directories if doesn't exit, or append bytes if file
     * already exists.
     *
     * @param data        The bytes you want to write into the path.
     * @param destination The file you want to save the string.
     * @throws IOException              If write access was denied for this path.
     * @throws IllegalArgumentException If destination path points to a directory.
     */
    public static void write(byte[] data, @NotNull Path destination) throws IOException
    {
        if (Files.isDirectory(destination))
            throw new IllegalArgumentException("destination path is a directory.");

        if (Files.notExists(destination)) {
            Path parent = destination.getParent();

            if (Files.notExists(parent))
                Files.createDirectories(parent);

            Files.createFile(destination);
        }

        Files.write(destination, data, StandardOpenOption.APPEND);
    }

    /**
     * Writes String into a file. Creates a new file and parent directories if doesn't exit, or append strings if file
     * already exists.
     *
     * @param data        The string you want to write into the path.
     * @param destination The file you want to save the string.
     * @throws IOException              If write access was denied for this path.
     * @throws IllegalArgumentException If destination path points to a directory.
     */
    public static void write(@NotNull String data, @NotNull Path destination) throws IOException
    {
        write(data.getBytes(StandardCharsets.UTF_8), destination);
    }

    /**
     * Deletes the file in the path. If the path points to a directory, all files inside this directory are deleted.
     *
     * @param path The path to the file or folder to delete.
     * @throws IOException If failed to delete any file in this path.
     */
    public static void deleteAll(@NotNull Path path) throws IOException
    {
        if (Files.notExists(path)) return;

        try (Stream<Path> childStream = Files.walk(path)) {
            for (Path child : childStream.sorted(Comparator.reverseOrder()).collect(Collectors.toList())) {
                Files.delete(child);
            }
        }
    }

    /**
     * Checks if the file in the end of the path already exists. If so, then this will rename the path by adding
     * "(1)" (Or a greater number depending on how many duplicates are in the parent folder) to the end of the file name.
     *
     * @param path The path to the desired file.
     * @return The same path or a renamed one depending if the file in the end already exists.
     */
    public static Path getUniquePath(@NotNull Path path)
    {
        String name = path.getFileName().toString();
        int extensionIndex = name.lastIndexOf('.');
        String extension = extensionIndex == -1 ? "" : name.substring(extensionIndex);
        Path parentPath = path.getParent();

        long count = 2;

        while (Files.exists(path)) {
            name = path.getFileName().toString();

            String nameNoExtension = name.contains(".") ? name.substring(0, name.lastIndexOf('.')).trim() : name.trim();
            String space = "";

            // Checking if a space should be added.
            if (nameNoExtension.contains(" ") && nameNoExtension.charAt(nameNoExtension.lastIndexOf(' ') + 1) != '(')
                space = " ";

            if (isADuplicate(nameNoExtension)) {
                count = Long.parseLong(nameNoExtension.substring(nameNoExtension.lastIndexOf('(') + 1, nameNoExtension.length() - 1));
                path = parentPath.resolve(nameNoExtension.substring(0, nameNoExtension.lastIndexOf('(')) +
                        space + '(' + (count + 1) + ')' + extension);
            } else {
                path = parentPath.resolve(nameNoExtension + " (1)" + extension);
            }

            ++count;
        }

        return path;
    }

    /**
     * Tests if the string has a number in parentheses appended to the end, indicating that it's a duplicate of a file.
     *
     * @param string The string to check if it has a number in parentheses.
     * @return If the string is a duplicate.
     */
    private static boolean isADuplicate(@NotNull String string)
    {
        string = string.trim();
        int length = string.length();

        // The minimum length a duplicate is three, because the minimum they can look like is "(1)".
        if (length < 3) return false;

        if (string.charAt(--length) != ')') return false;

        int openingParenthesisIndex = string.lastIndexOf('(');

        if (openingParenthesisIndex++ == -1) return false;

        for (int i = openingParenthesisIndex; i < length; ++i) {
            char c = string.charAt(i);

            if (c < '0' || c > '9')
                return false;
        }

        return true;
    }
}
