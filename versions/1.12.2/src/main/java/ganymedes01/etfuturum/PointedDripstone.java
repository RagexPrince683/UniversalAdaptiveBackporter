package ganymedes01.etfuturum;

import java.util.Random;
import ganymedes01.etfuturum.entities.EntityFallingDripstone;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.*;
import net.minecraft.block.state.*;
import net.minecraft.entity.*;
import net.minecraft.init.Blocks;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.world.*;

public final class PointedDripstone extends Block {
    public enum Thickness implements IStringSerializable { TIP,FRUSTUM,MIDDLE,BASE,TIP_MERGE;@Override public String getName(){return name().toLowerCase(java.util.Locale.ROOT);} }
    public static final PropertyDirection DIRECTION=PropertyDirection.create("vertical_direction",face->face.getAxis()==EnumFacing.Axis.Y);
    public static final PropertyEnum<Thickness> THICKNESS=PropertyEnum.create("thickness",Thickness.class);
    public static final DamageSource STALACTITE=new DamageSource("stalactite");
    PointedDripstone(){super(Material.ROCK);setHardness(1.5F);setResistance(3);setHarvestLevel("pickaxe",0);setTickRandomly(true);setDefaultState(blockState.getBaseState().withProperty(DIRECTION,EnumFacing.DOWN).withProperty(THICKNESS,Thickness.TIP));}
    @Override protected BlockStateContainer createBlockState(){return new BlockStateContainer(this,DIRECTION,THICKNESS);}
    @Override public IBlockState getStateFromMeta(int meta){return getDefaultState().withProperty(DIRECTION,meta>=5?EnumFacing.UP:EnumFacing.DOWN).withProperty(THICKNESS,Thickness.values()[Math.min(4,meta%5)]);}
    @Override public int getMetaFromState(IBlockState state){return state.getValue(THICKNESS).ordinal()+(state.getValue(DIRECTION)==EnumFacing.UP?5:0);}
    @Override public int damageDropped(IBlockState state){return 0;}
    @Override public boolean isOpaqueCube(IBlockState state){return false;}
    @Override public boolean isFullCube(IBlockState state){return false;}
    @Override public BlockRenderLayer getRenderLayer(){return BlockRenderLayer.CUTOUT;}
    @Override public BlockFaceShape getBlockFaceShape(IBlockAccess world,IBlockState state,BlockPos pos,EnumFacing face){return BlockFaceShape.UNDEFINED;}
    @Override public boolean isSideSolid(IBlockState state,IBlockAccess world,BlockPos pos,EnumFacing face){return false;}
    private boolean supported(World world,BlockPos pos,EnumFacing direction){BlockPos support=pos.offset(direction.getOpposite());if(!world.isBlockLoaded(support))return true;IBlockState state=world.getBlockState(support);return state.isSideSolid(world,support,direction)||state.getBlock()==this&&state.getValue(DIRECTION)==direction;}
    @Override public boolean canPlaceBlockOnSide(World world,BlockPos pos,EnumFacing face){return face.getAxis()==EnumFacing.Axis.Y?supported(world,pos,face):supported(world,pos,EnumFacing.DOWN)||supported(world,pos,EnumFacing.UP);}
    @Override public IBlockState getStateForPlacement(World world,BlockPos pos,EnumFacing face,float x,float y,float z,int meta,EntityLivingBase player){EnumFacing direction=face.getAxis()==EnumFacing.Axis.Y?face:supported(world,pos,EnumFacing.DOWN)?EnumFacing.DOWN:EnumFacing.UP;return getDefaultState().withProperty(DIRECTION,direction);}
    @Override public IBlockState getActualState(IBlockState state,IBlockAccess world,BlockPos pos){if(world.getBlockState(pos).getBlock()!=this)return state;EnumFacing direction=state.getValue(DIRECTION);IBlockState next=world.getBlockState(pos.offset(direction));Thickness shape=Thickness.TIP;if(next.getBlock()==this){if(next.getValue(DIRECTION)!=direction)shape=Thickness.TIP_MERGE;else{IBlockState farther=world.getBlockState(pos.offset(direction,2));shape=farther.getBlock()==this&&farther.getValue(DIRECTION)==direction?Thickness.MIDDLE:Thickness.FRUSTUM;if(shape==Thickness.MIDDLE&&world.getBlockState(pos.offset(direction.getOpposite())).getBlock()!=this)shape=Thickness.BASE;}}return state.withProperty(THICKNESS,shape);}
    @Override public AxisAlignedBB getBoundingBox(IBlockState state,IBlockAccess world,BlockPos pos){Thickness shape=getActualState(state,world,pos).getValue(THICKNESS);double inset=shape==Thickness.TIP||shape==Thickness.TIP_MERGE?.1875:shape==Thickness.FRUSTUM?.125:.0625;return new AxisAlignedBB(inset,0,inset,1-inset,1,1-inset);}
    @Override public void onBlockAdded(World world,BlockPos pos,IBlockState state){neighborChanged(state,world,pos,this,pos);}
    @Override public void neighborChanged(IBlockState state,World world,BlockPos pos,Block block,BlockPos from){if(world.isRemote)return;if(!supported(world,pos,state.getValue(DIRECTION))){if(state.getValue(DIRECTION)==EnumFacing.DOWN)world.scheduleUpdate(pos,this,2);else world.destroyBlock(pos,true);}else{IBlockState actual=getActualState(state,world,pos);if(actual!=state)world.setBlockState(pos,actual,2);}}
    @Override public void onFallenUpon(World world,BlockPos pos,Entity entity,float distance){IBlockState state=world.getBlockState(pos);if(state.getValue(DIRECTION)==EnumFacing.UP&&getActualState(state,world,pos).getValue(THICKNESS)==Thickness.TIP)entity.fall(distance+2,2);else super.onFallenUpon(world,pos,entity,distance);}
    private void collapse(World world,BlockPos pos){int count=0;while(count<256&&pos.getY()-count>=0){IBlockState below=world.getBlockState(pos.down(count));if(below.getBlock()!=this||below.getValue(DIRECTION)!=EnumFacing.DOWN)break;count++;}if(count==0)return;EntityFallingDripstone falling=new EntityFallingDripstone(world,pos.getX()+.5,pos.getY()-count+1.5,pos.getZ()+.5,getDefaultState(),count);if(!world.spawnEntity(falling))return;for(int i=0;i<count;i++)world.setBlockState(pos.down(i),Blocks.AIR.getDefaultState(),2);world.notifyNeighborsOfStateChange(pos,this,false);}
    @Override public void updateTick(World world,BlockPos pos,IBlockState state,Random random){if(world.isRemote||!world.isAreaLoaded(pos,1))return;if(!supported(world,pos,state.getValue(DIRECTION))){if(state.getValue(DIRECTION)==EnumFacing.DOWN)collapse(world,pos);else world.destroyBlock(pos,true);return;}IBlockState actual=getActualState(state,world,pos);if(actual!=state)world.setBlockState(pos,actual,2);if(actual.getValue(DIRECTION)!=EnumFacing.DOWN||actual.getValue(THICKNESS)!=Thickness.TIP)return;BlockPos base=pos;while(base.getY()-pos.getY()<10&&world.getBlockState(base.up()).getBlock()==this&&world.getBlockState(base.up()).getValue(DIRECTION)==EnumFacing.DOWN)base=base.up();if(base.getY()-pos.getY()>=10)return;
        if(!world.provider.isNether()&&Content.available("mud")&&world.getBlockState(base.up(2)).getBlock()==Content.block("mud")&&random.nextInt(256)<45)world.setBlockState(base.up(2),Blocks.CLAY.getDefaultState(),3);
        IBlockState fluid=world.getBlockState(base.up(2));if(fluid.getMaterial()!=Material.WATER&&fluid.getMaterial()!=Material.LAVA||fluid.getBlock() instanceof BlockLiquid&&fluid.getValue(BlockLiquid.LEVEL)!=0)return;
        for(BlockPos target=pos.down();target.getY()>=Math.max(0,pos.getY()-11);target=target.down()){IBlockState below=world.getBlockState(target);if(below.getBlock()==Blocks.CAULDRON){int level=below.getValue(BlockCauldron.LEVEL);if(fluid.getMaterial()==Material.WATER&&level<3&&random.nextInt(256)<45)world.setBlockState(target,below.withProperty(BlockCauldron.LEVEL,level+1),3);else if(fluid.getMaterial()==Material.LAVA&&level==0&&Content.available("lava_cauldron")&&random.nextInt(256)<15)world.setBlockState(target,Content.block("lava_cauldron").getDefaultState().withProperty(BlockCauldron.LEVEL,3),3);break;}if(!world.isAirBlock(target))break;}
        if(fluid.getMaterial()!=Material.WATER||base.getY()-pos.getY()>=7||world.getBlockState(base.up()).getBlock()!=Content.block("dripstone_block")||random.nextInt(5625)>=64)return;
        if(random.nextBoolean()){if(pos.getY()>1&&world.isAirBlock(pos.down()))world.setBlockState(pos.down(),getDefaultState(),3);}else for(BlockPos target=pos.down();target.getY()>Math.max(0,pos.getY()-11);target=target.down()){IBlockState below=world.getBlockState(target);if(below.getBlock()==this&&below.getValue(DIRECTION)==EnumFacing.UP){if(world.isAirBlock(target.up()))world.setBlockState(target.up(),getDefaultState().withProperty(DIRECTION,EnumFacing.UP),3);break;}if(!world.isAirBlock(target))break;if(world.getBlockState(target.down()).isSideSolid(world,target.down(),EnumFacing.UP)){world.setBlockState(target,getDefaultState().withProperty(DIRECTION,EnumFacing.UP),3);break;}}
    }
}
