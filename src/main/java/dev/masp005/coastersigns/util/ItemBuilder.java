package dev.masp005.coastersigns.util;

import javax.annotation.Nullable;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * Item Builder Utility Class.
 */
public class ItemBuilder {
    ItemStack itemStack;
    ItemMeta itemMeta;

    public ItemBuilder(String material) {
        itemStack = new ItemStack(Material.getMaterial(material.toUpperCase()), 1);
        itemMeta = itemStack.getItemMeta();
    }

    public ItemBuilder(Material material) {
        itemStack = new ItemStack(material, 1);
        itemMeta = itemStack.getItemMeta();
    }

    public ItemBuilder(ItemStack is) {
        itemStack = is;
        itemMeta = itemStack.getItemMeta();
    }

    public ItemBuilder setCount(int count) {
        itemStack.setAmount(count);
        return this;
    }

    public ItemBuilder setCustomModelData(int data) {
        itemMeta.setCustomModelData(data);
        return this;
    }

    public ItemBuilder setGlint() {
        itemMeta.addEnchant(Enchantment.BINDING_CURSE, 0, true);
        itemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        return this;
    }

    public ItemBuilder setName(@Nullable String name, boolean italic) {
        itemMeta.setDisplayName((italic ? "" : "§r") + name);
        return this;
    }

    public ItemBuilder setName(@Nullable String name) {
        return setName(name, false);
    }

    public ItemBuilder setLore(@Nullable String lore) {
        if (lore == null)
            itemMeta.setLore(null);
        else
            itemMeta.setLore(Util.arrayToList(lore.split("\n")));
        return this;
    }

    public ItemStack build() {
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }
}