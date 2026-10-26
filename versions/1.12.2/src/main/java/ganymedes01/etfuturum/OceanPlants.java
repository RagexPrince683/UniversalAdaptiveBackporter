package ganymedes01.etfuturum;

import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.structure.StructureBoundingBox;
import net.minecraftforge.common.BiomeDictionary;

/** The maintained chunk-contained ocean decoration and tree/shelf/branch reef shapes. */
final class OceanPlants {
    private static final String[] COLORS={"tube","brain","bubble","fire","horn"};
    private OceanPlants() { }
    private static boolean reserved(World world,StructureBoundingBox area) { return StructureReservations.vanilla(world,area)||OceanStructures.reserved(world,area)||OceanTreasure.reserved(world,area); }
    static void generate(World world,Random random,int cx,int cz) {
        BlockPos center=new BlockPos((cx<<4)+8,0,(cz<<4)+8);if(!BiomeDictionary.hasType(world.getBiome(center),BiomeDictionary.Type.OCEAN))return;
        reef(world,random,cx,cz);
        if(!Features.enabled("content.generate_ocean_plants"))return;
        int sea=world.getSeaLevel()-1;
        for(int i=0;i<24;i++) {
            int x=(cx<<4)+random.nextInt(16),z=(cz<<4)+random.nextInt(16);BlockPos pos=new BlockPos(x,Math.min(sea,world.getHeight(new BlockPos(x,0,z)).getY()-1),z);Biome biome=world.getBiome(pos);
            boolean warm=biome.getDefaultTemperature()>.8F;
            if(!BiomeDictionary.hasType(biome,BiomeDictionary.Type.OCEAN)||(warm?!Content.available("sea_pickle"):!Content.available("kelp")||BiomeDictionary.hasType(biome,BiomeDictionary.Type.SNOWY)))continue;
            while(pos.getY()>1&&AquaticPlant.sourceWater(world,pos))pos=pos.down();pos=pos.up();
            if(pos.getY()>=sea||reserved(world,new StructureBoundingBox(pos.down(),new BlockPos(x,sea,z))))continue;
            Block plant=Content.block(warm?"sea_pickle_underwater":"kelp");if(!plant.canPlaceBlockAt(world,pos))continue;
            if(warm){if(random.nextInt(6)==0)world.setBlockState(pos,plant.getStateFromMeta(random.nextInt(4)),18);}
            else { int height=1+random.nextInt(10);for(int y=0;y<height&&pos.getY()+y<sea&&AquaticPlant.sourceWater(world,pos.up(y));y++)world.setBlockState(pos.up(y),plant.getDefaultState(),18); }
        }
    }

    private static void reef(World world,Random random,int cx,int cz) {
        if(!Features.enabled("content.generate_coral_reefs")||random.nextInt(4)!=0)return;
        int x=(cx<<4)+5+random.nextInt(6),z=(cz<<4)+5+random.nextInt(6);BlockPos pos=new BlockPos(x,Math.min(world.getSeaLevel()-1,world.getHeight(new BlockPos(x,0,z)).getY()-1),z);
        if(world.getBiome(pos).getDefaultTemperature()<=.8F)return;
        while(pos.getY()>1&&AquaticPlant.sourceWater(world,pos))pos=pos.down();pos=pos.up();
        if(pos.getY()>world.getSeaLevel()-6||!AquaticPlant.sourceWater(world,pos)||!world.getBlockState(pos.down()).isSideSolid(world,pos.down(),EnumFacing.UP)||reserved(world,new StructureBoundingBox(pos.add(-4,-1,-4),pos.add(4,6,4))))return;
        String color=COLORS[random.nextInt(5)];int shape=random.nextInt(3),height=2+random.nextInt(3);
        for(int y=0;y<height;y++)for(int dx=-3;dx<=3;dx++)for(int dz=-3;dz<=3;dz++) {
            boolean solid=shape==0?dx==0&&dz==0||y==height-1&&Math.abs(dx)+Math.abs(dz)<=2:shape==1?y==0&&dx==0&&dz==0||y==height-1&&dx*dx+dz*dz<=8:dx==0&&dz==0||Math.abs(dx)==y&&dz==0||Math.abs(dz)==y&&dx==0;
            BlockPos target=pos.add(dx,y,dz);if(solid&&AquaticPlant.sourceWater(world,target))world.setBlockState(target,Content.block(color+"_coral_block").getDefaultState(),18);
        }
        for(int y=0;y<height;y++)for(int dx=-3;dx<=3;dx++)for(int dz=-3;dz<=3;dz++) {
            BlockPos target=pos.add(dx,y,dz);if(!(world.getBlockState(target).getBlock() instanceof CoralBlock))continue;
            if(AquaticPlant.sourceWater(world,target.up())) {
                if(Content.available("sea_pickle")&&random.nextInt(8)==0)world.setBlockState(target.up(),Content.block("sea_pickle_underwater").getStateFromMeta(random.nextInt(4)),18);
                else if(random.nextBoolean())world.setBlockState(target.up(),Content.block(color+"_coral"+(random.nextBoolean()?"":"_fan")+"_underwater").getDefaultState(),18);
            }
            for(EnumFacing face:EnumFacing.HORIZONTALS)if(random.nextInt(5)==0&&AquaticPlant.sourceWater(world,target.offset(face)))world.setBlockState(target.offset(face),Content.block(color+"_coral_wall_fan_underwater").getDefaultState().withProperty(CoralPlant.FACING,face),18);
        }
    }
}
