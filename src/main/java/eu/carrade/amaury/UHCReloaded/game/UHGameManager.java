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
package eu.carrade.amaury.UHCReloaded.game;

import eu.carrade.amaury.UHCReloaded.UHCReloaded;
import eu.carrade.amaury.UHCReloaded.events.EpisodeChangedCause;
import eu.carrade.amaury.UHCReloaded.events.UHEpisodeChangedEvent;
import eu.carrade.amaury.UHCReloaded.events.UHGameStartsEvent;
import eu.carrade.amaury.UHCReloaded.events.UHPlayerResurrectedEvent;
import eu.carrade.amaury.UHCReloaded.i18n.I18n;
import eu.carrade.amaury.UHCReloaded.misc.ProTipsSender;
import eu.carrade.amaury.UHCReloaded.task.FireworksOnWinnersTask;
import eu.carrade.amaury.UHCReloaded.teams.TeamColor;
import eu.carrade.amaury.UHCReloaded.teams.TeamManager;
import eu.carrade.amaury.UHCReloaded.teams.UHTeam;
import eu.carrade.amaury.UHCReloaded.timers.UHTimer;
import eu.carrade.amaury.UHCReloaded.utils.UHSound;
import eu.carrade.amaury.UHCReloaded.utils.UHUtils;
import fr.zcraft.zlib.tools.Callback;
import fr.zcraft.zlib.tools.runners.RunTask;
import fr.zcraft.zlib.tools.text.ActionBar;
import org.bukkit.Achievement;
import org.bukkit.Bukkit;
import org.bukkit.Difficulty;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Random;
import java.util.Set;
import java.util.UUID;


public class UHGameManager
{
    private final Boolean RANDOM_COLORS_IN_SOLO;
    private final Boolean BROADCAST_SLOW_START_PROGRESS;
    private final UHSound DEATH_SOUND;

    private UHCReloaded p = null;
    private TeamManager tm = null;
    private I18n i = null;
    private Random random = null;

    private Boolean damageIsOn = false;

    private HashSet<String> players = new HashSet<>(); // Will be converted to UUID when a built-in API for name->UUID conversion will be available
    private HashSet<UUID> alivePlayers = new HashSet<>();
    private HashSet<UHTeam> aliveTeams = new HashSet<>();
    private HashSet<UUID> spectators = new HashSet<>();
    private Map<UUID, Location> deathLocations = new HashMap<>();

    private HashSet<String> deadPlayersToBeResurrected = new HashSet<>(); // Same

    private Integer alivePlayersCount = 0;
    private Integer aliveTeamsCount = 0;

    private Boolean gameWithTeams = true;

    // Used for the slow start.
    private Boolean slowStartInProgress = false;
    private Boolean slowStartTPFinished = false;

    private Boolean gameStarted = false;
    private Boolean gameFinished = false;
    private Integer episode = 0;

    private Teleporter teleporter = null;

    // Used to send a contextual error message in UHCommandManager, using only one exception,
    // by checking the message. (Used in this.finishGame().)
    public final static String FINISH_ERROR_NOT_STARTED = "Unable to finish the game: the game is not started";
    public final static String FINISH_ERROR_NOT_FINISHED = "Unable to finish the game: the game is not finished";


    public UHGameManager(UHCReloaded plugin)
    {
        this.p = plugin;
        this.i = p.getI18n();
        this.tm = p.getTeamManager();

        this.random = new Random();


        // Loads the config
        RANDOM_COLORS_IN_SOLO = p.getConfig().getBoolean("teams-options.randomColors");
        BROADCAST_SLOW_START_PROGRESS = p.getConfig().getBoolean("start.slow.broadcastProgress");
        DEATH_SOUND = new UHSound(p.getConfig().getConfigurationSection("death.announcements.sound"));
    }

    /**
     * Initializes the environment before the start of the game.
     */
    public void initEnvironment()
    {
        p.getServer().getWorlds().get(0).setGameRuleValue("doDaylightCycle", "false");
        p.getServer().getWorlds().get(0).setTime(6000L);
        p.getServer().getWorlds().get(0).setStorm(false);
        p.getServer().getWorlds().get(0).setDifficulty(Difficulty.HARD);
    }

    /**
     * Initializes the given player.
     *
     * - Teleportation to the default world's spawn point.
     * - Max food level & health.
     * - Scoreboard.
     * - Fixed health score.
     * - Spectate mode disabled.
     * - Gamemode: creative (if permission "uh.build" granted) or adventure (else).
     *
     * @param player
     */
    public void initPlayer(final Player player)
    {

        if (p.getConfig().getBoolean("teleportToSpawnIfNotStarted"))
        {
            Location l = player.getWorld().getSpawnLocation().add(0.5, 0.5, 0.5);
            if (!UHUtils.safeTP(player, l))
            {
                player.teleport(l.add(0, 1, 0));
            }
        }

        player.setFoodLevel(20);
        player.setSaturation(20f);
        player.setHealth(20d);

        p.getScoreboardManager().setScoreboardForPlayer(player);

        // Used to update the "health" objective, to avoid a null one.
        // Launched later because else, the health is constantly set to 20,
        // and this prevents the health score to be updated.
        Bukkit.getScheduler().runTaskLater(p, new Runnable()
        {
            @Override
            public void run()
            {
                p.getScoreboardManager().updateHealthScore(player);
            }
        }, 20L);

        // Disable the spectator mode if the game is not started.
        if (p.getSpectatorPlusIntegration().isSPIntegrationEnabled())
        {
            p.getSpectatorPlusIntegration().getSPAPI().setSpectating(player, false);
        }

        // Resets the achievements
        if (p.getConfig().getBoolean("achievements.resetAchievementsAtStartup", true))
        {
            player.removeAchievement(Achievement.OPEN_INVENTORY);
        }

        // If the user has the permission to build before the game, he will probably needs
        // the creative mode.
        if (!player.hasPermission("uh.build"))
        {
            player.setGameMode(GameMode.ADVENTURE);
        }
        else
        {
            player.setGameMode(GameMode.CREATIVE);
        }
    }


    /**
     * Starts the game. - Teleports the teams - Changes the gamemode, reset the life, clear
     * inventories, etc. - Launches the timer
     *
     * @param sender      The player who launched the game.
     * @param slow        If true, the slow mode is enabled. With the slow mode, the players are, at
     *                    first, teleported team by team with a configurable delay, and with the
     *                    fly. Then, the fly is removed and the game starts.
     * @param ignoreTeams If true, the players will be teleported in individual teleportation spots,
     *                    just like without teams, even with teams.
     *
     * @throws IllegalStateException if the game is running.
     */
    public void start(final CommandSender sender, final Boolean slow, Boolean ignoreTeams) throws IllegalStateException
    {
        if (isGameRunning())
        {
            throw new IllegalStateException("The game is currently running!");
        }

        p.getMOTDManager().updateMOTDDuringStart();


        /** Initialization of the teams **/

        alivePlayers.clear();
        aliveTeams.clear();
        alivePlayersCount = 0;
        aliveTeamsCount = 0;

        // Stores the teams created on-the-fly, to unregister them if something bad happens.
        Set<UHTeam> onTheFlyTeams = new HashSet<>();

        // If there isn't any team, we add all players (startup spectators excluded) to a new solo team.
        if (tm.getTeams().isEmpty())
        {
            gameWithTeams = false;

            for (Player player : Bukkit.getOnlinePlayers())
            {
                if (!spectators.contains(player.getUniqueId()))
                {
                    UHTeam team = new UHTeam(player.getName(), RANDOM_COLORS_IN_SOLO ? TeamColor.RANDOM : null);

                    team.addPlayer(player, true);

                    tm.addTeam(team);
                    onTheFlyTeams.add(team);
                }
            }
        }

        // Else, every non-startup-spectator out of any team player is added to a solo team.
        else
        {
            gameWithTeams = true;

            for (Player player : Bukkit.getOnlinePlayers())
            {
                if (!spectators.contains(player.getUniqueId()) && tm.getTeamForPlayer(player) == null)
                {
                    // We need an unique name for the team.
                    String teamName = player.getName();
                    while (tm.isTeamRegistered(teamName))
                    {
                        teamName = player.getName() + " " + random.nextInt(1000000);
                    }

                    UHTeam team = new UHTeam(teamName, RANDOM_COLORS_IN_SOLO ? TeamColor.RANDOM : null);

                    team.addPlayer(player, true);

                    tm.addTeam(team);
                    onTheFlyTeams.add(team);
                }
            }
        }


        /** Initialization of the players **/

        for (UHTeam team : tm.getTeams())
        {
            for (OfflinePlayer player : team.getPlayers())
            {
                if (!spectators.contains(player.getUniqueId()))
                {
                    alivePlayers.add(player.getUniqueId());
                }
            }
        }

        updateAliveCache();


        /** Spawns check **/

        Integer spawnsNeeded = ignoreTeams ? alivePlayersCount : aliveTeamsCount;

        if (p.getSpawnsManager().getSpawnPoints().size() < spawnsNeeded)
        {
            sender.sendMessage(i.t("start.notEnoughTP"));

            // We clears the teams created on-the-fly
            for (UHTeam team : onTheFlyTeams)
            {
                tm.removeTeam(team, true);
            }

            aliveTeamsCount = 0;
            alivePlayersCount = 0;
            return;
        }


        /** Initialization of the spectator mode **/

        if (p.getSpectatorPlusIntegration().isSPIntegrationEnabled())
        {
            for (Player player : Bukkit.getOnlinePlayers())
            {
                p.getSpectatorPlusIntegration().getSPAPI().setSpectating(player, spectators.contains(player.getUniqueId()));
            }
        }


        /** Teleportation **/

        teleporter = new Teleporter();

        List<Location> spawnPoints = new ArrayList<>(p.getSpawnsManager().getSpawnPoints());
        Collections.shuffle(spawnPoints);

        Queue<Location> unusedTP = new ArrayDeque<>(spawnPoints);

        for (final UHTeam team : tm.getTeams())
        {
            if (team.isEmpty()) continue;

            if (!ignoreTeams)
            {
                Location teamSpawn = unusedTP.poll();

                for (UUID player : team.getPlayersUUID())
                {
                    teleporter.setSpawnForPlayer(player, teamSpawn);
                }
            }
            else
            {
                for (UUID player : team.getPlayersUUID())
                {
                    teleporter.setSpawnForPlayer(player, unusedTP.poll());
                }
            }
        }

        if (slow)
        {
            slowStartInProgress = true;
            slowStartTPFinished = false;

            // The players are frozen during the start.
            p.getFreezer().setGlobalFreezeState(true, false);

            // A simple information, because this start is slower (yeah, Captain Obvious here)
            p.getServer().broadcastMessage(i.t("start.teleportationInProgress"));

            teleporter.whenTeleportationOccurs(new Callback<UUID>()
            {
                private int teleported = 0;
                private int total = alivePlayersCount;

                @Override
                public void call(UUID uuid)
                {
                    teleported++;

                    if (BROADCAST_SLOW_START_PROGRESS)
                    {
                        String message = i.t("start.teleportationInProgressInActionBar", teleported, total);
                        for (Player player : Bukkit.getOnlinePlayers())
                        {
                            ActionBar.sendPermanentMessage(player, message);
                        }
                    }
                }
            });
        }

        teleporter
                .whenTeleportationSuccesses(new Callback<UUID>()
                {
                    @Override
                    public void call(UUID uuid)
                    {
                        final Player player = Bukkit.getPlayer(uuid);

                        if (slow)
                        {
                            sender.sendMessage(i.t("start.startSlowTP", player.getName()));

                            RunTask.nextTick(new Runnable() {
                                @Override
                                public void run()
                                {
                                    player.setAllowFlight(true);
                                    player.setFlying(true);
                                }
                            });
                        }

                        player.setGameMode(GameMode.SURVIVAL);

                        player.setHealth(20D);
                        player.setFoodLevel(20);
                        player.setSaturation(20);
                        player.getInventory().clear();
                        player.getInventory().setArmorContents(null);
                        player.setExp(0L);
                        player.setLevel(0);
                        player.closeInventory();

                        for (PotionEffect effect : player.getActivePotionEffects())
                        {
                            player.removePotionEffect(effect.getType());
                        }

                        player.setCompassTarget(player.getWorld().getSpawnLocation());
                    }
                })

                .whenTeleportationFails(new Callback<UUID>()
                {
                    @Override
                    public void call(UUID uuid)
                    {

                    }
                })

                .whenTeleportationEnds(new Callback<Set<UUID>>()
                {
                    @Override
                    public void call(Set<UUID> uuids)
                    {
                        if (slow)
                        {
                            slowStartTPFinished = true;

                            try
                            {
                                sender.sendMessage(i.t("start.startSlowAllTeamsTP"));
                                sender.sendMessage(i.t("start.startSlowAllTeamsTPCmd"));
                            }
                            catch (NullPointerException ignored) {}

                            if (BROADCAST_SLOW_START_PROGRESS)
                            {
                                String message = i.t("start.teleportationFinishedInActionBar");
                                for (Player player : Bukkit.getOnlinePlayers())
                                {
                                    ActionBar.sendPermanentMessage(player, message);
                                }
                            }
                        }
                        else
                        {
                            startEnvironment();
                            startTimer();
                            scheduleDamages();
                            sendStartupProTips();
                            finalizeStart();
                        }
                    }
                })

                .startTeleportationProcess(slow);
    }

    /**
     * Finalizes the start of the game, with the slow mode. Removes the fly and ends the start
     * (environment, timer...)
     *
     * @param sender The sender of the {@code /uh start slow go} command
     */
    public void finalizeStartSlow(CommandSender sender)
    {
        if (!slowStartInProgress)
        {
            sender.sendMessage(i.t("start.startSlowBeforeStartSlowGo"));
            return;
        }

        if (!slowStartTPFinished)
        {
            sender.sendMessage(i.t("start.startSlowWaitBeforeGo"));
            return;
        }

        // The freeze is removed.
        p.getFreezer().setGlobalFreezeState(false, false);

        // The fly is removed to everyone
        for (Player player : p.getServer().getOnlinePlayers())
        {
            if (alivePlayers.contains(player.getUniqueId()))
            {
                player.setFlying(false);
                player.setAllowFlight(false);
            }
        }

        // The action bar is cleared
        if (BROADCAST_SLOW_START_PROGRESS)
        {
            for (Player player : Bukkit.getOnlinePlayers())
            {
                ActionBar.removeMessage(player);
            }
        }

        // The environment is initialized, the game is started.
        startEnvironment();
        startTimer();
        scheduleDamages();
        sendStartupProTips();
        finalizeStart();

        slowStartInProgress = false;
    }

    /**
     * Initializes the environment at the beginning of the game.
     */
    private void startEnvironment()
    {
        World w = p.getServer().getWorlds().get(0);

        w.setGameRuleValue("doDaylightCycle", ((Boolean) p.getConfig().getBoolean("daylightCycle.do")).toString());
        w.setGameRuleValue("keepInventory", Boolean.FALSE.toString()); // Just in case...

        w.setTime(p.getConfig().getLong("daylightCycle.time"));
        w.setStorm(false);
        w.setDifficulty(Difficulty.HARD);
    }

    /**
     * Launches the timer by launching the task that updates the scoreboard every second.
     */
    private void startTimer()
    {
        if (p.getConfig().getBoolean("episodes.enabled"))
        {
            this.episode = 1;

            // An empty string is used for the name of the main timer, because
            // such a name can't be used by players.
            UHTimer mainTimer = new UHTimer("");
            mainTimer.setDuration(this.getEpisodeLength());

            p.getTimerManager().registerMainTimer(mainTimer);

            mainTimer.start();
        }
    }

    /**
     * Enables the damages 30 seconds (600 ticks) later.
     */
    private void scheduleDamages()
    {
        // 30 seconds later, damages are enabled.
        RunTask.later(new Runnable()
        {
            @Override
            public void run()
            {
                damageIsOn = true;
            }
        }, 600L);
    }

    /**
     * Sends two ProTips: - about the team chat, to all players, 20 seconds after the beginning of
     * the game; - about the invincibility, 5 seconds after the beginning of the game.
     */
    private void sendStartupProTips()
    {
        // Team chat - 20 seconds after
        if (this.isGameWithTeams())
        {
            Bukkit.getScheduler().runTaskLater(p, new Runnable()
            {
                @Override
                public void run()
                {
                    for (Player player : getOnlineAlivePlayers())
                    {
                        p.getProtipsSender().sendProtip(player, ProTipsSender.PROTIP_USE_T_COMMAND);
                    }
                }
            }, 400L);
        }

        // Invincibility - 5 seconds after
        Bukkit.getScheduler().runTaskLater(p, new Runnable()
        {
            @Override
            public void run()
            {
                for (Player player : getOnlineAlivePlayers())
                {
                    p.getProtipsSender().sendProtip(player, ProTipsSender.PROTIP_STARTUP_INVINCIBILITY);
                }
            }
        }, 100L);
    }

    /**
     * Changes the state of the game. Also, forces the global freeze start to false, to avoid toggle
     * bugs (like inverted state).
     */
    private void finalizeStart()
    {
        p.getFreezer().setGlobalFreezeState(false);

        this.gameStarted = true;
        this.gameFinished = false;

        updateAliveCache();

        // Fires the event
        p.getServer().getPluginManager().callEvent(new UHGameStartsEvent());
    }

    /**
     * Returns true if the slow start is in progress.
     *
     * @return
     */
    public boolean isSlowStartInProgress()
    {
        return slowStartInProgress;
    }

    /**
     * Updates the cached values of the alive players and teams.
     */
    public void updateAliveCache()
    {
        // Alive teams
        aliveTeams.clear();
        for (UHTeam t : tm.getTeams())
        {
            for (UUID pid : t.getPlayersUUID())
            {
                if (!this.isPlayerDead(pid)) aliveTeams.add(t);
            }
        }

        // Counters
        this.alivePlayersCount = alivePlayers.size();
        this.aliveTeamsCount = aliveTeams.size();

        if (isGameRunning())
            p.getMOTDManager().updateMOTDDuringGame();
    }

    /**
     * Updates the cached values of the alive players and teams.
     *
     * @deprecated Use {@link #updateAliveCache()} instead.
     */
    @Deprecated
    public void updateAliveCounters() { updateAliveCache(); }


    /**
     * Shifts an episode.
     *
     * @param shifter The player who shifts the episode, an empty string if the episode is shifted
     *                because the timer is up.
     */
    public void shiftEpisode(String shifter)
    {
        if (p.getConfig().getBoolean("episodes.enabled"))
        {
            this.episode++;

            EpisodeChangedCause cause;
            if (shifter.equals("")) cause = EpisodeChangedCause.FINISHED;
            else cause = EpisodeChangedCause.SHIFTED;

            // Restarts the timer.
            // Useless for a normal start (restarted in the event), but needed if the episode was shifted.
            if (cause == EpisodeChangedCause.SHIFTED)
            {
                p.getTimerManager().getMainTimer().start();
            }

            p.getServer().getPluginManager().callEvent(new UHEpisodeChangedEvent(episode, cause, shifter));
        }
    }

    /**
     * Shift an episode because the timer is up.
     */
    public void shiftEpisode()
    {
        shiftEpisode("");
    }


    /**
     * Resurrects an offline player. The tasks that needed to be executed when the player is online
     * are delayed and executed when the player joins.
     *
     * @param playerName The name of the player to resurrect
     *
     * @return true if the player was dead, false otherwise.
     */
    public boolean resurrect(String playerName)
    {
        Player playerOnline = Bukkit.getPlayer(playerName);

        if (playerOnline != null && playerOnline.isOnline())
        {
            return resurrectPlayerOnlineTask(playerOnline);
        }
        else
        {
            // We checks if the player was a player
            if (!this.players.contains(playerName))
            {
                return false;
            }
        }

        // So, now, we are sure that the player is really offline.
        // The task needed to be executed will be executed when the player join.
        this.deadPlayersToBeResurrected.add(playerName);

        return true;
    }

    /**
     * The things that have to be done in order to resurrect the players and that needs the player
     * to be online.
     *
     * @param player The player to resurrect
     *
     * @return true if the player was dead, false otherwise.
     */
    public boolean resurrectPlayerOnlineTask(Player player)
    {
        if (alivePlayers.contains(player.getUniqueId()))
        {
            return false;
        }

        // Player registered as alive
        alivePlayers.add(player.getUniqueId());
        updateAliveCache();

        // This method can be used to add a player after the game start.
        if (!players.contains(player.getName()))
        {
            players.add(player.getName());
        }

        // Event
        p.getServer().getPluginManager().callEvent(new UHPlayerResurrectedEvent(player));

        return true;
    }

    /**
     * Returns true if a player need to be resurrected.
     *
     * @param player
     *
     * @return
     */
    public boolean isDeadPlayersToBeResurrected(Player player)
    {
        return deadPlayersToBeResurrected.contains(player.getName());
    }

    /**
     * Registers a player as resurrected.
     *
     * @param player
     */
    public void markPlayerAsResurrected(Player player)
    {
        deadPlayersToBeResurrected.remove(player.getName());
    }


    /**
     * This method saves the location of the death of a player.
     *
     * @param player
     * @param location
     */
    public void addDeathLocation(Player player, Location location)
    {
        deathLocations.put(player.getUniqueId(), location);
    }

    /**
     * This method removes the stored death location.
     *
     * @param player
     */
    public void removeDeathLocation(Player player)
    {
        deathLocations.remove(player.getUniqueId());
    }

    /**
     * This method returns the stored death location.
     *
     * @param player The player to retrieve the death location of.
     *
     * @return The death location, or {@code null} if not defined.
     */
    public Location getDeathLocation(Player player)
    {
        if (deathLocations.containsKey(player.getUniqueId()))
        {
            return deathLocations.get(player.getUniqueId());
        }

        return null;
    }

    /**
     * This method returns true if a death location is stored for the given player.
     *
     * @param player The player to check the death location of.
     *
     * @return {@code true} if a death location is set.
     */
    public boolean hasDeathLocation(Player player)
    {
        return deathLocations.containsKey(player.getUniqueId());
    }

    /**
     * Adds a spectator. When the game is started, spectators are ignored and the spectator mode is
     * enabled if SpectatorPlus is present.
     *
     * @param player The player to register as a spectator.
     */
    public void addStartupSpectator(Player player)
    {
        spectators.add(player.getUniqueId());
        tm.removePlayerFromTeam(player);
    }

    /**
     * Removes a spectator.
     *
     * @param player The spectator to remove.
     */
    public void removeStartupSpectator(Player player)
    {
        spectators.remove(player.getUniqueId());
    }

    /**
     * Returns a list of the current registered spectators.
     *
     * This returns only a list of the <em>initial</em> spectators. Use {@link #getAlivePlayers()}
     * to get the alive players, and remove the elements of this list from the online players to get
     * the spectators.
     *
     * @return The initial spectators.
     */
    public HashSet<String> getStartupSpectators()
    {

        HashSet<String> spectatorNames = new HashSet<>();

        for (UUID id : spectators)
        {
            spectatorNames.add(p.getServer().getPlayer(id).getName());
        }

        return spectatorNames;
    }

    /**
     * @return true if the game was launched and is not finished.
     */
    public boolean isGameRunning()
    {
        return gameStarted && !gameFinished;
    }

    /**
     * @return true if the game is started.
     */
    public boolean isGameStarted()
    {
        return gameStarted;
    }

    /**
     * @return true if the game is finished.
     */
    public boolean isGameFinished()
    {
        return gameFinished;
    }

    /**
     * Registers the state of the game.
     *
     * @param finished If true, the game will be marked as finished.
     */
    public void setGameFinished(boolean finished)
    {
        gameFinished = finished;
    }

    /**
     * @return true if the game is a game with teams, and false if the game is a solo game.
     */
    public boolean isGameWithTeams()
    {
        return gameWithTeams;
    }

    /**
     * @return true if damages are enabled. Damages are enabled 30 seconds after the beginning of
     * the game.
     */
    public boolean isTakingDamage()
    {
        return damageIsOn;
    }

    /**
     * Returns true if the given player is dead.
     *
     * @param player The player.
     *
     * @return True if the player is dead.
     */
    public boolean isPlayerDead(Player player)
    {
        return !alivePlayers.contains(player.getUniqueId());
    }

    /**
     * Returns true if the given player is dead.
     *
     * @param player The UUID of the player.
     *
     * @return True if the player is dead.
     */
    public boolean isPlayerDead(UUID player)
    {
        return !alivePlayers.contains(player);
    }

    /**
     * Registers a player as dead.
     *
     * @param player The player to mark as dead.
     */
    public void addDead(Player player)
    {
        alivePlayers.remove(player.getUniqueId());
        updateAliveCache();
    }

    /**
     * Registers a player as dead.
     *
     * @param player The UUID of the player to mark as dead.
     */
    public void addDead(UUID player)
    {
        alivePlayers.remove(player);
        updateAliveCache();
    }


    /**
     * Broadcasts the winner(s) of the game and launches some fireworks
     *
     * @throws IllegalStateException if the game is not started or not finished (use the message to
     *                               distinguish these cases, {@link #FINISH_ERROR_NOT_STARTED} or
     *                               {@link #FINISH_ERROR_NOT_FINISHED}).
     */
    public void finishGame()
    {
        if (!p.getGameManager().isGameStarted())
        {
            throw new IllegalStateException(FINISH_ERROR_NOT_STARTED);
        }

        if (p.getGameManager().getAliveTeamsCount() != 1)
        {
            throw new IllegalStateException(FINISH_ERROR_NOT_FINISHED);
        }

        // There's only one team.
        p.getLogger().info(p.getGameManager().getAliveTeams().toString());
        UHTeam winnerTeam = p.getGameManager().getAliveTeams().iterator().next();
        Set<OfflinePlayer> listWinners = winnerTeam.getPlayers();

        if (p.getConfig().getBoolean("finish.message"))
        {
            if (p.getGameManager().isGameWithTeams())
            {
                String winners = "";
                int j = 0;

                for (OfflinePlayer winner : listWinners)
                {
                    if (j == 0)
                    {
                        // Nothing
                    }
                    else if (j == listWinners.size() - 1)
                    {
                        winners += " " + i.t("finish.and") + " ";
                    }
                    else
                    {
                        winners += ", ";
                    }

                    winners += winner.getName();
                    j++;
                }

                p.getServer().broadcastMessage(i.t("finish.broadcast.withTeams", winners, winnerTeam.getDisplayName()));
            }
            else
            {
                p.getServer().broadcastMessage(i.t("finish.broadcast.withoutTeams", winnerTeam.getName()));
            }
        }

        if (p.getConfig().getBoolean("finish.fireworks.enabled"))
        {
            new FireworksOnWinnersTask(p, listWinners).runTaskTimer(p, 0l, 10l);
        }
    }

    /**
     * Returns the names of the players of this game.
     *
     * @return The set.
     */
    public HashSet<String> getPlayers()
    {
        return players;
    }

    /**
     * Returns a list of the currently alive teams.
     *
     * @return The set.
     */
    public Set<UHTeam> getAliveTeams()
    {
        return aliveTeams;
    }

    /**
     * Returns a list of the currently alive players.
     *
     * @return The set.
     */
    public Set<OfflinePlayer> getAlivePlayers()
    {

        HashSet<OfflinePlayer> alivePlayersList = new HashSet<>();

        for (UUID id : alivePlayers)
        {
            alivePlayersList.add(p.getServer().getOfflinePlayer(id));
        }

        return alivePlayersList;
    }

    /**
     * Returns a list of the currently alive and online players.
     *
     * @return The list.
     */
    public HashSet<Player> getOnlineAlivePlayers()
    {

        HashSet<Player> alivePlayersList = new HashSet<>();

        for (UUID id : alivePlayers)
        {
            Player player = p.getServer().getPlayer(id);
            if (player != null)
            {
                alivePlayersList.add(player);
            }
        }

        return alivePlayersList;
    }

    /**
     * @return the death sound, or null if no death sound is registered.
     */
    public UHSound getDeathSound()
    {
        return DEATH_SOUND;
    }

    /**
     * @return the length of one episode, in seconds.
     */
    public Integer getEpisodeLength()
    {
        try
        {
            return UHUtils.string2Time(p.getConfig().getString("episodes.length"));
        }
        catch (IllegalArgumentException e)
        {
            return 20 * 60; // default value, 20 minutes
        }
    }

    /**
     * @return the (cached) number of alive players.
     */
    public Integer getAlivePlayersCount()
    {
        return alivePlayersCount;
    }

    /**
     * @return the (cached) number of alive teams.
     */
    public Integer getAliveTeamsCount()
    {
        return aliveTeamsCount;
    }

    /**
     * @return the number of the current episode.
     */
    public Integer getEpisode()
    {
        return episode;
    }


    /**
     * @return the teleporter instance used to start the game, containing the spawn points of each
     * player.
     */
    public Teleporter getTeleporter()
    {
        return teleporter;
    }



    /* ***  Deprecated methods  *** */

    /**
     * Adds a spectator. When the game is started, spectators are ignored and the spectator mode is
     * enabled if SpectatorPlus is present.
     *
     * @param player The player to register as a spectator.
     *
     * @deprecated Use {@link #addStartupSpectator(Player)} instead.
     */
    @Deprecated
    public void addSpectator(Player player)
    {
        addStartupSpectator(player);
    }

    /**
     * Removes a spectator.
     *
     * @param player
     *
     * @deprecated Use {@link #removeStartupSpectator(Player)} instead.
     */
    @Deprecated
    public void removeSpectator(Player player)
    {
        removeStartupSpectator(player);
    }

    /**
     * Returns a list of the current registered spectators.
     *
     * This returns only a list of the <em>initial</em> spectators. Use {@link #getAlivePlayers()}
     * to get the alive players, and remove the elements of this list from the online players to get
     * the spectators.
     *
     * @return The initial spectators.
     * @deprecated Use {@link #getStartupSpectators()} instead.
     */
    @Deprecated
    public HashSet<String> getSpectators()
    {
        return getStartupSpectators();
    }
}
