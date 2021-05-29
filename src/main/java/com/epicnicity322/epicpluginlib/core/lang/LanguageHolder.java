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

package com.epicnicity322.epicpluginlib.core.lang;

import com.epicnicity322.epicpluginlib.core.config.ConfigurationHolder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public abstract class LanguageHolder<X, Y>
{
    private final @NotNull HashMap<String, ConfigurationHolder> languageLocales = new HashMap<>();

    /**
     * Adds a language to this {@link LanguageHolder}.
     *
     * @param locale   The locale code to be assigned to this language. E.g. "EN".
     * @param language The {@link ConfigurationHolder} containing the {@link com.epicnicity322.yamlhandler.Configuration} with keys and strings.
     */
    public void addLanguage(@NotNull String locale, @NotNull ConfigurationHolder language)
    {
        synchronized (languageLocales) {
            languageLocales.put(locale, language);
        }
    }

    /**
     * Removes a language from this {@link LanguageHolder}.
     *
     * @param locale The locale attributed to the language to be removed.
     */
    public void removeLanguage(@NotNull String locale)
    {
        synchronized (languageLocales) {
            languageLocales.remove(locale);
        }
    }

    /**
     * Gets all registered languages in this {@link LanguageHolder}.
     *
     * @return An unmodifiable {@link Map} containing the locales and its configuration with the strings.
     */
    public @NotNull Map<String, ConfigurationHolder> getLanguages()
    {
        synchronized (languageLocales) {
            return Collections.unmodifiableMap(languageLocales);
        }
    }

    /**
     * Gets the language configuration attributed to this locale.
     *
     * @param locale The locale of the language you want to get.
     * @return A {@link ConfigurationHolder} containing the {@link com.epicnicity322.yamlhandler.Configuration} with keys and strings of this locale.
     */
    public @Nullable ConfigurationHolder getLanguage(@NotNull String locale)
    {
        synchronized (languageLocales) {
            return languageLocales.get(locale);
        }
    }

    /**
     * Sends a message to the receiver with the string of the key "General.Prefix" if available. If the strings of the
     * message has no chars the message is not sent.
     *
     * @param receiver Who this message will be sent.
     * @param message  The message to send to the receiver.
     * @see #send(Object receiver, boolean prefix, String message)
     * @see #get(String key)
     */
    public void send(@NotNull Y receiver, @NotNull String message)
    {
        send(receiver, true, message);
    }

    /**
     * Sends a message to the receiver with an optional prefix assigned to the key "General.Prefix" if available. If the
     * strings of the message has no chars the message is not sent.
     *
     * @param receiver Who this message will be sent.
     * @param prefix   If this message should start with the prefix in the key "General.Prefix".
     * @param message  The message to send to the receiver.
     * @see #get(String key)
     */
    public abstract void send(@NotNull Y receiver, boolean prefix, @NotNull String message);

    /**
     * Gets the message assigned to this key already formatted. If the key was not found then a message with the string
     * "[Key not found]" will be returned.
     *
     * @param key The key of the message.
     * @return The formatted message or "[Key not found]" if key not found.
     */
    public @NotNull X getColored(@NotNull String key)
    {
        return getColored(key, "[Key not found]");
    }

    /**
     * Gets the message assigned to this key already formatted. If the key was not found then a message with the string
     * in the parameter def will be returned.
     *
     * @param key The key of the message.
     * @param def The default message to return if the key was not found.
     * @return The formatted message or the default if key not found.
     */
    public abstract X getColored(@NotNull String key, @Nullable String def);

    /**
     * Gets the message assigned to this key. If the key was not found then a message with the string "[Key not found]"
     * will be returned.
     *
     * @param key The key of the message.
     * @return The message or the default if key not found.
     */
    public @NotNull String get(@NotNull String key)
    {
        return get(key, "[Key not found]");
    }

    /**
     * Gets the message assigned to this key. If the key was not found then a message with the string in the parameter
     * def will be returned.
     *
     * @param key The key of the message.
     * @param def The default message to return if the key was not found.
     * @return The message or the default if key not found.
     */
    public abstract String get(@NotNull String key, @Nullable String def);
}
