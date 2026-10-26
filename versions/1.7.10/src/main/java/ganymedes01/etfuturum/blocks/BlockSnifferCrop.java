package ganymedes01.etfuturum.blocks;

import cpw.mods.fml.relauncher.*;
import ganymedes01.etfuturum.ModBlocks;
import ganymedes01.etfuturum.ModItems;
import ganymedes01.etfuturum.client.renderer.block.ReleasedBlockModel;
import net.minecraft.block.Block;
import net.minecraft.block.BlockCrops;
import net.minecraft.item.*;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;
import java.util.*;

public class BlockSnifferCrop extends BlockCrops {
	private final boolean pitcher;
	@SideOnly(Side.CLIENT) private IIcon[] icons;
	@SideOnly(Side.CLIENT) private ReleasedBlockModel[] models;
	public BlockSnifferCrop(boolean pitcher) { this.pitcher = pitcher; setBlockName("etfuturum." + (pitcher ? "pitcher_crop" : "torchflower_crop")); setStepSound(ganymedes01.etfuturum.client.sound.ModSounds.soundCrops); }
	// Retain original 0..7 lower-half ages in existing saves; bit 8 identifies the upper half.
	private int stage(int meta) { return Math.min(4, (meta & 7) * 5 / 8); }
	@SideOnly(Side.CLIENT) @Override public void registerBlockIcons(net.minecraft.client.renderer.texture.IIconRegister atlas) {
		if (pitcher) {
			models = new ReleasedBlockModel[7];
			for (int i = 0; i < 5; i++) models[i] = new ReleasedBlockModel(atlas, "pitcher_crop_bottom_stage_" + i);
			for (int i = 3; i < 5; i++) models[i + 2] = new ReleasedBlockModel(atlas, "pitcher_crop_top_stage_" + i);
			blockIcon = models[0].particle;
		} else { icons = new IIcon[]{atlas.registerIcon("torchflower_crop_stage0"), atlas.registerIcon("torchflower_crop_stage1"), atlas.registerIcon("torchflower")}; blockIcon = icons[0]; }
	}
	@SideOnly(Side.CLIENT) public ReleasedBlockModel getReleasedModel(int meta) { int age = stage(meta); return models[(meta & 8) == 0 ? age : Math.max(3, age) + 2]; }
	@SideOnly(Side.CLIENT) @Override public IIcon getIcon(int side, int meta) { return pitcher ? getReleasedModel(meta).particle : icons[Math.min(2, (meta & 7) * 3 / 8)]; }
	@Override public int getRenderType() { return pitcher ? ganymedes01.etfuturum.lib.RenderIDs.RELEASED_BLOCK : 1; }
	@Override public boolean canBlockStay(World world, int x, int y, int z) {
		int meta = world.getBlockMetadata(x, y, z);
		if (pitcher && (meta & 8) != 0) return world.getBlock(x, y - 1, z) == this && (world.getBlockMetadata(x, y - 1, z) & 8) == 0 && stage(world.getBlockMetadata(x, y - 1, z)) >= 3;
		return super.canBlockStay(world, x, y, z);
	}
	@Override public void updateTick(World world, int x, int y, int z, Random random) {
		if (world.isRemote) return;
		int meta = world.getBlockMetadata(x, y, z);
		if ((meta & 8) != 0) { if (!canBlockStay(world, x, y, z)) world.setBlockToAir(x, y, z); return; }
		if (!canBlockStay(world, x, y, z)) { dropBlockAsItem(world, x, y, z, meta, 0); world.setBlockToAir(x, y, z); return; }
		if (pitcher && meta >= 4 && !roomAbove(world, x, y, z)) return;
		super.updateTick(world, x, y, z, random);
		if (world.getBlock(x, y, z) == this) finishGrowth(world, x, y, z);
	}
	private boolean roomAbove(World world, int x, int y, int z) { return y < 254 && (world.isAirBlock(x, y + 1, z) || world.getBlock(x, y + 1, z) == this && (world.getBlockMetadata(x, y + 1, z) & 8) != 0); }
	private void finishGrowth(World world, int x, int y, int z) {
		int age = world.getBlockMetadata(x, y, z) & 7;
		if (!pitcher) { if (age == 7) world.setBlock(x, y, z, ModBlocks.TORCHFLOWER.get(), 0, 3); return; }
		if (stage(age) >= 3 && roomAbove(world, x, y, z)) world.setBlock(x, y + 1, z, this, age | 8, 3);
	}
	@Override public void func_149863_m(World world, int x, int y, int z) {
		if (world.isRemote) return;
		if ((world.getBlockMetadata(x, y, z) & 8) != 0) { func_149863_m(world, x, y - 1, z); return; }
		int age = world.getBlockMetadata(x, y, z) & 7;
		if (pitcher && age >= 4 && !roomAbove(world, x, y, z)) return;
		int next = pitcher ? new int[]{2, 4, 5, 7, 7}[stage(age)] : Math.min(7, age + 3);
		if (pitcher && next >= 5 && !roomAbove(world, x, y, z)) next = 4;
		world.setBlockMetadataWithNotify(x, y, z, next, 2); finishGrowth(world, x, y, z);
	}
	@Override public boolean func_149851_a(World world, int x, int y, int z, boolean client) {
		int meta = world.getBlockMetadata(x, y, z);
		if ((meta & 8) != 0) return func_149851_a(world, x, y - 1, z, client);
		return (meta & 7) < 7 && (!pitcher || meta < 4 || roomAbove(world, x, y, z));
	}
	@Override public void onNeighborBlockChange(World world, int x, int y, int z, Block neighbor) {
		if (world.isRemote) return;
		int meta = world.getBlockMetadata(x, y, z);
		boolean brokenTop = pitcher && (meta & 8) == 0 && stage(meta) >= 3 && world.getBlock(x, y + 1, z) != this;
		if (brokenTop || !canBlockStay(world, x, y, z)) { dropBlockAsItem(world, x, y, z, meta, 0); world.setBlockToAir(x, y, z); }
	}
	@Override public ArrayList<ItemStack> getDrops(World world, int x, int y, int z, int meta, int fortune) {
		ArrayList<ItemStack> drops = new ArrayList<>();
		if ((meta & 8) == 0) drops.add(new ItemStack((meta & 7) == 7 ? func_149865_P() : func_149866_i()));
		return drops;
	}
	@Override protected Item func_149866_i() { return (pitcher ? ModItems.PITCHER_POD : ModItems.TORCHFLOWER_SEEDS).get(); }
	@Override protected Item func_149865_P() { return (pitcher ? ModBlocks.PITCHER_PLANT : ModBlocks.TORCHFLOWER).getItem(); }
}
