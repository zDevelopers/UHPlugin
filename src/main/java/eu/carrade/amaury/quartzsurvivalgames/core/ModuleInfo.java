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

package eu.carrade.amaury.quartzsurvivalgames.core;

import fr.zcraft.quartzlib.components.configuration.ConfigurationInstance;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.bukkit.Material;


@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface ModuleInfo {
    /**
     * @return A name for the module (optional).
     */
    String name() default "";

    /**
     * @return A description for this module.
     */
    String description() default "";

    /**
     * @return A short description for this module. Used instead of the long one in the modules
     * list GUI, so you can write novels in the description if you want.
     */
    String short_description() default "";

    /**
     * @return The author(s) of this module.
     */
    String authors() default "";

    /**
     * @return When this module should load.
     */
    ModuleLoadTime when() default ModuleLoadTime.POST_WORLD;

    /**
     * @return The category under which this module should be classed.
     */
    ModuleCategory category() default ModuleCategory.OTHER;

    /**
     * @return The icon we should use for this module. If not provide (or {@link Material#AIR air}),
     * we'll use the category's icon.
     */
    Material icon() default Material.AIR;

    /**
     * @return The configuration class to initialize (if any).
     * ConfigurationInstance.class is used as a default value to represent no settings as null values are
     * not allowed and classes will always be subclasses of this one.
     */
    Class<? extends ConfigurationInstance> settings() default ConfigurationInstance.class;

    /**
     * @return The settings's filename (without the .yml extension).
     * If empty/not provided, derived from the class name.
     */
    String settings_filename() default "";

    /**
     * @return The settings's default filename, containing the default configuration written
     * to the server's plugins/UHPlugin/modules directory if the file does not exists.
     * <p>
     * The format can either be a simple filename without extension, like “moduleName“, and
     * the file will be copied from the UHCReloaded's JAR, from modules/moduleName.yml; or
     * the filename prefixed by the plugin name like so: “OtherPlugin:moduleName”, and the
     * file will be copied from the given plugin's JAR, from modules/moduleName.yml too.
     * <p>
     * If left empty, it will be derived from the module name and category, by converting
     * the module's class name into hyphened-snake-case, removing the “-module” suffix (if
     * any) and prefixing by the category converted to hyphened-snake-case.
     * Example: “external-reports”.
     * <p>
     * In all cases, the file will be copied into UHPlugin's data directory, under module/.
     */
    String settings_default_filename() default "";

    /**
     * @return A list of external Bukkit/Spigot plugin this module depends on.
     */
    String[] depends() default {};

    /**
     * @return {@code true} if this module is an internal core module
     * that should always be loaded first.
     */
    boolean internal() default false;

    /**
     * @return {@code true} if this module is a technical core module
     * that should not be displayed in the /config gui.
     */
    boolean hidden() default false;

    /**
     * @return {@code true} if the module can be unloaded and re-loaded. This reflects the status change
     * from inside the game, as all modules can always be disabled on the configuration file (or not loaded
     * at all).
     * <p>
     * If this is {@code true}, when disabled, a module will have its {@link QSGModule#onDisable()}
     * method called, and after that, its listener will be unregistered and the module instance removed from the system.
     * <p>
     * When re-loaded, a whole new instance will be created.
     */
    boolean can_be_unloaded() default true;

    /**
     * @return {@code true} if the module can be loaded after the moment it was declared to be loaded. If {@code false},
     * and an user tries to enable/load it during the game and the module is configured to be loaded, let's say, on
     * {@link ModuleLoadTime#ON_GAME_STARTING game starting}, the operation will fail. Use this if you depends on the
     * fact that the {@link QSGModule#onEnable()} method is called at a specific time.
     */
    boolean can_be_loaded_late() default true;
}
