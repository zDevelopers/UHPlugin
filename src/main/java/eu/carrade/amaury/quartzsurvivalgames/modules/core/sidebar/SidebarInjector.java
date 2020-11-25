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
package eu.carrade.amaury.quartzsurvivalgames.modules.core.sidebar;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;


/**
 * An instance of this class will be passed to each modules to add for a given player lines to the sidebar.
 */
public class SidebarInjector
{
    private final Set<LinesBucket> buckets = new TreeSet<>(new LinesBucketComparator());

    /**
     * Injects lines into the sidebar.
     *
     * @param priority The lines priority to order all injected lines.
     * @param spacesAbove {@code true} to add a space above the given lines (if not first)
     * @param spacesBelow {@code true} to add a space below the given lines (if not last)
     * @param lines The lines (from top to bottom).
     *
     * @return current instance, for methods chaining.
     */
    public SidebarInjector injectLines(final SidebarPriority priority, final boolean spacesAbove, final boolean spacesBelow, final Collection<String> lines)
    {
        buckets.add(new LinesBucket(priority, spacesAbove, spacesBelow, new LinkedList<>(lines)));
        return this;
    }

    /**
     * Injects lines into the sidebar.
     *
     * @param priority The lines priority to order all injected lines.
     * @param spacesAbove {@code true} to add a space above the given lines (if not first)
     * @param spacesBelow {@code true} to add a space below the given lines (if not last)
     * @param lines The lines (from top to bottom).
     *
     * @return current instance, for methods chaining.
     */
    public SidebarInjector injectLines(final SidebarPriority priority, final boolean spacesAbove, final boolean spacesBelow, final String... lines)
    {
        injectLines(priority, spacesAbove, spacesBelow, Arrays.asList(lines));
        return this;
    }

    /**
     * Injects lines into the sidebar.
     *
     * @param priority The lines priority to order all injected lines.
     * @param spacesAround {@code true} to add a space above and below the given lines (if not first/last)
     * @param lines The lines (from top to bottom).
     *
     * @return current instance, for methods chaining.
     */
    public SidebarInjector injectLines(final SidebarPriority priority, final boolean spacesAround, final Collection<String> lines)
    {
        injectLines(priority, spacesAround, spacesAround, lines);
        return this;
    }

    /**
     * Injects lines into the sidebar.
     *
     * @param priority The lines priority to order all injected lines.
     * @param spacesAround {@code true} to add a space above and below the given lines (if not first/last)
     * @param lines The lines (from top to bottom).
     *
     * @return current instance, for methods chaining.
     */
    public SidebarInjector injectLines(final SidebarPriority priority, final boolean spacesAround, final String... lines)
    {
        injectLines(priority, spacesAround, spacesAround, Arrays.asList(lines));
        return this;
    }

    /**
     * Injects lines into the sidebar. No spaces are added before or after.
     *
     * @param priority The lines priority to order all injected lines.
     * @param lines The lines (from top to bottom).
     *
     * @return current instance, for methods chaining.
     */
    public SidebarInjector injectLines(final SidebarPriority priority, final Collection<String> lines)
    {
        injectLines(priority, false, false, lines);
        return this;
    }

    /**
     * Injects lines into the sidebar. No spaces are added before or after.
     *
     * @param priority The lines priority to order all injected lines.
     * @param lines The lines (from top to bottom).
     *
     * @return current instance, for methods chaining.
     */
    public SidebarInjector injectLines(final SidebarPriority priority, final String... lines)
    {
        injectLines(priority, false, false, Arrays.asList(lines));
        return this;
    }

    /**
     * Injects lines into the sidebar without constrain on placement.
     *
     * @param spacesAbove {@code true} to add a space above the given lines (if not first)
     * @param spacesBelow {@code true} to add a space below the given lines (if not last)
     * @param lines The lines (from top to bottom).
     *
     * @return current instance, for methods chaining.
     */
    public SidebarInjector injectLines(final boolean spacesAbove, final boolean spacesBelow, final Collection<String> lines)
    {
        injectLines(SidebarPriority.MIDDLE, spacesAbove, spacesBelow, lines);
        return this;
    }

    /**
     * Injects lines into the sidebar without constrain on placement.
     *
     * @param spacesAbove {@code true} to add a space above the given lines (if not first)
     * @param spacesBelow {@code true} to add a space below the given lines (if not last)
     * @param lines The lines (from top to bottom).
     *
     * @return current instance, for methods chaining.
     */
    public SidebarInjector injectLines(final boolean spacesAbove, final boolean spacesBelow, final String... lines)
    {
        injectLines(SidebarPriority.MIDDLE, spacesAbove, spacesBelow, Arrays.asList(lines));
        return this;
    }

    /**
     * Injects lines into the sidebar without constrain on placement.
     *
     * @param spacesAround {@code true} to add a space above and below the given lines (if not first/last)
     * @param lines The lines (from top to bottom).
     *
     * @return current instance, for methods chaining.
     */
    public SidebarInjector injectLines(final boolean spacesAround, final Collection<String> lines)
    {
        injectLines(SidebarPriority.MIDDLE, spacesAround, spacesAround, lines);
        return this;
    }

    /**
     * Injects lines into the sidebar without constrain on placement.
     *
     * @param spacesAround {@code true} to add a space above and below the given lines (if not first/last)
     * @param lines The lines (from top to bottom).
     *
     * @return current instance, for methods chaining.
     */
    public SidebarInjector injectLines(final boolean spacesAround, final String... lines)
    {
        injectLines(SidebarPriority.MIDDLE, spacesAround, spacesAround, Arrays.asList(lines));
        return this;
    }

    /**
     * Injects lines into the sidebar without constrain on placement. No spaces are added before or after.
     *
     * @param lines The lines (from top to bottom).
     *
     * @return current instance, for methods chaining.
     */
    public SidebarInjector injectLines(final Collection<String> lines)
    {
        injectLines(SidebarPriority.MIDDLE, false, false, lines);
        return this;
    }

    /**
     * Injects lines into the sidebar without constrain on placement. No spaces are added before or after.
     *
     * @param lines The lines (from top to bottom).
     *
     * @return current instance, for methods chaining.
     */
    public SidebarInjector injectLines(final String... lines)
    {
        injectLines(SidebarPriority.MIDDLE, false, false, Arrays.asList(lines));
        return this;
    }

    /**
     * Builds the lines for the sidebar according to the priorities and the spaces requirements.
     *
     * Does not adds multiple empty lines if one bucket requiring spaces below is followed by one
     * requiring spaces above.
     *
     * @return a list of lines ready to be used by the sidebar.
     */
    public List<String> buildLines()
    {
        final LinkedList<String> lines = new LinkedList<>();

        // Initially to true so for the first one, if there is a space above, it's not added.
        boolean lastInsertedASpace = true;

        for (final LinesBucket bucket : buckets)
        {
            if (bucket.spaceAbove && !lastInsertedASpace)
            {
                lines.add("");
            }

            lines.addAll(bucket.lines);

            if (bucket.spaceBelow)
            {
                lines.add("");
                lastInsertedASpace = true;
            }
            else
            {
                lastInsertedASpace = false;
            }
        }

        if (lines.getLast().equals("")) lines.removeLast();

        return lines;
    }

    /**
     * Represents a packet of lines added to the scoreboard.
     */
    private static class LinesBucket
    {
        private static int inserts = 0;

        private final int insertOrder;
        private final SidebarPriority priority;
        private final boolean spaceAbove;
        private final boolean spaceBelow;
        private final List<String> lines;

        private LinesBucket(final SidebarPriority priority, final boolean spaceAbove, final boolean spaceBelow, final List<String> lines)
        {
            this.insertOrder = ++inserts;

            this.priority = priority;
            this.spaceAbove = spaceAbove;
            this.spaceBelow = spaceBelow;
            this.lines = lines;
        }
    }

    /**
     * Comparator for {@link LinesBucket}.
     */
    protected class LinesBucketComparator implements Comparator<LinesBucket>
    {
        @Override
        public int compare(final LinesBucket bucket1, final LinesBucket bucket2)
        {
            final int priorityComparison = Integer.compare(bucket1.priority.ordinal(), bucket2.priority.ordinal());

            if (priorityComparison != 0) return priorityComparison;
            else return Integer.compare(bucket1.insertOrder, bucket2.insertOrder);
        }
    }

    /**
     * The priority is used to order the lines given by various modules in the
     * sidebar. Lines in the same priority bucket will be displayed in an order
     * dependant on the modules's loading order.
     */
    public enum SidebarPriority
    {
        /**
         * Places the lines at the very top of the scoreboard.
         */
        VERY_TOP,

        /**
         * Places the lines on the top part of the scoreboard.
         */
        TOP,

        /**
         * Places the lines somewhere between the top and the middle.
         */
        MIDDLE_TOP,

        /**
         * Places the lines at the middle, without any strong preferences on placement.
         */
        MIDDLE,

        /**
         * Places the lines somewhere between the middle and the bottom.
         */
        MIDDLE_BOTTOM,

        /**
         * Places the lines on the bottom part of the scoreboard.
         */
        BOTTOM,

        /**
         * Places the lines at the very bottom of the scoreboard.
         */
        VERY_BOTTOM
    }
}
