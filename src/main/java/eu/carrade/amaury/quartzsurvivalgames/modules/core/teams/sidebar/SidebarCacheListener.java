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
package eu.carrade.amaury.quartzsurvivalgames.modules.core.teams.sidebar;

import eu.carrade.amaury.quartzsurvivalgames.modules.core.game.GameModule;
import eu.carrade.amaury.quartzsurvivalgames.modules.core.game.events.players.AlivePlayerDeathEvent;
import eu.carrade.amaury.quartzsurvivalgames.modules.core.game.events.players.PlayerResurrectedEvent;
import eu.carrade.amaury.quartzsurvivalgames.modules.core.teams.TeamsModule;
import eu.carrade.amaury.quartzsurvivalgames.shortcuts.QSG;
import fr.zcraft.quartzlib.tools.runners.RunTask;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;


public class SidebarCacheListener implements Listener
{
    private final static GameModule game = QSG.module(GameModule.class);
    private final static TeamsModule teams = QSG.module(TeamsModule.class);

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent ev)
    {
        final SidebarPlayerCache cache = teams.getSidebarPlayerCache(ev.getPlayer().getUniqueId());

        cache.updateName(ev.getPlayer().getName());
        cache.updateOnlineStatus(true);

        // To be sure (and if the player was killed/resurrected while offline)
        onPlayerHealthChange(ev.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent ev)
    {
        onPlayerQuit(ev.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerKickEvent ev)
    {
        onPlayerQuit(ev.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerRegainHealth(EntityRegainHealthEvent ev)
    {
        if (ev.getEntity() instanceof Player)
            onPlayerHealthChange((Player) ev.getEntity());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerLoseHealth(EntityDamageEvent ev)
    {
        if (ev.getEntity() instanceof Player)
            onPlayerHealthChange((Player) ev.getEntity());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerDeath(AlivePlayerDeathEvent ev)
    {
        if (ev.getPlayer().isOnline())
            onPlayerHealthChange(ev.getPlayer().getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerResurrect(PlayerResurrectedEvent ev)
    {
        if (ev.getPlayer().isOnline())
            onPlayerHealthChange(ev.getPlayer().getPlayer());
    }

    private void onPlayerQuit(Player player)
    {
        teams.getSidebarPlayerCache(player.getUniqueId()).updateOnlineStatus(false);
    }

    private void onPlayerHealthChange(final Player player)
    {
        // One tick later to use the updated health value.
        RunTask.nextTick(() -> {
            final SidebarPlayerCache cache = teams.getSidebarPlayerCache(player.getUniqueId());
            cache.updateHealth(game.isAlive(player.getUniqueId()) ? player.getHealth() : 0d);
        });
    }
}
