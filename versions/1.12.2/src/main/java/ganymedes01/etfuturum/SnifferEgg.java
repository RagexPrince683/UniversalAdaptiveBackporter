package ganymedes01.etfuturum;
import ganymedes01.etfuturum.entities.EntitySniffer;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.*;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.world.*;
import java.util.Random;
final class SnifferEgg extends Block {
    static final PropertyInteger HATCH=PropertyInteger.create("hatch",0,2);
    SnifferEgg(){super(Material.DRAGON_EGG);setHardness(.5F);setDefaultState(blockState.getBaseState().withProperty(HATCH,0));}
    @Override protected BlockStateContainer createBlockState(){return new BlockStateContainer(this,HATCH);}
    @Override public IBlockState getStateFromMeta(int meta){return getDefaultState().withProperty(HATCH,Math.min(2,meta&3));}
    @Override public int getMetaFromState(IBlockState state){return state.getValue(HATCH);}
    @Override public boolean isOpaqueCube(IBlockState state){return false;}
    @Override public boolean isFullCube(IBlockState state){return false;}
    @Override public AxisAlignedBB getBoundingBox(IBlockState state,IBlockAccess world,BlockPos pos){return new AxisAlignedBB(.0625,0,.125,.9375,1,.875);}
    private int delay(World world,BlockPos pos){return world.getBlockState(pos.down()).getBlock()==Content.block("moss_block")?4000:8000;}
    @Override public void onBlockAdded(World world,BlockPos pos,IBlockState state){if(!world.isRemote)world.scheduleUpdate(pos,this,delay(world,pos));}
    @Override public void updateTick(World world,BlockPos pos,IBlockState state,Random random){if(world.isRemote)return;if(!Content.available("sniffer_egg")||!MobRegistry.enabled(EntitySniffer.class)){world.scheduleUpdate(pos,this,delay(world,pos));return;}int stage=state.getValue(HATCH);if(stage<2){world.setBlockState(pos,state.withProperty(HATCH,stage+1),2);sound(world,pos,"crack");world.scheduleUpdate(pos,this,delay(world,pos));}else{EntitySniffer sniffer=new EntitySniffer(world);sniffer.setLocationAndAngles(pos.getX()+.5,pos.getY(),pos.getZ()+.5,0,0);sniffer.setGrowingAge(-48000);if(world.spawnEntity(sniffer)){sound(world,pos,"hatch");world.setBlockToAir(pos);}else world.scheduleUpdate(pos,this,200);}}
    private void sound(World world,BlockPos pos,String kind){world.playSound(null,pos,ModernSounds.get("block.sniffer_egg."+kind),SoundCategory.BLOCKS,1,1);}
    @Override public int damageDropped(IBlockState state){return 0;}
}
