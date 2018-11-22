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
package eu.carrade.amaury.UHCReloaded.modules.core.game;

import eu.carrade.amaury.UHCReloaded.core.ModuleInfo;
import eu.carrade.amaury.UHCReloaded.core.UHModule;
import eu.carrade.amaury.UHCReloaded.modules.core.game.commands.StartCommand;
import eu.carrade.amaury.UHCReloaded.modules.core.game.events.GamePhaseChangedEvent;
import eu.carrade.amaury.UHCReloaded.modules.core.sidebar.SidebarInjector;
import fr.zcraft.zlib.components.commands.Command;
import fr.zcraft.zlib.components.i18n.I;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;


@ModuleInfo (
        name = "Game",
        description = "Manages the game execution and phases",
        settings = Config.class,
        internal = true,
        can_be_disabled = false
)
public class GameModule extends UHModule implements Listener
{
    private GamePhase phase = null;

    private final Set<UUID> players = new HashSet<>();
    private final Set<UUID> spectators = new HashSet<>();

    @Override
    protected void onEnable()
    {
        setPhase(GamePhase.WAIT);
    }

    @Override
    public List<Class<? extends Command>> getCommands()
    {
        return Collections.singletonList(StartCommand.class);
    }

    @Override
    public void injectIntoSidebar(Player player, SidebarInjector injector)
    {
        injector.injectLines(
                SidebarInjector.SidebarPriority.VERY_TOP, true,
                I.tn("{white}{0}{gray} player", "{white}{0}{gray} players", Bukkit.getOnlinePlayers().size(), Bukkit.getOnlinePlayers().size())
        );
    }

    /**
     * @return the current phase of the game.
     */
    public GamePhase getPhase()
    {
        return phase;
    }

    /**
     * Changes the phase of the game.
     *
     * @param phase The new phase (must be after the current one, else nothing is done).
     */
    public void setPhase(GamePhase phase)
    {
        if (this.phase == null || (this.phase != phase && phase.ordinal() > this.phase.ordinal()))
        {
            final GamePhase oldPhase = this.phase;

            this.phase = phase;
            Bukkit.getServer().getPluginManager().callEvent(new GamePhaseChangedEvent(oldPhase, phase));
        }
    }
}
