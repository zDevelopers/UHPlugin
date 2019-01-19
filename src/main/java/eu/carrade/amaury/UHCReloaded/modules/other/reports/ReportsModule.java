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
package eu.carrade.amaury.UHCReloaded.modules.other.reports;

import eu.carrade.amaury.UHCReloaded.UHConfig;
import eu.carrade.amaury.UHCReloaded.core.ModuleCategory;
import eu.carrade.amaury.UHCReloaded.core.ModuleInfo;
import eu.carrade.amaury.UHCReloaded.core.ModuleLoadTime;
import eu.carrade.amaury.UHCReloaded.core.UHModule;
import eu.carrade.amaury.UHCReloaded.modules.core.game.GameModule;
import eu.carrade.amaury.UHCReloaded.modules.core.game.GamePhase;
import eu.carrade.amaury.UHCReloaded.modules.core.game.events.game.GamePhaseChangedEvent;
import eu.carrade.amaury.UHCReloaded.modules.core.game.events.players.AlivePlayerDeathEvent;
import eu.carrade.amaury.UHCReloaded.modules.core.game.events.players.PlayerResurrectedEvent;
import eu.carrade.amaury.UHCReloaded.modules.core.timers.TimeDelta;
import eu.carrade.amaury.UHCReloaded.utils.CommandUtils;
import eu.carrade.amaury.UHCReloaded.shortcuts.UR;
import fr.zcraft.zlib.components.events.FutureEventHandler;
import fr.zcraft.zlib.components.events.WrappedEvent;
import fr.zcraft.zlib.components.gui.GuiUtils;
import fr.zcraft.zlib.components.i18n.I;
import fr.zcraft.zlib.components.rawtext.RawText;
import fr.zcraft.zlib.tools.PluginLogger;
import fr.zcraft.zlib.tools.reflection.Reflection;
import fr.zcraft.zlib.tools.runners.RunTask;
import fr.zcraft.zlib.tools.text.RawMessage;
import fr.zcraft.zteams.ZTeam;
import fr.zcraft.zteams.ZTeams;
import fr.zcraft.zteams.events.*;
import me.cassayre.florian.damageslogger.ReportsManager;
import me.cassayre.florian.damageslogger.report.InvalidReportException;
import me.cassayre.florian.damageslogger.report.Report;
import me.cassayre.florian.damageslogger.report.ReportEvent;
import me.cassayre.florian.damageslogger.report.ReportTeam;
import org.bukkit.*;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.BrewEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.scheduler.BukkitTask;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


@ModuleInfo (
        name = "Reports",
        short_description = "Generates reports of the game, including history, damages, " +
                "heals, statistics, etc., displayed on a web page, and gives the URL " +
                "when the match ends.",
        description = "Generates reports of the game, including history, damages, " +
                "heals, statistics, etc., displayed on a web page, and gives the URL " +
                "when the match ends.\n\n" +
                "Using DamagesLogger by Florian Cassayre & Amaury Carrade.",
        authors = "Florian Cassayre & Amaury Carrade",
        when = ModuleLoadTime.ON_GAME_START,
        category = ModuleCategory.EXTERNAL,
        icon = Material.BOOK_AND_QUILL,
        settings = Config.class,
        can_be_unloaded = false
)
public class ReportsModule extends UHModule
{
    private GameModule game;

    private Report report;

    /*
     * Switches for some timeline's events.
     */
    private boolean firstApple = false;
    private boolean firstGold = false;
    private boolean firstDiamond = false;
    private boolean firstEnchant = false;
    private boolean firstBrew = false;
    private boolean firstNether = false;
    private boolean firstEnd = false;
    private boolean firstGoldenApple = false;
    private boolean firstGoldenHead = false;  // FIXME not implemented

    /*
     * Used to know who used the brewing stands last to know to who the “first
     * brew” event should be attributed.
     */
    private Map<Location, UUID> lastBrewingStandUsers = new HashMap<>();

    /*
     * After the end of the game, we wait a bit to handle late events
     * (like final cross-kill, or fire-kill from fire aspect…).
     */
    private BukkitTask waitAfterEndTask = null;
    private TimeDelta waitAfterEndDelay = new TimeDelta(30);
    private boolean waitingfterEnd = false;


    @Override
    protected void onEnable()
    {
        game = UR.module(GameModule.class);

        ReportsManager.init(UR.get());

        if (!Config.REPORTS_API_BASE_URL.get().isEmpty())
        {
            ReportsManager.get().setRemoteInstanceURL(Config.REPORTS_API_BASE_URL.get());
        }

        report = new Report()
            .selfRegister()
            .autoTrack(true)
            .stopTrackOnDisconnection(false)

            // We'll manage theses ourselves.
            .stopTrackOnDeath(false)
            .autoTrackNewPlayers(false)
            .addDefaultEvents(false)

            .autoCollectPreviousStatistics(true)
            .registerPlayers(game.getAlivePlayers())

            .title(UHConfig.TITLE.get())

            .settings()
                .displayDate(Config.DATE.get())
                .displayPlayersCount(Config.PLAYERS_COUNT.get())
                .displayWinners(Config.WINNERS.get())
                .enableSummary(
                        Config.SUMMARY.HISTORY.get(),
                        Config.SUMMARY.PLAYERS.get(),
                        Config.SUMMARY.TEAMS.get()
                )
                .enableDamages(
                        Config.DAMAGES.DAMAGES_PER_PLAYERS.get(),
                        Config.DAMAGES.DAMAGES_PER_TEAMS.get(),
                        Config.DAMAGES.DAMAGES_FROM_ENVIRONMENT.get(),
                        Config.DAMAGES.DISPLAY_KILLER.get()
                )
                .enablePlayers(
                        Config.PLAYERS.PLAY_TIME.get(),
                        Config.PLAYERS.GLOBAL_STATISTICS.get(),
                        Config.PLAYERS.USED.get(),
                        Config.PLAYERS.MINED.get(),
                        Config.PLAYERS.PICKED_UP.get()
                )
                .highlightingTheseStatistics(
                        Config.PLAYERS.STATISTICS_HIGHLIGHT.isDefined()
                                ? Config.PLAYERS.STATISTICS_HIGHLIGHT
                                : Config.defaultStatsHighlight()
                )
                .highlightingTheseUsedItems(
                        Config.PLAYERS.USED_HIGHLIGHT.isDefined()
                                ? Config.PLAYERS.USED_HIGHLIGHT
                                : Config.defaultUsedHighlight()
                )
                .highlightingTheseMinedBlocks(
                        Config.PLAYERS.MINED_HIGHLIGHT.isDefined()
                                ? Config.PLAYERS.MINED_HIGHLIGHT
                                : Config.defaultMinedHighlight()
                )
                .highlightingThesePickedUpItems(
                        Config.PLAYERS.PICKED_UP_HIGHLIGHT.isDefined()
                                ? Config.PLAYERS.PICKED_UP_HIGHLIGHT
                                : Config.defaultPickedUpHighlight()
                )
                .withTheseInGlobalStatisticsWhitelist(Config.PLAYERS.STATISTICS_WHITELIST)
                .withTheseInUsedStatisticsWhitelist(Config.PLAYERS.USED_WHITELIST)
                .withTheseInMinedStatisticsWhitelist(Config.PLAYERS.MINED_WHITELIST)
                .withTheseInPickedUpStatisticsWhitelist(Config.PLAYERS.PICKED_UP_WHITELIST)
                .withGenerator(
                        "UHC Reloaded",
                        "https://www.spigotmc.org/resources/ultrahardcore-reloaded.1622/"
                )
            .done();

        if (!Config.SUMMARY.ENABLED.get()) report.settings().disableSummary();
        if (!Config.DAMAGES.ENABLED.get()) report.settings().disableDamages();
        if (!Config.PLAYERS.ENABLED.get()) report.settings().disablePlayers();

        updateReportTeams();

        RunTask.nextTick(() -> log().info("The reports tracker started successfully."));
    }

    public Report getReport()
    {
        return report;
    }

    private void updateReportTeams()
    {
        report.resetTeams();

        if (game.isTeamsGame())
        {
            ZTeams.get().getTeams().stream()
                    .map(team -> new ReportTeam(team.getName(), team.getColorOrWhite().toChatColor(), team.getPlayers()))
                    .forEach(report::registerTeam);
        }
    }

    @EventHandler (priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onGamePhaseChanged(final GamePhaseChangedEvent ev)
    {
        if (ev.getNewPhase() == GamePhase.END)
        {
            final ZTeam winner = game.getWinner();
            if (winner != null) report.setWinners(winner.getPlayers());

            waitingfterEnd = true;

            waitAfterEndTask = RunTask.later(() ->
            {
                waitingfterEnd = false;

                report.autoTrack(false);

                report.save(
                    saved ->
                        PluginLogger.info("The JSON report for this game has been written to {0}.", saved.getAbsolutePath()),
                    error ->
                        PluginLogger.error("Unable to save the JSON report for this game to the disk. There were backups, you may be lucky and find something there. If configured to do so, the report will still be published.", error)
                );

                if (!Config.PUBLISH_REPORT.get()) return;

                report.publish(
                    uri -> {
                        switch (Config.BROADCAST_REPORT_TO.get())
                        {
                            case ALL:
                                Bukkit.getOnlinePlayers().forEach(CommandUtils::displaySeparator);

                                Bukkit.broadcastMessage("\n " + GuiUtils.generatePrefixedFixedLengthString(
                                        " ",
                                        I.t("{aqua}{bold}Want a summary?") + ChatColor.RESET + "\n" +
                                        I.t("{darkaqua}Click the link below to see (and share, if you want) the game timeline, events, damages summary, and other statistics!")
                                ));

                                Bukkit.broadcastMessage("");

                                RawMessage.broadcast(
                                    new RawText("  ").hover(I.t("Open {aqua}{0}", uri.toString()))
                                        .then("»").style(ChatColor.DARK_AQUA, ChatColor.BOLD).uri(uri)
                                        .then("     ").uri(uri)
                                        .then(uri.toString()).style(ChatColor.AQUA).uri(uri)
                                        .then("     ").uri(uri)
                                        .then("«").style(ChatColor.DARK_AQUA, ChatColor.BOLD).uri(uri)
                                        .build()
                                );

                                Bukkit.broadcastMessage("");
                                Bukkit.getOnlinePlayers().forEach(CommandUtils::displaySeparator);

                                break;

                            case ADMINISTRATORS:
                                log().broadcastAdministrative("");
                                log().broadcastAdministrative(I.t("{darkaqua}{bold}A game report was generated"));
                                log().broadcastAdministrative(I.t("{darkaqua}You can share it using the following URL. It was not broadcast to other players."));
                                log().broadcastAdministrative("");

                                RawMessage.broadcast(
                                    new RawText().uri(uri).hover(I.t("Open {aqua}{0}", uri.toString()))
                                        .then("»  ").style(ChatColor.DARK_AQUA, ChatColor.BOLD)
                                        .then(uri.toString()).style(ChatColor.AQUA)
                                        .build()
                                );

                                break;

                            case CONSOLE:
                                log().info("A game report was generated. You can share it using the following URL: {0} .", uri);
                                break;
                        }
                    },
                    error -> {
                        final String message;

                        if (error instanceof IOException)
                        {
                            message = I.t("I/O Error: {0}", error.getLocalizedMessage());
                        }
                        else if (error instanceof InvalidReportException)
                        {
                            message = I.t("Invalid Report: {0} ({1})", error.getMessage(), ((InvalidReportException) error).getCode());
                        }
                        else message = error.getMessage();

                        log().broadcastAdministrative("");
                        log().broadcastAdministrative(I.t("{red}{bold}Unable to publish the game report"));
                        log().broadcastAdministrative(ChatColor.RED + message);
                        log().broadcastAdministrative("");
                    }
                );

            }, waitAfterEndDelay.getSeconds() * 20L);
        }
        else if (ev.getNewPhase() == GamePhase.IN_GAME && !ev.isRunningForward())
        {
            report.resetWinners();
            report.autoTrack(true);

            waitingfterEnd = false;

            if (waitAfterEndTask != null)
            {
                waitAfterEndTask.cancel();
                waitAfterEndTask = null;
            }
        }
    }

    @EventHandler (priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerDeath(final AlivePlayerDeathEvent ev)
    {
        report
            .untrackPlayer(ev.getPlayer())
            .record(ReportEvent.withPlayer(
                    ReportEvent.EventType.GOLD,
                    /// Title of the death event on the game report's timeline.
                    I.t("Death of {0}", ev.getPlayer().getName()),
                    ev.getPlayerDeathEvent() != null
                        &&  ev.getPlayerDeathEvent().getDeathMessage() != null
                        && !ev.getPlayerDeathEvent().getDeathMessage().isEmpty()
                            ? ev.getPlayerDeathEvent().getDeathMessage()
                            : null,
                    ev.getPlayer()
            ));
    }

    @EventHandler (priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerDeath(final PlayerDeathEvent ev)
    {
        // Recording cross-kills and other deaths after the game end (i.e. from fire)
        // during a 30-seconds period.

        if (!waitingfterEnd) return;

        report
                .untrackPlayer(ev.getEntity())
                .record(ReportEvent.withPlayer(
                        ReportEvent.EventType.GOLD,
                        /// Title of the death event on the game report's timeline.
                        I.t("Death of {0}", ev.getEntity().getName()),
                        !ev.getDeathMessage().isEmpty() ? ev.getDeathMessage() : null,
                        ev.getEntity()
                ));
    }

    @EventHandler (priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerResurrected(final PlayerResurrectedEvent ev)
    {
        report
            .trackPlayer(ev.getPlayer())
            .record(ReportEvent.withPlayer(
                    ReportEvent.EventType.GREEN,
                    /// Title of the resurrection event on the game report's timeline.
                    I.t("{0} was resurrected", ev.getPlayer().getName()),
                    ev.getPlayer()
            ));
        updateReportTeams();
    }

    @EventHandler (priority = EventPriority.MONITOR) public void onTeamsChange(final TeamUpdatedEvent ev)      { updateReportTeams(); }
    @EventHandler (priority = EventPriority.MONITOR) public void onTeamsChange(final TeamRegisteredEvent ev)   { updateReportTeams(); }
    @EventHandler (priority = EventPriority.MONITOR) public void onTeamsChange(final TeamUnregisteredEvent ev) { updateReportTeams(); }
    @EventHandler (priority = EventPriority.MONITOR) public void onTeamsChange(final PlayerJoinedTeamEvent ev) { updateReportTeams(); }
    @EventHandler (priority = EventPriority.MONITOR) public void onTeamsChange(final PlayerLeftTeamEvent ev)   { updateReportTeams(); }

    @EventHandler (priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onFirstEnchant(final EnchantItemEvent ev)
    {
        if (firstEnchant || !game.isAlive(ev.getEnchanter())) return;

        report.record(ReportEvent.withIcon(
            I.t("{0} enchants the first tool", ev.getEnchanter().getName()),
            "item-book-enchanted"
        ));

        firstEnchant = true;
    }

    @EventHandler (priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBrewingStandUsed(final PlayerInteractEvent ev)
    {
        if (firstBrew
                || !game.isAlive(ev.getPlayer())
                || !ev.hasBlock()
                || ev.getAction() != Action.RIGHT_CLICK_BLOCK
                || ev.getClickedBlock().getType() != Material.BREWING_STAND)
        {
            return;
        }

        lastBrewingStandUsers.put(ev.getClickedBlock().getLocation(), ev.getPlayer().getUniqueId());
    }

    @EventHandler (priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onFirstBrew(final BrewEvent ev)
    {
        if (firstBrew) return;

        final UUID player = lastBrewingStandUsers.get(ev.getBlock().getLocation());
        if (player == null) return;

        report.record(ReportEvent.withIcon(
                I.t("{0} brewed the first potion", Bukkit.getOfflinePlayer(player).getName()),
                "item-brewing-stand"
        ));

        firstBrew = true;

        lastBrewingStandUsers.clear();
        lastBrewingStandUsers = null;
    }

    private void onFirstItemCollected(final Material item, final OfflinePlayer player)
    {
        if (!game.isAlive(player)) return;

        if (!firstDiamond && (item == Material.DIAMOND || item == Material.DIAMOND_ORE))
        {
            report.record(ReportEvent.withIcon(
                    I.t("{0} finds the first diamond", player.getName()),
                    "item-diamond"
            ));

            firstDiamond = true;
        }

        else if (!firstGold && (item == Material.GOLD_INGOT || item == Material.GOLD_ORE))
        {
            report.record(ReportEvent.withIcon(
                    I.t("{0} finds the first gold ingot", player.getName()),
                    "item-gold-ingot"
            ));

            firstGold = true;
        }

        else if (!firstApple && item == Material.APPLE)
        {
            report.record(ReportEvent.withIcon(
                    I.t("{0} harvested the first apple", player.getName()),
                    ""
            ));

            firstApple = true;
        }
    }

    /*
     * This event is deprecated. We load it as a future event, so when it will be removed,
     * the plugin will not break. We conjointly listen for the new event below, also
     * as a future event so the plugin can run in older Minecraft versions where the new
     * event isn't there yet.
     *
     * The event will be called twice on intermediate versions, but the flag will ensure only
     * one event will be recorded for each item.
     */
    @FutureEventHandler (event = "entity.PlayerPickupItemEvent", priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onOldFirstItemCollected(final WrappedEvent ev)
    {
        try
        {
            onFirstItemCollected(
                    ((Item) Reflection.call(ev.getEvent(), "getItem")).getItemStack().getType(),
                    ((Player) Reflection.call(ev.getEvent(), "getPlayer"))
            );
        }
        catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e)
        {
            PluginLogger.error("Unable to retrieve picked-up item to build the report. Will be ignored.", e);
        }
    }

    @FutureEventHandler(event = "entity.EntityPickupItemEvent", priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onFirstItemCollected(final WrappedEvent ev)
    {
        try
        {
            LivingEntity entity = (LivingEntity) Reflection.call(ev.getEvent(), "getEntity");

            if (entity instanceof Player)
            {
                onFirstItemCollected(
                        ((Item) Reflection.call(ev.getEvent(), "getItem")).getItemStack().getType(),
                        (Player) entity
                );
            }
        }
        catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e)
        {
            PluginLogger.error("Unable to retrieve picked-up item to build the report. Will be ignored.", e);
        }
    }

    @EventHandler (priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onWorldChange(final PlayerChangedWorldEvent ev)
    {
        if (!game.isAlive(ev.getPlayer())) return;

        if (!firstNether && ev.getPlayer().getWorld().getName().equals(UR.get().getWorld(World.Environment.NETHER).getName()))
        {
            report.record(ReportEvent.withIcon(
                    ReportEvent.EventType.RED,
                    I.t("{0} enters first the Nether", ev.getPlayer().getName()),
                    "block-portal"
            ));

            firstNether = true;
        }

        else if (!firstEnd && ev.getPlayer().getWorld().getName().equals(UR.get().getWorld(World.Environment.THE_END).getName()))
        {
            report.record(ReportEvent.withIcon(
                    I.t("{0} enters first The End", ev.getPlayer().getName()),
                    "block-endframe"
            ));

            firstEnd = true;
        }
    }

    @EventHandler (priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onItemConsumed(final PlayerItemConsumeEvent ev)
    {
        if (firstGoldenApple || !game.isAlive(ev.getPlayer()) || ev.getItem().getType() != Material.GOLDEN_APPLE)
            return;

        report.record(ReportEvent.withIcon(
                I.t("{0} used the first golden apple", ev.getPlayer().getName()),
                "item-apple-golden"
        ));

        firstGoldenApple = true;
    }
}
