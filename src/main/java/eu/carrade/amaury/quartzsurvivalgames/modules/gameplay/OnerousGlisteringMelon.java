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

package eu.carrade.amaury.quartzsurvivalgames.modules.gameplay;

import eu.carrade.amaury.quartzsurvivalgames.core.ModuleCategory;
import eu.carrade.amaury.quartzsurvivalgames.core.ModuleInfo;
import eu.carrade.amaury.quartzsurvivalgames.core.ModuleLoadTime;
import eu.carrade.amaury.quartzsurvivalgames.core.QSGModule;
import eu.carrade.amaury.quartzsurvivalgames.utils.RecipesUtils;
import fr.zcraft.quartzlib.tools.items.CraftingRecipes;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;


@ModuleInfo(
        name = "Onerous Glistering Melon",
        description = "Changes the vanilla recipe for glistering melon, replacing gold nuggets by gold ingots, to make healing potions harder to get.",
        when = ModuleLoadTime.ON_GAME_START,
        category = ModuleCategory.GAMEPLAY,
        icon = Material.GLISTERING_MELON_SLICE   // FIXME 1.13
)
public class OnerousGlisteringMelon extends QSGModule {
    private final Recipe VANILLA_RECIPE = CraftingRecipes.shaped(
            new ItemStack(Material.GLISTERING_MELON_SLICE),
            "AAA",
            "ABA",
            "AAA",
            Material.GOLD_NUGGET, Material.MELON  // FIXME 1.13
    );

    private final Recipe ONEROUS_RECIPE = CraftingRecipes.shaped(
            new ItemStack(Material.GLISTERING_MELON_SLICE),
            "AAA",
            "ABA",
            "AAA",
            Material.GOLD_INGOT, Material.MELON  // FIXME 1.13
    );

    @Override
    protected void onEnable() {
        CraftingRecipes.add(ONEROUS_RECIPE);
    }

    @EventHandler
    private void onPreCraft(final PrepareItemCraftEvent ev) {
        if (RecipesUtils.areSimilar(ev.getRecipe(), VANILLA_RECIPE)) {
            ev.getInventory().setResult(new ItemStack(Material.AIR));
        }
    }
}
