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

import eu.carrade.amaury.UHCReloaded.teams.UHTeam;
import eu.carrade.amaury.UHCReloaded.utils.ColorsUtils;
import eu.carrade.amaury.UHCReloaded.utils.TextUtils;
import fr.zcraft.zlib.components.gui.ActionGui;
import fr.zcraft.zlib.components.gui.Gui;
import fr.zcraft.zlib.components.gui.GuiAction;
import fr.zcraft.zlib.components.gui.GuiUtils;
import fr.zcraft.zlib.components.gui.PromptGui;
import fr.zcraft.zlib.components.i18n.I;
import fr.zcraft.zlib.tools.Callback;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class TeamEditGUI extends ActionGui
{
    private final UHTeam team;

    public TeamEditGUI(UHTeam team)
    {
        this.team = team;
    }


    @Override
    protected void onUpdate()
    {
        setTitle(I.t("team.chestGui.editor.title", team.getDisplayName()));
        setSize(36);

        // Banner
        ItemStack banner = team.getBanner();
        GuiUtils.makeItem(banner, ChatColor.RESET + team.getDisplayName(), Collections.singletonList(I.t("team.chestGui.editor.banner.members", team.getSize())));
        GuiUtils.hideItemAttributes(banner);
        action("", 9, banner);

        // Color
        action("color", 11, GuiUtils.makeItem(
                new ItemStack(Material.WOOL, 1, ColorsUtils.chat2Dye(team.getColor().toChatColor()).getWoolData()),
                I.t("team.chestGui.editor.color.title"),
                Collections.singletonList(I.t("team.chestGui.editor.color.current", team.getColor().toChatColor() + TextUtils.friendlyEnumName(team.getColor())))
        ));

        // Name
        action("name", 13, GuiUtils.makeItem(
                Material.BOOK_AND_QUILL,
                I.t("team.chestGui.editor.name.title"),
                Collections.singletonList(I.t("team.chestGui.editor.name.current", team.getName()))
        ));

        // Members
        List<String> lore = new ArrayList<>();
        for (OfflinePlayer player : team.getPlayers())
            if (player.isOnline())
                lore.add(I.t("team.list.bulletPlayerOnline") + ChatColor.RESET + player.getName());
            else
                lore.add(I.t("team.list.bulletPlayerOffline") + ChatColor.RESET + player.getName());

        action("members", 15, GuiUtils.makeItem(
                new ItemStack(Material.SKULL_ITEM, 1, (short) 3),
                I.t("team.chestGui.editor.members.title"),
                lore
        ));

        // Delete
        action("delete", 17, GuiUtils.makeItem(
                Material.BARRIER,
                I.t("team.chestGui.editor.delete.title"),
                Collections.singletonList(I.t("team.chestGui.editor.delete.warning"))
        ));

        // Exit
        action("exit", getSize() - 5, GuiUtils.makeItem(
                Material.EMERALD,
                I.t("team.chestGui.editor.exit.title")
        ));
    }


    @GuiAction ("color")
    protected void color()
    {
        Gui.open(getPlayer(), new TeamEditColorGUI(team), this);
    }

    @GuiAction ("name")
    protected void name()
    {
        Gui.open(getPlayer(), new PromptGui(new Callback<String>() {
            @Override
            public void call(String name)
            {
                if (!name.trim().isEmpty())
                    team.setName(name);
            }
        }, team.getName()), this);
    }

    @GuiAction ("members")
    protected void members()
    {
        Gui.open(getPlayer(), new TeamEditMembersGUI(team), this);
    }

    @GuiAction ("delete")
    protected void delete()
    {
        Gui.open(getPlayer(), new TeamEditDeleteGUI(team), this);
    }

    @GuiAction ("exit")
    protected void exit()
    {
        close();
    }
}
