/*
 * Copyright or © or Copr. Amaury Carrade (2014 - 2016)
 *
 * http://amaury.carrade.eu
 *
 * This software is governed by the CeCILL-B license under French law and
 * abiding by the rules of distribution of free software.  You can  use,
 * modify and/ or redistribute the software under the terms of the CeCILL-B
 * license as circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info".
 *
 * As a counterpart to the access to the source code and  rights to copy,
 * modify and redistribute granted by the license, users are provided only
 * with a limited warranty  and the software's author,  the holder of the
 * economic rights,  and the successive licensors  have only  limited
 * liability.
 *
 * In this respect, the user's attention is drawn to the risks associated
 * with loading,  using,  modifying and/or developing or reproducing the
 * software by the user in light of its specific status of free software,
 * that may mean  that it is complicated to manipulate,  and  that  also
 * therefore means  that it is reserved for developers  and  experienced
 * professionals having in-depth computer knowledge. Users are therefore
 * encouraged to load and test the software's suitability as regards their
 * requirements in conditions enabling the security of their systems and/or
 * data to be ensured and,  more generally, to use and operate it in the
 * same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL-B license and that you accept its terms.
 */
package eu.carrade.amaury.UHCReloaded.gui.teams.editor;

import eu.carrade.amaury.UHCReloaded.UHCReloaded;
import eu.carrade.amaury.UHCReloaded.gui.teams.TeamsSelectorGUI;
import eu.carrade.amaury.UHCReloaded.teams.UHTeam;
import fr.zcraft.zlib.components.gui.Gui;
import fr.zcraft.zlib.components.gui.GuiAction;
import fr.zcraft.zlib.components.gui.GuiUtils;
import fr.zcraft.zlib.components.i18n.I;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;


public class TeamEditDeleteGUI extends TeamActionGUI
{
    public TeamEditDeleteGUI(UHTeam team)
    {
        super(team);
    }


    @Override
    protected void onUpdate()
    {
        /// The title of the delete team GUI. {0} = team name (raw).
        setTitle(I.t("{0} » {darkred}Delete", team.getName()));
        setSize(9);

        if (!exists())
        {
            action("", 4, getDeletedItem());
            return;
        }

        for (int slot = 0; slot < 3; slot++)
        {
            action("keep", slot, GuiUtils.makeItem(
                    new ItemStack(Material.STAINED_GLASS_PANE, 1, DyeColor.LIME.getWoolData()),
                    /// The title of the "keep" button in the delete team GUI
                    I.t("{green}Keep this team alive"),
                    null
            ));
        }

        action("", 4, team.getBanner());

        for (int slot = 6; slot < 9; slot++)
        {
            action("delete", slot, GuiUtils.makeItem(
                    new ItemStack(Material.STAINED_GLASS_PANE, 1, DyeColor.RED.getWoolData()),
                    /// The title of the "delete" button in the delete team GUI
                    I.t("{red}Delete this team {italic}forever"),
                    null
            ));
        }
    }


    @GuiAction ("keep")
    protected void keep()
    {
        close();
    }

    @GuiAction ("delete")
    protected void delete()
    {
        UHCReloaded.get().getTeamManager().removeTeam(team);
        Gui.open(getPlayer(), new TeamsSelectorGUI());
    }
}
