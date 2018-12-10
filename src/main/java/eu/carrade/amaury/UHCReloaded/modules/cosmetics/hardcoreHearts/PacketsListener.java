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

package eu.carrade.amaury.UHCReloaded.modules.cosmetics.hardcoreHearts;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.EnumWrappers;
import eu.carrade.amaury.UHCReloaded.UHConfig;
import eu.carrade.amaury.UHCReloaded.shortcuts.UR;
import fr.zcraft.zlib.tools.runners.RunTask;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

import java.lang.reflect.InvocationTargetException;


public class PacketsListener extends PacketAdapter implements Listener
{
    private final ProtocolManager pm = ProtocolLibrary.getProtocolManager();
    private final PacketContainer respawnPacket;

    public PacketsListener()
    {
        // This listener needs to listen on login packets only.
        super(UR.get(), ListenerPriority.NORMAL, PacketType.Play.Server.LOGIN);

        // The packet to send to automatically respawn the player.
        respawnPacket = pm.createPacket(PacketType.Play.Client.CLIENT_COMMAND);
        respawnPacket.getClientCommands().write(0, EnumWrappers.ClientCommand.PERFORM_RESPAWN);
    }

    /**
     * Used to present the server as an hardcore server, for the clients to display hardcore hearts.
     */
    @Override
    public void onPacketSending(final PacketEvent ev)
    {
        // If its a login packet, write the hardcore flag (first boolean) to true.
        if (ev.getPacketType().equals(PacketType.Play.Server.LOGIN))
        {
            ev.getPacket().getBooleans().write(0, true);
        }
    }

    /**
     * Used to automatically respawn the dead players.
     */
    @EventHandler
    public void onPlayerDeath(final PlayerDeathEvent ev)
    {
        if (Config.AUTO_RESPAWN.DO.get())
        {
            RunTask.later(() ->
            {
                try
                {
                    pm.recieveClientPacket(ev.getEntity(), respawnPacket);
                }
                catch (final IllegalAccessException | InvocationTargetException e)
                {
                    UR.log(HardcoreHeartsModule.class).error("Unable to respawn player {0}", e, ev.getEntity().getName());
                }
            }, Config.AUTO_RESPAWN.DELAY.get() * 20L);
        }
    }
}
