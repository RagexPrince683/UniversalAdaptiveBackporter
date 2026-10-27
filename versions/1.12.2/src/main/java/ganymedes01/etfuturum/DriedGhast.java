package ganymedes01.etfuturum;
import net.minecraft.block.*;
import net.minecraft.block.material.*;
import net.minecraft.block.properties.*;
import net.minecraft.block.state.*;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.*;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.world.*;
public final class DriedGhast extends Block {
    public static final PropertyInteger HYDRATION=PropertyInteger.create("hydration",0,3);
    public static final PropertyBool WATERLOGGED=PropertyBool.create("waterlogged");
    DriedGhast(){super(Material.ROCK);setHardness(.5F);setLightOpacity(0);setDefaultState(blockState.getBaseState().withProperty(HYDRATION,0).withProperty(WATERLOGGED,false));}
    @Override protected BlockStateContainer createBlockState(){return new BlockStateContainer(this,HYDRATION,WATERLOGGED);}
    @Override public IBlockState getStateFromMeta(int meta){return getDefaultState().withProperty(HYDRATION,meta&3).withProperty(WATERLOGGED,(meta&4)!=0);}
    @Override public int getMetaFromState(IBlockState state){return state.getValue(HYDRATION)|(state.getValue(WATERLOGGED)?4:0);}
    @Override public boolean isOpaqueCube(IBlockState state){return false;}
    @Override public boolean isFullCube(IBlockState state){return false;}
    @Override public AxisAlignedBB getBoundingBox(IBlockState state,IBlockAccess world,BlockPos pos){return new AxisAlignedBB(.25,0,.25,.75,.625,.75);}
    @Override public EnumPushReaction getPushReaction(IBlockState state){return EnumPushReaction.DESTROY;}
    @Override public int damageDropped(IBlockState state){return 0;}
    @Override public boolean hasTileEntity(IBlockState state){return true;}
    @Override public TileEntity createTileEntity(World world,IBlockState state){return new DriedGhastTile();}
    @Override public IBlockState getStateForPlacement(World world,BlockPos pos,EnumFacing side,float x,float y,float z,int meta,EntityLivingBase placer,EnumHand hand){IBlockState old=world.getBlockState(pos);return getDefaultState().withProperty(WATERLOGGED,!world.provider.doesWaterVaporize()&&old.getMaterial()==Material.WATER&&old.getPropertyKeys().contains(BlockLiquid.LEVEL)&&old.getValue(BlockLiquid.LEVEL)==0);}
    @Override public boolean removedByPlayer(IBlockState state,World world,BlockPos pos,EntityPlayer player,boolean harvest){return world.setBlockState(pos,state.getValue(WATERLOGGED)?Blocks.WATER.getDefaultState():Blocks.AIR.getDefaultState(),3);}
    @Override public boolean onBlockActivated(World world,BlockPos pos,IBlockState state,EntityPlayer player,EnumHand hand,EnumFacing side,float x,float y,float z){ItemStack held=player.getHeldItem(hand);if(!player.canPlayerEdit(pos,side,held))return false;boolean wet=state.getValue(WATERLOGGED),fill=held.getItem()==Items.WATER_BUCKET&&!wet&&!world.provider.doesWaterVaporize(),drain=held.getItem()==Items.BUCKET&&wet;if(!fill&&!drain)return false;if(!world.isRemote){world.setBlockState(pos,state.withProperty(WATERLOGGED,fill),3);if(!player.capabilities.isCreativeMode)FilledCauldron.exchange(player,hand,new ItemStack(fill?Items.BUCKET:Items.WATER_BUCKET));}return true;}
    @Override public BlockRenderLayer getRenderLayer(){return BlockRenderLayer.CUTOUT;}
    @Override public boolean canRenderInLayer(IBlockState state,BlockRenderLayer layer){return layer==BlockRenderLayer.CUTOUT||layer==BlockRenderLayer.TRANSLUCENT&&state.getValue(WATERLOGGED);}
    @Override public void randomDisplayTick(IBlockState state,World world,BlockPos pos,java.util.Random random){if(state.getValue(WATERLOGGED)&&random.nextInt(4)==0)world.spawnParticle(EnumParticleTypes.VILLAGER_HAPPY,pos.getX()+random.nextDouble(),pos.getY()+.7,pos.getZ()+random.nextDouble(),0,0,0);}
}
