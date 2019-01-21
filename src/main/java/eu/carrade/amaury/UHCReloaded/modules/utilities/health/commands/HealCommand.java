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
package eu.carrade.amaury.UHCReloaded.modules.utilities.health.commands;

import eu.carrade.amaury.UHCReloaded.modules.core.teams.TeamsModule;
import eu.carrade.amaury.UHCReloaded.shortcuts.UR;
import fr.zcraft.zlib.components.commands.Command;
import fr.zcraft.zlib.components.commands.CommandException;
import fr.zcraft.zlib.components.commands.CommandInfo;
import fr.zcraft.zlib.components.i18n.I;
import org.bukkit.entity.Player;

import java.util.List;


@CommandInfo (name = "heal", usageParameters = "<player> [half-hearts=20|±diff]", aliases = {"life", "set-health", "sethealth"})
public class HealCommand extends Command
{
    @Override
    protected void run() throws CommandException
    {
        if (args.length == 0)
        {
            throwInvalidArgument(I.t("A player is required"));
        }

        final Player target = getPlayerParameter(0);

        double health;
        boolean add = false; // "add" (±, true) or "raw" (exact health, false) mode

        // /uh heal <player> : full life for player.
        if (args.length == 1)
        {
            health = 20D;
        }

        // /uh heal <player> <hearts>
        else
        {
            double diffHealth;

            try
            {
                if (args[1].startsWith("+") || args[1].startsWith("-"))
                {
                    add = true;
                }

                diffHealth = Double.parseDouble(args[1]);
            }
            catch (final NumberFormatException e)
            {
                throwInvalidArgument(I.t("{ce}Hey, this is not a number of half-hearts. It's a text. Pfff."));
                return;
            }

            health = !add ? diffHealth : target.getHealth() + diffHealth;

            if (health <= 0D)
            {
                error(I.t("{ce}You can't kill a player with this command, to avoid typo fails."));
            }
            else if (health > 20D)
            {
                health = 20D;
            }
        }

        target.setHealth(health);

        // TODO Could we get rid of this hard reference?
        UR.module(TeamsModule.class).getSidebarPlayerCache(target.getUniqueId()).updateHealth(health);

        success(I.t("The health of {0} was set to {1}.", target.getName(), health));
    }

    @Override
    protected List<String> complete()
    {
        if (args.length == 1) return getMatchingPlayerNames(args[0]);
        else return null;
    }
}
