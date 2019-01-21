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
package eu.carrade.amaury.UHCReloaded.modules.cosmetics.episodes;

import eu.carrade.amaury.UHCReloaded.core.ModuleCategory;
import eu.carrade.amaury.UHCReloaded.core.ModuleInfo;
import eu.carrade.amaury.UHCReloaded.core.ModuleLoadTime;
import eu.carrade.amaury.UHCReloaded.core.UHModule;
import eu.carrade.amaury.UHCReloaded.modules.core.game.GameModule;
import eu.carrade.amaury.UHCReloaded.modules.core.game.GamePhase;
import eu.carrade.amaury.UHCReloaded.modules.core.sidebar.SidebarInjector;
import eu.carrade.amaury.UHCReloaded.modules.core.sidebar.SidebarInjector.SidebarPriority;
import eu.carrade.amaury.UHCReloaded.modules.core.timers.Timer;
import eu.carrade.amaury.UHCReloaded.modules.core.timers.TimersModule;
import eu.carrade.amaury.UHCReloaded.modules.core.timers.events.TimerEndsEvent;
import eu.carrade.amaury.UHCReloaded.modules.cosmetics.playerListHeaderFooter.PlayerListHeaderFooterModule;
import eu.carrade.amaury.UHCReloaded.modules.cosmetics.episodes.commands.ShiftCommand;
import eu.carrade.amaury.UHCReloaded.modules.cosmetics.episodes.events.EpisodeChangedEvent;
import eu.carrade.amaury.UHCReloaded.modules.cosmetics.episodes.events.EpisodeChangedEvent.EpisodeChangedCause;
import eu.carrade.amaury.UHCReloaded.shortcuts.UR;
import fr.zcraft.zlib.components.commands.Command;
import fr.zcraft.zlib.components.i18n.I;
import fr.zcraft.zlib.tools.text.Titles;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

import java.util.Collections;
import java.util.List;


@ModuleInfo (
        name = "Episodes",
        description = "Displays time marks every 20 minutes (by default), e.g. to divide a recording for diffusion.",
        when = ModuleLoadTime.ON_GAME_START,
        category = ModuleCategory.COSMETICS,
        icon = Material.SIGN,
        settings = Config.class,
        can_be_loaded_late = false
)
public class EpisodesModule extends UHModule
{
    private final GameModule game = UR.game();
    private final TimersModule timers = UR.module(TimersModule.class);
    private final PlayerListHeaderFooterModule headFoot = UR.module(PlayerListHeaderFooterModule.class);

    private Timer episodesTimer;
    private int episode = 1;

    @Override
    protected void onEnable()
    {
        episodesTimer = new Timer("Episodes");
        episodesTimer.setDuration(Config.LENGTH.get());
        episodesTimer.setSystem(true);

        timers.registerTimer(episodesTimer);

        episodesTimer.setDisplayed(true);
        episodesTimer.setNameDisplayed(false);

        episodesTimer.start();

        if (headFoot != null)
        {
            headFoot.registerPlaceholder("episodeText", () -> I.t("Episode {0}", episode));
            headFoot.registerPlaceholder("episodeNumber", () -> String.valueOf(episode));
        }
    }

    @Override
    protected void onDisable()
    {
        episodesTimer.setDisplayed(false);
        timers.unregisterTimer(episodesTimer);
    }

    @Override
    public List<Class<? extends Command>> getCommands()
    {
        return Collections.singletonList(ShiftCommand.class);
    }

    @Override
    public void injectIntoSidebar(Player player, SidebarInjector injector)
    {
        if (!Config.DISPLAY_IN_SIDEBAR.get() || game.currentPhaseBefore(GamePhase.IN_GAME)) return;

        injector.injectLines(SidebarPriority.VERY_TOP, true, I.t("{gray}Episode {white}{0}", episode));
    }

    @EventHandler
    public void onTimerEnds(final TimerEndsEvent ev)
    {
        if (!ev.getTimer().equals(episodesTimer)) return;

        ev.setRestart(true);
        shift(false, null, episode + 1);
    }

    /**
     * Used to broadcast the episode change.
     */
    @EventHandler
    public void onEpisodeChange(final EpisodeChangedEvent ev)
    {
        final String message;

        if (ev.getCause() == EpisodeChangedEvent.EpisodeChangedCause.SHIFTED)
        {
            final String shifterName = ev.getShifter() instanceof ConsoleCommandSender ? I.t("the console") : ev.getShifter().getName();
            message = I.t("{aqua}-------- End of episode {0} [forced by {1}] --------", ev.getOldEpisode(), shifterName);
        }
        else
        {
            message = I.t("{aqua}-------- End of episode {0} --------", String.valueOf(ev.getOldEpisode()));
        }

        Bukkit.broadcastMessage(message);


        // Broadcasts title
        if (Config.DISPLAY_TITLE.get())
        {
            Titles.broadcastTitle(
                    5, 32, 8,
                    /// The title displayed when the episode change. {0} = new episode number; {1} = old.
                    I.t("{darkaqua}Episode {aqua}{0}", ev.getNewEpisode(), ev.getOldEpisode()),
                    ""
            );
        }


        // Update headers & footers
        if (headFoot != null)
        {
            headFoot.update();
        }
    }

    public int getEpisode()
    {
        return episode;
    }

    public void shift(final CommandSender shifter, final int newEpisode)
    {
        shift(true, shifter != null ? shifter : Bukkit.getConsoleSender(), newEpisode);
    }

    public void shift(final CommandSender shifter)
    {
        shift(shifter, episode + 1);
    }

    private void shift(final boolean forced, final CommandSender shifter, final int newEpisode)
    {
        final int oldEpisode = episode;
        episode = newEpisode;

        // If not forced, the timer is restarted in the event, else we have to restart
        // it manually.
        if (forced)
        {
            episodesTimer.start();
        }

        Bukkit.getPluginManager().callEvent(
                new EpisodeChangedEvent(
                        episode,
                        oldEpisode,
                        forced ? EpisodeChangedCause.SHIFTED : EpisodeChangedCause.FINISHED,
                        shifter
                )
        );
    }
}
