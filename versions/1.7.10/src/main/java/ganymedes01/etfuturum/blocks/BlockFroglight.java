package ganymedes01.etfuturum.blocks;

import ganymedes01.etfuturum.EtFuturum;
import net.minecraft.block.BlockRotatedPillar;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.util.IIcon;

public class BlockFroglight extends BlockRotatedPillar {
	private final String type; private IIcon side, top;
	public BlockFroglight(String type) { super(Material.glass); this.type = type; setBlockName("etfuturum." + type); setHardness(0.3F); setLightLevel(1); setCreativeTab(EtFuturum.creativeTabBlocks); }
	@Override public void registerBlockIcons(IIconRegister register) { side = register.registerIcon(type + "_side"); top = register.registerIcon(type + "_top"); }
	@Override protected IIcon getSideIcon(int meta) { return side; }
	@Override protected IIcon getTopIcon(int meta) { return top; }
	@Override public int damageDropped(int meta) { return 0; }
}
