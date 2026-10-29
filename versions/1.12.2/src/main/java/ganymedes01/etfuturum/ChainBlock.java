package ganymedes01.etfuturum;

import net.minecraft.block.BlockRotatedPillar;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.world.IBlockAccess;

public class ChainBlock extends BlockRotatedPillar {
    ChainBlock() { super(Material.IRON); setHardness(5); setResistance(6); setHarvestLevel("pickaxe", 0); setLightOpacity(0); setSoundType(net.minecraft.block.SoundType.METAL); }
    @Override public boolean isOpaqueCube(IBlockState state) { return false; }
    @Override public boolean isFullCube(IBlockState state) { return false; }
    @Override public BlockRenderLayer getRenderLayer() { return BlockRenderLayer.CUTOUT; }
    @Override public boolean isSideSolid(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side) { return false; }
    @Override public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {
        switch (state.getValue(AXIS)) {
            case X: return new AxisAlignedBB(0, .40625, .40625, 1, .59375, .59375);
            case Z: return new AxisAlignedBB(.40625, .40625, 0, .59375, .59375, 1);
            default: return new AxisAlignedBB(.40625, 0, .40625, .59375, 1, .59375);
        }
    }
}
