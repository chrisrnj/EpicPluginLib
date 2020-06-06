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

import com.epicnicity322.epicpluginlib.core.config.PluginConfig;
import com.epicnicity322.epicpluginlib.core.lang.LanguageHolder;
import com.epicnicity322.epicpluginlib.core.util.ObjectUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.channel.MessageReceiver;
import org.spongepowered.api.text.serializer.TextSerializers;

import java.util.Optional;

public final class MessageSender extends LanguageHolder<Text, MessageReceiver>
{
    private final @NotNull PluginConfig mainConfig;
    private final @NotNull PluginConfig defaultLanguage;

    /**
     * Creates an instance of {@link MessageSender}. Message senders can get and send strings from the plugin's language.
     *
     * @param mainConfig      Your main configuration containing the key "Language Locale".
     * @param defaultLanguage The default language to get the keys in case the other language specified in config
     *                        doesn't exist or doesn't contain the key.
     */
    public MessageSender(@NotNull PluginConfig mainConfig, @NotNull PluginConfig defaultLanguage)
    {
        this.mainConfig = mainConfig;
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
        Optional<String> optionalLocale = mainConfig.getConfiguration().getString("Language Locale");
        PluginConfig language = optionalLocale.map(locale -> ObjectUtils.getOrDefault(getLanguage(locale), defaultLanguage))
                .orElse(defaultLanguage);

        return language.getConfiguration().getString(key).orElse(def);
    }
}
