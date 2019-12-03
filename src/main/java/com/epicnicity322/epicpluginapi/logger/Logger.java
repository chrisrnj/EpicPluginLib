package com.epicnicity322.epicpluginapi.logger;

import com.sun.istack.internal.Nullable;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.logging.Level;

public class Logger
{
    private String prefix;
    private FileConfiguration config;

    public Logger(String prefix, @Nullable FileConfiguration mainConfig)
    {
        this.prefix = prefix;
        config = mainConfig;
    }

    public void log(String message, Level level)
    {
        if (config == null || !config.getBoolean("Show Colored Messages In Console")) {
            Bukkit.getLogger().log(level, (prefix + message).replaceAll("&1|&2|&3|&4|&5|&6|&7|&8|&9|&0|&a|&b|&c|&d|&e|&f|&k|&l|&m|&n|&o|&r", ""));
            return;
        }

        Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + message));
    }

    public void log(CommandSender sender, String message)
    {
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + message));
    }
}
