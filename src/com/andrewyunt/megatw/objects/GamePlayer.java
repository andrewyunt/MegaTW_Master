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
package com.andrewyunt.megatw.objects;

import java.util.HashMap;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitScheduler;

import com.andrewyunt.megatw.MegaTW;
import net.shortninja.staffplus.StaffPlus;

/**
 * The class used to store player's information.
 * 
 * @author Andrew Yunt
 */
public class GamePlayer {
	
	private final String name;
	private Class classType;
	private int coins = 0;
	private int earnedCoins = 0;
	private int kills = 0;
	private DisplayBoard displayBoard = null;
	private boolean loaded = false;
	private boolean hasBloodEffect = false;
	private Map<Upgradable, Integer> upgradeLevels = new HashMap<Upgradable, Integer>();
	
	public GamePlayer(String name) {
		
		/* Set variables */
		this.name = name;
		
		/* Load upgradable levels */
		for (Class classType : Class.values()) {
			int level = MegaTW.getInstance().getDataSource().getLevel(this, classType);
			upgradeLevels.put(classType, level);
		}
		
		for (Skill skillType : Skill.values()) {
			int level = MegaTW.getInstance().getDataSource().getLevel(this, skillType);
			upgradeLevels.put(skillType, level);
		}
		
		for (Ability abilityType : Ability.values()) {
			int level = MegaTW.getInstance().getDataSource().getLevel(this, abilityType);
			upgradeLevels.put(abilityType, level);
		}
		
		/* Get the scheduler */
		BukkitScheduler scheduler = MegaTW.getInstance().getServer().getScheduler();
		
		/* Repeating task to remove withering */
		OfflinePlayer op = Bukkit.getServer().getOfflinePlayer(name);
		
		scheduler.scheduleSyncRepeatingTask(MegaTW.getInstance(), new Runnable() {
			@Override
			public void run() {
				
				if (!op.isOnline())
					return;
				
				getBukkitPlayer().removePotionEffect(PotionEffectType.WITHER);
			}
		}, 0L, 20L);
		
		/* Set up scoreboard */
		String title = ChatColor.AQUA + "" + ChatColor.BOLD + "MegaTW";
		
		displayBoard = new DisplayBoard(getBukkitPlayer(), title);
		
	    scheduler.scheduleSyncRepeatingTask(MegaTW.getInstance(), new Runnable() {
	    	ChatColor curTitleColor = ChatColor.AQUA;
	    
	    	@Override
	    	public void run() {
	    		ChatColor newTitleColor = curTitleColor == ChatColor.AQUA ? ChatColor.WHITE : ChatColor.AQUA;
	    		
	    		displayBoard.setTitle(newTitleColor + "" + ChatColor.BOLD + "MegaTW");
	    		
	    		curTitleColor = newTitleColor;
	    	}
	    }, 0L, 20L);
	}
	
	public String getName() {
		
		return name;
	}
	
	public Player getBukkitPlayer() {
		
		return MegaTW.getInstance().getServer().getPlayer(name);
	}
	
	public void setClassType(Class classType) {
		
		this.classType = classType;
	}
	
	public Class getClassType() {
		
		return classType;
	}
	
	public boolean hasSelectedClass() {
		
		return classType != null;
	}
	
	public boolean isStaffMode() {
		
		return StaffPlus.get().modeCoordinator.isInMode(getBukkitPlayer().getUniqueId());
	}
	
	public void updateHotbar() {
		
		PlayerInventory inv = getBukkitPlayer().getInventory();
		
		inv.setHelmet(new ItemStack(Material.AIR));
		inv.setChestplate(new ItemStack(Material.AIR));
		inv.setLeggings(new ItemStack(Material.AIR));
		inv.setBoots(new ItemStack(Material.AIR));
		inv.clear();
		
		for (Map.Entry<Integer, ItemStack> entry : MegaTW.getInstance().getHotbarItems().entrySet()) {
		    int pos = entry.getKey();
		    ItemStack is = entry.getValue();
		    inv.setItem(pos, is);
		}
	}
	
	public void addCoins(int coins) {
		
		setCoins(this.coins + coins);
		
		setEarnedCoins(this.earnedCoins + coins);
	}
	
	public void removeCoins(int coins) {
		
		setCoins(this.coins - coins);
	}
	
	public void setCoins(int coins) {
		
		this.coins = coins;
		
		updateScoreboard();
	}
	
	public int getCoins() {
		
		return coins;
	}
	
	public void setEarnedCoins(int earnedCoins) {
		
		this.earnedCoins = earnedCoins;
	}
	
	public int getEarnedCoins() {
		
		return earnedCoins;
	}
	
	public void setKills(int kills) {
		
		this.kills = kills;
		
		updateScoreboard();
	}
	
	public int getKills() {
		
		return kills;
	}
	
	public DisplayBoard getDisplayBoard() {
		
		return displayBoard;
	}
	
	public void updateScoreboard() {
		
		/* Clear current fields */
		displayBoard.clear();
		
		displayBoard.putField(ChatColor.RESET + " ");
		
		/* Display player's coins */
		displayBoard.putField(ChatColor.GOLD + "" + ChatColor.BOLD + "Coins");
		displayBoard.putField(String.valueOf(coins));
		
		/* Display player's kills */
		displayBoard.putField(ChatColor.RED + "" + ChatColor.BOLD + "Kills");
		displayBoard.putField(String.valueOf(kills));
		
		displayBoard.putField(ChatColor.RESET + "   ");
		
		/* Display server's IP */
		displayBoard.putField(ChatColor.YELLOW + "mc.amosita.net");
		
		/* Display board to player */
		displayBoard.display();
	}
	
	public void setLoaded(boolean loaded) {
		
		this.loaded = loaded;
	}
	
	public boolean isLoaded() {
		
		return loaded;
	}
	
	public void setBloodEffect(boolean hasBloodEffect) {
		
		this.hasBloodEffect = hasBloodEffect;
	}
	
	public boolean hasBloodEffect() {
		
		return hasBloodEffect;
	}
	
	public void setClassLevel(Upgradable upgradable, int level) {
		
		if (upgradeLevels.containsKey(upgradable))
			upgradeLevels.remove(upgradable);
		
		upgradeLevels.put(upgradable, level);
	}
	
	public int getLevel(Upgradable upgradable) {
		
		if (upgradeLevels.containsKey(upgradable))
			return upgradeLevels.get(upgradable);
		
		return 1;
	}
	
	public Map<Upgradable, Integer> getUpgradeLevels() {
		
		return upgradeLevels;
	}
}