package ganymedes01.etfuturum;

import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.Item;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

final class CoralBlock extends Block {
    final String color; final boolean dead;
    CoralBlock(String color,boolean dead) { super(Material.ROCK);this.color=color;this.dead=dead;setHardness(1.5F);setResistance(6);setHarvestLevel("pickaxe",0); }
    static boolean hasWater(World world,BlockPos pos) { for(EnumFacing face:EnumFacing.values())if(!world.isBlockLoaded(pos.offset(face))||world.getBlockState(pos.offset(face)).getMaterial()==Material.WATER)return true;return false; }
    @Override public Item getItemDropped(IBlockState state,Random random,int fortune) { return Content.item("dead_"+color+"_coral_block"); }
    @Override public void onBlockAdded(World world,BlockPos pos,IBlockState state) { if(!world.isRemote&&!dead)world.scheduleUpdate(pos,this,60+world.rand.nextInt(40)); }
    @Override public void neighborChanged(IBlockState state,World world,BlockPos pos,Block neighbor,BlockPos from) { onBlockAdded(world,pos,state); }
    @Override public void updateTick(World world,BlockPos pos,IBlockState state,Random random) { if(!world.isRemote&&!dead&&!hasWater(world,pos))world.setBlockState(pos,Content.block("dead_"+color+"_coral_block").getDefaultState(),3); }
}
