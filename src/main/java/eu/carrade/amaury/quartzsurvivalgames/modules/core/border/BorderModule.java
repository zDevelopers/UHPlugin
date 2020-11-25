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
package eu.carrade.amaury.quartzsurvivalgames.modules.core.border;

import eu.carrade.amaury.quartzsurvivalgames.QuartzSurvivalGames;
import eu.carrade.amaury.quartzsurvivalgames.core.ModuleCategory;
import eu.carrade.amaury.quartzsurvivalgames.core.ModuleInfo;
import eu.carrade.amaury.quartzsurvivalgames.core.QSGModule;
import eu.carrade.amaury.quartzsurvivalgames.modules.core.border.commands.BorderCommand;
import eu.carrade.amaury.quartzsurvivalgames.modules.core.border.events.BorderChangedEvent;
import eu.carrade.amaury.quartzsurvivalgames.modules.core.border.worldborders.WorldBorder;
import eu.carrade.amaury.quartzsurvivalgames.modules.core.game.GameModule;
import eu.carrade.amaury.quartzsurvivalgames.modules.core.game.GamePhase;
import eu.carrade.amaury.quartzsurvivalgames.modules.core.game.events.game.GamePhaseChangedEvent;
import eu.carrade.amaury.quartzsurvivalgames.modules.core.sidebar.SidebarInjector;
import eu.carrade.amaury.quartzsurvivalgames.modules.core.timers.TimeDelta;
import eu.carrade.amaury.quartzsurvivalgames.shortcuts.QSG;
import eu.carrade.amaury.quartzsurvivalgames.utils.QSGUtils;
import fr.zcraft.quartzlib.components.commands.Command;
import fr.zcraft.quartzlib.components.i18n.I;
import fr.zcraft.quartzlib.core.ZLib;
import fr.zcraft.quartzlib.tools.runners.RunTask;
import fr.zcraft.quartzlib.tools.text.Titles;
import org.bukkit.*;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


@ModuleInfo (
        name = "Border",
        description = "Manages the border size and reduction.",
        category = ModuleCategory.CORE,
        icon = Material.FENCE,
        settings = Config.class,
        can_be_unloaded = false,
        internal = true
)
public class BorderModule extends QSGModule
{
    private MapShape mapShape = null;
    private WorldBorder border = null;

    private WorldBorderDependency worldBorderDependency = null;

    /**
     * The lines in the sidebar, calculated once for every player. Caching purposes.
     */
    private final List<String> sidebar = new ArrayList<>();

    @Override
    public void onEnable()
    {
        worldBorderDependency = ZLib.loadComponent(WorldBorderDependency.class);

        mapShape = Config.SHAPE.get();

        final World world = QuartzSurvivalGames.get().getWorld(World.Environment.NORMAL);

        border = WorldBorder.getInstance(world, Config.MOTOR.get(), mapShape);

        border.setShape(mapShape);
        border.setCenter(world.getSpawnLocation());
        border.setDiameter(Config.SIZE.get());

        border.init();

        log().info("Using {0} to set the world border.", border.getClass().getSimpleName());
    }

    @Override
    public List<Class<? extends Command>> getCommands()
    {
        return Collections.singletonList(BorderCommand.class);
    }

    @Override
    public void prepareInjectionIntoSidebar()
    {
        if (QSG.module(GameModule.class).currentPhaseBefore(GamePhase.IN_GAME)) return;

        sidebar.clear();

        if (Config.SIDEBAR.DISPLAYED.get())
        {
            /// Title of the border section in the sidebar
            sidebar.add(I.t("{blue}{bold}Border"));

            int diameter = (int) Math.ceil(border.getDiameter());

            if (Config.SIDEBAR.DISPLAY_DIAMETER.get() || border.getShape() == MapShape.CIRCULAR)
            {
                if (border.getShape() == MapShape.SQUARED)
                    /// Border diameter for a squared map in the sidebar
                    sidebar.add(I.tn("{white}{0} block wide", "{white}{0} blocks wide", diameter, diameter));
                else
                    /// Border diameter for a circular map in the sidebar
                    sidebar.add(I.tn("{gray}Diameter: {white}{0} block", "{gray}Diameter: {white}{0} blocks", diameter, diameter));
            }
            else
            {
                Location center = border.getCenter();
                int radius = (int) Math.ceil(diameter / 2d);

                int minX = center.getBlockX() - radius;
                int maxX = center.getBlockX() + radius;
                int minZ = center.getBlockZ() - radius;
                int maxZ = center.getBlockZ() + radius;

                // Same min & max, we can display both at once
                if (minX == minZ && maxX == maxZ)
                {
                    /// Min & max coordinates in the sidebar, to locate the border. Ex: "-500 +500". {0} = minimal coord, {1} = maximal coord.
                    sidebar.add(I.t("{white}{0} {1}", QSGUtils.integerToStringWithSign(minX), QSGUtils.integerToStringWithSign(maxZ)));
                }
                else
                {
                    /// Min & max X coordinates in the sidebar, to locate the border. Ex: "X: -500 +500". {0} = minimal coord, {1} = maximal coord.
                    sidebar.add(I.t("{gray}X: {white}{0} {1}", QSGUtils.integerToStringWithSign(minX), QSGUtils.integerToStringWithSign(maxX)));
                    /// Min & max Z coordinates in the sidebar, to locate the border. Ex: "Z: -500 +500". {0} = minimal coord, {1} = maximal coord.
                    sidebar.add(I.t("{gray}Z: {white}{0} {1}", QSGUtils.integerToStringWithSign(minZ), QSGUtils.integerToStringWithSign(maxZ)));
                }
            }
        }
    }

    @Override
    public void injectIntoSidebar(Player player, SidebarInjector injector)
    {
        if (QSG.module(GameModule.class).currentPhaseBefore(GamePhase.IN_GAME) || sidebar.isEmpty()) return;
        injector.injectLines(SidebarInjector.SidebarPriority.MIDDLE_BOTTOM, true, sidebar);
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

    public WorldBorderDependency getWorldBorderDependency()
    {
        return worldBorderDependency;
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
        return QSG.module(GameModule.class)
                .getAliveConnectedPlayers().stream()
                .filter(player -> !isInsideBorder(player.getLocation(), diameter))
                .collect(Collectors.toSet());
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
        final BorderModule borderModule = QSG.module(BorderModule.class);
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
    private void scheduleBorderReduction()
    {
        if (Config.SHRINKING.ENABLED.get())
        {
            RunTask.later(() -> {
                if (QSG.module(GameModule.class).getPhase() != GamePhase.IN_GAME) return;

                final int secondsPerBlock = (int) Math.rint(Config.SHRINKING.SHRINKS_DURING.get().getSeconds() / (border.getDiameter() - Config.SHRINKING.DIAMETER_AFTER_SHRINK.get())) * 2;

                border.setDiameter(Config.SHRINKING.DIAMETER_AFTER_SHRINK.get(), Config.SHRINKING.SHRINKS_DURING.get());

                Titles.broadcastTitle(5, 30, 8, I.t("{red}Warning!"), I.t("{white}The border begins to shrink..."));

                Bukkit.broadcastMessage(QSGUtils.prefixedMessage(I.t("Border"), I.t("{red}{bold}The border begins to shrink...")));
                Bukkit.broadcastMessage(QSGUtils.prefixedMessage(I.t("Border"), I.t("{gray}It will shrink by one block every {0} second(s) until {1} blocks in diameter.", secondsPerBlock, Config.SHRINKING.DIAMETER_AFTER_SHRINK.get())));
            }, Config.SHRINKING.STARTS_AFTER.get().getSeconds() * 20L);

            scheduleBorderReductionWarning(new TimeDelta(1, 0, 0));
            scheduleBorderReductionWarning(new TimeDelta(0, 30, 0));
            scheduleBorderReductionWarning(new TimeDelta(0, 10, 0));
        }
    }

    private void scheduleBorderReductionWarning(final TimeDelta warnBefore)
    {
        if (Config.SHRINKING.STARTS_AFTER.get().greaterThan(warnBefore.add(new TimeDelta(0, 5, 0))))
        {
            RunTask.later(() -> {
                if (QSG.module(GameModule.class).getPhase() != GamePhase.IN_GAME) return;

                Bukkit.broadcastMessage("");
                Bukkit.broadcastMessage(QSGUtils.prefixedMessage(I.t("Border"), I.tn("{red}The border will start to shrink in {0} minute...", "{red}The border will start to shrink in {0} minutes...", (int) (warnBefore.getSeconds() / 60))));
                Bukkit.broadcastMessage("");
            }, Config.SHRINKING.STARTS_AFTER.get().subtract(warnBefore).getSeconds() * 20L);
        }
    }

    @EventHandler
    public void onGameStarts(final GamePhaseChangedEvent ev)
    {
        if (ev.getNewPhase() != GamePhase.IN_GAME) return;
        scheduleBorderReduction();
    }
}
