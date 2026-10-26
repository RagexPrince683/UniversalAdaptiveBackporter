package ganymedes01.etfuturum.blocks;

import ganymedes01.etfuturum.EtFuturum;
import ganymedes01.etfuturum.core.utils.Utils;
import ganymedes01.etfuturum.world.generate.decorate.WorldGenPaleOak;
import net.minecraft.block.BlockSapling;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import net.minecraftforge.event.terraingen.TerrainGen;

import java.util.Random;

public class BlockPaleOakSapling extends BlockSapling {
	public BlockPaleOakSapling() {
		setBlockName(Utils.getUnlocalisedName("pale_oak_sapling"));
		setBlockTextureName("pale_oak_sapling");
		setCreativeTab(EtFuturum.creativeTabBlocks);
	}
	@Override public int damageDropped(int meta) { return 0; }
	@Override public void registerBlockIcons(net.minecraft.client.renderer.texture.IIconRegister register) { blockIcon = register.registerIcon("pale_oak_sapling"); }
	@Override public net.minecraft.util.IIcon getIcon(int side, int meta) { return blockIcon; }
	@Override public void getSubBlocks(net.minecraft.item.Item item, net.minecraft.creativetab.CreativeTabs tab, java.util.List<net.minecraft.item.ItemStack> list) { list.add(new net.minecraft.item.ItemStack(item)); }
	@Override public void func_149878_d(World world, int x, int y, int z, Random random) {
		if (!TerrainGen.saplingGrowTree(world, random, x, y, z)) return;
		for (int dx = 0; dx >= -1; dx--) for (int dz = 0; dz >= -1; dz--) {
			int bx = x + dx, bz = z + dz;
			if (world.getBlock(bx, y, bz) != this || world.getBlock(bx + 1, y, bz) != this || world.getBlock(bx, y, bz + 1) != this || world.getBlock(bx + 1, y, bz + 1) != this) continue;
			int[] metadata = new int[4];
			for (int i = 0; i < 4; i++) { metadata[i] = world.getBlockMetadata(bx + i % 2, y, bz + i / 2); world.setBlock(bx + i % 2, y, bz + i / 2, Blocks.air, 0, 4); }
			if (!new WorldGenPaleOak(true, false).generate(world, random, bx, y, bz)) for (int i = 0; i < 4; i++) world.setBlock(bx + i % 2, y, bz + i / 2, this, metadata[i], 2);
			return;
		}
	}
}
