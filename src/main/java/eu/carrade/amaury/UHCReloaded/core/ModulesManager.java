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
package eu.carrade.amaury.UHCReloaded.core;

import com.google.common.reflect.ClassPath;
import eu.carrade.amaury.UHCReloaded.core.events.AllModulesLoadedEvent;
import eu.carrade.amaury.UHCReloaded.core.events.ModuleLoadedEvent;
import eu.carrade.amaury.UHCReloaded.core.events.ModuleUnloadedEvent;
import eu.carrade.amaury.UHCReloaded.modules.ingame.kick.KickModule;
import eu.carrade.amaury.UHCReloaded.modules.scenarii.alliances.AlliancesModule;
import eu.carrade.amaury.UHCReloaded.shortcuts.UR;
import eu.carrade.amaury.UHCReloaded.utils.ModulesUtils;
import fr.zcraft.zlib.components.commands.Command;
import fr.zcraft.zlib.components.commands.Commands;
import fr.zcraft.zlib.core.ZLib;
import fr.zcraft.zlib.core.ZLibComponent;
import fr.zcraft.zlib.tools.PluginLogger;
import fr.zcraft.zlib.tools.reflection.Reflection;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandMap;
import org.bukkit.command.PluginCommand;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.stream.Collectors;

public class ModulesManager extends ZLibComponent implements Listener
{
    /**
     * The package were all built-in modules are stored.
     */
    private final static String MODULES_PACKAGE = "eu.carrade.amaury.UHCReloaded.modules";

    /**
     * These built-in modules will not be enabled by default. It includes all scenarii, so the default game
     * is a standard one, and the kick module, as spectators are better for most admins.
     */
    private final static List<Class<? extends UHModule>> DISABLED_BY_DEFAULT = Arrays.asList(
            KickModule.class,
            AlliancesModule.class
    );

    /**
     * All the registered modules.
     */
    private Map<Class<? extends UHModule>, ModuleWrapper> modules = new HashMap<>();

    /**
     * These loads times were loaded using {@link #loadModules(ModuleLoadTime)} and are stored to not
     * be loaded again (e.g. in case of cancelled start, modules with {@link ModuleLoadTime#ON_GAME_STARTING}
     * will not be loaded twice).
     */
    private Set<ModuleLoadTime> loadedPriorities = new HashSet<>();


    /**
     * Lookup and loads all built-in modules. They will be enabled by default, except if in
     * the {@link #DISABLED_BY_DEFAULT disabled-by-default list}.
     */
    public void registerBuiltInModules()
    {
        long t = System.currentTimeMillis();
        int i = 0;

        try
        {
            for (ClassPath.ClassInfo classInfo : ClassPath.from(getClass().getClassLoader()).getTopLevelClassesRecursive(MODULES_PACKAGE))
            {
                final Class<?> potentialModuleClass = classInfo.load();

                if (UHModule.class.isAssignableFrom(potentialModuleClass))
                {
                    registerModule((Class<? extends UHModule>) potentialModuleClass, !DISABLED_BY_DEFAULT.contains(potentialModuleClass));
                    i++;
                }
            }
        }
        catch (final IOException e)
        {
            PluginLogger.error("Unable to load built-in modules.", e);
        }

        PluginLogger.info("Registered {0} built-in modules in {1}ms", i, System.currentTimeMillis() - t);
    }


    /**
     * @return A view on all registered modules.
     */
    public Collection<ModuleWrapper> getModules()
    {
        return modules.values();
    }

    /**
     * Registers an UHCReloaded module (or many). It is not loaded by this method.
     *
     * @param modules the module's class, that must accept a zero-arguments constructor.
     */
    @SafeVarargs
    public final void registerModules(final Class<? extends UHModule>... modules)
    {
        Arrays.stream(modules).forEach(module -> registerModule(module, true));
    }

    /**
     * Registers an UHCReloaded module. It is not loaded by this method.
     *
     * @param module the module's class, that must accept a zero-arguments constructor.
     * @param initiallyEnabled {@code true} if this module, according to the configuration file, should be enabled at startup.
     */
    public void registerModule(final Class<? extends UHModule> module, final boolean initiallyEnabled)
    {
        if (!modules.containsKey(module))
        {
            this.modules.put(module, new ModuleWrapper(module, initiallyEnabled));
        }
    }

    /**
     * Registers an UHCReloaded module. It is not loaded by this method.
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
     * Registers an UHCReloaded module. It is not loaded by this method.
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
    public void registerModule(final String module, boolean initiallyEnabled)
    {
        final Class<? extends UHModule> moduleClass = ModulesUtils.getClassFromName(
                module.replace('-', '.'),
                MODULES_PACKAGE,
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
    public void loadModules(final ModuleLoadTime loadTime)
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

        Bukkit.getPluginManager().callEvent(new AllModulesLoadedEvent(loadTime));
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
        final ModuleWrapper module = UR.get().getModulesManager().modules.get(moduleClass);

        if (module == null) return null;
        else return (M) module.get();
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
                    pluginCommands.put(pluginCommandConstructor.newInstance(commandAlias.getKey(), ZLib.getPlugin()), commandAlias.getValue());
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

                String mutPrefix = UR.get().getDescription().getPrefix();
                if (mutPrefix == null) mutPrefix = UR.get().getDescription().getName().toLowerCase();

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
}
