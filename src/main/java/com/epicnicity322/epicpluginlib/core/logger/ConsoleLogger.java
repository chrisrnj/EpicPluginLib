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

package com.epicnicity322.epicpluginlib.core.logger;

import org.jetbrains.annotations.NotNull;

/**
 * A logger to log colored messages or leveled messages to console.
 *
 * @param <R> The receiver that will receive messages logged by the method {@link #log(Object receiver, String message)}.
 */
public interface ConsoleLogger<R>
{
    /**
     * @return The prefix applied to the start of every message.
     */
    @NotNull String getPrefix();

    /**
     * Logs formatted messages with the prefix to console.
     *
     * @param message The message with color codes to send to console.
     */
    void log(@NotNull String message);

    /**
     * Removes color codes the from message and logs to console with a specific {@link Level} and the prefix.
     *
     * @param message The message to log to console.
     * @param level   The level the message should be logged to console.
     */
    void log(@NotNull String message, @NotNull Level level);

    /**
     * Sends formatted messages with the prefix to the message receiver.
     *
     * @param receiver Who the message will be sent.
     * @param message  The message to be sent.
     */
    void log(@NotNull R receiver, @NotNull String message);

    enum Level
    {
        ERROR,
        WARN,
        INFO
    }
}
