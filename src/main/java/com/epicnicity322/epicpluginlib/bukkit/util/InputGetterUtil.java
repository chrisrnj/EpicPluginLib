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
import com.epicnicity322.epicpluginlib.bukkit.reflection.ReflectionUtil;
import com.epicnicity322.epicpluginlib.core.EpicPluginLib;
import com.epicnicity322.epicpluginlib.core.logger.ConsoleLogger;
import org.bukkit.Location;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/**
 * A class to help getting input from a player. Useful for GUIs.
 */
public final class InputGetterUtil
{
    private static final @NotNull HashMap<UUID, WaitingInput> inputListeningPlayers = new HashMap<>();
    private static final boolean hasOpenAnvil = ReflectionUtil.getMethod(HumanEntity.class, "openAnvil", Location.class, boolean.class) != null;
    private static final @NotNull Listener chatListener = new Listener()
    {
        @EventHandler(priority = EventPriority.LOWEST)
        public void onChat(AsyncPlayerChatEvent event)
        {
            WaitingInput waiting = inputListeningPlayers.remove(event.getPlayer().getUniqueId());
            if (inputListeningPlayers.isEmpty()) HandlerList.unregisterAll(this);
            if (waiting == null) return;

            event.setCancelled(true);
            waiting.task.cancel();
            String message = event.getMessage();

            try {
                waiting.onInput.accept(message);
            } catch (Throwable t) {
                EpicPluginLibBukkit.logger().log("Failed to accept Chat Input:", ConsoleLogger.Level.WARN);
                t.printStackTrace();
            }
        }
    };

    private InputGetterUtil()
    {
    }

    /**
     * Asks the player for input. If the server is running on paper ({@link EpicPluginLib.Platform#isPaper()}), an Anvil
     * Input Inventory is opened using {@link #askAnvilInput(HumanEntity, ItemStack, Consumer)}, otherwise, the input is
     * requested in chat using {@link #askChatInput(HumanEntity, long, Consumer)}
     * <p>
     * If the request is in chat, a prompt message must be sent to the player. Use the returned boolean of this method to
     * know if a prompt message should be sent.
     * <p>
     * Uses 30 seconds as default for interval for chat input.
     *
     * @param player    The player to ask the input for.
     * @param inputItem The item to set in the first slot of the anvil.
     * @param onInput   The consumer to be accepted once the player sends the input.
     * @return Whether the input is being waited for in chat.
     * @throws IllegalStateException If EpicPluginLib is not loaded.
     * @see #askInput(HumanEntity, ItemStack, long, Consumer)
     */
    public static boolean askInput(@NotNull HumanEntity player, @NotNull ItemStack inputItem, @NotNull Consumer<String> onInput)
    {
        return askInput(player, inputItem, 600, onInput);
    }

    /**
     * Asks the player for input. If the server is running on paper ({@link EpicPluginLib.Platform#isPaper()}), an Anvil
     * Input Inventory is opened using {@link #askAnvilInput(HumanEntity, ItemStack, Consumer)}, otherwise, the input is
     * requested in chat using {@link #askChatInput(HumanEntity, long, Consumer)}
     * <p>
     * If the request is in chat, a prompt message must be sent to the player. Use the returned boolean of this method to
     * know if a prompt message should be sent.
     *
     * @param player    The player to ask the input for.
     * @param inputItem The item to set in the first slot of the anvil.
     * @param interval  The interval to wait before stop waiting for input IN CHAT.
     * @param onInput   The consumer to be accepted once the player sends the input.
     * @return Whether the input is being waited for in chat.
     * @throws IllegalStateException If EpicPluginLib is not loaded.
     * @see #askAnvilInput(HumanEntity, ItemStack, Consumer)
     * @see #askChatInput(HumanEntity, long, Consumer, Runnable)
     */
    public static boolean askInput(@NotNull HumanEntity player, @NotNull ItemStack inputItem, long interval, @NotNull Consumer<String> onInput)
    {
        if (askAnvilInput(player, inputItem, onInput)) return false;
        askChatInput(player, interval, onInput);
        return true;
    }

    /**
     * Opens an Anvil Inventory to the player. The player will type in the input on the rename item part of the anvil.
     * Once the player clicks the result item or closes the inventory, the item's name is used as input and onInput consumer is accepted.
     *
     * @param player    The player to open the Anvil Input Inventory to.
     * @param inputItem The item to set in the first slot of the anvil.
     * @param onInput   The consumer to be accepted once the player types in the input.
     * @return Whether the anvil inventory opened successfully.
     * @throws IllegalStateException If EpicPluginLib is not loaded.
     * @see #askInput(HumanEntity, ItemStack, Consumer) Use this method to always ensure the input is asked for correctly.
     */
    public static boolean askAnvilInput(@NotNull HumanEntity player, @NotNull ItemStack inputItem, @NotNull Consumer<String> onInput)
    {
        if (!hasOpenAnvil) return false;

        InventoryView view = player.openAnvil(null, true);
        if (view == null) return false;
        Inventory anvil = view.getTopInventory();

        if (anvil.getType() != InventoryType.ANVIL) {
            player.closeInventory();
            return false;
        }

        view.setItem(0, inputItem);

        AtomicBoolean ignoreClose = new AtomicBoolean(false);

        InventoryUtils.openInventory(anvil, Collections.singletonMap(2, event -> {
            ItemStack item = event.getCurrentItem();
            ItemMeta meta = item == null ? null : item.getItemMeta();
            String input = meta == null ? "" : meta.getDisplayName();

            try {
                ignoreClose.set(true);
                view.close();
                ignoreClose.set(false);
                onInput.accept(input);
            } catch (Throwable t) {
                EpicPluginLibBukkit.logger().log("Failed to accept Anvil Input:", ConsoleLogger.Level.WARN);
                t.printStackTrace();
            }
        }), player, event -> {
            Inventory inventory = event.getInventory();

            if (ignoreClose.get()) {
                inventory.setItem(0, null);
                return;
            }

            ItemStack item = inventory.getItem(2);
            ItemMeta meta = item == null ? null : item.getItemMeta();
            String input = meta == null ? "" : meta.getDisplayName();
            inventory.setItem(0, null);

            try {
                onInput.accept(input);
            } catch (Throwable t) {
                EpicPluginLibBukkit.logger().log("Failed to accept Anvil Input:", ConsoleLogger.Level.WARN);
                t.printStackTrace();
            }
        });
        return true;
    }

    /**
     * Asks the player for input in chat.
     * <p>
     * This method will register a listener for {@link AsyncPlayerChatEvent} and start waiting for the player to type in
     * something. If the player takes longer than 30 seconds (600 ticks), onInput consumer is accepted with an empty string
     * as the argument and the chat listener stops waiting for this player's input. When the player sends the message,
     * onInput consumer is accepted with the message as the argument.
     *
     * @param player  The player to ask input for.
     * @param onInput The consumer to accept once the player sends the input.
     * @throws IllegalStateException If EpicPluginLib is not loaded.
     */
    public static void askChatInput(@NotNull HumanEntity player, @NotNull Consumer<String> onInput)
    {
        askChatInput(player, 600, onInput);
    }

    /**
     * Asks the player for input in chat.
     * <p>
     * This method will register a listener for {@link AsyncPlayerChatEvent} and start waiting for the player to type in
     * something. If the player takes longer than the specified interval, onInput consumer is accepted with an empty string
     * as the argument and the chat listener stops waiting for this player's input. When the player sends the message,
     * onInput consumer is accepted with the message as the argument.
     *
     * @param player   The player to ask input for.
     * @param interval The interval to wait before stop waiting for input.
     * @param onInput  The consumer to accept once the player sends the input.
     * @throws IllegalStateException If EpicPluginLib is not loaded.
     */
    public static void askChatInput(@NotNull HumanEntity player, long interval, @NotNull Consumer<String> onInput)
    {
        askChatInput(player, interval, onInput, () -> {
            try {
                onInput.accept("");
            } catch (Throwable t) {
                EpicPluginLibBukkit.logger().log("Failed to accept Chat Input:", ConsoleLogger.Level.WARN);
                t.printStackTrace();
            }
        });
    }

    /**
     * Asks the player for input in chat.
     * <p>
     * This method will register a listener for {@link AsyncPlayerChatEvent} and start waiting for the player to type in
     * something. If the player takes longer than the specified interval, onTookTooLong runnable is executed and the chat
     * listener stops waiting for this player's input. When the player sends the message, onInput consumer is accepted with
     * the message as the argument.
     *
     * @param player        The player to ask input for.
     * @param interval      The interval to wait before stop waiting for input.
     * @param onInput       The consumer to accept once the player sends the input.
     * @param onTookTooLong The runnable to run if the player did not type in something in time.
     * @throws IllegalStateException If EpicPluginLib is not loaded.
     */
    public static void askChatInput(@NotNull HumanEntity player, long interval, @NotNull Consumer<String> onInput, @NotNull Runnable onTookTooLong)
    {
        EpicPluginLibBukkit plugin = EpicPluginLibBukkit.getInstance();
        if (plugin == null || !plugin.isEnabled()) throw new IllegalStateException("EpicPluginLib is not loaded.");

        player.closeInventory();

        UUID id = player.getUniqueId();

        // Registering chatListener if it was not registered before.
        if (inputListeningPlayers.isEmpty()) plugin.getServer().getPluginManager().registerEvents(chatListener, plugin);

        WaitingInput previous = inputListeningPlayers.put(id, new WaitingInput(onInput, onTookTooLong, plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            // Once the interval is done, chatListener should stop waiting for this player's input.
            inputListeningPlayers.remove(id);
            if (inputListeningPlayers.isEmpty()) HandlerList.unregisterAll(chatListener);

            try {
                onTookTooLong.run();
            } catch (Throwable t) {
                EpicPluginLibBukkit.logger().log("Failed to run 'took too long' runnable for Chat Input method:", ConsoleLogger.Level.WARN);
                t.printStackTrace();
            }
        }, interval)));

        // Cancelling previous waiting input and running the took too long runnable.
        if (previous != null) {
            previous.task.cancel();

            try {
                previous.tookTooLong.run();
            } catch (Throwable t) {
                EpicPluginLibBukkit.logger().log("Failed to run THE PREVIOUS 'took too long' runnable for Chat Input method:", ConsoleLogger.Level.WARN);
                t.printStackTrace();
            }
        }
    }

    private static final class WaitingInput
    {
        private final @NotNull Consumer<String> onInput;
        private final @NotNull BukkitTask task;
        private final @NotNull Runnable tookTooLong;

        private WaitingInput(@NotNull Consumer<String> onInput, @NotNull Runnable tookTooLong, @NotNull BukkitTask task)
        {
            this.onInput = onInput;
            this.tookTooLong = tookTooLong;
            this.task = task;
        }
    }
}
