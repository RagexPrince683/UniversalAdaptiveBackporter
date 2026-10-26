package ganymedes01.etfuturum.items;

import ganymedes01.etfuturum.configuration.ContentAvailability;
import ganymedes01.etfuturum.tileentities.TileEntityBrushable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.*;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;

public final class ItemBrush extends BaseItem {
	public ItemBrush() { super("brush"); setMaxStackSize(1); setMaxDamage(64); }
	@Override public EnumAction getItemUseAction(ItemStack stack) { return EnumAction.bow; }
	@Override public int getMaxItemUseDuration(ItemStack stack) { return 72000; }
	@Override public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {
		if (ContentAvailability.enabled("brushes") && ContentAvailability.enabled("archaeology")) player.setItemInUse(stack, getMaxItemUseDuration(stack));
		return stack;
	}
	@Override public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float hx, float hy, float hz) {
		if (!(world.getTileEntity(x, y, z) instanceof TileEntityBrushable) || !ContentAvailability.enabled("archaeology") || !player.canPlayerEdit(x, y, z, side, stack)) return false;
		player.setItemInUse(stack, getMaxItemUseDuration(stack)); return true;
	}
	@Override public void onUsingTick(ItemStack stack, EntityPlayer player, int remaining) {
		if (player.worldObj.isRemote || (getMaxItemUseDuration(stack) - remaining) % 10 != 5) return;
		if (!ContentAvailability.enabled("brushes") || !ContentAvailability.enabled("archaeology")) { player.stopUsingItem(); return; }
		MovingObjectPosition hit = getMovingObjectPositionFromPlayer(player.worldObj, player, false);
		if (hit == null || hit.typeOfHit != MovingObjectPosition.MovingObjectType.BLOCK || !player.canPlayerEdit(hit.blockX, hit.blockY, hit.blockZ, hit.sideHit, stack)) return;
		if (player.worldObj.getTileEntity(hit.blockX, hit.blockY, hit.blockZ) instanceof TileEntityBrushable tile && tile.brush(hit.sideHit)) {
			stack.damageItem(1, player); player.stopUsingItem();
		}
	}
}
