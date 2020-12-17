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

package com.epicnicity322.epicpluginlib.sponge.lang;

import com.epicnicity322.epicpluginlib.core.config.ConfigurationHolder;
import com.epicnicity322.epicpluginlib.core.lang.LanguageHolder;
import com.epicnicity322.yamlhandler.Configuration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.channel.MessageReceiver;
import org.spongepowered.api.text.serializer.TextSerializers;

import java.util.function.Supplier;

public final class MessageSender extends LanguageHolder<Text, MessageReceiver>
{
    private final @NotNull Supplier<String> locale;
    private final @NotNull Configuration defaultLanguage;

    /**
     * Creates an instance of {@link com.epicnicity322.epicpluginlib.bukkit.lang.MessageSender}. Message senders can get and send strings from the plugin's language.
     *
     * @param locale          The locale of the language to get.
     * @param prefix          The prefix to be in the start of every message.
     * @param defaultLanguage The default language to get the keys in case the other language doesn't exist or doesn't
     *                        contain the key.
     */
    public MessageSender(@NotNull Supplier<String> locale, @Nullable Supplier<String> prefix, @NotNull Configuration defaultLanguage)
    {
        this.locale = locale;
        this.defaultLanguage = defaultLanguage;
    }

    @Override
    public void send(@NotNull MessageReceiver receiver, boolean prefix, @NotNull String message)
    {
        if (!message.isEmpty()) {
            receiver.sendMessage(TextSerializers.FORMATTING_CODE.deserialize(
                    (prefix ? get("General.Prefix", "") : "") + message));
        }
    }

    @Override
    public Text getColored(@NotNull String key, @Nullable String def)
    {
        String string = get(key, def);

        if (string == null)
            return null;
        else
            return TextSerializers.FORMATTING_CODE.deserialize(string);
    }

    @Override
    public String get(@NotNull String key, @Nullable String def)
    {
        ConfigurationHolder language = getLanguage(locale.get());

        if (language == null) {
            return defaultLanguage.getString(key).orElse(def);
        } else {
            return language.getConfiguration().getString(key).orElse(def);
        }
    }
}
