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
package eu.carrade.amaury.quartzsurvivalgames.modules.core.spectators.commands;

import eu.carrade.amaury.quartzsurvivalgames.modules.core.spectators.SpectatorsModule;
import eu.carrade.amaury.quartzsurvivalgames.shortcuts.QSG;
import eu.carrade.amaury.quartzsurvivalgames.utils.OfflinePlayersLoader;
import fr.zcraft.quartzlib.components.commands.Command;
import fr.zcraft.quartzlib.components.commands.CommandException;
import fr.zcraft.quartzlib.components.commands.CommandInfo;
import fr.zcraft.quartzlib.components.i18n.I;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


@CommandInfo (name = "spectators", usageParameters = "[add <player>] [remove <player>]", aliases = {"spec", "sp"})
public class SpectatorsCommand extends Command
{
    @Override
    protected void run() throws CommandException
    {
        // /uh spec
        if (args.length == 0)
        {
            final Set<String> spectators = QSG.module(SpectatorsModule.class)
                    .getSpectators().stream()
                    .map(uuid -> Bukkit.getOfflinePlayer(uuid).getName())
                    .collect(Collectors.toSet());

            if (spectators.size() == 0)
            {
                error(I.t("{ce}There isn't any spectator to list."));
            }
            else
            {
                info("");
                info(I.tn("{ci}{0} registered spectator.", "{ci}{0} registered spectators.", spectators.size()));

                /// A list item in the startup spectators list
                spectators.stream().map(spectator -> I.tc("startup_specs", "{lightpurple} - {0}", spectator)).forEach(this::info);
            }
        }

        else
        {
            switch (args[0].toLowerCase())
            {
                case "add":
                case "a":
                    // /uh spec add
                    if (args.length == 1)
                    {
                        throwInvalidArgument(I.t("Please add the player you want to register as spectator."));
                    }

                    // /uh spec add <player>
                    else
                    {
                        final CommandSender finalSender = sender;

                        OfflinePlayersLoader.loadPlayer(args[1], player -> {
                            if (player == null)
                            {
                                finalSender.sendMessage(I.t("{ce}Unable to retrieve the player {0}."));

                                if (!Bukkit.getOnlineMode())
                                    finalSender.sendMessage(I.t("{ce}In offline mode, you cannot add players if they never came to this server."));

                                return;
                            }

                            QSG.module(SpectatorsModule.class).addSpectator(player);
                            finalSender.sendMessage(I.t("{cs}The player {0} is now a spectator.", player.getName()));
                        });
                    }

                    break;

                case "remove":
                case "r":
                    // /uh spec remove
                    if (args.length == 1)
                    {
                        throwInvalidArgument(I.t("Please add the player you want to unregister from spectators."));
                    }

                    // /uh spec remove <player>
                    else
                    {
                        final OfflinePlayer oldSpectator = OfflinePlayersLoader.getOfflinePlayer(args[1]);
                        if (oldSpectator == null)
                        {
                            error(I.t("{ce}The player {0} was not found.", args[1]));
                        }
                        else
                        {
                            QSG.module(SpectatorsModule.class).removeSpectator(oldSpectator);
                            success(I.t("{cs}The player {0} is now a player.", args[1]));
                        }
                    }

                    break;
            }
        }
    }

    @Override
    protected List<String> complete()
    {
        if (args.length == 1) return getMatchingSubset(args[0], "add", "remove");
        else if (args.length == 2)
        {
            switch (args[0].toLowerCase())
            {
                case "add":
                case "a":
                    return getMatchingSubset(
                            Arrays.stream(Bukkit.getOfflinePlayers())
                                    .filter(player -> !QSG.module(SpectatorsModule.class).isSpectator(player))
                                    .map(OfflinePlayer::getName)
                                    .collect(Collectors.toList()),
                            args[1]
                    );

                case "remove":
                case "r":
                    return getMatchingSubset(
                            Arrays.stream(Bukkit.getOfflinePlayers())
                                    .filter(player -> QSG.module(SpectatorsModule.class).isSpectator(player))
                                    .map(OfflinePlayer::getName)
                                    .collect(Collectors.toList()),
                            args[1]
                    );

                default:
                    return null;
            }
        }
        else return null;
    }
}
