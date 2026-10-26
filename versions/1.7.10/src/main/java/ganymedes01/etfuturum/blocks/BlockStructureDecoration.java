package ganymedes01.etfuturum.blocks;

import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraft.world.IBlockAccess;

/** Released small-block geometry shared by the underground structure dependencies. */
public class BlockStructureDecoration extends BaseBlock {
	protected final String[] modelNames;
	@cpw.mods.fml.relauncher.SideOnly(cpw.mods.fml.relauncher.Side.CLIENT) private ganymedes01.etfuturum.client.renderer.block.ReleasedBlockModel[][] models;
	public BlockStructureDecoration(String name, String... models) { super(Material.rock); setNames(name); modelNames = models; setHardness(1); setLightOpacity(0); if (name.equals("heavy_core")) setBlockBounds(.25F, 0, .25F, .75F, .5F, .75F); if (name.equals("candle")) setBlockBounds(.4375F, 0, .4375F, .5625F, .375F, .5625F); }
	@Override @cpw.mods.fml.relauncher.SideOnly(cpw.mods.fml.relauncher.Side.CLIENT) public void registerBlockIcons(net.minecraft.client.renderer.texture.IIconRegister atlas) {
		models = new ganymedes01.etfuturum.client.renderer.block.ReleasedBlockModel[modelNames.length][4];
		for (int i = 0; i < modelNames.length; i++) for (int r = 0; r < 4; r++) models[i][r] = new ganymedes01.etfuturum.client.renderer.block.ReleasedBlockModel(atlas, modelNames[i], r);
		blockIcon = models[0][0].particle;
	}
	@cpw.mods.fml.relauncher.SideOnly(cpw.mods.fml.relauncher.Side.CLIENT) public ganymedes01.etfuturum.client.renderer.block.ReleasedBlockModel getReleasedModel(int meta) { return models[Math.min(modelNames.length - 1, meta >> 2)][meta & 3]; }
	@Override public boolean isOpaqueCube() { return false; }
	@Override public boolean renderAsNormalBlock() { return false; }
	@Override public int getRenderType() { return ganymedes01.etfuturum.lib.RenderIDs.RELEASED_BLOCK; }
	@Override public int getLightValue(IBlockAccess world, int x, int y, int z) { return modelNames[0].equals("candle_one_candle") ? (world.getBlockMetadata(x, y, z) & 4) == 0 ? 0 : 3 : super.getLightValue(world, x, y, z); }
	@Override public int damageDropped(int meta) { return 0; }
}
