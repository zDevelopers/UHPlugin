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
package eu.carrade.amaury.quartzsurvivalgames.core;

import fr.zcraft.quartzlib.components.i18n.I;
import fr.zcraft.quartzlib.tools.items.ItemStackBuilder;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.function.Supplier;

public enum ModuleCategory
{
    CORE (
            I.t("Core Modules"),
            I.t("These modules are the core of UHCReloaded, required by other modules."),
            Material.BEDROCK,
            ChatColor.DARK_RED
    ),

    WORLD_GENERATION (
            I.t("World Generation"),
            I.t("These modules alter the world generation. To use them, the server must be started without world."),
            Material.REDSTONE_COMPARATOR,
            ChatColor.DARK_GREEN
    ),

    WAITING_PHASE (
            I.t("Waiting Phase"),
            I.t("These modules manage the waiting phase, when the game is not yet started."),
            Material.WATCH,
            ChatColor.YELLOW
    ),

    STARTING (
            I.t("Game Beginning"),
            I.t("These modules enhance the starting phase of the game."),
            Material.FEATHER,
            ChatColor.GOLD
    ),

    END (
            I.t("Game End"),
            I.t("These modules alter the game end (either player deaths or whole game end)."),
            Material.SKULL_ITEM,
            ChatColor.RED
    ),

    GAMEPLAY (
            I.t("Gameplay"),
            I.t("These modules alter the world or gameplay during the game, e.g. adding or nerfing creatures, effects... This category does not contains scenarii, which are in a dedicated one."),
            Material.SADDLE,
            ChatColor.DARK_AQUA
    ),

    SCENARII (
            I.t("Scenarii"),
            I.t("These modules adds scenarii to the game, i.e. global set of changes that changes the gameplay in a deeper way, and possibly the whole game experience."),
            Material.BEACON,
            ChatColor.AQUA
    ),

    UTILITIES (
            I.t("Utilities"),
            I.t("These modules provides tools and utilities to manage the game and offer useful commands."),
            Material.COMMAND,
            ChatColor.DARK_PURPLE
    ),

    COSMETICS (
            I.t("Cosmetics"),
            I.t("These modules adds cosmetics things to the game, like effects or visual enhancements that can be useful but does not change the gameplay."),
            new ItemStackBuilder(Material.RED_ROSE).data((short) 1),  // FIXME 1.13
            ChatColor.LIGHT_PURPLE
    ),

    EXTERNAL (
            I.t("External"),
            I.t("These modules adds features alongside the game, like web maps, external summaries..."),
            Material.ENDER_CHEST,
            ChatColor.DARK_GREEN
    ),

    OTHER (
            I.t("Others"),
            I.t("All uncategorized modules goes there."),
            () -> {
                final ItemStack icon = new ItemStackBuilder(Material.SKULL_ITEM).data((short) 3).item();
                final SkullMeta meta = (SkullMeta) icon.getItemMeta();
                meta.setOwner("MHF_Question");
                icon.setItemMeta(meta);
                return icon;
            },
            ChatColor.WHITE
    )

    ;


    private final String displayName;
    private final String description;
    private final ItemStack icon;
    private final ChatColor color;

    ModuleCategory(final String displayName, final String description, final ItemStack icon, ChatColor color)
    {

        this.displayName = displayName;
        this.description = description;
        this.icon = icon;
        this.color = color;
    }

    ModuleCategory(final String displayName, final String description, final ItemStackBuilder icon, ChatColor color)
    {
        this(displayName, description, icon.item(), color);
    }

    ModuleCategory(final String displayName, final String description, final Supplier<ItemStack> icon, ChatColor color)
    {
        this(displayName, description, icon.get(), color);
    }

    ModuleCategory(final String displayName, final String description, final Material icon, ChatColor color)
    {
        this(displayName, description, new ItemStack(icon), color);
    }

    public String getDisplayName()
    {
        return displayName;
    }

    public String getDescription()
    {
        return description;
    }

    public ItemStack getIcon()
    {
        return icon.clone();
    }

    public ChatColor getColor()
    {
        return color;
    }
}
