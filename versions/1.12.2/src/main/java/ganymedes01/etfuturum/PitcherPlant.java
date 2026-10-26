package ganymedes01.etfuturum;
import net.minecraft.block.*;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.*;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.*;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.*;
final class PitcherPlant extends BlockBush {
    static final PropertyEnum<BlockDoublePlant.EnumBlockHalf> HALF=BlockDoublePlant.HALF;
    PitcherPlant(){setDefaultState(blockState.getBaseState().withProperty(HALF,BlockDoublePlant.EnumBlockHalf.LOWER));}
    @Override protected BlockStateContainer createBlockState(){return new BlockStateContainer(this,HALF);}
    @Override public IBlockState getStateFromMeta(int meta){return getDefaultState().withProperty(HALF,(meta&8)==0?BlockDoublePlant.EnumBlockHalf.LOWER:BlockDoublePlant.EnumBlockHalf.UPPER);}
    @Override public int getMetaFromState(IBlockState state){return state.getValue(HALF)==BlockDoublePlant.EnumBlockHalf.UPPER?8:0;}
    @Override public boolean canPlaceBlockAt(World world,BlockPos pos){return super.canPlaceBlockAt(world,pos)&&pos.getY()<254&&world.isAirBlock(pos.up());}
    @Override public void onBlockAdded(World world,BlockPos pos,IBlockState state){if(!world.isRemote&&getMetaFromState(state)==0&&world.isAirBlock(pos.up()))world.setBlockState(pos.up(),getStateFromMeta(8),2);}
    @Override public boolean canBlockStay(World world,BlockPos pos,IBlockState state){return getMetaFromState(state)==8?world.getBlockState(pos.down()).getBlock()==this&&getMetaFromState(world.getBlockState(pos.down()))==0:super.canBlockStay(world,pos,state);}
    @Override public void neighborChanged(IBlockState state,World world,BlockPos pos,Block neighbor,BlockPos from){if(world.isRemote)return;if(!canBlockStay(world,pos,state)||getMetaFromState(state)==0&&world.getBlockState(pos.up()).getBlock()!=this){dropBlockAsItem(world,pos,state,0);world.setBlockToAir(pos);}}
    @Override public Item getItemDropped(IBlockState state,java.util.Random random,int fortune){return getMetaFromState(state)==8?Items.AIR:Item.getItemFromBlock(this);}
}
