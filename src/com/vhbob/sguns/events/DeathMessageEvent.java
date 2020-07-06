package com.vhbob.sguns.events;

import java.util.HashMap;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

public class DeathMessageEvent implements Listener {

	private static HashMap<Player, String> deathMessages;

	public DeathMessageEvent() {
		deathMessages = new HashMap<Player, String>();
	}

	@EventHandler
	public void onDeath(PlayerDeathEvent e) {
		if (deathMessages.containsKey(e.getEntity())) {
			e.setDeathMessage(deathMessages.remove(e.getEntity()));
		}
	}

	public static void addDeathMessage(String s, Player p) {
		deathMessages.put(p, s);
		System.out.println(s);
	}

}
