package ganymedes01.etfuturum.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraftforge.common.IShearable;

import java.util.ArrayList;
import java.util.Random;

/** Submerged vegetation is itself water material; no waterlogging engine is needed. */
public class BlockSeagrass extends BaseBlock implements IShearable, net.minecraft.block.IGrowable {
	@cpw.mods.fml.relauncher.SideOnly(cpw.mods.fml.relauncher.Side.CLIENT) private net.minecraft.util.IIcon[] icons;
	@cpw.mods.fml.relauncher.SideOnly(cpw.mods.fml.relauncher.Side.CLIENT) @Override public void registerBlockIcons(net.minecraft.client.renderer.texture.IIconRegister atlas) { icons = new net.minecraft.util.IIcon[]{atlas.registerIcon("seagrass"), atlas.registerIcon("tall_seagrass_bottom"), atlas.registerIcon("tall_seagrass_top")}; blockIcon = icons[0]; }
	@cpw.mods.fml.relauncher.SideOnly(cpw.mods.fml.relauncher.Side.CLIENT) @Override public net.minecraft.util.IIcon getIcon(int side, int meta) { return icons[Math.min(2, meta)]; }
	public BlockSeagrass() { super(Material.water); setNames("seagrass"); setHardness(0); setLightOpacity(0); setBlockBounds(.1F, 0, .1F, .9F, .8F, .9F); setBlockSound(ganymedes01.etfuturum.client.sound.ModSounds.soundWetGrass); }
	@Override public boolean isOpaqueCube() { return false; }
	@Override public boolean renderAsNormalBlock() { return false; }
	@Override public int getRenderType() { return ganymedes01.etfuturum.lib.RenderIDs.SEAGRASS; }
	@cpw.mods.fml.relauncher.SideOnly(cpw.mods.fml.relauncher.Side.CLIENT) @Override public int getRenderBlockPass() { return 1; }
	@cpw.mods.fml.relauncher.SideOnly(cpw.mods.fml.relauncher.Side.CLIENT) @Override public boolean canRenderInPass(int pass) { return pass == 0 || pass == 1; }
	@Override public AxisAlignedBB getCollisionBoundingBoxFromPool(World world, int x, int y, int z) { return null; }
	@Override public boolean canCollideCheck(int meta, boolean liquid) { return false; }
	private boolean sourceWater(World world, int x, int y, int z) { Block block = world.getBlock(x, y, z); return (block == Blocks.water || block == Blocks.flowing_water) && world.getBlockMetadata(x, y, z) == 0; }
	private boolean supported(World world, int x, int y, int z) { Block below = world.getBlock(x, y - 1, z); return below != this && below.isSideSolid(world, x, y - 1, z, net.minecraftforge.common.util.ForgeDirection.UP); }
	@Override public boolean isSideSolid(net.minecraft.world.IBlockAccess world, int x, int y, int z, net.minecraftforge.common.util.ForgeDirection side) { return false; }
	@Override public boolean isReplaceable(net.minecraft.world.IBlockAccess world, int x, int y, int z) { return false; }
	@Override public boolean canPlaceBlockAt(World world, int x, int y, int z) { return y > 0 && y < 255 && sourceWater(world, x, y, z) && supported(world, x, y, z); }
	@Override public boolean canBlockStay(World world, int x, int y, int z) {
		int meta = world.getBlockMetadata(x, y, z);
		if (meta == 2) return world.getBlock(x, y - 1, z) == this && world.getBlockMetadata(x, y - 1, z) == 1;
		return supported(world, x, y, z) && (meta != 1 || world.getBlock(x, y + 1, z) == this && world.getBlockMetadata(x, y + 1, z) == 2);
	}
	@Override public void onNeighborBlockChange(World world, int x, int y, int z, Block block) { if (!world.isRemote && !canBlockStay(world, x, y, z)) world.setBlock(x, y, z, Blocks.water); }
	@Override public int quantityDropped(Random random) { return 0; }
	@Override public boolean isShearable(ItemStack stack, net.minecraft.world.IBlockAccess world, int x, int y, int z) { return true; }
	@Override public ArrayList<ItemStack> onSheared(ItemStack stack, net.minecraft.world.IBlockAccess world, int x, int y, int z, int fortune) { ArrayList<ItemStack> drops = new ArrayList<>(); drops.add(new ItemStack(this, world.getBlockMetadata(x, y, z) == 0 ? 1 : 2)); return drops; }
	@Override public void onBlockDestroyedByPlayer(World world, int x, int y, int z, int meta) { if (!world.isRemote && world.isAirBlock(x, y, z)) world.setBlock(x, y, z, Blocks.water); }
	@Override public boolean func_149851_a(World world, int x, int y, int z, boolean client) { return world.getBlockMetadata(x, y, z) == 0 && y < 254 && sourceWater(world, x, y + 1, z); }
	@Override public boolean func_149852_a(World world, Random random, int x, int y, int z) { return true; }
	@Override public void func_149853_b(World world, Random random, int x, int y, int z) { if (!world.isRemote && func_149851_a(world, x, y, z, false)) { world.setBlockMetadataWithNotify(x, y, z, 1, 2); world.setBlock(x, y + 1, z, this, 2, 3); } }
}
