package ganymedes01.etfuturum.client;

import ganymedes01.etfuturum.AquaticPlant;
import ganymedes01.etfuturum.DriedGhast;
import ganymedes01.etfuturum.CampfireBlock;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.Biome;

/** Read-only view used only while tessellating the water contained by UAB blocks. */
public final class ContainedWaterAccess implements IBlockAccess {
    private final IBlockAccess delegate;
    public ContainedWaterAccess(IBlockAccess delegate) { this.delegate = delegate; }
    public static boolean containsWater(IBlockState state) {
        return state.getBlock() instanceof AquaticPlant && ((AquaticPlant) state.getBlock()).submerged
                || state.getBlock() instanceof DriedGhast && state.getValue(DriedGhast.WATERLOGGED)
                || state.getBlock() instanceof CampfireBlock && state.getValue(CampfireBlock.WATERLOGGED);
    }
    @Override public IBlockState getBlockState(BlockPos pos) {
        IBlockState state = delegate.getBlockState(pos);
        return containsWater(state) ? Blocks.WATER.getDefaultState() : state;
    }
    @Override public TileEntity getTileEntity(BlockPos pos) { return delegate.getTileEntity(pos); }
    @Override public int getCombinedLight(BlockPos pos, int light) { return delegate.getCombinedLight(pos, light); }
    @Override public boolean isAirBlock(BlockPos pos) { return delegate.isAirBlock(pos); }
    @Override public Biome getBiome(BlockPos pos) { return delegate.getBiome(pos); }
    @Override public int getStrongPower(BlockPos pos, EnumFacing side) { return delegate.getStrongPower(pos, side); }
    @Override public WorldType getWorldType() { return delegate.getWorldType(); }
    @Override public boolean isSideSolid(BlockPos pos, EnumFacing side, boolean fallback) { return delegate.isSideSolid(pos, side, fallback); }
}
