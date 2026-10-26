package ganymedes01.etfuturum.blocks;
import net.minecraft.block.Block;
import net.minecraft.block.BlockBush;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.world.World;
import java.util.Random;
public class BlockSnifferPlant extends BlockBush {
	private final boolean pitcher;
	@cpw.mods.fml.relauncher.SideOnly(cpw.mods.fml.relauncher.Side.CLIENT) private ganymedes01.etfuturum.client.renderer.block.ReleasedBlockModel[] models;
	@Override @cpw.mods.fml.relauncher.SideOnly(cpw.mods.fml.relauncher.Side.CLIENT) public void registerBlockIcons(net.minecraft.client.renderer.texture.IIconRegister atlas) { if (!pitcher) { super.registerBlockIcons(atlas); return; } models = new ganymedes01.etfuturum.client.renderer.block.ReleasedBlockModel[]{new ganymedes01.etfuturum.client.renderer.block.ReleasedBlockModel(atlas, "pitcher_plant_bottom"), new ganymedes01.etfuturum.client.renderer.block.ReleasedBlockModel(atlas, "pitcher_plant_top")}; blockIcon = models[0].particle; }
	@cpw.mods.fml.relauncher.SideOnly(cpw.mods.fml.relauncher.Side.CLIENT) public ganymedes01.etfuturum.client.renderer.block.ReleasedBlockModel getReleasedModel(int meta) { return models[(meta & 8) == 0 ? 0 : 1]; }
	@Override @cpw.mods.fml.relauncher.SideOnly(cpw.mods.fml.relauncher.Side.CLIENT) public net.minecraft.util.IIcon getIcon(int side, int meta) { return pitcher ? getReleasedModel(meta).particle : blockIcon; }
	@Override public int getRenderType() { return pitcher ? ganymedes01.etfuturum.lib.RenderIDs.RELEASED_BLOCK : 1; }
	public BlockSnifferPlant(boolean pitcher) { this.pitcher = pitcher; setBlockName("etfuturum." + (pitcher ? "pitcher_plant" : "torchflower")); setBlockTextureName(pitcher ? "pitcher_crop_bottom_stage_4" : "torchflower"); setStepSound(net.minecraft.block.Block.soundTypeGrass); setCreativeTab(ganymedes01.etfuturum.EtFuturum.creativeTabBlocks); }
	@Override public boolean canPlaceBlockAt(World world, int x, int y, int z) { return super.canPlaceBlockAt(world, x, y, z) && (!pitcher || y < 254 && world.isAirBlock(x, y + 1, z)); }
	@Override public void onBlockAdded(World world, int x, int y, int z) { if (pitcher && !world.isRemote && (world.getBlockMetadata(x, y, z) & 8) == 0 && world.isAirBlock(x, y + 1, z)) world.setBlock(x, y + 1, z, this, 8, 2); }
	@Override public boolean canBlockStay(World world, int x, int y, int z) { return pitcher && (world.getBlockMetadata(x, y, z) & 8) != 0 ? world.getBlock(x, y - 1, z) == this && (world.getBlockMetadata(x, y - 1, z) & 8) == 0 : super.canBlockStay(world, x, y, z); }
	@Override public void onNeighborBlockChange(World world, int x, int y, int z, Block neighbor) { if (pitcher && (world.getBlockMetadata(x, y, z) & 8) == 0 && world.getBlock(x, y + 1, z) != this) { if (!world.isRemote) { dropBlockAsItem(world, x, y, z, 0, 0); world.setBlockToAir(x, y, z); } } else super.onNeighborBlockChange(world, x, y, z, neighbor); }
	@Override public Item getItemDropped(int meta, Random random, int fortune) { return (meta & 8) != 0 ? null : Item.getItemFromBlock(this); }
}
