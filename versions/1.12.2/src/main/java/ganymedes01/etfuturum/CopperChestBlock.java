package ganymedes01.etfuturum;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.*;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.*;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.world.*;
public final class CopperChestBlock extends Block {
    public static final PropertyDirection FACING=BlockHorizontal.FACING;
    public final int stage;public final boolean waxed;
    CopperChestBlock(){this(0,false);}
    CopperChestBlock(int stage,boolean waxed){super(Material.IRON);this.stage=stage;this.waxed=waxed;setTickRandomly(!waxed);setHardness(2.5F);setDefaultState(blockState.getBaseState().withProperty(FACING,EnumFacing.SOUTH));}
    @Override protected BlockStateContainer createBlockState(){return new BlockStateContainer(this,FACING);}
    @Override public IBlockState getStateFromMeta(int meta){return getDefaultState().withProperty(FACING,EnumFacing.byHorizontalIndex(meta&3));}
    @Override public int getMetaFromState(IBlockState state){return state.getValue(FACING).getHorizontalIndex();}
    @Override public IBlockState getStateForPlacement(World world,BlockPos pos,EnumFacing side,float x,float y,float z,int meta,EntityLivingBase placer,EnumHand hand){return getDefaultState().withProperty(FACING,placer.getHorizontalFacing().getOpposite());}
    @Override public IBlockState withRotation(IBlockState state,Rotation rotation){return state.withProperty(FACING,rotation.rotate(state.getValue(FACING)));}
    @Override public IBlockState withMirror(IBlockState state,Mirror mirror){return withRotation(state,mirror.toRotation(state.getValue(FACING)));}
    @Override public boolean hasTileEntity(IBlockState state){return true;}
    @Override public TileEntity createTileEntity(World world,IBlockState state){return new CopperChestTile();}
    @Override public EnumBlockRenderType getRenderType(IBlockState state){return EnumBlockRenderType.ENTITYBLOCK_ANIMATED;}
    @Override public boolean isOpaqueCube(IBlockState state){return false;}
    @Override public boolean isFullCube(IBlockState state){return false;}
    @Override public AxisAlignedBB getBoundingBox(IBlockState state,IBlockAccess world,BlockPos pos){return new AxisAlignedBB(.0625,0,.0625,.9375,.875,.9375);}
    @Override public boolean onBlockActivated(World world,BlockPos pos,IBlockState state,EntityPlayer player,EnumHand hand,EnumFacing side,float x,float y,float z){if(CopperAging.use(world,pos,state,player,hand,side))return true;if(!world.isRemote&&!world.getBlockState(pos.up()).isNormalCube()&&world.getTileEntity(pos) instanceof CopperChestTile)player.displayGUIChest((CopperChestTile)world.getTileEntity(pos));return true;}
    @Override public void onBlockPlacedBy(World world,BlockPos pos,IBlockState state,EntityLivingBase placer,ItemStack stack){if(stack.hasDisplayName()&&world.getTileEntity(pos) instanceof CopperChestTile)((CopperChestTile)world.getTileEntity(pos)).setCustomName(stack.getDisplayName());}
    @Override public boolean eventReceived(IBlockState state,World world,BlockPos pos,int id,int value){TileEntity tile=world.getTileEntity(pos);return tile!=null&&tile.receiveClientEvent(id,value);}
    @Override public boolean hasComparatorInputOverride(IBlockState state){return true;}
    @Override public int getComparatorInputOverride(IBlockState state,World world,BlockPos pos){return Container.calcRedstone(world.getTileEntity(pos));}
    @Override public void randomTick(World world,BlockPos pos,IBlockState state,java.util.Random random){CopperAging.tick(world,pos,state,random);}
    @Override public void breakBlock(World world,BlockPos pos,IBlockState state){if(world.getBlockState(pos).getBlock() instanceof CopperChestBlock)return;if(world.getTileEntity(pos) instanceof IInventory)InventoryHelper.dropInventoryItems(world,pos,(IInventory)world.getTileEntity(pos));super.breakBlock(world,pos,state);}
    @Override public int damageDropped(IBlockState state){return 0;}
}
