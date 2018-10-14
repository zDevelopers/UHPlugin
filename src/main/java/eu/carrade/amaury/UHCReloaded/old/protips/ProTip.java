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
package eu.carrade.amaury.UHCReloaded.old.protips;

import eu.carrade.amaury.UHCReloaded.UHCReloaded;
import eu.carrade.amaury.UHCReloaded.UHConfig;
import eu.carrade.amaury.UHCReloaded.utils.UHSound;
import fr.zcraft.zlib.components.i18n.I;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArraySet;


/**
 * A ProTip, sent to a player only once.
 */
public class ProTip
{
    private static final UHSound proTipsSound = new UHSound(UHConfig.PROTIPS.SOUND);

    private String text;
    private Boolean enabled;

    private Set<UUID> sentTo = new CopyOnWriteArraySet<>();


    /**
     * @param enabled {@code false} to avoid this ProTip from being sent when {@link #sendTo(Player)} is called.
     * @param text The ProTip text.
     */
    public ProTip(Boolean enabled, String text)
    {
        this.text = text;
        this.enabled = enabled;
    }

    /**
     * @param name A name registered in the UHCReloaded config file, used to disable the powerup if needed, following the configuration.
     * @param text The ProTip text.
     */
    public ProTip(String name, String text)
    {
        this(UHCReloaded.get().getConfig().getBoolean("protips." + name), text);
    }


    public void setEnabled(Boolean enabled)
    {
        this.enabled = enabled;
    }

    public String getText()
    {
        return text;
    }

    public Boolean isEnabled()
    {
        return enabled;
    }


    /**
     * Checks if this ProTip was sent to this player.
     *
     * @param id The player's UUID.
     * @return {@code true} if already sent.
     */
    public Boolean wasSentTo(UUID id)
    {
        return sentTo.contains(id);
    }

    /**
     * Checks if this ProTip was sent to this player.
     *
     * @param player The player.
     * @return {@code true} if already sent.
     */
    public Boolean wasSentTo(Player player)
    {
        return wasSentTo(player.getUniqueId());
    }


    /**
     * Sends a ProTip, if this ProTip wasn't sent before to this player.
     *
     * @param player The receiver of this ProTip.
     */
    public void sendTo(Player player)
    {
        if (!isEnabled() || wasSentTo(player))
            return;

        sentTo.add(player.getUniqueId());

        /// ProTip invite, displayed before a ProTip.
        player.sendMessage(I.t("{darkpurple}ProTip!") + " " + ChatColor.RESET + text);
        proTipsSound.play(player);
    }

    /**
     * Sends a ProTip, if this ProTip wasn't sent before to this player and this player is online.
     *
     * @param id The receiver of this ProTip.
     */
    public void sendTo(UUID id)
    {
        Player player = Bukkit.getPlayer(id);
        if (player != null && player.isOnline())
            sendTo(player);
    }
}
