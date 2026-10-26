package ganymedes01.etfuturum;

import java.util.*;
import net.minecraft.block.*;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.*;
import net.minecraft.item.ItemStack;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.world.*;
import net.minecraftforge.common.IShearable;

public final class Seagrass extends AquaticPlant implements IGrowable,IShearable {
    public enum Part implements IStringSerializable { SINGLE, LOWER, UPPER;public String getName(){return name().toLowerCase(Locale.ROOT);} }
    public static final PropertyEnum<Part> PART=PropertyEnum.create("part",Part.class);
    Seagrass(){super(true);setDefaultState(getDefaultState().withProperty(PART,Part.SINGLE));}
    @Override protected BlockStateContainer createBlockState(){return states(PART);}
    @Override public IBlockState getStateFromMeta(int meta){return getDefaultState().withProperty(PART,Part.values()[Math.min(2,meta&3)]);}
    @Override public int getMetaFromState(IBlockState state){return state.getValue(PART).ordinal();}
    @Override public int quantityDropped(Random random){return 0;}
    @Override public boolean isReplaceable(IBlockAccess world,BlockPos pos){return false;}
    @Override public AxisAlignedBB getBoundingBox(IBlockState state,IBlockAccess world,BlockPos pos){return new AxisAlignedBB(.1,0,.1,.9,state.getValue(PART)==Part.SINGLE?.8:1,.9);}
    @Override protected boolean supported(World world,BlockPos pos,IBlockState state){
        IBlockState below=world.getBlockState(pos.down());Part part=state.getValue(PART);
        if(part==Part.UPPER)return below.getBlock()==this&&below.getValue(PART)==Part.LOWER;
        if(below.getBlock()==this||!below.isSideSolid(world,pos.down(),EnumFacing.UP))return false;
        IBlockState above=world.getBlockState(pos.up());return part!=Part.LOWER||above.getBlock()==this&&above.getValue(PART)==Part.UPPER;
    }
    @Override public boolean canPlaceBlockAt(World world,BlockPos pos){return sourceWater(world,pos)&&super.canPlaceBlockAt(world,pos);}
    @Override public ItemStack getItem(World world,BlockPos pos,IBlockState state){return Content.stack("seagrass");}
    @Override public boolean isShearable(ItemStack tool,IBlockAccess world,BlockPos pos){return true;}
    @Override public List<ItemStack> onSheared(ItemStack tool,IBlockAccess world,BlockPos pos,int fortune){ItemStack drop=Content.stack("seagrass");drop.setCount(world.getBlockState(pos).getValue(PART)==Part.SINGLE?1:2);return Collections.singletonList(drop);}
    @Override public boolean canGrow(World world,BlockPos pos,IBlockState state,boolean client){return state.getValue(PART)==Part.SINGLE&&pos.getY()<world.getHeight()-2&&world.isBlockLoaded(pos.up())&&sourceWater(world,pos.up());}
    @Override public boolean canUseBonemeal(World world,Random random,BlockPos pos,IBlockState state){return true;}
    @Override public void grow(World world,Random random,BlockPos pos,IBlockState state){if(!world.isRemote&&canGrow(world,pos,state,false)){world.setBlockState(pos,state.withProperty(PART,Part.LOWER),2);world.setBlockState(pos.up(),getDefaultState().withProperty(PART,Part.UPPER),3);}}
    static void generate(World world,Random random,int cx,int cz){
        if(!Content.available("seagrass"))return;
        for(int attempt=0;attempt<12;attempt++){
            int x=(cx<<4)+random.nextInt(16),z=(cz<<4)+random.nextInt(16);
            for(int y=Math.min(62,world.getSeaLevel()-1);y>=45;y--){
                BlockPos pos=new BlockPos(x,y,z);if(!world.isBlockLoaded(pos)||!Content.block("seagrass").canPlaceBlockAt(world,pos))continue;
                net.minecraft.world.gen.structure.StructureBoundingBox area=new net.minecraft.world.gen.structure.StructureBoundingBox(pos,pos.up());
                if(StructureReservations.vanilla(world,area)||OceanStructures.reserved(world,area)||OceanTreasure.reserved(world,area))continue;
                world.setBlockState(pos,Content.block("seagrass").getDefaultState(),18);break;
            }
        }
    }

}
