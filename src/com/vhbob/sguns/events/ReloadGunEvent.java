package com.vhbob.sguns.events;

import java.util.ArrayList;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.ItemStack;

import com.vhbob.sguns.SpigotGuns;
import com.vhbob.sguns.util.Gun;

public class ReloadGunEvent implements Listener {

	public static ArrayList<Player> inInventory = new ArrayList<>();

	@EventHandler
	public void onReload(PlayerDropItemEvent e) {
		Player p = e.getPlayer();
		ItemStack item = e.getItemDrop().getItemStack();
		// Check if we will allow drop
		if (item == null || !item.hasItemMeta() || inInventory.contains(p)) {
			// If dropping a reloading gun, cancel reload
			if (item != null && SpigotGuns.gunLinks.containsKey(item)) {
				SpigotGuns.gunLinks.get(item).stopReload();
			}
			return;
		}

		// If we found gun, try to reload
		if (SpigotGuns.gunLinks.containsKey(item)) {
			e.setCancelled(true);
			Gun gun = SpigotGuns.gunLinks.get(item);
			// Reload the gun using the players ammo
			if (gun.getBullets() < gun.getMaxBullets()) {
				gun.reload(e.getPlayer(), e.getItemDrop().getItemStack());
			}
		}

		// Cancel if item name is a guns
		for (String itemName : SpigotGuns.gunTemplates.keySet()) {
			if (item.getItemMeta().getDisplayName().contains(itemName)) {
				e.setCancelled(true);
				break;
			}
		}
	}

	@EventHandler
	public void invClose(InventoryCloseEvent e) {
		if (e.getPlayer() instanceof Player) {
			Player p = (Player) e.getPlayer();
			if (inInventory.contains(p))
				inInventory.remove(p);
		}
	}

	@EventHandler
	public void invOpen(InventoryOpenEvent e) {
		if (e.getPlayer() instanceof Player) {
			Player p = (Player) e.getPlayer();
			if (!inInventory.contains(p))
				inInventory.add(p);
		}

	}

	@EventHandler
	public void invClick(InventoryClickEvent e) {
		// Stop reload if clicked a reloading gun
		ItemStack i = e.getCurrentItem();
		if (i != null && SpigotGuns.gunLinks.containsKey(i) && SpigotGuns.gunLinks.get(i).isOnCooldown()) {
			SpigotGuns.gunLinks.get(i).stopReload();
			e.setCancelled(true);
		}
		if (e.getWhoClicked() instanceof Player) {
			Player p = (Player) e.getWhoClicked();
			if (!inInventory.contains(p)) {
				inInventory.add(p);
			}
		}
	}

	@EventHandler
	public void changeItem(PlayerItemHeldEvent e) {
		// Stop reload if swapped from a gun
		ItemStack i = e.getPlayer().getInventory().getItem(e.getPreviousSlot());
		if (i != null && SpigotGuns.gunLinks.containsKey(i)) {
			SpigotGuns.gunLinks.get(i).stopReload();
		}
	}

}
