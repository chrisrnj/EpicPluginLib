package com.epicnicity322.epicpluginlib.sponge.lang;

import com.epicnicity322.epicpluginlib.core.config.PluginConfig;
import com.epicnicity322.epicpluginlib.core.lang.LanguageHolder;
import com.epicnicity322.epicpluginlib.core.util.ObjectUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.channel.MessageReceiver;
import org.spongepowered.api.text.serializer.TextSerializers;

public final class MessageSender extends LanguageHolder<Text, MessageReceiver>
{
    private final @NotNull PluginConfig mainConfig;
    private final @NotNull PluginConfig defaultLanguage;

    /**
     * Creates an instance of {@link MessageSender}. Message senders can get and send strings from the plugin's language.
     *
     * @param mainConfig      Your main configuration containing the key "Language Locale".
     * @param defaultLanguage The default language to get the keys in case the other language specified in config
     *                        doesn't exist or doesn't contain the key.
     */
    public MessageSender(@NotNull PluginConfig mainConfig, @NotNull PluginConfig defaultLanguage)
    {
        this.mainConfig = mainConfig;
        this.defaultLanguage = defaultLanguage;
    }

    @Override
    public void send(@NotNull MessageReceiver receiver, boolean prefix, @NotNull String message)
    {
        if (!message.isEmpty()) {
            receiver.sendMessage(TextSerializers.FORMATTING_CODE.deserialize(
                    (prefix ? get("General.Prefix", "") : "") + message));
        }
    }

    @Override
    public Text getColored(@NotNull String key, @Nullable String def)
    {
        String string = get(key, def);

        if (string == null)
            return null;
        else
            return TextSerializers.FORMATTING_CODE.deserialize(string);
    }

    @Override
    public String get(@NotNull String key, @Nullable String def)
    {
        PluginConfig language = ObjectUtils.getOrDefault(
                getLanguage(mainConfig.getYamlConfiguration().getString("Language Locale")),
                defaultLanguage);

        return language.getYamlConfiguration().getString(key, def);
    }
}
