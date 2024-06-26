package org.avarion.graves.manager;

import org.avarion.graves.Graves;
import org.avarion.graves.util.StringUtil;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public final class RecipeManager {

    private final Graves plugin;
    private final List<NamespacedKey> namespacedKeyList;

    public RecipeManager(Graves plugin) {
        this.plugin = plugin;
        this.namespacedKeyList = new ArrayList<>();
        reload();
    }

    public void reload() {
        unload();
        load();
    }

    public void load() {
        ConfigurationSection configurationSection = plugin.getConfig().getConfigurationSection("settings.token");

        if (configurationSection != null) {
            for (String key : configurationSection.getKeys(false)) {
                if (plugin.getConfig().getBoolean("settings.token." + key + ".craft")) {
                    addTokenRecipe(key, getToken(key));
                    plugin.debugMessage("Added recipe " + key, 1);
                }
            }
        }
    }

    public void unload() {
        Iterator<Recipe> iterator = plugin.getServer().recipeIterator();

        while (iterator.hasNext()) {
            Recipe recipe = iterator.next();

            if (recipe != null) {
                ItemStack itemStack = recipe.getResult();

                if (itemStack.hasItemMeta() && isToken(itemStack)) {
                    iterator.remove();
                }
            }
        }
    }

    public @Nullable ItemStack getToken(String token) {
        if (plugin.getConfig().isConfigurationSection("settings.token." + token)) {
            Material material = Material.matchMaterial(plugin.getConfig()
                                                             .getString("settings.token."
                                                                        + token
                                                                        + ".material", "SUNFLOWER"));
            ItemStack itemStack = new ItemStack(material != null ? material : Material.CHEST);

            setRecipeData(token, itemStack);

            if (itemStack.hasItemMeta()) {
                ItemMeta itemMeta = itemStack.getItemMeta();

                if (itemMeta != null) {
                    String name = ChatColor.WHITE + StringUtil.parseString(plugin.getConfig()
                                                                                 .getString("settings.token."
                                                                                            + token
                                                                                            + ".name"), plugin);
                    List<String> loreList = new ArrayList<>();
                    int customModelData = plugin.getConfig().getInt("settings.token." + token + ".model-data", -1);

                    for (String string : plugin.getConfig().getStringList("settings.token." + token + ".lore")) {
                        loreList.add(ChatColor.GRAY + StringUtil.parseString(string, plugin));
                    }

                    if (plugin.getConfig().getBoolean("settings.token." + token + ".glow")) {
                        itemMeta.addEnchant(Enchantment.DURABILITY, 1, true);
                        itemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                    }

                    if (customModelData > -1) {
                        itemMeta.setCustomModelData(customModelData);
                    }

                    itemMeta.setLore(loreList);
                    itemMeta.setDisplayName(name);
                    itemStack.setItemMeta(itemMeta);
                }
            }

            return itemStack;
        }

        return null;
    }

    public @NotNull List<String> getTokenList() {
        List<String> stringList = new ArrayList<>();
        ConfigurationSection configurationSection = plugin.getConfig().getConfigurationSection("settings.token");

        if (configurationSection != null) {
            stringList.addAll(configurationSection.getKeys(false));
        }

        return stringList;
    }

    public void addTokenRecipe(String token, ItemStack itemStack) {
        NamespacedKey namespacedKey = new NamespacedKey(plugin, token + "GraveToken");

        if (!namespacedKeyList.contains(namespacedKey)) {
            ShapedRecipe shapedRecipe = new ShapedRecipe(namespacedKey, itemStack);

            shapedRecipe.shape("ABC", "DEF", "GHI");

            List<String> lineList = plugin.getConfig().getStringList("settings.token." + token + ".recipe");
            int recipeKey = 1;

            for (String string : lineList.get(0).split(" ")) {
                Material material = Material.matchMaterial(string);

                if (material != null) {
                    shapedRecipe.setIngredient(getChar(recipeKey), material);
                }

                recipeKey++;
            }

            for (String string : lineList.get(1).split(" ")) {
                Material material = Material.matchMaterial(string);

                if (material != null) {
                    shapedRecipe.setIngredient(getChar(recipeKey), material);
                }

                recipeKey++;
            }

            for (String string : lineList.get(2).split(" ")) {
                Material material = Material.matchMaterial(string);

                if (material != null) {
                    shapedRecipe.setIngredient(getChar(recipeKey), material);
                }

                recipeKey++;
            }


            if (plugin.getServer().getRecipe(namespacedKey) == null) {
                plugin.getServer().addRecipe(shapedRecipe);
                namespacedKeyList.add(namespacedKey);
            }
            else {
                plugin.debugMessage("Unable to add recipe " + namespacedKey.getKey(), 1);
            }
        }
    }

    public @Nullable ItemStack getGraveTokenFromPlayer(String token, @NotNull List<ItemStack> itemStackList) {
        for (ItemStack itemStack : itemStackList) {
            if (itemStack != null && isToken(token, itemStack)) {
                return itemStack;
            }
        }

        return null;
    }

    public void setRecipeData(String token, @NotNull ItemStack itemStack) {
        ItemMeta itemMeta = itemStack.getItemMeta();

        if (itemMeta != null) {
            itemMeta.getPersistentDataContainer()
                    .set(new NamespacedKey(plugin, "token"), PersistentDataType.STRING, token);
            itemStack.setItemMeta(itemMeta);
        }
    }

    public boolean isToken(String token, @NotNull ItemStack itemStack) {
        if (itemStack.getItemMeta() != null && itemStack.getItemMeta()
                                                        .getPersistentDataContainer()
                                                        .has(new NamespacedKey(plugin, "token"), PersistentDataType.STRING)) {
            String string = itemStack.getItemMeta()
                                     .getPersistentDataContainer()
                                     .get(new NamespacedKey(plugin, "token"), PersistentDataType.STRING);

            return string != null && string.equals(token);
        }

        return false;
    }

    public @Nullable String getTokenName(@NotNull ItemStack itemStack) {
        if (itemStack.getItemMeta() != null && itemStack.getItemMeta()
                                                        .getPersistentDataContainer()
                                                        .has(new NamespacedKey(plugin, "token"), PersistentDataType.STRING)) {
            return itemStack.getItemMeta()
                            .getPersistentDataContainer()
                            .get(new NamespacedKey(plugin, "token"), PersistentDataType.STRING);
        }

        return null;
    }

    public boolean isToken(@NotNull ItemStack itemStack) {
        return itemStack.getItemMeta() != null && itemStack.getItemMeta()
                                                           .getPersistentDataContainer()
                                                           .has(new NamespacedKey(plugin, "token"), PersistentDataType.STRING);
    }

    private char getChar(int count) {
        return switch (count) {
            case 1 -> 'A';
            case 2 -> 'B';
            case 3 -> 'C';
            case 4 -> 'D';
            case 5 -> 'E';
            case 6 -> 'F';
            case 7 -> 'G';
            case 8 -> 'H';
            case 9 -> 'I';
            default -> '*';
        };
    }

}
