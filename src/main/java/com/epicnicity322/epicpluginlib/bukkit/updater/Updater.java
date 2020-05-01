package com.epicnicity322.epicpluginlib.bukkit.updater;

import com.epicnicity322.epicpluginlib.core.tools.Downloader;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginDescriptionFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

/**
 * Downloads and checks for updates through spiget.org
 */
public class Updater
{
    private static final @NotNull Pattern versionSeparatorRegex = Pattern.compile("\\.");
    private final @NotNull File jar;
    private final @NotNull String currentVersion;
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
    public Updater(@NotNull File jar, @NotNull String currentVersion, int id)
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
    public Updater(@NotNull File jar, @NotNull PluginDescriptionFile descriptionFile, int id)
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
     * Checks if a version is greater than the other.
     *
     * @param version        The version to check if is greater than greaterVersion.
     * @param greaterVersion The version to check if version parameter is greater.
     * @return true if version is greater than greaterVersion.
     */
    private static boolean isVersionGreater(String version, String greaterVersion)
    {
        String[] versionNodes = versionSeparatorRegex.split(version);
        String[] greaterNodes = versionSeparatorRegex.split(greaterVersion);

        int length = Math.max(versionNodes.length, greaterNodes.length);

        for (int i = 0; i < length; ++i) {
            int versionNode = i < versionNodes.length ? Integer.parseInt(versionNodes[i]) : 0;
            int greaterNode = i < greaterNodes.length ? Integer.parseInt(greaterNodes[i]) : 0;

            if (versionNode < greaterNode)
                return false;
            if (versionNode > greaterNode)
                return true;
        }

        return false;
    }

    public @NotNull CheckResult check()
    {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Downloader downloader = new Downloader(VERSION_URL, baos);
            Thread thread = new Thread(downloader, "Update Checker");

            thread.start();

            if (thread.isAlive())
                thread.join();

            if (downloader.getResult() != Downloader.Result.SUCCESS)
                return CheckResult.valueOf(downloader.getResult().toString());

            latestVersion = new String(baos.toByteArray(), StandardCharsets.UTF_8);

            if (isVersionGreater(latestVersion, currentVersion)) {
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

    public @NotNull Downloader.Result download()
    {
        try {
            File update = Bukkit.getUpdateFolderFile();

            if (update.mkdirs()) {

                Downloader downloader = new Downloader(DOWNLOAD_URL, new FileOutputStream(new File(update,
                        jar.getName())));
                Thread thread = new Thread(downloader, "Update Downloader");

                thread.start();

                if (thread.isAlive())
                    thread.join();

                return downloader.getResult();
            } else {
                return Downloader.Result.UNEXPECTED_ERROR;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return Downloader.Result.UNEXPECTED_ERROR;
    }

    public @Nullable String getLatestVersion()
    {
        return latestVersion;
    }

    public @NotNull String getCurrentVersion()
    {
        return currentVersion;
    }

    public boolean hasUpdate()
    {
        return hasUpdate;
    }

    public enum CheckResult
    {
        /**
         * There is a update available.
         */
        AVAILABLE,
        /**
         * Latest version is installed.
         */
        NOT_AVAILABLE,
        /**
         * Unable to connect to api.spigotmc.org.
         */
        OFFLINE,
        /**
         * Connection timed out.
         */
        TIMEOUT,
        /**
         * Something went wrong while checking for updates.
         */
        UNEXPECTED_ERROR
    }
}
