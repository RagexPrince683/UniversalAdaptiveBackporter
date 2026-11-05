package ganymedes01.etfuturum;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.IPlantable;

/** Mud is plantable soft ground, including its vanilla seven-eighths collision height. */
final class MudBlock extends Block {
    MudBlock() {
        super(Material.GROUND);
        setHardness(.5F);
        setResistance(.5F);
        setSoundType(SoundType.GROUND);
        setHarvestLevel("shovel", 0);
    }
    @Override public boolean canSustainPlant(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side, IPlantable plant) {
        return Blocks.DIRT.canSustainPlant(Blocks.DIRT.getDefaultState(), world, pos, side, plant);
    }
    @Override public AxisAlignedBB getCollisionBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {
        return new AxisAlignedBB(0, 0, 0, 1, .875, 1);
    }
}
