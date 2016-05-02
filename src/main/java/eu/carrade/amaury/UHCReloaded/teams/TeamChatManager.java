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
import eu.carrade.amaury.UHCReloaded.protips.ProTips;
import fr.zcraft.zlib.components.i18n.I;
import fr.zcraft.zlib.tools.runners.RunTask;
import fr.zcraft.zlib.tools.text.MessageSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;


public class TeamChatManager
{
    private final UHCReloaded p;

    private final Set<UUID> teamChatLocked = new HashSet<>();
    private final Map<UUID, UHTeam> otherTeamChatLocked = new HashMap<>();
    private final Set<UUID> globalSpies = new HashSet<>();

    public TeamChatManager(UHCReloaded p)
    {
        this.p = p;
    }

    /**
     * Sends a team-message from the given sender.
     *
     * @param sender The sender.
     * @param message The message to send.
     */
    public void sendTeamMessage(Player sender, String message)
    {
        sendTeamMessage(sender, message, null);
    }

    /**
     * Sends a team-message from the given sender.
     *
     * @param sender The sender.
     * @param message The message to send.
     * @param team If not null, this message will be considered as an external message from another player to this team.
     */
    public void sendTeamMessage(Player sender, String message, UHTeam team)
    {
        // Permission check
        if (team == null && !sender.hasPermission("uh.teamchat.self"))
        {
            sender.sendMessage(I.t("{ce}You are not allowed to send a private message to your team."));
            return;
        }
        if (team != null && !sender.hasPermission("uh.teamchat.others"))
        {
            sender.sendMessage(I.t("{ce}You are not allowed to enter in the private chat of another team."));
            return;
        }

        String rawMessage;
        UHTeam recipient;

        if (team == null)
        {
            /// Format of a private team message from a team member. {0} = sender display name, {1} = message.
            rawMessage = I.t("{gold}[{0}{gold} -> his team] {reset}{1}", sender.getDisplayName(), message);
            recipient = p.getTeamManager().getTeamForPlayer(sender);

            if (recipient == null)
            {
                /// Error message if someone try to send a team private message out of any team
                sender.sendMessage(I.t("{ce}You are not in a team!"));
                return;
            }
        }
        else
        {
            /// Format of a private team message from a non-team-member. {0} = sender display name, {1} = team display name, {2} = message.
            rawMessage = I.t("{gold}[{0}{gold} -> team {1}{gold}] {reset}{2}", sender.getDisplayName(), team.getDisplayName(), message);
            recipient = team;
        }

        sendRawTeamMessage(sender, rawMessage, recipient);
    }

    /**
     * Sends a raw team-message from the given player.
     *
     * @param sender The sender of this message.
     * @param rawMessage The raw message to be sent.
     * @param team The recipient of this message.
     */
    private void sendRawTeamMessage(final Player sender, String rawMessage, UHTeam team)
    {
        // The message is sent to the players of the team...
        for (final Player player : team.getOnlinePlayers())
        {
            MessageSender.sendChatMessage(player, rawMessage);
        }

        // ... to the spies ...
        if (otherTeamChatLocked.containsValue(team))
        {
            for (UUID playerId : otherTeamChatLocked.keySet())
            {
                // The message is only sent to the spies not in the team, to avoid double messages
                if (otherTeamChatLocked.get(playerId).equals(team) && !team.containsPlayer(playerId))
                {
                    MessageSender.sendChatMessage(p.getServer().getPlayer(playerId), rawMessage);
                }
            }
        }

        // ... to the global spies ...
        for (UUID playerId : globalSpies)
        {
            if (!team.containsPlayer(playerId))
            {
                p.getServer().getPlayer(playerId).sendMessage(rawMessage);
            }
        }

        // ... and to the console.
        if (UHConfig.TEAMS_OPTIONS.TEAM_CHAT.LOG.get())
        {
            p.getServer().getConsoleSender().sendMessage(rawMessage);
        }

        RunTask.later(new Runnable()
        {
            @Override
            public void run()
            {
                ProTips.LOCK_CHAT.sendTo(sender);
            }
        }, 30L);
    }

    /**
     * Sends a global message from the given player.
     *
     * @param sender The sender of this message.
     * @param message The message to be sent.
     */
    public void sendGlobalMessage(Player sender, String message)
    {
        // This message will be sent synchronously.
        // The players' messages are sent asynchronously.
        // That's how we differentiates the messages sent through /g and the messages sent using
        // the normal chat.
        sender.chat(message);
    }


    /**
     * Toggles the chat between the global chat and the team chat.
     *
     * @param player The chat of this player will be toggled.
     * @return {@code true} if the chat is now the team chat; false else.
     */
    public boolean toggleChatForPlayer(Player player)
    {
        return toggleChatForPlayer(player, null);
    }

    /**
     * Toggles the chat between the global chat and the team chat.
     *
     * @param player The chat of this player will be toggled.
     * @param team The team to chat with. If null, the player's team will be used.
     * @return {@code true} if the chat is now the team chat; false else.
     */
    public boolean toggleChatForPlayer(final Player player, UHTeam team)
    {
        // Permission check
        if (team == null && !player.hasPermission("uh.teamchat.self"))
        {
            player.sendMessage(I.t("{ce}You are not allowed to send a private message to your team."));
            return false;
        }
        if (team != null && !player.hasPermission("uh.teamchat.others"))
        {
            player.sendMessage(I.t("{ce}You are not allowed to enter in the private chat of another team."));
            return false;
        }


        // If the team is not null, we will always go to the team chat
        // Else, normal toggle

        if (team != null)
        {
            // if the player was in another team chat before, we removes it.
            teamChatLocked.remove(player.getUniqueId());
            otherTeamChatLocked.put(player.getUniqueId(), team);

            return true;
        }

        else
        {
            if (isAnyTeamChatEnabled(player))
            {
                teamChatLocked.remove(player.getUniqueId());
                otherTeamChatLocked.remove(player.getUniqueId());

                return false;
            }
            else
            {
                teamChatLocked.add(player.getUniqueId());

                RunTask.later(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        ProTips.USE_G_COMMAND.sendTo(player);
                    }
                }, 10L);

                return true;
            }
        }
    }

    /**
     * Returns true if the team chat is enabled for the given player.
     *
     * @param player The player.
     * @param team If non-null, this will check if the given player is spying the current team.
     * @return {@code true} if the team chat is enabled for the given player.
     */
    public boolean isTeamChatEnabled(Player player, UHTeam team)
    {
        if (team == null)
        {
            return teamChatLocked.contains(player.getUniqueId());
        }
        else
        {
            UHTeam lockedTeam = this.otherTeamChatLocked.get(player.getUniqueId());
            UHTeam playerTeam = p.getTeamManager().getTeamForPlayer(player);
            return (lockedTeam != null && lockedTeam.equals(team)) || (playerTeam != null && playerTeam.equals(team));
        }
    }

    /**
     * Returns true if the team chat is enabled for the given player.
     *
     * @param player The player.
     * @return {@code true} if the team chat is enabled for the given player.
     */
    public boolean isTeamChatEnabled(Player player)
    {
        return this.isTeamChatEnabled(player, null);
    }

    /**
     * Returns true if the given player is in the team chat of another team.
     *
     * @param player The player.
     * @return {@code true} if the given player is in the team chat of another team.
     */
    public boolean isOtherTeamChatEnabled(Player player)
    {
        return otherTeamChatLocked.containsKey(player.getUniqueId());
    }

    /**
     * Returns true if a team chat is enabled for the given player.
     *
     * @param player The player.
     * @return {@code true} if a team chat is enabled for the given player.
     */
    public boolean isAnyTeamChatEnabled(Player player)
    {
        return (teamChatLocked.contains(player.getUniqueId()) || otherTeamChatLocked.containsKey(player.getUniqueId()));
    }

    /**
     * Returns the other team viewed by the given player, or null if the player is not in
     * the chat of another team.
     *
     * @param player The player.
     * @return The other team viewed by the given player.
     */
    public UHTeam getOtherTeamEnabled(Player player)
    {
        return otherTeamChatLocked.get(player.getUniqueId());
    }


    /**
     * Registers a player receiving ALL the teams chats.
     *
     * @param id The spy's UUID.
     */
    public void addGlobalSpy(UUID id)
    {
        globalSpies.add(id);
    }

    /**
     * Stops a player from receiving ALL the teams chats.
     *
     * @param id The spy's UUID.
     */
    public void removeGlobalSpy(UUID id)
    {
        globalSpies.remove(id);
    }

    /**
     * Checks if the given player receives all the teams chats.
     *
     * @param id The spy's UUID.
     * @return {@code true} if spying.
     */
    public boolean isGlobalSpy(UUID id)
    {
        return globalSpies.contains(id);
    }
}
