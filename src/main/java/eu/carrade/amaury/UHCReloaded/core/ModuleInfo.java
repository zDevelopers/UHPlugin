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

import fr.zcraft.zlib.components.configuration.ConfigurationInstance;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


@Retention(RetentionPolicy.RUNTIME)
@Target ({ElementType.TYPE})
public @interface ModuleInfo
{
    /**
     * @return A name for the module (optional).
     */
    String name() default "";

    /**
     * @return A short description for this module.
     */
    String description() default "";

    /**
     * @return When this module should load.
     */
    ModuleLoadTime when() default ModuleLoadTime.POST_WORLD;

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
     * @return A list of external Bukkit/Spigot plugin this module depends on.
     */
    String[] depends() default {};

    /**
     * @return {@code true} if this module is an internal core module
     * that should always be loaded first.
     */
    boolean internal() default false;

    /**
     * @return {@code true} if the module can be disabled and re-enabled. This reflects the status change
     * from inside the game, as all modules can always be disabled on the configuration file (or not loaded
     * at all).
     *
     * If this is {@code true}, when disabled, a module will have its {@link UHModule#onDisable() onDisable()}
     * method called, and after that, its listener will be unregistered and the module instance removed from the system.
     *
     * When re-enabled, a whole new instance will be created.
     */
    boolean can_be_disabled() default true;


    enum ModuleLoadTime
    {
        /**
         * Loads the module at startup, before the worlds are loaded.
         *
         * Please note that most core modules (and localization) are not loaded at this point. Use that
         * for modules altering the world generation.
         */
        STARTUP,

        /**
         * Loads the module after the world(s), or immediately if the plugin is reloaded.
         * The thing is, all worlds will be loaded when the module is.
         */
        POST_WORLD,

        /**
         * Loads the module when the game phase is set to STARTING, i.e. when the /uh start command
         * is used.
         */
        ON_GAME_STARTING,

        /**
         * Loads the module when the game starts, i.e. when all players falls from their spawn into
         * the world.
         */
        ON_GAME_START,

        /**
         * Loads the module when the game ends.
         */
        ON_GAME_END
    }
}
