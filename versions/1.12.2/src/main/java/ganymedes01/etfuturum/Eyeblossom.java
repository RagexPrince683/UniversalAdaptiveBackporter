package ganymedes01.etfuturum;
import net.minecraft.block.*;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.*;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.*;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
public final class Eyeblossom extends BlockBush {
    public static final PropertyBool OPEN=PropertyBool.create("open");
    Eyeblossom(){setTickRandomly(true);setDefaultState(blockState.getBaseState().withProperty(OPEN,false));}
    @Override protected BlockStateContainer createBlockState(){return new BlockStateContainer(this,OPEN);}
    @Override public IBlockState getStateFromMeta(int meta){return getDefaultState().withProperty(OPEN,(meta&1)!=0);}
    @Override public int getMetaFromState(IBlockState state){return state.getValue(OPEN)?1:0;}
    @Override public int damageDropped(IBlockState state){return getMetaFromState(state);}
    @Override public void getSubBlocks(CreativeTabs tab,NonNullList<ItemStack> items){items.add(new ItemStack(this,1,0));items.add(new ItemStack(this,1,1));}
    @Override public void updateTick(World world,BlockPos pos,IBlockState state,java.util.Random random){super.updateTick(world,pos,state,random);if(!world.isRemote&&world.getBlockState(pos).getBlock()==this&&world.provider.hasSkyLight()){long time=Math.floorMod(world.getWorldTime(),24000L);world.setBlockState(pos,state.withProperty(OPEN,time>=13000&&time<23000),2);}}
}
