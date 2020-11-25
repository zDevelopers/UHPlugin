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

import fr.zcraft.quartzlib.components.commands.CommandException;
import fr.zcraft.quartzlib.components.commands.CommandInfo;
import fr.zcraft.quartzlib.components.i18n.I;
import fr.zcraft.quartzteams.QuartzTeam;
import fr.zcraft.quartzteams.QuartzTeams;
import fr.zcraft.quartzteams.texts.TextUtils;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.stream.Collectors;

@CommandInfo (name = "tp-team", usageParameters = "<x> <y> <z> \"<team name>\" | <target> \"<team name>\"", aliases = {"tpteam", "tpt"})
public class TPTeamCommand extends WorldBasedCommand
{
    @Override
    protected void run() throws CommandException
    {
        final String[] qargs = TextUtils.extractArgsWithQuotes(args, 0);

        // possibly /uh tp-team <x> <y> <z> "<team ...>"
        if (qargs.length == 4)
        {
            final QuartzTeam team = QuartzTeams.get().getTeamByName(qargs[3]);

            // ok, the team exists.
            if (team != null)
            {
                try
                {
                    final World world = getTargetWorld();

                    final double x = Integer.parseInt(args[0]) + 0.5;
                    final double y = Integer.parseInt(args[1]) + 0.5;
                    final double z = Integer.parseInt(args[2]) + 0.5;

                    team.teleportTo(new Location(world, x, y, z));

                    /// {1}: players names, comma-separated. {2}: world name. {3-5}: x, y, z.
                    success(I.t(
                            "The players in the team {0} ({1}) were teleported to ({2} ; {3} ; {4} ; {5}).",
                            team.getName(),
                            String.join(", ", team.getPlayers().stream().map(OfflinePlayer::getName).collect(Collectors.toSet())),
                            world.getName(), x, y, z
                    ));

                    return;
                }
                catch (NumberFormatException e)
                {
                    throwInvalidArgument(I.t("{ce}The coordinates must be three valid numbers."));
                }
            }
        }

        // /uh tp team <target> "<team ...>"
        if (qargs.length == 2)
        {
            final QuartzTeam team = QuartzTeams.get().getTeamByName(qargs[1]);

            if (team == null)
            {
                throwInvalidArgument(I.t("{ce}This team is not registered."));
            }
            else
            {
                final Player player = getPlayerParameter(0);
                team.teleportTo(player.getLocation());

                /// {1}: players names, comma-separated.
                success(I.t(
                        "The players in the team {0} ({1}) were teleported to the player {2}.",
                        team.getName(),
                        String.join(", ", team.getPlayers().stream().map(OfflinePlayer::getName).collect(Collectors.toSet())),
                        player.getName()
                ));
            }
        }
    }
}
