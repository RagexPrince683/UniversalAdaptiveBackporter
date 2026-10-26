package ganymedes01.etfuturum;

import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockRenderLayer;

/** The maintained legacy adaptation displaces water instead of introducing waterlogging. */
final class MangroveRoots extends Block {
    MangroveRoots() { super(Material.WOOD); setHardness(.7F); setSoundType(SoundType.WOOD); }
    @Override public boolean isOpaqueCube(IBlockState state) { return false; }
    @Override public BlockRenderLayer getRenderLayer() { return BlockRenderLayer.CUTOUT; }
}
