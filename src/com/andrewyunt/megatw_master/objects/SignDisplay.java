/*
 * Unpublished Copyright (c) 2016 Andrew Yunt, All Rights Reerved.
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
package com.andrewyunt.megatw_master.objects;

import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.scheduler.BukkitScheduler;

import com.andrewyunt.megatw_base.MegaTWBase;
import com.andrewyunt.megatw_base.utilities.Utils;
import com.andrewyunt.megatw_master.MegaTWMaster;
import com.andrewyunt.megatw_master.exception.ServerException;

/**
 * The object used to perform operations on signs in the MegaTW plugin.
 * 
 * @author Andrew Yunt
 */
public class SignDisplay {
	
	public enum Type {
		LEADERBOARD,
		SERVER
	}
	
	private Sign bukkitSign;
	private int place = 0;
	private GameServer server = null;
	
	private final Type type;
	private int configNumber;
	
	/**
	 * Creates a sign display with the specified location and update interval.
	 * 
	 * @param loc
	 * 		The location of the display.
	 * @param updateInterval
	 * 		The update interval of the display in ticks.
	 * @param place
	 * 		The place on the leaderboard the sign should display.
	 */
	public SignDisplay (Type type, int configNumber, Location loc, long updateInterval, boolean load) {
		
		this.type = type;
		this.configNumber = configNumber;
		
		Block block = loc.getWorld().getBlockAt(loc);
		
		if (block.getType() == Material.SIGN_POST || block.getType() == Material.WALL_SIGN)
			bukkitSign =(Sign) block.getState();
		
		BukkitScheduler scheduler = MegaTWMaster.getInstance().getServer().getScheduler();
		scheduler.scheduleSyncRepeatingTask(MegaTWMaster.getInstance(), new Runnable() {
			boolean refresh = !load;
			
			@Override
			public void run() {
				
				if (refresh)
					refresh();
				
				refresh = true;
			}
		}, 0L, updateInterval);
	}
	
	public Type getType() {
		
		return type;
	}
	
	public void setConfigNumber(int configNumber) {
		
		this.configNumber = configNumber;
	}
	
	public int getConfigNumber() {
		
		return configNumber;
	}
	
	public void setPlace(int place) {
		
		this.place = place;
		
		save();
	}
	
	public void setServer(GameServer server) {
		
		this.server = server;
		
		save();
	}
	
	public GameServer getServer() {
		
		return server;
	}
	
	public Sign getBukkitSign() {
		
		return bukkitSign;
	}
	
	public void refresh() {
		
		if (type == Type.LEADERBOARD) {
			if (place == 0)
				return;
			
			Map<Integer, Entry<OfflinePlayer, Integer>> mostKills = MegaTWBase.getInstance().getDataSource().getMostKills();
			Entry<OfflinePlayer, Integer> entry = mostKills.get(place);
			
			OfflinePlayer op = entry.getKey();
			
			bukkitSign.setLine(0, op.getName());
			bukkitSign.setLine(1, entry.getValue() + " Kills");
			bukkitSign.setLine(3, place + Utils.getNumberSuffix(place) + " Place");
		} else {
			if (server == null)
				return;
			
			bukkitSign.setLine(0, "[Join]");
			bukkitSign.setLine(1, ChatColor.BOLD + server.getName());
			bukkitSign.setLine(3, server.getPlayerCount() + " Online");
		}

		bukkitSign.update();
	}
	
	public void save() {
		
		MegaTWMaster plugin = MegaTWMaster.getInstance();
		FileConfiguration signConfig = plugin.getSignConfig().getConfig();
		
		signConfig.set("signs." + configNumber + ".type", type.toString());
		
		if (type == Type.LEADERBOARD)
			signConfig.set("signs." + configNumber + ".place", place);
		
		if (type == Type.SERVER)
			signConfig.set("signs." + configNumber + ".server", server.getName());
		
		signConfig.createSection("signs." + configNumber + ".location",
				Utils.serializeLocation(bukkitSign.getLocation()));
		
		plugin.getSignConfig().saveConfig();
	}
	
	public static SignDisplay loadFromConfig(ConfigurationSection section) {
		
		SignDisplay.Type type = SignDisplay.Type.valueOf(section.getString("type"));
		Location loc = Utils.deserializeLocation(section.getConfigurationSection("location"));
		SignDisplay signDisplay = new SignDisplay(type, Integer.valueOf(section.getName()), loc, 6000L, true);
		
		if (type == Type.LEADERBOARD)
			signDisplay.setPlace(section.getInt("place"));
		
		if (type == Type.SERVER)
			try {
				signDisplay.setServer(MegaTWMaster.getInstance().getServerManager().getServer(section.getString("server")));
			} catch (ServerException e) {
				e.printStackTrace();
			}
		
		return signDisplay;
	}
}