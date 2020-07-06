package com.vhbob.sguns.events;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import com.vhbob.sguns.SpigotGuns;
import com.vhbob.sguns.util.Gun;

public class DeagleEvents implements Listener {

	@EventHandler
	public void onShoot(PlayerInteractEvent e) {
		if (e.getAction().toString().contains("LEFT_CLICK") || e.getAction().toString().contains("RIGHT_CLICK")) {
			Player p = e.getPlayer();
			// Stop if the item doesnt exist
			ItemStack item = p.getInventory().getItemInHand();
			if (item == null || !item.hasItemMeta())
				return;

			if (item.getItemMeta().getDisplayName().contains(SpigotGuns.deagle)) {
				// pick left gun if left click
				Gun leftGun, rightGun;

				// Generate guns if needed
				if (!SpigotGuns.deagleLeft.containsKey(item) || !SpigotGuns.deagleRight.containsKey(item)) {
					SpigotGuns.deagleLeft.put(item, SpigotGuns.gunTemplates.get(SpigotGuns.deagle).clone());
					SpigotGuns.deagleRight.put(item, SpigotGuns.gunTemplates.get(SpigotGuns.deagle).clone());
				}

				leftGun = SpigotGuns.deagleLeft.get(item);
				rightGun = SpigotGuns.deagleRight.get(item);

				// Shoot proper gun
				if (e.getAction().toString().contains("LEFT_CLICK")) {
					if (leftGun.shoot(p)) {
						Gun.updateDeagleAmmo(item, leftGun, rightGun);
					}
				} else {
					if (rightGun.shoot(p)) {
						Gun.updateDeagleAmmo(item, leftGun, rightGun);
					}
				}
				e.setCancelled(true);
			}
		}
	}

	@EventHandler
	public void onReload(PlayerDropItemEvent e) {
		// Make sure its a valid item
		ItemStack item = e.getItemDrop().getItemStack();
		if (item == null || !item.hasItemMeta() || ReloadGunEvent.inInventory.contains(e.getPlayer())) {
			// Cancel reload if needed
			if (item != null && SpigotGuns.gunLinks.containsKey(item)) {
				SpigotGuns.gunLinks.get(item).stopReload();
			}
			return;
		}

		// If it is a loaded gun
		if (SpigotGuns.deagleRight.containsKey(item)) {
			// Get guns
			Gun leftGun = SpigotGuns.deagleLeft.get(item);
			Gun rightGun = SpigotGuns.deagleRight.get(item);

			// Reload guns
			if (leftGun.getBullets() < leftGun.getMaxBullets())
				leftGun.reload(e.getPlayer(), e.getItemDrop().getItemStack());
			if (rightGun.getBullets() < rightGun.getMaxBullets())
				rightGun.reload(e.getPlayer(), e.getItemDrop().getItemStack());

			// Update deagle
			Gun.updateDeagleAmmo(item, leftGun, rightGun);

			e.setCancelled(true);
		}
	}

}
