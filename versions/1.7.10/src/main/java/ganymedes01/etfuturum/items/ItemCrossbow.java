package ganymedes01.etfuturum.items;

import ganymedes01.etfuturum.EtFuturum;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.init.Items;
import net.minecraft.item.*;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

public class ItemCrossbow extends Item {
	public ItemCrossbow() { setUnlocalizedName("etfuturum.crossbow"); setTextureName("crossbow_standby"); setMaxStackSize(1); setMaxDamage(465); setCreativeTab(EtFuturum.creativeTabItems); }
	public static boolean charged(ItemStack stack) { return stack.hasTagCompound() && stack.getTagCompound().getBoolean("Charged"); }
	public static int chargeTime(ItemStack stack) { return 25 - 5 * Math.min(3, ganymedes01.etfuturum.ModEnchantments.level(ganymedes01.etfuturum.ModEnchantments.quickCharge, stack)); }
	@cpw.mods.fml.relauncher.SideOnly(cpw.mods.fml.relauncher.Side.CLIENT) public net.minecraft.util.IIcon getChargingIcon(ItemStack stack, float ticks) { return pulling[Math.min(2, Math.max(0, (int) (ticks * 3 / chargeTime(stack))))]; }
	@cpw.mods.fml.relauncher.SideOnly(cpw.mods.fml.relauncher.Side.CLIENT) private net.minecraft.util.IIcon loaded;
	@cpw.mods.fml.relauncher.SideOnly(cpw.mods.fml.relauncher.Side.CLIENT) private net.minecraft.util.IIcon[] pulling;
	@Override @cpw.mods.fml.relauncher.SideOnly(cpw.mods.fml.relauncher.Side.CLIENT) public void registerIcons(net.minecraft.client.renderer.texture.IIconRegister icons) { super.registerIcons(icons); loaded = icons.registerIcon("crossbow_arrow"); pulling = new net.minecraft.util.IIcon[]{icons.registerIcon("crossbow_pulling_0"), icons.registerIcon("crossbow_pulling_1"), icons.registerIcon("crossbow_pulling_2")}; }
	@Override @cpw.mods.fml.relauncher.SideOnly(cpw.mods.fml.relauncher.Side.CLIENT) public net.minecraft.util.IIcon getIcon(ItemStack stack, int pass) { return charged(stack) ? loaded : itemIcon; }
	@Override @cpw.mods.fml.relauncher.SideOnly(cpw.mods.fml.relauncher.Side.CLIENT) public net.minecraft.util.IIcon getIcon(ItemStack stack, int pass, EntityPlayer player, ItemStack using, int remaining) { if (charged(stack)) return loaded; if (using == stack) return pulling[Math.min(2, (getMaxItemUseDuration(stack) - remaining) * 3 / chargeTime(stack))]; return itemIcon; }
	@Override public EnumAction getItemUseAction(ItemStack stack) { return EnumAction.bow; }
	@Override public int getMaxItemUseDuration(ItemStack stack) { return 72000; }
	@Override public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {
		if (charged(stack)) {
			if (!world.isRemote) {
				int count = ganymedes01.etfuturum.ModEnchantments.level(ganymedes01.etfuturum.ModEnchantments.multishot, stack) > 0 ? 3 : 1;
				boolean fired = false;
				for (int i = 0; i < count; i++) { ganymedes01.etfuturum.entities.EntityCrossbowBolt arrow = new ganymedes01.etfuturum.entities.EntityCrossbowBolt(world, player, ganymedes01.etfuturum.ModEnchantments.level(ganymedes01.etfuturum.ModEnchantments.piercing, stack), i == 0 ? 0 : i == 1 ? -10 : 10); arrow.canBePickedUp = i != 0 || stack.getTagCompound().getBoolean("CreativeCharge") ? 2 : 1; fired |= world.spawnEntityInWorld(arrow); }
				if (fired) { stack.getTagCompound().setBoolean("Charged", false); stack.damageItem(count, player); world.playSoundAtEntity(player, ganymedes01.etfuturum.Tags.MC_ASSET_VER + ":item.crossbow.shoot", 1, .8F); }
			}
		} else if (player.capabilities.isCreativeMode || player.inventory.hasItem(Items.arrow)) player.setItemInUse(stack, getMaxItemUseDuration(stack));
		return stack;
	}
	@Override public void onPlayerStoppedUsing(ItemStack stack, World world, EntityPlayer player, int remaining) {
		if (world.isRemote || charged(stack) || getMaxItemUseDuration(stack) - remaining < chargeTime(stack)) return;
		if (!player.capabilities.isCreativeMode && !player.inventory.consumeInventoryItem(Items.arrow)) return;
		if (!stack.hasTagCompound()) stack.setTagCompound(new NBTTagCompound());
		stack.getTagCompound().setBoolean("Charged", true); stack.getTagCompound().setBoolean("CreativeCharge", player.capabilities.isCreativeMode);
		world.playSoundAtEntity(player, ganymedes01.etfuturum.Tags.MC_ASSET_VER + ":item.crossbow.loading_end", 1, 1);
	}
	@Override public int getItemEnchantability() { return 1; }
}
