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
import eu.carrade.amaury.UHCReloaded.core.events.ModuleDisabledEvent;
import eu.carrade.amaury.UHCReloaded.core.events.ModuleEnabledEvent;
import eu.carrade.amaury.UHCReloaded.core.events.ModuleLoadedEvent;
import eu.carrade.amaury.UHCReloaded.core.events.ModuleUnloadedEvent;
import eu.carrade.amaury.UHCReloaded.shortcuts.UR;
import fr.zcraft.zlib.components.configuration.ConfigurationInstance;
import fr.zcraft.zlib.components.i18n.I;
import fr.zcraft.zlib.core.ZLib;
import fr.zcraft.zlib.tools.PluginLogger;
import fr.zcraft.zlib.tools.items.ItemStackBuilder;
import fr.zcraft.zlib.tools.reflection.Reflection;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.stream.Stream;


public class ModuleWrapper
{
    private final String name;
    private final String shortDescription;
    private final String description;
    private final String authors;

    private final ModuleLoadTime when;

    private final ModuleCategory category;
    private final Material icon;

    private boolean enabled;

    private final Class<? extends UHModule> moduleClass;

    private final Class<? extends ConfigurationInstance> moduleConfiguration;
    private final String settingsFileName;
    private String[] dependencies;

    private final boolean internal;
    private final boolean canBeUnloaded;
    private final boolean canBeLoadedLate;

    private UHModule instance = null;

    public ModuleWrapper(final Class<? extends UHModule> moduleClass)
    {
        this(moduleClass, true);
    }

    public ModuleWrapper(final Class<? extends UHModule> moduleClass, boolean enabled)
    {
        this.name = computeModuleName(moduleClass);
        this.moduleClass = moduleClass;
        this.enabled = enabled;

        final ModuleInfo info = moduleClass.getAnnotation(ModuleInfo.class);

        if (info == null)
        {
            description = "";
            shortDescription = "";
            authors = "";
            internal = false;
            canBeUnloaded = true;
            canBeLoadedLate = true;
            when = ModuleLoadTime.POST_WORLD;
            category = ModuleCategory.OTHER;
            icon = Material.AIR;
            moduleConfiguration = null;
            settingsFileName = null;
            dependencies = new String[] {};
        }
        else
        {
            description = info.description();
            shortDescription = info.short_description();
            authors = info.authors();
            internal = info.internal();
            canBeUnloaded = info.can_be_unloaded();
            canBeLoadedLate = info.can_be_loaded_late();
            when = info.when();
            category = info.category();
            icon = info.icon();
            moduleConfiguration = info.settings().equals(ConfigurationInstance.class) ? null : info.settings();
            settingsFileName = info.settings_filename().isEmpty() ? null : info.settings_filename();
            dependencies = info.depends();
        }

        loadConfiguration();
    }

    /**
     * Enables this module.
     *
     * @param late {@code true} if the module is not loaded when specified in
     * its {@link ModuleInfo properties}.
     */
    public boolean load(boolean late)
    {
        if (isLoaded()) return true;

        // Check dependencies

        for (final String dependency : dependencies)
        {
            final Plugin plugin = Bukkit.getPluginManager().getPlugin(dependency);
            if (plugin == null)
            {
                if (dependencies.length >= 2)
                {
                    PluginLogger.error("Cannot enable module {0}: missing dependency {1} (depends on {2}).", name, dependency, String.join(", ", dependencies));
                }
                else
                {
                    PluginLogger.error("Cannot enable module {0}: missing dependency {1}.", name, dependency);
                }

                return false;
            }
            else if (!plugin.isEnabled())
            {
                // Ensures every dependency is available when a module is loaded.
                Bukkit.getPluginManager().enablePlugin(plugin);
            }
        }

        instance = ZLib.loadComponent(moduleClass);

        Bukkit.getPluginManager().callEvent(new ModuleLoadedEvent(this, late));

        if (late) instance.onEnableLate();

        return true;
    }

    /**
     * Disable this module.
     */
    public void unload()
    {
        if (instance == null) return;

        instance.setEnabled(false);
        ZLib.unregisterEvents(instance);

        Bukkit.getPluginManager().callEvent(new ModuleUnloadedEvent(this));

        instance = null;
    }

    /**
     * Enables or disables this module.
     *
     * @param enabled new status.
     * @return {@code true} if the operation succeeded. It will be {@code false}
     * if you try to disable the module and if the module is internal or the
     * module is loaded and marked as un-loadable, or if you try to enable the
     * module and it is marked as un-loadable after its auto-load moment.
     */
    public boolean setEnabled(boolean enabled)
    {
        if (this.enabled != enabled)
        {
            // Can we enabled this module?
            if (enabled)
            {
                if (!canBeEnabled())
                {
                    return false;
                }
            }

            // Can we disable this module?
            else if (!canBeDisabled())
            {
                return false;
            }


            this.enabled = enabled;

            if (enabled)
            {
                Bukkit.getPluginManager().callEvent(new ModuleEnabledEvent(this));

                if (UR.get().isLoaded(when))
                {
                    return load(true);
                }
            }
            else
            {
                Bukkit.getPluginManager().callEvent(new ModuleDisabledEvent(this));
                unload();
            }
        }

        return true;
    }

    /**
     * @return {@code true} if after being disabled, this module will be reloadable
     * (can depends on the moment this method is called).
     */
    public boolean canBeEnabled()
    {
        return canBeLoadedLate || !UR.get().isLoaded(when);
    }

    /**
     * @return {@code true} if, at the moment this method is called, this module can be disabled.
     */
    public boolean canBeDisabled()
    {
        return !internal && !(isLoaded() && !canBeUnloaded);
    }

    /**
     * @return A name for this module. Either the provided name using {@link ModuleInfo} or a name derived from the class name.
     */
    public String getName()
    {
        return name;
    }

    /**
     * @return A description for the module, or {@code null} if none provided.
     */
    public String getDescription()
    {
        return description != null && !description.isEmpty() ? description : null;
    }

    /**
     * @return A short description for the module, or {@code null} if none provided.
     */
    public String getShortDescription()
    {
        return shortDescription != null && !shortDescription.isEmpty() ? shortDescription : null;
    }

    /**
     * @return The authors of the module, or {@code null} if not provided.
     */
    public String getAuthors()
    {
        return authors != null && !authors.isEmpty() ? authors : null;
    }

    /**
     * @return This module's category.
     */
    public ModuleCategory getCategory()
    {
        return category;
    }

    /**
     * @return This module's icon: either the declared one, or the category's.
     */
    public ItemStack getIcon()
    {
        return icon == Material.AIR ? category.getIcon() : new ItemStack(icon);
    }

    /**
     * Generates and returns a full icon for the module, including its status and
     * description in the tooltip.
     *
     * @param complete If {@code true}, will use the long complete description.
     *                 Else, will use the short description if available, else
     *                 the long one.
     * @return The icon.
     */
    public ItemStackBuilder getFullIcon(final boolean complete)
    {
        final ItemStackBuilder icon = new ItemStackBuilder(getIcon())
                .title(
                    (isLoaded() ? ChatColor.GREEN : (isEnabled() ? ChatColor.GOLD : ChatColor.RED)) + "" + ChatColor.BOLD + "\u2758 ",
                    category.getColor() + "" + ChatColor.BOLD + name
                )
                .loreLine(
                    ChatColor.DARK_GRAY + category.getDisplayName(),
                    ChatColor.DARK_GRAY + " - ",
                    ChatColor.DARK_GRAY + (enabled ? (isLoaded() ? I.t("Enabled and loaded") : I.t("Enabled")) : I.t("Disabled"))
                );

        String description;
        if (((description = getShortDescription()) != null && !complete) || (description = getDescription()) != null)
        {
            icon.loreSeparator().longLore(ChatColor.WHITE, description, complete ? 100 : 42);
        }

        icon.loreSeparator().longLore(ChatColor.BLUE, I.t("Load time")).longLore(ChatColor.WHITE, when.getDescription(), 42);

        if (dependencies.length != 0)
        {
            icon.loreSeparator().longLore(ChatColor.BLUE, I.t("External dependencies"));
            Stream.of(dependencies).forEach(dep -> icon.loreLine(ChatColor.GRAY + "- ", (Bukkit.getPluginManager().getPlugin(dep) != null ? ChatColor.WHITE : ChatColor.RED) + dep));
        }

        String authors;
        if ((authors = getAuthors()) != null)
        {
            icon.loreSeparator().longLore(ChatColor.BLUE, I.t("Brought to you by")).longLore(ChatColor.WHITE, authors, 42);
        }

        return icon.hideAttributes();
    }

    /**
     * @return A list of external plugins this module depends on.
     */
    public String[] getDependencies()
    {
        return dependencies;
    }

    /**
     * @return When this module should be loaded.
     */
    public ModuleLoadTime getWhen()
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
     * @return {@code true} if this module can be disabled at runtime.
     */
    public boolean canBeUnloaded()
    {
        return canBeUnloaded;
    }

    /**
     * @return {@code true} if the module is enabled. Disabled modules will not be
     * loaded when the time comes.
     */
    public boolean isEnabled()
    {
        return enabled;
    }

    /**
     * @return {@code true} if the module was loaded and enabled.
     */
    public boolean isLoaded()
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

    static String computeModuleName(Class<? extends UHModule> moduleClass)
    {
        final ModuleInfo info = moduleClass.getAnnotation(ModuleInfo.class);

        if (info == null || info.name().isEmpty())
            return StringUtils.capitalize(String.join(" ", StringUtils.splitByCharacterTypeCamelCase(moduleClass.getSimpleName())));

        else return info.name();
    }
}
