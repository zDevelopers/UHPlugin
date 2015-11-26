/**
 * Plugin UltraHardcore Reloaded (UHPlugin) Copyright (C) 2013 azenet Copyright (C) 2014-2015 Amaury
 * Carrade
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program.  If
 * not, see [http://www.gnu.org/licenses/].
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
import org.bukkit.scheduler.BukkitRunnable;

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
            Bukkit.getScheduler().runTaskLater(p, new BukkitRunnable()
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
