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

package eu.carrade.amaury.quartzsurvivalgames.core;

import com.google.common.reflect.ClassPath;
import eu.carrade.amaury.quartzsurvivalgames.core.events.AllModulesLoadedEvent;
import eu.carrade.amaury.quartzsurvivalgames.core.events.ModuleLoadedEvent;
import eu.carrade.amaury.quartzsurvivalgames.core.events.ModuleUnloadedEvent;
import eu.carrade.amaury.quartzsurvivalgames.modules.cosmetics.episodes.EpisodesModule;
import eu.carrade.amaury.quartzsurvivalgames.modules.end.kick.KickModule;
import eu.carrade.amaury.quartzsurvivalgames.modules.other.PomfModule;
import eu.carrade.amaury.quartzsurvivalgames.modules.scenarii.alliances.AlliancesModule;
import eu.carrade.amaury.quartzsurvivalgames.modules.worldgen.creatures.CreaturesModule;
import eu.carrade.amaury.quartzsurvivalgames.shortcuts.QSG;
import eu.carrade.amaury.quartzsurvivalgames.utils.ModulesUtils;
import fr.zcraft.quartzlib.components.commands.Command;
import fr.zcraft.quartzlib.components.commands.Commands;
import fr.zcraft.quartzlib.core.QuartzComponent;
import fr.zcraft.quartzlib.core.QuartzLib;
import fr.zcraft.quartzlib.tools.PluginLogger;
import fr.zcraft.quartzlib.tools.reflection.Reflection;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandMap;
import org.bukkit.command.PluginCommand;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

public class ModulesManager extends QuartzComponent implements Listener {
    /**
     * The package were all built-in modules are stored.
     */
    private final static String MODULES_PACKAGE = "eu.carrade.amaury.quartzsurvivalgames.modules";

    /**
     * These built-in modules will not be enabled by default. It includes all scenarii and world generation modules,
     * so the default game is a standard one, the kick module, as spectators are better for most admins, and
     * the episodes modules, as not everyone is a youtuber.
     */
    private final static List<Class<? extends QSGModule>> DISABLED_BY_DEFAULT = Arrays.asList(
            KickModule.class,
            EpisodesModule.class,
            PomfModule.class,

            // Scenarii
            AlliancesModule.class,

            // World generation
            CreaturesModule.class
    );

    /**
     * All the registered modules.
     */
    private final Map<Class<? extends QSGModule>, ModuleWrapper> modules = new HashMap<>();

    /**
     * These loads times were loaded using {@link #loadModules(ModuleLoadTime)} and are stored to not
     * be loaded again (e.g. in case of cancelled start, modules with {@link ModuleLoadTime#ON_GAME_STARTING}
     * will not be loaded twice).
     */
    private final Set<ModuleLoadTime> loadedPriorities = new HashSet<>();

    /**
     * Gets a module's instance. This may return null if the module is not currently
     * enabled.
     *
     * @param moduleClass The module's class.
     * @param <M>         The module's type.
     * @return The module's instance.
     */
    public static <M extends QSGModule> M getModule(final Class<M> moduleClass) {
        final ModuleWrapper module = QSG.get().getModulesManager().modules.get(moduleClass);

        if (module == null || !module.isLoaded()) {
            return null;
        } else {
            return (M) module.get();
        }
    }

    /**
     * Lookup and loads all built-in modules. They will be enabled by default, except if in
     * the {@link #DISABLED_BY_DEFAULT disabled-by-default list}.
     */
    public void registerBuiltInModules() {
        final long t = System.currentTimeMillis();
        int i = 0;

        try {
            for (final ClassPath.ClassInfo classInfo : ClassPath.from(getClass().getClassLoader())
                    .getTopLevelClassesRecursive(MODULES_PACKAGE)) {
                try {
                    final Class<?> potentialModuleClass = classInfo.load();

                    if (QSGModule.class.isAssignableFrom(potentialModuleClass)) {
                        registerModule((Class<? extends QSGModule>) potentialModuleClass,
                                !DISABLED_BY_DEFAULT.contains(potentialModuleClass));
                        i++;
                    }
                } catch (final Throwable e) {
                    PluginLogger.error("Unable to load built-in module {0}: {1}", classInfo.getName(), e.getMessage());
                }
            }
        } catch (final IOException e) {
            PluginLogger.error("Unable to load built-in modules.", e);
        }

        PluginLogger.info("Registered {0} built-in modules in {1}ms", i, System.currentTimeMillis() - t);
    }

    /**
     * @return A view on all registered modules.
     */
    public Collection<ModuleWrapper> getModules() {
        return modules.values();
    }

    /**
     * Registers an UHCReloaded module (or many). It is not loaded by this method.
     *
     * @param modules the module's class, that must accept a zero-arguments constructor.
     */
    @SafeVarargs
    public final void registerModules(final Class<? extends QSGModule>... modules) {
        Arrays.stream(modules).forEach(module -> registerModule(module, true));
    }

    /**
     * Registers an UHCReloaded module. It is not loaded by this method.
     *
     * @param module           the module's class, that must accept a zero-arguments constructor.
     * @param initiallyEnabled {@code true} if this module, according to the configuration file, should be enabled at startup.
     */
    public void registerModule(final Class<? extends QSGModule> module, final boolean initiallyEnabled) {
        if (!modules.containsKey(module)) {
            this.modules.put(module, new ModuleWrapper(module, initiallyEnabled));
        }
    }

    /**
     * Registers an UHCReloaded module. It is not loaded by this method.
     * <p>
     * It tries to load the following classes (in this order, taking the first existing):
     * <p>
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
    public void registerModule(final String module) {
        registerModule(module, true);
    }

    /**
     * Registers an UHCReloaded module. It is not loaded by this method.
     * <p>
     * It tries to load the following classes (in this order, taking the first existing):
     * <p>
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
     * @param module           the module's class name; the class must accept a zero-arguments constructor.
     * @param initiallyEnabled {@code true} if this module, according to the configuration file,
     *                         should be enabled at startup.
     */
    public void registerModule(final String module, boolean initiallyEnabled) {
        final Class<? extends QSGModule> moduleClass = ModulesUtils.getClassFromName(
                module.replace('-', '.'),
                MODULES_PACKAGE,
                "Module",
                QSGModule.class
        );

        if (moduleClass != null) {
            registerModule(moduleClass, initiallyEnabled);
        } else {
            PluginLogger
                    .error("Error registering a module: unable to find a module named {0} in the class path. Maybe you spelled it wrong?",
                            module);
        }
    }

    /**
     * Loads registered modules. Internal modules will always be loaded first.
     *
     * @param loadTime Loads the modules registered to be loaded at that given time.
     */
    public void loadModules(final ModuleLoadTime loadTime) {
        if (loadedPriorities.contains(loadTime)) {
            return;
        }

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

        Bukkit.getPluginManager().callEvent(new AllModulesLoadedEvent(loadTime));
    }

    /**
     * Checks if the given load time was already loaded.
     *
     * @param loadTime The load time.
     * @return {@code true} if loaded.
     */
    public boolean isLoaded(final ModuleLoadTime loadTime) {
        return loadedPriorities.contains(loadTime);
    }

    /**
     * Checks if the given module is loaded.
     *
     * @param module The module's class.
     * @return {@code true} if loaded.
     */
    public boolean isLoaded(final Class<? extends QSGModule> module) {
        final ModuleWrapper wrapper = modules.get(module);

        return wrapper != null && wrapper.isLoaded();
    }

    @SuppressWarnings("unchecked")
    private void collectCommandsFromModules() {
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

        try {
            final Constructor<PluginCommand> pluginCommandConstructor =
                    PluginCommand.class.getDeclaredConstructor(String.class, Plugin.class);
            pluginCommandConstructor.setAccessible(true);

            for (final Map.Entry<String, Class<? extends Command>> commandAlias : commandAliases.entrySet()) {
                try {
                    pluginCommands
                            .put(pluginCommandConstructor.newInstance(commandAlias.getKey(), QuartzLib.getPlugin()),
                                    commandAlias.getValue());
                    registered.add(commandAlias.getKey());
                }
                catch (InstantiationException | InvocationTargetException | IllegalAccessException e) {
                    PluginLogger
                            .error("Unable to register plugin command for {0}, is this version supported by UHCReloaded?",
                                    e, commandAlias);
                }
            }

            try {
                final CommandMap commandMap = (CommandMap) Reflection.getFieldValue(Bukkit.getServer(), "commandMap");

                String mutPrefix = QSG.get().getDescription().getPrefix();
                if (mutPrefix == null) {
                    mutPrefix = QSG.get().getDescription().getName().toLowerCase();
                }

                final String prefix = mutPrefix;

                pluginCommands.forEach((pluginCommand, commandClass) ->
                {
                    if (commandMap.register(prefix, pluginCommand)) {
                        PluginLogger.info(
                                "Hot-registered new command /{0} for class “{1}”.",
                                pluginCommand.getName(),
                                commandClass.getName().replace("eu.carrade.amaury.UHCReloaded.", "...")
                        );
                    }
                });
            }
            catch (NoSuchFieldException | IllegalAccessException e) {
                PluginLogger
                        .error("Unable to retrieve Bukkit's command map, is this version supported by UHCReloaded?", e);
            }
        }
        catch (NoSuchMethodException | SecurityException e) {
            PluginLogger
                    .error("Unable to register plugin commands: unable to retrieve PluginCommand's constructor. Is this version supported by UHCReloaded?",
                            e);
        }

        // Now that all commands are registered into Bukkit, we can register them into zLib.

        commandAliases.forEach((name, klass) ->
        {
            // Bukkit registration failed?
            if (!registered.contains(name)) {
                return;
            }

            Commands.registerShortcut("uh", klass, name);
        });
    }


    @EventHandler
    public void onModuleLoaded(final ModuleLoadedEvent ev) {
        PluginLogger.info("Module {0} loaded.", ev.getModule().getName());

        if (ev.isLoadedLate()) {
            // If loaded late, we may have to re-register the module's commands.
            collectCommandsFromModules();
        }
    }

    @EventHandler
    public void onModuleUnloaded(final ModuleUnloadedEvent ev) {
        PluginLogger.info("Module {0} unloaded.", ev.getModule().getName());

        // We remove commands if needed when a module is unloaded,
        // as it will be unable to handle them properly (the module
        // instance being null).
        collectCommandsFromModules();
    }
}
