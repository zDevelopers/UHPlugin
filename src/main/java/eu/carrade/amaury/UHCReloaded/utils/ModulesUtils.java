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
package eu.carrade.amaury.UHCReloaded.utils;

import org.apache.commons.lang.StringUtils;

import java.util.Arrays;
import java.util.List;


public class ModulesUtils
{
    /**
     * Tries to find a class from its name, by combining suffixes, packages, capitalization.
     *
     * @param name The class base name to search for.
     * @param optionalPackage An optional package to search in.
     * @param optionalSuffix An optional suffix to test, appended to the class name.
     * @param superClass The superclass this class must have.
     * @param <T> The superclass type this class must have.
     *
     * @return The {@link Class}, if found; else, {@code null}.
     */
    public static <T> Class<? extends T> getClassFromName(final String name, final String optionalPackage, final String optionalSuffix, final Class<T> superClass)
    {
        final List<String> possibilities = Arrays.asList(
                optionalPackage + "." + name,
                optionalPackage + "." + name + optionalSuffix,
                optionalPackage + "." + StringUtils.capitalize(name),
                optionalPackage + "." + StringUtils.capitalize(name) + optionalSuffix,
                optionalPackage + "." + StringUtils.capitalize(name.toLowerCase()),
                optionalPackage + "." + StringUtils.capitalize(name.toLowerCase()) + optionalSuffix,
                optionalPackage + "." + StringUtils.uncapitalize(name) + "." + name,
                optionalPackage + "." + StringUtils.uncapitalize(name) + "." + name + optionalSuffix,
                optionalPackage + "." + StringUtils.uncapitalize(name) + "." + StringUtils.capitalize(name),
                optionalPackage + "." + StringUtils.uncapitalize(name) + "." + StringUtils.capitalize(name) + optionalSuffix,
                optionalPackage + "." + StringUtils.uncapitalize(name) + "." + optionalSuffix,
                name
        );

        for (String clazzName : possibilities)
        {
            try
            {
                final Class clazz = Class.forName(clazzName);
                if (superClass.isAssignableFrom(clazz)) return (Class<? extends T>) clazz;
            }
            catch (ClassNotFoundException e) { /* The search continues... */ }
        }

        return null;
    }
}
