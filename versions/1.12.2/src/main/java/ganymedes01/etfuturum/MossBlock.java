package ganymedes01.etfuturum;

import java.util.Random;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.*;
import net.minecraftforge.common.IPlantable;

public final class MossBlock extends Block implements IGrowable {
    MossBlock(){super(Material.GRASS);setHardness(.1F);setResistance(.1F);setHarvestLevel("hoe",0);setSoundType(SoundType.PLANT);}
    @Override public boolean canGrow(World world,BlockPos pos,IBlockState state,boolean client){return world.isAirBlock(pos.up());}
    @Override public boolean canUseBonemeal(World world,Random random,BlockPos pos,IBlockState state){return true;}
    @Override public boolean canSustainPlant(IBlockState state,IBlockAccess world,BlockPos pos,EnumFacing side,IPlantable plant){return Blocks.DIRT.canSustainPlant(Blocks.DIRT.getDefaultState(),world,pos,side,plant);}
    @Override public void grow(World world,Random random,BlockPos pos,IBlockState state){
        if(world.isRemote||!canGrow(world,pos,state,false))return;
        for(int attempt=0;attempt<128;attempt++){
            int x=pos.getX()+random.nextInt(7)-3,z=pos.getZ()+random.nextInt(7)-3;
            for(int y=Math.min(world.getHeight()-3,pos.getY()+2);y>=Math.max(1,pos.getY()-2);y--){
                BlockPos ground=new BlockPos(x,y,z);if(!world.isBlockLoaded(ground))break;IBlockState old=world.getBlockState(ground);Block b=old.getBlock(),above=world.getBlockState(ground.up()).getBlock();
                if(world.getTileEntity(ground)!=null||b!=Blocks.GRASS&&b!=Blocks.DIRT&&b!=Blocks.STONE&&b!=this&&b!=Content.BLOCKS.get("deepslate"))continue;
                if(!world.isAirBlock(ground.up())&&!(above instanceof BlockBush))continue;
                world.setBlockState(ground,getDefaultState(),2);if(!world.isAirBlock(ground.up()))break;
                int choice=random.nextInt(100);IBlockState plant=null;
                if(choice<3)plant=Content.block(choice==2?"flowering_azalea":"azalea").getDefaultState();
                else if(choice==3&&world.isAirBlock(ground.up(2)))Blocks.DOUBLE_PLANT.placeAt(world,ground.up(),BlockDoublePlant.EnumPlantType.GRASS,2);
                else if(choice<14)plant=Content.block("moss_carpet").getDefaultState();
                else if(choice<21)plant=Blocks.TALLGRASS.getDefaultState().withProperty(BlockTallGrass.TYPE,BlockTallGrass.EnumType.GRASS);
                if(plant!=null)world.setBlockState(ground.up(),plant,2);break;
            }
        }
    }
}
