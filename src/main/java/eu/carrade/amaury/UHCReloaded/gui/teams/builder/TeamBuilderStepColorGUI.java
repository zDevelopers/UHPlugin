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
import eu.carrade.amaury.UHCReloaded.utils.ColorsUtils;
import eu.carrade.amaury.UHCReloaded.utils.TextUtils;
import fr.zcraft.zlib.components.gui.Gui;
import fr.zcraft.zlib.components.gui.GuiAction;
import fr.zcraft.zlib.components.gui.GuiUtils;
import fr.zcraft.zlib.tools.runners.RunTask;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

import java.util.Random;


public class TeamBuilderStepColorGUI extends TeamBuilderBaseGUI
{
    private final Random randomSource = new Random();

    private BukkitTask randomUpdate = null;


    @Override
    protected void onUpdate()
    {
        setTitle(UHCReloaded.i().t("team.chestGui.creator.color.title"));
        setSize(6 * 9);

        generateBreadcrumbs(BuildingStep.COLOR);


        action("random", 22, GuiUtils.makeItem(Material.WOOL, UHCReloaded.i().t("team.chestGui.creator.color.random")));

        insertColor(28, ChatColor.WHITE);
        insertColor(29, ChatColor.AQUA);
        insertColor(30, ChatColor.BLUE);
        insertColor(31, ChatColor.GREEN);
        insertColor(32, ChatColor.YELLOW);
        insertColor(33, ChatColor.GOLD);
        insertColor(34, ChatColor.LIGHT_PURPLE);

        insertColor(37, ChatColor.RED);
        insertColor(38, ChatColor.DARK_RED);
        insertColor(39, ChatColor.DARK_GREEN);
        insertColor(40, ChatColor.DARK_PURPLE);
        insertColor(41, ChatColor.DARK_BLUE);
        insertColor(42, ChatColor.DARK_AQUA);
        insertColor(43, ChatColor.BLACK);

        insertColor(47, ChatColor.GRAY);
        insertColor(51, ChatColor.DARK_GRAY);


        randomUpdate = RunTask.timer(new Runnable() {
            @Override
            public void run()
            {
                ItemStack random = getInventory().getItem(22);
                if (random != null)
                    random.setDurability((short) randomSource.nextInt(16));
            }
        }, 15l, 15l);
    }

    private void insertColor(int slot, ChatColor color)
    {
        action("", slot, GuiUtils.makeItem(new ItemStack(Material.WOOL, 1, ColorsUtils.chat2Dye(color).getWoolData()), color + TextUtils.friendlyEnumName(color), null));
    }


    @GuiAction ("random")
    protected void random()
    {
        next(TeamColor.RANDOM);
    }

    @Override
    protected void unknown_action(String name, int slot, ItemStack item)
    {
        if (item.hasItemMeta() && item.getItemMeta().hasDisplayName())
            next(TeamColor.fromChatColor(ChatColor.getByChar(ChatColor.getLastColors(item.getItemMeta().getDisplayName()).substring(1))));
    }

    private void next(TeamColor color)
    {
        Gui.open(getPlayer(), new TeamBuilderStepNameGUI(color));
    }

    @Override
    protected void onClose()
    {
        if (randomUpdate != null)
        {
            randomUpdate.cancel();
            randomUpdate = null;
        }
    }

    @Override
    protected TeamColor getColor() { return null; }

    @Override
    protected String getName() { return null; }
}
