package ganymedes01.etfuturum.items;

import ganymedes01.etfuturum.EtFuturum;
import ganymedes01.etfuturum.entities.EntityTrident;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.*;
import net.minecraft.world.World;

public class ItemTrident extends Item {
	public ItemTrident() { setMaxStackSize(1); setMaxDamage(250); setUnlocalizedName("etfuturum.trident"); setTextureName("trident"); setCreativeTab(EtFuturum.creativeTabItems); }
	@Override public EnumAction getItemUseAction(ItemStack stack) { return EnumAction.bow; }
	@Override public int getMaxItemUseDuration(ItemStack stack) { return 72000; }
	@Override public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) { if (stack.getItemDamage() < stack.getMaxDamage() - 1) player.setItemInUse(stack, getMaxItemUseDuration(stack)); return stack; }
	@Override public void onPlayerStoppedUsing(ItemStack stack, World world, EntityPlayer player, int remaining) {
		if (world.isRemote || getMaxItemUseDuration(stack) - remaining < 10 || stack.getItemDamage() >= stack.getMaxDamage() - 1) return;
		int riptide = ganymedes01.etfuturum.ModEnchantments.level(ganymedes01.etfuturum.ModEnchantments.riptide, stack);
		if (riptide > 0) { if (!player.isWet()) return; net.minecraft.util.Vec3 look = player.getLookVec(); float speed = .6F + .6F * riptide; player.addVelocity(look.xCoord * speed, look.yCoord * speed + (player.onGround ? .4 : 0), look.zCoord * speed); player.velocityChanged = true; stack.damageItem(1, player); return; }
		ItemStack thrown = stack.copy(); thrown.stackSize = 1;
		if (!player.capabilities.isCreativeMode) thrown.damageItem(1, player);
		EntityTrident trident = new EntityTrident(world, player, player.capabilities.isCreativeMode ? null : thrown);
		if (world.spawnEntityInWorld(trident)) { if (!player.capabilities.isCreativeMode) player.inventory.setInventorySlotContents(player.inventory.currentItem, null); world.playSoundAtEntity(player, "random.bow", 1, .8F); }
	}
	@Override public boolean hitEntity(ItemStack stack, EntityLivingBase target, EntityLivingBase attacker) { stack.damageItem(1, attacker); return true; }
	@Override public int getItemEnchantability() { return 1; }
	@Override public com.google.common.collect.Multimap getItemAttributeModifiers() {
		com.google.common.collect.Multimap attributes = super.getItemAttributeModifiers();
		attributes.put(net.minecraft.entity.SharedMonsterAttributes.attackDamage.getAttributeUnlocalizedName(), new net.minecraft.entity.ai.attributes.AttributeModifier(field_111210_e, "Weapon modifier", 8, 0)); return attributes;
	}
}
