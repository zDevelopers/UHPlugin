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
package eu.carrade.amaury.UHCReloaded.old.commands.commands.uh;

import eu.carrade.amaury.UHCReloaded.UHCReloaded;
import eu.carrade.amaury.UHCReloaded.old.commands.commands.categories.Category;
import eu.carrade.amaury.UHCReloaded.old.commands.core.AbstractCommand;
import eu.carrade.amaury.UHCReloaded.old.commands.core.annotations.Command;
import eu.carrade.amaury.UHCReloaded.old.commands.core.exceptions.CannotExecuteCommandException;
import eu.carrade.amaury.UHCReloaded.old.commands.core.utils.CommandUtils;
import eu.carrade.amaury.UHCReloaded.old.teams.UHTeam;
import fr.zcraft.zlib.components.i18n.I;
import fr.zcraft.zlib.tools.text.RawMessage;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

@Command (name = "infos")
public class UHInfosCommand extends AbstractCommand
{
    private UHCReloaded p;

    public UHInfosCommand(UHCReloaded p)
    {
        this.p = p;
    }

    @Override
    public void run(CommandSender sender, String[] args) throws CannotExecuteCommandException
    {
        CommandUtils.displaySeparator(sender);

        if (p.getGameManager().isGameStarted())
        {
            /// Header of the /uh infos command. Plural based on the players count.
            sender.sendMessage(I.tn("{ci}{0} player alive in {1} team.", "{ci}{0} players alive in {1} teams.", p.getGameManager().getAlivePlayersCount(), p.getGameManager().getAlivePlayersCount(), p.getGameManager().getAliveTeamsCount()));
        }
        else
        {
            sender.sendMessage(I.t("{ci}The game is not started."));
        }

        for (UHTeam team : p.getTeamManager().getTeams())
        {
            for (OfflinePlayer player : team.getPlayers())
            {
                if (sender instanceof Player)
                {
                    /* We can use a JSON-based message */

                    String json = "{\"text\":\"\",\"extra\":[";


                    // Online/offline bullet
                    json += "{";
                    if (player.isOnline())
                    {
                        /// Online status dot in /uh infos
                        json += "\"text\":\"" + I.t("{green} • ") + "\",";
                        json += "\"hoverEvent\":{\"action\":\"show_text\",\"value\":\"" + I.t("Currently online") + "\"}";
                    }
                    else
                    {
                        /// Offline status dot in /uh infos
                        json += "\"text\":\"" + I.t("{red} • ") + "\",";
                        json += "\"hoverEvent\":{\"action\":\"show_text\",\"value\":\"" + I.t("Currently offline") + "\"}";
                    }
                    json += "},";


                    // Name and team
                    json += "{";
                    json += "\"text\":\"" + team.getColorOrWhite().toChatColor() + player.getName() + ChatColor.RESET + "\",";
                    /// Team name in tooltip in /uh infos
                    json += "\"hoverEvent\":{\"action\":\"show_text\",\"value\":\"" + I.t("Team: {0}", team.getDisplayName()) + "\"}";
                    json += "}";


                    if (p.getGameManager().isGameStarted())
                    {
                        /// Separator in /uh infos
                        json += ",{\"text\":\"" + I.t("{gray} - ") + "\"},";

                        // Alive state
                        json += "{";
                        if (!p.getGameManager().isPlayerDead(player.getUniqueId()))
                        {
                            /// Alive state in /uh infos
                            json += "\"text\":\"" + I.t("{green}alive") + "\",";
                            if (player.isOnline())
                            {
                                json += "\"hoverEvent\":{\"action\":\"show_text\",\"value\":\"" + I.t("{0} half-hearts", String.valueOf((int) ((Player) player).getHealth())) + "\"}";
                            }
                        }
                        else
                        {
                            /// Alive state in /uh infos
                            json += "\"text\":\"" + I.t("{red}dead") + "\"";
                        }
                        json += "}";
                    }

                    // End
                    json += "]}";

                    RawMessage.send((Player) sender, json);
                }
                else
                {
					/* Fallback to a simple display for the console */

                    String info;

                    if (player.isOnline())
                    {
                        info = I.t("{green} • ");
                    }
                    else
                    {
                        info = I.t("{red} • ");
                    }

                    info += team.getColorOrWhite().toChatColor() + player.getName() + ChatColor.RESET;

                    if (p.getGameManager().isGameStarted())
                    {
                        info += I.t("{gray} - ");

                        if (!p.getGameManager().isPlayerDead(player.getUniqueId()))
                        {
                            info += I.t("{green}alive");
                        }
                        else
                        {
                            info += I.t("{red}dead");
                        }
                    }

                    sender.sendMessage(info);
                }
            }
        }


        CommandUtils.displaySeparator(sender);
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args)
    {
        return null;
    }

    @Override
    public List<String> help(CommandSender sender)
    {
        return null;
    }

    @Override
    public List<String> onListHelp(CommandSender sender)
    {
        return Collections.singletonList(I.t("{cc}/uh infos {ci}: prints some infos about the current game."));
    }

    @Override
    public String getCategory()
    {
        return Category.MISC.getTitle();
    }
}
