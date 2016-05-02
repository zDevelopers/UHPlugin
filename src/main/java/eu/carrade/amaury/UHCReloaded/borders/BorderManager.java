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

package eu.carrade.amaury.UHCReloaded.borders;

import eu.carrade.amaury.UHCReloaded.UHCReloaded;
import eu.carrade.amaury.UHCReloaded.UHConfig;
import eu.carrade.amaury.UHCReloaded.borders.exceptions.CannotGenerateWallsException;
import eu.carrade.amaury.UHCReloaded.borders.generators.WallGenerator;
import eu.carrade.amaury.UHCReloaded.borders.worldborders.WorldBorder;
import eu.carrade.amaury.UHCReloaded.task.BorderWarningTask;
import eu.carrade.amaury.UHCReloaded.timers.UHTimer;
import eu.carrade.amaury.UHCReloaded.utils.UHUtils;
import fr.zcraft.zlib.components.i18n.I;
import fr.zcraft.zlib.tools.PluginLogger;
import fr.zcraft.zlib.tools.runners.RunTask;
import fr.zcraft.zlib.tools.text.Titles;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;
import java.util.Set;


public class BorderManager
{
    private final boolean BORDER_SHRINKING;
    private final long BORDER_SHRINKING_STARTS_AFTER;
    private final long BORDER_SHRINKING_DURATION;
    private final double BORDER_SHRINKING_FINAL_SIZE;

    private UHCReloaded p = null;

    private WorldBorder border = null;

    private Integer warningSize = 0;
    private BukkitRunnable warningTask = null;

    private Boolean warningFinalTimeEnabled = false;
    private String warningTimerName = null;
    private CommandSender warningSender = null;

    private MapShape mapShape = null;


    public BorderManager(UHCReloaded plugin)
    {
        p = plugin;

        /// The name of the warning timer displaying the time left before the next border
        warningTimerName = I.t("Border shrinking");

        mapShape = MapShape.fromString(UHConfig.MAP.SHAPE.get());
        if (mapShape == null)
        {
            PluginLogger.warning("Invalid shape '" + UHConfig.MAP.SHAPE.get() + "'; using 'squared' instead.");
            mapShape = MapShape.SQUARED;
        }


        World world = UHUtils.getOverworld();

        if (world == null)
        {
            world = Bukkit.getWorlds().get(0);
            PluginLogger.warning("Cannot find overworld! Using the world '{0}' instead (environment: {1}).", world.getName(), world.getEnvironment());
        }

        border = WorldBorder.getInstance(world, UHConfig.MAP.BORDER.MOTOR.get(), mapShape);

        border.setShape(mapShape);
        border.setCenter(world.getSpawnLocation());
        border.setDiameter(UHConfig.MAP.SIZE.get());

        border.init();

        PluginLogger.info("Using {0} to set the world border.", border.getClass().getSimpleName());


        BORDER_SHRINKING = UHConfig.MAP.BORDER.SHRINKING.ENABLED.get();
        BORDER_SHRINKING_STARTS_AFTER = UHUtils.string2Time(UHConfig.MAP.BORDER.SHRINKING.STARTS_AFTER.get(), 30*60);  // Seconds
        BORDER_SHRINKING_DURATION = UHUtils.string2Time(UHConfig.MAP.BORDER.SHRINKING.SHRINKS_DURING.get(), 60*60*2);  // Same
        BORDER_SHRINKING_FINAL_SIZE = UHConfig.MAP.BORDER.SHRINKING.DIAMETER_AFTER_SHRINK.get();
    }

    /**
     * Sets the shape of the map. Updates the WorldBorder too.
     *
     * @param shape The shape.
     */
    public void setMapShape(MapShape shape)
    {
        this.mapShape = shape;
        border.setShape(shape);
    }

    /**
     * Returns the current shape of the map.
     *
     * @return The shape.
     */
    public MapShape getMapShape()
    {
        return mapShape;
    }

    /**
     * @return The WorldBorder proxy to set the border in-game.
     */
    public WorldBorder getBorderProxy()
    {
        return border;
    }

    /**
     * Checks if a given location is inside the border with the given diameter.
     * The check is performed for a circular or squared border, following the configuration.
     *
     * @param location The location to check.
     * @param diameter The diameter of the checked border.
     *
     * @return {@code true} if inside.
     */
    public boolean isInsideBorder(Location location, double diameter)
    {
        // The nether/end are not limited.
        return !location.getWorld().getEnvironment().equals(Environment.NORMAL) || mapShape.getShape().isInsideBorder(location, diameter, location.getWorld().getSpawnLocation());
    }

    /**
     * Checks if a given location is inside the border with the current diameter.
     * The check is performed for a circular or squared border, following the configuration.
     *
     * @param location The location to check.
     * @return {@code true} if inside.
     */
    public boolean isInsideBorder(Location location)
    {
        return this.isInsideBorder(location, getCurrentBorderDiameter());
    }

    /**
     * Returns the distance from the location to the border, if the location is outside this border.
     * If it is inside, or in another world, returns 0.
     *
     * @param location The location to check.
     * @param diameter The diameter of the checked border.
     *
     * @return The distance, or 0 if the player is either inside the border or not in the world.
     */
    public double getDistanceToBorder(Location location, double diameter)
    {
        return mapShape.getShape().getDistanceToBorder(location, diameter, location.getWorld().getSpawnLocation());
    }


    /**
     * Returns a list of the players outside a border with the given diameter.
     * The check is performed for a circular or squared border, following the configuration.
     *
     * @param diameter The diameter of the checked border.
     * @return A list of players out of the given diameter.
     */
    public Set<Player> getPlayersOutside(int diameter)
    {
        HashSet<Player> playersOutside = new HashSet<Player>();

        for (final Player player : p.getGameManager().getOnlineAlivePlayers())
        {
            if (!isInsideBorder(player.getLocation(), diameter))
            {
                playersOutside.add(player);
            }
        }

        return playersOutside;
    }

    /**
     * Returns the size of the future border, used in the warning messages sent to the
     * players out of this future border.
     *
     * @return the future border diameter.
     */
    public int getWarningSize()
    {
        return this.warningSize;
    }

    /**
     * @return true if there is currently a warning with a time left displayed.
     */
    public boolean getWarningFinalTimeEnabled()
    {
        return this.warningFinalTimeEnabled;
    }

    /**
     * @return the sender of the last warning configured.
     */
    public CommandSender getWarningSender()
    {
        return this.warningSender;
    }

    /**
     * Sets the size of the future border, used in the warning messages sent to the
     * players out of this future border.
     *
     * This also starts the display of the warning messages, every 90 seconds by default
     * (configurable, see config.yml, map.border.warningInterval).
     *
     * If timeLeft is not null, the time available for the players to go inside the future
     * border is displayed in the warning message.
     *
     * @param diameter The future diameter.
     * @param timeLeft The time available for the players to go inside the future border (minutes).
     * @param sender The user who requested this change.
     */
    public void setWarningSize(int diameter, int timeLeft, CommandSender sender)
    {
        cancelWarning();

        this.warningSize = diameter;

        if (timeLeft != 0)
        {
            UHTimer timer = new UHTimer(this.warningTimerName);
            timer.setDuration(timeLeft * 60);

            p.getTimerManager().registerTimer(timer);

            timer.start();
        }

        if (sender != null)
        {
            this.warningSender = sender;
        }

        warningTask = new BorderWarningTask(p);
        warningTask.runTaskTimer(p, 20L, 20L * p.getConfig().getInt("map.border.warningInterval", 90));
    }

    /**
     * Sets the size of the future border, used in the warning messages sent to the
     * players out of this future border.
     *
     * This also starts the display of the warning messages, every 90 seconds by default
     * (configurable, see config.yml, map.border.warningInterval).
     *
     * @param diameter The diameter of the future border.
     */
    public void setWarningSize(int diameter)
    {
        setWarningSize(diameter, 0, null);
    }

    /**
     * Returns the UHTimer object representing the countdown before the next border reduction.
     *
     * <p>Returns {@code null} if there isn't any countdown running currently.</p>
     *
     * @return The timer.
     */
    public UHTimer getWarningTimer()
    {
        return p.getTimerManager().getTimer(this.warningTimerName);
    }

    /**
     * Stops the display of the warning messages.
     */
    public void cancelWarning()
    {
        if (warningTask != null)
        {
            try
            {
                warningTask.cancel();
            }
            catch (IllegalStateException ignored) {}
        }

        UHTimer timer = getWarningTimer();
        if (timer != null)
        {
            timer.stop();
            p.getTimerManager().unregisterTimer(timer);
        }
    }

    /**
     * @return the current border diameter.
     */
    public int getCurrentBorderDiameter()
    {
        return (int) border.getDiameter();
    }

    /**
     * Changes the current border diameter.
     * This also reconfigures the used world border.
     *
     * If WorldBorder is installed, all players out of this new border will be teleported inside the new one.
     * Else, nothing will happens.
     *
     * @param diameter the new diameter.
     */
    public void setCurrentBorderDiameter(int diameter)
    {
        cancelWarning();

        border.setDiameter(diameter);
    }


    /**
     * Sends a list of the players outside the given border to the specified sender.
     *
     * @param to The player/console to send the check.
     * @param diameter The diameter of the border to be checked.
     */
    public void sendCheckMessage(CommandSender to, int diameter)
    {
        Set<Player> playersOutside = getPlayersOutside(diameter);

        if (playersOutside.size() == 0)
        {
            to.sendMessage(I.t("{cs}All players are inside the given border."));
        }
        else
        {
            to.sendMessage(I.t("{ci}There are {0} players outside the given border.", String.valueOf(playersOutside.size())));
            for (Player player : getPlayersOutside(diameter))
            {
                double distance = getDistanceToBorder(player.getLocation(), diameter);
                if (distance > 150)
                {
                    to.sendMessage(I.t("{lightpurple} - {red}{0}{ci} (far away from the border)", player.getName()));
                }
                else if (distance > 25)
                {
                    to.sendMessage(I.t("{lightpurple} - {yellow}{0}{ci} (close to the border)", player.getName()));
                }
                else
                {
                    to.sendMessage(I.t("{lightpurple} - {green}{0}{ci} (very close to the border)", player.getName()));
                }
            }
        }
    }

    /**
     * Generates the walls in the given world, following the current border configuration.
     *
     * @param world The world were the walls will be built in.
     * @throws CannotGenerateWallsException
     */
    public void generateWalls(World world) throws CannotGenerateWallsException
    {
        Integer wallHeight = UHConfig.MAP.WALL.HEIGHT.get();

        Material wallBlockAir = Material.matchMaterial(UHConfig.MAP.WALL.BLOCK.REPLACE_AIR.get());
        Material wallBlockSolid = Material.matchMaterial(UHConfig.MAP.WALL.BLOCK.REPLACE_SOLID.get());

        if (wallBlockAir == null || !wallBlockAir.isSolid() || wallBlockSolid == null || !wallBlockSolid.isSolid())
        {
            throw new CannotGenerateWallsException("Cannot generate the walls: invalid blocks set in the config");
        }

        WallGenerator generator = mapShape.getWallGeneratorInstance(wallBlockAir, wallBlockSolid);
        if (generator != null)
            generator.build(world, getCurrentBorderDiameter(), wallHeight);
        else
            throw new CannotGenerateWallsException("Unable to load walls generator.");
    }

    /**
     * Schedules the automatic border reduction, if enabled in the configuration.
     */
    public void scheduleBorderReduction()
    {
        if (BORDER_SHRINKING)
        {
            RunTask.later(new Runnable() {
                @Override
                public void run()
                {
                    Integer secondsPerBlock = (int) Math.rint(BORDER_SHRINKING_DURATION / (border.getDiameter() - BORDER_SHRINKING_FINAL_SIZE)) * 2;

                    border.setDiameter(BORDER_SHRINKING_FINAL_SIZE, BORDER_SHRINKING_DURATION);

                    Titles.broadcastTitle(5, 30, 8, I.t("{red}Warning!"), I.t("{white}The border begins to shrink..."));

                    Bukkit.broadcastMessage(I.t("{red}{bold}The border begins to shrink..."));
                    Bukkit.broadcastMessage(I.t("{gray}It will shrink by one block every {0} second(s) until {1} blocks in diameter.", secondsPerBlock, BORDER_SHRINKING_FINAL_SIZE));
                }
            }, BORDER_SHRINKING_STARTS_AFTER * 20l);
        }
    }
}
