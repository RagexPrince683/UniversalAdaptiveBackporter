package ganymedes01.etfuturum;

import java.util.Random;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.*;
import net.minecraft.block.state.*;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.world.*;

/** Native block, inventory and container packets own all lectern interactions. */
public final class LecternBlock extends Block {
    public static final PropertyDirection FACING=BlockHorizontal.FACING;
    public static final PropertyBool POWERED=PropertyBool.create("powered"),HAS_BOOK=PropertyBool.create("has_book");
    public LecternBlock(){super(Material.WOOD);setHardness(2.5F);setSoundType(SoundType.WOOD);setDefaultState(blockState.getBaseState().withProperty(FACING,EnumFacing.NORTH).withProperty(POWERED,false).withProperty(HAS_BOOK,false));net.minecraft.init.Blocks.FIRE.setFireInfo(this,30,20);}
    @Override protected BlockStateContainer createBlockState(){return new BlockStateContainer(this,FACING,POWERED,HAS_BOOK);}
    @Override public IBlockState getStateFromMeta(int meta){return getDefaultState().withProperty(FACING,EnumFacing.byHorizontalIndex(meta&3)).withProperty(POWERED,(meta&4)!=0).withProperty(HAS_BOOK,(meta&8)!=0);}
    @Override public int getMetaFromState(IBlockState state){return state.getValue(FACING).getHorizontalIndex()|(state.getValue(POWERED)?4:0)|(state.getValue(HAS_BOOK)?8:0);}
    @Override public IBlockState getStateForPlacement(World world,BlockPos pos,EnumFacing side,float x,float y,float z,int meta,EntityLivingBase entity,EnumHand hand){return getDefaultState().withProperty(FACING,entity.getHorizontalFacing().getOpposite());}
    @Override public IBlockState withRotation(IBlockState state,Rotation rotation){return state.withProperty(FACING,rotation.rotate(state.getValue(FACING)));}
    @Override public boolean isOpaqueCube(IBlockState state){return false;}
    @Override public boolean isFullCube(IBlockState state){return false;}
    @Override public boolean hasTileEntity(IBlockState state){return true;}
    @Override public TileEntity createTileEntity(World world,IBlockState state){return new LecternTile();}
    @Override public boolean onBlockActivated(World world,BlockPos pos,IBlockState state,EntityPlayer player,EnumHand hand,EnumFacing side,float x,float y,float z){
        TileEntity raw=world.getTileEntity(pos);if(!(raw instanceof LecternTile))return false;LecternTile tile=(LecternTile)raw;
        if(!tile.isEmpty()){if(!world.isRemote)player.openGui(UAB.instance,7,world,pos.getX(),pos.getY(),pos.getZ());return true;}
        ItemStack held=player.getHeldItem(hand);if(!LecternTile.book(held)||!player.canPlayerEdit(pos,side,held))return false;
        if(!world.isRemote){ItemStack copy=held.copy();copy.setCount(1);tile.setInventorySlotContents(0,copy);if(!player.capabilities.isCreativeMode)held.shrink(1);player.inventory.markDirty();}return true;
    }
    @Override public void breakBlock(World world,BlockPos pos,IBlockState state){TileEntity raw=world.getTileEntity(pos);if(raw instanceof LecternTile)InventoryHelper.dropInventoryItems(world,pos,(LecternTile)raw);super.breakBlock(world,pos,state);world.updateComparatorOutputLevel(pos,this);if(state.getValue(POWERED))world.notifyNeighborsOfStateChange(pos.down(),this,false);}
    @Override public boolean hasComparatorInputOverride(IBlockState state){return true;}
    @Override public int getComparatorInputOverride(IBlockState state,World world,BlockPos pos){TileEntity raw=world.getTileEntity(pos);return raw instanceof LecternTile?((LecternTile)raw).signal():0;}
    @Override public boolean canProvidePower(IBlockState state){return true;}
    @Override public int getWeakPower(IBlockState state,IBlockAccess world,BlockPos pos,EnumFacing side){return state.getValue(POWERED)?15:0;}
    @Override public int getStrongPower(IBlockState state,IBlockAccess world,BlockPos pos,EnumFacing side){return side==EnumFacing.UP?getWeakPower(state,world,pos,side):0;}
    @Override public void updateTick(World world,BlockPos pos,IBlockState state,Random random){TileEntity raw=world.getTileEntity(pos);if(raw instanceof LecternTile)((LecternTile)raw).endPulse();}
}
