package com.epicnicity322.epicpluginlib.logger;

import com.epicnicity322.epicpluginlib.config.ConfigManager;
import com.epicnicity322.epicpluginlib.util.Utility;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginDescriptionFile;
import org.jetbrains.annotations.Nullable;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.logging.Level;

public class ErrorLogger
{
    private PluginDescriptionFile description;
    private Path dataFolder;
    private ConfigManager manager;

    public ErrorLogger(PluginDescriptionFile description, Path dataFolder, @Nullable ConfigManager manager)
    {
        this.description = description;
        this.dataFolder = dataFolder;
        this.manager = manager;
    }

    public void setConfigManager(ConfigManager manager)
    {
        this.manager = manager;
    }

    public void report(Exception exception, String title)
    {
        try {
            Path folder = dataFolder.resolve("Error Report");

            if (!Files.exists(folder)) {
                Files.createDirectories(folder);
            }

            Path error = Utility.getUniquePath(Paths.get(folder.toString(), LocalDateTime.now()
                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH.mm.ss")) + ".LOG"));

            Utility.stringToFile("=====================================================================" +
                    "\n>> Please report this file to " + description.getAuthors() +
                    (description.getWebsite() == null ? "" : "\n>> " + description.getWebsite()) +
                    "\n=====================================================================" +
                    "\n" +
                    "\n - " + description.getName() + " v" + description.getVersion() +
                    "\n" +
                    "\n" + title +
                    "\n" + stackTraceToString(exception), error);

            if (manager != null && manager.getConfigByName("config.yml").getBoolean("Notify New Error Reports to Console")) {
                manager.getPlugin().getLogger().log(Level.WARNING, "&4Warn ->&8 New log at &nError Report&8 folder.");
            }
        } catch (Exception e) {
            System.out.println(" ");
            Bukkit.getLogger().log(Level.SEVERE, "Something went wrong while reporting an error of \"" + description.getName() + "\".");
            Bukkit.getLogger().log(Level.SEVERE, "Please contact the developer(s): " + description.getAuthors());
            System.out.println(" ");
            System.out.println("Error that was being reported:");
            System.out.println(" ");
            exception.printStackTrace();
            System.out.println(" ");
            System.out.println("Error that occurred while reporting:");
            System.out.println(" ");
            e.printStackTrace();
            System.out.println(" ");
            Bukkit.getLogger().log(Level.WARNING, "Please read the messages above these errors and report them.");
            System.out.println(" ");
        }
    }

    public static String stackTraceToString(Exception exception)
    {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        exception.printStackTrace(pw);
        return sw.toString();
    }
}
