package ganymedes01.etfuturum;

import java.util.Random;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.*;
import net.minecraft.world.*;
import net.minecraft.world.gen.feature.WorldGenTrees;
import net.minecraftforge.event.terraingen.TerrainGen;

public final class AzaleaBlock extends BlockBush implements IGrowable {
    AzaleaBlock(){super(Material.WOOD);setHardness(0);setSoundType(SoundType.PLANT);}
    @Override protected boolean canSustainBush(IBlockState state){return super.canSustainBush(state)||state.getMaterial()==Material.CLAY||state.getBlock() instanceof MossBlock;}
    @Override public boolean canBlockStay(World world,BlockPos pos,IBlockState state){return canSustainBush(world.getBlockState(pos.down()));}
    @Override public boolean isReplaceable(IBlockAccess world,BlockPos pos){return false;}
    @Override public AxisAlignedBB getBoundingBox(IBlockState state,IBlockAccess world,BlockPos pos){return FULL_BLOCK_AABB;}
    @Override public AxisAlignedBB getCollisionBoundingBox(IBlockState state,IBlockAccess world,BlockPos pos){return new AxisAlignedBB(0,.5,0,1,1,1);}
    @Override public boolean canGrow(World world,BlockPos pos,IBlockState state,boolean client){return Content.available("azalea_leaves");}
    @Override public boolean canUseBonemeal(World world,Random random,BlockPos pos,IBlockState state){return random.nextFloat()<.45F;}
    @Override public void grow(World world,Random random,BlockPos pos,IBlockState state){
        if(world.isRemote||!Content.available("azalea_leaves")||!world.isAreaLoaded(pos.add(-3,0,-3),pos.add(3,9,3))||!TerrainGen.saplingGrowTree(world,random,pos))return;
        IBlockState soil=world.getBlockState(pos.down());world.setBlockToAir(pos);
        if(soil.getMaterial()==Material.CLAY||soil.getBlock() instanceof MossBlock)world.setBlockState(pos.down(),Blocks.DIRT.getDefaultState(),4);
        WorldGenTrees tree=new WorldGenTrees(true,4,Blocks.LOG.getDefaultState(),Blocks.LEAVES.getDefaultState(),false){
            @Override protected void setBlockAndNotifyAdequately(World target,BlockPos at,IBlockState placed){if(placed.getBlock()==Blocks.LEAVES)placed=Content.block(random.nextInt(4)==0?"flowering_azalea_leaves":"azalea_leaves").getDefaultState();super.setBlockAndNotifyAdequately(target,at,placed);}
        };
        if(!tree.generate(world,random,pos)){world.setBlockState(pos.down(),soil,4);world.setBlockState(pos,state,3);}
    }
}
