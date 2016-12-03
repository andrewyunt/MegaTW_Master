package com.andrewyunt.megatw_master.objects;

import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import com.andrewyunt.megatw_base.MegaTWBase;
import com.andrewyunt.megatw_base.objects.Class;
import com.andrewyunt.megatw_base.utilities.Utils;
import com.andrewyunt.megatw_master.MegaTWMaster;

public class LeaderboardSign extends SignDisplay {
	
	Class classType;
	boolean weekly, finalKill;
	int place = 0;
	
	public LeaderboardSign(int configNumber, Location loc, long updateInterval, boolean load) {
		
		super(configNumber, loc, updateInterval, load);
	}
	
	public void setClassType(Class classType) {
		
		this.classType = classType;
	}
	
	public void setWeekly(boolean weekly) {
		
		this.weekly = weekly;
		
		save();
	}
	
	public void setFinalKill(boolean finalKill) {
		
		this.finalKill = finalKill;
	}
	
	public void setPlace(int place) {
		
		this.place = place;
		
		save();
	}
	
	public void refresh() {
		
		if (place == 0)
			return;
		
		Map<Integer, Entry<OfflinePlayer, Integer>> mostKills = MegaTWBase.getInstance().getDataSource()
				.getMostKills(weekly, finalKill, classType);
		
		Entry<OfflinePlayer, Integer> entry = null;
		
		try {
			entry = mostKills.get(place);
			
			OfflinePlayer op = entry.getKey();
			
			bukkitSign.setLine(0, op.getName());
			bukkitSign.setLine(1, entry.getValue() + " Kills");
			bukkitSign.setLine(3, place + Utils.getNumberSuffix(place) + " Place");
		} catch (NullPointerException e) {
			bukkitSign.setLine(0, "N/A");
		} finally {
			bukkitSign.update();
		}
	}
	
	public void save() {
		
		MegaTWMaster plugin = MegaTWMaster.getInstance();
		FileConfiguration signConfig = plugin.getSignConfig().getConfig();
		
		signConfig.set("signs." + configNumber + ".type", "LEADERBOARD");
		signConfig.set("signs." + configNumber + ".place", place);
		signConfig.set("signs." + configNumber + ".reset_weekly", weekly);
		signConfig.set("signs." + configNumber + ".final", finalKill);
		
		signConfig.createSection("signs." + configNumber + ".location",
				Utils.serializeLocation(bukkitSign.getLocation()));
		
		plugin.getSignConfig().saveConfig();
	}
	
	public static LeaderboardSign loadFromConfig(ConfigurationSection section) {
		
		Location loc = Utils.deserializeLocation(section.getConfigurationSection("location"));
		LeaderboardSign leaderboardSign = new LeaderboardSign(Integer.valueOf(section.getName()), loc, 6000L, true);
		
		leaderboardSign.setPlace(section.getInt("place"));
		leaderboardSign.setWeekly(section.getBoolean("reset_weekly"));
		leaderboardSign.setFinalKill(section.getBoolean("final"));
		
		return leaderboardSign;
	}
}