package ganymedes01.etfuturum.items;

import ganymedes01.etfuturum.EtFuturum;
import ganymedes01.etfuturum.entities.*;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemBucket;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

/** Delegates ray tracing, permissions, Forge bucket events and evaporation to ItemBucket. */
public class ItemFishBucket extends ItemBucket {
	private final int type;
	private final ThreadLocal<Placement> placement = new ThreadLocal<>();
	private static class Placement { boolean success; int x, y, z; }
	public ItemFishBucket(int type, String name) {
		super(Blocks.flowing_water); this.type = type; setMaxStackSize(1); setUnlocalizedName("etfuturum." + name); setTextureName(name); setCreativeTab(EtFuturum.creativeTabItems);
	}
	@Override public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {
		Placement attempt = new Placement(); placement.set(attempt);
		try {
			ItemStack result = super.onItemRightClick(stack, world, player);
			if (attempt.success && !world.isRemote && !world.provider.isHellWorld) {
				EntityLiving fish = switch (type) { case 1 -> new EntitySalmon(world); case 2 -> new EntityTropicalFish(world); case 3 -> new EntityPufferfish(world); case 4 -> new EntityAxolotl(world); default -> new EntityCod(world); };
				fish.setLocationAndAngles(attempt.x + .5, attempt.y + .2, attempt.z + .5, 0, 0);
				NBTTagCompound tag = stack.hasTagCompound() ? stack.getTagCompound() : new NBTTagCompound();
				if (fish instanceof EntityFish fishEntity) fishEntity.readBucket(tag); else ((EntityAxolotl) fish).readBucket(tag);
				if (stack.hasDisplayName()) fish.setCustomNameTag(stack.getDisplayName());
				if (!world.spawnEntityInWorld(fish)) return stack;
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
