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

package eu.carrade.amaury.quartzsurvivalgames.core.events;

import eu.carrade.amaury.quartzsurvivalgames.core.ModuleWrapper;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;


/**
 * Fired after a module was loaded.
 */
public class ModuleLoadedEvent extends Event {
    private static final HandlerList handlers = new HandlerList();

    private final ModuleWrapper module;
    private final boolean loadedLate;


    public ModuleLoadedEvent(final ModuleWrapper module) {
        this(module, false);
    }

    public ModuleLoadedEvent(final ModuleWrapper module, boolean loadedLate) {
        this.module = module;
        this.loadedLate = loadedLate;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    /**
     * @return the loaded module.
     */
    public ModuleWrapper getModule() {
        return module;
    }

    /**
     * @return {@code true} if the module is not loaded when specified in its
     * {@link eu.carrade.amaury.quartzsurvivalgames.core.ModuleInfo properties}.
     */
    public boolean isLoadedLate() {
        return loadedLate;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
}
