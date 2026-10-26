package ganymedes01.etfuturum.blocks;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.util.IIcon;

public final class BlockDriedKelp extends BaseBlock {
	@SideOnly(Side.CLIENT) private IIcon top, bottom;
	public BlockDriedKelp() { super(Material.grass); setNames("dried_kelp_block"); setHardness(.5F); setBlockSound(soundTypeGrass); }
	@Override @SideOnly(Side.CLIENT) public void registerBlockIcons(IIconRegister atlas) { blockIcon = atlas.registerIcon("dried_kelp_side"); top = atlas.registerIcon("dried_kelp_top"); bottom = atlas.registerIcon("dried_kelp_bottom"); }
	@Override @SideOnly(Side.CLIENT) public IIcon getIcon(int side, int meta) { return side == 0 ? bottom : side == 1 ? top : blockIcon; }
}
