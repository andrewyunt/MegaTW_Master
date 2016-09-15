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
package com.andrewyunt.megatw.listeners;

import com.andrewyunt.megatw.MegaTW;
import com.andrewyunt.megatw.exception.PlayerException;
import com.andrewyunt.megatw.exception.SignException;
import com.andrewyunt.megatw.objects.GamePlayer;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
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
		
		BukkitScheduler scheduler = MegaTW.getInstance().getServer().getScheduler();
		scheduler.scheduleSyncDelayedTask(MegaTW.getInstance(), () -> {

            GamePlayer player = null;

            /* Get the player's GamePlayer object and if it doesn't exist, add it */
            try {
                player = MegaTW.getInstance().getPlayerManager().createPlayer(bp.getName());
            } catch (PlayerException e) {
            }

            /* Set player's scoreboard to default scoreboard */
            player.updateScoreboard();

            /* Update player hotbar */
            player.updateHotbar();

            /* Teleport the player to the spawn location */
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
			GamePlayer gp = MegaTW.getInstance().getPlayerManager().getPlayer(event.getPlayer().getName());
			MegaTW.getInstance().getPlayerManager().deletePlayer(gp);
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

		if (!(type == Material.BOOK || type == Material.COMMAND || type == Material.DIAMOND_SWORD))
			return;

		ItemMeta meta = item.getItemMeta();
		String name = meta.getDisplayName();
		Player player = event.getPlayer();
		GamePlayer gp = null;
		MegaTW plugin = MegaTW.getInstance();

		try {
			gp = plugin.getPlayerManager().getPlayer(player.getName());
		} catch (PlayerException e) {
		}

		if (name.equals(ChatColor.AQUA + "General")) {

			plugin.getGeneralMenu().openMainMenu(gp);

		} else if (name.equals(ChatColor.GREEN + "Class Selector")) {
			
			plugin.getClassSelectorMenu().openMainMenu(gp);
			
		} else if (name.equals("Play : MegaTW")) {
			
			
		}
	}

	@EventHandler
	public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {

		GamePlayer player = null;

		try {
			player = MegaTW.getInstance().getPlayerManager().getPlayer(event.getPlayer().getName());
		} catch (PlayerException e) {
		}

		String message = event.getMessage();

		if (message.startsWith("/tp") && !(message.equalsIgnoreCase("/tps"))) {

			if (!(player.isStaffMode())) {
				player.getBukkitPlayer()
						.sendMessage(ChatColor.RED + "You must enter staff mode before using that command.");
				player.getBukkitPlayer().sendMessage(ChatColor.RED + "Usage: /staff");
				event.setCancelled(true);
			}

		}
	}

	@EventHandler
	public void onPlayerDropItem(PlayerDropItemEvent event) {

		event.setCancelled(true);
	}
	
	@EventHandler
	public void onSignChange(SignChangeEvent event) {
		
		if (event.getLine(0) == null || event.getLine(1) == null)
			return;
		
		if (!event.getLine(0).equalsIgnoreCase("[Leaderboard]"))
			return;
		
		Player player = event.getPlayer();
		
		if (!player.hasPermission("megatw.sign.create")) {
			player.sendMessage(ChatColor.RED + "You do not have permission to create a leaderboard sign.");
			return;
		}
		
		int place = 0;
		
		try {
			place = Integer.valueOf(event.getLine(1));
		} catch (NumberFormatException e) {
			player.sendMessage(ChatColor.RED + "You did not enter an integer for the sign place.");
			return;
		}
		
		if (place > 5) {
			player.sendMessage(ChatColor.RED + "You may not enter a place over 5.");
			return;
		}
		
		try {
			MegaTW.getInstance().getSignManager().createSign(
					event.getBlock().getLocation(),
					place,
					6000L);
		} catch (SignException e) {
			player.sendMessage(ChatColor.RED + e.getMessage());
		}
	}
}