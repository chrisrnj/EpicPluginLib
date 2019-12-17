package com.epicnicity322.epicpluginlib;

import com.epicnicity322.epicpluginlib.logger.Logger;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashSet;
import java.util.logging.Level;

public class EpicPluginLib extends JavaPlugin
{
    @Override
    public void onEnable()
    {
        HashSet<String> dependingPlugins = new HashSet<>();

        for (Plugin plugin : Bukkit.getPluginManager().getPlugins()) {
            if (plugin.getDescription().getSoftDepend().contains("EpicPluginLib")) {
                dependingPlugins.add(plugin.getName());
            } else if (plugin.getDescription().getDepend().contains("EpicPluginLib")) {
                dependingPlugins.add(plugin.getName());
            }
        }

        Logger logger = new Logger("[EpicPluginLib] ", null);

        if (dependingPlugins.size() > 0) {
            logger.log("Lib enabled successfully.", Level.INFO);

            for (String plugin : dependingPlugins) {
                logger.log("Found dependency: " + plugin, Level.INFO);
            }
        } else {
            logger.log("Lib enabled but no dependencies were found.", Level.WARNING);
        }
    }
}
