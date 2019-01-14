/*
 * Plugin UHCReloaded : Alliances
 *
 * Copyright ou © ou Copr. Amaury Carrade (2016)
 * Idées et réflexions : Alexandre Prokopowicz, Amaury Carrade, "Vayan".
 *
 * Ce logiciel est régi par la licence CeCILL soumise au droit français et
 * respectant les principes de diffusion des logiciels libres. Vous pouvez
 * utiliser, modifier et/ou redistribuer ce programme sous les conditions
 * de la licence CeCILL telle que diffusée par le CEA, le CNRS et l'INRIA
 * sur le site "http://www.cecill.info".
 *
 * En contrepartie de l'accessibilité au code source et des droits de copie,
 * de modification et de redistribution accordés par cette licence, il n'est
 * offert aux utilisateurs qu'une garantie limitée.  Pour les mêmes raisons,
 * seule une responsabilité restreinte pèse sur l'auteur du programme,  le
 * titulaire des droits patrimoniaux et les concédants successifs.
 *
 * A cet égard  l'attention de l'utilisateur est attirée sur les risques
 * associés au chargement,  à l'utilisation,  à la modification et/ou au
 * développement et à la reproduction du logiciel par l'utilisateur étant
 * donné sa spécificité de logiciel libre, qui peut le rendre complexe à
 * manipuler et qui le réserve donc à des développeurs et des professionnels
 * avertis possédant  des  connaissances  informatiques approfondies.  Les
 * utilisateurs sont donc invités à charger  et  tester  l'adéquation  du
 * logiciel à leurs besoins dans des conditions permettant d'assurer la
 * sécurité de leurs systèmes et ou de leurs données et, plus généralement,
 * à l'utiliser et l'exploiter dans les mêmes conditions de sécurité.
 *
 * Le fait que vous puissiez accéder à cet en-tête signifie que vous avez
 * pris connaissance de la licence CeCILL, et que vous en avez accepté les
 * termes.
 */
package eu.carrade.amaury.UHCReloaded.modules.beginning.wait;

import eu.carrade.amaury.UHCReloaded.core.ModuleCategory;
import eu.carrade.amaury.UHCReloaded.core.ModuleInfo;
import eu.carrade.amaury.UHCReloaded.core.UHModule;
import eu.carrade.amaury.UHCReloaded.modules.core.game.GameModule;
import eu.carrade.amaury.UHCReloaded.modules.core.game.GamePhase;
import eu.carrade.amaury.UHCReloaded.modules.core.game.events.game.GamePhaseChangedEvent;
import eu.carrade.amaury.UHCReloaded.shortcuts.UR;
import eu.carrade.amaury.UHCReloaded.utils.UHUtils;
import fr.zcraft.zlib.components.gui.Gui;
import fr.zcraft.zlib.components.gui.GuiUtils;
import fr.zcraft.zlib.components.i18n.I;
import fr.zcraft.zlib.core.ZLib;
import fr.zcraft.zlib.tools.items.ItemStackBuilder;
import fr.zcraft.zlib.tools.text.ActionBar;
import fr.zcraft.zteams.ZTeam;
import fr.zcraft.zteams.ZTeams;
import fr.zcraft.zteams.events.PlayerJoinedTeamEvent;
import fr.zcraft.zteams.events.PlayerLeftTeamEvent;
import fr.zcraft.zteams.events.TeamUnregisteredEvent;
import fr.zcraft.zteams.events.TeamUpdatedEvent;
import fr.zcraft.zteams.guis.TeamsSelectorGUI;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.Permissible;


@ModuleInfo (
        name = "Waiting phase",
        description = "Manages the waiting phase: inventory, effects, modes, teleportation, etc.",
        category = ModuleCategory.WAITING_PHASE,
        icon = Material.WATCH,
        settings = Config.class
)
public class WaitModule extends UHModule
{
    @Override
    protected void onEnable()
    {
        Bukkit.getOnlinePlayers().forEach(this::handleNewPlayer);
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
        if (isGameStarted()) return;

        if (Config.TEAM_SELECTOR.ENABLED.get()
                && item != null
                && item.getType() == Config.TEAM_SELECTOR.ITEM.get())
        {
            Gui.open(player, new TeamsSelectorGUI());
        }
    }

    /**
     * Sets the state of a player joining the game while the waiting phase is in progress.
     *
     * @param player The player to setup.
     */
    private void handleNewPlayer(final Player player)
    {
        final boolean builder = isBuilder(player);

        if (Config.TELEPORT_TO_SPAWN_IF_NOT_STARTED.get())
        {
            final Location worldSpawn = UR.get().getWorld(World.Environment.NORMAL).getSpawnLocation().add(0.5, 0.5, 0.5);
            if (!UHUtils.safeTP(player, worldSpawn))
            {
                player.teleport(worldSpawn.add(0, 1, 0));
            }

            if (Config.ENABLE_PVP.get())
            {
                player.setBedSpawnLocation(worldSpawn, true);
            }
        }

        player.setHealth(player.getMaxHealth());
        player.setFoodLevel(20);
        player.setSaturation(20f);

        player.setGameMode(builder ? GameMode.CREATIVE : GameMode.ADVENTURE);

        if (!builder && Config.INVENTORY.CLEAR.get())
        {
            player.getInventory().clear();
            player.getInventory().setArmorContents(null);
        }

        if (Config.TEAM_SELECTOR.ENABLED.get())
        {
            Material itemType = Config.TEAM_SELECTOR.ITEM.get();

            ItemStack item = new ItemStackBuilder(itemType)
                    /// The title of the item given before the game to select a team
                    .title(I.t("{green}{bold}Select a team {gray}(Right-Click)"))
                    /// The lore of  the item given before the game to select a team
                    .lore(GuiUtils.generateLore(I.t("{gray}Right-click to select your team for this game")))
                    .hideAttributes()
                    .item();

            final ItemStack centralItem = player.getInventory().getItem(4);
            if (!builder || centralItem == null || centralItem.getType() == org.bukkit.Material.AIR)
                player.getInventory().setItem(4, item);
        }

        displayTeamInActionBar(player);
    }

    /**
     * If the configuration option is enabled, displays its team in the player's
     * action bar. If the player is not in a team, the action bar is cleared.
     *
     * @param player The player.
     */
    private void displayTeamInActionBar(final OfflinePlayer player)
    {
        if (!Config.TEAM_IN_ACTION_BAR.get() || player == null) return;

        final Player onlinePlayer;

        if (player instanceof Player) onlinePlayer = (Player) player;
        else onlinePlayer = Bukkit.getPlayer(player.getUniqueId());

        if (onlinePlayer == null) return;

        final ZTeam team = ZTeams.get().getTeamForPlayer(player);

        if (team != null)
            ActionBar.sendPermanentMessage(onlinePlayer, I.t("{gold}Your team: {0}", team.getDisplayName()));
        else
            ActionBar.removeMessage(onlinePlayer, true);
    }

    @EventHandler
    public void onPlayerJoin(final PlayerJoinEvent ev)
    {
        if (isGameStarted()) return;

        handleNewPlayer(ev.getPlayer());
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
        if (Config.INVENTORY.PREVENT_USAGE.get())
        {
            if (isGameStarted()) return;
            if (isBuilder(ev.getWhoClicked())) return;
            if (!ev.getInventory().equals(ev.getWhoClicked().getInventory())) return;

            ev.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerDrag(InventoryDragEvent ev)
    {
        if (Config.INVENTORY.PREVENT_USAGE.get())
        {
            if (isGameStarted()) return;
            if (isBuilder(ev.getWhoClicked())) return;
            if (!ev.getInventory().equals(ev.getWhoClicked().getInventory())) return;

            ev.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerDrop(PlayerDropItemEvent ev)
    {
        if (Config.INVENTORY.PREVENT_USAGE.get())
        {
            if (isGameStarted()) return;
            if (isBuilder(ev.getPlayer())) return;

            ev.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerPickup(PlayerPickupItemEvent ev)
    {
        if (isGameStarted()) return;
        if (isBuilder(ev.getPlayer())) return;

        if (Config.INVENTORY.PREVENT_USAGE.get())
        {
            ev.setCancelled(true);
        }
    }

    /**
     * Used to disable all damages if the game is not started.
     */
    @EventHandler
    public void onEntityDamage(final EntityDamageEvent ev)
    {
        if (ev.getEntity() instanceof Player)
        {
            if (!isGameStarted()
                    || (UR.module(GameModule.class).getPhase() == GamePhase.WAIT && !Config.ENABLE_PVP.get()))
            {
                ev.setCancelled(true);
            }
        }
    }

    /**
     * Used to prevent the food level from dropping if the game has not started.
     */
    @EventHandler
    public void onFoodUpdate(final FoodLevelChangeEvent ev)
    {
        if (isGameStarted()) return;

        if (ev.getEntity() instanceof Player)
        {
            ((Player) ev.getEntity()).setFoodLevel(20);
            ((Player) ev.getEntity()).setSaturation(20f);
        }

        ev.setCancelled(true);
    }


    /**
     * Used to display the team in the action bar (if needed).
     */
    @EventHandler
    public void onTeamJoin(final PlayerJoinedTeamEvent ev)
    {
        displayTeamInActionBar(ev.getPlayer());
    }

    /**
     * Used to display the team in the action bar (if needed).
     */
    @EventHandler
    public void onTeamUpdated(final TeamUpdatedEvent ev)
    {
        ev.getTeam().getOnlinePlayers().forEach(this::displayTeamInActionBar);
    }

    /**
     * Used to display the team in the action bar (if needed).
     */
    @EventHandler
    public void onTeamDeleted(final TeamUnregisteredEvent ev)
    {
        ev.getTeam().getOnlinePlayers().forEach(this::displayTeamInActionBar);
    }

    /**
     * Used to display the team in the action bar (if needed).
     */
    @EventHandler
    public void onTeamLeft(final PlayerLeftTeamEvent ev)
    {
        displayTeamInActionBar(ev.getPlayer());
    }


    /**
     * The action bar messages are removed when the starting phase starts.
     * This listener will self-disable when the game starts.
     */
    @EventHandler
    public void onGameStarts(final GamePhaseChangedEvent ev)
    {
        switch (ev.getNewPhase())
        {
            case STARTING:
                Bukkit.getOnlinePlayers().forEach(player -> {
                    ActionBar.removeMessage(player);

                    player.getInventory().clear();
                    player.getInventory().setArmorContents(null);

                    player.closeInventory();
                });
                break;

            case IN_GAME:
                ZLib.unregisterEvents(this);
                break;
        }
    }


    /**
     * @param player A player
     * @return True if an inventory action should not be done because he is a builder.
     */
    private boolean isBuilder(final Permissible player)
    {
        return Config.INVENTORY.ALLOW_FOR_BUILDERS.get() && player.hasPermission("uh.build");
    }

    /**
     * @return If we are in the right game phase (wait).
     */
    private boolean isGameStarted()
    {
        final GamePhase phase = UR.module(GameModule.class).getPhase();
        return phase != GamePhase.WAIT && phase != GamePhase.STARTING;
    }
}
