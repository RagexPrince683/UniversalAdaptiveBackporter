package ganymedes01.etfuturum;

import java.util.Random;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.*;
import net.minecraft.block.state.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.*;
import net.minecraft.item.*;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.world.*;

public final class CandleBlock extends Block {
    public static final PropertyInteger COUNT=PropertyInteger.create("candles",1,4);public static final PropertyBool LIT=PropertyBool.create("lit");
    CandleBlock(){super(Material.CIRCUITS);setHardness(.1F);setLightOpacity(0);setSoundType(SoundType.CLOTH);setDefaultState(blockState.getBaseState().withProperty(COUNT,1).withProperty(LIT,false));}
    @Override protected BlockStateContainer createBlockState(){return new BlockStateContainer(this,COUNT,LIT);}
    @Override public IBlockState getStateFromMeta(int meta){return getDefaultState().withProperty(COUNT,1+(meta&3)).withProperty(LIT,(meta&4)!=0);}
    @Override public int getMetaFromState(IBlockState state){return state.getValue(COUNT)-1+(state.getValue(LIT)?4:0);}
    @Override public int damageDropped(IBlockState state){return 0;}
    @Override public int quantityDropped(IBlockState state,int fortune,Random random){return state.getValue(COUNT);}
    @Override public boolean isOpaqueCube(IBlockState state){return false;}
    @Override public boolean isFullCube(IBlockState state){return false;}
    @Override public int getLightValue(IBlockState state,IBlockAccess world,BlockPos pos){return state.getValue(LIT)?state.getValue(COUNT)*3:0;}
    @Override public BlockRenderLayer getRenderLayer(){return BlockRenderLayer.CUTOUT;}
    @Override public AxisAlignedBB getBoundingBox(IBlockState state,IBlockAccess world,BlockPos pos){return new AxisAlignedBB(.25,0,.25,.75,.5,.75);}
    @Override public boolean canPlaceBlockAt(World world,BlockPos pos){return super.canPlaceBlockAt(world,pos)&&world.getBlockState(pos.down()).isSideSolid(world,pos.down(),EnumFacing.UP);}
    @Override public void neighborChanged(IBlockState state,World world,BlockPos pos,Block block,BlockPos from){if(!world.isRemote&&!world.getBlockState(pos.down()).isSideSolid(world,pos.down(),EnumFacing.UP))world.destroyBlock(pos,true);}
    @Override public boolean onBlockActivated(World world,BlockPos pos,IBlockState state,EntityPlayer player,EnumHand hand,EnumFacing face,float x,float y,float z){ItemStack held=player.getHeldItem(hand);if(!player.canPlayerEdit(pos,face,held))return false;if(held.getItem()==Item.getItemFromBlock(this)&&state.getValue(COUNT)<4){if(!world.isRemote){world.setBlockState(pos,state.cycleProperty(COUNT),3);if(!player.capabilities.isCreativeMode)held.shrink(1);}return true;}boolean ignite=held.getItem()==Items.FLINT_AND_STEEL||held.getItem()==Items.FIRE_CHARGE;if(!state.getValue(LIT)&&ignite){if(!world.isRemote){world.setBlockState(pos,state.withProperty(LIT,true),3);if(!player.capabilities.isCreativeMode){if(held.getItem()==Items.FLINT_AND_STEEL)held.damageItem(1,player);else held.shrink(1);}}return true;}if(state.getValue(LIT)&&held.isEmpty()){if(!world.isRemote)world.setBlockState(pos,state.withProperty(LIT,false),3);return true;}return false;}
    @Override public void randomDisplayTick(IBlockState state,World world,BlockPos pos,Random random){if(state.getValue(LIT))for(int i=0;i<state.getValue(COUNT);i++)world.spawnParticle(EnumParticleTypes.FLAME,pos.getX()+.4+random.nextDouble()*.2,pos.getY()+.5,pos.getZ()+.4+random.nextDouble()*.2,0,0,0);}
}
