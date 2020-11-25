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
package eu.carrade.amaury.quartzsurvivalgames.modules.gameplay.compass;

import eu.carrade.amaury.quartzsurvivalgames.core.ModuleCategory;
import eu.carrade.amaury.quartzsurvivalgames.core.ModuleInfo;
import eu.carrade.amaury.quartzsurvivalgames.core.ModuleLoadTime;
import eu.carrade.amaury.quartzsurvivalgames.core.QSGModule;
import eu.carrade.amaury.quartzsurvivalgames.modules.core.game.GameModule;
import eu.carrade.amaury.quartzsurvivalgames.shortcuts.QSG;
import eu.carrade.amaury.quartzsurvivalgames.utils.QSGSound;
import eu.carrade.amaury.quartzsurvivalgames.utils.QSGUtils;
import fr.zcraft.quartzlib.components.i18n.I;
import fr.zcraft.quartzlib.components.i18n.I18n;
import fr.zcraft.quartzlib.components.rawtext.RawText;
import fr.zcraft.quartzlib.core.ZLib;
import fr.zcraft.quartzlib.tools.runners.RunTask;
import fr.zcraft.quartzlib.tools.text.MessageSender;
import fr.zcraft.zteams.ZTeams;
import org.apache.commons.lang.math.RandomUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
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
public class CompassModule extends QSGModule
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
    @EventHandler (priority = EventPriority.LOWEST)
    public void onPlayerInteract(final PlayerInteractEvent ev)
    {
        if (ev.getAction() != Action.PHYSICAL)
        {
            ev.setCancelled(activateCompass(ev.getPlayer()));
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerInteractAtEntity(final PlayerInteractAtEntityEvent ev)
    {
        ev.setCancelled(activateCompass(ev.getPlayer()));
    }

    /**
     * Activates the compass for the player.
     *
     * @param compassUser The player.
     * @return {@code true} if the compass was activated.
     */
    private boolean activateCompass(final Player compassUser)
    {
        if (!QSG.game().isAlive(compassUser) || compassUser.getItemInHand().getType() != Material.COMPASS) return false;
        if (compassLocked.contains(compassUser.getUniqueId())) return false;

        compassLocked.add(compassUser.getUniqueId());
        RunTask.later(() -> compassLocked.remove(compassUser.getUniqueId()), 10L);

        final Locale locale = I18n.getPlayerLocale(compassUser);

        // We check if the player have what needed

        int feeAvailable = Arrays.stream(compassUser.getInventory().getContents())
                .filter(item -> item != null && item.getType() == Config.COMPASS_FEE_ITEM.get())
                .mapToInt(ItemStack::getAmount)
                .sum();

        if (feeAvailable < Config.COMPASS_FEE_AMOUNT.get())
        {
            MessageSender.sendActionBarMessage(compassUser, new RawText()
                    /// The singular is in the sentence « To use the compass, you must have one rotten flesh ». The plural: « […] you must have Rotten Flesh × 2 »?
                    .then(I.tln(locale, "To use the compass, you must have one ", "To use the compass, you must have ", Config.COMPASS_FEE_AMOUNT.get())).color(ChatColor.RED)
                    .then().translate(new ItemStack(Config.COMPASS_FEE_ITEM.get(), Config.COMPASS_FEE_AMOUNT.get())).color(ChatColor.RED)
                    .then(Config.COMPASS_FEE_AMOUNT.get() > 1 ? " × " + Config.COMPASS_FEE_AMOUNT.get() : "").color(ChatColor.RED)
                    .then(".").color(ChatColor.RED)
                    .build()
            );
            new QSGSound(1F, 1F, "BLOCK_WOOD_STEP", "STEP_WOOD").play(compassUser);
            return false;
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

        for (final Player otherPlayer : QSG.module(GameModule.class).getAliveConnectedPlayers())
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
            MessageSender.sendActionBarMessage(compassUser, QSGUtils
                    .prefixedMessage(ChatColor.BOLD + I.tl(locale, "Compass"), ChatColor.YELLOW + "" + ChatColor.BOLD + I.tl(locale, "Only silence answers your request.")));

            new QSGSound(1F, 1F, "BLOCK_WOOD_STEP", "STEP_WOOD").play(compassUser);
            return false;
        }

        CompassBehavior behavior = Config.COMPASS_BEHAVIOR.get();

        if (behavior == CompassBehavior.GIVE_EITHER_RANDOMLY)
        {
            final double r = RandomUtils.nextDouble();

            if (r < .45) behavior = CompassBehavior.GIVE_DIRECTION;
            else if (r < .9) behavior = CompassBehavior.GIVE_DISTANCE;
            else behavior = CompassBehavior.GIVE_BOTH;
        }

        if (behavior == CompassBehavior.GIVE_BOTH)
        {
            compassUser.setCompassTarget(nearest.getLocation());
            compassUser.sendMessage(QSGUtils.prefixedMessage(
                    I.tl(locale, "Compass"),
                    ChatColor.YELLOW + I.tln(
                            locale,
                            "The compass now points to the closest player, {gold}{0} block {yellow}from you.",
                            "The compass now points to the closest player, {gold}{0} blocks {yellow}from you.",
                            (int) nearest.getLocation().distanceSquared(compassUser.getLocation())
                    )
            ));
        }

        else if (behavior == CompassBehavior.GIVE_DIRECTION)
        {
            compassUser.setCompassTarget(nearest.getLocation());

            /// Success message when a player uses his pointing compass.
            MessageSender.sendActionBarMessage(compassUser, ChatColor.YELLOW + "" + ChatColor.BOLD + I.tl(locale, "The compass now points to the closest player."));
        }

        else
        {
            compassUser.sendMessage(QSGUtils.prefixedMessage(I.tl(locale, "Compass"), I.tln(locale, "{yellow}There is {gold}{0} block {yellow}between the nearest player and yourself.", "{yellow}There are {gold}{0} blocks {yellow}between the nearest player and yourself.", (int) nearest.getLocation().distance(compassUser.getLocation()))));
        }

        new QSGSound(1F, 1F, "ENTITY_ENDERMEN_TELEPORT", "ENDERMAN_TELEPORT").play(compassUser);

        return true;
    }

    public enum CompassBehavior
    {
        GIVE_DIRECTION,
        GIVE_DISTANCE,
        GIVE_BOTH,
        GIVE_EITHER_RANDOMLY
    }
}
