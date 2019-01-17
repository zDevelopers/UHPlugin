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
package eu.carrade.amaury.UHCReloaded.modules.ingame.weather;

import eu.carrade.amaury.UHCReloaded.core.ModuleCategory;
import eu.carrade.amaury.UHCReloaded.core.ModuleInfo;
import eu.carrade.amaury.UHCReloaded.core.UHModule;
import eu.carrade.amaury.UHCReloaded.modules.core.game.GameModule;
import eu.carrade.amaury.UHCReloaded.modules.core.game.GamePhase;
import eu.carrade.amaury.UHCReloaded.modules.core.game.events.game.GamePhaseChangedEvent;
import eu.carrade.amaury.UHCReloaded.shortcuts.UR;
import org.bukkit.Material;
import org.bukkit.WeatherType;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.weather.WeatherChangeEvent;

import java.util.HashSet;
import java.util.Set;


@ModuleInfo (
        name = "Weather",
        description = "Manages the in-game weather.",
        settings = Config.class,
        category = ModuleCategory.GAMEPLAY,
        icon = Material.DOUBLE_PLANT  // Sunflower  -  FIXME 1.13
)
public class WeatherModule extends UHModule
{
    private Set<World> firstWeatherUpdateOccurred = new HashSet<>();

    @Override
    protected void onEnable()
    {
        UR.get().getWorlds().forEach(world -> world.setStorm(Config.WAITING_PHASE_WEATHER.get() == WeatherType.DOWNFALL));
    }

    @Override
    public void onLateEnable()
    {
        UR.get().getWorlds().forEach(world -> world.setStorm(Config.INITIAL_WEATHER.get() == WeatherType.DOWNFALL));
    }

    @EventHandler
    public void onGameStarts(final GamePhaseChangedEvent ev)
    {
        if (ev.getNewPhase() == GamePhase.IN_GAME && ev.isRunningForward())
        {
            onLateEnable();
        }
    }

    @EventHandler
    public void onWeatherChange(final WeatherChangeEvent ev)
    {
        switch (UR.module(GameModule.class).getPhase())
        {
            case WAIT:
            case STARTING:
                ev.setCancelled(true);
                break;

            case IN_GAME:
            case END:
                if (!Config.WEATHER_CYCLE.get())
                {
                    // We allow a single weather update, as it will be
                    // the one from the onGameStart event.
                    if (firstWeatherUpdateOccurred.contains(ev.getWorld()))
                    {
                        ev.setCancelled(true);
                    }
                    else
                    {
                        firstWeatherUpdateOccurred.add(ev.getWorld());
                    }
                }
                break;
        }
    }
}
