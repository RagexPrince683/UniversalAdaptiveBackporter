package ganymedes01.etfuturum;

import java.util.Random;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.*;
import net.minecraftforge.common.*;

public final class NyliumBlock extends Block implements IGrowable {
    final boolean crimson;
    NyliumBlock(boolean crimson){super(Material.ROCK);this.crimson=crimson;setHardness(.4F);setResistance(.4F);setSoundType(SoundType.GROUND);setTickRandomly(true);setHarvestLevel("pickaxe",0);}
    @Override public Item getItemDropped(IBlockState state,Random random,int fortune){return Item.getItemFromBlock(Blocks.NETHERRACK);}
    @Override public void updateTick(World world,BlockPos pos,IBlockState state,Random random){IBlockState above=world.getBlockState(pos.up());if(!world.isRemote&&(above.isOpaqueCube()||above.getMaterial()==Material.SNOW||above.getMaterial()==Material.CRAFTED_SNOW))world.setBlockState(pos,Blocks.NETHERRACK.getDefaultState(),2);}
    @Override public boolean canSustainPlant(IBlockState state,IBlockAccess world,BlockPos pos,EnumFacing side,IPlantable plant){return plant.getPlantType(world,pos)==EnumPlantType.Nether||plant instanceof BlockMushroom;}
    @Override public void onPlantGrow(IBlockState state,World world,BlockPos pos,BlockPos source){if(world.rand.nextBoolean())world.setBlockState(pos,Blocks.NETHERRACK.getDefaultState(),2);}
    @Override public boolean canGrow(World world,BlockPos pos,IBlockState state,boolean client){return Content.available(crimson?"crimson_roots":"warped_roots")&&!world.getBlockState(pos.up()).isOpaqueCube();}
    @Override public boolean canUseBonemeal(World world,Random random,BlockPos pos,IBlockState state){return true;}
    @Override public void grow(World world,Random random,BlockPos pos,IBlockState state){if(!world.isRemote&&canGrow(world,pos,state,false))NetherVegetation.scatter(world,random,pos.up(),crimson,false);}
}
