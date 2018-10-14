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
package eu.carrade.amaury.UHCReloaded;

import eu.carrade.amaury.UHCReloaded.modules.core.sidebar.SidebarInjector;
import org.junit.Assert;
import org.junit.ComparisonFailure;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;


public class SidebarInjectorTest
{
    private static<T> void assertListsEquals(final String message, final List<T> expected, final List<T> actual)
    {
        if (expected == actual) return;

        if (expected.size() != actual.size())
            Assert.fail(message + " - Lists size differ (expected <" + expected.size() + "> but got <" + actual.size() + ">)");

        for (int i = 0; i < expected.size(); i++)
        {
            final T expectedValue = expected.get(i);
            final T actualValue = actual.get(i);

            if (!expectedValue.equals(actualValue))
            {
                if (expectedValue instanceof String && actualValue instanceof String)
                {
                    final String completeMessage = (message == null ? "" : message + " - ") + "Lists first differ at index " + i + ":";
                    throw new ComparisonFailure(completeMessage, (String) expectedValue, (String) actualValue);
                }
                else
                {
                    Assert.fail((message == null ? "" : message + " - ") + "Lists first differ at index " + i + ": expected <" + expected.toString() + "> but got <" + actual.toString() + ">");
                }
            }
        }
    }

    @Test
    public void testBasicInjector()
    {
        assertListsEquals(
                "Items added without priority or spacing should be left as is and in order",
                Arrays.asList("Line 1", "Line 2", "Line 3", "Line 4", "Line 5", "Line 6"),
                new SidebarInjector()
                        .injectLines("Line 1", "Line 2", "Line 3")
                        .injectLines("Line 4")
                        .injectLines("Line 5", "Line 6")
                        .buildLines()
        );
    }

    @Test
    public void testPriorityInInjector()
    {
        assertListsEquals(
                "Items added with priority should be ordered according to theses, regardless of the inclusion order",
                Arrays.asList("Line 4", "Line 1", "Line 2", "Line 3", "Line 5", "Line 6"),
                new SidebarInjector()
                        .injectLines("Line 1", "Line 2", "Line 3")
                        .injectLines(SidebarInjector.SidebarPriority.TOP,"Line 4")
                        .injectLines("Line 5", "Line 6")
                        .buildLines()
        );

        assertListsEquals(
                "Items added with multiple priorities should be ordered according to theses, regardless of the inclusion order",
                Arrays.asList("Line 5", "Line 6", "Line 4", "Line 1", "Line 2", "Line 3"),
                new SidebarInjector()
                        .injectLines(SidebarInjector.SidebarPriority.BOTTOM, "Line 1", "Line 2", "Line 3")
                        .injectLines(SidebarInjector.SidebarPriority.TOP,"Line 4")
                        .injectLines(SidebarInjector.SidebarPriority.VERY_TOP, "Line 5", "Line 6")
                        .buildLines()
        );

        assertListsEquals(
                "Items added without priority or spacing should be left as is and in order",
                Arrays.asList("Line 1", "Line 2", "Line 4", "Line 3", "Line 5", "Line 6"),
                new SidebarInjector()
                        .injectLines(SidebarInjector.SidebarPriority.VERY_BOTTOM, "Line 6")
                        .injectLines(SidebarInjector.SidebarPriority.BOTTOM, "Line 5")
                        .injectLines(SidebarInjector.SidebarPriority.MIDDLE, "Line 4")
                        .injectLines("Line 3")
                        .injectLines(SidebarInjector.SidebarPriority.TOP, "Line 2")
                        .injectLines(SidebarInjector.SidebarPriority.VERY_TOP, "Line 1")
                        .buildLines()
        );
    }

    @Test
    public void testSpacesInInjector()
    {
        assertListsEquals(
                "If spaces are required around the only element, they are not added",
                Collections.singletonList("Line 1"),
                new SidebarInjector()
                        .injectLines(true, "Line 1")
                        .buildLines()
        );

        assertListsEquals(
                "If spaces are required above the first element, it is not added",
                Arrays.asList("Line 1", "Line 2", "Line 3"),
                new SidebarInjector()
                        .injectLines(true, false, "Line 1")
                        .injectLines("Line 2", "Line 3")
                        .buildLines()
        );

        assertListsEquals(
                "If spaces are required around the first element, it is added after but not before",
                Arrays.asList("Line 1", "", "Line 2", "Line 3"),
                new SidebarInjector()
                        .injectLines(true, "Line 1")
                        .injectLines("Line 2", "Line 3")
                        .buildLines()
        );

        assertListsEquals(
                "If spaces are required below the last element, it is not added",
                Arrays.asList("Line 1", "Line 2", "Line 3"),
                new SidebarInjector()
                        .injectLines("Line 1")
                        .injectLines(false, true, "Line 2", "Line 3")
                        .buildLines()
        );

        assertListsEquals(
                "If spaces are required around the last element, it is added before but not after",
                Arrays.asList("Line 1", "Line 2", "", "Line 3", "Line 4"),
                new SidebarInjector()
                        .injectLines("Line 1", "Line 2")
                        .injectLines(true, "Line 3", "Line 4")
                        .buildLines()
        );

        assertListsEquals(
                "If spaces are required after an element and before the next, it is added only once",
                Arrays.asList("Line 1", "Line 2", "", "Line 3", "Line 4"),
                new SidebarInjector()
                        .injectLines(true, "Line 1", "Line 2")
                        .injectLines(true, "Line 3", "Line 4")
                        .buildLines()
        );
    }

    @Test
    public void testSpacesAndPrioritiesInInjector()
    {
        assertListsEquals(
                "If spaces are required above the first element, it is not added (with priorities)",
                Arrays.asList("Line 1", "Line 2", "Line 3"),
                new SidebarInjector()
                        .injectLines("Line 2", "Line 3")
                        .injectLines(SidebarInjector.SidebarPriority.TOP, true, false, "Line 1")
                        .buildLines()
        );

        assertListsEquals(
                "If spaces are required around the first element, it is added after but not before",
                Arrays.asList("Line 1", "", "Line 2", "Line 3"),
                new SidebarInjector()
                        .injectLines("Line 2", "Line 3")
                        .injectLines(SidebarInjector.SidebarPriority.VERY_TOP, true, "Line 1")
                        .buildLines()
        );

        assertListsEquals(
                "If spaces are required below the last element, it is not added",
                Arrays.asList("Line 1", "Line 2", "Line 3"),
                new SidebarInjector()
                        .injectLines(SidebarInjector.SidebarPriority.BOTTOM, false, true, "Line 2", "Line 3")
                        .injectLines("Line 1")
                        .buildLines()
        );

        assertListsEquals(
                "If spaces are required around the last element, it is added before but not after",
                Arrays.asList("Line 1", "Line 2", "", "Line 3", "Line 4"),
                new SidebarInjector()
                        .injectLines(SidebarInjector.SidebarPriority.VERY_BOTTOM, true, "Line 3", "Line 4")
                        .injectLines("Line 1", "Line 2")
                        .buildLines()
        );

        assertListsEquals(
                "If spaces are required after an element and before the next, it is added only once",
                Arrays.asList("Line 1", "Line 2", "", "Line 3", "Line 4"),
                new SidebarInjector()
                        .injectLines(true, "Line 1", "Line 2")
                        .injectLines(SidebarInjector.SidebarPriority.MIDDLE,true, "Line 3", "Line 4")
                        .buildLines()
        );
    }
}
