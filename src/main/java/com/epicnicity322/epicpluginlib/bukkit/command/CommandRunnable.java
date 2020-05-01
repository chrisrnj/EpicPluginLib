package com.epicnicity322.epicpluginlib.bukkit.command;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public interface CommandRunnable
{
    void run(@NotNull String label, @NotNull CommandSender sender, @NotNull String[] args);
}
