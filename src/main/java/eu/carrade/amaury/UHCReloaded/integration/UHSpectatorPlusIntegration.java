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

import com.pgcraft.spectatorplus.SpectateAPI;
import com.pgcraft.spectatorplus.SpectatorPlus;
import eu.carrade.amaury.UHCReloaded.UHCReloaded;
import fr.zcraft.zlib.tools.PluginLogger;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;


public class UHSpectatorPlusIntegration
{

    private UHCReloaded p = null;
    private SpectatorPlus sp = null;
    private SpectateAPI spAPI = null;

    public UHSpectatorPlusIntegration(UHCReloaded p)
    {
        this.p = p;

        Plugin spTest = Bukkit.getServer().getPluginManager().getPlugin("SpectatorPlus");
        if (spTest == null || !spTest.isEnabled())
        {
            this.p.getLogger().warning("SpectatorPlus is not present, so the integration was disabled.");
            return;
        }

        this.sp = (SpectatorPlus) spTest;


        try
        {
            Class.forName("com.pgcraft.spectatorplus.SpectateAPI");

            if (sp.getDescription().getVersion().equals("1.9.1"))
            {
                // The API of SpectatorPlus 1.9.1 was not working.
                throw new ClassNotFoundException();
            }
        }
        catch (ClassNotFoundException e)
        {
            PluginLogger.warning("SpectatorPlus is available, but the version you are using is too old.");
            PluginLogger.warning("This plugin is tested and works with SpectatorPlus 1.9.2 or later. The SpectateAPI is needed.");

            this.sp = null;
            return;
        }


        // All is OK, let's integrate.
        try
        {
            spAPI = sp.getAPI();

            spAPI.setCompass(true, true);
            spAPI.setSpectateOnDeath(true, true);
            spAPI.setColouredTabList(false, true); // potential conflict with our scoreboard

            PluginLogger.info("Successfully hooked into SpectatorPlus.");
        }

        // Generic catch block to catch any kind of exception (logged, anyway), including e.g.
        // NoSuchMethodError, if the API change, so the plugin is not broken.
        catch (Throwable e)
        {
            PluginLogger.error("Cannot hook into SpectatorPlus, is this version compatible?", e);

            spAPI = null;
            sp = null;
        }
    }

    public boolean isSPIntegrationEnabled()
    {
        return !(this.sp == null);
    }

    public SpectatorPlus getSP()
    {
        return this.sp;
    }

    public SpectateAPI getSPAPI()
    {
        return this.spAPI;
    }
}
