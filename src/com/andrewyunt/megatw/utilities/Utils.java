package com.andrewyunt.megatw.utilities;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.craftbukkit.v1_7_R4.inventory.CraftItemStack;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import net.minecraft.server.v1_7_R4.NBTTagCompound;
import net.minecraft.server.v1_7_R4.NBTTagList;

/**
 * The general utilities class for methods without a category / methods yet
 * to be categorized into another class.
 * 
 * @author Andrew Yunt
 * @author Gavin Lutz
 * @author md_5
 * @author blablubbabc
 */
public class Utils {

	public static Location deserializeLocation(ConfigurationSection section) {

		return new Location(Bukkit.getWorld(section.getString("w")), section.getDouble("x"), section.getDouble("y"),
				section.getDouble("z"));
	}

	public static Map<String, Object> serializeLocation(Location loc) {

		Map<String, Object> map = new HashMap<String, Object>();

		map.put("w", loc.getWorld().getName());
		map.put("x", loc.getX());
		map.put("y", loc.getY());
		map.put("z", loc.getZ());

		return map;
	}
	
	public static List<String> colorizeList(List<String> list, ChatColor color) {

		return list.stream().map(line -> color + line).collect(Collectors.toList());
	}
    
    public static Inventory fromChest(Inventory inv) {
    	
    	Inventory newInv = Bukkit.createInventory(null, 36);
    	
    	for (int i = 0; i <= 26; i++) {
    		ItemStack is = inv.getItem(i);
    		
    		if (is == null)
    			continue;
    		
    		newInv.setItem(i + 9, is.clone());
    	}
    	
    	for (int i = 27; i <= 35; i++) {
    		ItemStack is = inv.getItem(i);
    		
    		if (is == null)
    			continue;
    		
    		newInv.setItem(i - 27, is.clone());
    	}
    	
		return newInv;
    }
    
    public static Inventory toChest(Inventory inv) {
    	
    	Inventory newInv = Bukkit.createInventory(null, 36);
    	
    	for (int i = 0; i <= 8; i++) {
    		ItemStack is = inv.getItem(i);
    		
    		if (is == null)
    			continue;
    		
    		newInv.setItem(i + 27, is.clone());
    	}
    	
    	for (int i = 9; i <= 34; i++) {
    		ItemStack is = inv.getItem(i);
    		
    		if (is == null)
    			continue;
    		
    		newInv.setItem(i - 9, is.clone());
    	}
    	
		return newInv;
    }
    
    public static int getHighestEntry(ConfigurationSection section) {
    	
    	int highest = 0;
    	
    	if (section == null)
    		return 1;
    	
    	Set<String> keys = section.getKeys(false);
    	
    	if (keys.size() == 0)
    		return 0;
    	
    	for (String key : section.getKeys(false)) {
    		int num = Integer.valueOf(key);
    		
    		if (highest < num)
    			highest = num;
    	}
    	
    	return highest;
    }
    
    public static String getNumberSuffix(int num) {
    	
    	num = num % 100;
    	
        if (num >= 11 && num <= 13)
            return "th";
        
        switch (num % 10) {
        case 1:
            return "st";
        case 2:
            return "nd";
        case 3:
            return "rd";
        default:
            return "th";
        }
    }
    
    public static ItemStack addGlow(ItemStack item){ 
    	
    	net.minecraft.server.v1_7_R4.ItemStack nmsStack = CraftItemStack.asNMSCopy(item);
    	NBTTagCompound tag = null;
    	
    	if (!nmsStack.hasTag()) {
    		tag = new NBTTagCompound();
    		nmsStack.setTag(tag);
    	}
    	
    	if (tag == null)
    		tag = nmsStack.getTag();
    	
    	NBTTagList ench = new NBTTagList();
    	tag.set("ench", ench);
    	nmsStack.setTag(tag);
    	
    	return CraftItemStack.asCraftMirror(nmsStack);
    }
}