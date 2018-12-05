/*
 * Copyright or © or Copr. Amaury Carrade (2014 - 2016)
 *
 * http://amaury.carrade.eu
 *
 * This software is governed by the CeCILL-B license under French law and
 * abiding by the rules of distribution of free software.  You can  use,
 * modify and/ or redistribute the software under the terms of the CeCILL-B
 * license as circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info".
 *
 * As a counterpart to the access to the source code and  rights to copy,
 * modify and redistribute granted by the license, users are provided only
 * with a limited warranty  and the software's author,  the holder of the
 * economic rights,  and the successive licensors  have only  limited
 * liability.
 *
 * In this respect, the user's attention is drawn to the risks associated
 * with loading,  using,  modifying and/or developing or reproducing the
 * software by the user in light of its specific status of free software,
 * that may mean  that it is complicated to manipulate,  and  that  also
 * therefore means  that it is reserved for developers  and  experienced
 * professionals having in-depth computer knowledge. Users are therefore
 * encouraged to load and test the software's suitability as regards their
 * requirements in conditions enabling the security of their systems and/or
 * data to be ensured and,  more generally, to use and operate it in the
 * same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL-B license and that you accept its terms.
 */

package eu.carrade.amaury.UHCReloaded.old.listeners;

import eu.carrade.amaury.UHCReloaded.UHCReloaded;
import eu.carrade.amaury.UHCReloaded.old.protips.ProTips;
import eu.carrade.amaury.UHCReloaded.old.recipes.RecipesManager;
import fr.zcraft.zlib.components.i18n.I;
import fr.zcraft.zlib.tools.runners.RunTask;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType.SlotType;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashSet;
import java.util.Set;


public class CraftingListener implements Listener
{
    private UHCReloaded p = null;

    public CraftingListener(UHCReloaded p)
    {
        this.p = p;
    }


    /**
     * Used to:
     *  - prevent items to be crafted;
     *  - send a ProTip containing the craft to use, if an error occurred;
     *  - add a lure to the golden apples crafted from a head;
     *  - keep the name of the item when the anti-lore craft is used.
     */
    @EventHandler
    public void onPreCraftEvent(PrepareItemCraftEvent ev)
    {
        Recipe recipe = ev.getRecipe();

        if (recipe == null)
        {
            return;
        }

        /* *** Prevents items to be crafted *** */

        if (!p.getRecipesManager().isRecipeAllowed(recipe))
        {
            ev.getInventory().setResult(new ItemStack(Material.AIR));

            // ProTips
            final String failedRecipe = p.getRecipesManager().getLastFailedRecipe();
            final Player player = (Player) ev.getViewers().get(0); // crafting inventory: only one viewer in all cases.

            RunTask.later(() ->
            {
                switch (failedRecipe)
                {
                    case RecipesManager.RECIPE_COMPASS:
                        switch (p.getRecipesManager().getCompassRecipeType())
                        {
                            case RecipesManager.COMPASS_EASY:
                                ProTips.CRAFT_COMPASS_EASY.sendTo(player);
                                break;
                            case RecipesManager.COMPASS_MEDIUM:
                                ProTips.CRAFT_COMPASS_MEDIUM.sendTo(player);
                                break;
                            case RecipesManager.COMPASS_HARD:
                                ProTips.CRAFT_COMPASS_HARD.sendTo(player);
                                break;
                        }

                        break;

                    case RecipesManager.RECIPE_GLISTERING_MELON:
                        ProTips.CRAFT_GLISTERING_MELON.sendTo(player);
                        break;

                    case RecipesManager.RECIPE_ENCHANTED_GOLDEN_APPLE:
                        ProTips.CRAFT_NO_ENCHANTED_GOLDEN_APPLE.sendTo(player);
                        break;
                }
            }, 40L);

            return;
        }


        /* *** Adds a lore to the golden apples crafted from a head *** */

        ItemStack loreResult = p.getRecipesManager().addLore(recipe, ev.getInventory());
        if (loreResult != null)
        {
            ev.getInventory().setResult(loreResult);
            return;
        }


        /* *** The lore remover don't change the name of the item *** */

        ItemStack keepNameResult = p.getRecipesManager().keepNameOnLoreRemover(recipe, ev.getInventory());
        if (keepNameResult != null)
        {
            ev.getInventory().setResult(keepNameResult);
            return;
        }
    }


    /**
     *   - Workaround to fix the crafting grid being not updated when the item is taken
     *     from the grid.
     *     <p>
     *   - Prevents an apple to be renamed to/from the name of an head apple.
     *
     *     (In vanilla clients, it is not possible to rename an apple to that name because of the
     *     ChatColor.RESET before, but some modded clients allows the player to write §r.)
     *
     *     (Thanks to Zelnehlun on BukkitDev.)
     *     <p>
     *   - Crafts the special compass (“semi-shapeless” recipe).
     */
    @EventHandler (ignoreCancelled = true)
    public void onInventoryClick(final InventoryClickEvent ev)
    {
        // Just in case
        if (ev.getWhoClicked() instanceof Player)
        {
            final Inventory inventory = ev.getInventory();

            // Workaround to fix the crafting grid being not updated when the item is taken
            // from the grid.
            if (inventory instanceof CraftingInventory && ev.getSlotType() == SlotType.RESULT)
            {
                RunTask.later(
                    () -> ev.getViewers().stream()
                            .filter(viewer -> viewer instanceof Player)
                            .forEach(viewer -> ((Player) viewer).updateInventory()),
                    1L
                );
            }


            /* *** Allows any shape for the loots in the compass recipe. *** */

            if (inventory instanceof CraftingInventory)
            {
                // This is ran one tick after the click because when the event is fired, the inventory
                // object is not updated, and so the result of the isValidCompassResult is invalid.

                RunTask.later(() ->
                {
                    if (p.getRecipesManager().isValidCompassRecipe(((CraftingInventory) inventory).getMatrix()))
                    {

                        // Puts the compass in the result slot
                        if (ev.getSlotType() == SlotType.CRAFTING)
                        {
                            ((CraftingInventory) inventory).setResult(new ItemStack(Material.COMPASS));
                            ev.setResult(Result.ALLOW);

                            ((Player) ev.getWhoClicked()).updateInventory(); // deprecated but needed
                        }

                        // Consumes the materials in the crafting grid.
                        // Because this is not an "official" recipe, we need to do that manually.
                        else if (ev.getSlotType() == SlotType.RESULT)
                        {
                            int index = 1;
                            for (ItemStack stack : ((CraftingInventory) inventory).getMatrix())
                            {
                                if (stack == null) continue;

                                if (stack.getAmount() != 1)
                                {
                                    stack.setAmount(stack.getAmount() - 1);
                                    inventory.setItem(index, stack);
                                }
                                else
                                {
                                    inventory.setItem(index, new ItemStack(Material.AIR));
                                }

                                index++;
                            }

                            ev.setCurrentItem(new ItemStack(Material.COMPASS));
                            ev.setResult(Result.ALLOW);

                            ((Player) ev.getWhoClicked()).updateInventory(); // deprecated but needed
                        }
                    }
                }, 1L);
            }


            /* *** Prevent an apple to be renamed to/from the name of an head apple. *** */

            else if (inventory instanceof AnvilInventory)
            {
                InventoryView view = ev.getView();
                int rawSlot = ev.getRawSlot();

                // ensure we are talking about the upper inventory
                if (rawSlot == view.convertSlot(rawSlot))
                {
                    // "result" slot
                    if (rawSlot == 2)
                    {
                        ItemStack item = ev.getCurrentItem();

                        // result slot non empty
                        if (item != null)
                        {
                            final ItemMeta meta = item.getItemMeta();

                            final Set<String> prohibited = new HashSet<>();

                            prohibited.add(I.t("Golden head"));
                            prohibited.add(ChatColor.RESET + I.t("{aqua}Golden head"));
                            prohibited.add(ChatColor.RESET + I.t("{lightpurple}Golden head"));

                            // It is possible that the client filters the name of the golden apple in the anvil UI,
                            // removing all §.
                            new HashSet<>(prohibited).stream()
                                    .map(prohibition -> prohibition.replace("§", ""))
                                    .forEach(prohibited::add);


                            // An item can't be renamed to the name of a golden head
                            if (meta != null && meta.hasDisplayName())
                            {
                                if (prohibited.contains(meta.getDisplayName()))
                                {
                                    ev.setCancelled(true); // nope nope nope
                                }
                            }

                            // A golden head can't be renamed to any other name
                            if (view.getItem(0) != null) // slot 0 = first slot
                            {
                                ItemMeta metaOriginal = view.getItem(0).getItemMeta();

                                if (metaOriginal != null && metaOriginal.hasDisplayName())
                                {
                                    if (prohibited.contains(metaOriginal.getDisplayName()))
                                    {
                                        ev.setCancelled(true);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }


    /**
     * Used to craft the special compass (“semi-shapeless” recipe).
     */
    @EventHandler (ignoreCancelled = true)
    public void onInventoryDrag(final InventoryDragEvent ev)
    {
        if (ev.getInventory() instanceof CraftingInventory)
        {
            RunTask.later(() ->
            {
                if (p.getRecipesManager().isValidCompassRecipe(((CraftingInventory) ev.getInventory()).getMatrix()))
                {
                    ((CraftingInventory) ev.getInventory()).setResult(new ItemStack(Material.COMPASS));
                    ((Player) ev.getWhoClicked()).updateInventory(); // deprecated but needed
                }
            }, 1L);
        }
    }


    /**
     * Adds the team banner on crafted shields.
     *
     * Done indirectly because the plugin must be able to run
     * on Minecraft 1.8.
     */
    @EventHandler (ignoreCancelled = true)
    public void onShieldPreCraft(PrepareItemCraftEvent ev)
    {
//        if (!UHConfig.TEAMS_OPTIONS.BANNER.SHIELDS.ADD_ON_SHIELDS.get()) return;
//
//        final Player player = (Player) ev.getViewers().get(0);
//        final UHTeam team = UHCReloaded.get().getTeamManager().getTeamForPlayer(player);
//
//        if (team == null || team.getBanner() == null) return;
//
//        final ItemStack result = ev.getRecipe().getResult();
//
//        final Material MATERIAL_SHIELD = Material.getMaterial("SHIELD");
//        if (MATERIAL_SHIELD == null) return; // MC 1.8
//
//        if (result != null && result.getType() == MATERIAL_SHIELD)
//        {
//            try
//            {
//                final BannerMeta banner = (BannerMeta) team.getBanner().getItemMeta();
//
//                final BlockStateMeta bsMeta = (BlockStateMeta) result.getItemMeta();
//                final Banner shieldBanner   = (Banner) bsMeta.getBlockState();
//
//                shieldBanner.setBaseColor(banner.getBaseColor());
//                shieldBanner.setPatterns(banner.getPatterns());
//
//                shieldBanner.update();
//
//                bsMeta.setBlockState(shieldBanner);
//                result.setItemMeta(bsMeta);
//
//                ev.getInventory().setResult(result);
//            }
//            catch (ClassCastException | NullPointerException ignored)
//            {
//                // Bad Minecraft version (1.8)
//            }
//        }
    }
}
