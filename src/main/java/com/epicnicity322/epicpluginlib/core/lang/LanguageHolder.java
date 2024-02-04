/*
 * EpicPluginLib - Library with basic utilities for bukkit plugins.
 * Copyright (C) 2024  Christiano Rangel
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
import com.epicnicity322.yamlhandler.Configuration;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Supplier;

/**
 * A LanguageHolder can help you manage and switch between languages. This class is intended for plugins that have more
 * than one language.
 *
 * @param <X> The colored messages type.
 * @param <Y> The receiver type.
 */
public abstract class LanguageHolder<X, Y>
{
    private final @NotNull WeakHashMap<UUID, Long> lastSentMessageTimes = new WeakHashMap<>();
    private final @NotNull HashMap<String, ConfigurationHolder> languageLocales = new HashMap<>();
    private final @NotNull Supplier<String> currentLocale;
    private final @NotNull Configuration defaultLanguage;

    protected LanguageHolder(@NotNull Supplier<String> currentLocale, @NotNull Configuration defaultLanguage)
    {
        this.currentLocale = currentLocale;
        this.defaultLanguage = defaultLanguage;
    }

    /**
     * Creates a simple, platform-independent language. This language should be used as a placeholder, you should use
     * your  platform's {@link LanguageHolder} implementing class as soon as it's available.
     *
     * @param currentLocale   The locale code to be assigned to this language. E.g. "EN_US".
     * @param defaultLanguage The {@link ConfigurationHolder} containing the {@link com.epicnicity322.yamlhandler.Configuration} with keys and strings.
     */
    public static @NotNull LanguageHolder<?, ?> simpleLanguage(@NotNull Supplier<String> currentLocale, @NotNull Configuration defaultLanguage)
    {
        return new LanguageHolder<Object, Object>(currentLocale, defaultLanguage)
        {
            @Override
            protected void sendMessage(@NotNull Object message, @NotNull Object receiver)
            {
            }

            @Override
            protected @NotNull Object translateColorCodes(@NotNull String message)
            {
                return message;
            }

            @Override
            protected @Nullable UUID receiverUUID(@NotNull Object receiver)
            {
                return null;
            }
        };
    }

    protected abstract void sendMessage(@NotNull X message, @NotNull Y receiver);

    protected abstract @NotNull X translateColorCodes(@NotNull String message);

    protected abstract @Nullable UUID receiverUUID(@NotNull Y receiver);

    /**
     * Adds a language to this {@link LanguageHolder}.
     *
     * @param locale   The locale code to be assigned to this language. E.g. "EN_US".
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
     * Sends a message to the receiver using {@link #send(Object, boolean, String)} with the prefix enabled.
     *
     * @param receiver Who this message will be sent to.
     * @param message  The message to send to the receiver.
     * @see #send(Object receiver, boolean prefix, String message)
     * @see #get(String key)
     */
    public void send(@NotNull Y receiver, @Nullable String message)
    {
        send(receiver, true, message);
    }

    /**
     * Sends a message to the receiver with a prefix assigned on the key "General.Prefix" if available. If the string
     * of the message has no chars, the message is not sent.
     * <p>
     * You can add properties to the start of the message. Properties are enclosed in brackets and have no space between
     * them (Messages can have more than one property). The first space of the message will be used to tell that the
     * message does not have any more properties. These properties are not shown in the final sent message.
     * <p>
     * These are the currently available properties:
     * <ul>
     *     <li>noprefix - Removes the default prefix of this message.</li>
     *     <li>cooldown=<b>TIME</b> - Prevents the message from being sent if another message has been sent to this receiver within the specified time. Replace <b>TIME</b> with the cooldown time in milliseconds.</li>
     *     <li>dummy - A dummy property that gets removed on the output, allowing you to use '<' at the start of the message.</li>
     * </ul>
     * An example message of how to use properties:
     * <pre>{@code "<noprefix><cooldown=5000> Hello World!"}</pre>
     * Sent output:
     * <pre>{@code "Hello World!"}</pre>
     * <p>
     * Properties silently fail, if for example, you don't use a real number in cooldown, or you don't enclose the brackets correctly.
     *
     * @param receiver Who this message will be sent to.
     * @param prefix   If this message should start with the prefix in the key "General.Prefix".
     * @param message  The message to send to the receiver.
     * @see #get(String key)
     */
    public void send(@NotNull Y receiver, boolean prefix, @Nullable String message)
    {
        if (message == null || message.isEmpty()) return;

        // Messages starting with '<' could have message-specific properties.
        if (message.charAt(0) == '<') {
            int spaceIndex = message.indexOf(' ');
            if (spaceIndex != -1) {
                String[] properties = message.substring(0, spaceIndex).split(">");

                for (String property : properties) {
                    if (property.equals("<noprefix")) {
                        prefix = false;
                    } else if (property.startsWith("<cooldown=")) {
                        if (inCooldown(receiver, property.substring(property.indexOf('=') + 1))) return;
                    } else break;
                }

                message = message.substring(spaceIndex + 1);
            }
        }

        sendMessage(translateColorCodes((prefix ? get("General.Prefix", "") : "") + message), receiver);
    }

    private boolean inCooldown(@NotNull Y receiver, @NotNull String cooldownString)
    {
        UUID uuid = receiverUUID(receiver);
        if (uuid == null) return false;

        long currentTime = System.currentTimeMillis();
        long cooldown;

        try {
            cooldown = Long.parseLong(cooldownString);
        } catch (NumberFormatException ignored) {
            return false;
        }

        Long lastMessageTime = lastSentMessageTimes.get(uuid);

        if (lastMessageTime == null) {
            lastSentMessageTimes.put(uuid, currentTime);
            return false;
        }
        if (currentTime - lastMessageTime <= cooldown) {
            return true;
        } else {
            lastSentMessageTimes.put(uuid, currentTime);
            return false;
        }
    }

    /**
     * Gets the message assigned to this key using {@link #get(String)}. The message already has its color codes
     * formatted.
     *
     * @param key The key of the message.
     * @return The formatted message or "[Key not found]" if key not found.
     */
    public @NotNull X getColored(@NotNull String key)
    {
        return translateColorCodes(get(key));
    }

    /**
     * Gets the message assigned to this key using {@link #get(String, String)}. The message already has its color codes
     * formatted.
     *
     * @param key The key path of the message.
     * @param def The default message to return if the key was not found.
     * @return The formatted message or the default if key not found.
     */
    @Contract("_,!null -> !null")
    public X getColored(@NotNull String key, @Nullable String def)
    {
        String message = get(key, def);

        if (message == null) {
            return null;
        } else {
            return translateColorCodes(message);
        }
    }

    /**
     * Gets the message assigned to this key on the current set locale. If no language is associated with the current
     * locale, the default locale is used. If the key is not found both on the current set locale and the default locale,
     * <code>"[Key not found]"</code> is returned.
     *
     * @param key The key of the message.
     * @return The message assigned to the key.
     */
    public @NotNull String get(@NotNull String key)
    {
        ConfigurationHolder language = getLanguage(currentLocale.get());

        if (language == null) {
            return defaultLanguage.getString(key).orElse("[Key not found: " + key + "]");
        } else {
            return language.getConfiguration().getString(key).orElseGet(() -> defaultLanguage.getString(key).orElse("[Key not found: " + key + "]"));
        }
    }

    /**
     * Gets the message assigned to this key on the current set locale. If no message is associated with this key,
     * then def is returned. If no language is associated with the current locale, the default locale is used.
     *
     * @param key The key path of the message.
     * @param def The default message to return if the key was not found.
     * @return The message or the default if key not found.
     * @deprecated A key should never not be present in the specified defaultLanguage, use {@link #get(String)}
     */
    @Deprecated
    @Contract("_,!null -> !null")
    public String get(@NotNull String key, @Nullable String def)
    {
        ConfigurationHolder language = getLanguage(currentLocale.get());

        if (language == null) {
            return defaultLanguage.getString(key).orElse(def);
        } else {
            return language.getConfiguration().getString(key).orElse(def);
        }
    }
}
