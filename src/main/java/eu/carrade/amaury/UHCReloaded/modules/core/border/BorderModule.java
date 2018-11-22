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
package eu.carrade.amaury.UHCReloaded.modules.core.border;

import eu.carrade.amaury.UHCReloaded.UHCReloaded;
import eu.carrade.amaury.UHCReloaded.UHConfig;
import eu.carrade.amaury.UHCReloaded.core.ModuleInfo;
import eu.carrade.amaury.UHCReloaded.core.UHModule;
import eu.carrade.amaury.UHCReloaded.modules.core.border.commands.BorderCommand;
import eu.carrade.amaury.UHCReloaded.modules.core.border.events.BorderChangedEvent;
import eu.carrade.amaury.UHCReloaded.modules.core.border.worldborders.WorldBorder;
import eu.carrade.amaury.UHCReloaded.shortcuts.UR;
import fr.zcraft.zlib.components.commands.Command;
import fr.zcraft.zlib.components.i18n.I;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


@ModuleInfo (
        name = "Border",
        description = "Manages the border size and reduction",
        settings = Config.class,
        can_be_disabled = false,
        internal = true
)
public class BorderModule extends UHModule
{
    private MapShape mapShape = null;
    private WorldBorder border = null;

    @Override
    public void onEnable()
    {
        mapShape = Config.SHAPE.get();

        final World world = UHCReloaded.get().getWorld(World.Environment.NORMAL);

        border = WorldBorder.getInstance(world, Config.MOTOR.get(), mapShape);

        border.setShape(mapShape);
        border.setCenter(world.getSpawnLocation());
        border.setDiameter(UHConfig.MAP.SIZE.get());

        border.init();

        log().info("Using {0} to set the world border.", border.getClass().getSimpleName());
    }

    @Override
    public List<Class<? extends Command>> getCommands()
    {
        return Collections.singletonList(BorderCommand.class);
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
        return !location.getWorld().getEnvironment().equals(World.Environment.NORMAL) || mapShape.getShape().isInsideBorder(location, diameter, location.getWorld().getSpawnLocation());
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
        final Set<Player> playersOutside = new HashSet<>();

        // TODO
//        for (final Player player : UR.module(GameModule.class).getOnlineAlivePlayers())
//        {
//            if (!isInsideBorder(player.getLocation(), diameter))
//            {
//                playersOutside.add(player);
//            }
//        }

        return playersOutside;
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
        border.setDiameter(diameter);
        Bukkit.getPluginManager().callEvent(new BorderChangedEvent(diameter));
    }

    /**
     * Sends a list of the players outside the given border to the specified sender.
     *
     * @param to The player/console to send the check.
     * @param diameter The diameter of the border to be checked.
     */
    public void sendCheckMessage(final CommandSender to, final int diameter)
    {
        final BorderModule borderModule = UR.module(BorderModule.class);
        final Set<Player> playersOutside = borderModule.getPlayersOutside(diameter);

        if (playersOutside.size() == 0)
        {
            to.sendMessage(I.t("{cs}All players are inside the given border."));
        }
        else
        {
            to.sendMessage(I.t("{ci}There are {0} players outside the given border.", String.valueOf(playersOutside.size())));
            for (Player player : borderModule.getPlayersOutside(diameter))
            {
                double distance = borderModule.getDistanceToBorder(player.getLocation(), diameter);
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
     * Schedules the automatic border reduction, if enabled in the configuration.
     */
    public void scheduleBorderReduction()
    {
//        if (BORDER_SHRINKING)
//        {
//            RunTask.later(() -> {
//                Integer secondsPerBlock = (int) Math.rint(BORDER_SHRINKING_DURATION / (border.getDiameter() - BORDER_SHRINKING_FINAL_SIZE)) * 2;
//
//                border.setDiameter(BORDER_SHRINKING_FINAL_SIZE, BORDER_SHRINKING_DURATION);
//
//                Titles.broadcastTitle(5, 30, 8, I.t("{red}Warning!"), I.t("{white}The border begins to shrink..."));
//
//                Bukkit.broadcastMessage(I.t("{red}{bold}The border begins to shrink..."));
//                Bukkit.broadcastMessage(I.t("{gray}It will shrink by one block every {0} second(s) until {1} blocks in diameter.", secondsPerBlock, BORDER_SHRINKING_FINAL_SIZE));
//            }, BORDER_SHRINKING_STARTS_AFTER * 20l);
//        }
    }
}
