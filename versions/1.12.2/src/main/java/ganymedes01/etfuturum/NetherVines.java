package ganymedes01.etfuturum;

import java.util.*;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.*;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.*;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.world.*;
import net.minecraftforge.common.IShearable;

public final class NetherVines extends Block implements IGrowable,IShearable {
    public static final PropertyBool TIP=PropertyBool.create("tip"),STOPPED=PropertyBool.create("stopped");
    final boolean upward;
    NetherVines(boolean upward){super(Material.PLANTS);this.upward=upward;setHardness(0);setSoundType(SoundType.PLANT);setLightOpacity(0);setTickRandomly(true);setDefaultState(blockState.getBaseState().withProperty(TIP,true).withProperty(STOPPED,false));}
    @Override protected BlockStateContainer createBlockState(){return new BlockStateContainer(this,TIP,STOPPED);}
    @Override public IBlockState getStateFromMeta(int meta){return getDefaultState().withProperty(STOPPED,(meta&1)!=0);}
    @Override public int getMetaFromState(IBlockState state){return state.getValue(STOPPED)?1:0;}
    private EnumFacing direction(){return upward?EnumFacing.UP:EnumFacing.DOWN;}
    @Override public IBlockState getActualState(IBlockState state,IBlockAccess world,BlockPos pos){return state.withProperty(TIP,world.getBlockState(pos.offset(direction())).getBlock()!=this);}
    @Override public int damageDropped(IBlockState state){return 0;}
    @Override public boolean isOpaqueCube(IBlockState state){return false;}
    @Override public boolean isFullCube(IBlockState state){return false;}
    @Override public boolean isSideSolid(IBlockState state,IBlockAccess world,BlockPos pos,EnumFacing face){return false;}
    @Override public BlockFaceShape getBlockFaceShape(IBlockAccess world,IBlockState state,BlockPos pos,EnumFacing side){return BlockFaceShape.UNDEFINED;}
    @Override public BlockRenderLayer getRenderLayer(){return BlockRenderLayer.CUTOUT;}
    @Override public AxisAlignedBB getCollisionBoundingBox(IBlockState state,IBlockAccess world,BlockPos pos){return NULL_AABB;}
    @Override public AxisAlignedBB getBoundingBox(IBlockState state,IBlockAccess world,BlockPos pos){return upward?new AxisAlignedBB(.3,0,.3,.7,1,.7):getActualState(state,world,pos).getValue(TIP)?new AxisAlignedBB(.25,.6,.25,.75,1,.75):new AxisAlignedBB(.1,0,.1,.9,1,.9);}
    @Override public boolean isLadder(IBlockState state,IBlockAccess world,BlockPos pos,EntityLivingBase entity){return true;}
    private boolean supported(World world,BlockPos pos){BlockPos support=pos.offset(direction().getOpposite());IBlockState below=world.getBlockState(support);return below.getBlock()==this||below.isOpaqueCube();}
    @Override public boolean canPlaceBlockAt(World world,BlockPos pos){return pos.getY()>0&&pos.getY()<world.getHeight()-1&&world.isAirBlock(pos)&&supported(world,pos);}
    @Override public void neighborChanged(IBlockState state,World world,BlockPos pos,Block neighbor,BlockPos from){if(!world.isRemote&&!supported(world,pos))world.destroyBlock(pos,true);}
    @Override public int quantityDropped(IBlockState state,int fortune,Random random){return random.nextFloat()<.33F+fortune*.22F?1:0;}
    @Override public int quantityDropped(Random random){return quantityDropped(getDefaultState(),0,random);}
    @Override public boolean isShearable(ItemStack item,IBlockAccess world,BlockPos pos){return true;}
    @Override public List<ItemStack> onSheared(ItemStack item,IBlockAccess world,BlockPos pos,int fortune){return Collections.singletonList(new ItemStack(this));}
    private BlockPos tip(World world,BlockPos pos){for(int i=0;i<26&&world.isBlockLoaded(pos.offset(direction()))&&world.getBlockState(pos.offset(direction())).getBlock()==this;i++)pos=pos.offset(direction());return pos;}
    private int length(World world,BlockPos pos){int count=1;while(count<26&&world.isBlockLoaded(pos.offset(direction().getOpposite(),count))&&world.getBlockState(pos.offset(direction().getOpposite(),count)).getBlock()==this)count++;return count;}
    @Override public boolean canGrow(World world,BlockPos pos,IBlockState state,boolean client){BlockPos tip=tip(world,pos),next=tip.offset(direction());return !world.getBlockState(tip).getValue(STOPPED)&&length(world,tip)<26&&next.getY()>0&&next.getY()<world.getHeight()-1&&world.isBlockLoaded(next)&&world.isAirBlock(next);}
    @Override public boolean canUseBonemeal(World world,Random random,BlockPos pos,IBlockState state){return true;}
    @Override public void grow(World world,Random random,BlockPos pos,IBlockState state){if(world.isRemote)return;BlockPos tip=tip(world,pos);for(int i=0,n=1+random.nextInt(3);i<n&&canGrow(world,tip,world.getBlockState(tip),false);i++){tip=tip.offset(direction());world.setBlockState(tip,getDefaultState(),3);}}
    @Override public void updateTick(World world,BlockPos pos,IBlockState state,Random random){if(!world.isRemote&&random.nextFloat()<=.1F&&getActualState(state,world,pos).getValue(TIP)&&canGrow(world,pos,state,false))world.setBlockState(pos.offset(direction()),getDefaultState(),2);}
    @Override public boolean onBlockActivated(World world,BlockPos pos,IBlockState state,EntityPlayer player,EnumHand hand,EnumFacing side,float x,float y,float z){ItemStack held=player.getHeldItem(hand);if(!(held.getItem() instanceof ItemShears)||state.getValue(STOPPED)||!getActualState(state,world,pos).getValue(TIP)||!player.canPlayerEdit(pos,side,held))return false;if(!world.isRemote){world.setBlockState(pos,state.withProperty(STOPPED,true),3);if(!player.capabilities.isCreativeMode)held.damageItem(1,player);world.playSound(null,pos,SoundEvents.ENTITY_SHEEP_SHEAR,SoundCategory.BLOCKS,1,1);}return true;}
}
