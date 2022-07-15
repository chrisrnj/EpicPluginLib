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

package com.epicnicity322.epicpluginlib.core.util;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Comparator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class PathUtils
{
    private static final @NotNull String lineSeparator = ObjectUtils.getOrDefault(System.getProperty("line.separator"), "\n");
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
     * Gets a unique path if the specified path already exists.
     * <p>
     * If the path exists, a suffix of "(1)" is added to the name of the current path. And if that already exists, then
     * the number in parentheses is increased subsequently until a non-existing path
     * ({@link Files#notExists(Path, LinkOption...)}) is found.
     *
     * @param path A unique/non-existing path.
     * @return The path or a path with different name if it already exists.
     * @throws UnsupportedOperationException If the path has no name or no parent.
     */
    public static @NotNull Path getUniquePath(@NotNull Path path)
    {
        if (path.getFileName() == null || path.getParent() == null)
            throw new UnsupportedOperationException("Path \"" + path + "\" has either no name or no parent.");

        Path parentPath = path.getParent();

        // Adding suffix or increasing count if path already exists.
        while (Files.exists(path)) {
            String nameWithExtension = path.getFileName().toString();
            int extensionIndex = nameWithExtension.lastIndexOf('.');
            String name = extensionIndex == -1 ? nameWithExtension : nameWithExtension.substring(0, extensionIndex);
            String extension = extensionIndex == -1 ? "" : nameWithExtension.substring(extensionIndex);
            Long duplicateCount = getDuplicateCount(name);

            // If the path already is a duplicate, then increase number enclosed in parentheses.
            if (duplicateCount != null) {
                path = parentPath.resolve(name.substring(0, name.lastIndexOf('(')) + '(' + (duplicateCount + 1) + ')' + extension);
            } else {
                path = parentPath.resolve(name + " (1)" + extension);
            }
        }

        return path;
    }

    /**
     * Creates a directory in the specified path, if one does not already exist.
     * <p>
     * If a regular file ({@link Files#isRegularFile(Path, LinkOption...)}) exists in the destination of the path, then
     * the folder will be created in a new path with the suffix " (1)". And if that already exists as a regular file,
     * then the number in parentheses is increased subsequently until an existing directory or a non-existing path
     * ({@link Files#notExists(Path, LinkOption...)}) is found.
     * <p>
     * This method always returns an existing directory. Whether it already existed, or it was just created.
     *
     * @param path The path to create a directory/find a directory.
     * @return The directory path. Different from the specified path in case a regular file exists in the destination path.
     * @throws IOException                   If failed to create a directory in the specified path or new path.
     * @throws UnsupportedOperationException If the path has no name or no parent.
     */
    public static @NotNull Path getDirectory(@NotNull Path path) throws IOException
    {
        // Creating directory if path does not exist.
        if (Files.notExists(path)) {
            Files.createDirectories(path);
            return path;
        }

        if (path.getFileName() == null || path.getParent() == null)
            throw new UnsupportedOperationException("Path \"" + path + "\" has either no name or no parent.");

        Path parentPath = path.getParent();

        // Adding suffix or increasing count if path already exists, and it's a regular file rather than a directory.
        while (!Files.isDirectory(path)) {
            String name = path.getFileName().toString();
            Long duplicateCount = getDuplicateCount(name);

            // If the path already is a duplicate, then increase number enclosed in parentheses.
            if (duplicateCount != null) {
                path = parentPath.resolve(name.substring(0, name.lastIndexOf('(')) + '(' + (duplicateCount + 1) + ')');
            } else {
                path = parentPath.resolve(name + " (1)");
            }

            if (Files.notExists(path)) {
                Files.createDirectories(path);
                return path;
            }
        }

        return path;
    }

    /**
     * Gets the number enclosed in parentheses at the end of a file name. If the name has no such parentheses and valid
     * {@link Long} number, then null is returned.
     *
     * @param string The string to get number from.
     * @return The number enclosed in parentheses, or null if not present.
     */
    private static @Nullable Long getDuplicateCount(@NotNull String string)
    {
        string = string.trim();
        int length = string.length();

        // The minimum length a duplicate can have is three, because the minimum they can look like is "(1)".
        if (length < 3) return null;

        if (string.charAt(--length) != ')') return null;

        int openingParenthesisIndex = string.lastIndexOf('(');

        if (openingParenthesisIndex == -1) return null;

        String count = string.substring(++openingParenthesisIndex, length);

        try {
            return Long.parseLong(count);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
