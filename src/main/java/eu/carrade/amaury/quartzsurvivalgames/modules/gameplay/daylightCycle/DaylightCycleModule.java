/*
 * Plugin UHCReloaded : Alliances
 *
 * Copyright ou © ou Copr. Amaury Carrade (2016)
 * Idées et réflexions : Alexandre Prokopowicz, Amaury Carrade, "Vayan".
 *
 * Ce logiciel est régi par la licence CeCILL soumise au droit français et
 * respectant les principes de diffusion des logiciels libres. Vous pouvez
 * utiliser, modifier et/ou redistribuer ce programme sous les conditions
 * de la licence CeCILL telle que diffusée par le CEA, le CNRS et l'INRIA
 * sur le site "http://www.cecill.info".
 *
 * En contrepartie de l'accessibilité au code source et des droits de copie,
 * de modification et de redistribution accordés par cette licence, il n'est
 * offert aux utilisateurs qu'une garantie limitée.  Pour les mêmes raisons,
 * seule une responsabilité restreinte pèse sur l'auteur du programme,  le
 * titulaire des droits patrimoniaux et les concédants successifs.
 *
 * A cet égard  l'attention de l'utilisateur est attirée sur les risques
 * associés au chargement,  à l'utilisation,  à la modification et/ou au
 * développement et à la reproduction du logiciel par l'utilisateur étant
 * donné sa spécificité de logiciel libre, qui peut le rendre complexe à
 * manipuler et qui le réserve donc à des développeurs et des professionnels
 * avertis possédant  des  connaissances  informatiques approfondies.  Les
 * utilisateurs sont donc invités à charger  et  tester  l'adéquation  du
 * logiciel à leurs besoins dans des conditions permettant d'assurer la
 * sécurité de leurs systèmes et ou de leurs données et, plus généralement,
 * à l'utiliser et l'exploiter dans les mêmes conditions de sécurité.
 *
 * Le fait que vous puissiez accéder à cet en-tête signifie que vous avez
 * pris connaissance de la licence CeCILL, et que vous en avez accepté les
 * termes.
 */
package eu.carrade.amaury.quartzsurvivalgames.modules.gameplay.daylightCycle;

import eu.carrade.amaury.quartzsurvivalgames.core.ModuleCategory;
import eu.carrade.amaury.quartzsurvivalgames.core.ModuleInfo;
import eu.carrade.amaury.quartzsurvivalgames.core.QSGModule;
import eu.carrade.amaury.quartzsurvivalgames.modules.core.game.GameModule;
import eu.carrade.amaury.quartzsurvivalgames.modules.core.game.GamePhase;
import eu.carrade.amaury.quartzsurvivalgames.modules.core.game.events.game.GamePhaseChangedEvent;
import eu.carrade.amaury.quartzsurvivalgames.modules.core.timers.TimeDelta;
import eu.carrade.amaury.quartzsurvivalgames.shortcuts.QSG;
import fr.zcraft.quartzlib.tools.runners.RunTask;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.scheduler.BukkitTask;

import java.util.concurrent.atomic.AtomicLong;


@ModuleInfo (
        name = "Daylight Cycle",
        description = "Configures the daylight cycle (disabled, slowed down, normal, accelerated) and the initial time.",
        category = ModuleCategory.GAMEPLAY,
        icon = Material.CLOCK,
        settings = Config.class
)
public class DaylightCycleModule extends QSGModule
{
    // Here are the Magic Values™.
    private final static long TICKS_IN_ONE_DAYLIGHT_CYCLE = 24000L;
    private final static TimeDelta NORMAL_DAYLIGHT_CYCLE_DURATION = new TimeDelta(0, 20, 0);

    private BukkitTask daylightCycleTask = null;


    @Override
    protected void onEnable()
    {
        QSG.get().getWorlds().forEach(world -> {
            world.setFullTime(Config.WAITING_PHASE_HOUR.get());
            world.setGameRuleValue("doDaylightCycle", Boolean.FALSE.toString());
        });
    }

    @Override
    public void onLateEnable()
    {
        if (QSG.module(GameModule.class).currentPhaseAfter(GamePhase.STARTING))
        {
            initDayLightCycle(QSG.get().getWorld(World.Environment.NORMAL).getFullTime());
        }
    }

    @Override
    protected void onDisable()
    {
        if (daylightCycleTask != null)
        {
            daylightCycleTask.cancel();
            daylightCycleTask = null;
        }
    }

    @EventHandler
    public void onGameStart(final GamePhaseChangedEvent ev)
    {
        if (ev.getNewPhase() == GamePhase.IN_GAME && ev.isRunningForward())
        {
            initDayLightCycle(Config.INITIAL_GAME_HOUR.get());
        }
    }

    private void initDayLightCycle(final long initialTime)
    {
        final World world = QSG.get().getWorld(World.Environment.NORMAL);

        world.setFullTime(Config.INITIAL_GAME_HOUR.get());
        world.setGameRuleValue("doDaylightCycle", Config.ENABLE_DAYLIGHT_CYCLE.get().toString());

        // If the day cycle duration needs to be altered
        if (!Config.DAYLIGHT_CYCLE_DURATION.get().equals(NORMAL_DAYLIGHT_CYCLE_DURATION))
        {
            // We disable the automatic cycle to avoid the sun and the moon to boggle on the clients
            world.setGameRuleValue("doDaylightCycle", Boolean.FALSE.toString());

            final long ticksPerDay = Config.DAYLIGHT_CYCLE_DURATION.get().getSeconds() * 20L;

            // For days slower than Minecraft days, it is not required to update every tick, as the daytime
            // will be the same for multiple updates.
            final long updateInterval = (long) Math.max(1L, (ticksPerDay * 1.0f) / TICKS_IN_ONE_DAYLIGHT_CYCLE);

            final AtomicLong tick = new AtomicLong(initialTime % TICKS_IN_ONE_DAYLIGHT_CYCLE);
            if (tick.get() < 0L) tick.addAndGet(TICKS_IN_ONE_DAYLIGHT_CYCLE);

            daylightCycleTask = RunTask.timer(() -> {
                // We keep the current tick in one daylight cycle.
                if (tick.addAndGet(updateInterval) >= ticksPerDay) tick.set(0L);

                // On each tick, we calculate the time of day we should be at this point.
                final long convertedTick = (long) Math.floor((tick.floatValue() / ticksPerDay) * TICKS_IN_ONE_DAYLIGHT_CYCLE);

                // We update the main world.
                world.setTime(convertedTick);
            }, updateInterval, updateInterval);
        }
    }
}
