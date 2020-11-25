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
package eu.carrade.amaury.quartzsurvivalgames.modules.core.spawns;

import eu.carrade.amaury.quartzsurvivalgames.QuartzSurvivalGames;
import eu.carrade.amaury.quartzsurvivalgames.core.ModuleCategory;
import eu.carrade.amaury.quartzsurvivalgames.core.ModuleInfo;
import eu.carrade.amaury.quartzsurvivalgames.core.QSGModule;
import eu.carrade.amaury.quartzsurvivalgames.modules.core.border.BorderModule;
import eu.carrade.amaury.quartzsurvivalgames.modules.core.game.GameModule;
import eu.carrade.amaury.quartzsurvivalgames.modules.core.game.events.start.BeforeTeleportationPhaseEvent;
import eu.carrade.amaury.quartzsurvivalgames.modules.core.spawns.commands.SpawnsCommand;
import eu.carrade.amaury.quartzsurvivalgames.modules.core.spawns.exceptions.CannotGenerateSpawnPointsException;
import eu.carrade.amaury.quartzsurvivalgames.modules.core.spawns.exceptions.UnknownGeneratorException;
import eu.carrade.amaury.quartzsurvivalgames.modules.core.spawns.generators.SpawnPointsGenerator;
import eu.carrade.amaury.quartzsurvivalgames.shortcuts.QSG;
import eu.carrade.amaury.quartzsurvivalgames.utils.QSGUtils;
import fr.zcraft.quartzlib.components.commands.Command;
import fr.zcraft.zteams.ZTeams;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.util.Vector;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;


@ModuleInfo(
        name = "Spawns",
        description = "Manages the spawn points and allow users to generate them randomly.",
        category = ModuleCategory.CORE,
        icon = Material.MAP,
        settings = Config.class,
        internal = true,
        can_be_unloaded = false
)
public class SpawnsModule extends QSGModule
{
    private List<Location> spawnPoints = new LinkedList<>();

    @Override
    protected void onEnable()
    {
        spawnPoints.addAll(Config.SPAWN_POINTS);
    }

    @Override
    public List<Class<? extends Command>> getCommands()
    {
        return Collections.singletonList(SpawnsCommand.class);
    }

    /**
     * Adds a spawn point at (x;z) in the default world.
     *
     * @param vec The vector representing the X and Z coordinates.
     */
    public void addSpawnPoint(final Vector vec)
    {
        addSpawnPoint(QuartzSurvivalGames.get().getWorld(World.Environment.NORMAL), vec.getX(), vec.getZ());
    }

    /**
     * Adds a spawn point at (x;z) in the default world.
     *
     * @param x The X coordinate.
     * @param z The Z coordinate.
     */
    public void addSpawnPoint(final Double x, final Double z)
    {
        addSpawnPoint(QuartzSurvivalGames.get().getWorld(World.Environment.NORMAL), x, z);
    }

    /**
     * Adds a spawn point at (x;z) in the given world.
     *
     * @param world The world.
     * @param x     The X coordinate.
     * @param z     The Z coordinate.
     */
    public void addSpawnPoint(final World world, final Double x, final Double z)
    {
        addSpawnPoint(new Location(world, x, 0, z));
    }

    /**
     * Adds a spawn point from a location.
     *
     * @param location The location. Cloned, so you can use the same location object with
     *                 modifications between two calls.
     * @throws RuntimeException         If the spawn point is in the Nether and no safe spot was
     *                                  found.
     * @throws IllegalArgumentException If the spawn point is out of the current border.
     */
    public void addSpawnPoint(final Location location)
    {
        final Location spawnPoint = location.clone();

        // Initial fall, except in the nether.
        if (!(spawnPoint.getWorld().getEnvironment() == World.Environment.NETHER))
        {
            spawnPoint.setY(location.getWorld().getHighestBlockYAt(location.getBlockX(), location.getBlockZ()) + 120);
        }
        else
        {
            final Location safeSpot = QSGUtils.searchSafeSpot(location);
            if (safeSpot == null)
            {
                throw new RuntimeException("Unable to find a safe spot to set the spawn point " + location.toString());
            }

            spawnPoint.setY(safeSpot.getY());
        }

        if (!QSG.module(BorderModule.class).isInsideBorder(spawnPoint))
        {
            throw new IllegalArgumentException("The given spawn location is outside the current border");
        }

        spawnPoints.add(spawnPoint);
    }

    /**
     * Returns the registered spawn points.
     *
     * @return The spawn points.
     */
    public List<Location> getSpawnPoints()
    {
        return spawnPoints;
    }

    /**
     * Removes all spawn points with the same coordinates as the given location object (X, Z,
     * world).
     *
     * @param location The location to be removed.
     * @param precise  If true, only the spawn points at the exact same location will be removed.
     *                 Else, the points in the same block.
     * @return true if something were removed.
     */
    public boolean removeSpawnPoint(final Location location, final boolean precise)
    {
        final List<Location> toRemove = getSpawnPoints().stream()
                .filter(spawn -> location.getWorld().equals(spawn.getWorld()))
                .filter(spawn -> precise
                        && location.getX() == spawn.getX()
                        && location.getZ() == spawn.getZ() || !precise
                        && location.getBlockX() == spawn.getBlockX()
                        && location.getBlockZ() == spawn.getBlockZ())
                .collect(Collectors.toCollection(LinkedList::new));

        for (Location spawnToRemove : toRemove)
        {
            // Used to remove all occurrences of the spawn point
            while (spawnPoints.remove(spawnToRemove))
                ;
        }

        return toRemove.size() != 0;
    }

    /**
     * Removes all registered spawn points.
     * <p>
     * CANNOT BE CANCELLED.
     */
    public void reset()
    {
        spawnPoints.clear();
    }



    /**
     * Generates spawn points with the given generator.
     *
     * @param generatorName                   The generator to use.
     * @param world                           The world where the spawn points will be generated.
     * @param spawnCount                      The number of spawn points to generate.
     * @param regionDiameter                  The diameter of the region where the spawn points will
     *                                        be generated.<br> This is limited by the size of the
     *                                        map. This will be seen as the diameter of a circular
     *                                        or of a squared map, following the shape of the world
     *                                        set in the configuration.
     * @param minimalDistanceBetweenTwoPoints The minimal distance between two points.
     * @param xCenter                         The x coordinate of the point in the center of the
     *                                        region where the points will be generated.
     * @param zCenter                         The z coordinate of the point in the center of the
     *                                        region where the points will be generated.
     * @throws CannotGenerateSpawnPointsException In case of fail.
     * @throws UnknownGeneratorException          If no generator was found by the given name.
     */
    public void generateSpawnPoints(final String generatorName, final World world, final int spawnCount, final int regionDiameter, final int minimalDistanceBetweenTwoPoints, final double xCenter, final double zCenter) throws CannotGenerateSpawnPointsException, UnknownGeneratorException
    {
        Generator generator = Generator.fromString(generatorName);
        if (generator != null)
        {
            generateSpawnPoints(generator, world, spawnCount, regionDiameter, minimalDistanceBetweenTwoPoints, xCenter, zCenter);
        }
        else
        {
            throw new UnknownGeneratorException("The generator '" + generatorName + "' does not exists.");
        }
    }

    /**
     * Generates spawn points with the given generator.
     *
     * @param generator                       The generator to use.
     * @param world                           The world where the spawn points will be generated.
     * @param spawnCount                      The number of spawn points to generate.
     * @param regionDiameter                  The diameter of the region where the spawn points will
     *                                        be generated.<br> This is limited by the size of the
     *                                        map. This will be seen as the diameter of a circular
     *                                        or of a squared map, following the shape of the world
     *                                        set in the configuration.
     * @param minimalDistanceBetweenTwoPoints The minimal distance between two points.
     * @param xCenter                         The x coordinate of the point in the center of the
     *                                        region where the points will be generated.
     * @param zCenter                         The z coordinate of the point in the center of the
     *                                        region where the points will be generated.
     * @throws CannotGenerateSpawnPointsException In case of fail.
     */
    public void generateSpawnPoints(final Generator generator, final World world, final int spawnCount, final int regionDiameter, final int minimalDistanceBetweenTwoPoints, final double xCenter, final double zCenter) throws CannotGenerateSpawnPointsException
    {
        generateSpawnPoints(generator.getInstance(), world, spawnCount, regionDiameter, minimalDistanceBetweenTwoPoints, xCenter, zCenter);
    }

    /**
     * Generates spawn points with the given generator.
     *
     * @param generator                       The generator to use.
     * @param world                           The world where the spawn points will be generated.
     * @param spawnCount                      The number of spawn points to generate.
     * @param regionDiameter                  The diameter of the region where the spawn points will
     *                                        be generated.<br> This is limited by the size of the
     *                                        map. This will be seen as the diameter of a circular
     *                                        or of a squared map, following the shape of the world
     *                                        set in the configuration.
     * @param minimalDistanceBetweenTwoPoints The minimal distance between two points.
     * @param xCenter                         The x coordinate of the point in the center of the
     *                                        region where the points will be generated.
     * @param zCenter                         The z coordinate of the point in the center of the
     *                                        region where the points will be generated.
     * @throws CannotGenerateSpawnPointsException In case of fail.
     */
    public void generateSpawnPoints(final SpawnPointsGenerator generator, final World world, final int spawnCount, final int regionDiameter, final int minimalDistanceBetweenTwoPoints, final double xCenter, final double zCenter) throws CannotGenerateSpawnPointsException
    {
        generator.generate(
                world, spawnCount,
                regionDiameter, minimalDistanceBetweenTwoPoints,
                xCenter, zCenter, Config.AVOID_WATER.get()
        ).forEach(this::addSpawnPoint);
    }


    /**
     * Generates on the fly missing spawn points when the game starts.
     */
    @EventHandler
    public void beforeTeleportationPhase(final BeforeTeleportationPhaseEvent ev)
    {
        final GameModule game = QSG.module(GameModule.class);

        final World normalWorld = QSG.get().getWorld(World.Environment.NORMAL);

        final int regionDiameter = QSG.module(BorderModule.class).getCurrentBorderDiameter();
        final int playersWithoutTeam = (int) Bukkit.getOnlinePlayers().stream()
                .filter(player -> ZTeams.get().getTeamForPlayer(player) == null)
                .count();

        int spawnsNeeded = 0;

        switch (game.getTeleportationMode())
        {
            case NORMAL:
                spawnsNeeded = ZTeams.get().countTeams() + playersWithoutTeam;
                break;

            case IGNORE_TEAMS:
                spawnsNeeded = ZTeams.get().getTeams().stream().mapToInt(team -> team.getPlayers().size()).sum()
                        + playersWithoutTeam;
                break;
        }

        spawnsNeeded -= spawnPoints.size(); // We don't need what we already have.

        if (spawnsNeeded <= 0) return;

        Exception error = null;

        for (int i = 0; i < 6; i++)
        {
            try
            {
                generateSpawnPoints(
                        Generator.RANDOM,
                        normalWorld,
                        spawnsNeeded,
                        regionDiameter - 25,
                        (int) Math.floor(regionDiameter / Math.max(spawnsNeeded * 1.4, 10)),
                        normalWorld.getSpawnLocation().getX(),
                        normalWorld.getSpawnLocation().getZ()
                );

                error = null;
            }
            catch (final CannotGenerateSpawnPointsException e)
            {
                error = e;
                continue;
            }

            break;
        }

        if (error == null)
        {
            log().info("Randomly generated {0} missing spawn points on the fly. See /uh spawn for details.", spawnsNeeded);
        }
        else
        {
            log().error("There where {0} missing spawn points but we weren''t able to generate them automatically, even after 6 tries. Try to generate them yourself using /uh spawns generate, or to add them manually with /uh spawns add.", error, spawnsNeeded);
        }
    }
}
