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
import eu.carrade.amaury.UHCReloaded.core.ModuleWrapper;
import eu.carrade.amaury.UHCReloaded.core.UHModule;
import eu.carrade.amaury.UHCReloaded.core.events.AllModulesLoadedEvent;
import eu.carrade.amaury.UHCReloaded.core.events.ModuleLoadedEvent;
import eu.carrade.amaury.UHCReloaded.core.events.ModuleUnloadedEvent;
import eu.carrade.amaury.UHCReloaded.game.UHGameManager;
import eu.carrade.amaury.UHCReloaded.modules.core.border.BorderModule;
import eu.carrade.amaury.UHCReloaded.modules.core.game.GameModule;
import eu.carrade.amaury.UHCReloaded.modules.core.game.events.game.GamePhaseChangedEvent;
import eu.carrade.amaury.UHCReloaded.modules.core.modules.ModulesManagerModule;
import eu.carrade.amaury.UHCReloaded.modules.core.sidebar.SidebarModule;
import eu.carrade.amaury.UHCReloaded.modules.core.spawns.SpawnsModule;
import eu.carrade.amaury.UHCReloaded.modules.core.spectators.SpectatorsModule;
import eu.carrade.amaury.UHCReloaded.modules.core.spectators.managers.SpectatorsManager;
import eu.carrade.amaury.UHCReloaded.modules.core.teams.TeamsModule;
import eu.carrade.amaury.UHCReloaded.modules.core.timers.TimersModule;
import eu.carrade.amaury.UHCReloaded.old.integration.UHSpectatorPlusIntegration;
import eu.carrade.amaury.UHCReloaded.old.integration.UHWorldBorderIntegration;
import eu.carrade.amaury.UHCReloaded.old.misc.*;
import eu.carrade.amaury.UHCReloaded.old.recipes.RecipesManager;
import eu.carrade.amaury.UHCReloaded.scoreboard.ScoreboardManager;
import eu.carrade.amaury.UHCReloaded.utils.ModulesUtils;
import eu.carrade.amaury.UHCReloaded.utils.OfflinePlayersLoader;
import fr.zcraft.zlib.components.commands.Command;
import fr.zcraft.zlib.components.commands.Commands;
import fr.zcraft.zlib.components.gui.Gui;
import fr.zcraft.zlib.components.i18n.I;
import fr.zcraft.zlib.components.i18n.I18n;
import fr.zcraft.zlib.components.scoreboard.SidebarScoreboard;
import fr.zcraft.zlib.core.ZLib;
import fr.zcraft.zlib.core.ZPlugin;
import fr.zcraft.zlib.tools.PluginLogger;
import fr.zcraft.zlib.tools.reflection.Reflection;
import fr.zcraft.zlib.tools.runners.RunTask;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandMap;
import org.bukkit.command.PluginCommand;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scoreboard.Scoreboard;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;


public class UHCReloaded extends ZPlugin implements Listener
{
    private static UHCReloaded instance;

    private Map<Class<? extends UHModule>, ModuleWrapper> modules = new HashMap<>();
    private Set<ModuleLoadTime> loadedPriorities = new HashSet<>();

    private Scoreboard scoreboard = null;

    private World worldNormal = null;
    private World worldNether = null;
    private World worldTheEnd = null;

    private boolean worldsLoaded = false;

    private UHGameManager gameManager = null;
    private SpectatorsManager spectatorsManager = null;
    private ScoreboardManager scoreboardManager = null;
    private MOTDManager motdManager = null;
    private RulesManager rulesManager = null;
    private PlayerListHeaderFooterManager playerListHeaderFooterManager = null;
    private RecipesManager recipesManager = null;

    private RuntimeCommandsExecutor runtimeCommandsExecutor = null;

    private Freezer freezer = null;

    private UHWorldBorderIntegration wbintegration = null;
    private UHSpectatorPlusIntegration spintegration = null;


    @Override
    public void onEnable()
    {
        instance = this;

        this.saveDefaultConfig();


        /* *** Required zLib base components *** */

        loadComponents(UHConfig.class, I18n.class, Commands.class, SidebarScoreboard.class, Gui.class, OfflinePlayersLoader.class);


        /* *** Internationalization *** */

        if (UHConfig.LANG.get() == null) I18n.useDefaultPrimaryLocale();
        else I18n.setPrimaryLocale(UHConfig.LANG.get());

        I18n.setFallbackLocale(Locale.US);


        /* *** Core events *** */

        ZLib.registerEvents(this);


        /* *** Core modules *** */

        registerModules(
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

        UHConfig.MODULES.forEach((BiConsumer<String, Boolean>) this::registerModule);


        /* *** Loads modules from startup time *** */

        loadModules(ModuleLoadTime.STARTUP);


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

            loadModules(ModuleLoadTime.POST_WORLD);
        });

        worldsLoaded = true;
    }



    /**
     * Registers an UHCReloaded module (or many). It is not enabled by this method.
     *
     * @param modules the module's class, that must accept a zero-arguments constructor.
     */
    @SafeVarargs
    public final void registerModules(final Class<? extends UHModule>... modules)
    {
        Arrays.stream(modules).forEach(module -> registerModule(module, true));
    }

    /**
     * Registers an UHCReloaded module (or many). It is not enabled by this method.
     *
     * @param module the module's class, that must accept a zero-arguments constructor.
     * @param enableAtStartup {@code true} if this module, according to the configuration file, should be loaded at startup.
     */
    private void registerModule(final Class<? extends UHModule> module, final boolean enableAtStartup)
    {
        this.modules.put(module, new ModuleWrapper(module, enableAtStartup));
    }

    /**
     * Registers an UHCReloaded module. It is not enabled by this method.
     *
     * It tries to load the following classes (in this order, taking the first existing):
     *
     * - eu.carrade.amaury.UHCReloaded.modules.[name]
     * - eu.carrade.amaury.UHCReloaded.modules.[name]Module
     * - eu.carrade.amaury.UHCReloaded.modules.[capitalizedName]
     * - eu.carrade.amaury.UHCReloaded.modules.[capitalizedName]Module
     * - eu.carrade.amaury.UHCReloaded.modules.[firstLowercasedName].[name]
     * - eu.carrade.amaury.UHCReloaded.modules.[firstLowercasedName].[name]Module
     * - eu.carrade.amaury.UHCReloaded.modules.[firstLowercasedName].[capitalizedName]
     * - eu.carrade.amaury.UHCReloaded.modules.[firstLowercasedName].[capitalizedName]Module
     * - eu.carrade.amaury.UHCReloaded.modules.[firstLowercasedName].Module
     * - [name]
     *
     * @param module the module's class name; the class must accept a zero-arguments constructor.
     */
    public void registerModule(final String module)
    {
        registerModule(module, true);
    }

    /**
     * Registers an UHCReloaded module. It is not enabled by this method.
     *
     * It tries to load the following classes (in this order, taking the first existing):
     *
     * - eu.carrade.amaury.UHCReloaded.modules.[name]
     * - eu.carrade.amaury.UHCReloaded.modules.[name]Module
     * - eu.carrade.amaury.UHCReloaded.modules.[capitalizedName]
     * - eu.carrade.amaury.UHCReloaded.modules.[capitalizedName]Module
     * - eu.carrade.amaury.UHCReloaded.modules.[firstLowercasedName].[name]
     * - eu.carrade.amaury.UHCReloaded.modules.[firstLowercasedName].[name]Module
     * - eu.carrade.amaury.UHCReloaded.modules.[firstLowercasedName].[capitalizedName]
     * - eu.carrade.amaury.UHCReloaded.modules.[firstLowercasedName].[capitalizedName]Module
     * - eu.carrade.amaury.UHCReloaded.modules.[firstLowercasedName].Module
     * - [name]
     *
     * @param module the module's class name; the class must accept a zero-arguments constructor.
     * @param initiallyEnabled {@code true} if this module, according to the configuration file,
     *                         should be enabled at startup.
     */
    private void registerModule(final String module, boolean initiallyEnabled)
    {
        final Class<? extends UHModule> moduleClass = ModulesUtils.getClassFromName(
                module.replace('-', '.'),
                "eu.carrade.amaury.UHCReloaded.modules",
                "Module",
                UHModule.class
        );

        if (moduleClass != null)
        {
            registerModule(moduleClass, initiallyEnabled);
        }
        else
        {
            PluginLogger.error("Error registering a module: unable to find a module named {0} in the class path. Maybe you spelled it wrong?", module);
        }
    }



    /**
     * Loads registered modules. Internal modules will always be loaded first.
     *
     * @param loadTime Loads the modules registered to be loaded at that given time.
     */
    private void loadModules(final ModuleLoadTime loadTime)
    {
        if (loadedPriorities.contains(loadTime)) return;

        // Loads all internal modules first
        modules.values().stream()
                .filter(module -> module.getWhen() == loadTime)
                .filter(ModuleWrapper::isEnabled)
                .filter(module -> !module.isLoaded())
                .filter(ModuleWrapper::isInternal)
                .forEach(module -> module.load(false));

        // Then loads other modules
        modules.values().stream()
                .filter(module -> module.getWhen() == loadTime)
                .filter(ModuleWrapper::isEnabled)
                .filter(module -> !module.isLoaded())
                .filter(module -> !module.isInternal())
                .forEach(module -> module.load(false));

        loadedPriorities.add(loadTime);

        collectCommandsFromModules();

        getServer().getPluginManager().callEvent(new AllModulesLoadedEvent(loadTime));
    }

    /**
     * Checks if the given load time was already loaded.
     *
     * @param loadTime The load time.
     * @return {@code true} if loaded.
     */
    public boolean isLoaded(final ModuleLoadTime loadTime)
    {
        return loadedPriorities.contains(loadTime);
    }

    /**
     * Gets a module's instance. This may return null if the module is not currently
     * enabled.
     *
     * @param moduleClass The module's class.
     * @param <M> The module's type.
     *
     * @return The module's instance.
     */
    public static <M extends UHModule> M getModule(final Class<M> moduleClass)
    {
        final ModuleWrapper module = get().modules.get(moduleClass);

        if (module == null) return null;
        else return (M) module.get();
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
                loadModules(ModuleLoadTime.ON_GAME_STARTING);
                break;

            case IN_GAME:
                loadModules(ModuleLoadTime.ON_GAME_START);
                break;

            case END:
                loadModules(ModuleLoadTime.ON_GAME_END);
                break;
        }
    }

    @EventHandler
    public void onModuleLoaded(final ModuleLoadedEvent ev)
    {
        PluginLogger.info("Module {0} loaded.", ev.getModule().getName());

        if (ev.isLoadedLate())
        {
            // If loaded late, we may have to re-register the module's commands.
            collectCommandsFromModules();
        }
    }

    @EventHandler
    public void onModuleUnloaded(final ModuleUnloadedEvent ev)
    {
        PluginLogger.info("Module {0} unloaded.", ev.getModule().getName());

        // We remove commands if needed when a module is unloaded,
        // as it will be unable to handle them properly (the module
        // instance being null).
        collectCommandsFromModules();
    }

    @EventHandler (priority = EventPriority.LOWEST)
    public final void onPlayerJoin(final PlayerJoinEvent ev)
    {
        ev.getPlayer().setScoreboard(scoreboard);
    }



    @SuppressWarnings ("unchecked")
    private void collectCommandsFromModules()
    {
        Commands.register("uh", modules.values().stream()
                .filter(ModuleWrapper::isLoaded)
                .map(module -> module.get().getCommands())
                .filter(Objects::nonNull)
                .flatMap(Collection::stream)
                .toArray(Class[]::new));

        final Map<String, Class<? extends Command>> commandAliases = modules.values().stream()
                .filter(ModuleWrapper::isLoaded)
                .map(module -> module.get().getCommandsAliases())
                .filter(Objects::nonNull)
                .flatMap(commandsAliases -> commandsAliases.entrySet().stream())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (a, b) -> a));

        // As they are not registered in the plugin.yml, for each command, we have to force-register
        // the name manually.

        final Map<org.bukkit.command.Command, Class<? extends Command>> pluginCommands = new HashMap<>();
        final Set<String> registered = new HashSet<>();

        try
        {
            final Constructor<PluginCommand> pluginCommandConstructor = PluginCommand.class.getDeclaredConstructor(String.class, Plugin.class);
            pluginCommandConstructor.setAccessible(true);

            for (final Map.Entry<String, Class<? extends Command>> commandAlias : commandAliases.entrySet())
            {
                try
                {
                    pluginCommands.put(pluginCommandConstructor.newInstance(commandAlias.getKey(), this), commandAlias.getValue());
                    registered.add(commandAlias.getKey());
                }
                catch (InstantiationException | InvocationTargetException | IllegalAccessException e)
                {
                    PluginLogger.error("Unable to register plugin command for {0}, is this version supported by UHCReloaded?", e, commandAlias);
                }
            }

            try
            {
                final CommandMap commandMap = (CommandMap) Reflection.getFieldValue(Bukkit.getServer(), "commandMap");

                String mutPrefix = getDescription().getPrefix();
                if (mutPrefix == null) mutPrefix = getDescription().getName().toLowerCase();

                final String prefix = mutPrefix;

                pluginCommands.forEach((pluginCommand, commandClass) ->
                {
                    if (commandMap.register(prefix, pluginCommand))
                    {
                        PluginLogger.info(
                                "Hot-registered new command /{0} for class “{1}”.",
                                pluginCommand.getName(),
                                commandClass.getName().replace("eu.carrade.amaury.UHCReloaded.", "...")
                        );
                    }
                });
            }
            catch (NoSuchFieldException | IllegalAccessException e)
            {
                PluginLogger.error("Unable to retrieve Bukkit's command map, is this version supported by UHCReloaded?", e);
            }
        }
        catch (NoSuchMethodException | SecurityException e)
        {
            PluginLogger.error("Unable to register plugin commands: unable to retrieve PluginCommand's constructor. Is this version supported by UHCReloaded?", e);
        }

        // Now that all commands are registered into Bukkit, we can register them into zLib.

        commandAliases.forEach((name, klass) ->
        {
            // Bukkit registration failed?
            if (!registered.contains(name)) return;

            Commands.registerShortcut("uh", klass, name);
        });
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
     * Returns the game manager.
     */
    public UHGameManager getGameManager()
    {
        return gameManager;
    }

    /**
     * @return the spectators manager.
     */
    public SpectatorsManager getSpectatorsManager()
    {
        return spectatorsManager;
    }

    /**
     * Returns the scoreboard manager.
     */
    public ScoreboardManager getScoreboardManager()
    {
        return scoreboardManager;
    }

    /**
     * Returns the MOTD manager.
     */
    public MOTDManager getMOTDManager()
    {
        return motdManager;
    }

    /**
     * @return the rules manager.
     */
    public RulesManager getRulesManager()
    {
        return rulesManager;
    }

    /**
     * Returns the players list's headers & footers manager.
     */
    public PlayerListHeaderFooterManager getPlayerListHeaderFooterManager()
    {
        return playerListHeaderFooterManager;
    }

    /**
     * Returns the recipe manager.
     */
    public RecipesManager getRecipesManager()
    {
        return recipesManager;
    }

    /**
     * Returns the manager used to manage the commands executed after the start/the end of the
     * game (or any other moment using the generic API).
     */
    public RuntimeCommandsExecutor getRuntimeCommandsExecutor()
    {
        return runtimeCommandsExecutor;
    }

    /**
     * Returns the freezer.
     */
    public Freezer getFreezer()
    {
        return freezer;
    }

    /**
     * Returns the representation of the WorldBorder integration in the plugin.
     */
    public UHWorldBorderIntegration getWorldBorderIntegration()
    {
        return wbintegration;
    }

    /**
     * Returns the representation of the SpectatorPlus integration in the plugin.
     */
    public UHSpectatorPlusIntegration getSpectatorPlusIntegration()
    {
        return spintegration;
    }

    /**
     * Returns the plugin's instance.
     */
    public static UHCReloaded get()
    {
        return instance;
    }

    /**
     * @return A view on all registered modules.
     */
    public Collection<ModuleWrapper> getModules()
    {
        return modules.values();
    }
}
