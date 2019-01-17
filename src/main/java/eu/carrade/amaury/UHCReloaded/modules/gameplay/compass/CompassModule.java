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
package eu.carrade.amaury.UHCReloaded.modules.gameplay.compass;

import eu.carrade.amaury.UHCReloaded.core.ModuleCategory;
import eu.carrade.amaury.UHCReloaded.core.ModuleInfo;
import eu.carrade.amaury.UHCReloaded.core.ModuleLoadTime;
import eu.carrade.amaury.UHCReloaded.core.UHModule;
import eu.carrade.amaury.UHCReloaded.modules.core.game.GameModule;
import eu.carrade.amaury.UHCReloaded.shortcuts.UR;
import eu.carrade.amaury.UHCReloaded.utils.UHSound;
import eu.carrade.amaury.UHCReloaded.utils.UHUtils;
import fr.zcraft.zlib.components.i18n.I;
import fr.zcraft.zlib.components.rawtext.RawText;
import fr.zcraft.zlib.core.ZLib;
import fr.zcraft.zlib.tools.runners.RunTask;
import fr.zcraft.zlib.tools.text.RawMessage;
import fr.zcraft.zteams.ZTeams;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;

@ModuleInfo (
        name = "Compass",
        description = "Compasses in the game can be used to point to the nearest player, " +
                "and/or give the distance to them, at a configurable fee. The compass' craft " +
                "can also be made harder.",
        when = ModuleLoadTime.ON_GAME_START,
        category = ModuleCategory.GAMEPLAY,
        icon = Material.COMPASS,
        settings = Config.class
)
public class CompassModule extends UHModule
{
    private CompassRecipes recipes = null;

    private Set<UUID> compassLocked = new HashSet<>();


    @Override
    public void onEnable()
    {
        recipes = ZLib.loadComponent(CompassRecipes.class);
    }

    @Override
    protected void onDisable()
    {
        if (recipes != null)
        {
            ZLib.unregisterEvents(recipes);
            recipes = null;
        }
    }

    /**
     * Used to update the compass.
     */
    @EventHandler (ignoreCancelled = true)
    public void onPlayerInteract(final PlayerInteractEvent ev)
    {
        if (compassLocked.contains(ev.getPlayer().getUniqueId())) return;
        else
        {
            compassLocked.add(ev.getPlayer().getUniqueId());
            RunTask.later(() -> compassLocked.remove(ev.getPlayer().getUniqueId()), 20L);
        }

        if ((ev.getAction() == Action.RIGHT_CLICK_AIR || ev.getAction() == Action.RIGHT_CLICK_BLOCK)
                && ev.getPlayer().getItemInHand().getType() == Material.COMPASS
                && UR.module(GameModule.class).isAlive(ev.getPlayer()))
        {
            final Player compassUser = ev.getPlayer();

            // We check if the player have what needed

            int feeAvailable = Arrays.stream(compassUser.getInventory().getContents())
                    .filter(item -> item != null && item.getType() == Config.COMPASS_FEE_ITEM.get())
                    .mapToInt(ItemStack::getAmount)
                    .sum();

            if (feeAvailable < Config.COMPASS_FEE_AMOUNT.get())
            {
                RawMessage.send(compassUser, new RawText(I.t("To use the compass, you must have the following: "))
                        .then().translate(new ItemStack(Config.COMPASS_FEE_ITEM.get(), Config.COMPASS_FEE_AMOUNT.get()))
                        .then(Config.COMPASS_FEE_AMOUNT.get() > 1 ? " × " + Config.COMPASS_FEE_AMOUNT.get() + "." : ".")
                        .build()
                );
                new UHSound(1F, 1F, "BLOCK_WOOD_STEP", "STEP_WOOD").play(compassUser);
                return;
            }

            // We consume the fee

            int feeLeft = Config.COMPASS_FEE_AMOUNT.get();

            for (final ItemStack item : compassUser.getInventory().getContents())
            {
                if (item != null && item.getType() == Config.COMPASS_FEE_ITEM.get())
                {
                    final int consumed = item.getAmount() - feeLeft;

                    if (consumed <= 0)
                    {
                        feeLeft -= item.getAmount();
                        item.setAmount(0);
                        item.setType(Material.AIR);
                    }
                    else
                    {
                        feeLeft = 0;
                        item.setAmount(consumed);
                    }

                    if (feeLeft == 0) break;
                }
            }

            // We lookup for the nearest player

            Player nearest = null;
            Double distance = Double.MAX_VALUE;

            for (Player otherPlayer : UR.module(GameModule.class).getAliveConnectedPlayers())
            {
                try
                {
                    Double calc = compassUser.getLocation().distanceSquared(otherPlayer.getLocation());

                    if (calc > 1 && calc < distance)
                    {
                        distance = calc;

                        if (!otherPlayer.getUniqueId().equals(compassUser.getUniqueId()) && (!Config.NEVER_TARGET_TEAMMATES.get() || !Objects.equals(ZTeams.get().getTeamForPlayer(compassUser), ZTeams.get().getTeamForPlayer(otherPlayer))))
                        {
                            nearest = otherPlayer.getPlayer();
                        }
                    }
                }
                catch (Exception ignored) {}  // Different worlds
            }

            if (nearest == null)
            {
                /// Error message if a player tries to use his pointing compass without a player nearby.
                compassUser.sendMessage(UHUtils.prefixedMessage(I.t("Compass"), ChatColor.YELLOW + I.t("Only silence answers your request.")));  // TODO update language files

                new UHSound(1F, 1F, "BLOCK_WOOD_STEP", "STEP_WOOD").play(compassUser);
                return;
            }

            final CompassBehavior behavior = Config.COMPASS_BEHAVIOR.get();

            if (behavior == CompassBehavior.GIVE_DIRECTION || behavior == CompassBehavior.GIVE_BOTH)
            {
                /// Success message when a player uses his pointing compass.
                compassUser.sendMessage(UHUtils.prefixedMessage(I.t("Compass"), ChatColor.YELLOW + I.t("The compass now points to the closest player.")));  // TODO update language files
                compassUser.setCompassTarget(nearest.getLocation());
            }

            if (behavior == CompassBehavior.GIVE_DISTANCE || behavior == CompassBehavior.GIVE_BOTH)
            {
                compassUser.sendMessage(UHUtils.prefixedMessage(I.t("Compass"), I.tn("{yellow}There is {gold}{0} block {yellow}between the nearest player and yourself.", "{yellow}There are {gold}{0} blocks {yellow}between the nearest player and yourself.", (int) nearest.getLocation().distance(compassUser.getLocation()))));
            }

            else if (behavior == CompassBehavior.GIVE_EITHER_RANDOMLY)
            {
                // TODO
            }

            new UHSound(1F, 1F, "ENTITY_ENDERMEN_TELEPORT", "ENDERMAN_TELEPORT").play(compassUser);
        }
    }

    public enum CompassBehavior
    {
        GIVE_DIRECTION,
        GIVE_DISTANCE,
        GIVE_BOTH,
        GIVE_EITHER_RANDOMLY
    }
}
