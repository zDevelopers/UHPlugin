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
package eu.carrade.amaury.UHCReloaded.modules.core.modules;

import eu.carrade.amaury.UHCReloaded.UHCReloaded;
import eu.carrade.amaury.UHCReloaded.core.ModuleWrapper;
import fr.zcraft.zlib.components.commands.Command;
import fr.zcraft.zlib.components.commands.CommandException;
import fr.zcraft.zlib.components.commands.CommandInfo;
import fr.zcraft.zlib.components.i18n.I;

import java.util.Set;
import java.util.TreeSet;


@CommandInfo (name = "modules", usageParameters = "[list|enable|disable]", aliases = {"module"})
public class ModulesCommand extends Command
{
    @Override
    protected void run() throws CommandException
    {
        final Set<ModuleWrapper> modules = new TreeSet<>((module1, module2) -> {
            if (module1.equals(module2)) return 0;

            if (module1.isEnabled() != module2.isEnabled()) return module1.isEnabled() ? -1 : 1;

            if (module1.getWhen() != module2.getWhen())
                return Integer.compare(module1.getWhen().ordinal(), module2.getWhen().ordinal());

            if (module1.isInternal() != module2.isInternal()) return module1.isInternal() ? -1 : 1;

            return module1.getName().compareTo(module2.getName());
        });

        modules.addAll(UHCReloaded.get().getModules());

        success(I.tn("{0} module registered", "{0} modules registered", modules.size()));
        modules.forEach(module -> {
            if (module.isEnabled())
            {
                info(I.t(
                        "{green} • {white}{0} (enabled - {1})",
                        module.getName(),
                        module.getWhen()
                ));
            }
            else
            {
                info(I.t(
                        "{red} • {white}{0} (disabled - {1})",
                        module.getName(),
                        module.getWhen()
                ));
            }
        });
    }
}
