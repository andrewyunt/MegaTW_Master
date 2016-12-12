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
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.scheduler.BukkitScheduler;

import com.andrewyunt.megatw_base.MegaTWBase;
import com.andrewyunt.megatw_base.objects.DynamicScoreboard;
import com.andrewyunt.megatw_master.MegaTWMaster;

/**
 * The class used to store player's information.
 * 
 * @author Andrew Yunt
 */
public class GamePlayer extends com.andrewyunt.megatw_base.objects.GamePlayer {
	
	public GamePlayer(String name) {
		
		super(name);
		
		// Set up the scoreboard
		String title = ChatColor.YELLOW + "" + ChatColor.BOLD + "MEGATW";
		
		dynamicScoreboard = new DynamicScoreboard(title);
		getBukkitPlayer().setScoreboard(dynamicScoreboard.getScoreboard());
		
		BukkitScheduler scheduler = MegaTWMaster.getInstance().getServer().getScheduler();
		scheduler.scheduleSyncRepeatingTask(MegaTWMaster.getInstance(), new Runnable() {
			ChatColor curTitleColor = ChatColor.YELLOW;
			
			@Override
			public void run() {
				ChatColor newTitleColor = curTitleColor == ChatColor.YELLOW ? ChatColor.WHITE : ChatColor.YELLOW;
				
				dynamicScoreboard.getObjective().setDisplayName(newTitleColor.toString() + ChatColor.BOLD + "MEGATW");
				
				curTitleColor = newTitleColor;
			}
		}, 0L, 20L);
	}
	
	public void updateHotbar() {
		
		PlayerInventory inv = getBukkitPlayer().getInventory();
		
		inv.setHelmet(new ItemStack(Material.AIR));
		inv.setChestplate(new ItemStack(Material.AIR));
		inv.setLeggings(new ItemStack(Material.AIR));
		inv.setBoots(new ItemStack(Material.AIR));
		inv.clear();
		
		for (Map.Entry<Integer, ItemStack> entry : MegaTWMaster.getInstance().getHotbarItems().entrySet()) {
			int pos = entry.getKey();
			ItemStack is = entry.getValue();
			inv.setItem(pos, is);
		}
	}
	
	@Override
	public void setCoins(int coins) {
		
		this.coins = coins;
		
		updateDynamicScoreboard();
	}
	
	@Override
	public void setKills(int kills) {
		
		this.kills = kills;
		
		updateDynamicScoreboard();
	}
	
	@Override
	public void updateDynamicScoreboard() {
		
		dynamicScoreboard.blankLine(10);
		
		// Display player's coins
		dynamicScoreboard.update(9, ChatColor.WHITE + "Coins: " + ChatColor.GREEN + coins);
		
		// Display player's wins
		dynamicScoreboard.update(8, ChatColor.WHITE + "Wins: " + ChatColor.GREEN + wins);
		
		// Display kill stats
		int weeklyKills = MegaTWBase.getInstance().getDataSource().getKills(this, true, false, null);
		int weeklyFinalKills = MegaTWBase.getInstance().getDataSource().getKills(this, true, true, null);
		
		dynamicScoreboard.update(7, ChatColor.WHITE + "Weekly Kills: "
				+ ChatColor.GREEN + weeklyKills);
		dynamicScoreboard.update(6, ChatColor.WHITE + "Weekly Final Kills: "
				+ ChatColor.GREEN + weeklyFinalKills);
		
		dynamicScoreboard.blankLine(5);
		
		// Display player's chosen class
		dynamicScoreboard.update(4, ChatColor.WHITE + "Chosen Class:");
		dynamicScoreboard.update(3, ChatColor.GREEN + (classType == null ? "None" : classType.getName()));
		
		dynamicScoreboard.blankLine(2);
		
		// Display server's IP
		dynamicScoreboard.update(1, ChatColor.YELLOW + "mc.amosita.net");
	}
}