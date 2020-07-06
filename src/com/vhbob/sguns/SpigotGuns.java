package com.vhbob.sguns;

import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import com.vhbob.sguns.commands.GiveAmmo;
import com.vhbob.sguns.commands.GiveGun;
import com.vhbob.sguns.events.DeagleEvents;
import com.vhbob.sguns.events.DeathMessageEvent;
import com.vhbob.sguns.events.ReloadGunEvent;
import com.vhbob.sguns.events.ShootGunEvent;
import com.vhbob.sguns.events.StopOlympiaFallDamage;
import com.vhbob.sguns.events.ZoomEvent;
import com.vhbob.sguns.util.Gun;

import net.milkbowl.vault.economy.Economy;

public class SpigotGuns extends JavaPlugin {

	private static SpigotGuns inst;

	public static String prefix = ChatColor.GRAY + "[" + ChatColor.GOLD + "SpigotGuns" + ChatColor.GRAY + "]"
			+ ChatColor.RESET + ": ";

	public static String deagle;

	public static HashMap<String, ItemStack> itemTemplates;
	public static HashMap<String, Gun> gunTemplates;
	public static HashMap<ItemStack, Gun> gunLinks;
	public static HashMap<String, ItemStack> ammoTemplates;
	public static HashMap<ItemStack, Gun> deagleLeft, deagleRight;

	public static Sound shootSound, reloadSound;
	public static Effect shootEffect;

	public static Economy economy = null;

	@Override
	public void onEnable() {
		logMessage("Enabling the plugin...");
		inst = this;

		// Begin config stuff
		logMessage("Loading config...");
		saveDefaultConfig();
		shootSound = Sound.valueOf(getConfig().getString("sounds.shoot"));
		reloadSound = Sound.valueOf(getConfig().getString("sounds.reload"));

		// Load ammo from config
		logMessage("Loading ammo...");
		ItemStack arAmmo = itemFromConfig("ammo.ar");
		ItemStack shotgunAmmo = itemFromConfig("ammo.shotgun");
		ItemStack sniperAmmo = itemFromConfig("ammo.sniper");
		ItemStack smgAmmo = itemFromConfig("ammo.smg");
		ItemStack olympAmmo = itemFromConfig("ammo.olympia");
		ItemStack pistolAmmo = itemFromConfig("ammo.pistol");
		ammoTemplates = new HashMap<String, ItemStack>();
		ammoTemplates.put("ar", arAmmo);
		ammoTemplates.put("shotgun", shotgunAmmo);
		ammoTemplates.put("sniper", sniperAmmo);
		ammoTemplates.put("smg", smgAmmo);
		ammoTemplates.put("olympia", olympAmmo);
		ammoTemplates.put("pistol", pistolAmmo);

		// Load guns from config
		logMessage("Loading guns...");
		gunLinks = new HashMap<>();
		itemTemplates = new HashMap<String, ItemStack>();
		gunTemplates = new HashMap<>();
		deagleLeft = new HashMap<>();
		deagleRight = new HashMap<>();
		// Loop thru each gun
		for (String gunToken : getConfig().getConfigurationSection("guns").getKeys(false)) {
			logMessage("Loading " + gunToken + "...");
			String path = "guns." + gunToken;

			// Load gun item
			ItemStack gunItem = itemFromConfig(path);
			itemTemplates.put(gunToken.toLowerCase(), gunItem);

			// Load actual gun
			Gun gun = new Gun();
			gun.setMaxBullets(getConfig().getInt(path + ".mag"));
			gun.setBullets(getConfig().getInt(path + ".mag"));
			gun.setRange(getConfig().getDouble(path + ".range"));
			gun.setDamage(getConfig().getDouble(path + ".damage"));
			gun.setCooldown(getConfig().getDouble(path + ".cooldown"));
			gun.setZoom(getConfig().getInt(path + ".zoom"));
			gun.setReloadTime(getConfig().getDouble(path + ".reload"));

			// Load effect
			String effectStr = getConfig().getString(path + ".effect");
			// Check for byte
			if (!effectStr.contains("-")) {
				gun.setEffect(Effect.valueOf(effectStr));
				gun.setEffectData((byte) 0);
			} else {
				int ind = effectStr.indexOf("-");
				gun.setEffect(Effect.valueOf(effectStr.substring(0, ind)));
				String preParsed = effectStr.substring(ind + 1);
				byte data = Byte.parseByte(preParsed);
				gun.setEffectData(data);
			}

			gun.setID(gunToken);

			// Assign ammo to gun
			if (gunToken.equalsIgnoreCase("M4") || gunToken.equalsIgnoreCase("AK"))
				gun.setAmmo(arAmmo);
			else if (gunToken.equalsIgnoreCase("Tesla") || gunToken.equalsIgnoreCase("Gauss"))
				gun.setAmmo(shotgunAmmo);
			else if (gunToken.equalsIgnoreCase("Sniper"))
				gun.setAmmo(sniperAmmo);
			else if (gunToken.equalsIgnoreCase("Type95") || gunToken.equalsIgnoreCase("Minigun"))
				gun.setAmmo(smgAmmo);
			else if (gunToken.equalsIgnoreCase("Olympia"))
				gun.setAmmo(olympAmmo);
			else {
				deagle = gunItem.getItemMeta().getDisplayName();
				gun.setAmmo(pistolAmmo);
			}

			gunTemplates.put(gunItem.getItemMeta().getDisplayName(), gun);

		}

		// Load commands and events
		logMessage("Loading events and commands...");
		Bukkit.getPluginManager().registerEvents(new ZoomEvent(), this);
		Bukkit.getPluginManager().registerEvents(new ShootGunEvent(), this);
		Bukkit.getPluginManager().registerEvents(new ReloadGunEvent(), this);
		Bukkit.getPluginManager().registerEvents(new DeagleEvents(), this);
		Bukkit.getPluginManager().registerEvents(new DeathMessageEvent(), this);
		Bukkit.getPluginManager().registerEvents(new StopOlympiaFallDamage(), this);
		getCommand("GiveGun").setExecutor(new GiveGun());
		getCommand("GiveAmmo").setExecutor(new GiveAmmo());

		// Load eco
		logMessage("Loading economy...");
		setupEconomy();

	}

	@Override
	public void onDisable() {
		logMessage("Disabling the plugin...");
	}

	public static SpigotGuns getInst() {
		return inst;
	}

	public static void logMessage(String msg) {
		Bukkit.getConsoleSender().sendMessage(prefix + msg);
	}

	private ItemStack itemFromConfig(String path) {
		ItemStack i = new ItemStack(Material.valueOf(getConfig().getString(path + ".item")));
		ItemMeta im = i.getItemMeta();
		im.setDisplayName(ChatColor.translateAlternateColorCodes('&', getConfig().getString(path + ".name")));
		ArrayList<String> lore = new ArrayList<String>();
		for (String s : getConfig().getStringList(path + ".lore"))
			lore.add(ChatColor.translateAlternateColorCodes('&', s));
		im.setLore(lore);
		i.setItemMeta(im);
		return i;
	}

	private boolean setupEconomy() {
		RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager()
				.getRegistration(net.milkbowl.vault.economy.Economy.class);
		if (economyProvider != null) {
			economy = economyProvider.getProvider();
		}

		return (economy != null);
	}

}
