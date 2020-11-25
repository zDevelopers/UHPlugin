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

package eu.carrade.amaury.quartzsurvivalgames.modules.core.game.submanagers;

import com.google.common.collect.ImmutableSet;
import eu.carrade.amaury.quartzsurvivalgames.modules.core.game.Config;
import eu.carrade.amaury.quartzsurvivalgames.modules.core.game.GamePhase;
import eu.carrade.amaury.quartzsurvivalgames.modules.core.game.events.game.GamePhaseChangedEvent;
import eu.carrade.amaury.quartzsurvivalgames.modules.core.timers.TimeDelta;
import eu.carrade.amaury.quartzsurvivalgames.shortcuts.QSG;
import eu.carrade.amaury.quartzsurvivalgames.utils.EntitiesUtils;
import eu.carrade.amaury.quartzsurvivalgames.utils.Run;
import fr.zcraft.quartzlib.components.i18n.I;
import fr.zcraft.quartzlib.core.QuartzComponent;
import fr.zcraft.quartzlib.tools.runners.RunTask;
import java.util.Set;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageEvent;

public class GameBeginning extends QuartzComponent implements Listener {
    /**
     * A flag used to disable damages at the beginning of the game to avoid fall damages and early ones.
     */
    private boolean inGracePeriod = false;

    /**
     * A flag used to disable hostile mobs spawn on the surface a few minutes after the beginning of the game.
     */
    private boolean inMobsFreePeriod = false;
    
    private static final Set<String> SURFACE_MATERIAL_ENDS = ImmutableSet.of(
            "LOG",
            "LEAVES",
            "PLANKS",
            "STAIRS",
            "BANNER",
            "WALL",
            "FENCE"
    );


    @EventHandler(priority = EventPriority.LOWEST)
    private void onGameStarts(GamePhaseChangedEvent ev) {
        if (ev.getNewPhase() != GamePhase.IN_GAME || !ev.isRunningForward()) {
            return;
        }


        /* *** Grace period (damages disabled) *** */

        TimeDelta grace = Config.BEGINNING.GRACE_PERIOD.get();

        if (grace.getSeconds() < 15) {
            grace = new TimeDelta(15);
        }

        inGracePeriod = true;

        Run.withCountdown(
                I.t("{green}{bold}Grace period"),
                I.t("{green}All damages are disabled"),
                second -> I.tn("{yellow}{bold}Over in {gold}{bold}{0}{yellow}{bold} second",
                        "{yellow}{bold}Over in {gold}{bold}{0}{yellow}{bold} second", (int) second),
                I.t("{yellow}{bold}Over! {yellow}You are now vulnerable..."),
                () -> QSG.game().getAliveConnectedPlayers(),
                () -> {
                    inGracePeriod = false;

                    if (Config.BEGINNING.BROADCAST_GRACE_END.get()) {
                        Bukkit.broadcastMessage(
                                I.t("{red}{bold}Warning!{white} The grace period is over, you are now vulnerable."));
                    }
                },
                grace
        );


        /* *** Peace period (PVP disabled) *** */

        if (Config.BEGINNING.PEACE_PERIOD.get().getSeconds() > 0) {
            setPVP(false);

            Run.withCountdown(
                    I.t("{red}{bold}Combats between players"),
                    null,
                    second -> I.tn("{yellow}{bold}Allowed in {gold}{bold}{0}{yellow}{bold} second",
                            "{yellow}{bold}Allowed in {gold}{bold}{0}{yellow}{bold} second", (int) second),
                    I.t("{yellow}{bold}Now allowed! {yellow}Beware..."),
                    (short) 10,
                    () -> QSG.game().getAliveConnectedPlayers(),
                    () -> {
                        setPVP(true);
                        Bukkit.broadcastMessage(I.t("{red}{bold}Warning!{white} PvP is now enabled."));
                    },
                    Config.BEGINNING.PEACE_PERIOD.get()
            );
        } else {
            setPVP(true);
        }


        /* *** Mobs-free period (mobs disabled on surface) *** */

        if (Config.BEGINNING.SURFACE_MOBS_FREE_PERIOD.get().getSeconds() > 0) {
            inMobsFreePeriod = true;
            RunTask.later(() -> inMobsFreePeriod = false,
                    Config.BEGINNING.SURFACE_MOBS_FREE_PERIOD.get().getSeconds() * 20L);
        }
    }


    /**
     * Used to disable all damages if the game is not started.
     */
    @EventHandler
    private void onEntityDamage(final EntityDamageEvent ev) {
        if (ev.getEntity() instanceof Player && inGracePeriod) {
            ev.setCancelled(true);
        }
    }

    /**
     * Used to cancel the spawn of hostile entities on the surface only, at the beginning of the game.
     */
    @EventHandler
    private void onSurfaceCreatureSpawn(final CreatureSpawnEvent ev) {
        if (inMobsFreePeriod && EntitiesUtils.isNaturalSpawn(ev.getSpawnReason()) &&
                EntitiesUtils.isHostile(ev.getEntityType())) {
            // We check the blocs above the entity to see if we only find surface blocks.
            final Location spawnLocation = ev.getLocation();
            final World world = spawnLocation.getWorld();
            final int highestBlockY = world.getHighestBlockYAt(spawnLocation);

            final int x = spawnLocation.getBlockX();
            final int z = spawnLocation.getBlockZ();

            boolean surface = true;

            for (int y = spawnLocation.getBlockY(); y <= highestBlockY; y++) {
                final Material type = world.getBlockAt(x, y, z).getType();

                switch (type) {
                    // Air
                    case AIR:
                        
                    // Trees
                    case BROWN_MUSHROOM_BLOCK:
                    case RED_MUSHROOM_BLOCK:

                    // Vegetation
                    case DEAD_BUSH:
                    case CHORUS_PLANT:
                    case KELP_PLANT:
                    case GRASS:
                    case CHORUS_FLOWER:
                    case SUNFLOWER:
                    case CORNFLOWER:
                    case VINE:
                    case SUGAR_CANE:
                    case BROWN_MUSHROOM:
                    case RED_MUSHROOM:

                        // Nature
                    case SNOW:

                        // Igloos
                    case SNOW_BLOCK:

                        // Villages
                    case BOOKSHELF:

                        // Redstone
                    case REDSTONE_WIRE:
                    case COMPARATOR:
                    case REDSTONE_TORCH:

                        // Other blocs frequently used on surface on custom maps
                    case TORCH:
                    case RAIL:
                    case ACTIVATOR_RAIL:
                    case DETECTOR_RAIL:
                    case POWERED_RAIL:
                        break;

                    default:
                        surface = false;
                }

                if (!surface) {
                    final String typeName = type.name();
                    surface = SURFACE_MATERIAL_ENDS.stream().anyMatch(typeName::endsWith);
                }

                if (!surface) {
                    break;
                }
            }

            if (surface) {
                ev.setCancelled(true);
            }
        }
    }

    private void setPVP(final boolean pvp) {
        QSG.get().getWorlds().forEach(world -> world.setPVP(pvp));
    }
}
