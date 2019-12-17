package com.epicnicity322.epicpluginlib.updater;

import com.epicnicity322.epicpluginlib.updater.tools.Downloader;
import com.epicnicity322.epicpluginlib.util.Utility;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginDescriptionFile;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * Downloads and checks for updates through spiget.org
 */
public class Updater
{
    private File jar;
    private String currentVersion;
    private String latestVersion;
    private boolean hasUpdate = false;
    private URL VERSION_URL;
    private URL DOWNLOAD_URL;

    /**
     * Creates a instance of Updater using your plugin's jar file, the current version and the id in spigotmc.org.
     * The jar is used to name the downloaded file, in case you want to download one.
     * The version is used to check if it's greater than the latest one in spigotmc.org.
     * The id is used to check for version and find download links in spigotmc.org. You can identify the id by your
     * plugin's URL. Basically, every plugin URL should look like "https://www.spigotmc.org/resources/<name>.<id>". We
     * want the thing after the dot.
     *
     * @param jar            Your plugin's jar, you can get it by JavaPlugin#getFile().
     * @param currentVersion The current version of your plugin. Only numbers and dots allowed!
     * @param id             The id of your plugin in spigotmc.org.
     */
    public Updater(File jar, String currentVersion, int id)
    {
        this.jar = jar;
        this.currentVersion = currentVersion;

        try {
            VERSION_URL = new URL("https://api.spigotmc.org/legacy/update.php?resource=" + id);
            DOWNLOAD_URL = new URL("https://api.spiget.org/v2/resources/" + id + "/download");
        } catch (MalformedURLException ignored) {
        }
    }

    /**
     * Creates a instance of Updater using your plugin's jar file, the current version and the id in spigotmc.org.
     * The jar is used to name the downloaded file, in case you want to download one.
     * The description file is used to get your plugin's current version, so it can check if the latest version in
     * spigotmc.org is greater than your plugin version.
     * The id is used to check for version and find download links in spigotmc.org. You can identify the id by your
     * plugin's URL. Basically, every plugin URL should look like "https://www.spigotmc.org/resources/<name>.<id>". We
     * want the thing after the dot.
     *
     * @param jar             Your plugin's jar, you can get it by JavaPlugin#getFile().
     * @param descriptionFile Your plugin's plugin.yml file. In case your version contains only numbers and dots.
     * @param id              The id of your plugin in spigotmc.org.
     */
    public Updater(File jar, PluginDescriptionFile descriptionFile, int id)
    {
        this.jar = jar;
        this.currentVersion = descriptionFile.getVersion();

        try {
            VERSION_URL = new URL("https://api.spigotmc.org/legacy/update.php?resource=" + id);
            DOWNLOAD_URL = new URL("https://api.spiget.org/v2/resources/" + id + "/download");
        } catch (MalformedURLException ignored) {
        }
    }

    /**
     * AVAILABLE = There is a update available.
     * NOT_AVAILABLE = Latest version is installed.
     * OFFLINE = Unable to connect to api.spigotmc.org.
     * TIMEOUT = Connection timed out.
     * UNEXPECTED_ERROR = Something went wrong while checking for updates.
     */
    public enum CheckResult
    {
        AVAILABLE,
        NOT_AVAILABLE,
        OFFLINE,
        TIMEOUT,
        UNEXPECTED_ERROR
    }

    public CheckResult check()
    {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Downloader downloader = new Downloader(VERSION_URL, baos);
            Thread thread = new Thread(downloader, "Update Checker");

            thread.start();

            if (thread.isAlive()) {
                thread.join();
            }

            if (downloader.getResult() != Downloader.Result.SUCCESS) {
                return CheckResult.valueOf(downloader.getResult().toString());
            }

            latestVersion = new String(baos.toByteArray(), StandardCharsets.UTF_8);

            Integer latestNoDot = Integer.parseInt(latestVersion.replace(".", ""));
            Integer currentNoDot = Integer.parseInt(currentVersion.replace(".", ""));

            if (Utility.matchZeros(currentNoDot.toString().length(), latestNoDot.toString()) >
                    Utility.matchZeros(latestNoDot.toString().length(), currentNoDot.toString())) {
                hasUpdate = true;
                return CheckResult.AVAILABLE;
            } else {
                return CheckResult.NOT_AVAILABLE;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return CheckResult.UNEXPECTED_ERROR;
        }
    }

    public Downloader.Result download()
    {
        try {
            File update = Bukkit.getUpdateFolderFile();

            update.mkdirs();

            Downloader downloader = new Downloader(DOWNLOAD_URL, new FileOutputStream(new File(update, jar.getName())));
            Thread thread = new Thread(downloader, "Update Downloader");

            thread.start();

            if (thread.isAlive()) {
                thread.join();
            }

            return downloader.getResult();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return Downloader.Result.UNEXPECTED_ERROR;
    }

    public String getLatestVersion()
    {
        return latestVersion;
    }

    public String getCurrentVersion()
    {
        return currentVersion;
    }

    public boolean hasUpdate()
    {
        return hasUpdate;
    }
}
