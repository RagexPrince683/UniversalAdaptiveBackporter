package ganymedes01.etfuturum;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.*;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
public final class CalibratedSculkSensor extends SculkSensor {
 public static final PropertyDirection FACING=BlockHorizontal.FACING;
 CalibratedSculkSensor(){setDefaultState(getDefaultState().withProperty(FACING,EnumFacing.NORTH));}
 @Override protected BlockStateContainer createBlockState(){return new BlockStateContainer(this,PHASE,FACING);}
 @Override public IBlockState getStateFromMeta(int meta){return super.getStateFromMeta(meta).withProperty(FACING,EnumFacing.byHorizontalIndex(meta&3));}
 @Override public int getMetaFromState(IBlockState state){return super.getMetaFromState(state)|state.getValue(FACING).getHorizontalIndex();}
 @Override public IBlockState getStateForPlacement(World world,BlockPos pos,EnumFacing side,float x,float y,float z,int meta,EntityLivingBase entity,EnumHand hand){return getDefaultState().withProperty(FACING,entity.getHorizontalFacing().getOpposite());}
 @Override public IBlockState withRotation(IBlockState state,Rotation rotation){return state.withProperty(FACING,rotation.rotate(state.getValue(FACING)));}
}
