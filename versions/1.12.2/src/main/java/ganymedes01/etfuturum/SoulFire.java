package ganymedes01.etfuturum;
import java.util.Random;
import net.minecraft.block.*;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
final class SoulFire extends BlockFire {
    SoulFire(){setLightLevel(10/15F);}
    static boolean soil(World world,BlockPos pos){Block block=world.getBlockState(pos.down()).getBlock();return block==Blocks.SOUL_SAND||block==Content.BLOCKS.get("soul_soil");}
    @Override public boolean canPlaceBlockAt(World world,BlockPos pos){return soil(world,pos);}
    @Override public void onBlockAdded(World world,BlockPos pos,IBlockState state){if(!soil(world,pos))world.setBlockToAir(pos);else world.scheduleUpdate(pos,this,30);}
    @Override public void neighborChanged(IBlockState state,World world,BlockPos pos,Block block,BlockPos from){if(!soil(world,pos))world.setBlockToAir(pos);}
    @Override public void updateTick(World world,BlockPos pos,IBlockState state,Random random){if(!Features.enabled("mixins.soulFire")){world.setBlockState(pos,Blocks.FIRE.getDefaultState(),3);return;}if(!soil(world,pos))world.setBlockToAir(pos);else world.scheduleUpdate(pos,this,30);}
}
