package com.vhbob.sguns.util;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Effect;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import com.vhbob.sguns.SpigotGuns;
import com.vhbob.sguns.commands.GiveGun;
import com.vhbob.sguns.events.DeathMessageEvent;

public class Gun {

	private ItemStack ammo;
	private int bullets, maxBullets, zoom;
	private double damage, range, cooldown, reloadTime;
	private boolean onCooldown;
	private byte effectData;
	private String ID;
	private Effect shootEffect;
	private BukkitTask reloadRunnable;

	// Constructor for a gun
	public Gun() {

	}

	public Gun(int maxBullets, double damage, double range, double cooldown, ItemStack ammo, int zoom, String ID,
			double reloadTime, Effect shoot, byte effectData) {
		this.maxBullets = maxBullets;
		this.bullets = maxBullets;
		this.damage = damage;
		this.range = range;
		this.cooldown = cooldown;
		this.ammo = ammo;
		this.zoom = zoom;
		this.setID(ID);
		this.reloadTime = reloadTime;
		this.shootEffect = shoot;
		this.effectData = effectData;
	}

	// Setters and Getters
	public int getMaxBullets() {
		return maxBullets;
	}

	public void setMaxBullets(int maxBullets) {
		this.maxBullets = maxBullets;
	}

	public int getBullets() {
		return bullets;
	}

	public void setBullets(int bullets) {
		this.bullets = bullets;
	}

	public ItemStack getAmmo() {
		return ammo;
	}

	public void setAmmo(ItemStack ammo) {
		this.ammo = ammo;
	}

	public double getDamage() {
		return damage;
	}

	public void setDamage(double damage) {
		this.damage = damage;
	}

	public double getRange() {
		return range;
	}

	public void setRange(double range) {
		this.range = range;
	}

	public double getCooldown() {
		return cooldown;
	}

	public void setCooldown(double cooldown) {
		this.cooldown = cooldown;
	}

	public int getZoom() {
		return zoom;
	}

	public void setZoom(int zoom) {
		this.zoom = zoom;
	}

	public String getID() {
		return ID;
	}

	public void setID(String iD) {
		ID = iD;
	}

	public double getReloadTime() {
		return reloadTime;
	}

	public void setReloadTime(double reloadTime) {
		this.reloadTime = reloadTime;
	}

	public void setEffect(Effect effect) {
		this.shootEffect = effect;
	}

	public byte getEffectData() {
		return effectData;
	}

	public void setEffectData(byte effectData) {
		this.effectData = effectData;
	}

	public boolean isOnCooldown() {
		return onCooldown;
	}

	// This method will reload a gun based off a players inventory
	public void reload(Player holder, ItemStack item) {

		// If gun isnt on cooldown and we need to reload
		if (!onCooldown && bullets < maxBullets) {
			String fullMag = "ak m4 minigun type95 deagle";

			// If fullmag, reload gun and set it on cooldown
			if (fullMag.contains(ID.toLowerCase())) {
				if (usedBullets(holder)) {
					bullets = maxBullets;
					holder.getWorld().playSound(holder.getLocation(), SpigotGuns.reloadSound, 1, 1);
					updateAmmo(item);
					cooldownOn(reloadTime);
				}
			}
			// Else setup timer for reloading
			else {
				cooldownOn(reloadTime);
				reloadRunnable = new BukkitRunnable() {

					@Override
					public void run() {
						// Try to use bullets
						if (bullets < maxBullets && usedBullets(holder)) {
							bullets += 1;
							holder.getWorld().playSound(holder.getLocation(), SpigotGuns.reloadSound, 1, 1);
							updateAmmo(holder.getItemInHand());
							cooldownOn(reloadTime);
						} else {
							// Out of bullets or at max
							stopReload();
						}
					}
				}.runTaskTimer(SpigotGuns.getInst(), (long) (reloadTime * 20), (long) (reloadTime * 20));
			}

		}

	}

	// This method will check for, and consume ammo
	private boolean usedBullets(Player holder) {
		for (ItemStack i : holder.getInventory().getContents()) {
			if (i != null && i.hasItemMeta()
					&& i.getItemMeta().getDisplayName().equals(ammo.getItemMeta().getDisplayName())) {
				if (i.getAmount() == 1)
					holder.getInventory().removeItem(i);
				else
					i.setAmount(i.getAmount() - 1);
				return true;
			}
		}

		return false;
	}

	// This method will stop a gun's reload
	public void stopReload() {
		if (reloadRunnable != null) {
			reloadRunnable.cancel();
			reloadRunnable = null;
		}
	}

	// This method will update a gun item with the correct ammo
	public void updateAmmo(ItemStack gunItem) {
		if (gunItem.hasItemMeta() && gunItem.getItemMeta().hasLore()) {
			String base = "";
			for (String itemName : SpigotGuns.gunTemplates.keySet()) {
				if (gunItem.getItemMeta().getDisplayName().contains(itemName)) {
					base = itemName;
					break;
				}
			}
			if (base != "") {
				SpigotGuns.gunLinks.remove(gunItem);
				List<String> lore = gunItem.getItemMeta().getLore();
				int i = lore.size() - 2;
				lore.set(i + 1, GiveGun.toInvis(UUID.randomUUID().toString()));
				ItemMeta gunMeta = gunItem.getItemMeta();
				gunMeta.setDisplayName(base + " <" + bullets + ">");
				gunMeta.setLore(lore);
				gunItem.setItemMeta(gunMeta);
				SpigotGuns.gunLinks.put(gunItem, this);
			}
		}
	}

	// This method will make the gun shoot, returns true if the gun can and did
	// shoot
	public boolean shoot(Player holder) {

		if (bullets > 0 && reloadRunnable != null) {
			stopReload();
		}

		if (bullets > 0 && !onCooldown) {
			bullets--;

			// Find targeted entity
			LivingEntity current = null;

			// Get raycast from player
			RayTrace ray = new RayTrace(holder.getEyeLocation().toVector(), holder.getEyeLocation().getDirection());

			// Setup support
			double support = SpigotGuns.getInst().getConfig().getDouble("guns." + ID + ".support");
			Vector supportVector = new Vector(support, support, support);

			// Get entities in the raycast
			ArrayList<LivingEntity> currents = new ArrayList<>();
			for (Entity e : holder.getNearbyEntities(range, range, range)) {
				if (e instanceof LivingEntity && holder.hasLineOfSight(e)) {

					Location eLoc = e.getLocation();

					// Create target bounding box
					BoundingBox bb = new BoundingBox(e);

					// Add inverse of targets velocity to bounding box

					Vector invVelocity = e.getVelocity().multiply(-5);
					if (invVelocity.getX() < 0)
						bb.min = bb.min.add(new Vector(invVelocity.getX(), 0, 0));
					else
						bb.max = bb.max.add(new Vector(invVelocity.getX(), 0, 0));
					if (invVelocity.getY() < 0)
						bb.min = bb.min.add(new Vector(0, invVelocity.getY(), 0));
					else
						bb.max = bb.max.add(new Vector(0, invVelocity.getY(), 0));
					if (invVelocity.getZ() < 0)
						bb.min = bb.min.add(new Vector(0, 0, invVelocity.getZ()));
					else
						bb.max = bb.max.add(new Vector(0, 0, invVelocity.getZ()));

					// Add support

					bb.max = bb.max.add(supportVector);
					bb.min = bb.min.add(supportVector.clone().multiply(-1));

					// If the raycast hits the hitbox, set this entity to the target
					if (ray.intersects(bb, holder.getLocation().distance(eLoc) + 1, .001)) {
						currents.add((LivingEntity) e);
						if (current == null || holder.getLocation().distance(eLoc) < holder.getLocation()
								.distance(current.getLocation())) {
							current = (LivingEntity) e;
						}
					}
				}
			}

			// Shoot based on type
			if (!ID.toLowerCase().equalsIgnoreCase("olympia")) {

				// Damage entity
				if (current != null) {

					// Check for headshot if its a sniper
					if (ID.toLowerCase().equalsIgnoreCase("sniper")) {

						// Create hitbox
						LivingEntity headHitbox = (LivingEntity) current.getWorld()
								.spawnEntity(new Location(current.getWorld(), 0, 0, 0), EntityType.RABBIT);
						headHitbox.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 1000, 1));
						headHitbox.teleport(current.getEyeLocation());

						// Create bounding box
						BoundingBox bb = new BoundingBox(headHitbox);

						// Add inverse of targets velocity to bounding box

						Vector invVelocity = current.getVelocity().multiply(-5);
						if (invVelocity.getX() < 0)
							bb.min = bb.min.add(new Vector(invVelocity.getX(), 0, 0));
						else
							bb.max = bb.max.add(new Vector(invVelocity.getX(), 0, 0));
						if (invVelocity.getY() < 0)
							bb.min = bb.min.add(new Vector(0, invVelocity.getY(), 0));
						else
							bb.max = bb.max.add(new Vector(0, invVelocity.getY(), 0));
						if (invVelocity.getZ() < 0)
							bb.min = bb.min.add(new Vector(0, 0, invVelocity.getZ()));
						else
							bb.max = bb.max.add(new Vector(0, 0, invVelocity.getZ()));

						// Add support

						bb.max = bb.max.add(supportVector.clone().multiply(1));
						bb.min = bb.min.add(supportVector.clone().multiply(-1));

						// Check if raycast intersects the hitbox
						if (ray.intersects(bb, holder.getLocation().distance(headHitbox.getLocation()), .001)) {
							checkDeath(current, damage * 10.0 / 3.0, holder);
							current.damage(damage * 10.0 / 3.0);

							// Create firework
							Firework firework = (Firework) current.getWorld().spawnEntity(current.getEyeLocation(),
									EntityType.FIREWORK);

							FireworkMeta fm = firework.getFireworkMeta();
							fm.setPower(2);
							fm.addEffect(FireworkEffect.builder().withColor(Color.RED).trail(true).build());

							firework.setFireworkMeta(fm);

						} else {
							checkDeath(current, damage * 2, holder);
							current.damage(damage * 2);
						}
						headHitbox.remove();
					} else {
						checkDeath(current, damage * 2, holder);
						current.damage(damage * 2);
					}

					// Apply knockback
					Vector currVel = current.getVelocity();
					Vector kbVel = holder.getLocation().getDirection().multiply(0.5);
					kbVel.setY(0.3);
					currVel.add(kbVel);
					current.setVelocity(currVel);

					// Armor damaging
					if (current instanceof Player) {
						Player p = (Player) current;
						for (ItemStack armor : p.getInventory().getArmorContents()) {
							if (armor != null)
								armor.setDurability((short) (armor.getDurability() + 1));

						}
					}

					// Death messages
					if (current.getHealth() <= 0) {
						SpigotGuns.economy.depositPlayer(holder, SpigotGuns.getInst().getConfig().getDouble("reward"));
					}

				}

			} else {
				// Olympia movement
				Vector vel = holder.getVelocity();
				vel.add(holder.getLocation().getDirection().multiply(-1 * damage));
				holder.setVelocity(vel);
				// Movement for other entity as well
				if (!currents.isEmpty()) {
					for (LivingEntity curr : currents) {
						Vector cVel = curr.getVelocity();
						cVel.add(holder.getLocation().getDirection().multiply(damage));
						curr.setVelocity(cVel);

					}
				}
			}

			// Create particles and sound
			Location effectLoc = holder.getEyeLocation()
					.add(holder.getLocation().getDirection().add(new Vector(0.25, 0, 0.25)));
			for (int i = 0; i < SpigotGuns.getInst().getConfig().getInt("guns." + ID + ".particles"); ++i)
				holder.getWorld().playEffect(effectLoc, shootEffect, effectData, 10);
			holder.getWorld().playSound(holder.getLocation(), SpigotGuns.shootSound, 1, 1);

			// Schedule cooldown
			cooldownOn(cooldown);
			return true;
		}
		return false;
	}

	// This method will check for a death message
	private void checkDeath(Entity current, double dmg, Player killer) {
		if (current instanceof Player) {
			Player p = (Player) current;
			if (p.getHealth() - dmg <= 0) {
				DeathMessageEvent
						.addDeathMessage(
								ChatColor
										.translateAlternateColorCodes('&',
												"&8" + current.getName() + "&7 is vermoord door &8" + killer.getName()
														+ "&7 met een &8" + SpigotGuns.itemTemplates
																.get(ID.toLowerCase()).getItemMeta().getDisplayName()),
								p);
			}
		}
	}

	// This method will update a deagle item
	public static void updateDeagleAmmo(ItemStack deagleItem, Gun leftGun, Gun rightGun) {
		if (deagleItem.hasItemMeta() && deagleItem.getItemMeta().hasLore()) {
			if (deagleItem.getItemMeta().getDisplayName().contains(SpigotGuns.deagle)) {
				SpigotGuns.deagleLeft.remove(deagleItem);
				SpigotGuns.deagleRight.remove(deagleItem);
				List<String> lore = deagleItem.getItemMeta().getLore();
				int i = lore.size() - 2;
				lore.set(i + 1, GiveGun.toInvis(UUID.randomUUID().toString()));
				ItemMeta gunMeta = deagleItem.getItemMeta();
				gunMeta.setDisplayName(
						SpigotGuns.deagle + " <" + leftGun.getBullets() + " | " + rightGun.getBullets() + ">");
				gunMeta.setLore(lore);
				deagleItem.setItemMeta(gunMeta);
				SpigotGuns.deagleLeft.put(deagleItem, leftGun);
				SpigotGuns.deagleRight.put(deagleItem, rightGun);
			}
		}
	}

	private void cooldownOn(double time) {
		onCooldown = true;
		// Schedule next shot
		new BukkitRunnable() {

			@Override
			public void run() {
				onCooldown = false;
			}
		}.runTaskLater(SpigotGuns.getInst(), (long) (time * 20));
	}

	// This method will return a clone of the current gun
	public Gun clone() {
		Gun gun = new Gun(maxBullets, damage, range, cooldown, ammo, zoom, ID, reloadTime, shootEffect, effectData);
		return gun;
	}

}
