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

import eu.carrade.amaury.quartzsurvivalgames.modules.core.spectators.SpectatorsModule;
import eu.carrade.amaury.quartzsurvivalgames.shortcuts.QSG;
import fr.zcraft.quartzlib.components.commands.CommandException;
import fr.zcraft.quartzlib.components.commands.CommandInfo;
import fr.zcraft.quartzlib.components.i18n.I;
import java.util.List;
import java.util.Objects;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

@CommandInfo(name = "tp-spectators", usageParameters = "<x> <y> <z> | <target>", aliases = {"tpspectators", "tp-specs",
        "tpspecs"})
public class TPSpectatorsCommand extends WorldBasedCommand {
    @Override
    protected void run() throws CommandException {
        final SpectatorsModule spectators = QSG.module(SpectatorsModule.class);

        // /uh tp-spectators <x> <y> <z>
        if (args.length == 3) {
            try {
                final World world = getTargetWorld();

                final double x = Integer.parseInt(args[0]) + 0.5;
                final double y = Integer.parseInt(args[1]) + 0.5;
                final double z = Integer.parseInt(args[2]) + 0.5;

                spectators.getSpectators().stream()
                        .map(Bukkit::getPlayer).filter(Objects::nonNull)
                        .forEach(spectator -> spectator.teleport(new Location(world, x, y, z), TeleportCause.PLUGIN));

                /// {0}: world name. {1-3}: x, y, z.
                success(I.t("All spectators were teleported to ({0} ; {1} ; {2} ; {3}).", world.getName(), x, y, z));
            }
            catch (final NumberFormatException e) {
                throwInvalidArgument(I.t("{ce}The coordinates must be three valid numbers."));
            }
        }

        // /uh tp-spectators <target>
        else if (args.length == 1) {
            final Player target = getPlayerParameter(0);

            spectators.getSpectators().stream()
                    .map(Bukkit::getPlayer).filter(Objects::nonNull)
                    .forEach(spectator -> spectator.teleport(target, TeleportCause.PLUGIN));

            success(I.t("All spectators were teleported to the player {0}.", target.getName()));
        } else {
            throwInvalidArgument(I.t("You must specify either three coordinates or a player name."));
        }
    }

    @Override
    protected List<String> complete() {
        if (args.length == 1) {
            return getMatchingPlayerNames(args[0]);
        } else {
            return null;
        }
    }
}
