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

package com.epicnicity322.epicpluginlib.bukkit.lang;

import com.epicnicity322.epicpluginlib.bukkit.EpicPluginLibBukkit;
import com.epicnicity322.epicpluginlib.core.config.ConfigurationHolder;
import com.epicnicity322.epicpluginlib.core.lang.LanguageHolder;
import com.epicnicity322.yamlhandler.Configuration;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.function.Supplier;

public final class MessageSender extends LanguageHolder<String, CommandSender>
{
    private final @NotNull Supplier<String> locale;
    private final @NotNull Configuration defaultLanguage;

    /**
     * Creates an instance of {@link MessageSender}. Message senders can get and send strings from the plugin's language.
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

    private static boolean inCooldown(@NotNull CommandSender receiver, @NotNull String cooldownString)
    {
        Plugin pl = EpicPluginLibBukkit.getInstance();
        if (pl == null) return false;
        if (!(receiver instanceof Player)) return false;

        Player player = (Player) receiver;
        long currentTime = System.currentTimeMillis();
        long cooldown;

        try {
            cooldown = Long.parseLong(cooldownString);
        } catch (NumberFormatException ignored) {
            return false;
        }

        Iterator<MetadataValue> metadata = player.getMetadata("epllastsentmessage").iterator();

        if (!metadata.hasNext()) {
            player.setMetadata("epllastsentmessage", new FixedMetadataValue(pl, currentTime));
            return false;
        }

        long lastMessageTime = metadata.next().asLong();

        if (currentTime - lastMessageTime < cooldown) {
            return true;
        } else {
            player.setMetadata("epllastsentmessage", new FixedMetadataValue(pl, currentTime));
            return false;
        }
    }

    @Override
    public void send(@NotNull CommandSender receiver, boolean prefix, @NotNull String message)
    {
        if (message.isEmpty()) return;

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
                    }
                }

                message = message.substring(spaceIndex + 1);
            }
        }

        receiver.sendMessage(ChatColor.translateAlternateColorCodes('&', (prefix ? get("General.Prefix", "") : "") + message));
    }

    @Override
    public @Nullable String getColored(@NotNull String key, @Nullable String def)
    {
        String string = get(key, def);

        if (string == null) {
            return null;
        } else {
            return ChatColor.translateAlternateColorCodes('&', string);
        }
    }

    @Override
    public @Nullable String get(@NotNull String key, @Nullable String def)
    {
        ConfigurationHolder language = getLanguage(locale.get());

        if (language == null) {
            return defaultLanguage.getString(key).orElse(def);
        } else {
            return language.getConfiguration().getString(key).orElse(def);
        }
    }
}
