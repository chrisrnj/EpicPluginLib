package com.epicnicity322.epicpluginapi;

import com.epicnicity322.epicpluginapi.logger.Logger;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashSet;
import java.util.logging.Level;

public class EpicPluginAPI extends JavaPlugin
{
    @Override
    public void onEnable()
    {
        HashSet<String> dependingPlugins = new HashSet<>();

        for (Plugin plugin : Bukkit.getPluginManager().getPlugins()) {
            if (plugin.getDescription().getSoftDepend().contains("EpicPluginAPI")) {
                dependingPlugins.add(plugin.getName());
            } else if (plugin.getDescription().getDepend().contains("EpicPluginAPI")) {
                dependingPlugins.add(plugin.getName());
            }
        }

        Logger logger = new Logger("[EpicPluginAPI] ", null);

        if (dependingPlugins.size() > 0) {
            logger.log("API enabled successfully.", Level.INFO);

            for (String plugin : dependingPlugins) {
                logger.log("Found dependency: " + plugin, Level.INFO);
            }
        } else {
            logger.log("API enabled but no dependencies were found.", Level.WARNING);
        }
    }
}
