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
package eu.carrade.amaury.UHCReloaded.modules.utilities.food.commands;

import fr.zcraft.zlib.components.commands.Command;
import fr.zcraft.zlib.components.commands.CommandException;
import fr.zcraft.zlib.components.commands.CommandInfo;
import fr.zcraft.zlib.components.i18n.I;
import org.bukkit.entity.Player;

import java.util.List;


@CommandInfo (name = "feed", usageParameters = "<player> [foodPoints=20] [saturation=max]", aliases = {"food"})
public class FeedCommand extends Command
{
    @Override
    protected void run() throws CommandException
    {
        final Player target = getPlayerParameter(0);

        int foodLevel = 20;
        float saturation = 20f;

        // /uh feed <player> <foodLevel>
        if (args.length > 1)
        {
            try
            {
                foodLevel = Integer.valueOf(args[1]);
            }
            catch (final NumberFormatException e)
            {
                throwInvalidArgument(I.t("{ce}Food points and saturation must be numbers (floats for the saturation)!"));
            }

            // /uh feed <player> <foodLevel> <saturation>
            if (args.length > 2)
            {
                try
                {
                    // The saturation value cannot be more than the food level.
                    saturation = Math.max(foodLevel, Float.valueOf(args[2]));
                }
                catch (final NumberFormatException e)
                {
                    throwInvalidArgument(I.t("{ce}Food points and saturation must be numbers (floats for the saturation)!"));
                }
            }
        }

        target.setFoodLevel(foodLevel);
        target.setSaturation(saturation);

        success(I.t("Set food level to {0} and saturation to {1} for {2}.", foodLevel, saturation, target.getName()));
    }

    @Override
    protected List<String> complete()
    {
        if (args.length == 1) return getMatchingPlayerNames(args[0]);
        else return null;
    }
}
