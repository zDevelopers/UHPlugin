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
package eu.carrade.amaury.UHCReloaded.commands.commands.uh;

import eu.carrade.amaury.UHCReloaded.UHCReloaded;
import eu.carrade.amaury.UHCReloaded.commands.commands.categories.Category;
import eu.carrade.amaury.UHCReloaded.commands.core.AbstractCommand;
import eu.carrade.amaury.UHCReloaded.commands.core.annotations.Command;
import eu.carrade.amaury.UHCReloaded.commands.core.exceptions.CannotExecuteCommandException;
import eu.carrade.amaury.UHCReloaded.commands.core.utils.CommandUtils;
import eu.carrade.amaury.UHCReloaded.i18n.I18n;
import eu.carrade.amaury.UHCReloaded.teams.UHTeam;
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
    UHCReloaded p;
    I18n i;

    public UHInfosCommand(UHCReloaded p)
    {
        this.p = p;
        this.i = p.getI18n();
    }

    @Override
    public void run(CommandSender sender, String[] args) throws CannotExecuteCommandException
    {

        CommandUtils.displaySeparator(sender);

        if (p.getGameManager().isGameStarted())
        {
            sender.sendMessage(i.t("infos.players", String.valueOf(p.getGameManager().getAlivePlayersCount()), String.valueOf(p.getGameManager().getAliveTeamsCount())));
        }
        else
        {
            sender.sendMessage(i.t("infos.notStarted"));
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
                        json += "\"text\":\"" + i.t("infos.bulletOnline") + "\",";
                        json += "\"hoverEvent\":{\"action\":\"show_text\",\"value\":\"" + i.t("infos.tooltips.online") + "\"}";
                    }
                    else
                    {
                        json += "\"text\":\"" + i.t("infos.bulletOffline") + "\",";
                        json += "\"hoverEvent\":{\"action\":\"show_text\",\"value\":\"" + i.t("infos.tooltips.offline") + "\"}";
                    }
                    json += "},";


                    // Name and team
                    json += "{";
                    json += "\"text\":\"" + team.getColor().toChatColor() + player.getName() + ChatColor.RESET + "\",";
                    json += "\"hoverEvent\":{\"action\":\"show_text\",\"value\":\"" + i.t("infos.tooltips.team", team.getDisplayName()) + "\"}";
                    json += "}";


                    if (p.getGameManager().isGameStarted())
                    {
                        // Separator
                        json += ",{\"text\":\"" + i.t("infos.separatorAliveState") + "\"},";

                        // Alive state
                        json += "{";
                        if (!p.getGameManager().isPlayerDead(player.getUniqueId()))
                        {
                            json += "\"text\":\"" + i.t("infos.alive") + "\",";
                            if (player.isOnline())
                            {
                                json += "\"hoverEvent\":{\"action\":\"show_text\",\"value\":\"" + i.t("infos.tooltips.health", String.valueOf((int) ((Player) player).getHealth())) + "\"}";
                            }
                        }
                        else
                        {
                            json += "\"text\":\"" + i.t("infos.dead") + "\"";
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

                    String info = null;

                    if (player.isOnline())
                    {
                        info = i.t("infos.bulletOnline");
                    }
                    else
                    {
                        info = i.t("infos.bulletOffline");
                    }

                    info += team.getColor().toChatColor() + player.getName() + ChatColor.RESET;

                    if (p.getGameManager().isGameStarted())
                    {
                        info += i.t("infos.separatorAliveState");

                        if (!p.getGameManager().isPlayerDead(player.getUniqueId()))
                        {
                            info += i.t("infos.alive");
                        }
                        else
                        {
                            info += i.t("infos.dead");
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
        return Collections.singletonList(i.t("cmd.helpInfos"));
    }

    @Override
    public String getCategory()
    {
        return Category.MISC.getTitle();
    }
}
