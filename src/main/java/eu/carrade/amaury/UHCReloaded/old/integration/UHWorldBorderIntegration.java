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

package eu.carrade.amaury.UHCReloaded.old.integration;

import com.wimbli.WorldBorder.WorldBorder;
import fr.zcraft.zlib.tools.PluginLogger;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;


public class UHWorldBorderIntegration
{
    private WorldBorder wb = null;

    public UHWorldBorderIntegration()
    {
        Plugin wbTest = Bukkit.getServer().getPluginManager().getPlugin("WorldBorder");
        if (wbTest == null || !wbTest.isEnabled())
        {
            PluginLogger.warning("WorldBorder is not present, so the integration was disabled.");
            return;
        }

        this.wb = (WorldBorder) wbTest;

        try
        {
            Class.forName("com.wimbli.WorldBorder.BorderData");
            Class.forName("com.wimbli.WorldBorder.Config");
        }
        catch (ClassNotFoundException e)
        {
            PluginLogger.warning("WorldBorder is available, but the version you are using is too old.");
            PluginLogger.warning("This plugin is tested and works with WorldBorder 1.8.0 or later.");

            this.wb = null;
            return;
        }

        PluginLogger.info("Successfully hooked into WorldBorder.");
    }

    public boolean isWBIntegrationEnabled()
    {
        return !(this.wb == null);
    }

    public WorldBorder getWorldBorder()
    {
        return wb;
    }
}
