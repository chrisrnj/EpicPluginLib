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

package com.epicnicity322.epicpluginlib.bukkit.logger;

import com.epicnicity322.epicpluginlib.core.logger.ConsoleLogger;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.regex.Pattern;

public class Logger implements ConsoleLogger<CommandSender>
{
    private static final @NotNull Pattern formatCodes = Pattern.compile("&[a-z\\d]");
    private final @NotNull String prefix;
    private @NotNull java.util.logging.Logger logger;

    /**
     * Creates a logger to log colored messages to console. Leveled messages will be logged using bukkit's default logger.
     *
     * @param prefix The string that should be in the start of every message.
     */
    public Logger(@NotNull String prefix)
    {
        this(prefix, null);
    }

    /**
     * Creates a logger to log colored messages to console.
     *
     * @param prefix The string that should be in the start of every message.
     * @param logger The logger to use on leveled messages, null to use bukkit's default logger.
     */
    public Logger(@NotNull String prefix, @Nullable java.util.logging.Logger logger)
    {
        this.prefix = prefix;

        if (logger == null)
            this.logger = Bukkit.getLogger();
        else
            this.logger = logger;
    }

    @Override
    public @NotNull String getPrefix()
    {
        return prefix;
    }

    public void setLogger(@Nullable java.util.logging.Logger logger)
    {
        if (logger == null)
            this.logger = Bukkit.getLogger();
        else
            this.logger = logger;
    }

    public void log(@NotNull String message)
    {
        log(Bukkit.getConsoleSender(), message);
    }

    public void log(@NotNull String message, @NotNull Level level)
    {
        if (logger == Bukkit.getLogger()) {
            message = prefix + " " + message;
        }

        message = formatCodes.matcher(message).replaceAll("");

        switch (level) {
            case ERROR:
                logger.severe(message);
                break;
            case WARN:
                logger.warning(message);
                break;
            case INFO:
                logger.info(message);
                break;
        }
    }

    public void log(@NotNull CommandSender sender, @NotNull String message)
    {
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + message));
    }
}
