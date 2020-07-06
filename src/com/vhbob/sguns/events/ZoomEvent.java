package com.vhbob.sguns.events;

import java.util.ArrayList;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.vhbob.sguns.SpigotGuns;
import com.vhbob.sguns.util.Gun;

public class ZoomEvent implements Listener {

	private static ArrayList<Player> zoomed = new ArrayList<>();

	@EventHandler
	public void onZoom(PlayerInteractEvent e) {
		Player p = e.getPlayer();
		if (e.getAction().toString().contains("LEFT_CLICK") && p.getItemInHand() != null
				&& p.getItemInHand().hasItemMeta()) {
			// Get gun item base
			String base = "";
			for (String itemName : SpigotGuns.gunTemplates.keySet()) {
				if (p.getItemInHand().getItemMeta().getDisplayName().contains(itemName)) {
					base = itemName;
					break;
				}
			}

			// Get gun
			if (base != "") {
				Gun g = SpigotGuns.gunTemplates.get(base);
				if (g.getZoom() == 0 || g.getID().equalsIgnoreCase("deagle"))
					return;
				// Apply zoom if not zoomed, stop zooming if zooming
				if (zoomed.contains(p)) {
					zoomed.remove(p);
					p.removePotionEffect(PotionEffectType.SLOW);
				} else {
					zoomed.add(p);
					p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 999999, g.getZoom()));
				}
				e.setCancelled(true);
			}
		}
	}

	@EventHandler
	public void swapItem(PlayerItemHeldEvent e) {
		if (zoomed.contains(e.getPlayer())) {
			zoomed.remove(e.getPlayer());
			e.getPlayer().removePotionEffect(PotionEffectType.SLOW);
		}
	}

}
