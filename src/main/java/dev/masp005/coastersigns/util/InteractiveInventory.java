package dev.masp005.coastersigns.util;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import javax.annotation.Nullable;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import dev.masp005.coastersigns.CoasterSigns;

public class InteractiveInventory {
    public static InteractiveInventoryHandler handler;
    private static JavaPlugin plugin;
    private static int ROW_SIZE = 9;
    protected Inventory inventory;
    private InteractiveItem[] items;

    public InteractiveInventory(int rows) {
        items = new InteractiveItem[rows * ROW_SIZE];
    }

    public void open(Player holder, String name) {
        inventory = Bukkit.createInventory(holder, items.length, name);
        for (int i = 0; i < items.length; i++) {
            if (items[i] != null)
                inventory.setItem(i, items[i].toItemStack());
        }
        holder.openInventory(inventory);
        handler.inventories.put(holder, this);
    }

    /*
     * // TODO: clone
     * public InteractiveInventory clone() {
     * InteractiveInventory inventory;
     * 
     * }
     */

    public void handleClick(InventoryClickEvent event) {
        int slot = event.getRawSlot();
        InteractiveItem item = null;
        if (slot >= 0 && slot < items.length)
            item = items[event.getRawSlot()];
        if (item != null)
            item.handleClick(event);
        else
            event.setCancelled(true);
    }

    public InteractiveItem setItem(int slot, Material material) {
        InteractiveItem item = new InteractiveItem(new ItemStack(material), this);
        items[slot] = item;
        return item;
    }

    public InteractiveInventory setItemInstant(int slot, ItemStack material) {
        items[slot] = new InteractiveItem(material, this);
        return this;
    }

    public InteractiveInventory setItemInstant(int slot, ItemStack material, Consumer<InventoryClickEvent> listener) {
        items[slot] = new InteractiveItem(material, this);
        items[slot].setUniversalListener(listener);
        return this;
    }

    // CONSIDER: extends ItemStack
    public static class InteractiveItem {
        private InteractiveInventory inventory;
        private ItemStack itemStack;
        private Consumer<InventoryClickEvent> listener;

        protected InteractiveItem(ItemStack itemStack, InteractiveInventory inventory) {
            this.itemStack = itemStack;
            this.inventory = inventory;
            setUniversalListener(null);
        }

        public InteractiveInventory finish() {
            return inventory;
        }

        public InteractiveItem setUniversalListener(@Nullable Consumer<InventoryClickEvent> listener) {
            if (listener == null)
                listener = event -> event.setCancelled(true);
            this.listener = listener;
            return this;
        }

        public void handleClick(InventoryClickEvent event) {
            listener.accept(event);
            /*
             * // TODO: type-specific listeners (methods like onDrop or onSwap)
             * switch (event.getAction()) {
             * case CLONE_STACK:
             * case COLLECT_TO_CURSOR:
             * case DROP_ALL_SLOT:
             * case DROP_ONE_SLOT:
             * case PICKUP_ALL:
             * case PICKUP_HALF:
             * case PICKUP_ONE:
             * case PICKUP_SOME:
             * case PLACE_ALL:
             * case PLACE_ONE:
             * case PLACE_SOME:
             * case SWAP_WITH_CURSOR:
             * }
             */
        }

        protected ItemStack toItemStack() {
            return itemStack;
        }
    }

    public static class InteractiveInventoryHandler implements Listener {
        protected Map<Player, InteractiveInventory> inventories = new HashMap<>();

        public InteractiveInventoryHandler(CoasterSigns pl) {
            plugin = pl;
            Bukkit.getPluginManager().registerEvents(this, plugin);
        }

        @EventHandler
        public void onInventoryClick(InventoryClickEvent event) {
            InteractiveInventory inventory = inventories.get((Player) event.getWhoClicked());
            if (inventory != null)
                inventory.handleClick(event);
        }

        @EventHandler
        public void onInventoryClose(InventoryCloseEvent event) {
            inventories.remove((Player) event.getPlayer());
            // TODO: inventory-specific close handler?
        }
    }
}
