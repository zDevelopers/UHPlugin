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
package eu.carrade.amaury.UHCReloaded.modules.core.modules.commands;

import eu.carrade.amaury.UHCReloaded.UHCReloaded;
import eu.carrade.amaury.UHCReloaded.core.ModuleWrapper;
import fr.zcraft.zlib.components.commands.Command;
import fr.zcraft.zlib.components.commands.CommandException;
import fr.zcraft.zlib.components.commands.CommandInfo;
import fr.zcraft.zlib.components.commands.Commands;
import fr.zcraft.zlib.components.i18n.I;
import fr.zcraft.zlib.components.rawtext.RawText;
import fr.zcraft.zlib.components.rawtext.RawTextPart;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import java.util.*;
import java.util.stream.Stream;


@CommandInfo (name = "modules")
public class ModulesCommand extends Command
{
    @Override
    protected void run() throws CommandException
    {
        final Set<ModuleWrapper> modules = new TreeSet<>((module1, module2) -> {
            if (module1.equals(module2)) return 0;

            if (module1.isLoaded() != module2.isLoaded()) return module1.isLoaded() ? -1 : 1;
            if (module1.isEnabled() != module2.isEnabled()) return module1.isEnabled() ? -1 : 1;

            if (module1.getWhen() != module2.getWhen())
                return Integer.compare(module1.getWhen().ordinal(), module2.getWhen().ordinal());

            if (module1.isInternal() != module2.isInternal()) return module1.isInternal() ? -1 : 1;

            return module1.getName().compareTo(module2.getName());
        });

        modules.addAll(UHCReloaded.get().getModulesManager().getModules());

        success(I.tn("{0} module registered {gray}(hover for details)", "{0} modules registered {gray}(hover for details)", modules.size()));
        modules.forEach(module -> {
            final List<String> commands = new ArrayList<>();

            if (module.isLoaded())
            {
                final List<Class<? extends Command>> commandsClasses = module.get().getCommands();
                if (commandsClasses != null)
                {
                    commandsClasses.forEach(clazz -> {
                        final Command cmd = Commands.getCommandInfo(clazz);
                        if (cmd != null) commands.add(cmd.getUsageString());
                    });
                }

                final Map<String, Class<? extends Command>> commandsAliases = module.get().getCommandsAliases();
                if (commandsAliases != null)
                {
                    commandsAliases.forEach((alias, clazz) -> {
                        final Command cmd = Commands.getCommandInfo(clazz);
                        if (cmd != null) commands.add("/" + alias + " " + cmd.getUsageParameters() + ChatColor.DARK_GRAY + " (alias)");
                    });
                }
            }

            final RawTextPart<?> tooltip = new RawText();

            tooltip
                .then(module.getName())
                    .style(ChatColor.BOLD, module.isLoaded() ? ChatColor.GREEN : (module.isEnabled() ? ChatColor.GOLD : ChatColor.RED))
                .then("\n")
                .then(module.isLoaded() ? I.t("Loaded") : I.t("Unloaded"))
                    .color(ChatColor.GRAY)
                .then(" - ")
                    .color(ChatColor.DARK_GRAY)
                .then(module.isEnabled() ? I.t("Enabled") : I.t("Disabled"))
                    .color(ChatColor.GRAY)
                .then("\n\n")
                .then(module.getDescription())
                    .color(ChatColor.WHITE)
                .then("\n\n")
                .then(I.t("Load time"))
                    .color(ChatColor.BLUE)
                .then("\n")
                .then(module.getWhen().toString())
                    .color(ChatColor.WHITE);

            if (!commands.isEmpty())
            {
                tooltip.then("\n\n").then(I.t("Provided commands")).style(ChatColor.BLUE);
                commands.forEach(command -> tooltip.then("\n- ").style(ChatColor.GRAY).then(command).color(ChatColor.WHITE));
            }

            if (module.getDependencies().length != 0)
            {
                tooltip.then("\n\n").then(I.t("External dependencies")).style(ChatColor.BLUE);
                Stream.of(module.getDependencies()).forEach(dep -> tooltip.then("\n- ").style(ChatColor.GRAY).then(dep).color(Bukkit.getPluginManager().getPlugin(dep) != null ? ChatColor.WHITE : ChatColor.RED));
            }

            if (module.isInternal())
            {
                tooltip.then("\n\n").then(I.t("Internal module")).style(ChatColor.DARK_GRAY);
            }
            if (!module.canBeUnloaded())
            {
                tooltip.then(module.isInternal() ? " - " : "\n\n").color(ChatColor.DARK_GRAY).then(I.t("Cannot be disabled")).color(ChatColor.DARK_GRAY);
            }

            send(new RawText().hover(tooltip)
                    .then("• ").color(module.isLoaded() ? ChatColor.GREEN : (module.isEnabled() ? ChatColor.GOLD : ChatColor.RED))
                    .then(module.getName()).color(ChatColor.WHITE)
                    .build()
            );
        });
    }
}
