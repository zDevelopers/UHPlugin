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
package eu.carrade.amaury.quartzsurvivalgames.modules.other;

import eu.carrade.amaury.quartzsurvivalgames.core.ModuleCategory;
import eu.carrade.amaury.quartzsurvivalgames.core.ModuleInfo;
import eu.carrade.amaury.quartzsurvivalgames.core.ModuleLoadTime;
import eu.carrade.amaury.quartzsurvivalgames.core.QSGModule;
import eu.carrade.amaury.quartzsurvivalgames.modules.core.game.GamePhase;
import eu.carrade.amaury.quartzsurvivalgames.modules.core.game.events.game.GamePhaseChangedEvent;
import eu.carrade.amaury.quartzsurvivalgames.shortcuts.QSG;
import fr.zcraft.quartzlib.components.events.FutureEventHandler;
import fr.zcraft.quartzlib.components.events.WrappedEvent;
import fr.zcraft.quartzlib.tools.PluginLogger;
import fr.zcraft.quartzlib.tools.reflection.Reflection;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandException;
import org.bukkit.event.EventHandler;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;


@ModuleInfo (
        name = "Advancements",
        description = "Resets the advancements when the game starts. Disable if " +
                "you want to keep old advancements.\n\n" +
                "Even if the name corresponds to Minecraft 1.12+, this module " +
                "also handles achievements for older Minecraft versions.",
        when = ModuleLoadTime.POST_WORLD,
        category = ModuleCategory.OTHER,
        icon = Material.HAY_BLOCK
)
public class AdvancementsModule extends QSGModule
{
    @FutureEventHandler (event = "player.PlayerAchievementAwardedEvent", ignoreCancelled = true)
    public void onAchievementAwarded(final WrappedEvent ev)
    {
        if (QSG.game().getPhase() == GamePhase.WAIT)
        {
            ev.setCancelled(true);
        }
    }

    @EventHandler
    public void onGameStarts(final GamePhaseChangedEvent ev)
    {
        if (ev.getNewPhase() != GamePhase.IN_GAME || !ev.isRunningForward()) return;


        // Achievements

        try
        {
            final Object[] achievements = Class.forName("org.bukkit.Achievement").getEnumConstants();

            QSG.game().getAliveConnectedPlayers().forEach(player -> {
                try
                {
                    for (final Object achievement : achievements)
                    {
                        PluginLogger.info("Removing achievement {0}", achievement);
                        Reflection.call(player, "removeAchievement", achievement);
                    }
                }
                catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException ignored) {}
            });
        }
        catch (final Exception ignored) {} // Unsupported


        // Advancements

        final Map<String, String> oldGameRule = new HashMap<>();
        QSG.get().getWorlds().forEach(world -> {
            oldGameRule.put(world.getName(), world.getGameRuleValue("sendCommandFeedback"));
            world.setGameRuleValue("sendCommandFeedback", "false");
        });

        QSG.game().getAliveConnectedPlayers().forEach(player -> {
            try
            {
                // ¯\_(ツ)_/¯
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "advancement revoke " + player.getName() + " everything");
            }
            catch (final CommandException ignored) {}
        });

        QSG.get().getWorlds().forEach(world -> world.setGameRuleValue("sendCommandFeedback", oldGameRule.get(world.getName())));
        oldGameRule.clear();
    }
}
