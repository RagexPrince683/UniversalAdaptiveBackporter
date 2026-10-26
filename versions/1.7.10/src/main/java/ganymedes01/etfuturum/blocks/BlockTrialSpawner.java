package ganymedes01.etfuturum.blocks;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
public class BlockTrialSpawner extends BlockContainer {
	@cpw.mods.fml.relauncher.SideOnly(cpw.mods.fml.relauncher.Side.CLIENT) private ganymedes01.etfuturum.client.renderer.block.ReleasedBlockModel[] models;
	@cpw.mods.fml.relauncher.SideOnly(cpw.mods.fml.relauncher.Side.CLIENT)
	@Override public void registerBlockIcons(net.minecraft.client.renderer.texture.IIconRegister atlas) {
		models = new ganymedes01.etfuturum.client.renderer.block.ReleasedBlockModel[6]; String[] names = {"trial_spawner", "trial_spawner_active", "trial_spawner_ejecting_reward", "trial_spawner_inactive_ominous", "trial_spawner_active_ominous", "trial_spawner_ejecting_reward_ominous"};
		for (int i = 0; i < models.length; i++) models[i] = new ganymedes01.etfuturum.client.renderer.block.ReleasedBlockModel(atlas, names[i]); blockIcon = models[0].particle;
	}
	@cpw.mods.fml.relauncher.SideOnly(cpw.mods.fml.relauncher.Side.CLIENT) public ganymedes01.etfuturum.client.renderer.block.ReleasedBlockModel getReleasedModel(int meta) { return models[Math.min(2, (meta >> 1) & 3) + ((meta & 8) != 0 ? 3 : 0)]; }
	public BlockTrialSpawner() { super(Material.iron); setBlockName("etfuturum.trial_spawner"); setBlockTextureName("trial_spawner_side_inactive"); setHardness(50); setResistance(2000); setLightOpacity(0); setStepSound(ganymedes01.etfuturum.client.sound.ModSounds.soundTrialSpawner); setCreativeTab(ganymedes01.etfuturum.EtFuturum.creativeTabBlocks); }
	@Override public boolean isOpaqueCube() { return false; }
	@Override public boolean renderAsNormalBlock() { return false; }
	@Override public int getRenderType() { return ganymedes01.etfuturum.lib.RenderIDs.RELEASED_BLOCK; }
	@Override public TileEntity createNewTileEntity(World world, int meta) { return new ganymedes01.etfuturum.tileentities.TileEntityTrialSpawner(); }
	@Override public net.minecraft.item.Item getItemDropped(int meta, java.util.Random random, int fortune) { return null; }
}
