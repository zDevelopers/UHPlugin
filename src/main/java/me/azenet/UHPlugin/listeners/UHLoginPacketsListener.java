/**
 *  Plugin UltraHardcore (UHPlugin)
 *  Copyright (C) 2013 azenet
 *  Copyright (C) 2014 Amaury Carrade
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see [http://www.gnu.org/licenses/].
 */

package me.azenet.UHPlugin.listeners;

import me.azenet.UHPlugin.UHPlugin;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;


public class UHLoginPacketsListener extends PacketAdapter {
	
	public UHLoginPacketsListener(UHPlugin p) {
		super(p, ListenerPriority.NORMAL, PacketType.Play.Server.LOGIN);
	}
	
	 @Override
	 public void onPacketSending(PacketEvent event) {
		 // If its a login packet, write the hardcore flag (first boolean) to true.
		 if (event.getPacketType().equals(PacketType.Play.Server.LOGIN)) {
			 event.getPacket().getBooleans().write(0, true);
		 }
	 }
}
