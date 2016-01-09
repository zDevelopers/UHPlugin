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

package eu.carrade.amaury.UHCReloaded.listeners;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.EnumWrappers;
import eu.carrade.amaury.UHCReloaded.UHCReloaded;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

import java.lang.reflect.InvocationTargetException;


public class PacketsListener extends PacketAdapter implements Listener
{

    private UHCReloaded p = null;
    private ProtocolManager pm = ProtocolLibrary.getProtocolManager();

    private final PacketContainer respawnPacket;

    public PacketsListener(UHCReloaded p)
    {
        // This listener needs to listen on login packets only.
        super(p, ListenerPriority.NORMAL, PacketType.Play.Server.LOGIN);

        this.p = p;

        // The packet to send to automatically respawn the player.
        respawnPacket = pm.createPacket(PacketType.Play.Client.CLIENT_COMMAND);
        respawnPacket.getClientCommands().write(0, EnumWrappers.ClientCommand.PERFORM_RESPAWN);
    }

    /**
     * Used to present the server as an hardcore server, for the clients to display hardcore hearts.
     *
     * @param ev
     */
    @Override
    public void onPacketSending(PacketEvent ev)
    {
        // If its a login packet, write the hardcore flag (first boolean) to true.
        if (ev.getPacketType().equals(PacketType.Play.Server.LOGIN))
        {
            ev.getPacket().getBooleans().write(0, true);
        }
    }

    /**
     * Used to automatically respawn the dead players.
     *
     * @param ev
     */
    @EventHandler
    public void onPlayerDeath(final PlayerDeathEvent ev)
    {
        if (p.getConfig().getBoolean("auto-respawn.do"))
        {
            Bukkit.getScheduler().runTaskLater(p, new Runnable()
            {
                @Override
                public void run()
                {
                    try
                    {
                        pm.recieveClientPacket(ev.getEntity(), respawnPacket);
                    }
                    catch (IllegalAccessException | InvocationTargetException e)
                    {
                        e.printStackTrace();
                    }
                }
            }, p.getConfig().getInt("auto-respawn.delay", 6) * 20L);
        }
    }
}
