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
package eu.carrade.amaury.UHCReloaded.gui.teams.builder;

import eu.carrade.amaury.UHCReloaded.UHCReloaded;
import eu.carrade.amaury.UHCReloaded.teams.TeamColor;
import eu.carrade.amaury.UHCReloaded.utils.TextUtils;
import fr.zcraft.zlib.components.gui.ActionGui;
import fr.zcraft.zlib.components.gui.GuiUtils;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.Collections;


/**
 * The base of the team creator GUIs: contains the breadcrumb generator.
 */
public abstract class TeamBuilderBaseGUI extends ActionGui
{
    protected void generateBreadcrumbs(BuildingStep step)
    {
        for (int i = 0; i < 9; i++)
        {
            if (i != 1 && i != 4 && i != 7)
            {
                action("", i, GuiUtils.makeItem(new ItemStack(Material.STAINED_GLASS_PANE, 1, DyeColor.WHITE.getWoolData()), ChatColor.RESET + " ", null));
            }
            else
            {
                final BuildingStep slotStep;
                final String achievedSubtitle;

                if (i == 1)
                {
                    slotStep = BuildingStep.COLOR;
                    achievedSubtitle = getColor() != null ? (getColor() == TeamColor.RANDOM ? ChatColor.WHITE + "" + ChatColor.MAGIC + "Random color" : getColor().toChatColor() + TextUtils.friendlyEnumName(getColor())) : "";
                }
                else if (i == 4)
                {
                    slotStep = BuildingStep.NAME;
                    achievedSubtitle = getName() != null ? ChatColor.GRAY + getName() : "";
                }
                else
                {
                    slotStep = BuildingStep.PLAYERS;
                    achievedSubtitle = ""; // Never displayed as achieved: it's the last one.
                }

                final boolean achieved = slotStep.isAchieved(step);

                action("", i, GuiUtils.makeItem(
                        new ItemStack(Material.STAINED_GLASS_PANE, 1, achieved ? DyeColor.LIME.getWoolData() : DyeColor.RED.getWoolData()),
                        slotStep.getName(),
                        achieved ? Collections.singletonList(achievedSubtitle) : null
                ));
            }
        }
    }

    /**
     * @return the selected color. {@code null} if not selected yet.
     */
    protected abstract TeamColor getColor();

    /**
     * @return the chosen name. {@code null} if not chosen yet.
     */
    protected abstract String getName();


    /**
     * The current step used by {@link #generateBreadcrumbs(BuildingStep)}
     */
    protected enum BuildingStep
    {
        COLOR (UHCReloaded.i().t("team.chestGui.creator.color.breadcrumbs")),
        NAME (UHCReloaded.i().t("team.chestGui.creator.name.breadcrumbs")),
        PLAYERS (UHCReloaded.i().t("team.chestGui.creator.players.breadcrumbs"));


        private final String name;

        BuildingStep(String name)
        {
            this.name = name;
        }

        public String getName()
        {
            return name;
        }

        /**
         * Checks if this step is achieved, at the given step.
         *
         * @param step The current step.
         * @return {@code true} if achieved.
         */
        public boolean isAchieved(BuildingStep step)
        {
            return step.ordinal() > this.ordinal();
        }
    }
}
