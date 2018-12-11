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
package eu.carrade.amaury.UHCReloaded.modules.core.spectators;

import eu.carrade.amaury.UHCReloaded.core.ModuleInfo;
import eu.carrade.amaury.UHCReloaded.core.UHModule;
import eu.carrade.amaury.UHCReloaded.modules.core.game.GameModule;
import eu.carrade.amaury.UHCReloaded.modules.core.game.GamePhase;
import eu.carrade.amaury.UHCReloaded.modules.core.game.events.game.GamePhaseChangedEvent;
import eu.carrade.amaury.UHCReloaded.modules.core.game.events.players.AlivePlayerDeathEvent;
import eu.carrade.amaury.UHCReloaded.modules.core.game.events.players.PlayerResurrectedEvent;
import eu.carrade.amaury.UHCReloaded.modules.core.spectators.commands.SpectatorsCommand;
import eu.carrade.amaury.UHCReloaded.modules.core.spectators.managers.SpectatorsManager;
import eu.carrade.amaury.UHCReloaded.shortcuts.UR;
import fr.zcraft.zlib.components.commands.Command;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.*;


@ModuleInfo (
        name = "Spectators manager",
        description = "Handles non-playing players",
        when = ModuleInfo.ModuleLoadTime.POST_WORLD,
        can_be_disabled = false
)
public class SpectatorsModule extends UHModule
{
    private SpectatorsManager manager;

    /**
     * Lists players allowed to spectate. Also used for initial spectators: players who will
     * never play, only spectate.
     */
    private final Set<UUID> spectators = new HashSet<>();

    @Override
    protected void onEnable()
    {
        manager = SpectatorsManager.getInstance();
    }

    @Override
    public List<Class<? extends Command>> getCommands()
    {
        return Collections.singletonList(SpectatorsCommand.class);
    }

    /**
     * @return The manager instance to use to handle spectators.
     */
    public SpectatorsManager getManager()
    {
        return manager;
    }

    /**
     * @return The allowed spectators.
     */
    public Set<UUID> getSpectators()
    {
        return spectators;
    }

    public boolean isSpectator(final UUID playerID)
    {
        return spectators.contains(playerID);
    }

    public boolean isSpectator(final OfflinePlayer player)
    {
        return spectators.contains(player.getUniqueId());
    }

    /**
     * Adds a spectator.
     *
     * @param playerID The spectator's ID.
     */
    public void addSpectator(final UUID playerID)
    {
        spectators.add(playerID);

        if (UR.module(GameModule.class).getPhase() != GamePhase.WAIT)
            manager.setSpectating(playerID, true);
    }

    /**
     * Adds a spectator.
     *
     * @param player The spectator.
     */
    public void addSpectator(final OfflinePlayer player)
    {
        addSpectator(player.getUniqueId());
    }

    /**
     * Removes a spectator.
     *
     * @param playerID The spectator's ID.
     */
    public void removeSpectator(final UUID playerID)
    {
        spectators.remove(playerID);

        if (UR.module(GameModule.class).getPhase() != GamePhase.WAIT)
            manager.setSpectating(playerID, false);
    }

    /**
     * Removes a spectator.
     *
     * @param player The spectator.
     */
    public void removeSpectator(final OfflinePlayer player)
    {
        removeSpectator(player.getUniqueId());
    }

    /**
     * Ensures all players are in specator mode.
     *
     * @param strict If true, all spectating players not in our list are removed from the spectator mode.
     *               Else, only players in our list are placed into spectator mode.
     */
    private void ensureSpectatorMode(final boolean strict)
    {
        if (strict)
        {
            Bukkit.getOnlinePlayers().forEach(player -> manager.setSpectating(player, isSpectator(player)));
        }
        else
        {
            spectators.forEach(spectator -> manager.setSpectating(spectator, true));
        }
    }

    @EventHandler
    public void onGameStart(final GamePhaseChangedEvent ev)
    {
        if (ev.getNewPhase() != GamePhase.IN_GAME) return;

        ensureSpectatorMode(true);
    }

    @EventHandler (priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerDeath(final AlivePlayerDeathEvent ev)
    {
        addSpectator(ev.getPlayer());
    }


    @EventHandler (priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerResurrects(final PlayerResurrectedEvent ev)
    {
        removeSpectator(ev.getPlayer());
    }

    @EventHandler
    public void onPlayerJoin(final PlayerJoinEvent ev)
    {
        if (UR.module(GameModule.class).getPhase().ordinal() >= GamePhase.IN_GAME.ordinal())
        {
            manager.setSpectating(ev.getPlayer(), isSpectator(ev.getPlayer()));
        }
    }
}
