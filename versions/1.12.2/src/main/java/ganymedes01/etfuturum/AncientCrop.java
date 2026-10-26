package ganymedes01.etfuturum;
import net.minecraft.block.*;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.*;
import net.minecraft.init.*;
import net.minecraft.item.*;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.*;
import java.util.Random;
/** Retains the source's seven growth ages and metadata bit eight for pitcher upper halves. */
public final class AncientCrop extends BlockCrops {
    public static final PropertyBool UPPER=PropertyBool.create("upper");
    public final boolean pitcher;
    AncientCrop(boolean pitcher){this.pitcher=pitcher;setDefaultState(blockState.getBaseState().withProperty(AGE,0).withProperty(UPPER,false));}
    @Override protected BlockStateContainer createBlockState(){return new BlockStateContainer(this,AGE,UPPER);}
    @Override public IBlockState getStateFromMeta(int meta){return getDefaultState().withProperty(AGE,meta&7).withProperty(UPPER,(meta&8)!=0);}
    @Override public int getMetaFromState(IBlockState state){return state.getValue(AGE)|(state.getValue(UPPER)?8:0);}
    public int stage(IBlockState state){return Math.min(4,state.getValue(AGE)*5/8);}
    private boolean room(World world,BlockPos pos){return pos.getY()<254&&world.isBlockLoaded(pos.up())&&(world.isAirBlock(pos.up())||world.getBlockState(pos.up()).getBlock()==this&&world.getBlockState(pos.up()).getValue(UPPER));}
    @Override public boolean canBlockStay(World world,BlockPos pos,IBlockState state){if(pitcher&&state.getValue(UPPER)){IBlockState below=world.getBlockState(pos.down());return below.getBlock()==this&&!below.getValue(UPPER)&&stage(below)>=3;}return super.canBlockStay(world,pos,state);}
    private void finish(World world,BlockPos pos){IBlockState state=world.getBlockState(pos);if(state.getBlock()!=this)return;int age=state.getValue(AGE);if(!pitcher){if(age==7)world.setBlockState(pos,Content.block("torchflower").getDefaultState(),3);}else if(stage(state)>=3&&room(world,pos))world.setBlockState(pos.up(),state.withProperty(UPPER,true),3);}
    @Override public void updateTick(World world,BlockPos pos,IBlockState state,Random random){if(world.isRemote)return;if(state.getValue(UPPER)){if(!canBlockStay(world,pos,state))world.setBlockToAir(pos);return;}if(!canBlockStay(world,pos,state)){dropBlockAsItem(world,pos,state,0);world.setBlockToAir(pos);return;}if(pitcher&&state.getValue(AGE)>=4&&!room(world,pos))return;super.updateTick(world,pos,state,random);finish(world,pos);}
    @Override public void grow(World world,Random random,BlockPos pos,IBlockState state){if(state.getValue(UPPER)){BlockPos lower=pos.down();if(world.getBlockState(lower).getBlock()==this)grow(world,random,lower,world.getBlockState(lower));return;}if(pitcher&&state.getValue(AGE)>=4&&!room(world,pos))return;int next=pitcher?new int[]{2,4,5,7,7}[stage(state)]:Math.min(7,state.getValue(AGE)+3);if(pitcher&&next>=5&&!room(world,pos))next=4;world.setBlockState(pos,state.withProperty(AGE,next),2);finish(world,pos);}
    @Override public boolean canGrow(World world,BlockPos pos,IBlockState state,boolean client){if(state.getValue(UPPER)){IBlockState lower=world.getBlockState(pos.down());return lower.getBlock()==this&&canGrow(world,pos.down(),lower,client);}return state.getValue(AGE)<7&&(!pitcher||state.getValue(AGE)<4||room(world,pos));}
    @Override public void neighborChanged(IBlockState state,World world,BlockPos pos,Block neighbor,BlockPos from){if(world.isRemote)return;boolean missing= pitcher&&!state.getValue(UPPER)&&stage(state)>=3&&world.getBlockState(pos.up()).getBlock()!=this;if(missing||!canBlockStay(world,pos,state)){dropBlockAsItem(world,pos,state,0);world.setBlockToAir(pos);}}
    @Override public void getDrops(NonNullList<ItemStack> drops,IBlockAccess world,BlockPos pos,IBlockState state,int fortune){if(!state.getValue(UPPER)){String name=state.getValue(AGE)==7?pitcher?"pitcher_plant":"torchflower":pitcher?"pitcher_pod":"torchflower_seeds";if(Content.available(name))drops.add(Content.stack(name));}}
    @Override protected Item getSeed(){return Content.item(pitcher?"pitcher_pod":"torchflower_seeds");}
    @Override protected Item getCrop(){return Content.item(pitcher?"pitcher_plant":"torchflower");}
}
