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
package eu.carrade.amaury.quartzsurvivalgames.modules.utilities.teleportation.commands;

import eu.carrade.amaury.quartzsurvivalgames.modules.utilities.teleportation.TeleportationModule;
import eu.carrade.amaury.quartzsurvivalgames.shortcuts.QSG;
import eu.carrade.amaury.quartzsurvivalgames.utils.QSGUtils;
import fr.zcraft.quartzlib.components.commands.Command;
import fr.zcraft.quartzlib.components.commands.CommandException;
import fr.zcraft.quartzlib.components.commands.CommandInfo;
import fr.zcraft.quartzlib.components.i18n.I;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.stream.Collectors;

@CommandInfo (name = "tp-death", usageParameters = "[player] [force]",aliases = {"tpback", "tpdeath", "tpd"})
public class TPDeathCommand extends Command
{
    @Override
    protected void run() throws CommandException
    {
        final Player player = args.length > 0 ? getPlayerParameter(0) : playerSender();
        final boolean force = args.length > 1 && args[1].equalsIgnoreCase("force");

        if (!QSG.module(TeleportationModule.class).hasDeathLocation(player))
        {
            error(I.t("{ce}No death location available for the player {0}.", player.getName()));
        }

        final Location deathLocation = QSG.module(TeleportationModule.class).getDeathLocation(player);

        if (force)
        {
            QSGUtils.safeTP(player, deathLocation, true);
            success(I.t("{cs}The player {0} was teleported back.", player.getName()));
        }
        else if (QSGUtils.safeTP(player, deathLocation))
        {
            success(I.t("{cs}The player {0} was teleported back.", player.getName()));
        }
        else
        {
            warning(I.t("{ce}The player {0} was NOT teleported back because no safe spot was found.", player.getName()));
            warning(I.t("{ci}Use {cc}/uh tpback {0} force{ci} to teleport the player regardless this point.", player.getName()));
        }
    }

    @Override
    protected List<String> complete()
    {
        if (args.length == 1)
        {
            final TeleportationModule tm = QSG.module(TeleportationModule.class);

            return getMatchingPlayerNames(
                    Bukkit.getOnlinePlayers().stream().filter(tm::hasDeathLocation).collect(Collectors.toList()),
                    args[0]
            );
        }

        else if (args.length == 2)
        {
            return getMatchingSubset(args[1].toLowerCase(), "force");
        }

        else return null;
    }
}
