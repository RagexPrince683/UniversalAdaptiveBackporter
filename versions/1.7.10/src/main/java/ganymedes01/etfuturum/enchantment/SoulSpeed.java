package ganymedes01.etfuturum.enchantment;

import ganymedes01.etfuturum.ModBlocks;
import ganymedes01.etfuturum.ModEnchantments;
import ganymedes01.etfuturum.configuration.configs.ConfigEnchantsPotions;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.EnumEnchantmentType;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MathHelper;
import java.util.UUID;

public class SoulSpeed extends Enchantment {
	private static final UUID SPEED_ID = UUID.fromString("b10c9628-e8b7-4a56-b928-891b21aa491d");
	public SoulSpeed() { super(ConfigEnchantsPotions.soulSpeedID, 1, EnumEnchantmentType.armor_feet); setName("soul_speed"); }
	@Override public int getMaxLevel() { return 3; }
	@Override public int getMinEnchantability(int level) { return level * 10; }
	@Override public int getMaxEnchantability(int level) { return getMinEnchantability(level) + 15; }
	@Override public boolean canApplyAtEnchantingTable(ItemStack stack) { return false; }
	public static int level(EntityLivingBase entity) { return ModEnchantments.soulSpeed == null ? 0 : EnchantmentHelper.getEnchantmentLevel(ModEnchantments.soulSpeed.effectId, entity.getEquipmentInSlot(1)); }
	public static void update(EntityLivingBase entity) {
		IAttributeInstance speed = entity.getEntityAttribute(SharedMonsterAttributes.movementSpeed);
		if (speed == null) return;
		AttributeModifier previous = speed.getModifier(SPEED_ID);
		int level = level(entity);
		boolean active = false;
		if (level > 0 && entity.onGround) {
			int x = MathHelper.floor_double(entity.posX), y = MathHelper.floor_double(entity.boundingBox.minY - 0.1), z = MathHelper.floor_double(entity.posZ);
			if (entity.worldObj.blockExists(x, y, z)) {
				net.minecraft.block.Block block = entity.worldObj.getBlock(x, y, z);
				active = block == Blocks.soul_sand || ModBlocks.SOUL_SOIL.isEnabled() && block == ModBlocks.SOUL_SOIL.get();
			}
		}
		double amount = 0.03 * (1 + level * 0.35);
		if (previous != null && (!active || previous.getAmount() != amount)) { speed.removeModifier(previous); previous = null; }
		if (active && previous == null) speed.applyModifier(new AttributeModifier(SPEED_ID, "Soul speed", amount, 0).setSaved(false));
		if (active && entity.ticksExisted % 5 == 0 && entity.getRNG().nextInt(5) == 0 && (entity.posX != entity.prevPosX || entity.posZ != entity.prevPosZ)) {
			ItemStack boots = entity.getEquipmentInSlot(1); if (boots != null) boots.damageItem(1, entity);
		}
	}
}
