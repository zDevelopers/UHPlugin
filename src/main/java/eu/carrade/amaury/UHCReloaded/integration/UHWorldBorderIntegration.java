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

import com.wimbli.WorldBorder.BorderData;
import com.wimbli.WorldBorder.Config;
import com.wimbli.WorldBorder.WorldBorder;
import eu.carrade.amaury.UHCReloaded.UHCReloaded;
import eu.carrade.amaury.UHCReloaded.borders.MapShape;
import fr.zcraft.zlib.tools.PluginLogger;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.plugin.Plugin;


public class UHWorldBorderIntegration
{

    private UHCReloaded p = null;
    private WorldBorder wb = null;

    public UHWorldBorderIntegration(UHCReloaded p)
    {
        this.p = p;

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


        // All is good, let's integrate.
        setupBorders();

        PluginLogger.info("Successfully hooked into WorldBorder.");
    }

    public void setupBorders()
    {
        if (!isWBIntegrationEnabled())
        {
            return;
        }

        /** General configuration **/

        Config.setPortalRedirection(true); // Because the nether is border-less.


        /** Overworld border **/

        World overworld = getOverworld();
        BorderData borderOverworld = wb.getWorldBorder(overworld.getName());

        if (borderOverworld == null)
        { // The border needs to be created from scratch
            borderOverworld = new BorderData(0, 0, 0); // Random values, overwritten later.
        }

        borderOverworld.setShape(p.getBorderManager().getMapShape() == MapShape.CIRCULAR);

        borderOverworld.setX(overworld.getSpawnLocation().getX()); // A border centered on the spawn point
        borderOverworld.setZ(overworld.getSpawnLocation().getZ());

        borderOverworld.setRadius((int) Math.floor(p.getBorderManager().getCheckDiameter() / 2));

        Config.setBorder(overworld.getName(), borderOverworld);

        PluginLogger.info("Overworld border configured using WorldBorder (world '{0}').", overworld.getName());


        /** Nether border **/

        // There is not any border set for the Nether, because WorldBorder handles portal redirection
        // if a player rebuild a portal far from the Nether' spawn point.
    }

    /**
     * Returns the overworld.
     *
     * @return the... overworld?
     */
    private World getOverworld()
    {
        for (World world : Bukkit.getServer().getWorlds())
        {
            if (world.getEnvironment() != Environment.NETHER && world.getEnvironment() != Environment.THE_END)
            {
                return world;
            }
        }
        return null;
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
