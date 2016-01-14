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
package eu.carrade.amaury.UHCReloaded.gui.teams;

import eu.carrade.amaury.UHCReloaded.UHCReloaded;
import eu.carrade.amaury.UHCReloaded.gui.teams.builder.TeamBuilderStepColorGUI;
import eu.carrade.amaury.UHCReloaded.i18n.I18n;
import eu.carrade.amaury.UHCReloaded.teams.TeamManager;
import eu.carrade.amaury.UHCReloaded.teams.UHTeam;
import eu.carrade.amaury.UHCReloaded.utils.ColorsUtils;
import fr.zcraft.zlib.components.gui.ExplorerGui;
import fr.zcraft.zlib.components.gui.Gui;
import fr.zcraft.zlib.components.gui.GuiAction;
import fr.zcraft.zlib.components.gui.GuiUtils;
import fr.zcraft.zlib.components.gui.PromptGui;
import fr.zcraft.zlib.tools.Callback;
import fr.zcraft.zlib.tools.items.GlowEffect;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class TeamsSelectorGUI extends ExplorerGui<UHTeam>
{
    private final String IB = "team.chestGui.selector.";  // I18n base key

    private final String TEAM_ITEM_TYPE;
    private final boolean GLOW_ON_CURRENT_TEAM;

    private final I18n i = UHCReloaded.i();
    private final TeamManager tm = UHCReloaded.get().getTeamManager();

    public TeamsSelectorGUI()
    {
        final FileConfiguration config = UHCReloaded.get().getConfig();

        TEAM_ITEM_TYPE       = config.getString("teams-options.chestGui.display.teamItem").toLowerCase();
        GLOW_ON_CURRENT_TEAM = config.getBoolean("teams-options.chestGui.display.glowOnSelectedTeam");
    }

    @Override
    protected void onUpdate()
    {
        setTitle(i.t(IB + "title", tm.getTeams().size()));
        setData(tm.getTeams().toArray(new UHTeam[tm.getTeams().size()]));

        setMode(Mode.READONLY);
        setKeepHorizontalScrollingSpace(true);

        if (getPlayer().hasPermission("uh.player.renameTeam"))
        {
            int renameSlot = getPlayer().hasPermission("uh.team") ? getSize() - 6 : getSize() - 5;

            action("rename", renameSlot, GuiUtils.makeItem(
                    Material.BOOK_AND_QUILL, i.t(IB + "rename.title"),
                    tm.getTeamForPlayer(getPlayer()) == null ? Collections.singletonList(i.t(IB + "rename.selectBefore")) : null
            ));
        }

        if (getPlayer().hasPermission("uh.team"))
        {
            int newTeamSlot = getPlayer().hasPermission("uh.player.renameTeam") ? getSize() - 4 : getSize() - 5;

            action("new", newTeamSlot, GuiUtils.makeItem(Material.EMERALD, i.t(IB + "new.title")));
        }
    }

    @Override
    protected ItemStack getViewItem(UHTeam team)
    {
        final boolean playerInTeam = team.getPlayersUUID().contains(getPlayer().getUniqueId());


        // Lore
        final List<String> lore = new ArrayList<>();

        lore.add("");

        if (team.getSize() != 0)
        {
            lore.add(i.t(IB + "teamItem.subtitlePlayers"));
            for (OfflinePlayer player : team.getPlayers())
            {
                lore.add(i.t(IB + "teamItem.bulletPlayers", player.getName()));
            }

            lore.add("");
        }

        if (getPlayer().hasPermission("uh.player.join.self") && !playerInTeam)
        {
            lore.add(i.t(IB + "teamItem.inviteJoin"));
        }
        else if (getPlayer().hasPermission("uh.player.leave.self") && playerInTeam)
        {
            lore.add(i.t(IB + "teamItem.inviteLeave"));
        }

        if (getPlayer().hasPermission("uh.team"))  // TODO adapt with new granular permissions
        {
            lore.add(i.t(IB + "teamItem.inviteManage"));
        }


        // Item
        final ItemStack item;
        final DyeColor dye = ColorsUtils.chat2Dye(team.getColor().toChatColor());

        switch (TEAM_ITEM_TYPE)
        {
            case "banner":
                item = team.getBanner();
                break;

            case "clay":
                item = new ItemStack(Material.STAINED_CLAY, 1, dye.getWoolData());
                break;

            case "glass":
                item = new ItemStack(Material.STAINED_GLASS_PANE, 1, dye.getWoolData());
                break;

            case "glass_pane":
                item = new ItemStack(Material.STAINED_GLASS_PANE, 1, dye.getWoolData());
                break;

            case "dye":
                item = new ItemStack(Material.INK_SACK, 1, dye.getDyeData());
                break;

            default:
                item = new ItemStack(Material.WOOL, 1, dye.getWoolData());
        }


        // Title
        final String title = tm.getMaxPlayersPerTeam() != 0 ? i.t(IB + "teamItem.titleWithMax", team.getDisplayName(), team.getSize(), tm.getMaxPlayersPerTeam()) : i.t(IB + "teamItem.title", team.getDisplayName(), team.getSize());


        GuiUtils.makeItem(item, title, lore);
        GuiUtils.hideItemAttributes(item);

        if (GLOW_ON_CURRENT_TEAM && playerInTeam)
            GlowEffect.addGlow(item);

        return item;
    }

    @Override
    protected ItemStack getPickedUpItem(UHTeam team)
    {
        final boolean playerInTeam = team.getPlayersUUID().contains(getPlayer().getUniqueId());

        if (getPlayer().hasPermission("uh.player.join.self") && !playerInTeam)
        {
            team.addPlayer(getPlayer());
        }
        else if (getPlayer().hasPermission("uh.player.leave.self") && playerInTeam)
        {
            team.removePlayer(getPlayer());
        }

        update();
        return null;
    }

    @Override
    protected void onRightClick(UHTeam team)
    {
        if (getPlayer().hasPermission("uh.team"))  // TODO adapt with new granular permissions
        {
            // TODO open management GUI
        }
        else
        {
            getPickedUpItem(team);
        }
    }

    @GuiAction ("rename")
    public void rename()
    {
        final UHTeam team = tm.getTeamForPlayer(getPlayer());
        if (team == null)
            return;

        Gui.open(getPlayer(), new PromptGui(new Callback<String>() {
            @Override
            public void call(String name)
            {
                team.setName(name);
            }
        }, team.getName()), this);
    }

    @GuiAction ("new")
    public void newTeam()
    {
        Gui.open(getPlayer(), new TeamBuilderStepColorGUI());
    }
}
