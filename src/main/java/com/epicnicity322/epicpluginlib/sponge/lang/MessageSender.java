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

package com.epicnicity322.epicpluginlib.sponge.lang;

import com.epicnicity322.epicpluginlib.core.lang.LanguageHolder;
import com.epicnicity322.yamlhandler.Configuration;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.api.entity.living.player.Player;

import java.util.UUID;
import java.util.function.Supplier;

public final class MessageSender extends LanguageHolder<TextComponent, Audience>
{
    public MessageSender(@NotNull Supplier<String> currentLocale, @NotNull Configuration defaultLanguage)
    {
        super(currentLocale, defaultLanguage);
    }

    @Deprecated
    public MessageSender(@NotNull Supplier<String> locale, @Nullable Supplier<String> prefix, @NotNull Configuration defaultLanguage)
    {
        super(locale, defaultLanguage);
    }

    @Override
    protected void sendMessage(@NotNull TextComponent message, @NotNull Audience receiver)
    {
        receiver.sendMessage(message);
    }

    @Override
    protected @NotNull TextComponent translateColorCodes(@NotNull String message)
    {
        return LegacyComponentSerializer.legacyAmpersand().deserialize(message);
    }

    @Override
    protected @Nullable UUID receiverUUID(@NotNull Audience receiver)
    {
        if (receiver instanceof Player) {
            return ((Player) receiver).uniqueId();
        }
        return null;
    }
}
