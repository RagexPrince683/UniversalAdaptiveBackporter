package ganymedes01.etfuturum.blocks;

import ganymedes01.etfuturum.tileentities.TileEntityNewBeacon;
import net.minecraft.block.BlockBeacon;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import java.util.Random;

public class BlockNewBeacon extends BlockBeacon {

	public BlockNewBeacon() {
		setLightLevel(1.0F);
		setBlockTextureName("beacon");
		setBlockName("beacon");
		setCreativeTab(null);
	}

	@Override
	public Item getItemDropped(int meta, Random rand, int fortune) {
		return Item.getItemFromBlock(Blocks.beacon);
	}

	@Override
	public Item getItem(World world, int x, int y, int z) {
		return Item.getItemFromBlock(Blocks.beacon);
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		return new TileEntityNewBeacon();
	}
}
