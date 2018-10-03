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

package eu.carrade.amaury.UHCReloaded.commands.core.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * This annotation needs to be applied to every command class. It is used to define the
 * name and the permission of the command.
 *
 * @version 1.0
 * @author Amaury Carrade
 */
@Retention (RetentionPolicy.RUNTIME)
@Target (ElementType.TYPE)
public @interface Command
{

    /**
     * The name of the command, needed to type in the console/chat to execute
     * the (sub-)command.
     */
    String name();

    /**
     * The permission needed to execute this command.
     *
     * <p>
     *     Please note that with the current version of this API, the user will need to have the right to
     *     access the parent commands, to access this command.<br />
     *     This situation may evolve in the future.
     * </p>
     * <p>
     *     If the {@code inheritPermission} option is unset or set to {@code true},
     *     this permission is <strong>concatened to the parent permissions</strong>.<br />
     *     As example, if the permission is set to {@code sb}, and if the parent command
     *     have the permission {@code cmd.norris}, the real permission of the command will
     *     be {@code cmd.norris.sb}.
     * </p>
     * <p>
     *     If this is left empty, or not set, the permission will be the name of the command, excepted
     *     if {@link #useParentPermission} is set to true.
     * </p>
     */
    String permission() default "";

    /**
     * If this is set to {@code false}, the permission will be interpreted <em>as-is</em>,
     * without concatenation with the permissions of the parent commands.
     *
     * <p>
     *     You should not set this to {@code false} if the command have sub-commands with this
     *     set to {@code true}, or weired behavior may happens.
     * </p>
     */
    boolean inheritPermission() default true;

    /**
     * If this is set to {@code true}, the permission of the parent command will be used.
     *
     * <p>
     *     If the parent command is {@code null} (i.e. this command is a root one), the command will be
     *     accessible to everyone.
     * </p>
     */
    boolean useParentPermission() default false;

    /**
     * If this is set to true, no permissions check will be done when someone
     * executes this command.
     */
    boolean noPermission() default false;
}
