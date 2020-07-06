package com.vhbob.sguns.events;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.inventory.ItemStack;

import com.vhbob.sguns.SpigotGuns;

public class StopOlympiaFallDamage implements Listener {

	@EventHandler
	public void onFallDamage(EntityDamageEvent e) {
		if (e.getEntityType().equals(EntityType.PLAYER) && e.getCause().equals(DamageCause.FALL)) {
			// Get player and stop if there is no item
			Player p = (Player) e.getEntity();
			if (p.getItemInHand() == null || !p.getItemInHand().hasItemMeta()) {
				return;
			}

			// Cancel if holding olympia
			ItemStack i = p.getItemInHand();
			if (i.getItemMeta().getDisplayName()
					.contains(SpigotGuns.itemTemplates.get("olympia").getItemMeta().getDisplayName()))
				e.setCancelled(true);
		}
	}

}
