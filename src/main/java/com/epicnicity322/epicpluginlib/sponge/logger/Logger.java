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

package com.epicnicity322.epicpluginlib.sponge.logger;

import com.epicnicity322.epicpluginlib.core.logger.ConsoleLogger;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.text.channel.MessageReceiver;
import org.spongepowered.api.text.serializer.TextSerializers;

public class Logger implements ConsoleLogger<MessageReceiver>
{
    private final @NotNull String prefix;
    private final @NotNull org.slf4j.Logger logger;

    /**
     * Creates a logger to log colored messages to console.
     *
     * @param prefix The string that should be in the start of every message.
     * @param logger The logger to use on leveled messages.
     */
    public Logger(@NotNull String prefix, @NotNull org.slf4j.Logger logger)
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
        log(Sponge.getServer().getConsole(), message);
    }

    @Override
    public void log(@NotNull String message, @NotNull Level level)
    {
        message = TextSerializers.FORMATTING_CODE.stripCodes(message);

        switch (level) {
            case ERROR:
                logger.error(message);
                break;
            case WARN:
                logger.warn(message);
                break;
            case INFO:
                logger.info(message);
                break;
        }
    }

    @Override
    public void log(@NotNull MessageReceiver receiver, @NotNull String message)
    {
        receiver.sendMessage(TextSerializers.FORMATTING_CODE.deserialize(prefix + message));
    }
}
