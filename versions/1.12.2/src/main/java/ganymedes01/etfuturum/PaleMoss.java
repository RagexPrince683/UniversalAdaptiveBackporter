package ganymedes01.etfuturum;

import java.util.Random;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

final class PaleMoss extends Block implements IGrowable {
    PaleMoss(){super(Material.GRASS);setHardness(.1F);setSoundType(SoundType.PLANT);}
    @Override public boolean canGrow(World world,BlockPos pos,IBlockState state,boolean client){return world.isAirBlock(pos.up());}
    @Override public boolean canUseBonemeal(World world,Random random,BlockPos pos,IBlockState state){return true;}
    @Override public void grow(World world,Random random,BlockPos pos,IBlockState state){if(world.isRemote||!canGrow(world,pos,state,false))return;for(int i=0;i<64;i++){BlockPos at=pos.add(random.nextInt(7)-3,random.nextInt(3)-1,random.nextInt(7)-3);if(at.getY()<1||at.getY()>=world.getHeight()-1||!world.isBlockLoaded(at)||!world.isAirBlock(at.up())||world.getTileEntity(at)!=null)continue;Block old=world.getBlockState(at).getBlock();if(old!=Blocks.DIRT&&old!=Blocks.GRASS&&old!=Blocks.STONE&&old!=this)continue;world.setBlockState(at,getDefaultState(),2);if(random.nextInt(3)==0)world.setBlockState(at.up(),Content.block("pale_moss_carpet").getDefaultState(),2);}}
}
