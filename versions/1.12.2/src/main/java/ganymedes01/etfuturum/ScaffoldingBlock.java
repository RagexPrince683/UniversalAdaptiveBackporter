package ganymedes01.etfuturum;

import java.util.*;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.*;
import net.minecraft.block.state.*;
import net.minecraft.entity.*;
import net.minecraft.entity.item.EntityFallingBlock;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.*;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.world.*;

/** Local support updates and native falling entities; no terrain or movement-engine replacement. */
public final class ScaffoldingBlock extends Block {
    public static final PropertyInteger DISTANCE=PropertyInteger.create("distance",0,7);
    public static final PropertyBool BOTTOM=PropertyBool.create("bottom");
    ScaffoldingBlock(){super(Material.WOOD);setHardness(0);setLightOpacity(0);setSoundType(net.minecraft.block.SoundType.WOOD);setDefaultState(blockState.getBaseState().withProperty(DISTANCE,7).withProperty(BOTTOM,true));}
    @Override protected BlockStateContainer createBlockState(){return new BlockStateContainer(this,DISTANCE,BOTTOM);}
    @Override public IBlockState getStateFromMeta(int meta){return getDefaultState().withProperty(DISTANCE,meta&7).withProperty(BOTTOM,(meta&8)!=0);}
    @Override public int getMetaFromState(IBlockState state){return state.getValue(DISTANCE)|(state.getValue(BOTTOM)?8:0);}
    public int distance(IBlockAccess world,BlockPos pos){IBlockState below=world.getBlockState(pos.down());int distance=below.getBlock()==this?below.getValue(DISTANCE):below.isSideSolid(world,pos.down(),EnumFacing.UP)?0:7;for(EnumFacing side:EnumFacing.HORIZONTALS){IBlockState neighbor=world.getBlockState(pos.offset(side));if(neighbor.getBlock()==this)distance=Math.min(distance,neighbor.getValue(DISTANCE)+1);}return Math.min(7,distance);}
    private IBlockState supported(IBlockAccess world,BlockPos pos){int distance=distance(world,pos);return getDefaultState().withProperty(DISTANCE,distance).withProperty(BOTTOM,distance>0&&world.getBlockState(pos.down()).getBlock()!=this);}
    @Override public IBlockState getStateForPlacement(World world,BlockPos pos,EnumFacing side,float x,float y,float z,int meta,EntityLivingBase player){return supported(world,pos);}
    @Override public void onBlockAdded(World world,BlockPos pos,IBlockState state){schedule(world,pos);}
    @Override public void neighborChanged(IBlockState state,World world,BlockPos pos,Block block,BlockPos from){schedule(world,pos);}
    private void schedule(World world,BlockPos pos){if(!world.isRemote)world.scheduleUpdate(pos,this,1);}
    @Override public void updateTick(World world,BlockPos pos,IBlockState state,Random random){
        if(world.isRemote)return;
        if(!world.isAreaLoaded(new BlockPos(pos.getX()-1,Math.max(0,pos.getY()-1),pos.getZ()-1),pos.add(1,0,1))){world.scheduleUpdate(pos,this,20);return;}
        IBlockState next=supported(world,pos);
        if(next.getValue(DISTANCE)==7){
            if(state.getValue(DISTANCE)<7){world.destroyBlock(pos,true);return;}
            EntityFallingBlock falling=new EntityFallingBlock(world,pos.getX()+.5,pos.getY(),pos.getZ()+.5,state);
            if(!world.spawnEntity(falling))world.scheduleUpdate(pos,this,20);
        }else if(next!=state)world.setBlockState(pos,next,3);
    }
    @Override public boolean isOpaqueCube(IBlockState state){return false;}
    @Override public boolean isFullCube(IBlockState state){return false;}
    @Override public BlockRenderLayer getRenderLayer(){return BlockRenderLayer.CUTOUT;}
    @Override public net.minecraft.block.state.BlockFaceShape getBlockFaceShape(IBlockAccess world,IBlockState state,BlockPos pos,EnumFacing face){return net.minecraft.block.state.BlockFaceShape.UNDEFINED;}
    @Override public boolean isSideSolid(IBlockState state,IBlockAccess world,BlockPos pos,EnumFacing side){return false;}
    @Override public boolean isLadder(IBlockState state,IBlockAccess world,BlockPos pos,EntityLivingBase entity){return !entity.isSneaking();}
    @Override public AxisAlignedBB getCollisionBoundingBox(IBlockState state,IBlockAccess world,BlockPos pos){return new AxisAlignedBB(0,.875,0,1,1,1);}
    @Override public void addCollisionBoxToList(IBlockState state,World world,BlockPos pos,AxisAlignedBB box,List<AxisAlignedBB> result,Entity entity,boolean actual){
        if(entity!=null&&entity.isSneaking())return;
        double feet=entity==null?Double.POSITIVE_INFINITY:entity.getEntityBoundingBox().minY;
        if(feet>=pos.getY()+1-.001)addCollisionBoxToList(pos,box,result,new AxisAlignedBB(0,.875,0,1,1,1));
        else if(state.getValue(BOTTOM)&&feet>=pos.getY()+.125-.001)addCollisionBoxToList(pos,box,result,new AxisAlignedBB(0,0,0,1,.125,1));
    }
    @Override public void onEntityCollision(World world,BlockPos pos,IBlockState state,Entity entity){if(!(entity instanceof EntityLivingBase)||entity instanceof EntityPlayer&&((EntityPlayer)entity).capabilities.isFlying)return;entity.fallDistance=0;entity.motionX=MathHelper.clamp(entity.motionX,-.15,.15);entity.motionZ=MathHelper.clamp(entity.motionZ,-.15,.15);entity.motionY=entity.isSneaking()?-.15:LivingRules.jumping((EntityLivingBase)entity)?.2:Math.max(-.15,entity.motionY);}
    @Override public void onFallenUpon(World world,BlockPos pos,Entity entity,float distance){entity.fallDistance=0;}
    public static final class ScaffoldItem extends ItemBlock {
        public ScaffoldItem(Block block){super(block);}
        @Override public int getItemBurnTime(ItemStack stack){return 400;}
        @Override public EnumActionResult onItemUse(EntityPlayer player,World world,BlockPos pos,EnumHand hand,EnumFacing side,float x,float y,float z){
            if(world.getBlockState(pos).getBlock()!=block)return super.onItemUse(player,world,pos,hand,side,x,y,z);
            EnumFacing direction=player.isSneaking()?side:side==EnumFacing.UP?player.getHorizontalFacing():EnumFacing.UP;
            BlockPos target=pos;for(int step=0;step<world.getHeight();step++){
                target=target.offset(direction);if(target.getY()<0||target.getY()>=world.getHeight()||!world.isBlockLoaded(target))return EnumActionResult.FAIL;
                if(direction.getAxis()!=EnumFacing.Axis.Y&&step>=6)return EnumActionResult.FAIL;
                IBlockState state=world.getBlockState(target);if(state.getBlock()==block)continue;
                if(!state.getBlock().isReplaceable(world,target))return EnumActionResult.FAIL;
                return super.onItemUse(player,world,target,hand,EnumFacing.UP,x,y,z);
            }return EnumActionResult.FAIL;
        }
    }
}
