package ganymedes01.etfuturum.blocks;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import ganymedes01.etfuturum.client.renderer.block.ReleasedBlockModel;
import java.util.Random;

public final class BlockCoral extends BlockAquaticPlant {
	public final String color, shape;
	public final boolean dead;
	@SideOnly(Side.CLIENT) private ReleasedBlockModel[] models;
	public BlockCoral(String color, boolean dead, String shape, boolean wet) {
		super((dead ? "dead_" : "") + color + "_coral" + shape + (wet ? "_underwater" : ""), wet);
		this.color = color; this.dead = dead; this.shape = shape;
		setBlockBounds(.125F, 0, .125F, .875F, .75F, .875F);
	}
	@Override @SideOnly(Side.CLIENT) public void registerBlockIcons(IIconRegister atlas) {
		models = new ReleasedBlockModel[4];
		String name = (dead ? "dead_" : "") + color + "_coral" + shape;
		for (int i = 0; i < 4; i++) models[i] = new ReleasedBlockModel(atlas, name, new int[]{0, 2, 3, 1}[i]);
		blockIcon = models[0].particle;
	}
	@SideOnly(Side.CLIENT) public ReleasedBlockModel getReleasedModel(int meta) { return models[meta & 3]; }
	private String itemShape() { return shape.equals("_wall_fan") ? "_fan" : shape; }
	private Item item() { return Item.getItemFromBlock(CoralContent.get(color, dead, itemShape(), false)); }
	@Override @SideOnly(Side.CLIENT) public Item getItem(World world, int x, int y, int z) { return item(); }
	@Override protected ItemStack createStackedBlock(int meta) { return new ItemStack(item()); }
	@Override public boolean canSilkHarvest(World world, EntityPlayer player, int x, int y, int z, int meta) { return true; }
	@Override public int quantityDropped(Random random) { return 0; }
	@Override public boolean canBlockStay(World world, int x, int y, int z) {
		ForgeDirection face = shape.equals("_wall_fan") ? ForgeDirection.getOrientation((world.getBlockMetadata(x, y, z) & 3) + 2) : ForgeDirection.UP;
		int bx = x - face.offsetX, by = y - face.offsetY, bz = z - face.offsetZ;
		return y > 0 && world.getBlock(bx, by, bz).isSideSolid(world, bx, by, bz, face);
	}
	@Override public boolean canPlaceBlockAt(World world, int x, int y, int z) { return y > 0 && y < 256 && (sourceWater(world, x, y, z) || world.isAirBlock(x, y, z)); }
	@Override protected Block drainedBlock() { return CoralContent.get(color, dead, shape, false); }
	@Override protected Block filledBlock() { return CoralContent.get(color, dead, shape, true); }
	@Override public void onBlockAdded(World world, int x, int y, int z) { if (!world.isRemote && !dead && !submerged) world.scheduleBlockUpdate(x, y, z, this, 60 + world.rand.nextInt(40)); }
	@Override public void onNeighborBlockChange(World world, int x, int y, int z, Block neighbor) { super.onNeighborBlockChange(world, x, y, z, neighbor); if (world.getBlock(x, y, z) == this) onBlockAdded(world, x, y, z); }
	@Override public void updateTick(World world, int x, int y, int z, Random random) {
		if (!world.isRemote && !dead && !submerged && !CoralContent.hasWater(world, x, y, z)) world.setBlock(x, y, z, CoralContent.get(color, true, shape, false), world.getBlockMetadata(x, y, z), 3);
	}
}
