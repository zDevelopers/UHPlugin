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
import eu.carrade.amaury.UHCReloaded.gui.teams.TeamsSelectorGUI;
import eu.carrade.amaury.UHCReloaded.teams.TeamColor;
import eu.carrade.amaury.UHCReloaded.teams.UHTeam;
import eu.carrade.amaury.UHCReloaded.utils.OfflinePlayersComparator;
import eu.carrade.amaury.UHCReloaded.utils.TextUtils;
import fr.zcraft.zlib.components.gui.Gui;
import fr.zcraft.zlib.components.gui.GuiAction;
import fr.zcraft.zlib.components.gui.GuiUtils;
import fr.zcraft.zlib.components.i18n.I;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;


public class TeamBuilderStepPlayersGUI extends TeamBuilderBaseGUI
{
    private final TeamColor color;
    private final String name;

    private final Set<UUID> teamMembers = new HashSet<>();


    public TeamBuilderStepPlayersGUI(TeamColor color, String name)
    {
        this.color = color;
        this.name = name;
    }

    @Override
    protected void onUpdate()
    {
        setTitle(I.t("team.chestGui.creator.players.title"));
        setSize(6 * 9);

        generateBreadcrumbs(BuildingStep.PLAYERS);


        // Players

        Set<OfflinePlayer> players = new TreeSet<>(new OfflinePlayersComparator());
        Collections.addAll(players, Bukkit.getOfflinePlayers());

        int slot = 9;
        for (OfflinePlayer player : players)
        {
            action(player.getUniqueId().toString(), slot, generatePlayerButton(player));

            if (slot < 44) slot++;
            else break;
        }


        // Done button

        List<String> lore = new ArrayList<>();
        lore.add("");
        lore.add(I.t("team.chestGui.creator.players.doneItem.summary"));
        lore.add(I.t("team.chestGui.creator.players.doneItem.name", getName()));
        lore.add(I.t("team.chestGui.creator.players.doneItem.color", getColor() == TeamColor.RANDOM ? ChatColor.MAGIC + "Random" : getColor().toChatColor() + TextUtils.friendlyEnumName(getColor())));
        lore.add(I.t("team.chestGui.creator.players.doneItem.members", teamMembers.size()));
        lore.add("");
        for (UUID teamMember : teamMembers)
        {
            OfflinePlayer player = Bukkit.getOfflinePlayer(teamMember);
            lore.add(I.t("team.chestGui.creator.players.doneItem.memberBullet", player != null ? player.getName() : teamMember));
        }

        action("done", getSize() - 5, GuiUtils.makeItem(Material.EMERALD, I.t("team.chestGui.creator.players.doneItem.title"), lore));
    }

    private ItemStack generatePlayerButton(OfflinePlayer player)
    {
        ItemStack button = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
        SkullMeta meta = (SkullMeta) button.getItemMeta();

        String displayName = player instanceof Player ? ((Player) player).getDisplayName() : player.getName();
        UHTeam team = UHCReloaded.get().getTeamManager().getTeamForPlayer(player);

        meta.setOwner(player.getName());
        meta.setDisplayName(I.t("team.chestGui.creator.players.playerItem.title", displayName));
        meta.setLore(Arrays.asList(
                player.isOnline() ? I.t("team.chestGui.creator.players.playerItem.online") : I.t("team.chestGui.creator.players.playerItem.offline"),
                team != null ? I.t("team.chestGui.creator.players.playerItem.currentTeam", team.getDisplayName()) : I.t("team.chestGui.creator.players.playerItem.noCurrentTeam"),
                "",
                teamMembers.contains(player.getUniqueId()) ? I.t("team.chestGui.creator.players.playerItem.selected") : I.t("team.chestGui.creator.players.playerItem.select")
        ));

        button.setItemMeta(meta);
        return button;
    }


    @Override
    protected void unknown_action(String name, int slot, ItemStack item)
    {
        UUID playerUUID;
        try { playerUUID = UUID.fromString(name); } catch(IllegalArgumentException e) { return; }

        if (teamMembers.contains(playerUUID))
            teamMembers.remove(playerUUID);
        else
            teamMembers.add(playerUUID);

        update();
    }

    @GuiAction ("done")
    protected void done()
    {
        UHTeam team = new UHTeam(getName(), getColor());

        for (UUID member : teamMembers)
            team.addPlayer(Bukkit.getOfflinePlayer(member));

        try
        {
            UHCReloaded.get().getTeamManager().addTeam(team);
            getPlayer().sendMessage(I.t("team.chestGui.creator.players.done"));
        }
        catch (IllegalArgumentException e)
        {
            getPlayer().sendMessage(I.t("team.add.errorExists"));
        }

        Gui.open(getPlayer(), new TeamsSelectorGUI());
    }


    @Override
    protected TeamColor getColor() { return color; }

    @Override
    protected String getName() { return name; }

}
