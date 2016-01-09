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

package eu.carrade.amaury.UHCReloaded.integration;

import eu.carrade.amaury.UHCReloaded.UHCReloaded;
import fr.zcraft.zlib.tools.PluginLogger;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;


public class UHProtocolLibIntegrationWrapper
{

    private UHCReloaded p = null;
    private UHProtocolLibIntegration integration = null;

    public UHProtocolLibIntegrationWrapper(UHCReloaded p)
    {
        this.p = p;

        // Needed to avoid a NoClassDefFoundError.
        // I don't like this way of doing this, but else, the plugin will not load without ProtocolLib.

        Plugin pl = Bukkit.getServer().getPluginManager().getPlugin("ProtocolLib");
        if (pl != null && pl.isEnabled())
        {
            try
            {
                integration = new UHProtocolLibIntegration(p);
            }
            catch (NoClassDefFoundError e)
            {
                PluginLogger.error("ProtocolLib is present but cannot be loaded (outdated?), so the integration was disabled.", e);
            }
        }
        else
        {
            PluginLogger.warning("ProtocolLib is not present, so the integration was disabled.");
        }
    }

    /**
     * Returns true if ProtocolLib is installed and integrated into the plugin.
     * @return
     */
    public boolean isProtocolLibIntegrationEnabled()
    {
        return (this.integration != null);
    }

    /**
     * Checks if there are some enabled option which require ProtocolLib.
     *
     * @return A list of enabled options which requires ProtocolLib, or null
     * if there isn't any enabled option that requires ProtocolLib.
     */
    public List<String> isProtocolLibNeeded()
    {

        ArrayList<String> options = new ArrayList<String>();
        options.add("hardcore-hearts.display");
        options.add("auto-respawn.do");

        ArrayList<String> enabledOptions = new ArrayList<String>();

        for (String option : options)
        {
            if (p.getConfig().getBoolean(option))
            {
                enabledOptions.add(option);
            }
        }

        if (enabledOptions.size() != 0)
        {
            return enabledOptions;
        }
        else
        {
            return null;
        }
    }

    /**
     * Returns the wrapped integration.
     *
     * @return
     */
    public UHProtocolLibIntegration getIntegration()
    {
        return integration;
    }
}
