package ganymedes01.etfuturum.blocks;
import net.minecraft.block.material.Material;
import net.minecraft.world.World;
import java.util.Random;
public class BlockSnifferEgg extends BaseBlock {
	@cpw.mods.fml.relauncher.SideOnly(cpw.mods.fml.relauncher.Side.CLIENT) private ganymedes01.etfuturum.client.renderer.block.ReleasedBlockModel[] models;
	@Override @cpw.mods.fml.relauncher.SideOnly(cpw.mods.fml.relauncher.Side.CLIENT) public void registerBlockIcons(net.minecraft.client.renderer.texture.IIconRegister atlas) { String[] stages = {"sniffer_egg_not_cracked", "sniffer_egg_slightly_cracked", "sniffer_egg_very_cracked"}; models = new ganymedes01.etfuturum.client.renderer.block.ReleasedBlockModel[3]; for (int i = 0; i < 3; i++) models[i] = new ganymedes01.etfuturum.client.renderer.block.ReleasedBlockModel(atlas, stages[i]); blockIcon = models[0].particle; }
	@cpw.mods.fml.relauncher.SideOnly(cpw.mods.fml.relauncher.Side.CLIENT) public ganymedes01.etfuturum.client.renderer.block.ReleasedBlockModel getReleasedModel(int meta) { return models[Math.min(2, meta & 3)]; }
	@Override public int getRenderType() { return ganymedes01.etfuturum.lib.RenderIDs.RELEASED_BLOCK; }
	public BlockSnifferEgg() { super(Material.dragonEgg); setNames("sniffer_egg"); setHardness(.5F); setBlockBounds(.0625F, 0, .125F, .9375F, 1, .875F); setBlockSound(ganymedes01.etfuturum.client.sound.ModSounds.soundSnifferEgg); }
	@Override public boolean isOpaqueCube() { return false; }
	@Override public boolean renderAsNormalBlock() { return false; }
	private int delay(World world, int x, int y, int z) { return world.getBlock(x, y - 1, z) == ganymedes01.etfuturum.ModBlocks.MOSS_BLOCK.get() ? 4000 : 8000; }
	@Override public void onBlockAdded(World world, int x, int y, int z) { world.scheduleBlockUpdate(x, y, z, this, delay(world, x, y, z)); }
	@Override public void updateTick(World world, int x, int y, int z, Random random) {
		if (world.isRemote) return;
		int stage = world.getBlockMetadata(x, y, z);
		if (stage < 2) {
			world.setBlockMetadataWithNotify(x, y, z, stage + 1, 2); sound(world, x, y, z, "crack"); world.scheduleBlockUpdate(x, y, z, this, delay(world, x, y, z));
		} else {
			ganymedes01.etfuturum.entities.EntitySniffer sniffer = new ganymedes01.etfuturum.entities.EntitySniffer(world); sniffer.setLocationAndAngles(x + .5, y, z + .5, 0, 0); sniffer.setGrowingAge(-48000);
			if (world.spawnEntityInWorld(sniffer)) { sound(world, x, y, z, "hatch"); world.setBlockToAir(x, y, z); } else world.scheduleBlockUpdate(x, y, z, this, 200);
		}
	}
	private void sound(World world, int x, int y, int z, String event) { world.playSoundEffect(x + .5, y + .5, z + .5, ganymedes01.etfuturum.Tags.MC_ASSET_VER + ":block.sniffer_egg." + event, 1, 1); }
}
