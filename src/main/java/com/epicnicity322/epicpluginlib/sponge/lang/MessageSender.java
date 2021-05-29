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
