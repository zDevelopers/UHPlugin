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
package eu.carrade.amaury.UHCReloaded.modules.cosmetics.playerListHeaderFooter;

import eu.carrade.amaury.UHCReloaded.shortcuts.UR;
import fr.zcraft.zlib.components.commands.Command;
import fr.zcraft.zlib.components.commands.CommandException;
import fr.zcraft.zlib.components.commands.CommandInfo;
import fr.zcraft.zlib.components.commands.WithFlags;
import fr.zcraft.zlib.components.i18n.I;
import fr.zcraft.zlib.tools.commands.PaginatedTextView;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.Map;
import java.util.function.Supplier;

@CommandInfo (name = "list-hf-placeholders")
@WithFlags("page")
public class ListPlaceholdersCommand extends Command
{
    @Override
    protected void run() throws CommandException
    {
        final Map<String, Supplier<String>> placeholders = UR.module(PlayerListHeaderFooterModule.class).getPlaceholderSuppliers();
        final int page = args.length > 0 ? getIntegerParameter(0) : 1;

        new PlaceholdersList()
            .setData(placeholders.entrySet().toArray(new Map.Entry[placeholders.entrySet().size()]))
            .setCurrentPage(page)
            .display(sender);
    }

    private final class PlaceholdersList extends PaginatedTextView<Map.Entry<String, Supplier<String>>>
    {
        @Override
        protected void displayHeader(final CommandSender receiver)
        {
            receiver.sendMessage(ChatColor.GREEN + "" + ChatColor.BOLD + I.tn("{0} registered placeholder", "{0} registered placeholders", data().length));
        }

        @Override
        protected void displayItem(final CommandSender receiver, final Map.Entry<String, Supplier<String>> item)
        {
            receiver.sendMessage(String.format("%s{%s}%s\t« %s%s »", ChatColor.WHITE, item.getKey(), ChatColor.GRAY, item.getValue().get(), ChatColor.GRAY));
        }

        @Override
        protected String getCommandToPage(final int page)
        {
            return build(String.valueOf(page));
        }
    }
}
