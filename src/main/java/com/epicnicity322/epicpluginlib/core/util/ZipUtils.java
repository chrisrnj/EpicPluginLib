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

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.*;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A class made to easily extract or create zip files. Zip passwords are not supported.
 */
public final class ZipUtils
{
    private ZipUtils()
    {
    }

    /**
     * Creates a zip containing all files in the specified path, either folders or files. Symbolic links are ignored.
     *
     * @param toZip       The path of the files or folder to zip.
     * @param destination The destination of where the zip will be.
     * @return The zip path or an unique path of {@link PathUtils#getUniquePath(Path)} if the destination file already existed.
     * @throws IllegalArgumentException If {@param toZip} does not exist.
     * @throws IOException              If the zip file could not be created.
     */
    public static @NotNull Path zipFiles(@NotNull Path toZip, @NotNull Path destination) throws IOException
    {
        if (Files.notExists(toZip)) throw new IllegalArgumentException("File to zip does not exist.");

        destination = PathUtils.getUniquePath(destination);

        URI zipUri = URI.create("jar:" + destination.toUri());

        try (Stream<Path> pathTree = Files.walk(toZip);
             FileSystem zip = FileSystems.newFileSystem(zipUri, Collections.singletonMap("create", "true"))) {
            for (Path path : pathTree.collect(Collectors.toList())) {
                Path pathInZip = zip.getPath(toZip.relativize(path).toString());

                if (Files.isDirectory(path)) {
                    Files.createDirectories(pathInZip);
                } else {
                    Files.copy(path, pathInZip, StandardCopyOption.REPLACE_EXISTING);
                }
            }
        }

        return destination;
    }

    /**
     * Extracts the zip files to the destination folder. If the destination folder does not exist, one is automatically
     * created.
     *
     * @param toExtract   The path of the zip to extract.
     * @param destination The destination folder where the contents of the zip will be.
     * @throws IllegalArgumentException If {@param toExtract} is not a file.
     * @throws IllegalArgumentException If {@param destination} path already exists and is not a folder.
     * @throws IOException              If the zip file could not be extracted.
     * @throws IOException              If the zip file had a zip slip vulnerability.
     */
    public static void extractZip(@NotNull Path toExtract, @NotNull Path destination) throws IOException
    {
        if (Files.notExists(destination)) {
            Files.createDirectories(destination);
        } else if (!Files.isDirectory(destination)) {
            throw new IllegalArgumentException("Extract destination path is not a folder.");
        }

        if (Files.notExists(toExtract) || Files.isDirectory(toExtract)) {
            throw new IllegalArgumentException("Zip to be extracted does not exist or is a directory.");
        }

        URI zipUri = URI.create("jar:" + toExtract.toUri());

        try (FileSystem zip = FileSystems.newFileSystem(zipUri, Collections.emptyMap());
             Stream<Path> zipFiles = Files.walk(zip.getRootDirectories().iterator().next())) {
            // Collecting so IOException can be thrown.
            List<Path> zipFilesList = zipFiles.collect(Collectors.toList());

            // Removing '/' root folder.
            zipFilesList.remove(0);

            for (Path zipFile : zipFilesList) {
                // The destination will always start with a / because it's in a zip.
                Path fileDest = destination.resolve(zipFile.toString().substring(1).replace(zip.getSeparator(), File.separator)).normalize();

                // Checking if the path to copy to really is inside the destination folder, to avoid a zip slip.
                if (!fileDest.startsWith(destination))
                    throw new IOException("Bad zip entry: " + zipFile.toString().substring(1));

                Files.copy(zipFile, fileDest, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.COPY_ATTRIBUTES);
            }
        }
    }
}
