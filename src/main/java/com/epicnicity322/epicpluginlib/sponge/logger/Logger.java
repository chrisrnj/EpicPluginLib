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

package com.epicnicity322.epicpluginlib.sponge.logger;

import com.epicnicity322.epicpluginlib.core.logger.ConsoleLogger;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Pattern;

public class Logger implements ConsoleLogger<Audience>
{
    private static final @NotNull Pattern formatCodes = Pattern.compile("&[a-z0-9]");
    private final @NotNull String prefix;
    private final @NotNull org.apache.logging.log4j.Logger logger;

    /**
     * Creates a logger to log colored messages to console.
     *
     * @param prefix The string that should be in the start of every message.
     * @param logger The logger to use on leveled messages.
     */
    public Logger(@NotNull String prefix, @NotNull org.apache.logging.log4j.Logger logger)
    {
        this.prefix = prefix;
        this.logger = logger;
    }

    @Override
    public @NotNull String getPrefix()
    {
        return prefix;
    }

    @Override
    public void log(@NotNull String message)
    {
        log(message, Level.INFO);
    }

    @Override
    public void log(@NotNull String message, @NotNull Level level)
    {
        message = formatCodes.matcher(message).replaceAll("");

        switch (level) {
            case INFO:
                logger.info(message);
                break;
            case WARN:
                logger.warn(message);
                break;
            case ERROR:
                logger.error(message);
                break;
        }
    }

    @Override
    public void log(@NotNull Audience audience, @NotNull String message)
    {
        audience.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(prefix + message));
    }
}
