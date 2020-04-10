package com.epicnicity322.epicpluginlib.config;

import com.epicnicity322.epicpluginlib.config.type.ConfigType;
import com.epicnicity322.epicpluginlib.config.type.LanguageType;
import com.epicnicity322.epicpluginlib.util.Utility;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ConfigManager
{
    private JavaPlugin plugin;
    private HashSet<ConfigType> types;
    private HashMap<ConfigType, FileConfiguration> CONFIGURATIONS = new HashMap<>();
    private HashMap<LanguageType, Configuration> LANGUAGES = new HashMap<>();

    /**
     * This class will help you extract, load and get configurations and languages.
     *
     * @param plugin The main class of your plugin.
     * @param types  A set of {@link ConfigType}, make sure you don't left any null values when constructing {@link ConfigType}.
     */
    public ConfigManager(JavaPlugin plugin, HashSet<ConfigType> types)
    {
        this.plugin = plugin;
        this.types = types;
    }

    /**
     * @param type The type that you want.
     * @return The desired configuration.
     * @throws NullPointerException Make sure you loaded the ConfigTypes you want to get.
     */
    public FileConfiguration getConfig(ConfigType type) throws NullPointerException
    {
        return CONFIGURATIONS.get(type);
    }

    /**
     * Gets a {@link ConfigType} and then gets the FileConfiguration in the map.
     * Make sure you use {@link ConfigManager#loadConfig()} before using this.
     *
     * @param name The name of the config (Must contain .yml extension)
     * @return The FileConfiguration if this {@link ConfigType} was specified in {@link ConfigManager}'s constructor.
     * @throws NullPointerException If a {@link ConfigType} with this name wasn't found or wasn't loaded yet.
     */
    public FileConfiguration getConfigByName(String name) throws Exception
    {
        for (ConfigType type : types) {
            if (type.getName().equals(name)) {
                if (CONFIGURATIONS.containsKey(type)) {
                    return CONFIGURATIONS.get(type);
                } else {
                    throw new Exception("Configuration must be loaded before using this method.");
                }
            }
        }

        throw new NullPointerException("Param \"" + name + "\" is not specified as a ConfigType in ConfigManager's constructor.");
    }

    /**
     * Gets a converted hard coded, jar and extracted language to {@link Configuration}
     * Make sure you use {@link ConfigManager#loadConfig()} and {@link ConfigManager#loadLanguage(HashSet, HashMap)} first.
     *
     * @param type The type that you want.
     * @return Language in form of Configuration/YamlConfiguration.
     * @throws NullPointerException Make sure you loaded the languages first.
     */
    public Configuration getLanguage(LanguageType type) throws NullPointerException
    {
        return LANGUAGES.get(type);
    }

    /**
     * This helps the usage of less params in other constructors.
     *
     * @return The main class of the plugin.
     */
    public JavaPlugin getPlugin()
    {
        return plugin;
    }

    /**
     * This creates the data folder before you use it, just to avoid errors and simplify code.
     *
     * @return The data folder as {@link Path}
     */
    public Path getDataFolder()
    {
        File dataFolder = plugin.getDataFolder();

        dataFolder.mkdir();
        return dataFolder.toPath();
    }


    /**
     * Extracts language from the plugin jar and converts hard coded language and jar language into {@link Configuration}
     * Make sure you use {@link ConfigManager#loadConfig()} before using this.
     *
     * @param acceptable_language_versions If language is already extracted, what version can it be to not be restored?
     * @param hardCodedLang                The language in form of HashMap, key and value just like YamlConfiguration keys.
     * @return The load result of each language.
     * @throws Exception If config.yml isn't loaded or isn't in the {@link HashSet}<{@link ConfigType}> specified in this class constructor.
     */
    public HashMap<LanguageType, LoadOutput> loadLanguage(HashSet<String> acceptable_language_versions, HashMap<String, String> hardCodedLang) throws Exception
    {
        HashMap<LanguageType, LoadOutput> loadResult = new HashMap<>();
        String locale;

        locale = getConfigByName("config.yml").getString("Locale");

        if (locale == null) {
            throw new NullPointerException("Config.yml doesn't contain \"Locale\" key.");
        }

        try {
            Path langFolder = getDataFolder().resolve("Language");

            langFolder.toFile().mkdir();

            Path langFile = langFolder.resolve("Language " + locale.toUpperCase() + ".yml");

            boolean extract = false;

            if (Files.exists(langFile)) {
                FileConfiguration language = YamlConfiguration.loadConfiguration(langFile.toFile());
                LANGUAGES.put(LanguageType.EXTERNAL, language);

                if (!language.contains("Version") || !acceptable_language_versions.contains(language.getString("Version"))) {
                    loadResult.put(LanguageType.EXTERNAL, LoadOutput.output(LoadOutput.LoadResult.RESTORED_OLD));
                    Files.move(langFile, Utility.getUniquePath(Paths.get(langFile.toString() + ".old")));
                    extract = true;
                }
            } else {
                extract = true;
            }

            if (extract) {
                try {
                    plugin.saveResource("lang/" + langFile.getFileName(), true);

                    Path extractedLang = getDataFolder().resolve("lang");

                    try (Stream<Path> list = Files.list(extractedLang)) {
                        for (Path p : list.collect(Collectors.toCollection(HashSet::new))) {
                            Files.move(p, langFolder.resolve(p.getFileName()));
                        }
                    }

                    Files.delete(extractedLang);
                } catch (Exception e) {
                    loadResult.put(LanguageType.EXTERNAL, LoadOutput.output(LoadOutput.LoadResult.ERROR_EXTRACTION, e));
                }

                if (Files.exists(langFile)) {
                    LANGUAGES.put(LanguageType.EXTERNAL, YamlConfiguration.loadConfiguration(langFile.toFile()));
                }
            }

            loadResult.put(LanguageType.EXTERNAL, LoadOutput.output(LoadOutput.LoadResult.SUCCESS));
        } catch (Exception e) {
            loadResult.put(LanguageType.EXTERNAL, LoadOutput.output(LoadOutput.LoadResult.ERROR_LOAD, e));
        }

        //Language in jar load
        try {
            LANGUAGES.put(LanguageType.JAR, YamlConfiguration.loadConfiguration(new InputStreamReader(Objects.requireNonNull(plugin.getResource("lang/Language " + locale + ".yml")))));
            loadResult.put(LanguageType.JAR, LoadOutput.output(LoadOutput.LoadResult.SUCCESS));
        } catch (Exception e) {
            loadResult.put(LanguageType.JAR, LoadOutput.output(LoadOutput.LoadResult.ERROR_LOAD, e));
        }

        //Hard coded language load
        try {
            LANGUAGES.put(LanguageType.HARD_CODED, new MemoryConfiguration());

            for (String key : hardCodedLang.keySet()) {
                LANGUAGES.get(LanguageType.HARD_CODED).set(key, hardCodedLang.get(key));
            }

            loadResult.put(LanguageType.HARD_CODED, LoadOutput.output(LoadOutput.LoadResult.SUCCESS));
        } catch (Exception e) {
            loadResult.put(LanguageType.HARD_CODED, LoadOutput.output(LoadOutput.LoadResult.ERROR_LOAD, e));
        }

        return loadResult;
    }

    /**
     * Extracts and loads all {@link ConfigType} specified in the constructor.
     *
     * @return The load result of each configuration.
     */
    public HashMap<ConfigType, LoadOutput> loadConfig()
    {
        HashMap<ConfigType, LoadOutput> loadResult = new HashMap<>();

        for (ConfigType type : types) {
            String confName = type.getName();
            Path parentFolder = type.getFolder();
            Path confPath = parentFolder.resolve(confName);

            try {
                boolean createConfig = false;

                if (Files.exists(confPath)) {
                    CONFIGURATIONS.put(type, YamlConfiguration.loadConfiguration(confPath.toFile()));
                    FileConfiguration loadedConf = CONFIGURATIONS.get(type);

                    if (!loadedConf.contains("Version") || !type.getAcceptableVersions().contains(loadedConf.getString("Version"))) {
                        loadResult.put(type, LoadOutput.output(LoadOutput.LoadResult.RESTORED_OLD));
                        Files.move(confPath, Utility.getUniquePath(parentFolder.resolve(confPath.getFileName() + ".old")));
                        createConfig = true;
                    }
                } else {
                    createConfig = true;
                }

                if (createConfig) {
                    try {
                        Utility.stringToFile(type.getDefaults(), confPath);
                    } catch (Exception e) {
                        loadResult.put(type, LoadOutput.output(LoadOutput.LoadResult.ERROR_EXTRACTION, e));
                    }

                    if (Files.exists(confPath)) {
                        CONFIGURATIONS.put(type, YamlConfiguration.loadConfiguration(confPath.toFile()));
                    }
                }

                loadResult.putIfAbsent(type, LoadOutput.output(LoadOutput.LoadResult.SUCCESS));
            } catch (Exception e) {
                loadResult.put(type, LoadOutput.output(LoadOutput.LoadResult.ERROR_LOAD, e));
            }
        }

        return loadResult;
    }
}
