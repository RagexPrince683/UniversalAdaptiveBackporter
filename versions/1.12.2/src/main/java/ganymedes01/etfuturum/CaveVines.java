package ganymedes01.etfuturum;

import java.util.*;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.*;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.*;
import net.minecraft.item.*;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.world.*;
import net.minecraftforge.common.IShearable;

public final class CaveVines extends Block implements IGrowable,IShearable {
    public static final PropertyBool BERRIES=PropertyBool.create("berries");
    final boolean tip;
    CaveVines(boolean tip){super(Material.VINE);this.tip=tip;setHardness(.2F);setLightOpacity(0);setSoundType(SoundType.PLANT);setTickRandomly(tip);setDefaultState(blockState.getBaseState().withProperty(BERRIES,false));}
    @Override protected BlockStateContainer createBlockState(){return new BlockStateContainer(this,BERRIES);}
    @Override public IBlockState getStateFromMeta(int meta){return getDefaultState().withProperty(BERRIES,(meta&1)!=0);}
    @Override public int getMetaFromState(IBlockState state){return state.getValue(BERRIES)?1:0;}
    @Override public boolean hasTileEntity(IBlockState state){return tip;}
    @Override public TileEntity createTileEntity(World world,IBlockState state){return tip?new CaveVineTile():null;}
    @Override public boolean isOpaqueCube(IBlockState state){return false;}
    @Override public boolean isFullCube(IBlockState state){return false;}
    @Override public boolean isSideSolid(IBlockState state,IBlockAccess world,BlockPos pos,EnumFacing side){return false;}
    @Override public BlockFaceShape getBlockFaceShape(IBlockAccess world,IBlockState state,BlockPos pos,EnumFacing side){return BlockFaceShape.UNDEFINED;}
    @Override public AxisAlignedBB getBoundingBox(IBlockState state,IBlockAccess world,BlockPos pos){return new AxisAlignedBB(.0625,0,.0625,.9375,1,.9375);}
    @Override public AxisAlignedBB getCollisionBoundingBox(IBlockState state,IBlockAccess world,BlockPos pos){return NULL_AABB;}
    @Override public BlockRenderLayer getRenderLayer(){return BlockRenderLayer.CUTOUT;}
    @Override public int getLightValue(IBlockState state,IBlockAccess world,BlockPos pos){return state.getValue(BERRIES)?14:0;}
    @Override public boolean isLadder(IBlockState state,IBlockAccess world,BlockPos pos,EntityLivingBase entity){return true;}
    @Override public boolean isReplaceable(IBlockAccess world,BlockPos pos){return false;}
    private boolean supported(World world,BlockPos pos){IBlockState above=world.getBlockState(pos.up());return above.getBlock() instanceof CaveVines||above.isSideSolid(world,pos.up(),EnumFacing.DOWN);}
    @Override public boolean canPlaceBlockAt(World world,BlockPos pos){return pos.getY()>0&&pos.getY()<world.getHeight()-1&&world.isAirBlock(pos)&&supported(world,pos);}
    @Override public Item getItemDropped(IBlockState state,Random random,int fortune){return state.getValue(BERRIES)?Content.item("glow_berries"):Items.AIR;}
    @Override public int damageDropped(IBlockState state){return 0;}
    @Override public ItemStack getItem(World world,BlockPos pos,IBlockState state){return Content.stack("glow_berries");}
    @Override public void neighborChanged(IBlockState state,World world,BlockPos pos,Block neighbor,BlockPos from){
        if(world.isRemote)return;if(!supported(world,pos)){world.destroyBlock(pos,true);return;}
        if(!tip&&!(world.getBlockState(pos.down()).getBlock() instanceof CaveVines))world.setBlockState(pos,Content.block("cave_vines").getDefaultState().withProperty(BERRIES,state.getValue(BERRIES)),3);
    }
    public void extend(World world,BlockPos pos,boolean manual){
        if(world.isRemote||!tip||pos.getY()<=1||!world.isAirBlock(pos.down()))return;
        TileEntity tile=world.getTileEntity(pos);int max=tile instanceof CaveVineTile?((CaveVineTile)tile).maxLength:27;
        IBlockState old=world.getBlockState(pos),next=getDefaultState().withProperty(BERRIES,!manual&&world.rand.nextInt(9)==0);
        // Install the new tip first so body updates cannot recreate a second tip mid-transition.
        world.setBlockState(pos.down(),next,2);world.setBlockState(pos,Content.block("cave_vines_plant").getDefaultState().withProperty(BERRIES,old.getValue(BERRIES)),3);
        TileEntity grown=world.getTileEntity(pos.down());if(grown instanceof CaveVineTile){((CaveVineTile)grown).maxLength=max;grown.markDirty();}world.notifyNeighborsOfStateChange(pos.down(),this,false);
    }
    @Override public void updateTick(World world,BlockPos pos,IBlockState state,Random random){if(world.isRemote||!tip||random.nextInt(10)!=0)return;TileEntity tile=world.getTileEntity(pos);if(!(tile instanceof CaveVineTile)||((CaveVineTile)tile).sheared)return;int length=1;while(length<27&&world.getBlockState(pos.up(length)).getBlock() instanceof CaveVines)length++;if(length<((CaveVineTile)tile).maxLength)extend(world,pos,false);}
    @Override public boolean canGrow(World world,BlockPos pos,IBlockState state,boolean client){return !state.getValue(BERRIES);}
    @Override public boolean canUseBonemeal(World world,Random random,BlockPos pos,IBlockState state){return true;}
    @Override public void grow(World world,Random random,BlockPos pos,IBlockState state){if(!world.isRemote&&!state.getValue(BERRIES))world.setBlockState(pos,state.withProperty(BERRIES,true),3);}
    @Override public boolean isShearable(ItemStack item,IBlockAccess world,BlockPos pos){TileEntity tile=world.getTileEntity(pos);return tip&&tile instanceof CaveVineTile&&!((CaveVineTile)tile).sheared;}
    @Override public List<ItemStack> onSheared(ItemStack item,IBlockAccess world,BlockPos pos,int fortune){TileEntity tile=world.getTileEntity(pos);if(tile instanceof CaveVineTile){((CaveVineTile)tile).sheared=true;tile.markDirty();}return Collections.emptyList();}
    @Override public boolean onBlockActivated(World world,BlockPos pos,IBlockState state,EntityPlayer player,EnumHand hand,EnumFacing side,float x,float y,float z){
        if(state.getValue(BERRIES)){if(!world.isRemote){spawnAsEntity(world,pos,Content.stack("glow_berries"));world.setBlockState(pos,state.withProperty(BERRIES,false),3);}return true;}
        ItemStack held=player.getHeldItem(hand);if(held.getItem() instanceof ItemShears&&isShearable(held,world,pos)){if(!world.isRemote){onSheared(held,world,pos,0);if(!player.capabilities.isCreativeMode)held.damageItem(1,player);world.playSound(null,pos,SoundEvents.ENTITY_SHEEP_SHEAR,SoundCategory.BLOCKS,1,1);}return true;}return false;
    }
}
