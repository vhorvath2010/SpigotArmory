package com.vhbob.sguns.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.vhbob.sguns.SpigotGuns;

public class GiveAmmo implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String name, String[] args) {
		if (cmd.getName().equalsIgnoreCase("GiveAmmo")) {
			if (args.length == 2) {
				String player = args[0];
				String ammoType = args[1];
				if (Bukkit.getPlayer(player) != null && SpigotGuns.ammoTemplates.containsKey(ammoType)) {
					Player p = Bukkit.getPlayer(player);
					ItemStack ammo = SpigotGuns.ammoTemplates.get(ammoType).clone();
					ammo.setAmount(64);
					p.getInventory().addItem(ammo);
					sender.sendMessage(SpigotGuns.prefix + "Gave ammo to " + p.getName());
					return true;
				}
			}
		}
		return false;
	}

}
