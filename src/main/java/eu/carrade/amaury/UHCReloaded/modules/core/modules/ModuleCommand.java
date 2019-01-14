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
package eu.carrade.amaury.UHCReloaded.modules.core.modules;

import eu.carrade.amaury.UHCReloaded.core.ModuleWrapper;
import eu.carrade.amaury.UHCReloaded.shortcuts.UR;
import fr.zcraft.zlib.components.commands.Command;
import fr.zcraft.zlib.components.commands.CommandException;
import fr.zcraft.zlib.components.commands.CommandInfo;
import fr.zcraft.zlib.components.i18n.I;
import org.bukkit.ChatColor;

import java.text.Normalizer;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@CommandInfo (name = "module", usageParameters = "enable|disable <module>")
public class ModuleCommand extends Command
{
    private static final Pattern NON_LATIN = Pattern.compile("[^\\w-]");
    private static final Pattern WHITESPACE = Pattern.compile("[\\s]");


    @Override
    protected void run() throws CommandException
    {
        if (args.length < 2 || (!args[0].equalsIgnoreCase("enable") && !args[0].equalsIgnoreCase("disable")))
        {
            throwInvalidArgument(I.t("Invalid command usage."));
        }

        ModuleWrapper module = null;

        for (final ModuleWrapper m : UR.get().getModules())
        {
            if (getModuleKey(m).equalsIgnoreCase(args[1]))
            {
                module = m; break;
            }
        }

        if (module == null)
        {
            throwInvalidArgument(I.t("No module with key {0}. Use autocompletion to get a list of keys.", args[1]));
            return;
        }

        if (args[0].equalsIgnoreCase("enable"))
        {
            if (module.isEnabled())
            {
                error(I.t("{red}The module {darkred}{0}{red} is already enabled.", module.getName()));
            }
            else if (module.setEnabled(true))
            {
                success(I.t("{green}The module {darkgreen}{0}{green} was successfully enabled.", module.getName()));

                if (module.isLoaded())
                {
                    info(I.t("It was also loaded and is now running."));
                }
                else
                {
                    info(I.t("It will be loaded when needed."));
                }
            }
            else
            {
                warning(I.t("{red}Unable to load the module {darkred}{0}{red}.", module.getName()));
                info("It is probably too late to enable this module.");
            }
        }
        else
        {
            if (!module.isEnabled())
            {
                error(I.t("{red}he module {darkred}{0}{red} is already disabled.", module.getName()));
            }
            else if (module.setEnabled(false))
            {
                success(I.t("{green}The module {darkred}{0}{green} was successfully disabled.", module.getName()));
            }
            else
            {
                error(I.t("{red}Unable to disable the module {darkred}{0}{red}. It is probably protected.", module.getName()));
            }
        }
    }

    @Override
    protected List<String> complete()
    {
        if (args.length == 1) return getMatchingSubset(args[0], "enable", "disable");
        else if (args.length == 2) return getMatchingSubset(UR.get().getModules().stream().filter(module -> args[0].equalsIgnoreCase("enable") != module.isEnabled()).map(this::getModuleKey).collect(Collectors.toSet()), args[1]);
        else return null;
    }

    private String getModuleKey(final ModuleWrapper module)
    {
        return slug(module.getName());
    }

    /**
     * Generates a slug from the (potentially Minecraft-formatted) given string.
     *
     * @param input The string to convert into a slug. May contain Minecraft
     *              formatting codes: they will be striped.
     * @return The slug.
     */
    private static String slug(final String input)
    {
        final String nowhitespace = WHITESPACE.matcher(ChatColor.stripColor(input)).replaceAll("-");
        final String normalized = Normalizer.normalize(nowhitespace, Normalizer.Form.NFD);
        final String slug = NON_LATIN.matcher(normalized).replaceAll("");

        return slug.toLowerCase(Locale.ENGLISH);
    }
}
