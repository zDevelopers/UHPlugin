/**
 *  Plugin UltraHardcore Reloaded (UHPlugin)
 *  Copyright (C) 2013 azenet
 *  Copyright (C) 2014-2015 Amaury Carrade
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see [http://www.gnu.org/licenses/].
 */
package eu.carrade.amaury.UHCReloaded.commands.commands.uh;

import eu.carrade.amaury.UHCReloaded.commands.commands.categories.Category;
import eu.carrade.amaury.UHCReloaded.commands.core.annotations.Command;
import eu.carrade.amaury.UHCReloaded.commands.core.exceptions.CannotExecuteCommandException;
import eu.carrade.amaury.UHCReloaded.commands.core.utils.CommandUtils;
import eu.carrade.amaury.UHCReloaded.UHCReloaded;
import eu.carrade.amaury.UHCReloaded.commands.core.AbstractCommand;
import eu.carrade.amaury.UHCReloaded.i18n.I18n;
import org.bukkit.command.CommandSender;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.jar.Attributes;
import java.util.jar.Manifest;


/**
 * This command prints some informations about the plugin and the translation.
 *
 * Usage: /uh about
 */
@Command(name = "about")
public class UHAboutCommand extends AbstractCommand {

	UHCReloaded p;
	I18n i;

	public UHAboutCommand(UHCReloaded plugin) {
		p = plugin;
		i = plugin.getI18n();
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
	public void run(CommandSender sender, String[] args) throws CannotExecuteCommandException {
		CommandUtils.displaySeparator(sender);
		sender.sendMessage(i.t("cmd.titleHelp", p.getDescription().getDescription(), p.getDescription().getVersion()));

		// Authors

		String authors = "";
		List<String> listAuthors = p.getDescription().getAuthors();
		for(String author : listAuthors) {
			if(author == listAuthors.get(0)) {
				// Nothing
			}
			else if(author == listAuthors.get(listAuthors.size() - 1)) {
				authors += " " + i.t("about.and") + " ";
			}
			else {
				authors += ", ";
			}
			authors += author;
		}
		sender.sendMessage(i.t("about.authors", authors));

		// Build number

		String build = null;
		try {
			Class<? extends UHCReloaded> clazz = p.getClass();
			String className = clazz.getSimpleName() + ".class";
			String classPath = clazz.getResource(className).toString();
			if (classPath.startsWith("jar")) { // Class from JAR
				String manifestPath = classPath.substring(0, classPath.lastIndexOf("!") + 1) +
						"/META-INF/MANIFEST.MF";
				Manifest manifest = new Manifest(new URL(manifestPath).openStream());
				Attributes attr = manifest.getMainAttributes();

				build = attr.getValue("Git-Commit");
			}
		} catch (IOException e) {
			// Build not available.
		}

		if(build != null) {
			sender.sendMessage(i.t("about.build.number", build));
		}
		else {
			sender.sendMessage(i.t("about.build.notAvailable"));
		}

		// Translation

		sender.sendMessage(i.t("about.i18n.title"));
		sender.sendMessage(i.t("about.i18n.selected", i.getSelectedLanguage(), i.getTranslator(i.getSelectedLanguage())));
		sender.sendMessage(i.t("about.i18n.fallback", i.getDefaultLanguage(), i.getTranslator(i.getDefaultLanguage())));
		sender.sendMessage(i.t("about.license.title"));
		sender.sendMessage(i.t("about.license.license"));

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
	public List<String> tabComplete(CommandSender sender, String[] args) {
		return null;
	}

	@Override
	public List<String> help(CommandSender sender) {
		return null;
	}

	@Override
	public List<String> onListHelp(CommandSender sender) {
		return Arrays.asList(i.t("cmd.helpAbout"));
	}

	@Override
	public String getCategory() {
		return Category.MISC.getTitle();
	}
}
