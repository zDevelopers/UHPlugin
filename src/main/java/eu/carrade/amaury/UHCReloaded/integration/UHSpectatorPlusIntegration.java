/**
 * Plugin UltraHardcore Reloaded (UHPlugin) Copyright (C) 2013 azenet Copyright (C) 2014-2015 Amaury
 * Carrade
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program.  If
 * not, see [http://www.gnu.org/licenses/].
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
