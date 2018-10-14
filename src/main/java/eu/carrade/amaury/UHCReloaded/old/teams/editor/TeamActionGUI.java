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
package eu.carrade.amaury.UHCReloaded.old.teams.editor;

import eu.carrade.amaury.UHCReloaded.UHCReloaded;
import eu.carrade.amaury.UHCReloaded.old.teams.UHTeam;
import fr.zcraft.zlib.components.gui.ActionGui;
import fr.zcraft.zlib.components.gui.GuiUtils;
import fr.zcraft.zlib.components.i18n.I;
import fr.zcraft.zlib.tools.items.ItemStackBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;


public abstract class TeamActionGUI extends ActionGui
{
    protected final UHTeam team;

    public TeamActionGUI(UHTeam team)
    {
        this.team = team;
    }

    /**
     * Checks if the team still exists.
     * @return {@code true} if the team exists.
     */
    protected boolean exists()
    {
        return UHCReloaded.get().getTeamManager().isTeamRegistered(team);
    }

    /**
     * Generates the item to display if the team was deleted while a player edited the team on a GUI.
     * @return the item.
     */
    protected ItemStack getDeletedItem()
    {
        return new ItemStackBuilder(Material.BARRIER)
                /// Title of the item displayed if a team was deleted while someone edited it in a GUI.
                .title(I.t("{red}Team deleted"))
                /// Lore of the item displayed if a team was deleted while someone edited it in a GUI.
                .lore(GuiUtils.generateLore(I.t("{gray}The team {0}{gray} was deleted by another player.", team.getDisplayName())))
                .lore("")
                /// Lore of the item displayed if a team was deleted while someone edited it in a GUI.
                .lore(GuiUtils.generateLore(I.t("{gray}Press {white}Escape{gray} to go back to the teams list.")))
                .hideAttributes()
                .item();
    }
}
