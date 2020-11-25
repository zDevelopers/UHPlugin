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
package eu.carrade.amaury.quartzsurvivalgames.modules.core.modules;

import com.google.common.collect.ImmutableMap;
import eu.carrade.amaury.quartzsurvivalgames.core.ModuleCategory;
import eu.carrade.amaury.quartzsurvivalgames.core.ModuleInfo;
import eu.carrade.amaury.quartzsurvivalgames.core.ModuleLoadTime;
import eu.carrade.amaury.quartzsurvivalgames.core.QSGModule;
import eu.carrade.amaury.quartzsurvivalgames.modules.core.game.GameModule;
import eu.carrade.amaury.quartzsurvivalgames.modules.core.game.GamePhase;
import eu.carrade.amaury.quartzsurvivalgames.modules.core.modules.commands.ModuleCommand;
import eu.carrade.amaury.quartzsurvivalgames.modules.core.modules.commands.ModulesCommand;
import eu.carrade.amaury.quartzsurvivalgames.modules.core.modules.commands.ConfigurationCommand;
import eu.carrade.amaury.quartzsurvivalgames.modules.core.sidebar.SidebarInjector;
import eu.carrade.amaury.quartzsurvivalgames.shortcuts.QSG;
import fr.zcraft.quartzlib.components.commands.Command;
import fr.zcraft.quartzlib.components.i18n.I;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;
import java.util.Map;


@ModuleInfo (
        name = "Modules",
        description = "Offers to the users a way to manage modules.",
        when = ModuleLoadTime.STARTUP,
        category = ModuleCategory.CORE,
        icon = Material.COMMAND_BLOCK,
        can_be_unloaded = false
)
public class ModulesModule extends QSGModule
{
    @Override
    public List<Class<? extends Command>> getCommands()
    {
        return Arrays.asList(
                ModulesCommand.class,
                ModuleCommand.class,
                ConfigurationCommand.class
        );
    }

    @Override
    public Map<String, Class<? extends Command>> getCommandsAliases()
    {
        return ImmutableMap.of(
                "modules", ModulesCommand.class,
                "config", ConfigurationCommand.class
        );
    }

    @Override
    public void injectIntoSidebar(Player player, SidebarInjector injector)
    {
        if (player.isOp() && QSG.module(GameModule.class).getPhase() == GamePhase.WAIT) // TODO Permissions
        {
            injector.injectLines(
                    true,
                    I.t("{gold}To configure the game,"),
                    I.t("{gold}use {bold}/config")
            );
        }
    }
}
