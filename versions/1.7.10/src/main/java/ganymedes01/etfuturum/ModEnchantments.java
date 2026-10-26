package ganymedes01.etfuturum;

import baubles.common.lib.PlayerHandler;
import ganymedes01.etfuturum.compat.ModsList;
import ganymedes01.etfuturum.configuration.configs.ConfigEnchantsPotions;
import ganymedes01.etfuturum.configuration.configs.ConfigModCompat;
import ganymedes01.etfuturum.enchantment.FrostWalker;
import ganymedes01.etfuturum.enchantment.Mending;
import ganymedes01.etfuturum.enchantment.SwiftSneak;
import ganymedes01.etfuturum.core.utils.RegistrySafety;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;
import net.minecraftforge.event.entity.player.PlayerPickupXpEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

public class ModEnchantments {

	public static Enchantment frostWalker;
	public static Enchantment mending;
	public static Enchantment swiftSneak;
	public static Enchantment soulSpeed;
	public static Enchantment quickCharge, multishot, piercing, loyalty, impaling, riptide, channeling;
	public static int level(Enchantment enchantment, ItemStack stack) { return enchantment == null || stack == null ? 0 : EnchantmentHelper.getEnchantmentLevel(enchantment.effectId, stack); }

	private static final Map<EntityLivingBase, double[]> prevMoveCache = new WeakHashMap<>();

	public static void init() {
		RegistrySafety.requireVacant(Enchantment.enchantmentsList, ConfigEnchantsPotions.quickChargeID, "quick_charge"); quickCharge = new ganymedes01.etfuturum.enchantment.MobEquipmentEnchantment(ConfigEnchantsPotions.quickChargeID, "quick_charge", 3, 0);
		RegistrySafety.requireVacant(Enchantment.enchantmentsList, ConfigEnchantsPotions.multishotID, "multishot"); multishot = new ganymedes01.etfuturum.enchantment.MobEquipmentEnchantment(ConfigEnchantsPotions.multishotID, "multishot", 1, 0);
		RegistrySafety.requireVacant(Enchantment.enchantmentsList, ConfigEnchantsPotions.piercingID, "piercing"); piercing = new ganymedes01.etfuturum.enchantment.MobEquipmentEnchantment(ConfigEnchantsPotions.piercingID, "piercing", 4, 0);
		RegistrySafety.requireVacant(Enchantment.enchantmentsList, ConfigEnchantsPotions.loyaltyID, "loyalty"); loyalty = new ganymedes01.etfuturum.enchantment.MobEquipmentEnchantment(ConfigEnchantsPotions.loyaltyID, "loyalty", 3, 1);
		RegistrySafety.requireVacant(Enchantment.enchantmentsList, ConfigEnchantsPotions.impalingID, "impaling"); impaling = new ganymedes01.etfuturum.enchantment.MobEquipmentEnchantment(ConfigEnchantsPotions.impalingID, "impaling", 5, 1);
		RegistrySafety.requireVacant(Enchantment.enchantmentsList, ConfigEnchantsPotions.riptideID, "riptide"); riptide = new ganymedes01.etfuturum.enchantment.MobEquipmentEnchantment(ConfigEnchantsPotions.riptideID, "riptide", 3, 1);
		RegistrySafety.requireVacant(Enchantment.enchantmentsList, ConfigEnchantsPotions.channelingID, "channeling"); channeling = new ganymedes01.etfuturum.enchantment.MobEquipmentEnchantment(ConfigEnchantsPotions.channelingID, "channeling", 1, 1);
		 {
			RegistrySafety.requireVacant(Enchantment.enchantmentsList, ConfigEnchantsPotions.soulSpeedID, "Soul Speed");
			soulSpeed = new ganymedes01.etfuturum.enchantment.SoulSpeed();
		}
		 {
			RegistrySafety.requireVacant(Enchantment.enchantmentsList, ConfigEnchantsPotions.frostWalkerID, "Frost Walker");
			frostWalker = new FrostWalker();
		}
		 {
			RegistrySafety.requireVacant(Enchantment.enchantmentsList, ConfigEnchantsPotions.mendingID, "Mending");
			mending = new Mending();
		}
		 {
			RegistrySafety.requireVacant(Enchantment.enchantmentsList, ConfigEnchantsPotions.swiftSneakID, "Swift Sneak");
			swiftSneak = new SwiftSneak();
		}
	}

	// Frost Walker logic
	public static void onLivingUpdate(EntityLivingBase entity) {
		if (entity.worldObj.isRemote)
			return;
		if (ConfigEnchantsPotions.enableSoulSpeed) ganymedes01.etfuturum.enchantment.SoulSpeed.update(entity);
		if (!ConfigEnchantsPotions.enableFrostWalker)
			return;

		ItemStack boots = entity.getEquipmentInSlot(1);
		int level = 0;
		if ((level = EnchantmentHelper.getEnchantmentLevel(frostWalker.effectId, boots)) > 0 && entity.onGround) {
			double[] prevCoords = prevMoveCache.get(entity);
			if (prevCoords == null || Math.abs(prevCoords[0] - entity.posX) > 0.003D
					|| Math.abs(prevCoords[1] - entity.posZ) > 0.003D || prevCoords[2] != entity.posY) {
				int x = MathHelper.floor_double(entity.posX);
				int y = MathHelper.floor_double(entity.posY);
				int z = MathHelper.floor_double(entity.posZ);

				int radius = Math.min(16, 2 + level);

				for (int i = -radius; i <= radius; i++) {
					for (int j = -radius; j <= radius; j++) {
						if (i * i + j * j <= radius * radius) {
							Block block = entity.worldObj.getBlock(x + i, y - 1, z + j);
							Block blockUp = entity.worldObj.getBlock(x + i, y, z + j);
							if (!blockUp.isNormalCube() && blockUp.getMaterial() != Material.water && (block == Blocks.water || block == Blocks.flowing_water)) {
								if (entity.worldObj.getEntitiesWithinAABBExcludingEntity(entity, AxisAlignedBB.getBoundingBox(x + i, y - 1, z + j, x + i + 1, y, z + j + 1)).isEmpty()) {
									entity.worldObj.setBlock(x + i, y - 1, z + j, ModBlocks.FROSTED_ICE.get());
								}
							}
						}
					}
				}
				prevMoveCache.put(entity, new double[]{entity.posX, entity.posZ, entity.posY});
			}
		} else {
			prevMoveCache.remove(entity);
		}
	}

	// Mending logic
	public static void onPlayerPickupXP(PlayerPickupXpEvent event) {
		EntityPlayer player = event.entityPlayer;
		EntityXPOrb orb = event.orb;
		if (player.worldObj.isRemote)
			return;
		if (!ConfigEnchantsPotions.enableMending || orb.xpValue <= 0)
			return;

		ArrayList<ItemStack> stacks = new ArrayList<>();
		stacks.add(player.getCurrentEquippedItem()); // held
		stacks.add(player.getEquipmentInSlot(1)); // boots
		stacks.add(player.getEquipmentInSlot(2)); // leggings
		stacks.add(player.getEquipmentInSlot(3)); // chestplate
		stacks.add(player.getEquipmentInSlot(4)); // helmet
		if (ModsList.BAUBLES.isLoaded() && ConfigModCompat.baublesMending) {
			ItemStack[] baubles = PlayerHandler.getPlayerBaubles(player).stackList;
			if (baubles != null) {
				Collections.addAll(stacks, baubles);
			}
		}

		int xp = orb.xpValue;
		for (ItemStack stack : stacks)
			if (stack != null && stack.getItemDamage() > 0 && EnchantmentHelper.getEnchantmentLevel(mending.effectId, stack) > 0) {
				Item item = stack.getItem();
				if (item == null || !item.isRepairable()) continue;
				int repaired = (int) Math.min((long) xp * 2, stack.getItemDamage());
				stack.setItemDamage(stack.getItemDamage() - repaired);
				xp -= repaired / 2 + repaired % 2;
				if (xp <= 0) {
					orb.setDead();
					event.setCanceled(true);
					return;
				}
			}
		orb.xpValue = xp;
	}
}
