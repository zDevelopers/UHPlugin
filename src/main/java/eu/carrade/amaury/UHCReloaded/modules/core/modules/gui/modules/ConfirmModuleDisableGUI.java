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
package eu.carrade.amaury.UHCReloaded.modules.core.modules.gui.modules;

import eu.carrade.amaury.UHCReloaded.core.ModuleWrapper;
import fr.zcraft.zlib.components.gui.GuiAction;
import fr.zcraft.zlib.components.gui.GuiUtils;
import fr.zcraft.zlib.components.i18n.I;
import fr.zcraft.zlib.tools.items.ItemStackBuilder;
import fr.zcraft.zlib.tools.mojang.MojangHead;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Material;

import java.util.List;
import java.util.stream.Collectors;

public class ConfirmModuleDisableGUI extends FramedModuleGUI
{
    public ConfirmModuleDisableGUI(final ModuleWrapper module)
    {
        super(module);
    }

    @Override
    protected void onUpdate()
    {
        setTitle(I.t("{darkgray}{0} » {black}Disable", module.getName()));
        setSize(5 * 9);

        final List<String> title = GuiUtils.generateLore(I.t("If you disable this module, you won't be able to re-enable it."), 38);
        final String firstTitleLine = title.get(0);

        title.remove(0);

        action("", 22, MojangHead.QUESTION.asItemBuilder()
                .title(ChatColor.RED, ChatColor.BOLD + firstTitleLine)
                .lore(title.stream().map(line -> ChatColor.RED + "" + ChatColor.BOLD + line).collect(Collectors.toList()))
                .loreSeparator()
                .longLore(ChatColor.GRAY, I.t("This module cannot be re-loaded after its original load period. This means that if you disable this module, you won't be able to re-enable it during this game."), 38)
                .longLore(ChatColor.WHITE, I.t("Are you sure you want to disable {0}?", module.getName()), 38)
                .item());

        final ItemStackBuilder no = new ItemStackBuilder(Material.STAINED_GLASS_PANE)
                .data(DyeColor.RED.getWoolData())  // FIXME 1.13
                .title(ChatColor.GREEN, ChatColor.BOLD + I.t("I changed my mind"))
                .longLore(ChatColor.GRAY, I.t("Go back without disabling the module"));

        final ItemStackBuilder yes = new ItemStackBuilder(Material.STAINED_GLASS_PANE)
                .data(DyeColor.GREEN.getWoolData())  // FIXME 1.13
                .title(ChatColor.RED, ChatColor.BOLD + I.t("Yes, disable"))
                .longLore(ChatColor.GRAY, I.t("Disable {0} without any possibility of re-enabling it", module.getName()));

        for (int slot : new int[] {10, 11, 19, 20, 28, 29})
        {
            action("cancel", slot, no);
            action("disable", slot + 5, yes);
        }
    }

    @GuiAction
    protected void cancel()
    {
        close();
    }

    @GuiAction
    protected void disable()
    {
        module.setEnabled(false);
        close();
    }
}
