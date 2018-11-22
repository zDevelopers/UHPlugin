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
package eu.carrade.amaury.UHCReloaded.core;

import eu.carrade.amaury.UHCReloaded.modules.core.sidebar.SidebarInjector;
import fr.zcraft.zlib.components.commands.Command;
import fr.zcraft.zlib.components.scoreboard.Sidebar;
import fr.zcraft.zlib.core.ZLibComponent;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import java.util.List;
import java.util.Map;


public abstract class UHModule extends ZLibComponent implements Listener
{
    protected ModuleLogger logger;

    public UHModule()
    {
        logger = new ModuleLogger(getClass());
    }

    /**
     * Called when the configuration is loaded.
     */
    public void onConfigLoaded() {}

    /**
     * Use this method to register sub-commands into the /uh command.
     *
     * @return A list of command classes to register.
     */
    public List<Class<? extends Command>> getCommands() { return null; }

    /**
     * Use this method to register new commands aliases.
     *
     * @return The command aliases to add, map from the alias to the command class.
     */
    public Map<String, Class<? extends Command>> getCommandsAliases() { return null; }

    /**
     * This method will be called before calling {@link #injectIntoSidebar(Player, SidebarInjector)} so you can
     * prepare and cache lines shared between all players.
     *
     * Be careful as this method will be called every ten ticks.
     *
     * @see Sidebar#preRender()
     */
    public void prepareInjectionIntoSidebar() {}

    /**
     * Use this method to inject content into the game's sidebar.
     *
     * Be careful as this method will be called every ten ticks for each player.
     *
     * @param injector The injector will allows you to inject content in specific parts of the sidebar.
     */
    public void injectIntoSidebar(final Player player, final SidebarInjector injector) {}

    public ModuleLogger log()
    {
        return logger;
    }
}
