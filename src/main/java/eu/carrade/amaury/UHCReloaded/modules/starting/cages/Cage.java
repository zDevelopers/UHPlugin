/*
 * Copyright or © or Copr. AmauryCarrade (2015)
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
package eu.carrade.amaury.UHCReloaded.modules.starting.cages;

import fr.zcraft.zteams.ZTeam;
import fr.zcraft.zteams.colors.ColorsUtils;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.material.MaterialData;

import java.util.HashMap;
import java.util.Map;


public class Cage
{
    private final Location baseLocation;

    private Material material = Material.BARRIER;
    private MaterialData materialData = null;

    private final boolean buildCeiling;
    private final boolean visibleWalls;

    private int radius = 1;
    private int internalHeight = 3;

    private boolean built = false;
    private Map<Location, SimpleBlock> blocksBuilt = new HashMap<>();


    /**
     * @param baseLocation The cage base location (where a player can be
     *                     teleported to be on the ground).
     * @param buildCeiling {@code true} to build the ceiling of the cage.
     */
    public Cage(Location baseLocation, boolean buildCeiling, boolean visibleWalls)
    {
        this.baseLocation = baseLocation;
        this.buildCeiling = buildCeiling;
        this.visibleWalls = visibleWalls;
    }

    /**
     * Sets the custom material to use.
     *
     * @param customMaterial A material.
     * @param data           The data value (or {@code null}).
     */
    public void setCustomMaterial(Material customMaterial, MaterialData data)
    {
        this.material = customMaterial == null ? Material.BARRIER : customMaterial;
        this.materialData = data;
    }

    /**
     * Sets the custom material to use.
     *
     * @param customMaterial A material.
     * @param data           The data value.
     */
    public void setCustomMaterial(Material customMaterial, byte data)
    {
        setCustomMaterial(customMaterial, new MaterialData(this.material, data));
    }

    /**
     * Sets the custom material to use.
     *
     * @param customMaterial A material.
     */
    public void setCustomMaterial(Material customMaterial)
    {
        setCustomMaterial(customMaterial, null);
    }


    /**
     * Sets the internal height, i.e. the height of the open space for players
     * (the ceiling will be above this height, and the ground under).
     *
     * @param internalHeight The height.
     */
    public void setInternalHeight(int internalHeight)
    {
        this.internalHeight = internalHeight;
    }

    /**
     * Sets the square radius of the cage.
     *
     * With 0, you'll have a cage with one block to walk. With 1, you'll have a
     * 3×3 cage. With 2, a 5×5 cage. Etc.
     *
     * @param radius The radius.
     */
    public void setRadius(int radius)
    {
        this.radius = radius;
    }

    /**
     * Sets a block (and remembers the old one to clean up things after).
     *
     * @param location The location
     * @param material The block material
     */
    private void setBlock(final Location location, final Material material)
    {
        setBlock(location, material, null);
    }

    /**
     * Sets a block (and remembers the old one to clean up things after).
     *
     * @param location The location
     * @param material The block material
     * @param data     The block data value (as byte)
     */
    private void setBlock(final Location location, final Material material, final byte data)
    {
        setBlock(location, material, new MaterialData(material, data));
    }

    /**
     * Sets a block (and remembers the old one to clean up things after).
     *
     * @param location The location
     * @param material The block material
     * @param data     The block data value (as {@link MaterialData})
     */
    private void setBlock(final Location location, final Material material, final MaterialData data)
    {
        final Block block = location.getBlock();

        if (!blocksBuilt.containsKey(location))
            blocksBuilt.put(location, new SimpleBlock(block.getType(), block.getState().getData().clone()));

        block.setType(material);
        if (data != null) block.setData(data.getData());
    }


    /**
     * Builds the cage.
     */
    public boolean build()
    {
        if (built) return false;

        final int externalRadius = radius + 1;
        final int xMin = baseLocation.getBlockX() - externalRadius;
        final int xMax = baseLocation.getBlockX() + externalRadius;
        final int zMin = baseLocation.getBlockZ() - externalRadius;
        final int zMax = baseLocation.getBlockZ() + externalRadius;

        final World world = baseLocation.getWorld();


        // Builds the base barrier square under any cage, to support falling blocks and to avoid players falling
        // through the blocks when teleported

        for (int x = xMin; x <= xMax; x++)
            for (int z = zMin; z <= zMax; z++)
                setBlock(new Location(world, x, baseLocation.getBlockY() - 2, z), Material.BARRIER);


        // Builds the ground

        for (int x = xMin + 1; x <= xMax - 1; x++)
            for (int z = zMin + 1; z <= zMax - 1; z++)
                setBlock(new Location(world, x, baseLocation.getBlockY() - 1, z), material, materialData);


        // Builds the walls

        final Material wallsMaterial = visibleWalls ? material : Material.BARRIER;
        final MaterialData wallsMaterialData = visibleWalls ? materialData : null;

        for (int x = xMin; x <= xMax; x++)
        {
            for (int y = baseLocation.getBlockY() - 1; y < baseLocation.getBlockY() + internalHeight; y++)
            {
                setBlock(new Location(world, x, y, zMin), wallsMaterial, wallsMaterialData);
                setBlock(new Location(world, x, y, zMax), wallsMaterial, wallsMaterialData);
            }
        }

        for (int z = zMin; z <= zMax; z++)
        {
            for (int y = baseLocation.getBlockY() - 1; y < baseLocation.getBlockY() + internalHeight; y++)
            {
                setBlock(new Location(world, xMin, y, z), wallsMaterial, wallsMaterialData);
                setBlock(new Location(world, xMax, y, z), wallsMaterial, wallsMaterialData);
            }
        }


        // Builds the ceiling

        final Material ceilingMaterial = buildCeiling ? material : Material.BARRIER;
        final MaterialData ceilingMaterialData = buildCeiling ? materialData : null;

        int xMinCeiling = xMin, xMaxCeiling = xMax, zMinCeiling = zMin, zMaxCeiling = zMax;

        if (buildCeiling && !visibleWalls)
        {
            xMinCeiling++;
            xMaxCeiling--;
            zMinCeiling++;
            zMaxCeiling--;
        }

        for (int x = xMinCeiling; x <= xMaxCeiling; x++)
            for (int z = zMinCeiling; z <= zMaxCeiling; z++)
                setBlock(new Location(world, x, baseLocation.getBlockY() + internalHeight, z), ceilingMaterial, ceilingMaterialData);

        built = true;

        return true;
    }

    /**
     * Destroys the cage.
     */
    public void destroy()
    {
        if (!built) return;

        for (final Map.Entry<Location, SimpleBlock> entry : blocksBuilt.entrySet())
        {
            final Block block = entry.getKey().getBlock();
            final SimpleBlock originalBlock = entry.getValue();

            block.setType(originalBlock.material);

            if (originalBlock.data != null)
                block.getState().setData(originalBlock.data);
        }

        built = false;
    }


    /**
     * Creates a Cage instance for the given team.
     *
     * @param team The team. Can be null (fallbacks to white if a color is needed).
     * @param location Where the cage should be built.
     *
     * @return A Cage
     */
    static public Cage createInstanceForTeam(final ZTeam team, final Location location)
    {
        final Material cageMaterial;
        final Byte cageData;

        switch (Config.TYPE.get())
        {
            case TEAM_COLOR_TRANSPARENT:
                cageMaterial = Material.STAINED_GLASS;
                cageData = ColorsUtils.chat2Dye(team != null ? team.getColorOrWhite().toChatColor() : ChatColor.WHITE).getWoolData();
                break;

            case TEAM_COLOR_SOLID:
                cageMaterial = Material.STAINED_CLAY;
                cageData = ColorsUtils.chat2Dye(team != null ? team.getColorOrWhite().toChatColor() : ChatColor.WHITE).getWoolData();
                break;

            case CUSTOM:
                cageMaterial = Config.CUSTOM_BLOCK.get();
                cageData = null;
                break;

            // Should never happen
            default:
                cageMaterial = null;
                cageData = null;
        }

        final Cage cage = new Cage(location, Config.BUILD_CEILING.get(), Config.VISIBLE_WALLS.get());

        if (cageMaterial != null) // Should always be true
        {
            cage.setCustomMaterial(cageMaterial, cageData != null ? cageData : 0);
            cage.setInternalHeight(Config.HEIGHT.get());
            cage.setRadius(Config.RADIUS.get());
        }

        return cage;
    }


    /**
     * Cage type, enum used for the configuration
     */
    public enum CageType
    {
        /**
         * Cages in stained glass, using the team color (or the closest color
         * available).
         */
        TEAM_COLOR_TRANSPARENT,

        /**
         * Cages in stained hardened clay, using the team color (or the closest
         * color available).
         */
        TEAM_COLOR_SOLID,

        /**
         * Cages in a custom provided block.
         */
        CUSTOM
    }

    /**
     * A block + data value (storage class used to restore old blocks when the
     * cage is destroyed).
     */
    private class SimpleBlock
    {
        public Material material;
        public MaterialData data;

        public SimpleBlock(Material material, MaterialData data)
        {
            this.material = material;
            this.data = data;
        }
    }
}
