package ganymedes01.etfuturum.items;

import ganymedes01.etfuturum.entities.EntityTadpole;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBucket;
import net.minecraft.item.ItemStack;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;

public class ItemTadpoleBucket extends ItemBucket {
	private final ThreadLocal<Placement> placement = new ThreadLocal<>();
	private static class Placement { boolean success; int x, y, z; }
	public ItemTadpoleBucket() { super(Blocks.flowing_water); setUnlocalizedName("etfuturum.tadpole_bucket"); setTextureName("tadpole_bucket"); setMaxStackSize(1); setCreativeTab(ganymedes01.etfuturum.EtFuturum.creativeTabItems); }
	@Override public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {
		Placement attempt = new Placement(); placement.set(attempt);
		try {
			ItemStack result = super.onItemRightClick(stack, world, player);
			if (attempt.success && !world.isRemote) {
				EntityTadpole tadpole = new EntityTadpole(world); tadpole.setLocationAndAngles(attempt.x + 0.5, attempt.y + 0.1, attempt.z + 0.5, 0, 0); tadpole.setFromBucket(stack.hasTagCompound() ? stack.getTagCompound().getInteger("Age") : 0);
				if (stack.hasDisplayName()) tadpole.setCustomNameTag(stack.getDisplayName()); world.spawnEntityInWorld(tadpole);
			}
			return result;
		} finally { placement.remove(); }
	}
	@Override public boolean tryPlaceContainedLiquid(World world, int x, int y, int z) {
		boolean success = super.tryPlaceContainedLiquid(world, x, y, z);
		Placement attempt = placement.get();
		if (success && attempt != null) { attempt.success = true; attempt.x = x; attempt.y = y; attempt.z = z; }
		return success;
	}
}
