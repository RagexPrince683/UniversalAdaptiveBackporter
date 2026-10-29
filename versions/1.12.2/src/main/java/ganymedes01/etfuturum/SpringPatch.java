package ganymedes01.etfuturum;
import java.util.*;
import net.minecraft.block.*;
import net.minecraft.block.properties.*;
import net.minecraft.block.state.*;
import net.minecraft.entity.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.*;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.world.*;
public final class SpringPatch extends BlockBush implements IGrowable {
    public static final PropertyInteger FLOWERS=PropertyInteger.create("flower_amount",1,4),SEGMENTS=PropertyInteger.create("segment_amount",1,4);public static final PropertyDirection FACING=BlockHorizontal.FACING;
    public final boolean litter;
    public SpringPatch(boolean litter){this.litter=litter;setSoundType(litter?SoundType.PLANT:SoundType.PLANT);setDefaultState(blockState.getBaseState().withProperty(FLOWERS,1).withProperty(SEGMENTS,1).withProperty(FACING,EnumFacing.NORTH));}
    private PropertyInteger amount(){return litter?SEGMENTS:FLOWERS;}
    @Override protected BlockStateContainer createBlockState(){return new BlockStateContainer(this,FLOWERS,SEGMENTS,FACING);}
    @Override public IBlockState getStateFromMeta(int meta){return getDefaultState().withProperty(amount(),(meta>>2)+1).withProperty(FACING,EnumFacing.byHorizontalIndex(meta&3));}
    @Override public int getMetaFromState(IBlockState state){return state.getValue(FACING).getHorizontalIndex()|(state.getValue(amount())-1)<<2;}
    @Override public IBlockState getStateForPlacement(World world,BlockPos pos,EnumFacing side,float x,float y,float z,int meta,EntityLivingBase placer,EnumHand hand){return getDefaultState().withProperty(FACING,placer.getHorizontalFacing().getOpposite());}
    @Override public IBlockState withRotation(IBlockState state,Rotation rotation){return state.withProperty(FACING,rotation.rotate(state.getValue(FACING)));}
    @Override public AxisAlignedBB getBoundingBox(IBlockState state,IBlockAccess world,BlockPos pos){return new AxisAlignedBB(0,0,0,1,.125,1);}
    @Override public void getDrops(NonNullList<ItemStack> drops,IBlockAccess world,BlockPos pos,IBlockState state,int fortune){drops.add(new ItemStack(this,state.getValue(amount())));}
    @Override public int damageDropped(IBlockState state){return 0;}
    @Override public boolean onBlockActivated(World world,BlockPos pos,IBlockState state,EntityPlayer player,EnumHand hand,EnumFacing side,float x,float y,float z){ItemStack held=player.getHeldItem(hand);if(held.getItem()!=Item.getItemFromBlock(this)||state.getValue(amount())==4||!player.canPlayerEdit(pos,side,held))return false;if(!world.isRemote){world.setBlockState(pos,state.withProperty(amount(),state.getValue(amount())+1),3);if(!player.capabilities.isCreativeMode)held.shrink(1);}return true;}
    @Override public boolean canGrow(World world,BlockPos pos,IBlockState state,boolean client){return !litter;}@Override public boolean canUseBonemeal(World world,Random random,BlockPos pos,IBlockState state){return !litter;}@Override public void grow(World world,Random random,BlockPos pos,IBlockState state){if(state.getValue(amount())<4)world.setBlockState(pos,state.withProperty(amount(),state.getValue(amount())+1),3);else spawnAsEntity(world,pos,new ItemStack(this));}
}
