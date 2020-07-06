package com.vhbob.sguns.events;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import com.vhbob.sguns.SpigotGuns;
import com.vhbob.sguns.util.Gun;

public class ShootGunEvent implements Listener {

	@EventHandler
	public void onShoot(PlayerInteractEvent e) {
		Player p = e.getPlayer();
		if (e.getAction().toString().contains("RIGHT_CLICK")) {
			ItemStack item = p.getInventory().getItemInHand();
			if (item == null || !item.hasItemMeta())
				return;
			String base = "";
			for (String itemName : SpigotGuns.gunTemplates.keySet()) {
				if (item.getItemMeta().getDisplayName().contains(itemName)) {
					base = itemName;
					break;
				}
			}
			if (base != "" && base != SpigotGuns.deagle) {
				// Create gun object if needed
				if (!SpigotGuns.gunLinks.containsKey(item)) {
					SpigotGuns.gunLinks.put(item, SpigotGuns.gunTemplates.get(base).clone());
				}
				Gun gun = SpigotGuns.gunLinks.get(item);
				if (gun.shoot(p)) {
					gun.updateAmmo(item);
				}
				e.setCancelled(true);
			}
		}
	}

	@EventHandler
	public void stopMelee(EntityDamageByEntityEvent e) {
		// If damaged is a player and type is melee, stop it
		if (e.getEntity() instanceof Player && e.getDamager() instanceof Player
				&& e.getCause() == DamageCause.ENTITY_ATTACK)
			e.setCancelled(true);
	}

}
