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
package com.andrewyunt.megatw_master.listeners;

import com.andrewyunt.megatw_base.MegaTWBase;
import com.andrewyunt.megatw_base.exception.PlayerException;
import com.andrewyunt.megatw_master.MegaTWMaster;
import com.andrewyunt.megatw_master.exception.ServerException;
import com.andrewyunt.megatw_master.exception.SignException;
import com.andrewyunt.megatw_master.managers.SignManager;
import com.andrewyunt.megatw_base.objects.Class;
import com.andrewyunt.megatw_master.objects.GamePlayer;
import com.andrewyunt.megatw_master.objects.GameServer;
import com.andrewyunt.megatw_master.objects.LeaderboardSign;
import com.andrewyunt.megatw_master.objects.ServerSign;
import com.andrewyunt.megatw_master.objects.SignDisplay;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

import SebucoHD.Selector.Main;

import java.lang.reflect.Method;

import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitScheduler;

/**
 * The listener class used for general event handling within the plugin
 * which holds methods to listen on events.
 * 
 * @author Andrew Yunt
 */
public class PlayerListener implements Listener {
	
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		
		final Player bp = event.getPlayer();
		
		BukkitScheduler scheduler = MegaTWMaster.getInstance().getServer().getScheduler();
		scheduler.scheduleSyncDelayedTask(MegaTWMaster.getInstance(), () -> {
			GamePlayer player = null;
			
			// Get the player's GamePlayer object and if it doesn't exist, add it
			try {
				player = MegaTWMaster.getInstance().getPlayerManager().createPlayer(bp.getName());
			} catch (PlayerException e) {
			}
			
			// Set player's scoreboard to default scoreboard
			player.updateDynamicScoreboard();
			
			// Update player hotbar */
			player.updateHotbar();
			
			// Teleport the player to the spawn location
			Location loc = bp.getWorld().getSpawnLocation().clone();
			
			Chunk chunk = loc.getChunk();
			
			if (!chunk.isLoaded())
				chunk.load();
			
			loc.setY(loc.getY() + 1);
			
			bp.teleport(loc, TeleportCause.COMMAND);
		}, 1L);
	}

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event) {
		
		try {
			GamePlayer gp = MegaTWMaster.getInstance().getPlayerManager().getPlayer(event.getPlayer().getName());
			MegaTWMaster.getInstance().getPlayerManager().deletePlayer(gp);
		} catch (PlayerException e) {
		}
	}

	@EventHandler (priority = EventPriority.HIGHEST)
	public void onPlayerInteract(PlayerInteractEvent event) {
		
		if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK)
			return;

		ItemStack item = event.getItem();

		if (item == null || !item.hasItemMeta())
			return;

		Material type = item.getType();

		if (type != Material.COMPASS && type != Material.EMERALD && type != Material.COMMAND)
			return;

		ItemMeta meta = item.getItemMeta();
		String name = meta.getDisplayName();
		Player player = event.getPlayer();
		GamePlayer gp = null;

		try {
			gp = MegaTWMaster.getInstance().getPlayerManager().getPlayer(player.getName());
		} catch (PlayerException e) {
		}

		if (name.equals(ChatColor.RED + "Server Selector")) {
			Method method = null;
			
			try {
				method = Main.getInstance().getClass().getDeclaredMethod("getInv", Player.class);
				method.setAccessible(true);
				player.openInventory((Inventory) method.invoke(Main.getInstance(), player));
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (name.equals(ChatColor.GREEN + "Shop"))
			MegaTWMaster.getInstance().getShopMenu().openMainMenu(gp);
		else if (name.equals(ChatColor.YELLOW + "Class Selector"))
			MegaTWBase.getInstance().getClassSelectorMenu().openMainMenu(gp);
	}

	@EventHandler
	public void onPlayerDropItem(PlayerDropItemEvent event) {

		event.setCancelled(true);
	}
	
	@EventHandler
	public void onSignChange(SignChangeEvent event) {
		
		if (event.getLine(0) == null || event.getLine(1) == null)
			return;
		
		Player player = event.getPlayer();
		GameServer server = null;
		SignDisplay.Type type;
		int place = 0;
		Class classType = null;
		boolean weekly = false, finalKill = false;
		
		if (event.getLine(0).equalsIgnoreCase("[Leaderboard]")) {
			type = SignDisplay.Type.LEADERBOARD;
			
			classType = Class.valueOf(event.getLine(1));
			
			String[] spliced = event.getLine(2).split(" ");
			weekly = Boolean.valueOf(spliced[0]);
			finalKill = Boolean.valueOf(spliced[1]);
			
			try {
				place = Integer.valueOf(event.getLine(3));
			} catch (NumberFormatException e) {
				player.sendMessage(ChatColor.RED + "You did not enter an integer for the sign place.");
				return;
			}
			
			if (place > 5) {
				player.sendMessage(ChatColor.RED + "You may not enter a place over 5.");
				return;
			}
		} else if (event.getLine(0).equalsIgnoreCase("[Join]")) {
			type = SignDisplay.Type.SERVER;
			
			try {
				server = MegaTWMaster.getInstance().getServerManager().getServer(event.getLine(1));
			} catch (ServerException e) {
				player.sendMessage(String.format("The server %s is not registered in the config.",
						event.getLine(1)));
			}
		} else
			return;
		
		if (!player.hasPermission("megatw.sign.create")) {
			player.sendMessage(ChatColor.RED + "You do not have permission to create a sign display.");
			return;
		}
		
		SignDisplay sign = null;
		
		try {
			sign = MegaTWMaster.getInstance().getSignManager().createSign(
					type,
					event.getBlock().getLocation(),
					6000L);
		} catch (SignException e) {
			player.sendMessage(ChatColor.RED + e.getMessage());
			return;
		}
		
		if (type == SignDisplay.Type.LEADERBOARD) {
			((LeaderboardSign) sign).setPlace(place);
			((LeaderboardSign) sign).setClassType(classType);
			((LeaderboardSign) sign).setWeekly(weekly);
			((LeaderboardSign) sign).setFinalKill(finalKill);
		} else if (type == SignDisplay.Type.SERVER)
			((ServerSign) sign).setServer(server);
		
		sign.refresh();
	}
	
	@EventHandler
	public void onSignClick(PlayerInteractEvent event) {
		
		if (event.getAction() != Action.RIGHT_CLICK_BLOCK)
			return;
		
		Block block = event.getClickedBlock();
		
		if (block.getType() != Material.SIGN_POST && block.getType() != Material.WALL_SIGN)
			return;
		
		Sign sign =(Sign) block.getState();
		
		if (!sign.getLine(0).equals("[Join]"))
			return;
		
		ByteArrayDataOutput out = ByteStreams.newDataOutput();
		out.writeUTF("Connect");
		out.writeUTF(ChatColor.stripColor(sign.getLine(1)));
		event.getPlayer().sendPluginMessage(MegaTWMaster.getInstance(), "BungeeCord", out.toByteArray());
	}
	
	@EventHandler
	public void onSignBreak(BlockBreakEvent event) {
		
		Block block = event.getBlock();
		
		if (block.getType() != Material.SIGN_POST && block.getType() != Material.WALL_SIGN)
			return;
		
		SignManager signManager = MegaTWMaster.getInstance().getSignManager();
		
		try {
			signManager.deleteSign(signManager.getSign(block.getLocation()));
		} catch (SignException e) {
		}
	}
}