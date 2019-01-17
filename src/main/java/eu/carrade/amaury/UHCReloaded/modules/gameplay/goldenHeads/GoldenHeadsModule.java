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
package eu.carrade.amaury.UHCReloaded.modules.gameplay.goldenHeads;

import eu.carrade.amaury.UHCReloaded.core.ModuleCategory;
import eu.carrade.amaury.UHCReloaded.core.ModuleInfo;
import eu.carrade.amaury.UHCReloaded.core.ModuleLoadTime;
import eu.carrade.amaury.UHCReloaded.core.UHModule;
import eu.carrade.amaury.UHCReloaded.modules.core.game.events.players.AlivePlayerDeathEvent;
import fr.zcraft.zlib.components.attributes.Attribute;
import fr.zcraft.zlib.components.attributes.Attributes;
import fr.zcraft.zlib.components.i18n.I;
import fr.zcraft.zlib.tools.PluginLogger;
import fr.zcraft.zlib.tools.items.ItemStackBuilder;
import fr.zcraft.zlib.tools.items.ItemUtils;
import fr.zcraft.zlib.tools.reflection.NMSException;
import fr.zcraft.zlib.tools.runners.RunTask;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.SkullType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.UUID;


@ModuleInfo (
        name = "Golden Apple & Heads",
        description = "Changes golden apple behavior. This can change the regeneration from these apple, " +
                "and allow players to craft “golden heads” from fallen heads when they kill a player. You can " +
                "also enable or disable enchanted golden apples (aka “Notch Apples”).",
        when = ModuleLoadTime.ON_GAME_START,
        category = ModuleCategory.GAMEPLAY,
        icon = Material.GOLDEN_APPLE,  // TODO 1.13: enchanted golden apple or player head
        settings = Config.class
)
public class GoldenHeadsModule extends UHModule
{
    private final static UUID HEADS_UUID = UUID.fromString("1a050e3b-6274-434e-b8c0-a720048142e7");

    private final static int TICKS_BETWEEN_EACH_REGENERATION = 50;
    private final static int DEFAULT_NUMBER_OF_HEARTS_REGEN = 4;
    private final static int DEFAULT_NUMBER_OF_HEARTS_REGEN_NOTCH = 180;
    private final static int REGENERATION_LEVEL_GOLDEN_APPLE = 2;
    private final static int REGENERATION_LEVEL_NOTCH_GOLDEN_APPLE = 5;

    @Override
    protected void onEnable()
    {
        if (Config.ENCHANTED_GOLDEN_APPLE.ENABLE.get())
            Bukkit.addRecipe(getOldEnchantedGoldenAppleRecipe());

        if (Config.PLAYER_GOLDEN_HEAD.ENABLE.get())
            Bukkit.addRecipe(getGoldenHeadRecipe(true, false, Config.PLAYER_GOLDEN_HEAD.AMOUNT_CRAFTED.get(), ChatColor.AQUA + I.t("Golden head"))); // FIXME Translation

        if (Config.PLAYER_ENCHANTED_GOLDEN_HEAD.ENABLE.get())
            Bukkit.addRecipe(getGoldenHeadRecipe(true, true, Config.PLAYER_ENCHANTED_GOLDEN_HEAD.AMOUNT_CRAFTED.get(), ChatColor.LIGHT_PURPLE + I.t("Golden head"))); // FIXME Translation

        if (Config.WITHER_GOLDEN_HEAD.ENABLE.get())
            Bukkit.addRecipe(getGoldenHeadRecipe(false, false, Config.WITHER_GOLDEN_HEAD.AMOUNT_CRAFTED.get(), ChatColor.AQUA + I.t("Golden head"))); // FIXME Translation

        if (Config.WITHER_ENCHANTED_GOLDEN_HEAD.ENABLE.get())
            Bukkit.addRecipe(getGoldenHeadRecipe(false, true, Config.WITHER_ENCHANTED_GOLDEN_HEAD.AMOUNT_CRAFTED.get(), ChatColor.LIGHT_PURPLE + I.t("Golden head"))); // FIXME Translation
    }

    private Recipe getGoldenHeadRecipe(final boolean player, final boolean enchanted, final int amount, final String resultDisplayName)
    {
        short damage = (short) (enchanted ? 1 : 0);

        final ItemStack goldenApple = new ItemStackBuilder(Material.GOLDEN_APPLE)
                .title(ChatColor.RESET + resultDisplayName)
                .amount(amount)
                .data(damage)
                .craftItem();  // Required to write attributes by reference

        writeHeadType(goldenApple, player ? SkullType.PLAYER : SkullType.WITHER);

        final ShapedRecipe goldenAppleFromHeadRecipe = new ShapedRecipe(goldenApple);

        goldenAppleFromHeadRecipe.shape("GGG", "GHG", "GGG");
        goldenAppleFromHeadRecipe.setIngredient('G', enchanted ? Material.GOLD_BLOCK : Material.GOLD_INGOT);

        // TODO: deprecated, but no alternative found...
        // FIXME 1.13
        goldenAppleFromHeadRecipe.setIngredient('H', Material.SKULL_ITEM, player ? SkullType.PLAYER.ordinal() : SkullType.WITHER.ordinal());

        return goldenAppleFromHeadRecipe;
    }

    private Recipe getOldEnchantedGoldenAppleRecipe()
    {
        final ShapedRecipe enchantedGoldenAppleRecipe = new ShapedRecipe(new ItemStack(Material.GOLDEN_APPLE, 1, (short) 1));  // FIXME 1.13
        enchantedGoldenAppleRecipe.shape("GGG", "GAG", "GGG");
        enchantedGoldenAppleRecipe.setIngredient('G', Material.GOLD_BLOCK);
        enchantedGoldenAppleRecipe.setIngredient('A', Material.APPLE);

        return enchantedGoldenAppleRecipe;
    }

    private void writeHeadType(final ItemStack stack, final SkullType type)
    {
        final Attribute attribute = new Attribute();
        attribute.setUUID(HEADS_UUID);
        attribute.setCustomData(type.name());

        try
        {
            Attributes.set(stack, attribute);
        }
        catch (NMSException e)
        {
            PluginLogger.error("Unable to write head type into item stack.", e);
        }
    }

    private SkullType readHeadType(final ItemStack stack)
    {
        try
        {
            final Attribute attribute = Attributes.get(stack, HEADS_UUID);

            if (attribute != null)
            {
                return SkullType.valueOf(attribute.getCustomData());
            }
            else return null;
        }
        catch (NMSException | IllegalArgumentException e)
        {
            return null;
        }
    }

    @EventHandler
    public void onPreCraft(final PrepareItemCraftEvent ev)
    {
        /* *** We remove these recipes if disabled *** */

        final ItemStack result = ev.getInventory().getResult();
        if (result == null || (result.getType() != Material.GOLDEN_APPLE /* && result.getType() != Material.ENCHANTED_GOLDEN_APPLE FIXME 1.13 */)) return;

        if ((!Config.GOLDEN_APPLE.ENABLE.get() && result.getType() == Material.GOLDEN_APPLE && result.getData().getData() == 0)
                || (!Config.ENCHANTED_GOLDEN_APPLE.ENABLE.get() && result.getType() == Material.GOLDEN_APPLE && result.getData().getData() == 1))
        {
            result.setType(Material.AIR);
        }


        /* *** We add a lore to the golden apples *** */

        final SkullType headType = readHeadType(result);
        if (headType != null)
        {
            final ItemStackBuilder revampedResult = new ItemStackBuilder(result);

            if (headType == SkullType.WITHER &&
                    ((result.getData().getData() == 0 && Config.WITHER_GOLDEN_HEAD.ADD_LORE.get()) || (result.getData().getData() == 1 && Config.WITHER_ENCHANTED_GOLDEN_HEAD.ADD_LORE.get())))
            {
                revampedResult.longLore(ChatColor.GRAY, ChatColor.ITALIC + I.t("Made from the fallen head of a malignant monster"));
            }

            else if (headType == SkullType.PLAYER &&
                    ((result.getData().getData() == 0 && Config.PLAYER_GOLDEN_HEAD.ADD_LORE.get()) || (result.getData().getData() == 1 && Config.PLAYER_ENCHANTED_GOLDEN_HEAD.ADD_LORE.get())))
            {
                // We retrieve the player name to write it into the lore
                String name = null;

                for (final ItemStack item : ev.getInventory().getContents())
                {
                    // An human head
                    if (item.getType() == Material.SKULL_ITEM && item.getDurability() == (short) SkullType.PLAYER.ordinal())
                    {
                        SkullMeta sm = (SkullMeta) item.getItemMeta();
                        if (sm.hasOwner()) // An human head
                        {
                            name = sm.getOwner();
                        }
                        break;
                    }
                }

                if (name != null)
                {
                    revampedResult.longLore(ChatColor.GRAY, ChatColor.ITALIC + I.t("Made from the fallen head of {0}", name));
                }
                else
                {
                    revampedResult.longLore(ChatColor.GRAY, ChatColor.ITALIC + I.t("Made from the fallen head of a powerful opponent"));
                }
            }

            revampedResult.hideAttributes().item();
        }
    }

    @EventHandler
    public void onPlayerDeath(final AlivePlayerDeathEvent ev)
    {
        if (Config.DROP_HEAD_ON_DEATH.get() && ev.getPlayer().isOnline() && (!Config.DROP_HEAD_ON_DEATH_PVP_ONLY.get() || ev.getPlayer().getPlayer().getKiller() != null))
        {
            final ItemStackBuilder headBuilder = new ItemStackBuilder(Material.SKULL_ITEM)
                    .data((short) 3)
                    .title(ChatColor.AQUA, I.t("{0}'s head", ev.getPlayer().getName()));

            if (Config.PLAYER_GOLDEN_HEAD.ENABLE.get() || Config.PLAYER_ENCHANTED_GOLDEN_HEAD.ENABLE.get())
            {
                headBuilder.longLore(ChatColor.GRAY, ChatColor.ITALIC + I.t("Old legends tell how the heads of the brave fallen warriors can become, through a rich and complex transformation, a precious healing balm..."), 38);
            }

            final ItemStack head = headBuilder.item();
            final SkullMeta meta = (SkullMeta) head.getItemMeta();

            meta.setOwner(ev.getPlayer().getName());
            head.setItemMeta(meta);

            ItemUtils.dropNaturally(ev.getPlayer().getPlayer().getLocation(), head);
        }
    }


    /**
     * Used to change the amount of regenerated hearts from a golden apple.
     */
    @EventHandler
    public void onPlayerItemConsume(final PlayerItemConsumeEvent ev)
    {
        if (ev.getItem().getType() == Material.GOLDEN_APPLE)  // FIXME 1.13
        {
            final SkullType headType = readHeadType(ev.getItem());
            final short dataValue = ev.getItem().getDurability();

            final int halfHearts;
            final int level;

            // Standard golden apple
            if (headType == null)
            {
                if (dataValue == 0)  // FIXME 1.13
                {
                    halfHearts = Config.GOLDEN_APPLE.REGENERATION.get();
                    level = REGENERATION_LEVEL_GOLDEN_APPLE;
                }
                else
                {
                    halfHearts = Config.ENCHANTED_GOLDEN_APPLE.REGENERATION.get();
                    level = REGENERATION_LEVEL_NOTCH_GOLDEN_APPLE;
                }
            }

            else if (headType == SkullType.PLAYER)
            {
                if (dataValue == 0)  // FIXME 1.13
                {
                    halfHearts = Config.PLAYER_GOLDEN_HEAD.REGENERATION.get();
                    level = REGENERATION_LEVEL_GOLDEN_APPLE;
                }
                else
                {
                    halfHearts = Config.PLAYER_ENCHANTED_GOLDEN_HEAD.REGENERATION.get();
                    level = REGENERATION_LEVEL_NOTCH_GOLDEN_APPLE;
                }
            }

            else if (headType == SkullType.WITHER)
            {
                if (dataValue == 0)  // FIXME 1.13
                {
                    halfHearts = Config.WITHER_GOLDEN_HEAD.REGENERATION.get();
                    level = REGENERATION_LEVEL_GOLDEN_APPLE;
                }
                else
                {
                    halfHearts = Config.WITHER_ENCHANTED_GOLDEN_HEAD.REGENERATION.get();
                    level = REGENERATION_LEVEL_NOTCH_GOLDEN_APPLE;
                }
            }

            // Invalid attribute. Should never happen.
            else return;


            // Technically, a level-I effect is « level 0 ».
            final int realLevel = level - 1;


            // What is needed to do?
            if ((dataValue == 0 && halfHearts == DEFAULT_NUMBER_OF_HEARTS_REGEN)
                    || (dataValue == 1 && halfHearts == DEFAULT_NUMBER_OF_HEARTS_REGEN_NOTCH))
            {
                // Default behavior, nothing to do.
                return;
            }

            if ((dataValue == 0 && halfHearts > DEFAULT_NUMBER_OF_HEARTS_REGEN)
                    || (dataValue == 1 && halfHearts > DEFAULT_NUMBER_OF_HEARTS_REGEN_NOTCH))
            {
                // If the heal needs to be increased, the effect can be applied immediately.

                int duration = ((int) Math.floor(TICKS_BETWEEN_EACH_REGENERATION / (Math.pow(2, realLevel)))) * halfHearts;

                new PotionEffect(PotionEffectType.REGENERATION, duration, realLevel).apply(ev.getPlayer());
            }
            else
            {
                // The heal needs to be decreased.
                // We can't apply the effect immediately, because the server will just ignore it.
                // So, we apply it two ticks later, with one half-heart less (because in two ticks,
                // one half-heart is given to the player).
                final int healthApplied = halfHearts - 1;

                RunTask.later(() ->
                {
                    // The original, vanilla, effect is removed
                    ev.getPlayer().removePotionEffect(PotionEffectType.REGENERATION);

                    int duration = ((int) Math.floor(TICKS_BETWEEN_EACH_REGENERATION / (Math.pow(2, realLevel)))) * healthApplied;
                    new PotionEffect(PotionEffectType.REGENERATION, duration, realLevel).apply(ev.getPlayer());
                }, 2L);
            }
        }
    }
}
