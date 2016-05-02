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
package eu.carrade.amaury.UHCReloaded.teams;

import eu.carrade.amaury.UHCReloaded.UHCReloaded;
import eu.carrade.amaury.UHCReloaded.UHConfig;
import fr.zcraft.zlib.components.i18n.I;
import fr.zcraft.zlib.tools.text.RawMessage;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;


public class TeamManager
{
    private final int MAX_PLAYERS_PER_TEAM;

    private final UHCReloaded p;
    private final HashSet<UHTeam> teams = new HashSet<>();


    public TeamManager(UHCReloaded plugin)
    {
        p = plugin;

        MAX_PLAYERS_PER_TEAM = UHConfig.TEAMS_OPTIONS.MAX_PLAYERS_PER_TEAM.get();
    }

    /**
     * Is the given team registered?
     *
     * @param team The team.
     * @return {@code true} if the team is registered.
     */
    public boolean isTeamRegistered(UHTeam team)
    {
        return teams.contains(team);
    }

    /**
     * Is the given team registered?
     *
     * @param name The name of the team.
     * @return {@code true} if the team is registered.
     */
    public boolean isTeamRegistered(String name)
    {
        return getTeam(name) != null;
    }


    /**
     * Adds a team.
     *
     * @param color The color.
     * @param name The name of the team.
     *
     * @return The new team.
     *
     * @throws IllegalArgumentException if a team with the same name already exists.
     */
    public UHTeam addTeam(TeamColor color, String name)
    {
        if (isTeamRegistered(name))
        {
            throw new IllegalArgumentException("There is already a team named " + name + " registered!");
        }

        UHTeam team = new UHTeam(name, generateColor(color), p);
        teams.add(team);

        return team;
    }

    /**
     * Adds a team. A name is generated based on the color.
     *
     * @param color The color.
     *
     * @return The new team.
     *
     * @throws IllegalArgumentException if a team with the same name already exists.
     */
    public UHTeam addTeam(TeamColor color)
    {

        color = generateColor(color);
        String teamName = color.toString().toLowerCase();

        if (isTeamRegistered(teamName))
        { // Taken!
            Random rand = new Random();
            do
            {
                teamName = color.toString().toLowerCase() + rand.nextInt(1000);
            } while (isTeamRegistered(teamName));
        }

        UHTeam team = new UHTeam(teamName, color, p);
        teams.add(team);

        return team;
    }

    /**
     * Adds a team from an UHTeam object.
     *
     * @param team The team.
     * @return The new team.
     *
     * @throws IllegalArgumentException if a team with the same name already exists.
     */
    public UHTeam addTeam(UHTeam team)
    {
        if (isTeamRegistered(team))
        {
            throw new IllegalArgumentException("There is already a team named " + team.getName() + " registered!");
        }

        teams.add(team);
        return team;
    }

    /**
     * Deletes a team.
     *
     * @param team The team to delete.
     * @param dontNotify If true, the player will not be notified about the leave.
     * @return boolean True if a team was removed.
     */
    public boolean removeTeam(UHTeam team, boolean dontNotify)
    {
        if (team != null)
        {
            if (dontNotify)
            {
                for (OfflinePlayer player : team.getPlayers())
                {
                    this.removePlayerFromTeam(player, true);
                }
            }

            team.deleteTeam();
        }

        return teams.remove(team);
    }

    /**
     * Deletes a team.
     *
     * @param team The team to delete.
     * @return boolean True if a team was removed.
     */
    public boolean removeTeam(UHTeam team)
    {
        return removeTeam(team, false);
    }

    /**
     * Deletes a team.
     *
     * @param name The name of the team to delete.
     * @return boolean True if a team was removed.
     */
    public boolean removeTeam(String name)
    {
        return removeTeam(getTeam(name), false);
    }

    /**
     * Deletes a team.
     *
     * @param name The name of the team to delete.
     * @param dontNotify If true, the player will not be notified about the leave.
     * @return boolean True if a team was removed.
     */
    public boolean removeTeam(String name, boolean dontNotify)
    {
        return removeTeam(getTeam(name), dontNotify);
    }

    /**
     * Adds a player to a team.
     *
     * @param teamName The team in which we adds the player.
     * @param player The player to add.
     * @throws IllegalArgumentException if the team does not exists.
     */
    public void addPlayerToTeam(String teamName, OfflinePlayer player)
    {
        UHTeam team = getTeam(teamName);

        if (team == null)
        {
            throw new IllegalArgumentException("There isn't any team named" + teamName + " registered!");
        }

        team.addPlayer(player);
    }

    /**
     * Removes a player from his team.
     *
     * @param player The player to remove.
     * @param dontNotify If true, the player will not be notified about the leave.
     */
    public void removePlayerFromTeam(OfflinePlayer player, boolean dontNotify)
    {
        UHTeam team = getTeamForPlayer(player);
        if (team != null)
        {
            team.removePlayer(player, dontNotify);
        }
    }

    /**
     * Removes a player from his team.
     *
     * @param player The player to remove.
     */
    public void removePlayerFromTeam(OfflinePlayer player)
    {
        removePlayerFromTeam(player, false);
    }


    /**
     * Removes all teams.
     *
     * @param dontNotify If true, the player will not be notified when they leave the destroyed team.
     */
    public void reset(boolean dontNotify)
    {
        // 1: scoreboard reset
        for (UHTeam team : new HashSet<>(teams))
        {
            this.removeTeam(team, dontNotify);
        }

        // 2: internal list reset
        teams.clear();
    }

    /**
     * Removes all teams.
     */
    public void reset()
    {
        reset(false);
    }

    /**
     * Sets the correct display name of a player, according to his team.
     *
     * @param offlinePlayer The player to colorize.
     */
    public void colorizePlayer(OfflinePlayer offlinePlayer)
    {
        if (!UHConfig.COLORIZE_CHAT.get())
        {
            return;
        }

        if (!offlinePlayer.isOnline())
        {
            return;
        }

        Player player = (Player) offlinePlayer;

        UHTeam team = getTeamForPlayer(player);

        if (team == null)
        {
            player.setDisplayName(player.getName());
        }
        else
        {
            if (team.getColor() != null)
            {
                player.setDisplayName(team.getColor().toChatColor() + player.getName() + ChatColor.RESET);
            }
            else
            {
                player.setDisplayName(player.getName());
            }
        }
    }

    /**
     * Returns all the teams.
     *
     * @return The teams.
     */
    public Set<UHTeam> getTeams()
    {
        return teams;
    }

    /**
     * Returns the maximal number of players in each team.
     *
     * @return The max.
     */
    public int getMaxPlayersPerTeam()
    {
        return MAX_PLAYERS_PER_TEAM;
    }

    /**
     * Returns the UHTeam object of the team with the given name.
     *
     * @param name The name of the team.
     * @return The team, or null if the team does not exists.
     */
    public UHTeam getTeam(String name)
    {
        for (UHTeam t : teams)
        {
            if (t.getName().equalsIgnoreCase(name))
            {
                return t;
            }
        }

        return null;
    }

    /**
     * Gets a player's team.
     *
     * @param player The player.
     * @return The team of this player.
     */
    public UHTeam getTeamForPlayer(OfflinePlayer player)
    {
        for (UHTeam t : teams)
        {
            if (t.containsPlayer(player.getUniqueId())) return t;
        }

        return null;
    }

    /**
     * Checks if two players are in the same team.
     *
     * @param player1 The first player.
     * @param player2 The second player
     * @return True if the players are in the same team, false else.
     */
    public boolean inSameTeam(Player player1, Player player2)
    {
        return (getTeamForPlayer(player1).equals(getTeamForPlayer(player2)));
    }

    /**
     * Generates a color from the given color.
     * <p>
     * If the color is neither {@link TeamColor#RANDOM} nor {@code null}, returns the given color.<br />
     * Else, generates a random unused (if possible) color.
     * @param color
     * @return
     */
    public TeamColor generateColor(TeamColor color)
    {
        if (color != null && color != TeamColor.RANDOM)
        {
            return color;
        }

        // A list of the currently used colors.
        HashSet<TeamColor> availableColors = new HashSet<TeamColor>(Arrays.asList(TeamColor.values()));
        availableColors.remove(TeamColor.RANDOM);
        for (UHTeam team : getTeams())
        {
            availableColors.remove(team.getColor());
        }

        if (availableColors.size() != 0)
        {
            return (TeamColor) availableColors.toArray()[(new Random()).nextInt(availableColors.size())];
        }
        else
        {
            // length-1 so the RANDOM option is never selected.
            return TeamColor.values()[(new Random()).nextInt(TeamColor.values().length - 1)];
        }
    }

    /**
     * Imports the teams from the configuration.
     *
     * @return The number of teams imported.
     */
    public int importTeamsFromConfig()
    {
        if (UHConfig.TEAMS.get() != null)
        {
            int teamsCount = 0;
            for (String teamRaw : UHConfig.TEAMS.get())
            {
                if (teamRaw != null)
                {
                    String[] teamRawSeparated = teamRaw.split(",");
                    TeamColor color = TeamColor.fromString(teamRawSeparated[0]);
                    if (color == null)
                    {
                        p.getLogger().warning(I.t("Invalid team set in config: {0}", (String) teamRaw));
                    }
                    else
                    {
                        // "color,name"
                        if (teamRawSeparated.length == 2)
                        {
                            UHTeam newTeam = addTeam(color, teamRawSeparated[1]);
                            p.getLogger().info(I.t("Team {0} ({1}) added from the config file", newTeam.getName(), newTeam.getColor().toString()));
                            teamsCount++;
                        }

                        // "color"
                        else if (teamRawSeparated.length == 1)
                        {
                            UHTeam newTeam = addTeam(color, teamRawSeparated[0]);
                            p.getLogger().info(I.t("Team {0} added from the config file", newTeam.getColor().toString()));
                            teamsCount++;
                        }
                        else
                        {
                            p.getLogger().warning(I.t("Invalid team set in config: {0}", (String) teamRaw));
                        }
                    }
                }
            }

            return teamsCount;
        }

        return 0;
    }

    /**
     * Displays a chat-based GUI (using tellraw formatting) to player to select a team.
     * <p>
     * Nothing is displayed if the player cannot use the /join command.
     *
     * @param player The receiver of the chat-GUI.
     */
    public void displayTeamChooserChatGUI(Player player)
    {
        if (!player.hasPermission("uh.player.join.self")) return;

        if (p.getGameManager().isGameRunning())
        {
            if (!p.getGameManager().isGameWithTeams())
            {
                return;
            }
        }

        player.sendMessage(ChatColor.GRAY + "⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅");

        if (p.getTeamManager().getTeams().size() != 0)
        {
            /// Invite displayed in the chat team selector
            player.sendMessage(I.t("{gold}Click on the names below to join a team"));

            boolean displayPlayers = UHConfig.TEAMS_OPTIONS.GUI.DISPLAY_PLAYERS_IN_TEAMS.get();

            for (UHTeam team : p.getTeamManager().getTeams())
            {

                String text = "{\"text\":\"\",\"extra\":[";

                // Team count (something like "[2/5]”)
                text += "{";
                if (MAX_PLAYERS_PER_TEAM != 0)
                {
                    /// Team count with max players (ex. [3/5]) followed in-game by the team name. {0} = current count, {1} = max.
                    text += "\"text\": \"" + I.t("{gray}[{white}{0}{gray}/{white}{1}{gray}]", String.valueOf(team.getSize()), String.valueOf(MAX_PLAYERS_PER_TEAM)) + "\", ";
                }
                else
                {
                    /// Team count without max players (ex. [3]) followed in-game by the team name. {0} = current count.
                    text += "\"text\": \"" + I.t("{gray}[{white}{0}{gray}]", String.valueOf(team.getSize())) + "\", ";
                }

                String players = "";
                if (displayPlayers)
                {
                    String bullet = "\n - ";
                    for (OfflinePlayer opl : team.getPlayers())
                    {
                        if (!p.getGameManager().isGameRunning())
                        {
                            players += bullet + opl.getName();
                        }
                        else
                        {
                            if (p.getGameManager().isPlayerDead(opl.getUniqueId()))
                            {
                                /// Displayed in team tooltip of the chat team selector for a dead player
                                players += bullet + I.t("{0} ({red}dead{reset})", opl.getName());
                            }
                            else
                            {
                                /// Displayed in team tooltip of the chat team selector for an alive player
                                players += bullet + I.t("{0} ({green}alive{reset})", opl.getName());
                            }
                        }
                    }
                }
                text += "\"hoverEvent\":{\"action\":\"show_text\",\"value\":\"" + I.tn("{0} player in this team", "{0} players in this team", team.getPlayers().size(), team.getPlayers().size()) + players + "\"}";
                text += "},";

                text += "{\"text\":\" \"},{";

                // Team name (click event is here)
                text += "\"text\":\"" + team.getName() + "\",";
                text += "\"color\":\"" + team.getColor().toString().toLowerCase() + "\",";
                text += "\"clickEvent\":{\"action\":\"run_command\",\"value\":\"/join " + team.getName() + "\"},";
                if (team.containsPlayer(player))
                {
                    text += "\"bold\":\"true\",";
                    /// Tooltip on the chat team selector GUI when the player is in the team. {0} = team display name.
                    text += "\"hoverEvent\":{\"action\":\"show_text\",\"value\":\"" + I.t("You are in the team {0}", team.getDisplayName()) + "\"}";
                }
                else
                {
                    /// Tooltip on the chat team selector GUI when the player is not in the team. {0} = team display name.
                    text += "\"hoverEvent\":{\"action\":\"show_text\",\"value\":\"" + I.t("Click here to join the team {0}", team.getDisplayName()) + "\"}";
                }
                text += "}";

                text += "]}";

                RawMessage.send(player, text);
            }

            if (p.getTeamManager().getTeamForPlayer(player) != null && player.hasPermission("uh.player.leave.self"))
            {
                String text = "{";
                text += "\"text\":\"" + I.t("{darkred}[×] {red}Click here to leave your team") + "\",";
                text += "\"clickEvent\":{\"action\":\"run_command\",\"value\":\"/leave\"}";
                text += "}";

                RawMessage.send(player, text);
            }
            else
            {
                player.sendMessage(I.t("{gray}Run /join to display this again"));
            }
        }
        else
        {
            // No teams.
            player.sendMessage(I.t("{ce}There isn't any team available."));
        }

        player.sendMessage(ChatColor.GRAY + "⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅ ⋅");
    }
}
