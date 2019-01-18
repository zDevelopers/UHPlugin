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
package eu.carrade.amaury.UHCReloaded;

import eu.carrade.amaury.UHCReloaded.core.ModuleLoadTime;
import eu.carrade.amaury.UHCReloaded.core.ModulesManager;
import eu.carrade.amaury.UHCReloaded.modules.core.border.BorderModule;
import eu.carrade.amaury.UHCReloaded.modules.core.game.GameModule;
import eu.carrade.amaury.UHCReloaded.modules.core.game.events.game.GamePhaseChangedEvent;
import eu.carrade.amaury.UHCReloaded.modules.core.modules.ModulesManagerModule;
import eu.carrade.amaury.UHCReloaded.modules.core.sidebar.SidebarModule;
import eu.carrade.amaury.UHCReloaded.modules.core.spawns.SpawnsModule;
import eu.carrade.amaury.UHCReloaded.modules.core.spectators.SpectatorsModule;
import eu.carrade.amaury.UHCReloaded.modules.core.teams.TeamsModule;
import eu.carrade.amaury.UHCReloaded.modules.core.timers.TimersModule;
import eu.carrade.amaury.UHCReloaded.old.misc.Freezer;
import eu.carrade.amaury.UHCReloaded.old.misc.RulesManager;
import eu.carrade.amaury.UHCReloaded.utils.OfflinePlayersLoader;
import fr.zcraft.zlib.components.commands.Commands;
import fr.zcraft.zlib.components.gui.Gui;
import fr.zcraft.zlib.components.i18n.I;
import fr.zcraft.zlib.components.i18n.I18n;
import fr.zcraft.zlib.components.scoreboard.SidebarScoreboard;
import fr.zcraft.zlib.core.ZLib;
import fr.zcraft.zlib.core.ZPlugin;
import fr.zcraft.zlib.tools.PluginLogger;
import fr.zcraft.zlib.tools.runners.RunTask;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.scoreboard.Scoreboard;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.function.BiConsumer;


public class UHCReloaded extends ZPlugin implements Listener
{
    private static UHCReloaded instance;

    private ModulesManager modulesManager = null;

    private Scoreboard scoreboard = null;

    private World worldNormal = null;
    private World worldNether = null;
    private World worldTheEnd = null;

    private boolean worldsLoaded = false;

    private RulesManager rulesManager = null;

    private Freezer freezer = null;


    @Override
    public void onEnable()
    {
        instance = this;

        this.saveDefaultConfig();


        /* *** Required zLib base components *** */

        loadComponents(UHConfig.class, I18n.class, Commands.class, SidebarScoreboard.class, Gui.class, OfflinePlayersLoader.class);

        modulesManager = loadComponent(ModulesManager.class);


        /* *** Internationalization *** */

        if (UHConfig.LANG.get() == null) I18n.useDefaultPrimaryLocale();
        else I18n.setPrimaryLocale(UHConfig.LANG.get());

        I18n.setFallbackLocale(Locale.US);


        /* *** Core events *** */

        ZLib.registerEvents(this);


        /* *** Core modules *** */

        modulesManager.registerModules(
                ModulesManagerModule.class,     // Manages the modules from the game/commands.

                SidebarModule.class,            // Manages the sidebar and provides hooks for other modules.
                                                // Must be loaded before the game-related modules.

                TeamsModule.class,              // Manages the teams (for both team & solo games).
                                                // Must be loaded before the game-related modules.

                TimersModule.class,             // Manages the time in everything.

                BorderModule.class,             // Manages the border of the map.
                                                // Must be loaded before the spawns module.

                SpawnsModule.class,             // Manages the spawn points and teleportation.
                                                // Must be loaded before the game-related modules.

                GameModule.class,               // Manages the game progression.

                SpectatorsModule.class          // Manages the spectators.
        );


        /* *** Config modules *** */

        UHConfig.MODULES.forEach((BiConsumer<String, Boolean>) modulesManager::registerModule);


        /* *** Loads modules from startup time *** */

        modulesManager.loadModules(ModuleLoadTime.STARTUP);


        /* *** Loads modules from post-world time if worlds are loaded (server reloaded) *** */

        if (!getServer().getWorlds().isEmpty())
        {
            onEnableWhenWorldsAvailable();
        }


        /* *** Sets scoreboard for already-logged-in players (server reloaded) *** */

        RunTask.nextTick(() -> Bukkit.getOnlinePlayers().forEach(player -> player.setScoreboard(scoreboard)));


        /* *** Ready *** */

        PluginLogger.info(I.t("Ultra Hardcore plugin loaded."));
    }

    /**
     * Run when the worlds are available (on plugin enabled if reloaded, on worlds ready else).
     */
    private void onEnableWhenWorldsAvailable()
    {
        scoreboard = Bukkit.getServer().getScoreboardManager().getNewScoreboard();

        RunTask.nextTick(() -> {
            worldNormal = setDefaultWorld(World.Environment.NORMAL, UHConfig.WORLDS.OVERWORLD.get());
            worldNether = setDefaultWorld(World.Environment.NETHER, UHConfig.WORLDS.NETHER.get());
            worldTheEnd = setDefaultWorld(World.Environment.THE_END, UHConfig.WORLDS.THE_END.get());

            modulesManager.loadModules(ModuleLoadTime.POST_WORLD);
        });

        worldsLoaded = true;
    }

    /**
     * @return The modules manager.
     */
    public ModulesManager getModulesManager()
    {
        return modulesManager;
    }

    /**
     * @return The Bukkit scoreboard to use for everything.
     */
    public Scoreboard getScoreboard()
    {
        return scoreboard;
    }

    /**
     * @param environment An environment.
     * @return The world to use for this environment in the game.
     */
    public World getWorld(final World.Environment environment)
    {
        if (environment == null) return worldNormal;

        switch (environment)
        {
            case NORMAL:
                return worldNormal;

            case NETHER:
                return worldNether;

            case THE_END:
            default:
                return worldTheEnd;
        }
    }

    /**
     * @return A stream containing all three playing worlds.
     */
    public List<World> getWorlds()
    {
        return Arrays.asList(worldNormal, worldNether, worldTheEnd);
    }


    @EventHandler (priority = EventPriority.LOWEST)
    public final void onWorldsLoaded(final WorldLoadEvent e)
    {
        if (!worldsLoaded) onEnableWhenWorldsAvailable();
    }

    @EventHandler (priority = EventPriority.LOWEST)
    public void onGamePhaseChanged(final GamePhaseChangedEvent ev)
    {
        switch (ev.getNewPhase())
        {
            case STARTING:
                modulesManager.loadModules(ModuleLoadTime.ON_GAME_STARTING);
                break;

            case IN_GAME:
                modulesManager.loadModules(ModuleLoadTime.ON_GAME_START);
                break;

            case END:
                modulesManager.loadModules(ModuleLoadTime.ON_GAME_END);
                break;
        }
    }

    @EventHandler (priority = EventPriority.LOWEST)
    public final void onPlayerJoin(final PlayerJoinEvent ev)
    {
        ev.getPlayer().setScoreboard(scoreboard);
    }


    private World setDefaultWorld(final World.Environment environment, final String worldName)
    {
        final World userWorld = Bukkit.getWorld(worldName);

        // If the world is valid, it is used as-is.
        if (userWorld != null && userWorld.getEnvironment() == environment) return userWorld;

        // Else we use the first world we found with the right type.
        for (final World world : Bukkit.getWorlds())
        {
            if (world.getEnvironment() == environment) return world;
        }

        // We finally fallback on the first world regardless of its type to have at least something.
        return Bukkit.getWorlds().get(0);
    }

    /**
     * @return the rules manager.
     */
    public RulesManager getRulesManager()
    {
        return rulesManager;
    }

    /**
     * Returns the freezer.
     */
    public Freezer getFreezer()
    {
        return freezer;
    }

    /**
     * Returns the plugin's instance.
     */
    public static UHCReloaded get()
    {
        return instance;
    }
}
