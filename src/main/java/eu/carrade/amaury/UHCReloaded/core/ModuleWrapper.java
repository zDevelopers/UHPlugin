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

import com.google.common.base.CaseFormat;
import eu.carrade.amaury.UHCReloaded.events.modules.ModuleLoadedEvent;
import eu.carrade.amaury.UHCReloaded.events.modules.ModuleUnloadedEvent;
import fr.zcraft.zlib.components.configuration.ConfigurationInstance;
import fr.zcraft.zlib.core.ZLib;
import fr.zcraft.zlib.tools.PluginLogger;
import fr.zcraft.zlib.tools.reflection.Reflection;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;


public class ModuleWrapper
{
    private final String name;
    private final String description;
    private final ModuleInfo.ModuleLoadTime when;

    private final Class<? extends UHModule> moduleClass;

    private final Class<? extends ConfigurationInstance> moduleConfiguration;
    private final String settingsFileName;

    private final boolean internal;

    private boolean enabledAtStartup;

    private UHModule instance = null;

    public ModuleWrapper(
            final String name,
            final String description,
            final boolean internal,
            final boolean enabledAtStartup,
            final ModuleInfo.ModuleLoadTime when,
            final Class<? extends UHModule> moduleClass,
            final Class<? extends ConfigurationInstance> moduleConfiguration,
            final String settingsFileName)
    {
        this.name = name;
        this.description = description;
        this.internal = internal;
        this.enabledAtStartup = enabledAtStartup;
        this.when = when;
        this.moduleClass = moduleClass;
        this.moduleConfiguration = moduleConfiguration;
        this.settingsFileName = settingsFileName;

        loadConfiguration();
    }

    /**
     * Enables this module.
     */
    public void enable()
    {
        instance = ZLib.loadComponent(moduleClass);
        Bukkit.getPluginManager().callEvent(new ModuleLoadedEvent(instance));
    }

    /**
     * Disable this module.
     */
    public void disable()
    {
        if (instance == null) return;

        instance.setEnabled(false);
        ZLib.unregisterEvents(instance);

        Bukkit.getPluginManager().callEvent(new ModuleUnloadedEvent(instance));

        instance = null;
    }

    /**
     * If this module was not yet loaded (e.g. if we're pre-game and the module loads
     * when the game starts), sets the module to be loaded (or not) when the time comes.
     *
     * @param enabledAtStartup {@code true} to register this module to be enabled at the right time.
     */
    public void setEnabledAtStartup(boolean enabledAtStartup)
    {
        if (instance != null) return;

        this.enabledAtStartup = enabledAtStartup;
    }

    /**
     * @return A name for this module. Either the provided name using {@link ModuleInfo} or a name derived from the class name.
     */
    public String getName()
    {
        return name != null && !name.isEmpty()
                ? name
                : StringUtils.capitalize(String.join(" ", StringUtils.splitByCharacterTypeCamelCase(moduleClass.getSimpleName())));
    }

    /**
     * @return A description for the module, or {@code null} if none provided.
     */
    public String getDescription()
    {
        return description != null && !description.isEmpty() ? description : null;
    }

    /**
     * @return When this module should be loaded.
     */
    public ModuleInfo.ModuleLoadTime getWhen()
    {
        return when;
    }

    /**
     * @return This module's base class.
     */
    public Class<? extends UHModule> getModuleClass()
    {
        return moduleClass;
    }

    /**
     * @return A {@link File} representing the configuration file on the server's filesystem.
     */
    private File getConfigurationFile()
    {
        final String settingsFileName;

        if (this.settingsFileName != null)
        {
            settingsFileName = this.settingsFileName + ".yml";
        }
        else
        {
            settingsFileName = CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_HYPHEN, moduleClass.getSimpleName()) + ".yml";
        }

        return new File(ZLib.getPlugin().getDataFolder(), "modules" + File.separator + settingsFileName);
    }

    /**
     * Loads the configuration from its file and initialize the class.
     */
    private void loadConfiguration()
    {
        if (moduleConfiguration != null )
        {
            final File settingsFile = getConfigurationFile();
            try
            {
                if (!settingsFile.exists())
                {
                    try
                    {
                        settingsFile.getParentFile().mkdirs();
                        settingsFile.createNewFile();
                    }
                    catch (IOException e)
                    {
                        PluginLogger.error("Cannot create and populate {0}'s module configuration file - using default values.", e, getName());
                    }
                }

                final ConfigurationInstance settings = Reflection.instantiate(moduleConfiguration, settingsFile);
                settings.setEnabled(true);
                settings.save();
            }
            catch (NoSuchMethodException | InstantiationException | InvocationTargetException | IllegalAccessException e)
            {
                PluginLogger.info("Cannot initialize the configuration for the module {0} ({1})!", e, getName(), moduleClass.getName());
            }
        }
    }

    /**
     * @return {@code true} if this module is internal.
     */
    public boolean isInternal()
    {
        return internal;
    }

    /**
     * @return {@code true} if this module, according to the configuration file, should be loaded at startup.
     */
    public boolean isEnabledAtStartup()
    {
        return enabledAtStartup;
    }

    /**
     * @return {@code true} if the module was loaded and enabled.
     */
    public boolean isEnabled()
    {
        return instance != null && instance.isEnabled();
    }

    /**
     * @return This module's instance.
     */
    public UHModule get()
    {
        return instance;
    }
}
