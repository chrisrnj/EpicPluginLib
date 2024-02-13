/*
 * EpicPluginLib - Library with basic utilities for bukkit plugins.
 * Copyright (C) 2024  Christiano Rangel
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.epicnicity322.epicpluginlib.bukkit.util;

import com.epicnicity322.epicpluginlib.bukkit.EpicPluginLibBukkit;
import com.epicnicity322.epicpluginlib.bukkit.lang.MessageSender;
import com.epicnicity322.epicpluginlib.core.util.StringUtils;
import com.epicnicity322.yamlhandler.Configuration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;
import java.util.regex.Pattern;

public final class InventoryUtils
{
    private static final @NotNull Pattern loreLineBreaker = Pattern.compile("<line>|\\n");
    private static final @NotNull HashMap<UUID, Map<Integer, Consumer<InventoryClickEvent>>> openInventories = new HashMap<>();
    private static final @NotNull HashMap<UUID, Consumer<InventoryCloseEvent>> onClose = new HashMap<>();
    private static final @NotNull Listener inventoryListener = new Listener()
    {
        @EventHandler(priority = EventPriority.LOWEST)
        public void onInventoryClick(InventoryClickEvent event)
        {
            if (event.getClickedInventory() == null) return;

            HumanEntity player = event.getWhoClicked();
            Map<Integer, Consumer<InventoryClickEvent>> buttons = openInventories.get(player.getUniqueId());

            if (buttons == null) return;

            event.setCancelled(true);

            Consumer<InventoryClickEvent> button = buttons.get(event.getRawSlot());

            if (button != null) {
                try {
                    button.accept(event);
                } catch (Throwable t) {
                    EpicPluginLibBukkit.logger().log("Failed to accept GUI click:");
                    t.printStackTrace();
                }
            }
        }

        @EventHandler
        public void onInventoryClose(InventoryCloseEvent event)
        {
            HumanEntity player = event.getPlayer();

            if (openInventories.remove(player.getUniqueId()) != null) {
                if (openInventories.isEmpty()) HandlerList.unregisterAll(this);

                Consumer<InventoryCloseEvent> runnable = onClose.remove(player.getUniqueId());

                if (runnable != null) try {
                    runnable.accept(event);
                } catch (Throwable t) {
                    EpicPluginLibBukkit.logger().log("Failed to accept GUI close:");
                    t.printStackTrace();
                }

                player.closeInventory(); // Sometimes inventory close event is called without the inventory actually closing.
            }
        }
    };

    static {
        // Prevent stealing items by closing the inventory once the server disables.
        EpicPluginLibBukkit.runOnDisable(() -> {
            for (UUID id : openInventories.keySet()) {
                Player player = Bukkit.getPlayer(id);
                if (player == null) continue;
                player.getOpenInventory().getTopInventory().clear();
                player.closeInventory();
            }

            openInventories.clear();
            onClose.clear();
        });
    }

    private InventoryUtils()
    {
    }

    /**
     * Fills an inventory with the specified material, ignoring items that are not air.
     * <p>
     * If the material is from an item that has {@link ItemMeta}, the name is set to blank and the flag
     * {@link ItemFlag#HIDE_ATTRIBUTES} is added.
     * <p>
     * If you input an index that is greater than the inventory's size or lower than 0, the operation is aborted and
     * nothing is filled.
     *
     * @param inventory  The inventory to fill.
     * @param from_index The slot to start the filling.
     * @param to_index   The slot to stop the filling.
     * @see #forceFill(Material, Inventory, int, int)
     */
    public static void fill(@NotNull Material material, @NotNull Inventory inventory, int from_index, int to_index)
    {
        int size = inventory.getSize();
        if (from_index < 0 || to_index < 0 || from_index >= size || to_index >= size) return;

        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.setDisplayName(" ");
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            item.setItemMeta(meta);
        }

        for (int slot = from_index; slot <= to_index; ++slot) {
            ItemStack previous = inventory.getItem(slot);

            if (previous == null || !previous.getType().isAir()) {
                inventory.setItem(slot, item);
            }
        }
    }

    /**
     * Fills an inventory with the specified material, items that were already set in the index range are replaced.
     * <p>
     * If the material is from an item that has {@link ItemMeta}, the name is set to blank and the flag
     * {@link ItemFlag#HIDE_ATTRIBUTES} is added.
     * <p>
     * If you input an index that is greater than the inventory's size or lower than 0, the operation is aborted and
     * nothing is filled.
     *
     * @param inventory  The inventory to fill.
     * @param from_index The slot to start the filling.
     * @param to_index   The slot to stop the filling.
     * @see #fill(Material, Inventory, int, int)
     */
    public static void forceFill(@NotNull Material material, @NotNull Inventory inventory, int from_index, int to_index)
    {
        int size = inventory.getSize();
        if (from_index < 0 || to_index < 0 || from_index >= size || to_index >= size) return;

        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.setDisplayName(" ");
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            item.setItemMeta(meta);
        }

        for (int slot = from_index; slot <= to_index; ++slot) {
            inventory.setItem(slot, item);
        }
    }

    /**
     * Creates an {@link ItemStack} based on the {@link Material} specified in the provided configuration.
     * <p>
     * The item's material and whether it will be glowing properties will be got from the provided config, in the path
     * "Path.Material" and "Path.Glowing". (If the material is not found, or it does not have an {@link ItemMeta}, {@link Material#STONE} is used.)
     * <p>
     * The item's name and lore will be got from the provided language in the path "Path.Display Name" and "Path.Lore"
     * (The variable "{@literal <line>}" can be used to break a line).
     *
     * @param configPath The path to get material, glowing, display name and lore.
     * @param config     The config to look for the material and glowing paths.
     * @param lang       The language to look for display name and lore paths.
     * @return An item stack as specified in the plugin's configuration.
     * @see #getItemStack(String, Configuration, MessageSender, String...)
     */
    public static @NotNull ItemStack getItemStack(@NotNull String configPath, @NotNull Configuration config, @NotNull MessageSender lang)
    {
        return getItemStack(configPath, config, lang, (String) null);
    }

    /**
     * Creates an {@link ItemStack} based on the {@link Material} specified in the provided configuration.
     * <p>
     * The item's material and whether it will be glowing properties will be got from the provided config, in the path
     * "Path.Material" and "Path.Glowing". (If the material is not found, or it does not have an {@link ItemMeta}, {@link Material#STONE} is used.)
     * <p>
     * The item's name and lore will be got from the provided language in the path "Path.Display Name" and "Path.Lore"
     * (The variable "{@literal <line>}" or "\n" can be used to break a line).
     * <p>
     * The variables in the name and lore will be replaced according to the "variables" array, the variables are
     * set according to the index, so "{@literal <var0>}" will be replaced to the first string, "{@literal <var1>}" to
     * the second, etc...
     *
     * @param configPath The path to get material, glowing, display name and lore.
     * @param config     The config to look for the material and glowing paths.
     * @param lang       The language to look for display name and lore paths.
     * @param variables  The variables to replace in the item's name and lore.
     * @return An item stack as specified in the plugin's configuration.
     */
    public static @NotNull ItemStack getItemStack(@NotNull String configPath, @NotNull Configuration config, @NotNull MessageSender lang, @Nullable String... variables)
    {
        Material material = Material.matchMaterial(config.getString(configPath + ".Material").orElse("STONE"));
        if (material == null || material.isAir()) material = Material.STONE;
        ItemStack itemStack = new ItemStack(material);
        ItemMeta itemMeta = itemStack.getItemMeta();

        itemMeta.setDisplayName(replaceVar(lang.getColored(configPath + ".Display Name"), variables));
        itemMeta.setLore(Arrays.asList(loreLineBreaker.split(replaceVar(lang.getColored(configPath + ".Lore"), variables))));

        if (config.getBoolean(configPath + ".Glowing").orElse(false))
            itemMeta.addEnchant(Enchantment.DURABILITY, 1, true);

        itemMeta.addItemFlags(ItemFlag.values());

        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }

    private static @NotNull String replaceVar(@NotNull String string, @Nullable String... variables)
    {
        if (variables == null) return string;
        for (int i = 0; i < variables.length; ++i) {
            String var = variables[i];
            if (var != null) string = string.replace("<var" + i + ">", var);
        }
        return string;
    }

    /**
     * Opens an inventory that can't have any items moved/stolen.
     * This inventory is closed when EpicPluginLib is disabled.
     *
     * @param inventory The inventory to open.
     * @param player    The player to open the inventory to.
     * @throws IllegalStateException If EpicPluginLib is not loaded.
     * @see #openInventory(Inventory inventory, Map buttons, HumanEntity player, Consumer onClose)
     */
    public static void openInventory(@NotNull Inventory inventory, @NotNull HumanEntity player)
    {
        openInventory(inventory, null, player, null);
    }

    /**
     * Opens an inventory that you can map each slot number to a {@link Consumer} for {@link InventoryClickEvent}, so
     * when the player clicks specified slot, the consumer is accepted.
     * The items in this inventory can not be moved/stolen.
     * This inventory is closed when EpicPluginLib is disabled.
     *
     * @param inventory The inventory to open.
     * @param buttons   The map with the number of the slot that when clicked will run the {@link Runnable}.
     * @param player    The player to open the inventory to.
     * @throws IllegalStateException If EpicPluginLib is not loaded.
     * @see #openInventory(Inventory inventory, Map buttons, HumanEntity player, Consumer onClose)
     */
    public static void openInventory(@NotNull Inventory inventory, @NotNull Map<Integer, Consumer<InventoryClickEvent>> buttons, @NotNull HumanEntity player)
    {
        openInventory(inventory, buttons, player, null);
    }

    /**
     * Opens an inventory that you can map each slot number to a {@link Consumer} for {@link InventoryClickEvent}, so
     * when the player clicks the specified slot, the consumer is accepted. Also, a {@link Consumer} for
     * {@link InventoryCloseEvent} is accepted when the inventory closes.
     * The items in this inventory can not be moved/stolen.
     * This inventory is closed when EpicPluginLib is disabled.
     *
     * @param inventory The inventory to open.
     * @param buttons   The map with the number of the slot that when clicked will run the {@link Runnable}.
     * @param player    The player to open the inventory to.
     * @param onClose   The runnable to run when the inventory is closed.
     * @throws IllegalStateException If EpicPluginLib is not loaded.
     */
    public static void openInventory(@NotNull Inventory inventory, @Nullable Map<Integer, Consumer<InventoryClickEvent>> buttons, @NotNull HumanEntity player, @Nullable Consumer<InventoryCloseEvent> onClose)
    {
        EpicPluginLibBukkit instance = EpicPluginLibBukkit.getInstance();
        if (instance == null || !instance.isEnabled()) throw new IllegalStateException("EpicPluginLib is not loaded.");

        // Opening inventory if it isn't open, returning if inventory could not be open.
        if (!inventory.getViewers().contains(player)) if (player.openInventory(inventory) == null) return;

        if (openInventories.isEmpty()) Bukkit.getPluginManager().registerEvents(inventoryListener, instance);

        if (buttons == null) buttons = Collections.emptyMap();

        openInventories.put(player.getUniqueId(), buttons);
        InventoryUtils.onClose.put(player.getUniqueId(), onClose);
    }

    /**
     * Breaks lines automatically of a text to make it fit in an item's lore. This is useful to make so the lore doesn't
     * clip through the edge of the player's screen.
     * <p>
     * If a line's length is greater than the maxCharactersPerLine, the line breaks.
     * <p>
     * Here's an example of how the final lore will look like if the maxCharactersPerLine limit is 35 and maxLines is 5:
     * <blockquote>
     * Lorem ipsum dolor sit amet,<br>
     * consectetur adipiscing elit, sed do<br>
     * eiusmod tempor incididunt ut labore<br>
     * et dolore magna aliqua. Ut enim ad<br>
     * minim veniam, quis nostrud...
     * </blockquote>
     *
     * @param lore                     The text to break lines.
     * @param maxCharactersPerLine     The max characters each line of the text is allowed to have. Usually the lore can have up to 40 characters without clipping through the screen.
     * @param maxLines                 The max lines the text will have, a "..." will be appended to the end if the text has more than this limit. Use -1 for no limit.
     * @param lengthAlreadyInFirstLine If you want to place this text after something already in the lore, specify the length of the line here to format properly.
     * @param lineBreak                What to use at the end of every line.
     * @return The formatted lore text.
     * @deprecated Moved to {@link StringUtils#breakLore(String, int, int, int, String)} due to no dependency in bukkit API.
     */
    @Deprecated
    public static @NotNull String breakLore(@NotNull String lore, int maxCharactersPerLine, int maxLines, int lengthAlreadyInFirstLine, @NotNull String lineBreak)
    {
        return StringUtils.breakLore(lore, maxCharactersPerLine, maxLines, lengthAlreadyInFirstLine, lineBreak);
    }
}
