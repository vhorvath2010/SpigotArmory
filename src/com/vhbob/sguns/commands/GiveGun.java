package com.vhbob.sguns.commands;

import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.vhbob.sguns.SpigotGuns;

public class GiveGun implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String name, String[] args) {
		if (cmd.getName().equalsIgnoreCase("GiveGun")) {
			if (args.length == 2) {
				String gunToken = args[1].toLowerCase();
				String playerName = args[0];
				if (Bukkit.getPlayer(playerName) != null && SpigotGuns.itemTemplates.containsKey(gunToken)) {
					Player p = Bukkit.getPlayer(playerName);
					// Give player item
					ItemStack gunItem = SpigotGuns.itemTemplates.get(gunToken).clone();
					List<String> lore = gunItem.getItemMeta().getLore();
					lore.add(toInvis(UUID.randomUUID().toString()));
					ItemMeta gunItemMeta = gunItem.getItemMeta();
					gunItemMeta.setLore(lore);
					gunItem.setItemMeta(gunItemMeta);

					// Update ammo
					SpigotGuns.gunTemplates.get(gunItemMeta.getDisplayName()).updateAmmo(gunItem);
					p.getInventory().addItem(gunItem);
					p.sendMessage(SpigotGuns.prefix + "Gave a gun to " + p.getName());
					return true;
				}
			}
		}
		return false;
	}

	public static String toInvis(String s) {
		String invis = "";
		for (char c : s.toCharArray()) {
			invis += (ChatColor.COLOR_CHAR + "" + c);
		}
		return invis;
	}

}
