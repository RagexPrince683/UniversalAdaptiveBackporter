package ganymedes01.etfuturum.blocks.itemblocks;

import ganymedes01.etfuturum.ModBlocks;
import ganymedes01.etfuturum.blocks.BlockAquaticPlant;
import ganymedes01.etfuturum.blocks.BlockSeaPickle;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public final class ItemBlockSeaPickle extends ItemBlock {
	public ItemBlockSeaPickle(Block block) { super(block); }
	@Override public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float hx, float hy, float hz) {
		if (world.getBlock(x, y, z) instanceof BlockSeaPickle && stack.stackSize > 0 && player.canPlayerEdit(x, y, z, side, stack)) {
			int count = world.getBlockMetadata(x, y, z) & 3;
			if (count < 3) {
				if (!world.isRemote) { world.setBlockMetadataWithNotify(x, y, z, count + 1, 3); world.func_147451_t(x, y, z); if (!player.capabilities.isCreativeMode) stack.stackSize--; }
				return true;
			}
		}
		return super.onItemUse(stack, player, world, x, y, z, side, hx, hy, hz);
	}
	@Override public boolean placeBlockAt(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float hx, float hy, float hz, int meta) {
		Block block = BlockAquaticPlant.sourceWater(world, x, y, z) ? ModBlocks.SEA_PICKLE_UNDERWATER.get() : ModBlocks.SEA_PICKLE.get();
		return world.setBlock(x, y, z, block, 0, 3);
	}
}
