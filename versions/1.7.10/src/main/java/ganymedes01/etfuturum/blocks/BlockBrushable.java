package ganymedes01.etfuturum.blocks;

import cpw.mods.fml.relauncher.*;
import ganymedes01.etfuturum.tileentities.TileEntityBrushable;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.*;
import java.util.Random;

public final class BlockBrushable extends BaseBlock {
	public final Block base;
	private IIcon[] stages;
	private final String name;
	public BlockBrushable(String name, Block base) { super(Material.sand); this.name = name; this.base = base; setNames(name); setHardness(.25F); setStepSound(soundTypeSand); }
	@Override public boolean hasTileEntity(int meta) { return true; }
	@Override public TileEntity createTileEntity(World world, int meta) { return new TileEntityBrushable(); }
	@Override public Item getItemDropped(int meta, Random random, int fortune) { return null; }
	@Override public int quantityDropped(Random random) { return 0; }
	@Override protected boolean canSilkHarvest() { return false; }
	@Override public void onBlockAdded(World world, int x, int y, int z) { world.scheduleBlockUpdate(x, y, z, this, 2); }
	@Override public void onNeighborBlockChange(World world, int x, int y, int z, Block neighbor) { world.scheduleBlockUpdate(x, y, z, this, 2); }
	@Override public void updateTick(World world, int x, int y, int z, Random random) {
		if (!world.isRemote && y > 0 && net.minecraft.block.BlockFalling.func_149831_e(world, x, y - 1, z)) {
			net.minecraft.entity.item.EntityFallingBlock falling = new net.minecraft.entity.item.EntityFallingBlock(world, x + .5, y + .5, z + .5, this, 0);
			((ganymedes01.etfuturum.mixins.early.archaeology.FallingBlockAccess) falling).etfu$breakOnLanding(true);
			world.spawnEntityInWorld(falling);
		}
	}
	@Override @SideOnly(Side.CLIENT) public void registerBlockIcons(IIconRegister atlas) { stages = new IIcon[4]; for (int i = 0; i < 4; i++) stages[i] = atlas.registerIcon(name + "_" + i); blockIcon = stages[0]; }
	@Override @SideOnly(Side.CLIENT) public IIcon getIcon(IBlockAccess world, int x, int y, int z, int side) { TileEntity tile = world.getTileEntity(x, y, z); return stages[tile instanceof TileEntityBrushable ? Math.min(3, (((TileEntityBrushable) tile).progress + 2) / 3) : 0]; }
}
