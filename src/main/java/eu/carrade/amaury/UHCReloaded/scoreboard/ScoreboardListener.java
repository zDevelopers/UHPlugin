/*
 * Copyright or © or Copr. AmauryCarrade (2015)
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
package eu.carrade.amaury.UHCReloaded.scoreboard;

import eu.carrade.amaury.UHCReloaded.UHCReloaded;
import eu.carrade.amaury.UHCReloaded.events.UHPlayerDeathEvent;
import eu.carrade.amaury.UHCReloaded.events.UHPlayerResurrectedEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;


public class ScoreboardListener implements Listener
{
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent ev)
    {
        SidebarPlayerCache cache = UHCReloaded.get().getScoreboardManager().getSidebarPlayerCache(ev.getPlayer().getUniqueId());

        cache.updateName(ev.getPlayer().getName());
        cache.updateOnlineStatus(true);
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
        if(ev.getEntity() instanceof Player)
            onPlayerHealthChange((Player) ev.getEntity());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerLoseHealth(EntityDamageEvent ev)
    {
        if(ev.getEntity() instanceof Player)
            onPlayerHealthChange((Player) ev.getEntity());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerDeath(UHPlayerDeathEvent ev)
    {
        onPlayerHealthChange(ev.getPlayer());

        if (ev.getPlayerDeathEvent().getEntity().getKiller() != null)
            UHCReloaded.get().getScoreboardManager()
                    .getSidebarPlayerCache(ev.getPlayerDeathEvent().getEntity().getKiller().getUniqueId())
                    .getPlayersKilled().add(ev.getPlayer().getUniqueId());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerResurrect(UHPlayerResurrectedEvent ev)
    {
        onPlayerHealthChange(ev.getPlayer());

        for (SidebarPlayerCache cache : UHCReloaded.get().getScoreboardManager().getAllSidebarPlayerCache().values())
        {
            if(cache.getPlayersKilled().remove(ev.getPlayer().getUniqueId()))
                break;
        }
    }

    private void onPlayerQuit(Player player)
    {
        UHCReloaded.get().getScoreboardManager().getSidebarPlayerCache(player.getUniqueId()).updateOnlineStatus(false);
    }

    private void onPlayerHealthChange(final Player player)
    {
        // One tick later to use the updated health value.
        Bukkit.getScheduler().runTaskLater(UHCReloaded.get(), new Runnable() {
            @Override
            public void run()
            {
                SidebarPlayerCache cache = UHCReloaded.get().getScoreboardManager().getSidebarPlayerCache(player.getUniqueId());

                if(UHCReloaded.get().getGameManager().isPlayerDead(player.getUniqueId()))
                    cache.updateHealth(0d);

                else
                    cache.updateHealth(player.getHealth());
            }
        }, 1l);
    }
}
