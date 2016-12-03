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

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.scheduler.BukkitScheduler;

import com.andrewyunt.megatw_master.MegaTWMaster;

/**
 * The object used to perform operations on signs in the MegaTW plugin.
 * 
 * @author Andrew Yunt
 */
public abstract class SignDisplay {
	
	public enum Type {
		LEADERBOARD,
		SERVER
	}
	
	protected Sign bukkitSign;
	protected int configNumber;
	protected GameServer server = null;
	
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
	public SignDisplay (int configNumber, Location loc, long updateInterval, boolean load) {
		
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
	
	public int getConfigNumber() {
		
		return configNumber;
	}
	
	public Sign getBukkitSign() {
		
		return bukkitSign;
	}
	
	public abstract void refresh();
}