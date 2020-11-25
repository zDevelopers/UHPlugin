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

package eu.carrade.amaury.quartzsurvivalgames.modules.core.timers;

import eu.carrade.amaury.quartzsurvivalgames.core.ModuleCategory;
import eu.carrade.amaury.quartzsurvivalgames.core.ModuleInfo;
import eu.carrade.amaury.quartzsurvivalgames.core.QSGModule;
import eu.carrade.amaury.quartzsurvivalgames.modules.core.sidebar.SidebarInjector;
import eu.carrade.amaury.quartzsurvivalgames.modules.core.timers.commands.TimersCommand;
import fr.zcraft.quartzlib.components.commands.Command;
import fr.zcraft.quartzlib.tools.runners.RunTask;
import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.stream.Collectors;


@ModuleInfo (
        name = "Timers",
        description = "The timekeeper of the whole UHCReloaded plugin & companions.",
        category = ModuleCategory.CORE,
        icon = Material.CLOCK,
        internal = true,
        can_be_unloaded = false
)
public class TimersModule extends QSGModule
{
    private Set<Timer> timers = new CopyOnWriteArraySet<>();

    /**
     * Cached list of the running timers
     */
    private Set<Timer> runningTimers = new CopyOnWriteArraySet<>();

    /**
     * List of the timers to resume if running timers are paused.
     *
     * @see #pauseAllRunning(boolean)
     */
    private Set<Timer> timersToResume = new CopyOnWriteArraySet<>();

    /**
     * Sidebar cache.
     */
    private List<Pair<String, String>> sidebarInjection = new LinkedList<>();


    @Override
    protected void onEnable()
    {
        RunTask.timer(() -> timers.forEach(Timer::update), 1L, 20L);
    }


    @Override
    public List<Class<? extends Command>> getCommands()
    {
        return Collections.singletonList(TimersCommand.class);
    }


    @Override
    public void prepareInjectionIntoSidebar()
    {
        sidebarInjection.clear();
        sidebarInjection = timers.stream()
                .filter(Timer::isDisplayed)
                .map(timer -> Pair.of(timer.isNameDisplayed() ? timer.getDisplayName() : null, timer.toString()))
                .collect(Collectors.toList());
    }

    @Override
    public void injectIntoSidebar(Player player, SidebarInjector injector)
    {
        sidebarInjection.forEach(timer -> {
            final List<String> lines;

            if (timer.getLeft() == null) lines = Collections.singletonList(timer.getRight());
            else lines = Arrays.asList(timer.getLeft(), timer.getRight());

            injector.injectLines(SidebarInjector.SidebarPriority.VERY_BOTTOM, true, lines);
        });
    }

    /**
     * Registers a timer.
     *
     * @param timer The timer to register.
     * @throws IllegalArgumentException if a timer with the same name is already registered.
     */
    public void registerTimer(final Timer timer)
    {
        if (timers.contains(timer))
        {
            throw new IllegalArgumentException("The timer " + timer.getName() + " is already registered.");
        }

        timers.add(timer);

        timer.setRegistered(true);
    }

    /**
     * Unregisters a timer.
     * <p>
     * If the timer was not registered, nothing is done.
     *
     * @param timer The timer to unregister.
     */
    public void unregisterTimer(final Timer timer)
    {
        timers.remove(timer);
        runningTimers.remove(timer);

        timer.setRegistered(false);
    }

    /**
     * Updates the internal list of started timers.
     */
    public void updateStartedTimersList()
    {
        runningTimers = timers.stream().filter(Timer::isRunning).collect(Collectors.toSet());
    }

    /**
     * Returns a timer by his name.
     *
     * @param name The name of the timer.
     *
     * @return The first timer with this name, or null if there isn't any timer with this name.
     */
    public Timer getTimer(final String name)
    {
        return timers.stream().filter(timer -> timer.getName().equals(name)).findFirst().orElse(null);
    }

    /**
     * Returns a collection containing the registered timers.
     *
     * @return The collection.
     */
    public Collection<Timer> getTimers()
    {
        return Collections.unmodifiableSet(timers);
    }

    /**
     * Returns a collection containing the running timers.
     *
     * @return The collection.
     */
    public Collection<Timer> getRunningTimers()
    {
        return Collections.unmodifiableSet(runningTimers);
    }

    /**
     * Pauses (or resumes) all the running timers.
     *
     * @param paused If true, all the timers will be paused. Else, resumed.
     */
    public void pauseAll(boolean paused)
    {
        getRunningTimers().forEach(timer -> timer.setPaused(paused));

        if (!paused)
        {
            // If we restart all the timers regardless to their previous state,
            // this data is meaningless.
            timersToResume.clear();
        }
    }

    /**
     * Pauses (or resumes) all the running timers.
     * <p>
     * This method will only resume the previously-running timers.
     *
     * @param paused If true, all the timers will be paused. Else, resumed.
     */
    public void pauseAllRunning(boolean paused)
    {
        if (paused)
        {
            getRunningTimers().stream().filter(timer -> !timer.isPaused()).forEach(timer -> {
                timer.setPaused(true);
                timersToResume.add(timer);
            });
        }
        else
        {
            timersToResume.forEach(timer -> timer.setPaused(false));
            timersToResume.clear();
        }
    }
}
