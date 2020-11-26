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

package eu.carrade.amaury.quartzsurvivalgames.modules.starting.cages;

import fr.zcraft.quartzlib.tools.items.ColorableMaterial;
import fr.zcraft.quartzlib.tools.items.ItemUtils;
import fr.zcraft.quartzteams.QuartzTeam;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;


public class Cage {
    private final Location baseLocation;
    private final boolean buildCeiling;
    private final boolean visibleWalls;
    private final Map<Location, SimpleBlock> blocksBuilt = new HashMap<>();
    private Material material = Material.BARRIER;
    private int radius = 1;
    private int internalHeight = 3;
    private boolean built = false;


    /**
     * @param baseLocation The cage base location (where a player can be
     *                     teleported to be on the ground).
     * @param buildCeiling {@code true} to build the ceiling of the cage.
     */
    public Cage(Location baseLocation, boolean buildCeiling, boolean visibleWalls) {
        this.baseLocation = baseLocation;
        this.buildCeiling = buildCeiling;
        this.visibleWalls = visibleWalls;
    }

    /**
     * Creates a Cage instance for the given team.
     *
     * @param team     The team. Can be null (fallbacks to white if a color is needed).
     * @param location Where the cage should be built.
     * @return A Cage
     */
    static public Cage createInstanceForTeam(final QuartzTeam team, final Location location) {
        final Material cageMaterial;

        switch (Config.TYPE.get()) {
            case TEAM_COLOR_TRANSPARENT:
                cageMaterial = ItemUtils.colorize(
                        ColorableMaterial.STAINED_GLASS, team != null ? team.getColorOrWhite().toChatColor() : ChatColor.WHITE);
                break;

            case TEAM_COLOR_SOLID:
                cageMaterial = ItemUtils.colorize(ColorableMaterial.TERRACOTTA, team != null ? team.getColorOrWhite().toChatColor() : ChatColor.WHITE);
                break;

            case TEAM_COLOR_FANCY:
                cageMaterial = ItemUtils.colorize(ColorableMaterial.GLAZED_TERRACOTTA, team != null ? team.getColorOrWhite().toChatColor() : ChatColor.WHITE);
                break;

            case CUSTOM:
                cageMaterial = Config.CUSTOM_BLOCK.get();
                break;

            // Should never happen
            default:
                cageMaterial = null;
        }

        final Cage cage = new Cage(location, Config.BUILD_CEILING.get(), Config.VISIBLE_WALLS.get());

        if (cageMaterial != null) // Should always be true
        {
            cage.setCustomMaterial(cageMaterial);
            cage.setInternalHeight(Config.HEIGHT.get());
            cage.setRadius(Config.RADIUS.get());
        }

        return cage;
    }

    /**
     * Sets the custom material to use.
     *
     * @param customMaterial A material.
     */
    public void setCustomMaterial(Material customMaterial) {
        this.material = customMaterial == null ? Material.BARRIER : customMaterial;
    }

    /**
     * Sets the internal height, i.e. the height of the open space for players
     * (the ceiling will be above this height, and the ground under).
     *
     * @param internalHeight The height.
     */
    public void setInternalHeight(int internalHeight) {
        this.internalHeight = internalHeight;
    }

    /**
     * Sets the square radius of the cage.
     * <p>
     * With 0, you'll have a cage with one block to walk. With 1, you'll have a
     * 3×3 cage. With 2, a 5×5 cage. Etc.
     *
     * @param radius The radius.
     */
    public void setRadius(int radius) {
        this.radius = radius;
    }

    /**
     * Sets a block (and remembers the old one to clean up things after).
     *
     * @param location The location
     * @param material The block material
     */
    private void setBlock(final Location location, final Material material) {
        final Block block = location.getBlock();

        if (!blocksBuilt.containsKey(location)) {
            blocksBuilt.put(location, new SimpleBlock(block.getType(), block.getState().getBlockData().clone()));
        }

        block.setType(material);
    }

    /**
     * Builds the cage.
     */
    public boolean build() {
        if (built) {
            return false;
        }

        final int externalRadius = radius + 1;
        final int xMin = baseLocation.getBlockX() - externalRadius;
        final int xMax = baseLocation.getBlockX() + externalRadius;
        final int zMin = baseLocation.getBlockZ() - externalRadius;
        final int zMax = baseLocation.getBlockZ() + externalRadius;

        final World world = baseLocation.getWorld();


        // Builds the base barrier square under any cage, to support falling blocks and to avoid players falling
        // through the blocks when teleported

        for (int x = xMin; x <= xMax; x++) {
            for (int z = zMin; z <= zMax; z++) {
                setBlock(new Location(world, x, baseLocation.getBlockY() - 2, z), Material.BARRIER);
            }
        }


        // Builds the ground

        for (int x = xMin + 1; x <= xMax - 1; x++) {
            for (int z = zMin + 1; z <= zMax - 1; z++) {
                setBlock(new Location(world, x, baseLocation.getBlockY() - 1, z), material);
            }
        }


        // Builds the walls

        final Material wallsMaterial = visibleWalls ? material : Material.BARRIER;

        for (int x = xMin; x <= xMax; x++) {
            for (int y = baseLocation.getBlockY() - 1; y < baseLocation.getBlockY() + internalHeight; y++) {
                setBlock(new Location(world, x, y, zMin), wallsMaterial);
                setBlock(new Location(world, x, y, zMax), wallsMaterial);
            }
        }

        for (int z = zMin; z <= zMax; z++) {
            for (int y = baseLocation.getBlockY() - 1; y < baseLocation.getBlockY() + internalHeight; y++) {
                setBlock(new Location(world, xMin, y, z), wallsMaterial);
                setBlock(new Location(world, xMax, y, z), wallsMaterial);
            }
        }


        // Builds the ceiling

        final Material ceilingMaterial = buildCeiling ? material : Material.BARRIER;

        int xMinCeiling = xMin, xMaxCeiling = xMax, zMinCeiling = zMin, zMaxCeiling = zMax;

        if (buildCeiling && !visibleWalls) {
            xMinCeiling++;
            xMaxCeiling--;
            zMinCeiling++;
            zMaxCeiling--;
        }

        for (int x = xMinCeiling; x <= xMaxCeiling; x++) {
            for (int z = zMinCeiling; z <= zMaxCeiling; z++) {
                setBlock(new Location(world, x, baseLocation.getBlockY() + internalHeight, z), ceilingMaterial);
            }
        }

        built = true;

        return true;
    }

    /**
     * Destroys the cage.
     */
    public void destroy() {
        if (!built) {
            return;
        }

        for (final Map.Entry<Location, SimpleBlock> entry : blocksBuilt.entrySet()) {
            final Block block = entry.getKey().getBlock();
            final SimpleBlock originalBlock = entry.getValue();

            block.setType(originalBlock.material);

            if (originalBlock.data != null) {
                block.getState().setBlockData(originalBlock.data);
            }
        }

        built = false;
    }


    /**
     * Cage type, enum used for the configuration
     */
    public enum CageType {
        /**
         * Cages in stained glass, using the team color (or the closest color
         * available).
         */
        TEAM_COLOR_TRANSPARENT,

        /**
         * Cages in stained terracotta clay, using the team color (or the closest
         * color available).
         */
        TEAM_COLOR_SOLID,

        /**
         * Cages in stained glazed terracotta, using the team color (or the closest
         * color available).
         */
        TEAM_COLOR_FANCY,

        /**
         * Cages in a custom provided block.
         */
        CUSTOM
    }

    /**
     * A block + data value (storage class used to restore old blocks when the
     * cage is destroyed).
     */
    private class SimpleBlock {
        public Material material;
        public BlockData data;

        public SimpleBlock(Material material, BlockData data) {
            this.material = material;
            this.data = data;
        }
    }
}
