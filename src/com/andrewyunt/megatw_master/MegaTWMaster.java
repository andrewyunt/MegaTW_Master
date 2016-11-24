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
package com.andrewyunt.megatw_master;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.andrewyunt.megatw_master.configuration.SignConfiguration;
import com.andrewyunt.megatw_master.listeners.PlayerListener;
import com.andrewyunt.megatw_master.managers.PlayerManager;
import com.andrewyunt.megatw_master.managers.ServerManager;
import com.andrewyunt.megatw_master.managers.SignManager;
import com.andrewyunt.megatw_master.menu.GeneralMenu;
import com.andrewyunt.megatw_master.menu.LayoutEditorMenu;
import com.andrewyunt.megatw_master.menu.ShopMenu;

/**
 * The main class in the MegaTW plugin.
 * 
 * <p>
 * You can get the instance of this class using the static getInstance() method.
 * </p>
 * 
 * @author Andrew Yunt
 */
public class MegaTWMaster extends JavaPlugin {
	
	private final Logger logger = getLogger();
	private final Server server = getServer();
	private final PluginManager pm = server.getPluginManager();
    private final ShopMenu shopMenu = new ShopMenu();
    private final LayoutEditorMenu layoutEditorMenu = new LayoutEditorMenu();
    private final GeneralMenu generalMenu = new GeneralMenu();
    private final Map<Integer, ItemStack> hotbarItems = new HashMap<Integer, ItemStack>();
	private final SignConfiguration signConfiguration = new SignConfiguration();
	
	private PlayerManager playerManager;
	private SignManager signManager;
	private ServerManager serverManager;
	
	private static MegaTWMaster instance;
	
	/**
	 * Method is executed while the plugin is being enabled.
	 * 
	 * <p>
	 * Checks for dependencies, sets the static instance of the class, saves default
	 * configuration files, sets command executors, registers events, loads arenas from
	 * arenas.yml, creates FFA and TDM games, and creates default scoreboard.
	 * </p>
	 */
	@Override
	public void onEnable() {
		
		/* Set static instance to this */
		instance = this;
		
		/* Save default configs to plugin folder */
		saveDefaultConfig();
		signConfiguration.saveDefaultConfig();
		
		/* Set managers */
		playerManager = new PlayerManager();
		signManager = new SignManager();
		serverManager = new ServerManager();
		
		/* Load all servers from config.yml */
		serverManager.loadServers();
		
		/* Load all signs from signs.yml */
		signManager.loadSigns();
		
		/* Register BungeeCord plugin channel */
		server.getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
		server.getMessenger().registerIncomingPluginChannel(this, "BungeeCord", serverManager);
		
		pm.registerEvents(new PlayerListener(), this);
		pm.registerEvents(shopMenu, this);
		pm.registerEvents(layoutEditorMenu, this);
		pm.registerEvents(generalMenu, this);
		
		/* Create hotbar items and add them to the map */
		createHotbarItems();
	}
	
	/**
	 * Gets the instance of the MegaTW class.
	 * 
	 * @return
	 * 		Instance of the MegaTW class.
	 */
	public static MegaTWMaster getInstance() {
		
		return instance;
	}
	
	/**
	 * Gets the instance of the PlayerManager class.
	 * 
	 * @return
	 * 		Instance of the PlayerManager class.
	 */
	public PlayerManager getPlayerManager() {
		
		return playerManager;
	}
	
	/**
	 * Gets the instance of the SignManager class.
	 * 
	 * @return
	 * 		Instance of the SignManager class.
	 */
	public SignManager getSignManager() {
		
		return signManager;
	}
	
	/**
	 * Gets the instance of the ServerManager class.
	 * 
	 * @return
	 * 		Instance of the ServerManager class.
	 */
	public ServerManager getServerManager() {
		
		return serverManager;
	}
	
	/**
	 * Gets the instance of the SignConfiguration class.
	 * 
	 * @return
	 * 		Instance of the SignConfiguration class.
	 */
	public SignConfiguration getSignConfig() {
		
		return signConfiguration;
	}
	
	/**
	 * Gets the stored instance of the shop menu.
	 * 
	 * @return
	 * 		The instance of the shop menu.
	 */
	public ShopMenu getShopMenu() {
		
		return shopMenu;
	}
	
	/**
	 * Gets the stored instance of the general menu.
	 * 
	 * @return
	 * 		The instance of the genu menu.
	 */
	public GeneralMenu getGeneralMenu() {
		
		return generalMenu;
	}
	
	/**
	 * Gets the stored instance of the layout editor menu.
	 * 
	 * @return
	 * 		The instance of the layout editor menu.
	 */
	public LayoutEditorMenu getLayoutEditorMenu() {
		
		return layoutEditorMenu;
	}
	
	public void createHotbarItems() {
		
		/* Create items */
		ItemStack general = new ItemStack(Material.BOOK);
		ItemStack classSelector = new ItemStack(Material.COMMAND);
		
		/* Get item metas */
		ItemMeta generalMeta = general.getItemMeta();
		ItemMeta classSelectorMeta = classSelector.getItemMeta();
		
		/* Set meta display names */
		generalMeta.setDisplayName(ChatColor.AQUA + "General");
		classSelectorMeta.setDisplayName(ChatColor.GREEN + "Class Selector");
		
		/* Set item metas */
		general.setItemMeta(generalMeta);
		classSelector.setItemMeta(classSelectorMeta);
		
		/* Set hotbar items in map */
		hotbarItems.put(0, general);
		hotbarItems.put(1, classSelector);
	}
	
	public Map<Integer, ItemStack> getHotbarItems() {
		
		return hotbarItems;
	}
}