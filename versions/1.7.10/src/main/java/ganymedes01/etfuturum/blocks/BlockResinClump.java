package ganymedes01.etfuturum.blocks;

import ganymedes01.etfuturum.EtFuturum;
import ganymedes01.etfuturum.core.utils.Utils;
import net.minecraft.block.BlockVine;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.Item;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import java.util.Random;

public class BlockResinClump extends BlockVine {
	public BlockResinClump() { setBlockName(Utils.getUnlocalisedName("resin_clump")); setBlockTextureName("resin_clump"); setCreativeTab(EtFuturum.creativeTabBlocks); setHardness(0); setTickRandomly(false); }
	@Override public void updateTick(World world, int x, int y, int z, Random random) { }
	@Override public int colorMultiplier(IBlockAccess world, int x, int y, int z) { return 0xFFFFFF; }
	@Override public int getRenderColor(int meta) { return 0xFFFFFF; }
	@Override public boolean isLadder(IBlockAccess world, int x, int y, int z, EntityLivingBase entity) { return false; }
	@Override public Item getItemDropped(int meta, Random random, int fortune) { return Item.getItemFromBlock(this); }
	@Override public int quantityDropped(Random random) { return 1; }
	@Override public int damageDropped(int meta) { return 0; }
	@Override public void harvestBlock(World world, net.minecraft.entity.player.EntityPlayer player, int x, int y, int z, int meta) {
		if (!world.isRemote) dropBlockAsItem(world, x, y, z, meta, 0);
	}
}
