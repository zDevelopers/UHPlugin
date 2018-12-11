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
import eu.carrade.amaury.UHCReloaded.modules.core.game.commands.KillCommand;
import eu.carrade.amaury.UHCReloaded.modules.core.game.commands.ResurrectCommand;
import eu.carrade.amaury.UHCReloaded.modules.core.game.commands.StartCommand;
import eu.carrade.amaury.UHCReloaded.modules.core.game.events.game.GamePhaseChangedEvent;
import eu.carrade.amaury.UHCReloaded.modules.core.game.events.players.AlivePlayerDeathEvent;
import eu.carrade.amaury.UHCReloaded.modules.core.game.events.players.PlayerResurrectedEvent;
import eu.carrade.amaury.UHCReloaded.modules.core.game.events.players.TeamDeathEvent;
import eu.carrade.amaury.UHCReloaded.modules.core.game.events.start.*;
import eu.carrade.amaury.UHCReloaded.modules.core.game.submanagers.GameBeginning;
import eu.carrade.amaury.UHCReloaded.modules.core.game.teleporter.TeleportationMode;
import eu.carrade.amaury.UHCReloaded.modules.core.game.teleporter.Teleporter;
import eu.carrade.amaury.UHCReloaded.modules.core.sidebar.SidebarInjector;
import eu.carrade.amaury.UHCReloaded.modules.core.spawns.SpawnsModule;
import eu.carrade.amaury.UHCReloaded.modules.core.spectators.SpectatorsModule;
import eu.carrade.amaury.UHCReloaded.shortcuts.UR;
import eu.carrade.amaury.UHCReloaded.utils.UHSound;
import fr.zcraft.zlib.components.commands.Command;
import fr.zcraft.zlib.components.i18n.I;
import fr.zcraft.zlib.components.rawtext.RawText;
import fr.zcraft.zlib.core.ZLib;
import fr.zcraft.zlib.tools.runners.RunTask;
import fr.zcraft.zlib.tools.text.ActionBar;
import fr.zcraft.zlib.tools.text.Titles;
import fr.zcraft.zteams.ZTeam;
import fr.zcraft.zteams.ZTeams;
import fr.zcraft.zteams.colors.TeamColor;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;


@ModuleInfo (
        name = "Game",
        description = "Manages the game execution and phases",
        settings = Config.class,
        internal = true,
        can_be_disabled = false
)
public class GameModule extends UHModule implements Listener
{
    /**
     * The current game phase (initialized to {@link GamePhase#WAIT} in the
     * {@link #onEnable()} method).
     */
    private GamePhase phase = null;

    /**
     * A list containing the currently alive players.
     */
    private final Set<UUID> alivePlayers = new HashSet<>();

    /**
     * A list containing the currently (cached) alive teams.
     * Refreshed using the {@link #updateAliveTeams()} method.
     */
    private final Set<ZTeam> aliveTeams = new HashSet<>();

    /**
     * When the game ends, stores the last standing team.
     */
    private ZTeam winner = null;

    /**
     * {@code true} if there is teams in this game.
     */
    private boolean teamsGame = false;

    /**
     * {@code true} if the starting process should be slow (for small servers).
     */
    private boolean slowMode = false;

    /**
     * The teleportation process, to teleport teams as a group or apart.
     */
    private TeleportationMode teleportationMode = TeleportationMode.NORMAL;

    /**
     * The teleporter used, storing effective spawn points for each player, used
     * if we need them after the startup process.
     */
    private Teleporter teleporter = null;

    /**
     * An internal lock to avoid someone starting the game if the teleportation process
     * is still running.
     */
    private boolean teleportationProcessLock = false;

    /**
     * An internal lock used to avoid multiple starting countdowns at the same time.
     */
    private boolean startingCountdownLock = false;

    /**
     * An internal counter for the displayed teleportation progress in the action bar (x/y).
     */
    private int teleportationProgress = 0;


    @Override
    protected void onEnable()
    {
        setPhase(GamePhase.WAIT);
        Bukkit.getOnlinePlayers().forEach(this::updatePlayerFlightOptions);

        ZLib.loadComponent(GameBeginning.class);
    }

    @Override
    public List<Class<? extends Command>> getCommands()
    {
        return Arrays.asList(
                StartCommand.class,
                KillCommand.class,
                ResurrectCommand.class
        );
    }

    @Override
    public void injectIntoSidebar(Player player, SidebarInjector injector)
    {
        final List<String> topSidebar = new ArrayList<>();

        if (Config.SIDEBAR.PLAYERS.get())
        {
            topSidebar.add(I.tn(
                    "{white}{0}{gray} player", "{white}{0}{gray} players",
                    phase == GamePhase.WAIT ? Bukkit.getOnlinePlayers().size() : alivePlayers.size()
            ));
        }

        if (Config.SIDEBAR.TEAMS.get() && teamsGame && phase != GamePhase.WAIT)
        {
            topSidebar.add(I.tn("{white}{0}{gray} team", "{white}{0}{gray} teams", aliveTeams.size()));
        }

        injector.injectLines(SidebarInjector.SidebarPriority.TOP, true, topSidebar);

        switch (phase)
        {
            case WAIT:
                injector.injectLines(
                        SidebarInjector.SidebarPriority.TOP, true,
                        I.t("{gray}Waiting for players...")
                );
                break;

            case STARTING:
                injector.injectLines(
                        SidebarInjector.SidebarPriority.TOP, true,
                        I.t("{gray}The game is starting..."),
                        I.t("{gray}Please wait.")
                );
                break;
        }
    }

    public boolean isTeamsGame()
    {
        return teamsGame;
    }

    public Set<UUID> getAlivePlayersUUIDs()
    {
        return Collections.unmodifiableSet(alivePlayers);
    }

    public Set<OfflinePlayer> getAlivePlayers()
    {
        return alivePlayers.stream().map(Bukkit::getOfflinePlayer).collect(Collectors.toSet());
    }

    public int countAlivePlayers()
    {
        return alivePlayers.size();
    }

    public Set<Player> getAliveConnectedPlayers()
    {
        return alivePlayers.stream().map(Bukkit::getPlayer).filter(Objects::nonNull).filter(Player::isOnline).collect(Collectors.toSet());
    }

    public Set<ZTeam> getAliveTeams()
    {
        return Collections.unmodifiableSet(aliveTeams);
    }

    public int countAliveTeams()
    {
        return aliveTeams.size();
    }

    /**
     * @return The game's winner, if any; null else.
     */
    public ZTeam getWinner()
    {
        return winner;
    }

    public boolean isAlive(final OfflinePlayer player)
    {
        return alivePlayers.contains(player.getUniqueId());
    }

    public boolean isAlive(final UUID playerID)
    {
        return alivePlayers.contains(playerID);
    }

    public boolean isAlive(final ZTeam team)
    {
        return team.getPlayersUUID().stream().anyMatch(alivePlayers::contains);
    }

    /**
     * Kills a player.
     *
     * This method calls an event. If the event is cancelled, the player is not
     * killed.
     *
     * @param player The player to kill.
     * @return {@code true} if the player was effectively killed (event not cancelled).
     */
    public boolean kill(final OfflinePlayer player)
    {
        return kill(player, null);
    }

    /**
     * Kills a player. Internal use for natural deaths.
     *
     * This method calls an event. If the event is cancelled, the player is not
     * killed.
     *
     * @param player The player to kill.
     * @param ev The underlying death event.
     *
     * @return {@code true} if the player was effectively killed (event not cancelled).
     */
    private boolean kill(final OfflinePlayer player, final PlayerDeathEvent ev)
    {
        final AlivePlayerDeathEvent event = new AlivePlayerDeathEvent(player, ev);
        Bukkit.getPluginManager().callEvent(event);

        if (!event.isCancelled())
        {
            alivePlayers.remove(player.getUniqueId());

            updateAliveTeams();

            // We check the player's team to see if there is players left inside.
            if (teamsGame)
            {
                final ZTeam team = ZTeams.get().getTeamForPlayer(player);
                if (team != null && !aliveTeams.contains(team))
                {
                    Bukkit.getPluginManager().callEvent(new TeamDeathEvent(team));
                }
            }

            if (aliveTeams.size() <= 1)
            {
                setPhase(GamePhase.END);
            }

            return true;
        }

        return false;
    }

    /**
     * Resurrects a player and puts it into the game (even if he/she never played
     * before).
     *
     * @param player The player to resurrect.
     * @return {@code true} if the player was effectively resurrected (i.e. not already alive).
     */
    public boolean resurrect(final OfflinePlayer player)
    {
        if (isAlive(player)) return false;

        log().info("Resurrecting player {0}", player.getName());

        alivePlayers.add(player.getUniqueId());
        updateAliveTeams();

        log().info("Resurrected. Alive players: {0}. Teams: {1}. Phase: {2}.", alivePlayers.size(), aliveTeams.size(), phase);

        if (aliveTeams.size() > 1 && phase == GamePhase.END)
        {
            setPhase(GamePhase.IN_GAME);
            log().info("Going back to IN_GAME phase. Phase is now {0}.", phase);
        }

        Bukkit.getPluginManager().callEvent(new PlayerResurrectedEvent(player));

        return true;
    }

    /**
     * @return the current phase of the game.
     */
    public GamePhase getPhase()
    {
        return phase;
    }

    /**
     * Compares the current phase with the given one.
     *
     * @param phase The compared phase.
     * @return {@code true} if the current phase is strictly before this one.
     */
    public boolean currentPhaseBefore(final GamePhase phase)
    {
        return this.phase.ordinal() < phase.ordinal();
    }

    /**
     * Compares the current phase with the given one.
     *
     * @param phase The compared phase.
     * @return {@code true} if the current phase is strictly after this one.
     */
    public boolean currentPhaseAfter(final GamePhase phase)
    {
        return this.phase.ordinal() > phase.ordinal();
    }

    /**
     * Changes the phase of the game.
     *
     * The phase must be a phase after the current one, with two exceptions:
     * the phase order can be STARTING → WAIT or END → IN_GAME.
     *
     * @param phase The new phase.
     */
    public void setPhase(final GamePhase phase)
    {
        if (this.phase == null
                || (this.phase != phase && phase.ordinal() > this.phase.ordinal())
                || (this.phase == GamePhase.STARTING && phase == GamePhase.WAIT)
                || (this.phase == GamePhase.END && phase == GamePhase.IN_GAME)
        )
        {
            final GamePhase oldPhase = this.phase;

            this.phase = phase;

            log().info("Game phase changed to {0}.", phase);

            RunTask.nextTick(() -> Bukkit.getServer().getPluginManager().callEvent(new GamePhaseChangedEvent(oldPhase, phase)));
        }
    }

    public void setSlowMode(boolean slowMode)
    {
        this.slowMode = slowMode;
    }

    public boolean isSlowMode()
    {
        return slowMode;
    }

    public void setTeleportationMode(TeleportationMode teleportationMode)
    {
        this.teleportationMode = teleportationMode;
    }

    public TeleportationMode getTeleportationMode()
    {
        return teleportationMode;
    }

    public Teleporter getTeleporter()
    {
        return teleporter;
    }

    /**
     * Sets the phase to {@link GamePhase#IN_GAME} after a countdown.
     */
    public void start()
    {
        if (startingCountdownLock) return;
        if (phase != GamePhase.STARTING) throw new IllegalStateException("Cannot start the game if not in “starting” phase.");
        if (teleportationProcessLock) throw new IllegalStateException("Cannot start the game: the teleportation phase is still running.");

        startingCountdownLock = true;

        final AtomicInteger countdown = new AtomicInteger(Config.COUNTDOWN.get() + 1);
        final float[] countdownNotes = new float[] { .75f, .86f, .66f, 1, .5f, .5f, .5f };
        final AtomicInteger countdownIndex = new AtomicInteger(-1);

        RunTask.timer(new BukkitRunnable()
        {
            @Override
            public void run()
            {
                countdown.getAndDecrement();

                if (countdown.get() != 0 || !Config.STARTUP_TITLE.get())
                {
                    Titles.broadcastTitle(
                            countdown.get() == 10 ? 8 : 0,
                            countdown.get() == 0 ? 40 : 20,
                            countdown.get() == 0 ? 20 : 0,
                            (countdown.get() > 5 ? ChatColor.GREEN : (countdown.get() > 3 ? ChatColor.YELLOW : ChatColor.RED)) + countdown.toString(),
                            ""
                    );
                }
                else
                {
                    Titles.broadcastTitle(
                        0, 84, 8,
                        /// Title of title displayed when the game starts.
                        I.t("{darkgreen}Let's go!"),
                        /// Subtitle of title displayed when the game starts.
                        I.t("{green}Good luck, and have fun")
                    );
                }

                if (countdown.get() != 0)
                {
                    if (countdownIndex.incrementAndGet() == countdownNotes.length)
                    {
                        countdownIndex.set(0);
                    }

                    new UHSound(1f, countdownNotes[countdownIndex.get()], "ARROW_HIT_PLAYER", "SUCCESSFUL_HIT").broadcast();
                }
                else
                {
                    new UHSound("WITHER_DEATH").broadcast();
                }

                if (countdown.get() == 0)
                {
                    setPhase(GamePhase.IN_GAME);
                    cancel();
                }
            }
        }, 5L, 20L);
    }



    /* *** GAME STARTUP + TELEPORTATION *** */


    @EventHandler(priority = EventPriority.LOW)
    public void onGameStarting(final GamePhaseChangedEvent ev)
    {
        if (ev.getNewPhase() != GamePhase.STARTING) return;

        teleportationProcessLock = true;

        // We allow modules to prepare themselves for the teleportation phase
        // (e.g. the spawns modules can generate missing spawn points on the fly).
        Bukkit.getPluginManager().callEvent(new BeforeTeleportationPhaseEvent());

        // We determine if the game is or not with teams by counting the teams.
        teamsGame = ZTeams.get().countTeams() > 0;

        alivePlayers.clear();
        aliveTeams.clear();

        // The Team is the base unit of the game. In a solo game, there is one team per player, with one player inside.
        // In case of a teams game, we create a “wrapping” team for each alone player. Else, a team for each player.
        // These teams created on the fly are saved in case of startup fail.

        final Set<ZTeam> onTheFlyTeams = new HashSet<>();
        final Random random = new Random();

        Bukkit.getOnlinePlayers().stream()
                .filter(player -> ZTeams.get().getTeamForPlayer(player) == null)
                .filter(player -> !UR.module(SpectatorsModule.class).isSpectator(player))
                .forEach(player ->
                {
                    // We need an unique name for the team.
                    String teamName = player.getName();
                    while (ZTeams.get().isTeamRegistered(teamName))
                    {
                        teamName = player.getName() + " " + random.nextInt(1000000);
                    }

                    final ZTeam team = ZTeams.get().createTeam(
                            teamName,
                            Config.RANDOM_COLORS_IN_SOLO_GAMES.get() ? TeamColor.RANDOM : TeamColor.WHITE,
                            player
                    );

                    onTheFlyTeams.add(team);
                });


        // Loading alive players from teams, now that they are constructed and complete.

        ZTeams.get().getTeams().stream()
                .flatMap(team -> team.getPlayers().stream())
                .map(OfflinePlayer::getUniqueId)
                .filter(player -> !UR.module(SpectatorsModule.class).isSpectator(player))
                .forEach(alivePlayers::add);

        updateAliveTeams();


        // We have to check if there is enough spawn points.

        int spawnsNeeded = teleportationMode == TeleportationMode.IGNORE_TEAMS ? alivePlayers.size() : aliveTeams.size();

        if (UR.module(SpawnsModule.class).getSpawnPoints().size() < spawnsNeeded)
        {
            log().broadcastAdministrative(I.t("{ce}Unable to start the game: not enough teleportation spots."));
            log().broadcastAdministrative(I.t("{ci}You can use {cc}/uh spawns generate <random|circular|grid>{ci} to generate the missing spawns automatically."));

            /// In the sentence: "Or click here to generate the spawns randomly."
            log().broadcastAdministrative(new RawText(I.t("Or"))
                    .then(" ")
                    /// In the sentence: "Or click here to generate the spawns randomly."
                    .then(I.t("click here"))
                    .color(ChatColor.GREEN).style(ChatColor.BOLD)
                    .command("/uh spawns generate random")
                    .hover(new RawText("/uh spawns generate random"))
                    .then(" ")
                    /// In the sentence: "Or click here to generate the spawns randomly."
                    .then(I.t("to generate the spawns randomly.")).color(ChatColor.WHITE)
                    .build()
            );

            // We clears the teams created on-the-fly
            onTheFlyTeams.forEach(ZTeam::deleteTeam);

            // We set the phase back to WAIT.
            setPhase(GamePhase.WAIT);

            return;
        }


        // Preparation of the spawn points.

        final List<Location> spawnPoints = UR.module(SpawnsModule.class).getSpawnPoints();
        Collections.shuffle(spawnPoints);

        final Queue<Location> unusedSpawnPoints = new ArrayDeque<>(spawnPoints);

        teleporter = new Teleporter();

        ZTeams.get().getTeams().stream().filter(team -> !team.isEmpty()).forEach(team ->
        {
            if (teleportationMode == TeleportationMode.NORMAL && teamsGame)
            {
                // TODO re-add dynmap integration in dedicated module using events.

                final Location teamSpawn = unusedSpawnPoints.poll();

                // Should never happen
                if (teamSpawn == null)
                {
                    log().error(
                            "A fatal error occurred while starting the game: cannot set spawn point for team {0}: not enough spawn points",
                            team.getName()
                    );

                    return;
                }

                team.getPlayersUUID().forEach(player ->
                {
                    final PlayerSpawnPointSelectedEvent event = new PlayerSpawnPointSelectedEvent(
                            Bukkit.getOfflinePlayer(player), teamSpawn.clone());

                    Bukkit.getPluginManager().callEvent(event);

                    teleporter.setSpawnForPlayer(player, event.getSpawnPoint());
                });
            }
            else
            {
                team.getPlayersUUID().forEach(player ->
                {
                    // TODO re-add dynmap integration in dedicated module using events.

                    final Location playerSpawn = unusedSpawnPoints.poll();
                    final OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(player);

                    // Should never happen
                    if (playerSpawn == null)
                    {
                        log().error(
                                "A fatal error occurred while starting the game: cannot set spawn point for player {0}: not enough spawn points",
                                offlinePlayer.getName()
                        );

                        return;
                    }

                    final PlayerSpawnPointSelectedEvent event = new PlayerSpawnPointSelectedEvent(
                            offlinePlayer, playerSpawn);

                    Bukkit.getPluginManager().callEvent(event);

                    teleporter.setSpawnForPlayer(player, event.getSpawnPoint());
                });
            }
        });


        // Effective teleportation.

        teleporter
            .whenTeleportationOccurs(uuid -> Bukkit.getPluginManager().callEvent(
                    new PlayerAboutToBeTeleportedToSpawnPointEvent(Bukkit.getPlayer(uuid), teleporter.getSpawnForPlayer(uuid))))

            .whenTeleportationSuccesses(uuid -> {
                final Player player = Bukkit.getPlayer(uuid);

                log().info("Player {0} - {1} teleported to its spawn point.", uuid, player.getName());

                Bukkit.getPluginManager().callEvent(
                        new PlayerTeleportedToSpawnPointEvent(player, teleporter.getSpawnForPlayer(uuid)));
            })

            .whenTeleportationFails(uuid -> log().error("Unable to teleport player {0} - {1}", uuid, Bukkit.getPlayer(uuid).getName()))

            .whenTeleportationEnds(uuids -> {
                teleportationProcessLock = false;
                Bukkit.getPluginManager().callEvent(new AfterTeleportationPhaseEvent());
            })

            .startTeleportationProcess(slowMode);
    }

    @EventHandler (priority = EventPriority.LOWEST)
    public void onPlayerTeleportedToSpawnPoint(final PlayerTeleportedToSpawnPointEvent ev)
    {
        Player player = Bukkit.getPlayer(ev.getPlayer().getUniqueId());

        if (player != null)
        {
            player.setGameMode(GameMode.ADVENTURE);
            player.setAllowFlight(true);
            player.setFlying(true);
            player.setFlySpeed(0f);
        }

        if (Config.BROADCAST_PROGRESS.get())
        {
            teleportationProgress++;

            /// Displayed in the action bar while the slow teleportation occurs.
            final String message = I.t("{lightpurple}Teleporting... {gray}({0}/{1})", teleportationProgress, alivePlayers.size());
            Bukkit.getOnlinePlayers().forEach(onlinePlayer -> ActionBar.sendPermanentMessage(onlinePlayer, message));
        }
    }

    @EventHandler (priority = EventPriority.LOWEST)
    public void onTeleportationProcessComplete(final AfterTeleportationPhaseEvent ev)
    {
        log().broadcastAdministrative(I.t("{cs}All teams are teleported."));

        if (slowMode)
        {
            log().broadcastAdministrative(new RawText(I.t("{gray}Use {cc}/uh start{gray} or click here to start the game."))
                    .hover(new RawText(I.t("Click here to start the game")))
                    .command(StartCommand.class)
            );
        }
        else
        {
            start();
        }

        if (Config.BROADCAST_PROGRESS.get())
        {
            /// Displayed in the action bar when the slow teleportation is finished but the game not started.
            String message = I.t("{lightpurple}Teleportation complete. {gray}The game will start soon...");
            Bukkit.getOnlinePlayers().forEach(player -> ActionBar.sendPermanentMessage(player, message));
        }
    }

    @EventHandler (priority = EventPriority.LOWEST)
    public void onGameStarts(final GamePhaseChangedEvent ev)
    {
        if (ev.getNewPhase() != GamePhase.IN_GAME) return;
        if (ev.getOldPhase() != GamePhase.STARTING) return;

        Bukkit.getOnlinePlayers().forEach(ActionBar::removeMessage);

        getAliveConnectedPlayers().forEach(player -> {
            updatePlayerFlightOptions(player);

            player.setFlying(false);
            player.setAllowFlight(false);

            player.setGameMode(GameMode.SURVIVAL);

            player.setHealth(player.getMaxHealth());

            player.setFoodLevel(20);
            player.setSaturation(20);

            player.getInventory().clear();
            player.getInventory().setArmorContents(null);

            player.closeInventory();

            player.setExp(0L);
            player.setLevel(0);
        });
    }



    /* *** PLAYER OR TEAM DEATH & GAME END *** */


    @EventHandler
    public void onPlayerDeath(final PlayerDeathEvent ev)
    {
        if (phase != GamePhase.IN_GAME) return;
        if (!isAlive(ev.getEntity())) return;

        kill(ev.getEntity(), ev);
    }

    @EventHandler
    public void onPlayerKilled(final AlivePlayerDeathEvent ev)
    {
        log().info("{0} killed", ev.getPlayer().getName());
    }

    @EventHandler
    public void onPlayerResurrected(final PlayerResurrectedEvent ev)
    {
        log().info("{0} resurrected", ev.getPlayer().getName());

        /// Resurrection notification. {0} = raw resurrected player name.
        Bukkit.broadcastMessage(I.t("{gold}{0} returned from the dead!", ev.getPlayer().getName()));
    }

    @EventHandler
    public void onGameEndsOrEndsCancelled(final GamePhaseChangedEvent ev)
    {
        if (ev.getNewPhase() == GamePhase.END)
        {
            winner = aliveTeams.stream().findAny().orElse(null); // There will be one alive team left here.
        }
        else if (ev.getNewPhase() == GamePhase.IN_GAME && ev.getOldPhase() == GamePhase.END)
        {
            winner = null; // Win cancelled because a team was resurrected.
        }
    }



    /* *** OTHER PLAYERS MANAGEMENT *** */

    @EventHandler
    public void onPlayerJoin(final PlayerJoinEvent ev)
    {
        updatePlayerFlightOptions(ev.getPlayer());
    }


    /**
     * Sets the flight options for the player according to the current game phase.
     * @param player The player.
     */
    private void updatePlayerFlightOptions(final Player player)
    {
        switch (phase)
        {
            case WAIT:
                player.setFlySpeed(.1f);
                break;

            case STARTING:
                if (alivePlayers.contains(player.getUniqueId()))
                {
                    player.setAllowFlight(true);
                    player.setFlySpeed(0f);
                }
                else
                {
                    player.setFlySpeed(.1f);
                }

                break;

            case IN_GAME:
            case END:
                player.setFlySpeed(.1f);
        }
    }

    private void updateAliveTeams()
    {
        aliveTeams.clear();

        ZTeams.get().getTeams()
            .forEach(t -> t.getPlayersUUID().stream().filter(alivePlayers::contains).map(pid -> t).forEach(aliveTeams::add));
    }
}
