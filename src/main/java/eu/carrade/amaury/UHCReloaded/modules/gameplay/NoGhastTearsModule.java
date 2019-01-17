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
package eu.carrade.amaury.UHCReloaded.modules.gameplay;

import eu.carrade.amaury.UHCReloaded.core.ModuleCategory;
import eu.carrade.amaury.UHCReloaded.core.ModuleInfo;
import eu.carrade.amaury.UHCReloaded.core.ModuleLoadTime;
import eu.carrade.amaury.UHCReloaded.core.UHModule;
import eu.carrade.amaury.UHCReloaded.modules.core.game.GameModule;
import eu.carrade.amaury.UHCReloaded.shortcuts.UR;
import org.bukkit.Material;
import org.bukkit.entity.Ghast;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

@ModuleInfo (
        name = "No Ghast Tears",
        description = "Replaces ghast tears with gold, to disable regeneration " +
                "potions while keeping a reward for the action.",
        when = ModuleLoadTime.ON_GAME_START,
        category = ModuleCategory.GAMEPLAY,
        icon = Material.GHAST_TEAR
)
public class NoGhastTearsModule extends UHModule
{
    /**
     * Used to replace ghast tears with gold (if needed).
     */
    @EventHandler(ignoreCancelled = true)
    public void onEntityDeath(EntityDeathEvent ev)
    {
        if (ev.getEntity() instanceof Ghast)
        {
            final List<ItemStack> drops = new ArrayList<>(ev.getDrops());
            ev.getDrops().clear();

            for (final ItemStack i : drops)
            {
                if (i.getType() == Material.GHAST_TEAR)
                {
                    ev.getDrops().add(new ItemStack(Material.GOLD_INGOT, i.getAmount()));
                }
                else
                {
                    ev.getDrops().add(i);
                }
            }
        }
    }

    /**
     * Used to prevent the user to get a ghast tear.
     */
    @EventHandler (ignoreCancelled = true)
    public void onPlayerPickupItem(PlayerPickupItemEvent ev)
    {
        if (ev.getItem().getItemStack().getType() == Material.GHAST_TEAR && UR.module(GameModule.class).isAlive(ev.getPlayer()))
        {
            ev.setCancelled(true);
        }
    }
}
