package ganymedes01.etfuturum;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.*;
import net.minecraft.block.state.*;
import net.minecraft.entity.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.world.*;
public final class GrindstoneBlock extends Block {
    public enum Face implements IStringSerializable{FLOOR,WALL,CEILING;public String getName(){return name().toLowerCase(java.util.Locale.ROOT);}}
    public static final PropertyEnum<Face> FACE=PropertyEnum.create("face",Face.class);public static final PropertyDirection FACING=BlockHorizontal.FACING;
    public GrindstoneBlock(){super(Material.ROCK);setHardness(2);setResistance(6);setHarvestLevel("pickaxe",0);setDefaultState(blockState.getBaseState().withProperty(FACE,Face.FLOOR).withProperty(FACING,EnumFacing.NORTH));}
    @Override protected BlockStateContainer createBlockState(){return new BlockStateContainer(this,FACE,FACING);}@Override public IBlockState getStateFromMeta(int meta){return getDefaultState().withProperty(FACE,Face.values()[Math.min(2,meta>>2)]).withProperty(FACING,EnumFacing.byHorizontalIndex(meta&3));}@Override public int getMetaFromState(IBlockState state){return state.getValue(FACING).getHorizontalIndex()|state.getValue(FACE).ordinal()<<2;}
    @Override public IBlockState getStateForPlacement(World world,BlockPos pos,EnumFacing side,float x,float y,float z,int meta,EntityLivingBase entity,EnumHand hand){return getDefaultState().withProperty(FACE,side==EnumFacing.UP?Face.FLOOR:side==EnumFacing.DOWN?Face.CEILING:Face.WALL).withProperty(FACING,side.getAxis().isHorizontal()?side:entity.getHorizontalFacing());}
    @Override public boolean isFullCube(IBlockState state){return false;}@Override public boolean isOpaqueCube(IBlockState state){return false;}@Override public int damageDropped(IBlockState state){return 0;}@Override public BlockRenderLayer getRenderLayer(){return BlockRenderLayer.CUTOUT;}
    @Override public AxisAlignedBB getBoundingBox(IBlockState state,IBlockAccess access,BlockPos pos){boolean x=state.getValue(FACING).getAxis()==EnumFacing.Axis.X;if(state.getValue(FACE)==Face.WALL)return x?new AxisAlignedBB(0,.375,.125,1,.625,.875):new AxisAlignedBB(.125,.375,0,.875,.625,1);return x?new AxisAlignedBB(.375,0,.125,.625,1,.875):new AxisAlignedBB(.125,0,.375,.875,1,.625);}
    @Override public boolean canPlaceBlockOnSide(World world,BlockPos pos,EnumFacing face){BlockPos support=pos.offset(face.getOpposite());return world.getBlockState(support).isSideSolid(world,support,face);}
    @Override public void neighborChanged(IBlockState state,World world,BlockPos pos,Block block,BlockPos from){if(world.isRemote)return;EnumFacing face=state.getValue(FACE)==Face.FLOOR?EnumFacing.UP:state.getValue(FACE)==Face.CEILING?EnumFacing.DOWN:state.getValue(FACING);if(!canPlaceBlockOnSide(world,pos,face)){dropBlockAsItem(world,pos,state,0);world.setBlockToAir(pos);}}
    @Override public boolean onBlockActivated(World world,BlockPos pos,IBlockState state,EntityPlayer player,EnumHand hand,EnumFacing side,float x,float y,float z){if(!world.isRemote)player.openGui(UAB.instance,6,world,pos.getX(),pos.getY(),pos.getZ());return true;}
}
