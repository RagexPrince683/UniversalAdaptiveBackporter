package ganymedes01.etfuturum.blocks;

import ganymedes01.etfuturum.EtFuturum;
import ganymedes01.etfuturum.lib.GUIIDs;
import net.minecraft.block.BlockEnchantmentTable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.world.World;

import java.util.Random;

public class BlockNewEnchantmentTable extends BlockEnchantmentTable {

	public BlockNewEnchantmentTable() {
		setHardness(5.0F);
		setResistance(2000.0F);
		setBlockTextureName("enchanting_table");
		setBlockName("enchantmentTable");
		setCreativeTab(null);
	}

	@Override
	public Item getItemDropped(int meta, Random rand, int fortune) {
		return Item.getItemFromBlock(Blocks.enchanting_table);
	}

	@Override
	public Item getItem(World world, int x, int y, int z) {
		return Item.getItemFromBlock(Blocks.enchanting_table);
	}

	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ) {
		if (world.isRemote)
			return true;
		player.openGui(EtFuturum.instance, GUIIDs.ENCHANTING_TABLE, world, x, y, z);
		return true;
	}
}
