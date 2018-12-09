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
package eu.carrade.amaury.UHCReloaded.modules.core.teams.sidebar;

import eu.carrade.amaury.UHCReloaded.modules.core.game.GameModule;
import eu.carrade.amaury.UHCReloaded.shortcuts.UR;
import eu.carrade.amaury.UHCReloaded.utils.OfflinePlayersLoader;
import fr.zcraft.zlib.components.i18n.I;
import fr.zcraft.zteams.ZTeam;
import fr.zcraft.zteams.ZTeams;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;


/**
 * This class stores the data displayed in the sidebar for a player, like the color, the health, the name...
 */
public class SidebarPlayerCache
{
    private UUID playerID;

    private String playerName;
    private ChatColor healthColor = ChatColor.WHITE;

    private boolean isOnline;

    private final Set<UUID> metTeammates = new HashSet<>();


    public SidebarPlayerCache(final UUID id)
    {
        playerID = id;

        final OfflinePlayer player = Bukkit.getOfflinePlayer(id);

        if (player != null)
        {
            playerName = player.getName();
            isOnline = player.isOnline();

            if (isOnline)
                updateHealth(player.getPlayer().getHealth());
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
    }

    public void updateOnlineStatus(boolean isOnline)
    {
        this.isOnline = isOnline;
    }

    public UUID getPlayerID()
    {
        return playerID;
    }

    public String getPlayerName()
    {
        if (playerName != null && !playerName.isEmpty())
            return playerName;

        final OfflinePlayer player = OfflinePlayersLoader.getOfflinePlayer(playerID);
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

    public Set<OfflinePlayer> getMetTeammates()
    {
        return metTeammates.stream()
                .map(Bukkit::getOfflinePlayer)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    public void updateTeammatesDisplayed(final double distanceThreshold)
    {
        final Player player = Bukkit.getPlayer(playerID);
        if (player != null)
        {
            updateTeammatesDisplayed(
                    distanceThreshold * distanceThreshold,
                    player.getLocation(),
                    ZTeams.get().getTeamForPlayer(playerID)
            );
        }
    }

    public void updateTeammatesDisplayed(final double distanceThresholdSquared, final Location reference, final ZTeam team)
    {
        final GameModule game = UR.module(GameModule.class);

        // For each non-encountered online teammate, we check if they are close
        team.getOnlinePlayers().stream()
            .filter(p -> !metTeammates.contains(p.getUniqueId()))
            .filter(game::isAlive)
            .forEach(p -> {
                if (p.getWorld() == null) return;
                if (!p.getWorld().equals(reference.getWorld())) return;
                if (p.getLocation().distanceSquared(reference) > distanceThresholdSquared) return;

                // Close enough
                metTeammates.add(p.getUniqueId());
            });
    }
}
