/*
 * EpicPluginLib - Library with basic utilities for bukkit plugins.
 * Copyright (C) 2023  Christiano Rangel
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
import com.epicnicity322.yamlhandler.Configuration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.HumanEntity;
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

public final class InventoryUtils
{
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
                Bukkit.getScheduler().runTask(EpicPluginLibBukkit.getInstance(), () -> {
                    try {
                        button.accept(event);
                    } catch (Throwable t) {
                        EpicPluginLibBukkit.logger().log("Failed to accept GUI click:");
                        t.printStackTrace();
                    }
                });
            }
        }

        @EventHandler
        public void onInventoryClose(InventoryCloseEvent event)
        {
            HumanEntity player = event.getPlayer();

            if (openInventories.remove(player.getUniqueId()) != null) {
                if (openInventories.isEmpty()) HandlerList.unregisterAll(this);

                Consumer<InventoryCloseEvent> runnable = onClose.remove(player.getUniqueId());

                if (runnable != null)
                    Bukkit.getScheduler().runTask(EpicPluginLibBukkit.getInstance(), () -> {
                        try {
                            runnable.accept(event);
                        } catch (Throwable t) {
                            EpicPluginLibBukkit.logger().log("Failed to accept GUI close:");
                            t.printStackTrace();
                        }
                    });
            }
        }
    };

    private InventoryUtils()
    {
    }

    /**
     * Fills an inventory with the specified material, ignoring items that are not air.
     * <p>
     * If the material is from an item that has {@link ItemMeta}, the name is set to blank and the flag
     * {@link ItemFlag#HIDE_ATTRIBUTES} is added.
     * <p>
     * If you input an index that is greater than the inventories size or lower than 0, the operation is aborted and
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
     * If you input an index that is greater than the inventories size or lower than 0, the operation is aborted and
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
     * (The variable "{@literal <line>}" can be used to break a line).
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
        itemMeta.setLore(Arrays.asList(replaceVar(lang.getColored(configPath + ".Lore"), variables).split("<line>")));

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
     * Opens an inventory that can't have its items moved/stolen.
     * This inventory is closed when EpicPluginLib is disabled.
     *
     * @param inventory The inventory to open.
     * @param player    The player to open the inventory to.
     * @throws IllegalStateException If PlayMoreSounds is not loaded.
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
     * @throws IllegalStateException If PlayMoreSounds is not loaded.
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
     * @throws IllegalStateException If PlayMoreSounds is not loaded.
     */
    public static void openInventory(@NotNull Inventory inventory, @Nullable Map<Integer, Consumer<InventoryClickEvent>> buttons, @NotNull HumanEntity player, @Nullable Consumer<InventoryCloseEvent> onClose)
    {
        EpicPluginLibBukkit instance = EpicPluginLibBukkit.getInstance();
        if (instance == null || !instance.isEnabled()) throw new IllegalStateException("EpicPluginLib is not loaded.");

        player.openInventory(inventory);

        if (openInventories.isEmpty())
            Bukkit.getPluginManager().registerEvents(inventoryListener, instance);

        if (buttons == null) buttons = Collections.emptyMap();

        openInventories.put(player.getUniqueId(), buttons);
        InventoryUtils.onClose.put(player.getUniqueId(), onClose);
    }
}
