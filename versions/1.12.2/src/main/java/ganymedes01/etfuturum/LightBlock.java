package ganymedes01.etfuturum;

import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.*;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.*;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.world.*;

public final class LightBlock extends Block {
    public static final PropertyInteger LEVEL=PropertyInteger.create("level",0,15);
    LightBlock(){super(Material.BARRIER);setBlockUnbreakable();setResistance(6000000);setLightOpacity(0);setDefaultState(blockState.getBaseState().withProperty(LEVEL,15));}
    @Override protected BlockStateContainer createBlockState(){return new BlockStateContainer(this,LEVEL);}
    @Override public IBlockState getStateFromMeta(int meta){return getDefaultState().withProperty(LEVEL,meta&15);}
    @Override public int getMetaFromState(IBlockState state){return state.getValue(LEVEL);}
    @Override public int damageDropped(IBlockState state){return state.getValue(LEVEL);}
    @Override public int getLightValue(IBlockState state,IBlockAccess world,BlockPos pos){return state.getValue(LEVEL);}
    @Override public EnumBlockRenderType getRenderType(IBlockState state){return EnumBlockRenderType.INVISIBLE;}
    @Override public boolean isOpaqueCube(IBlockState state){return false;}
    @Override public boolean isFullCube(IBlockState state){return false;}
    @Override public AxisAlignedBB getCollisionBoundingBox(IBlockState state,IBlockAccess world,BlockPos pos){return NULL_AABB;}
    @Override public boolean isReplaceable(IBlockAccess world,BlockPos pos){return true;}
    @Override public IBlockState getStateForPlacement(World world,BlockPos pos,EnumFacing side,float x,float y,float z,int meta,EntityLivingBase placer){return getStateFromMeta(meta);}
    @Override public boolean onBlockActivated(World world,BlockPos pos,IBlockState state,EntityPlayer player,EnumHand hand,EnumFacing side,float x,float y,float z){if(player.getHeldItem(hand).getItem()!=Item.getItemFromBlock(this)||!player.canPlayerEdit(pos,side,player.getHeldItem(hand)))return false;if(!world.isRemote)world.setBlockState(pos,state.withProperty(LEVEL,(state.getValue(LEVEL)+1)&15),3);return true;}
    @Override public void getSubBlocks(CreativeTabs tab,NonNullList<ItemStack> items){for(int i=0;i<16;i++)items.add(new ItemStack(this,1,i));}
}
