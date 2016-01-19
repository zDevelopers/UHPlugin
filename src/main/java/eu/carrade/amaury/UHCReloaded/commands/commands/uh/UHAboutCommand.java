/*
 * Copyright or © or Copr. Amaury Carrade (2014 - 2016)
 *
 * http://amaury.carrade.eu
 *
 * This software is governed by the CeCILL-B license under French law and
 * abiding by the rules of distribution of free software.  You can  use,
 * modify and/ or redistribute the software under the terms of the CeCILL-B
 * license as circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info".
 *
 * As a counterpart to the access to the source code and  rights to copy,
 * modify and redistribute granted by the license, users are provided only
 * with a limited warranty  and the software's author,  the holder of the
 * economic rights,  and the successive licensors  have only  limited
 * liability.
 *
 * In this respect, the user's attention is drawn to the risks associated
 * with loading,  using,  modifying and/or developing or reproducing the
 * software by the user in light of its specific status of free software,
 * that may mean  that it is complicated to manipulate,  and  that  also
 * therefore means  that it is reserved for developers  and  experienced
 * professionals having in-depth computer knowledge. Users are therefore
 * encouraged to load and test the software's suitability as regards their
 * requirements in conditions enabling the security of their systems and/or
 * data to be ensured and,  more generally, to use and operate it in the
 * same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL-B license and that you accept its terms.
 */
package eu.carrade.amaury.UHCReloaded.commands.commands.uh;

import eu.carrade.amaury.UHCReloaded.UHCReloaded;
import eu.carrade.amaury.UHCReloaded.commands.commands.categories.Category;
import eu.carrade.amaury.UHCReloaded.commands.core.AbstractCommand;
import eu.carrade.amaury.UHCReloaded.commands.core.annotations.Command;
import eu.carrade.amaury.UHCReloaded.commands.core.exceptions.CannotExecuteCommandException;
import eu.carrade.amaury.UHCReloaded.commands.core.utils.CommandUtils;
import fr.zcraft.zlib.components.i18n.I;
import fr.zcraft.zlib.components.i18n.I18n;
import org.bukkit.command.CommandSender;

import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.jar.Attributes;
import java.util.jar.Manifest;


/**
 * This command prints some informations about the plugin and the translation.
 *
 * Usage: /uh about
 */
@Command (name = "about")
public class UHAboutCommand extends AbstractCommand
{
    private UHCReloaded p;

    public UHAboutCommand(UHCReloaded plugin)
    {
        p = plugin;
    }

    /**
     * Runs the command.
     *
     * @param sender The sender of the command.
     * @param args   The arguments passed to the command.
     *
     * @throws eu.carrade.amaury.UHCReloaded.commands.core.exceptions.CannotExecuteCommandException If the command cannot be executed.
     */
    @Override
    public void run(CommandSender sender, String[] args) throws CannotExecuteCommandException
    {
        CommandUtils.displaySeparator(sender);
        sender.sendMessage(I.t("cmd.titleHelp", p.getDescription().getDescription(), p.getDescription().getVersion()));

        // Authors

        String authors = "";
        List<String> listAuthors = p.getDescription().getAuthors();
        for (String author : listAuthors)
        {
            if (author == listAuthors.get(0))
            {
                // Nothing
            }
            else if (author == listAuthors.get(listAuthors.size() - 1))
            {
                authors += " " + I.t("about.and") + " ";
            }
            else
            {
                authors += ", ";
            }
            authors += author;
        }
        sender.sendMessage(I.t("about.authors", authors));

        // Build number

        String build = null;
        try
        {
            Class<? extends UHCReloaded> clazz = p.getClass();
            String className = clazz.getSimpleName() + ".class";
            String classPath = clazz.getResource(className).toString();
            if (classPath.startsWith("jar"))  // Class from JAR
            {
                String manifestPath = classPath.substring(0, classPath.lastIndexOf("!") + 1) +
                        "/META-INF/MANIFEST.MF";
                Manifest manifest = new Manifest(new URL(manifestPath).openStream());
                Attributes attr = manifest.getMainAttributes();

                build = attr.getValue("Git-Commit");
            }
        }
        catch (IOException e)
        {
            // Build not available.
        }

        if (build != null)
        {
            sender.sendMessage(I.t("about.build.number", build));
        }
        else
        {
            sender.sendMessage(I.t("about.build.notAvailable"));
        }

        // Translation

        sender.sendMessage(I.t("about.i18n.title"));
        sender.sendMessage(I.t("about.i18n.selected", I18n.getPrimaryLocale(), I18n.getTranslationTeam(I18n.getPrimaryLocale())));
        sender.sendMessage(I.t("about.i18n.fallback", I18n.getFallbackLocale(), I18n.getTranslationTeam(I18n.getFallbackLocale())));
        sender.sendMessage(I.t("about.license.title"));
        sender.sendMessage(I.t("about.license.license"));

        CommandUtils.displaySeparator(sender);
    }

    /**
     * Tab-completes this command.
     *
     * @param sender The sender.
     * @param args   The arguments passed to the command.
     *
     * @return A list of suggestions.
     */
    @Override
    public List<String> tabComplete(CommandSender sender, String[] args)
    {
        return null;
    }

    @Override
    public List<String> help(CommandSender sender)
    {
        return null;
    }

    @Override
    public List<String> onListHelp(CommandSender sender)
    {
        return Collections.singletonList(I.t("cmd.helpAbout"));
    }

    @Override
    public String getCategory()
    {
        return Category.MISC.getTitle();
    }
}
