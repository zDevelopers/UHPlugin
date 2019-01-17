/*
 * Plugin UHCReloaded : Alliances
 *
 * Copyright ou © ou Copr. Amaury Carrade (2016)
 * Idées et réflexions : Alexandre Prokopowicz, Amaury Carrade, "Vayan".
 *
 * Ce logiciel est régi par la licence CeCILL soumise au droit français et
 * respectant les principes de diffusion des logiciels libres. Vous pouvez
 * utiliser, modifier et/ou redistribuer ce programme sous les conditions
 * de la licence CeCILL telle que diffusée par le CEA, le CNRS et l'INRIA
 * sur le site "http://www.cecill.info".
 *
 * En contrepartie de l'accessibilité au code source et des droits de copie,
 * de modification et de redistribution accordés par cette licence, il n'est
 * offert aux utilisateurs qu'une garantie limitée.  Pour les mêmes raisons,
 * seule une responsabilité restreinte pèse sur l'auteur du programme,  le
 * titulaire des droits patrimoniaux et les concédants successifs.
 *
 * A cet égard  l'attention de l'utilisateur est attirée sur les risques
 * associés au chargement,  à l'utilisation,  à la modification et/ou au
 * développement et à la reproduction du logiciel par l'utilisateur étant
 * donné sa spécificité de logiciel libre, qui peut le rendre complexe à
 * manipuler et qui le réserve donc à des développeurs et des professionnels
 * avertis possédant  des  connaissances  informatiques approfondies.  Les
 * utilisateurs sont donc invités à charger  et  tester  l'adéquation  du
 * logiciel à leurs besoins dans des conditions permettant d'assurer la
 * sécurité de leurs systèmes et ou de leurs données et, plus généralement,
 * à l'utiliser et l'exploiter dans les mêmes conditions de sécurité.
 *
 * Le fait que vous puissiez accéder à cet en-tête signifie que vous avez
 * pris connaissance de la licence CeCILL, et que vous en avez accepté les
 * termes.
 */
package eu.carrade.amaury.UHCReloaded.shortcuts;

import com.google.common.reflect.ClassPath;
import eu.carrade.amaury.UHCReloaded.UHCReloaded;
import eu.carrade.amaury.UHCReloaded.core.ModuleLogger;
import eu.carrade.amaury.UHCReloaded.core.ModulesManager;
import eu.carrade.amaury.UHCReloaded.core.UHModule;
import fr.zcraft.zlib.tools.PluginLogger;
import fr.zcraft.zlib.tools.reflection.Reflection;
import org.apache.commons.lang.ArrayUtils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


/**
 * Useful shortcuts.
 */
public final class UR
{
    private static final Map<Class<?>, Class<? extends UHModule>> classesModules = new HashMap<>();
    private static final Map<String, Class<? extends UHModule>> packagesModules = new HashMap<>();

    /**
     * Returns the plugin's instance.
     */
    public static UHCReloaded get()
    {
        return UHCReloaded.get();
    }

    /**
     * Gets a module's instance. This may return null if the module is not currently
     * loaded.
     *
     * @param moduleClass The module's class.
     * @param <M> The module's type.
     *
     * @return The module's instance.
     */
    public static <M extends UHModule> M module(final Class<M> moduleClass)
    {
        return ModulesManager.getModule(moduleClass);
    }

    /**
     * Returns the module for the caller class' module.
     *
     * This works by looking up for the module class in the caller class'
     * package or “parent” packages. Throws an exception if no module class can
     * be found.
     *
     * @return The module's instance.
     * @throws IllegalArgumentException if no module can be found for the caller
     * class.
     */
    public static UHModule module()
    {
        final Class<?> caller = Reflection.getCallerClass();
        if (caller == null) throw new IllegalArgumentException("Cannot extract caller class in module()");

        final Class<? extends UHModule> moduleClass = getModuleFromClass(caller);

        if (moduleClass != null) return module(moduleClass);

        throw new IllegalArgumentException("The class " + caller.getCanonicalName() + " is not inside a module's package.");
    }

    /**
     * Returns the logger for a given module. This may return null if the module is not
     * currently loaded.
     *
     * @param moduleClass The module's class.
     * @return The module's logger.
     */
    public static ModuleLogger log(final Class<? extends UHModule> moduleClass)
    {
        try
        {
            return module(moduleClass).log();
        }
        catch (final NullPointerException e)
        {
            // Ensures no NPE so IDEs are happy.
            return new ModuleLogger(UnknownModule.class);
        }
    }

    /**
     * Returns the logger for the caller class' module.
     *
     * This works by looking up for the module class in the caller class'
     * package or “parent” packages. Throws an exception if no module class can
     * be found.
     *
     * @return The module's logger.
     * @throws IllegalArgumentException if no module can be found for the caller
     * class.
     */
    public static ModuleLogger log()
    {
        final Class<?> caller = Reflection.getCallerClass();
        if (caller == null) throw new IllegalArgumentException("Cannot extract caller class in log()");

        final Class<? extends UHModule> moduleClass = getModuleFromClass(caller);

        if (moduleClass != null) return log(moduleClass);

        throw new IllegalArgumentException("The class " + caller.getCanonicalName() + " is not inside a module's package.");
    }

    /**
     * Tries to retrieve the module of a given class.
     *
     * It will lookup for a class extending {@link UHModule} in the class's
     * package, then in the “parent” package, etc., until the “root” package is
     * reached. {@code null} will be returned if no package can be found.
     *
     * The result of this method is cached at runtime.
     *
     * @param clazz The class to search the module of.
     * @return The module class, or {@code null} if not found.
     */
    private static Class<? extends UHModule> getModuleFromClass(final Class<?> clazz)
    {
        if (classesModules.containsKey(clazz)) return classesModules.get(clazz);

        try
        {
            final ClassPath classPath = ClassPath.from(clazz.getClassLoader());
            final Set<String> analyzedPackages = new HashSet<>();

            String packaj = clazz.getPackage().getName();
            while (packaj != null)
            {
                // Cached?
                if (packagesModules.containsKey(packaj)) return packagesModules.get(packaj);

                analyzedPackages.add(packaj);

                // We try to find a class in this package extending UHModule
                for (final ClassPath.ClassInfo packajClazzInfo : classPath.getTopLevelClasses(packaj))
                {
                    final Class<?> packajClazz = packajClazzInfo.load();

                    if (UHModule.class.isAssignableFrom(packajClazz))
                    {
                        // We found the One™.
                        @SuppressWarnings("unchecked")
                        final Class<? extends UHModule> moduleClazz = (Class<? extends UHModule>) packajClazz;

                        // We cache as hard as we can as these operations can be heavy.
                        analyzedPackages.forEach(analyzed -> packagesModules.put(analyzed, moduleClazz));
                        classesModules.put(clazz, moduleClazz);

                        return moduleClazz;
                    }
                }

                // If we fail, we try with the “parent” package.
                if (packaj.contains("."))
                {
                    final String[] packajParts = packaj.split("\\.");
                    ArrayUtils.remove(packajParts, packajParts.length - 1);
                    packaj = String.join(".", (String[]) ArrayUtils.remove(packajParts, packajParts.length - 1));
                }
                else
                {
                    packaj = null;
                }
            }

            PluginLogger.error("Unable to find module for class {0}", clazz.getCanonicalName());
            classesModules.put(clazz, null);
            analyzedPackages.forEach(analyzed -> packagesModules.put(analyzed, null));

            return null;
        }
        catch (final Throwable e)
        {
            PluginLogger.error("Unable to find module for class {0}", e, clazz.getCanonicalName());
            classesModules.put(clazz, null);
            return null;
        }
    }

    private class UnknownModule extends UHModule {}
}
