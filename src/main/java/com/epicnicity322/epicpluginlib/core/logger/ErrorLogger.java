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

package com.epicnicity322.epicpluginlib.core.logger;

import com.epicnicity322.epicpluginlib.core.util.PathUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ErrorLogger
{
    private final static @NotNull DateTimeFormatter fileNameFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH.mm.ss");
    private final static @NotNull DateTimeFormatter logFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss.SSS");
    private final int authorsSize;
    private final @NotNull Path errorFolder;
    private final @NotNull String authors;
    private final @NotNull String pluginName;
    private final @NotNull String pluginVersion;
    private final @Nullable String website;
    private final @Nullable Logger logger;

    public ErrorLogger(@NotNull Path errorFolder, @NotNull String pluginName, @NotNull String pluginVersion,
                       @NotNull Collection<String> authors, @Nullable String website, @Nullable Logger logger)
    {
        if (!Files.isDirectory(errorFolder))
            throw new IllegalArgumentException("errorFolder parameter is not a valid directory.");

        if (authors.isEmpty())
            throw new IllegalArgumentException("Empty collection of authors.");

        this.errorFolder = errorFolder;
        this.authorsSize = authors.size();
        this.authors = authors.toString();
        this.pluginName = pluginName;
        this.pluginVersion = pluginVersion;
        this.website = website;
        this.logger = logger;
    }

    public ErrorLogger(@NotNull Path errorFolder, @NotNull String pluginName, @NotNull String pluginVersion,
                       @NotNull Collection<String> authors, @Nullable String website)
    {
        this(errorFolder, pluginName, pluginVersion, authors, website, null);
    }

    public ErrorLogger(@NotNull Path errorFolder, @NotNull String pluginName, @NotNull String pluginVersion,
                       @NotNull Collection<String> authors)
    {
        this(errorFolder, pluginName, pluginVersion, authors, null, null);
    }

    private static String stackTraceToString(Exception exception)
    {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);

        exception.printStackTrace(pw);
        return sw.toString();
    }

    public void report(@NotNull Exception exception, @NotNull String title)
    {
        try {
            LocalDateTime localDateTime = LocalDateTime.now();
            Path error = PathUtils.getUniquePath(errorFolder.resolve(localDateTime.format(fileNameFormatter) + ".LOG"));

            PathUtils.write("=====================================================================" +
                    "\n>> Please report this file to " + authors +
                    (website == null ? "" : "\n>> " + website) +
                    "\n=====================================================================" +
                    "\n" +
                    "\n - " + localDateTime.format(logFormatter) +
                    "\n - " + pluginName + " v" + pluginVersion +
                    "\n" +
                    "\n" + title +
                    "\n" + stackTraceToString(exception), error);

            if (logger != null)
                logger.log(Level.WARNING, "New log at " + errorFolder.getFileName().toString() + " folder.");
        } catch (Exception e) {
            System.out.println("\nSomething went wrong while reporting an error of \"" + pluginName + "\" plugin.");
            System.out.println("Please contact the developer" + (authorsSize > 1 ? "s" : "") + ": " + authors + "\n");
            System.out.println("Error that was being reported:\n");
            exception.printStackTrace();
            System.out.println("\nError that occurred while reporting:\n");
            e.printStackTrace();
            System.out.println("\nPlease read the messages above these errors and report them.\n");
        }
    }
}
