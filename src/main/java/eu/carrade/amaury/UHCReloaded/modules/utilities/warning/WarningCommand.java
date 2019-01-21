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
package eu.carrade.amaury.UHCReloaded.modules.utilities.warning;

import eu.carrade.amaury.UHCReloaded.modules.core.timers.TimeDelta;
import eu.carrade.amaury.UHCReloaded.shortcuts.UR;
import fr.zcraft.zlib.components.commands.Command;
import fr.zcraft.zlib.components.commands.CommandException;
import fr.zcraft.zlib.components.commands.CommandInfo;
import fr.zcraft.zlib.components.i18n.I;

import java.util.List;

@CommandInfo (
        name = "border-warning",
        usageParameters = "<future border size | cancel> [time delta (minutes or mm:ss or hh:mm:ss) until border reduction]",
        aliases = {"borderwarning", "borderwarn", "bw"}
)
public class WarningCommand extends Command
{
    @Override
    protected void run() throws CommandException
    {
        final WarningModule warnings = UR.module(WarningModule.class);

        // /uh border warning
        if (args.length == 0)
        {
            throwInvalidArgument(I.t("Missing future border size."));
        }

        // /uh border warning cancel
        else if (args[0].equalsIgnoreCase("cancel"))
        {
            warnings.cancelWarning();
            success(I.t("{cs}Warning canceled."));
        }

        // /uh border warning <?>
        // or
        // /uh border warning <?> <?>
        else
        {
            try
            {
                final int warnDiameter = Integer.parseInt(args[0]);
                TimeDelta warnTime = null;

                // /uh border warning <?> <?>
                if (args.length >= 2)
                {
                    warnTime = new TimeDelta(args[1]);
                }

                warnings.setWarningSize(warnDiameter, warnTime, sender);
                success(I.tn("{cs}Future size saved. All players outside this future border will be warned every {0} second.", "{cs}Future size saved. All players outside this future border will be warned every {0} seconds.", (int) Config.WARNING_INTERVAL.get().getSeconds()));

            }
            catch (NumberFormatException e)
            {
                error(I.t("{ce}“{0}” is not a number...", args[0]));
            }
        }
    }

    @Override
    protected List<String> complete() throws CommandException
    {
        return args.length == 1 ? getMatchingSubset(args[0], "cancel") : null;
    }
}
