package com.epicnicity322.epicpluginlib.bukkit.logger;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.logging.Level;
import java.util.regex.Pattern;

public class Logger
{
    private static final @NotNull Pattern formatCodes = Pattern.compile("&[a-fk-o0-9r]");
    private final @NotNull String prefix;

    public Logger(@NotNull String prefix)
    {
        this.prefix = prefix;
    }

    /**
     * Logs formatted messages with the prefix to console.
     *
     * @param message The message with color codes to send to console.
     */
    public void log(@NotNull String message)
    {
        log(Bukkit.getConsoleSender(), message);
    }

    /**
     * Removes color codes the from message and logs to console with a specific {@link Level} and the prefix using bukkit's
     * {@link java.util.logging.Logger}.
     *
     * @param message The message to log to console.
     */
    public void log(@NotNull String message, @NotNull Level level)
    {
        Bukkit.getLogger().log(level, formatCodes.matcher(message).replaceAll(""));
    }

    /**
     * Sends formatted messages with the prefix to the {@link CommandSender}.
     *
     * @param sender  Who the message will be sent.
     * @param message The message to be sent.
     */
    public void log(@NotNull CommandSender sender, @NotNull String message)
    {
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + message));
    }
}
