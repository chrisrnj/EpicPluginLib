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

import com.epicnicity322.epicpluginlib.core.lang.LanguageHolder;
import com.epicnicity322.yamlhandler.Configuration;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.function.Supplier;

public final class MessageSender extends LanguageHolder<String, CommandSender>
{
    public MessageSender(@NotNull Supplier<String> currentLocale, @NotNull Configuration defaultLanguage)
    {
        super(currentLocale, defaultLanguage);
    }

    @Deprecated
    public MessageSender(@NotNull Supplier<String> locale, @Nullable Supplier<String> prefix, @NotNull Configuration defaultLanguage)
    {
        this(locale, defaultLanguage);
    }

    @Override
    protected void sendMessage(@NotNull String message, @NotNull CommandSender receiver)
    {
        receiver.sendMessage(message);
    }

    @Override
    protected @NotNull String translateColorCodes(@NotNull String message)
    {
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    @Override
    protected @Nullable UUID receiverUUID(@NotNull CommandSender receiver)
    {
        if (receiver instanceof Player) {
            return ((Player) receiver).getUniqueId();
        }
        return null;
    }

    @Override
    public void send(@NotNull CommandSender receiver, boolean prefix, @Nullable String message)
    {
        super.send(receiver, prefix, message);
    }

    @Override
    public @NotNull String getColored(@NotNull String key)
    {
        return super.getColored(key);
    }

    @Deprecated
    @Override
    public String get(@NotNull String key, @Nullable String def)
    {
        return super.get(key, def);
    }
}
