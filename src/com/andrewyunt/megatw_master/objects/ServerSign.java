package com.andrewyunt.megatw_master.objects;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import com.andrewyunt.megatw_base.utilities.Utils;
import com.andrewyunt.megatw_master.MegaTWMaster;
import com.andrewyunt.megatw_master.exception.ServerException;

public class ServerSign extends SignDisplay {

	private GameServer server;
	
	public ServerSign(int configNumber, Location loc, long updateInterval, boolean load) {
		
		super(configNumber, loc, updateInterval, load);
	}
	
	public void setServer(GameServer server) {
		
		this.server = server;
	}
	
	public GameServer getServer() {
		
		return server;
	}
	
	public void refresh() {
		
		if (server == null)
			return;
		
		bukkitSign.setLine(0, "[Join]");
		bukkitSign.setLine(1, ChatColor.BOLD + server.getName());
		bukkitSign.setLine(3, server.getPlayerCount() + " Online");
		
		bukkitSign.update();
	}
	
	public void save() {
		
		MegaTWMaster plugin = MegaTWMaster.getInstance();
		FileConfiguration signConfig = plugin.getSignConfig().getConfig();
		
		signConfig.set("signs." + configNumber + ".type", "SERVER");
		signConfig.set("signs." + configNumber + ".server", server.getName());
		
		signConfig.createSection("signs." + configNumber + ".location",
				Utils.serializeLocation(bukkitSign.getLocation()));
		
		plugin.getSignConfig().saveConfig();
	}
	
	public static ServerSign loadFromConfig(ConfigurationSection section) {
		
		Location loc = Utils.deserializeLocation(section.getConfigurationSection("location"));
		ServerSign serverSign = new ServerSign(Integer.valueOf(section.getName()), loc, 6000L, true);
		
		try {
			serverSign.setServer(MegaTWMaster.getInstance().getServerManager().getServer(section.getString("server")));
		} catch (ServerException e) {
			e.printStackTrace();
		}
		
		return serverSign;
	}
}