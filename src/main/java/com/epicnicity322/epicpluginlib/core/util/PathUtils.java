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

        // Returning the string without the initial line separator.
        return output.length() <= lineSeparatorLength ? "" : output.substring(lineSeparatorLength);
    }

    /**
     * Writes bytes into a file. Creates a new file and parent directories if doesn't exit, or append bytes if file
     * already exists.
     *
     * @param data        The bytes you want to write into the path.
     * @param destination The file you want to save the string.
     * @throws IOException              If write access was denied for this path.
     * @throws NullPointerException     If any parameter is null.
     * @throws IllegalArgumentException If destination path points to a directory.
     */
    public static void write(byte[] data, @NotNull Path destination) throws IOException
    {
        if (Files.isDirectory(destination))
            throw new IllegalArgumentException("destination path is a directory.");

        if (!Files.exists(destination)) {
            Path parent = destination.getParent();

            if (!Files.exists(parent))
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
     * @throws NullPointerException     If any parameter is null.
     * @throws IllegalArgumentException If destination path points to a directory.
     */
    public static void write(@NotNull String data, @NotNull Path destination) throws IOException
    {
        write(data.getBytes(StandardCharsets.UTF_8), destination);
    }

    /**
     * Checks if the file in the end of the path already exists. If so, then this will rename the path by adding
     * "(1)" (Or a greater number depending on how many duplicates are in the parent folder) to the end of the file name.
     *
     * @param path The path to the desired file.
     * @return The same path or a renamed one depending if the file in the end already exists.
     * @throws NullPointerException If path is null
     */
    public static Path getUniquePath(@NotNull Path path)
    {
        String fileName = path.getFileName().toString();
        String extension = fileName.substring(fileName.lastIndexOf("."));
        Path parentPath = path.getParent();

        long l = 2;

        while (Files.exists(path)) {
            fileName = path.getFileName().toString();

            String fileNameOnly = fileName.contains(".") ? fileName.substring(0, fileName.lastIndexOf(".")).trim() :
                    fileName.trim();
            String parenthesisPrefix = "";

            // If a duplicate does not have a space before the parenthesis, then this space will be added by this.
            if (fileNameOnly.contains(" ") && fileNameOnly.charAt(fileNameOnly.lastIndexOf(" ") + 1) != '(')
                parenthesisPrefix = " ";

            if (isADuplicate(fileNameOnly)) {
                l = Long.parseLong(fileNameOnly.substring(fileNameOnly.lastIndexOf("(") + 1).replace(")",
                        ""));
                path = parentPath.resolve(fileNameOnly.substring(0, fileNameOnly.lastIndexOf("(")) +
                        parenthesisPrefix + "(" + (l + 1) + ")" + extension);
            } else {
                path = parentPath.resolve(fileNameOnly + " (1)" + extension);
            }

            ++l;
        }

        return path;
    }

    /**
     * This will check if the file name has a duplicate suffix.
     *
     * @param fileName The name of the desired file.
     * @return true if the string has a duplicate suffix.
     */
    private static boolean isADuplicate(@NotNull String fileName)
    {
        fileName = fileName.trim();

        if (fileName.endsWith(")")) {
            //The minimum length that a duplicated file name can have is 3, because that name could only look like something like "(2)".
            if (fileName.length() > 2) {
                //If the char before parenthesis is an integer.
                return StringUtils.isNumeric(Character.toString(fileName.charAt(fileName.length() - 2)));
            }
        }

        return false;
    }
}
