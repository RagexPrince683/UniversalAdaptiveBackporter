package ganymedes01.etfuturum.blocks;

import net.minecraft.block.material.Material;
import net.minecraft.util.IIcon;
public class BlockReinforcedDeepslate extends BaseBlock {
	@cpw.mods.fml.relauncher.SideOnly(cpw.mods.fml.relauncher.Side.CLIENT) private IIcon top, bottom;
	public BlockReinforcedDeepslate() { super(Material.rock); setNames("reinforced_deepslate"); setHardness(55); setResistance(1200); }
	@Override @cpw.mods.fml.relauncher.SideOnly(cpw.mods.fml.relauncher.Side.CLIENT) public void registerBlockIcons(net.minecraft.client.renderer.texture.IIconRegister atlas) { blockIcon = atlas.registerIcon("reinforced_deepslate_side"); top = atlas.registerIcon("reinforced_deepslate_top"); bottom = atlas.registerIcon("reinforced_deepslate_bottom"); }
	@Override public IIcon getIcon(int side, int meta) { return side == 0 ? bottom : side == 1 ? top : blockIcon; }
	@Override public net.minecraft.item.Item getItemDropped(int meta, java.util.Random random, int fortune) { return null; }
	@Override protected boolean canSilkHarvest() { return false; }
	@Override public int getMobilityFlag() { return 2; }
}
