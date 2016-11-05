/*
 * Unpublished Copyright (c) 2016 Andrew Yunt, All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of Andrew Yunt. The intellectual and technical concepts contained
 * herein are proprietary to Andrew Yunt and may be covered by U.S. and Foreign Patents, patents in process, and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material is strictly forbidden unless prior written permission is obtained
 * from Andrew Yunt. Access to the source code contained herein is hereby forbidden to anyone except current Andrew Yunt and those who have executed
 * Confidentiality and Non-disclosure agreements explicitly covering such access.
 *
 * The copyright notice above does not evidence any actual or intended publication or disclosure of this source code, which includes
 * information that is confidential and/or proprietary, and is a trade secret, of COMPANY. ANY REPRODUCTION, MODIFICATION, DISTRIBUTION, PUBLIC PERFORMANCE,
 * OR PUBLIC DISPLAY OF OR THROUGH USE OF THIS SOURCE CODE WITHOUT THE EXPRESS WRITTEN CONSENT OF ANDREW YUNT IS STRICTLY PROHIBITED, AND IN VIOLATION OF
 * APPLICABLE LAWS AND INTERNATIONAL TREATIES. THE RECEIPT OR POSSESSION OF THIS SOURCE CODE AND/OR RELATED INFORMATION DOES NOT CONVEY OR IMPLY ANY RIGHTS
 * TO REPRODUCE, DISCLOSE OR DISTRIBUTE ITS CONTENTS, OR TO MANUFACTURE, USE, OR SELL ANYTHING THAT IT MAY DESCRIBE, IN WHOLE OR IN PART.
 */
package com.andrewyunt.megatw_master.managers;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.bukkit.scheduler.BukkitScheduler;

import com.andrewyunt.megatw_master.MegaTWMaster;
import com.andrewyunt.megatw_master.exception.ServerException;
import com.andrewyunt.megatw_master.objects.GameServer;
import com.andrewyunt.megatw_master.objects.SignDisplay;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

public class ServerManager implements Listener, PluginMessageListener{
	
	private final Map<String, GameServer> servers = new HashMap<String, GameServer>();
	
	public ServerManager() {
		
		BukkitScheduler scheduler = MegaTWMaster.getInstance().getServer().getScheduler();
		scheduler.scheduleSyncRepeatingTask(MegaTWMaster.getInstance(), new Runnable() {
			@Override
			public void run() {
				
				for (Map.Entry<String, GameServer> entry : servers.entrySet()) {
					Player[] onlinePlayers = MegaTWMaster.getInstance().getServer().getOnlinePlayers();
					
					if (onlinePlayers.length == 0)
						return;
					
					ByteArrayDataOutput out = ByteStreams.newDataOutput();
					
					out.writeUTF("PlayerCount");
					out.writeUTF(entry.getKey());
					
					onlinePlayers[0].sendPluginMessage((Plugin) MegaTWMaster.getInstance(), "BungeeCord", out.toByteArray());
				}
			}
		}, 0L, 20L);
	}
	
	public Collection<GameServer> getServers() {
		
		return servers.values();
	}
	
	public GameServer getServer(String name) throws ServerException {
		
		if (!servers.containsKey(name))
			throw new ServerException("The specified server does not exist.");
		
		return servers.get(name);
	}
	
	public boolean serverExists(String name) {
		
		return servers.containsKey(name);
	}
	
	public GameServer getServerMostPlayers() {
		
		int mostPlayersCount = 0;
		GameServer mostPlayersServer = null;
		
		for (Map.Entry<String, GameServer> entry : servers.entrySet()) {
			GameServer server = entry.getValue();
			int playerCount = server.getPlayerCount();
			
			if (playerCount > mostPlayersCount) {
				mostPlayersServer = server;
				mostPlayersCount = playerCount;
			}
		}
		
		return mostPlayersServer;
	}
	
	@Override
	public void onPluginMessageReceived(String channel, Player player, byte[] message) {
		
		if (!channel.equals("BungeeCord"))
			return;
		
		ByteArrayDataInput in = ByteStreams.newDataInput(message);
		
		if (!in.readUTF().equals("PlayerCount"))
			return;
		
		GameServer server = servers.get(in.readUTF());
		
		server.setPlayerCount(in.readInt());
		
		for (SignDisplay sign : MegaTWMaster.getInstance().getSignManager().getServerSigns(server))
			sign.refresh();
	}
	
	public void loadServers() {
		
		for (String name : MegaTWMaster.getInstance().getConfig().getStringList("servers"))
			servers.put(name, new GameServer(name));
	}
}