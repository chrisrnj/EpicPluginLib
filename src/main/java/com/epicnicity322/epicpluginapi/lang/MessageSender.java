package com.epicnicity322.epicpluginapi.lang;

import com.epicnicity322.epicpluginapi.config.ConfigManager;
import com.epicnicity322.epicpluginapi.config.type.LanguageType;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.Configuration;

import java.nio.file.Files;
import java.nio.file.Path;

public class MessageSender
{
    private ConfigManager manager;

    /**
     * Make sure config and language are loaded before using this class.
     */
    public MessageSender(ConfigManager manager)
    {
        this.manager = manager;
    }

    public void send(CommandSender sender, boolean prefix, String message)
    {
        if (!message.contains("<null>")) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', (prefix ? get("General.Prefix") : "") + message));
        }
    }

    public String getColored(String string)
    {
        return ChatColor.translateAlternateColorCodes('&', get(string));
    }

    public String get(String string)
    {
        try {
            Path langFile = manager.getDataFolder().resolve("Language").resolve("Language " + manager.getConfigByName("config.yml").getString("Locale") + ".yml");

            Configuration jar = manager.getLanguage(LanguageType.JAR);
            Configuration hardCoded = manager.getLanguage(LanguageType.HARD_CODED);

            if (Files.exists(langFile)) {
                Configuration lang = manager.getLanguage(LanguageType.EXTERNAL);

                if (lang.contains(string)) {
                    return lang.getString(string);
                } else if (jar.contains(string)) {
                    return jar.getString(string);
                } else {
                    return hardCoded.getString(string);
                }
            } else if (jar.contains(string)) {
                return jar.getString(string);
            } else {
                return hardCoded.getString(string);
            }
        } catch (Exception e) {
            return "&fString not found. ";
        }
    }
}
