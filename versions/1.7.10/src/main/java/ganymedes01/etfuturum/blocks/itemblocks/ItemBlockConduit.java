package ganymedes01.etfuturum.blocks.itemblocks;

import ganymedes01.etfuturum.ModBlocks;
import ganymedes01.etfuturum.blocks.BlockAquaticPlant;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public final class ItemBlockConduit extends ItemBlock {
	public ItemBlockConduit(Block block) { super(block); }
	@Override public boolean placeBlockAt(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float hx, float hy, float hz, int meta) {
		return world.setBlock(x, y, z, (BlockAquaticPlant.sourceWater(world, x, y, z) ? ModBlocks.CONDUIT_UNDERWATER : ModBlocks.CONDUIT).get(), 0, 3);
	}
}
