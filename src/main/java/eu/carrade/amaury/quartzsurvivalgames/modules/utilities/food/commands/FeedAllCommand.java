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
package eu.carrade.amaury.quartzsurvivalgames.modules.utilities.food.commands;

import fr.zcraft.quartzlib.components.commands.Command;
import fr.zcraft.quartzlib.components.commands.CommandException;
import fr.zcraft.quartzlib.components.commands.CommandInfo;
import fr.zcraft.quartzlib.components.i18n.I;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;


@CommandInfo (name = "feed-all", usageParameters = "[foodPoints=20] [saturation=max]")
public class FeedAllCommand extends Command
{
    @Override
    protected void run() throws CommandException
    {
        int foodLevel = 20;
        float saturation = 20f;

        // /uh feed-all <foodLevel>
        if (args.length > 0)
        {
            try
            {
                foodLevel = Integer.valueOf(args[0]);
            }
            catch (NumberFormatException e)
            {
                throwInvalidArgument(I.t("{ce}Food points and saturation must be numbers (floats for the saturation)!"));
            }

            // /uh feed-all <foodLevel> <saturation>
            if (args.length > 1)
            {
                try
                {
                    // The saturation value cannot be more than the food level.
                    saturation = Math.max(foodLevel, Float.valueOf(args[1]));
                }
                catch (NumberFormatException e)
                {
                    throwInvalidArgument(I.t("{ce}Food points and saturation must be numbers (floats for the saturation)!"));
                }
            }
        }

        for (final Player player : Bukkit.getOnlinePlayers())
        {
            player.setFoodLevel(foodLevel);
            player.setSaturation(saturation);
        }

        success(I.t("Set food level to {0} and saturation to {1} for every player.", foodLevel, saturation));
    }
}
