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

package eu.carrade.amaury.UHCReloaded.listeners;

import eu.carrade.amaury.UHCReloaded.UHCReloaded;
import eu.carrade.amaury.UHCReloaded.UHConfig;
import eu.carrade.amaury.UHCReloaded.task.CancelBrewTask;
import fr.zcraft.zlib.components.i18n.I;
import fr.zcraft.zlib.tools.runners.RunTask;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Ghast;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.bukkit.inventory.BrewerInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.List;


public class GameplayListener implements Listener
{
    private final UHCReloaded p;

    public GameplayListener()
    {
        this.p = UHCReloaded.get();
    }


    /**
     * Used to replace ghast tears with gold (if needed).
     */
    @EventHandler (ignoreCancelled = true)
    public void onEntityDeath(EntityDeathEvent ev)
    {
        if (ev.getEntity() instanceof Ghast && UHConfig.GAMEPLAY_CHANGES.REPLACE_GHAST_TEARS_WITH_GOLD.get())
        {
            final List<ItemStack> drops = new ArrayList<ItemStack>(ev.getDrops());
            ev.getDrops().clear();

            for (final ItemStack i : drops)
            {
                if (i.getType() == Material.GHAST_TEAR)
                {
                    ev.getDrops().add(new ItemStack(Material.GOLD_INGOT, i.getAmount()));
                }
                else
                {
                    ev.getDrops().add(i);
                }
            }
        }
    }

    /**
     * Used to prevent the user to get a ghast tear, if forbidden by the config.
     */
    @EventHandler (ignoreCancelled = true)
    public void onPlayerPickupItem(PlayerPickupItemEvent ev)
    {
        if (ev.getItem().getItemStack().getType() == Material.GHAST_TEAR
                && ev.getPlayer().getGameMode().equals(GameMode.SURVIVAL)
                && UHConfig.GAMEPLAY_CHANGES.REPLACE_GHAST_TEARS_WITH_GOLD.get())
        {
            ev.setCancelled(true);
        }
    }


    /**
     * Used to disable power-II potions.
     */
    @EventHandler
    public void onInventoryDrag(InventoryDragEvent ev)
    {
        if (UHConfig.GAMEPLAY_CHANGES.DISABLE_LEVEL_II_POTIONS.get() && ev.getInventory() instanceof BrewerInventory)
        {
            RunTask.later(new CancelBrewTask((BrewerInventory) ev.getInventory(), ev.getWhoClicked()), 1L);
        }
    }

    /**
     * Used to disable power-II potions.
     */
    @EventHandler
    public void onInventoryClick(InventoryClickEvent ev)
    {
        if (UHConfig.GAMEPLAY_CHANGES.DISABLE_LEVEL_II_POTIONS.get() && ev.getInventory() instanceof BrewerInventory)
        {
           RunTask.later(new CancelBrewTask((BrewerInventory) ev.getInventory(), ev.getWhoClicked()), 1L);
        }
    }


    /**
     * Used to disable ender pearl damages (if needed).
     */
    @EventHandler (ignoreCancelled = true)
    public void onPlayerTeleport(final PlayerTeleportEvent ev)
    {
        if (UHConfig.GAMEPLAY_CHANGES.DISABLE_ENDERPEARLS_DAMAGES.get())
        {
            if (ev.getCause() == TeleportCause.ENDER_PEARL)
            {
                ev.setCancelled(true);
                ev.getPlayer().teleport(ev.getTo(), TeleportCause.PLUGIN); // Technically its an ender pearl teleportation, but
                                                                           // if we use that, an infinite loop will occur due to
                                                                           // the event being re-captured and re-emitted.
            }
        }
    }


    /**
     * Used to disable witch spawn (if needed).
     */
    @EventHandler
    public void onCreatureSpawn(CreatureSpawnEvent ev)
    {
        if (ev.getEntityType().equals(EntityType.WITCH))
        {
            if (UHConfig.GAMEPLAY_CHANGES.WITCH.DISABLE_NATURAL_SPAWN.get() && ev.getSpawnReason().equals(SpawnReason.NATURAL))
            {
                ev.setCancelled(true);
            }
            if (UHConfig.GAMEPLAY_CHANGES.WITCH.DISABLE_LIGHTNING_SPAWN.get() && ev.getSpawnReason().equals(SpawnReason.LIGHTNING))
            {
                ev.setCancelled(true);
            }
        }
    }


    /**
     * Used to change the amount of regenerated hearts from a golden apple.
     */
    @EventHandler
    public void onPlayerItemConsume(final PlayerItemConsumeEvent ev)
    {
        final int TICKS_BETWEEN_EACH_REGENERATION = 50;
        final int DEFAULT_NUMBER_OF_HEARTS_REGEN = 4;
        final int DEFAULT_NUMBER_OF_HEARTS_REGEN_NOTCH = 180;
        final int REGENERATION_LEVEL_GOLDEN_APPLE = 2;
        final int REGENERATION_LEVEL_NOTCH_GOLDEN_APPLE = 5;

        if (ev.getItem().getType() == Material.GOLDEN_APPLE)
        {
            ItemMeta meta = ev.getItem().getItemMeta();
            short dataValue = ev.getItem().getDurability();
            int halfHearts;
            int level;

            if (meta.hasDisplayName()
                    && (meta.getDisplayName().equals(ChatColor.RESET + I.t("{aqua}Golden head"))
                    || meta.getDisplayName().equals(ChatColor.RESET + I.t("{lightpurple}Golden head"))))
            {
                // Normal golden apple from a head
                if (dataValue == 0)
                {
                    halfHearts = UHConfig.GAMEPLAY_CHANGES.GOLDEN_APPLE.REGENERATION.FROM_NORMAL_HEAD.get();
                    level = REGENERATION_LEVEL_GOLDEN_APPLE;
                }
                // Notch golden apple from a head
                else
                {
                    halfHearts = UHConfig.GAMEPLAY_CHANGES.GOLDEN_APPLE.REGENERATION.FROM_NOTCH_HEAD.get();
                    level = REGENERATION_LEVEL_NOTCH_GOLDEN_APPLE;
                }
            }
            // Normal golden apple from an apple
            else if (dataValue == 0)
            {
                halfHearts = UHConfig.GAMEPLAY_CHANGES.GOLDEN_APPLE.REGENERATION.NORMAL.get();
                level = REGENERATION_LEVEL_GOLDEN_APPLE;
            }
            // Notch golden apple from an apple
            else
            {
                halfHearts = UHConfig.GAMEPLAY_CHANGES.GOLDEN_APPLE.REGENERATION.NOTCH.get();
                level = REGENERATION_LEVEL_NOTCH_GOLDEN_APPLE;
            }

            // Technically, a level-I effect is « level 0 ».
            final int realLevel = level - 1;


            // What is needed to do?
            if ((dataValue == 0 && halfHearts == DEFAULT_NUMBER_OF_HEARTS_REGEN)
                    || (dataValue == 1 && halfHearts == DEFAULT_NUMBER_OF_HEARTS_REGEN_NOTCH))
            {
                // Default behavior, nothing to do.
            }
            else if ((dataValue == 0 && halfHearts > DEFAULT_NUMBER_OF_HEARTS_REGEN)
                    || (dataValue == 1 && halfHearts > DEFAULT_NUMBER_OF_HEARTS_REGEN_NOTCH))
            {
                // If the heal needs to be increased, the effect can be applied immediately.

                int duration = ((int) Math.floor(TICKS_BETWEEN_EACH_REGENERATION / (Math.pow(2, realLevel)))) * halfHearts;

                new PotionEffect(PotionEffectType.REGENERATION, duration, realLevel).apply(ev.getPlayer());
            }
            else
            {
                // The heal needs to be decreased.
                // We can't apply the effect immediately, because the server will just ignore it.
                // So, we apply it two ticks later, with one half-heart less (because in two ticks,
                // one half-heart is given to the player).
                final int healthApplied = halfHearts - 1;

                RunTask.later(() ->
                {
                    // The original, vanilla, effect is removed
                    ev.getPlayer().removePotionEffect(PotionEffectType.REGENERATION);

                    int duration = ((int) Math.floor(TICKS_BETWEEN_EACH_REGENERATION / (Math.pow(2, realLevel)))) * healthApplied;
                    new PotionEffect(PotionEffectType.REGENERATION, duration, realLevel).apply(ev.getPlayer());
                }, 2L);
            }
        }
    }


    /**
     * Used to update the compass.
     */
    @SuppressWarnings ("deprecation")
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent ev)
    {
        if ((ev.getAction() == Action.RIGHT_CLICK_AIR || ev.getAction() == Action.RIGHT_CLICK_BLOCK)
                && ev.getPlayer().getItemInHand().getType() == Material.COMPASS
                && p.getConfig().getBoolean("gameplay-changes.compass.enabled")
                && !p.getGameManager().isPlayerDead(ev.getPlayer()))
        {
            Player player1 = ev.getPlayer();

            boolean foundRottenFlesh = false;
            for (ItemStack item : player1.getInventory().getContents())
            {
                if (item != null && item.getType() == Material.ROTTEN_FLESH)
                {
                    if (item.getAmount() != 1)
                    {
                        item.setAmount(item.getAmount() - 1);
                    }
                    else
                    {
                        player1.getInventory().removeItem(item);
                    }

                    player1.updateInventory();
                    foundRottenFlesh = true;
                    break;
                }
            }

            if (!foundRottenFlesh)
            {
                /// Error message if a player tries to use his pointing compass without rotten flesh.
                player1.sendMessage(I.t("{gray}{italic}You do not have rotten flesh."));
                player1.playSound(player1.getLocation(), Sound.STEP_WOOD, 1F, 1F);
                return;
            }

            Player nearest = null;
            Double distance = 99999D;
            for (Player player2 : p.getGameManager().getOnlineAlivePlayers())
            {
                try
                {
                    Double calc = player1.getLocation().distanceSquared(player2.getLocation());

                    if (calc > 1 && calc < distance)
                    {
                        distance = calc;
                        if (!player2.getUniqueId().equals(player1.getUniqueId()) && !p.getTeamManager().inSameTeam(player1, player2))
                        {
                            nearest = player2.getPlayer();
                        }
                    }
                }
                catch (Exception ignored)
                {

                }
            }

            if (nearest == null)
            {
                /// Error message if a player tries to use his pointing compass without a player nearby.
                player1.sendMessage(I.t("{gray}{italic}Only silence answers your request."));

                player1.playSound(player1.getLocation(), Sound.STEP_WOOD, 1F, 1F);
                return;
            }

            /// Success message when a player uses his pointing compass.
            player1.sendMessage(I.t("{gray}The compass now points to the closest player."));
            player1.setCompassTarget(nearest.getLocation());

            player1.playSound(player1.getLocation(), Sound.ENDERMAN_TELEPORT, 1F, 1F);
        }
    }


    /**
     * Used to disable the "bad" weather (aka non-clear weather).
     * The weather is initially clear.
     */
    @EventHandler
    public void onWeatherChange(WeatherChangeEvent ev)
    {
        if (!UHConfig.GAMEPLAY_CHANGES.WEATHER.get())
        {
            ev.setCancelled(true);
        }
    }
}
