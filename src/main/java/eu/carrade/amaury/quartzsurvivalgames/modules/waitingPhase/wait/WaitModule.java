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
package eu.carrade.amaury.quartzsurvivalgames.modules.waitingPhase.wait;

import eu.carrade.amaury.quartzsurvivalgames.core.ModuleCategory;
import eu.carrade.amaury.quartzsurvivalgames.core.ModuleInfo;
import eu.carrade.amaury.quartzsurvivalgames.core.QSGModule;
import eu.carrade.amaury.quartzsurvivalgames.modules.core.game.GameModule;
import eu.carrade.amaury.quartzsurvivalgames.modules.core.game.GamePhase;
import eu.carrade.amaury.quartzsurvivalgames.modules.core.game.events.game.GamePhaseChangedEvent;
import eu.carrade.amaury.quartzsurvivalgames.modules.core.modules.gui.MainConfigGUI;
import eu.carrade.amaury.quartzsurvivalgames.shortcuts.QSG;
import eu.carrade.amaury.quartzsurvivalgames.utils.EntitiesUtils;
import eu.carrade.amaury.quartzsurvivalgames.utils.QSGUtils;
import fr.zcraft.quartzlib.components.attributes.Attribute;
import fr.zcraft.quartzlib.components.attributes.Attributes;
import fr.zcraft.quartzlib.components.gui.Gui;
import fr.zcraft.quartzlib.components.i18n.I;
import fr.zcraft.quartzlib.core.QuartzLib;
import fr.zcraft.quartzlib.tools.PluginLogger;
import fr.zcraft.quartzlib.tools.items.ItemStackBuilder;
import fr.zcraft.quartzlib.tools.reflection.NMSException;
import fr.zcraft.quartzlib.tools.runners.RunTask;
import fr.zcraft.quartzlib.tools.text.ActionBar;
import fr.zcraft.quartzteams.QuartzTeam;
import fr.zcraft.quartzteams.QuartzTeams;
import fr.zcraft.quartzteams.events.PlayerJoinedTeamEvent;
import fr.zcraft.quartzteams.events.PlayerLeftTeamEvent;
import fr.zcraft.quartzteams.events.TeamUnregisteredEvent;
import fr.zcraft.quartzteams.events.TeamUpdatedEvent;
import fr.zcraft.quartzteams.guis.TeamsSelectorGUI;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.Permissible;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


@ModuleInfo (
        name = "Waiting phase",
        description = "Manages the waiting phase: inventory, effects, modes, teleportation, etc.",
        category = ModuleCategory.WAITING_PHASE,
        icon = Material.CLOCK,
        settings = Config.class,
        can_be_loaded_late = false
)
public class WaitModule extends QSGModule
{
    private final static UUID ACTIONABLE_ITEM_UUID = UUID.fromString("3f65c4a9-e1ae-437e-b8b4-57d80a831480");

    private final Map<UUID, String> playerInventoriesStates = new HashMap<>();
    private BukkitTask inventoriesUpdateTask = null;

    @Override
    protected void onEnable()
    {
        Bukkit.getOnlinePlayers().forEach(this::handleNewPlayer);

        inventoriesUpdateTask = RunTask.timer(() -> Bukkit.getOnlinePlayers().forEach(this::updateInventory), 20L, 20L);
    }

    @Override
    protected void onDisable()
    {
        if (inventoriesUpdateTask != null)
        {
            inventoriesUpdateTask.cancel();
            inventoriesUpdateTask = null;
        }
    }

    /**
     * Writes an action into an Item Stack.
     *
     * @param item The item. This must be a CraftItemStack.
     * @param action The action to store.
     * @see #readAction(ItemStack) to read a previously stored action.
     */
    private void writeAction(final ItemStack item, final String action)
    {
        final Attribute attribute = new Attribute();

        attribute.setUUID(ACTIONABLE_ITEM_UUID);
        attribute.setCustomData(action);

        try
        {
            Attributes.set(item, attribute);
        }
        catch (final NMSException e)
        {
            PluginLogger.error("Unable to store item action into attribute. Inventory tools won't work before the game.");
        }
    }

    /**
     * Reads an action previously written into this ItemStack.
     * @param item The item.
     * @return The action, or en empty string if nothing stored.
     * @see #writeAction(ItemStack, String) to write an action.
     */
    private String readAction(final ItemStack item)
    {
        try
        {
            final Attribute attribute = Attributes.get(item, ACTIONABLE_ITEM_UUID);
            return attribute != null && attribute.getCustomData() != null ? attribute.getCustomData() : "";
        }
        catch (NMSException ignored) {
            return "";
        }
    }

    /**
     * Opens the teams selector GUI, if needed (enabled, game not started, needed item).
     *
     * @param player The player who right-clicked an item.
     * @param item The right-clicked item.
     */
    private boolean openGUI(Player player, ItemStack item)
    {
        if (isGameStarted() || item == null) return false;

        switch (readAction(item))
        {
            case "teams":
                Gui.open(player, new TeamsSelectorGUI());
                return true;

            case "config":
                if (player.isOp()) // TODO add permissions
                {
                    Gui.open(player, new MainConfigGUI());
                    return true;
                }
                else return false;
        }

        return false;
    }

    /**
     * Sets the state of a player joining the game while the waiting phase is in progress.
     *
     * @param player The player to setup.
     */
    private void handleNewPlayer(final Player player)
    {
        if (Config.TELEPORT_TO_SPAWN_IF_NOT_STARTED.get() && QSG.game().getPhase() != GamePhase.STARTING)
        {
            final Location worldSpawn = QSG
                    .get().getWorld(World.Environment.NORMAL).getSpawnLocation().add(0.5, 0.5, 0.5);
            if (!QSGUtils.safeTP(player, worldSpawn))
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

        updateInventory(player);

        player.getInventory().setHeldItemSlot(4);

        displayTeamInActionBar(player);
    }

    /**
     * Update the player inventory and game mode, if its state changed since last update.
     *
     * @param player The player to update.
     */
    private void updateInventory(final Player player)
    {
        final boolean builder = isBuilder(player);
        final boolean teamsDisplayed = Config.TEAM_SELECTOR.ENABLED.get();
        final boolean configDisplayed = Config.CONFIG_ACCESSOR.ENABLED.get() && player.isOp();

        // Only updates the inventory when the access state change.
        final String state = String.format("%b%b%b", builder, teamsDisplayed, configDisplayed);
        if (playerInventoriesStates.containsKey(player.getUniqueId()) && playerInventoriesStates.get(player.getUniqueId()).equals(state))
        {
            return;
        }

        playerInventoriesStates.put(player.getUniqueId(), state);

        player.setGameMode(builder ? GameMode.CREATIVE : GameMode.ADVENTURE);

        if (!builder && Config.INVENTORY.CLEAR.get())
        {
            player.getInventory().clear();
            player.getInventory().setArmorContents(null);
        }

        if (Config.TEAM_SELECTOR.ENABLED.get() || Config.CONFIG_ACCESSOR.ENABLED.get())
        {
            final ItemStack teamsSelector = new ItemStackBuilder(Config.TEAM_SELECTOR.ITEM.get())
                    /// The title of the item given before the game to select a team
                    .title(I.t("{green}{bold}Select a team {gray}(Right-Click)"))
                    /// The lore of  the item given before the game to select a team
                    .longLore(I.t("{gray}Right-click to select your team for this game"))
                    .hideAttributes()
                    .craftItem();

            final ItemStack configAccessor = new ItemStackBuilder(Config.CONFIG_ACCESSOR.ITEM.get())
                    .title(I.t("{red}{bold}Configure the game {gray}(Right-Click)"))
                    .longLore(I.t("{gray}Right-click to open the game configuration GUI"))
                    .hideAttributes()
                    .craftItem();

            writeAction(teamsSelector, "teams");
            writeAction(configAccessor, "config");

            final int teamsSlot = configDisplayed ? 2 : 4;
            final int configSlot = teamsDisplayed ? 6 : 4;

            clearIfSimilar(player, teamsSelector, 2);
            clearIfSimilar(player, teamsSelector, 4);
            clearIfSimilar(player, configAccessor, 4);
            clearIfSimilar(player, configAccessor, 6);

            if (teamsDisplayed)
            {
                placeIfPossible(player, teamsSelector, teamsSlot);
            }

            if (configDisplayed)
            {
                placeIfPossible(player, configAccessor, configSlot);
            }
        }
    }

    private void placeIfPossible(final Player player, final ItemStack item, final int slot)
    {
        final ItemStack previousItem = player.getInventory().getItem(slot);
        if (!isBuilder(player) || previousItem == null || previousItem.getType() == org.bukkit.Material.AIR)
        {
            player.getInventory().setItem(slot, item);
        }
    }

    private void clearIfSimilar(final Player player, final ItemStack ifSimilarTo, final int slot)
    {
        final ItemStack previousItem = player.getInventory().getItem(slot);
        if (previousItem != null && previousItem.getType() == ifSimilarTo.getType())
        {
            player.getInventory().setItem(slot, new ItemStack(Material.AIR));
        }
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

        final QuartzTeam team = QuartzTeams.get().getTeamForPlayer(player);

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
        if (ev.getAction() != Action.PHYSICAL && openGUI(ev.getPlayer(), ev.getItem()))
        {
            ev.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerInteractAtEntity(PlayerInteractAtEntityEvent ev)
    {
        if (openGUI(ev.getPlayer(), ev.getPlayer().getItemInHand()))
        {
            ev.setCancelled(true);
        }
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
                    || (QSG.module(GameModule.class).getPhase() == GamePhase.WAIT && !Config.ENABLE_PVP.get()))
            {
                ev.setCancelled(true);
            }
        }
    }

    /**
     * Used to cancel the spawn of the creatures if the game is not started.
     * <p>
     * We don't use the peaceful difficulty for that because it causes bugs with Minecraft 1.8
     * (the difficulty is not correctly updated client-side when the game starts).
     */
    @EventHandler
    public void onCreatureSpawn(CreatureSpawnEvent ev)
    {
        if (!isGameStarted()
                && EntitiesUtils.isNaturalSpawn(ev.getSpawnReason())
                && EntitiesUtils.isHostile(ev.getEntityType()))
        {
            ev.setCancelled(true);
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
                QuartzLib.unregisterEvents(this);
                if (inventoriesUpdateTask != null)
                {
                    inventoriesUpdateTask.cancel();
                    inventoriesUpdateTask = null;
                }
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
        final GamePhase phase = QSG.module(GameModule.class).getPhase();
        return phase != GamePhase.WAIT && phase != GamePhase.STARTING;
    }
}
