package ganymedes01.etfuturum.blocks;

import ganymedes01.etfuturum.entities.EntityTadpole;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;
import java.util.Random;

public class BlockFrogspawn extends BaseBlock {
	@cpw.mods.fml.relauncher.SideOnly(cpw.mods.fml.relauncher.Side.CLIENT) private ganymedes01.etfuturum.client.renderer.block.ReleasedBlockModel model;
	@Override @cpw.mods.fml.relauncher.SideOnly(cpw.mods.fml.relauncher.Side.CLIENT) public void registerBlockIcons(net.minecraft.client.renderer.texture.IIconRegister atlas) { model = new ganymedes01.etfuturum.client.renderer.block.ReleasedBlockModel(atlas, "frogspawn"); blockIcon = model.particle; }
	@cpw.mods.fml.relauncher.SideOnly(cpw.mods.fml.relauncher.Side.CLIENT) public ganymedes01.etfuturum.client.renderer.block.ReleasedBlockModel getReleasedModel(int meta) { return model; }
	@Override public int getRenderType() { return ganymedes01.etfuturum.lib.RenderIDs.RELEASED_BLOCK; }
	public BlockFrogspawn() { super(Material.plants); setNames("frogspawn"); setHardness(0); setBlockSound(ganymedes01.etfuturum.client.sound.ModSounds.soundFrogspawn); setBlockBounds(0, 0, 0, 1, 0.0625F, 1); }
	@Override public boolean isOpaqueCube() { return false; }
	@Override public boolean renderAsNormalBlock() { return false; }
	@Override public AxisAlignedBB getCollisionBoundingBoxFromPool(World world, int x, int y, int z) { return null; }
	@Override public int quantityDropped(Random random) { return 0; }
	@Override public boolean canBlockStay(World world, int x, int y, int z) { return world.getBlock(x, y - 1, z).getMaterial() == Material.water; }
	@Override public void onBlockAdded(World world, int x, int y, int z) { world.scheduleBlockUpdate(x, y, z, this, 3600 + world.rand.nextInt(2400)); }
	@Override public void onNeighborBlockChange(World world, int x, int y, int z, Block neighbor) { if (!canBlockStay(world, x, y, z)) world.setBlockToAir(x, y, z); }
	@Override public void updateTick(World world, int x, int y, int z, Random random) {
		if (world.isRemote) return;
		if (!canBlockStay(world, x, y, z)) { world.setBlockToAir(x, y, z); return; }
		world.setBlockToAir(x, y, z);
		world.playSoundEffect(x + .5, y, z + .5, ganymedes01.etfuturum.Tags.MC_ASSET_VER + ":block.frogspawn.hatch", 1, 1);
		for (int i = 0, count = 2 + random.nextInt(4); i < count; i++) { EntityTadpole tadpole = new EntityTadpole(world); tadpole.setLocationAndAngles(x + 0.2 + random.nextDouble() * 0.6, y - 0.5, z + 0.2 + random.nextDouble() * 0.6, random.nextFloat() * 360, 0); world.spawnEntityInWorld(tadpole); }
	}
}
