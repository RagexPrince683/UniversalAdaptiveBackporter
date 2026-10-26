package ganymedes01.etfuturum.blocks;

import ganymedes01.etfuturum.EtFuturum;
import ganymedes01.etfuturum.lib.GUIIDs;
import ganymedes01.etfuturum.tileentities.TileEntityNewBrewingStand;
import net.minecraft.block.BlockBrewingStand;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityBrewingStand;
import net.minecraft.world.World;

import java.util.Random;

public class BlockNewBrewingStand extends BlockBrewingStand {

	public BlockNewBrewingStand() {
		setHardness(0.5F);
		setLightLevel(0.125F);
		setBlockTextureName("brewing_stand");
		setCreativeTab(null);
	}

	@Override
	public String getUnlocalizedName() {
		return Items.brewing_stand.getUnlocalizedName();
	}

	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ) {
		if (world.isRemote)
			return true;

		TileEntityBrewingStand tile = (TileEntityBrewingStand) world.getTileEntity(x, y, z);
		if (tile != null)
			player.openGui(EtFuturum.instance, GUIIDs.BREWING_STAND, world, x, y, z);

		return true;
	}

	@Override
	public Item getItemDropped(int meta, Random rand, int fortune) {
		return Items.brewing_stand;
	}

	@Override
	public Item getItem(World world, int x, int y, int z) {
		return Items.brewing_stand;
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		return new TileEntityNewBrewingStand();
	}

	@Override
	public String getItemIconName() {
		return "brewing_stand";
	}
}
