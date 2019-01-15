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
package eu.carrade.amaury.UHCReloaded.modules.core.modules.gui;

import eu.carrade.amaury.UHCReloaded.core.ModuleCategory;
import eu.carrade.amaury.UHCReloaded.core.ModuleWrapper;
import eu.carrade.amaury.UHCReloaded.shortcuts.UR;
import eu.carrade.amaury.UHCReloaded.utils.ColorsUtils;
import eu.carrade.amaury.UHCReloaded.utils.UHUtils;
import fr.zcraft.zlib.components.gui.ExplorerGui;
import fr.zcraft.zlib.components.gui.Gui;
import fr.zcraft.zlib.components.gui.GuiAction;
import fr.zcraft.zlib.components.i18n.I;
import fr.zcraft.zlib.tools.items.ItemStackBuilder;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class ModulesListGUI extends ExplorerGui<ModuleWrapper>
{
    /**
     * The category filtered. {@code null} means all.
     */
    private ModuleCategory filterCategory;

    public ModulesListGUI()
    {
        this(null);
    }

    public ModulesListGUI(final ModuleCategory filterCategory)
    {
        this.filterCategory = filterCategory;
    }

    @Override
    protected void onUpdate()
    {
        setSize(6 * 9);
        setMode(Mode.READONLY);
        setKeepHorizontalScrollingSpace(true);

        final ModuleWrapper[] modules = UR.get().getModules().stream()
            .filter(module -> filterCategory == null || module.getCategory() == filterCategory)
            .sorted((module1, module2) -> {
                if (module1.equals(module2)) return 0;

                if (module1.getCategory() != module2.getCategory())
                    return Integer.compare(module1.getCategory().ordinal(), module2.getCategory().ordinal());

                if (module1.getWhen() != module2.getWhen())
                    return Integer.compare(module1.getWhen().ordinal(), module2.getWhen().ordinal());

                if (module1.isInternal() != module2.isInternal()) return module1.isInternal() ? -1 : 1;

                return module1.getName().compareTo(module2.getName());
            })
            .toArray(ModuleWrapper[]::new);

        setData(modules);

        // Sets the title
        if (filterCategory == null)
        {
            setTitle(I.t("{black}All modules {darkgray}({0})", UR.get().getModules().size()));
        }
        else
        {
            setTitle(ChatColor.BLACK + filterCategory.getDisplayName() + ChatColor.DARK_GRAY + String.format(" (%d / %d)", modules.length, UR.get().getModules().size()));
        }

        // Displays the bottom bar of color
        final ItemStackBuilder bottomColorStripe = new ItemStackBuilder(Material.STAINED_GLASS_PANE)
                .data(ColorsUtils.chat2Dye(filterCategory != null ? filterCategory.getColor() : ChatColor.WHITE).getWoolData())  // FIXME 1.13
                .title(filterCategory != null ? filterCategory.getColor() + "" + ChatColor.BOLD + filterCategory.getDisplayName() : "");
        if (filterCategory != null) bottomColorStripe.longLore(ChatColor.GRAY, filterCategory.getDescription(), 38);

        for (int slot = getSize() - 9; slot < getSize(); slot++) action("", slot, bottomColorStripe);

        // Displays the category filter button
        final Material endCrystal = Material.getMaterial("END_CRYSTAL");
        final ItemStackBuilder catFilter = new ItemStackBuilder(filterCategory == null ? new ItemStack(endCrystal != null ? endCrystal : Material.HOPPER) : filterCategory.getIcon())
                .title(ChatColor.YELLOW, ChatColor.BOLD + I.t("Filter modules category"))
                .loreSeparator();

        final String prefixActive = ChatColor.YELLOW + "» ";
        final String prefixInactive = ChatColor.DARK_GRAY + "» " + ChatColor.GRAY;

        catFilter.loreLine((filterCategory == null ? prefixActive : prefixInactive) + I.t("All modules"));

        for (ModuleCategory category : ModuleCategory.values())
        {
            catFilter.loreLine((filterCategory == category ? prefixActive : prefixInactive) + category.getDisplayName());
        }

        catFilter.loreSeparator();
        catFilter.longLore(ChatColor.WHITE, filterCategory == null ? I.t("Click to filter the modules by category. A description will be displayed here.") : filterCategory.getDescription(), 38);

        action("switch_category", getSize() - 5, catFilter);
    }

    @Override
    protected ItemStack getViewItem(final ModuleWrapper module)
    {
        final ItemStackBuilder item = module.getFullIcon(false).loreSeparator();
        final String prefix = ChatColor.DARK_GRAY + "» ";

        if (!module.isEnabled())
        {
            if (module.canBeEnabled())
            {
                item.lore(prefix + I.t("{white}Click {gray}to enable this module"));
            }
            else
            {
                item.lore(prefix + I.t("{red}This module can no longer be enabled."));
            }
        }
        else
        {
            if (module.canBeDisabled())
            {
                item.lore(prefix + I.t("{white}Click {gray}to disable this module"));
            }
            else
            {
                item.lore(prefix + I.t("{red}This module cannot be disabled."));
            }
        }

        item.lore(prefix + I.t("{white}Right click {gray}to access details and config"));

        return item.item();
    }


    /**
     * On left click
     * @param module Clicked module
     */
    @Override
    protected ItemStack getPickedUpItem(final ModuleWrapper module)
    {
        if (module.isEnabled() && !module.canBeEnabled())
        {
            Gui.open(getPlayer(), new ConfirmModuleDisableGUI(module), this);
        }
        else
        {
            if (module.setEnabled(!module.isEnabled()))
            {
                if (module.isEnabled())
                {
                    UR.log().broadcastAdministrativePrefixed(I.t("{yellow}{0} {green}enabled {yellow}the module {1}.", getPlayer().getName(), module.getName()));
                }
                else
                {
                    UR.log().broadcastAdministrativePrefixed(I.t("{yellow}{0} {red}disabled {yellow}the module {1}.", getPlayer().getName(), module.getName()));
                }
            }

            update();
        }

        return null;
    }

    @Override
    protected void onRightClick(final ModuleWrapper module)
    {
        // TODO open details-gui
    }

    @GuiAction
    protected void switch_category(final InventoryClickEvent ev)
    {
        filterCategory = UHUtils.getNextElement(filterCategory, ev.getClick() == ClickType.RIGHT ? -1 : 1, true, ModuleCategory.class);
        update();
    }
}
