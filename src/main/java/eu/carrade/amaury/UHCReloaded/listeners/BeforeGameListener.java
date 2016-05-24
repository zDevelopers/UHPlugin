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
import eu.carrade.amaury.UHCReloaded.events.UHGameStartsEvent;
import eu.carrade.amaury.UHCReloaded.gui.teams.TeamsSelectorGUI;
import fr.zcraft.zlib.components.gui.Gui;
import fr.zcraft.zlib.components.gui.GuiUtils;
import fr.zcraft.zlib.components.i18n.I;
import fr.zcraft.zlib.core.ZLib;
import fr.zcraft.zlib.tools.items.ItemStackBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.Permissible;


public class BeforeGameListener implements Listener
{
    /**
     * @param player A player
     * @return True if an inventory action should not be done because they are a builder.
     */
    private boolean excludeBuilder(Permissible player)
    {
        return UHConfig.BEFORE_START.INVENTORY.ALLOW_FOR_BUILDERS.get() && player.hasPermission("uh.build");
    }

    /**
     * Opens the teams selector GUI, if needed (enabled, game not started, needed item).
     *
     * TODO improve selector item detection.
     *
     * @param player The player who right-clicked an item.
     * @param item The right-clicked item.
     */
    private void openTeamsGUI(Player player, ItemStack item)
    {
        if (UHCReloaded.get().getGameManager().isGameStarted()) return;

        if (UHConfig.BEFORE_START.TEAM_SELECTOR.ENABLED.get()
                && item != null
                && item.getType() == UHConfig.BEFORE_START.TEAM_SELECTOR.ITEM.get())
        {
            Gui.open(player, new TeamsSelectorGUI());
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent ev)
    {
        if (UHCReloaded.get().getGameManager().isGameStarted()) return;

        final boolean builder = excludeBuilder(ev.getPlayer());

        if (!builder && UHConfig.BEFORE_START.INVENTORY.CLEAR.get())
        {
            ev.getPlayer().getInventory().clear();
            ev.getPlayer().getInventory().setArmorContents(null);
        }

        if (UHConfig.BEFORE_START.TEAM_SELECTOR.ENABLED.get())
        {
            Material itemType = UHConfig.BEFORE_START.TEAM_SELECTOR.ITEM.get();

            ItemStack item = new ItemStackBuilder(itemType)
                    /// The title of the item given before the game to select a team
                    .title(I.t("{green}{bold}Select a team {gray}(Right-Click)"))
                    /// The lore of  the item given before the game to select a team
                    .lore(GuiUtils.generateLore(I.t("{gray}Right-click to select your team for this game")))
                    .hideAttributes()
                    .item();

            final ItemStack centralItem = ev.getPlayer().getInventory().getItem(4);
            if (!builder || centralItem == null || centralItem.getType() == org.bukkit.Material.AIR)
                ev.getPlayer().getInventory().setItem(4, item);
        }

        if (UHConfig.BEFORE_START.TEAM_IN_ACTION_BAR.get())
        {
            UHCReloaded.get().getTeamManager().displayTeamInActionBar(ev.getPlayer());
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerInteract(PlayerInteractEvent ev)
    {
        if (ev.getAction() != Action.PHYSICAL)
            openTeamsGUI(ev.getPlayer(), ev.getItem());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerInteractAtEntity(PlayerInteractAtEntityEvent ev)
    {
        openTeamsGUI(ev.getPlayer(), ev.getPlayer().getItemInHand());
    }

    @EventHandler
    public void onPlayerClick(InventoryClickEvent ev)
    {
        if (UHConfig.BEFORE_START.INVENTORY.PREVENT_USAGE.get())
        {
            if (UHCReloaded.get().getGameManager().isGameStarted()) return;
            if (excludeBuilder(ev.getWhoClicked())) return;
            if (!ev.getInventory().equals(ev.getWhoClicked().getInventory())) return;

            ev.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerDrag(InventoryDragEvent ev)
    {
        if (UHConfig.BEFORE_START.INVENTORY.PREVENT_USAGE.get())
        {
            if (UHCReloaded.get().getGameManager().isGameStarted()) return;
            if (excludeBuilder(ev.getWhoClicked())) return;
            if (!ev.getInventory().equals(ev.getWhoClicked().getInventory())) return;

            ev.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerDrop(PlayerDropItemEvent ev)
    {
        if (UHConfig.BEFORE_START.INVENTORY.PREVENT_USAGE.get())
        {
            if (UHCReloaded.get().getGameManager().isGameStarted()) return;
            if (excludeBuilder(ev.getPlayer())) return;

            ev.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerPickup(PlayerPickupItemEvent ev)
    {
        if (UHCReloaded.get().getGameManager().isGameStarted()) return;
        if (excludeBuilder(ev.getPlayer())) return;

        if (UHConfig.BEFORE_START.INVENTORY.PREVENT_USAGE.get())
        {
            ev.setCancelled(true);
        }
    }

    @EventHandler
    public void onGameStarts(UHGameStartsEvent ev)
    {
        ZLib.unregisterEvents(this);
    }
}
