package ganymedes01.etfuturum.blocks;

import ganymedes01.etfuturum.ModBlocks;
import net.minecraft.item.Item;
import net.minecraft.world.IBlockAccess;
import java.util.Random;

public class BlockPaleOakLeaves extends BaseLeaves {
	public BlockPaleOakLeaves() { super("pale_oak"); setBlockName("etfuturum.pale_oak_leaves"); setCreativeTab(ganymedes01.etfuturum.EtFuturum.creativeTabBlocks); }
	@Override public Item getItemDropped(int meta, Random random, int fortune) { return ModBlocks.PALE_OAK_SAPLING.getItem(); }
	@Override public int damageDropped(int meta) { return 0; }
	@Override public int colorMultiplier(IBlockAccess world, int x, int y, int z) { return 0xFFFFFF; }
	@Override public int getRenderColor(int meta) { return 0xFFFFFF; }
}
