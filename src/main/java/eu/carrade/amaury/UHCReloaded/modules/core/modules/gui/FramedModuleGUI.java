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

import eu.carrade.amaury.UHCReloaded.core.ModuleWrapper;
import fr.zcraft.zlib.components.gui.ActionGui;
import fr.zcraft.zlib.tools.items.ItemStackBuilder;
import fr.zcraft.zteams.colors.ColorsUtils;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public abstract class FramedModuleGUI extends ActionGui
{
    protected final ModuleWrapper module;

    public FramedModuleGUI(ModuleWrapper module)
    {
        this.module = module;
    }

    @Override
    protected void onAfterUpdate()
    {
        final ItemStack framePart = new ItemStackBuilder(Material.STAINED_GLASS_PANE)
                .title("")
                .data(ColorsUtils.chat2Dye(module.getCategory().getColor()).getWoolData())  // FIXME 1.13
                .item();

        // Top and bottom
        for (int slot = 0; slot < 9; slot++)
        {
            action("", slot, framePart);
            action("", getSize() - (slot + 1), framePart);
        }

        // Sides
        for (int line = 0; line < getSize() / 9; line++)
        {
            action("", line * 9, framePart);
            action("", line * 9 + 8, framePart);
        }

        // Icon
        action("__module_icon__", 4, module.getFullIcon(false));
    }
}
