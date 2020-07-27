/*
 * Copyright (c) 2020 Christiano Rangel
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
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
