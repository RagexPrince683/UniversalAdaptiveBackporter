package ganymedes01.etfuturum;

import java.util.Random;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.*;
import net.minecraft.block.state.*;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.*;

public final class CrafterBlock extends BlockContainer {
    public static final PropertyDirection FACING=PropertyDirection.create("facing");
    public static final PropertyBool TRIGGERED=PropertyBool.create("triggered"),CRAFTING=PropertyBool.create("crafting");
    CrafterBlock(){super(Material.ROCK);setHardness(1.5F);setResistance(3);setDefaultState(blockState.getBaseState().withProperty(FACING,EnumFacing.NORTH).withProperty(TRIGGERED,false).withProperty(CRAFTING,false));}
    @Override protected BlockStateContainer createBlockState(){return new BlockStateContainer(this,FACING,TRIGGERED,CRAFTING);}
    @Override public IBlockState getStateFromMeta(int meta){return getDefaultState().withProperty(FACING,EnumFacing.byIndex((meta&7)%6)).withProperty(TRIGGERED,(meta&8)!=0);}
    @Override public int getMetaFromState(IBlockState state){return state.getValue(FACING).getIndex()|(state.getValue(TRIGGERED)?8:0);}
    @Override public IBlockState getActualState(IBlockState state,IBlockAccess world,BlockPos pos){TileEntity tile=world.getTileEntity(pos);return state.withProperty(CRAFTING,tile instanceof CrafterTile&&((CrafterTile)tile).craftingTicks>0);}
    @Override public EnumBlockRenderType getRenderType(IBlockState state){return EnumBlockRenderType.MODEL;}
    @Override public TileEntity createNewTileEntity(World world,int meta){return new CrafterTile();}
    @Override public IBlockState getStateForPlacement(World world,BlockPos pos,EnumFacing face,float x,float y,float z,int meta,EntityLivingBase player){return getDefaultState().withProperty(FACING,EnumFacing.getDirectionFromEntityLiving(pos,player).getOpposite());}
    @Override public void neighborChanged(IBlockState state,World world,BlockPos pos,net.minecraft.block.Block neighbor,BlockPos from){power(world,pos);}
    @Override public void onBlockAdded(World world,BlockPos pos,IBlockState state){power(world,pos);}
    void power(World world,BlockPos pos){if(world.isRemote)return;IBlockState state=world.getBlockState(pos);if(state.getBlock()!=this)return;boolean powered=world.isBlockPowered(pos)||world.isBlockPowered(pos.up());if(powered==state.getValue(TRIGGERED))return;world.setBlockState(pos,state.withProperty(TRIGGERED,powered),3);TileEntity tile=world.getTileEntity(pos);if(powered&&tile instanceof CrafterTile)((CrafterTile)tile).pulse();}
    @Override public boolean onBlockActivated(World world,BlockPos pos,IBlockState state,EntityPlayer player,EnumHand hand,EnumFacing face,float x,float y,float z){if(!world.isRemote&&world.getTileEntity(pos) instanceof CrafterTile)player.openGui(UAB.instance,8,world,pos.getX(),pos.getY(),pos.getZ());return true;}
    @Override public boolean hasComparatorInputOverride(IBlockState state){return true;}
    @Override public int getComparatorInputOverride(IBlockState state,World world,BlockPos pos){TileEntity tile=world.getTileEntity(pos);return tile instanceof CrafterTile?((CrafterTile)tile).signal():0;}
    @Override public void breakBlock(World world,BlockPos pos,IBlockState state){TileEntity tile=world.getTileEntity(pos);if(tile instanceof CrafterTile){InventoryHelper.dropInventoryItems(world,pos,(CrafterTile)tile);((CrafterTile)tile).dropOutputs();world.updateComparatorOutputLevel(pos,this);}super.breakBlock(world,pos,state);}
    @Override public boolean rotateBlock(World world,BlockPos pos,EnumFacing axis){IBlockState state=world.getBlockState(pos);if(state.getValue(FACING)==axis)return false;return world.setBlockState(pos,state.withProperty(FACING,axis),3);}
}
