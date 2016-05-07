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
package eu.carrade.amaury.UHCReloaded.scoreboard;

import eu.carrade.amaury.UHCReloaded.misc.OfflinePlayersLoader;
import fr.zcraft.zlib.components.i18n.I;
import fr.zcraft.zlib.components.scoreboard.Sidebar;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArraySet;


/**
 * This class stores the data displayed in the sidebar for a player, like the color, the health, the name...
 */
public class SidebarPlayerCache
{
    private UUID playerId;

    private String playerName;
    private ChatColor healthColor = ChatColor.WHITE;

    private boolean isOnline;
    private boolean isAlive;

    private Set<UUID> playersKilled      = new CopyOnWriteArraySet<>();
    private Set<UUID> teammatesDisplayed = new CopyOnWriteArraySet<>();


    public SidebarPlayerCache(UUID id)
    {
        playerId = id;

        Player player = Sidebar.getPlayerAsync(id);

        if(player != null)
        {
            playerName = player.getName();
            isOnline = true;
            updateHealth(player.getHealth());
        }
        else
        {
            playerName = null;
            isOnline = false;
        }
    }

    public void updateName(String name)
    {
        playerName = name;
    }

    public void updateHealth(double health)
    {
        if (health <= 0)
            healthColor = ChatColor.GRAY;
        else if (health <= 4.1)
            healthColor = ChatColor.DARK_RED;
        else if (health <= 8.1)
            healthColor = ChatColor.RED;
        else if (health <= 12.1)
            healthColor = ChatColor.YELLOW;
        else if (health <= 16.1)
            healthColor = ChatColor.GREEN;
        else
            healthColor = ChatColor.DARK_GREEN;

        isAlive = (health > 0);
    }

    public void updateOnlineStatus(boolean isOnline)
    {
        this.isOnline = isOnline;
    }

    public void addKill(UUID id)
    {
        playersKilled.add(id);
    }

    public UUID getPlayerId()
    {
        return playerId;
    }

    public String getPlayerName()
    {
        if (playerName != null && !playerName.isEmpty())
            return playerName;

        OfflinePlayer player = OfflinePlayersLoader.getOfflinePlayer(playerId);
        if (player != null && player.getName() != null && !player.getName().isEmpty())
        {
            playerName = player.getName();
            return playerName;
        }

        /// Default nick name when a player cannot be recognized.
        return I.t("Unknown");
    }

    public ChatColor getHealthColor()
    {
        return healthColor;
    }

    public boolean isOnline()
    {
        return isOnline;
    }

    public boolean isAlive()
    {
        return isAlive;
    }

    public Set<UUID> getTeammatesDisplayed()
    {
        return teammatesDisplayed;
    }

    public Set<UUID> getPlayersKilled()
    {
        return playersKilled;
    }
}
