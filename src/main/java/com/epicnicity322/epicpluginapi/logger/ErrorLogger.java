package com.epicnicity322.epicpluginapi.logger;

import com.epicnicity322.epicpluginapi.config.ConfigManager;
import com.epicnicity322.epicpluginapi.util.Utility;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginDescriptionFile;

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
    private ConfigManager manager;
    private Logger logger;

    public ErrorLogger(ConfigManager manager, Logger logger)
    {
        this.manager = manager;
        this.logger = logger;
    }

    public void report(Exception exception, String title)
    {
        PluginDescriptionFile desc = manager.getPlugin().getDescription();

        try {
            Path folder = manager.getDataFolder().resolve("Error Report");

            Files.createDirectories(folder);

            Path error = Utility.getUniquePath(Paths.get(folder.toString(), LocalDateTime.now()
                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH.mm.ss")) + ".LOG"));

            Utility.stringToFile("=====================================================================" +
                    "\n>> Please report this file to " + desc.getAuthors() +
                    (desc.getWebsite() == null ? "" : "\n>> " + desc.getWebsite()) +
                    "\n=====================================================================" +
                    "\n" +
                    "\n - " + desc.getName() + " v" + desc.getVersion() +
                    "\n" +
                    "\n" + title +
                    "\n" + stackTraceToString(exception), error);

            if (manager.getConfigByName("config.yml").getBoolean("Notify New Error Reports to Console")) {
                logger.log("&4Warn ->&8 New log at &nError Report&8 folder.", Level.WARNING);
            }
        } catch (Exception e) {
            System.out.println(" ");
            Bukkit.getLogger().log(Level.SEVERE, "Something went wrong while reporting an error of \"" + desc.getName() + "\".");
            Bukkit.getLogger().log(Level.SEVERE, "Please contact the developer(s): " + desc.getAuthors());
            System.out.println(" ");

            Utility.sleep(3000);

            System.out.println("Error that was being reported:");
            System.out.println(" ");

            Utility.sleep(1000);

            exception.printStackTrace();

            Utility.sleep(2000);

            System.out.println(" ");
            System.out.println("Error that occurred while reporting:");
            System.out.println(" ");
            Utility.sleep(1000);

            e.printStackTrace();

            Utility.sleep(2000);
            System.out.println(" ");
            Bukkit.getLogger().log(Level.WARNING, "Please read the messages above these errors and report them.");
            System.out.println(" ");

            Utility.sleep(5000);
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
