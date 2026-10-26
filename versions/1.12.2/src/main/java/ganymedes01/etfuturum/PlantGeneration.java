package ganymedes01.etfuturum;

import java.util.Random;
import net.minecraft.block.*;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.*;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.structure.StructureBoundingBox;
import net.minecraftforge.common.BiomeDictionary;

/** Native, new-chunk decoration: no neighbor generation or edits inside saved structures. */
final class PlantGeneration {
    private final boolean bamboo=Features.enabled("world.bambooWorldgen");
    private static boolean reserved(World world,BlockPos from,BlockPos to){StructureBoundingBox area=new StructureBoundingBox(from,to);return StructureReservations.vanilla(world,area)||OceanStructures.reserved(world,area)||OceanTreasure.reserved(world,area);}
    void generate(World world,Random random,int cx,int cz){
        Biome biome=world.getBiome(new BlockPos((cx<<4)+8,0,(cz<<4)+8));
        if(bamboo&&Content.available("bamboo")&&BiomeDictionary.hasType(biome,BiomeDictionary.Type.JUNGLE)){
            int count=BiomeDictionary.hasType(biome,BiomeDictionary.Type.SPARSE)?16:random.nextInt(16)==0?256:16;
            for(int i=0;i<count;i++){BlockPos pos=world.getHeight(new BlockPos((cx<<4)+random.nextInt(16),0,(cz<<4)+random.nextInt(16)));bamboo(world,random,pos);}
        }
        boolean lichen=Content.available("glow_lichen"),vines=Content.available("glow_berries");
        if(!lichen&&!vines)return;
        for(int i=0;i<60;i++){
            if(i<40&&!lichen||i>=40&&!vines)continue;
            BlockPos pos=new BlockPos((cx<<4)+random.nextInt(16),1+random.nextInt(127),(cz<<4)+random.nextInt(16));
            if(!world.isAirBlock(pos)||world.getLightFor(EnumSkyBlock.SKY,pos)>3||reserved(world,pos.down(6),pos.up()))continue;
            if(i<40){EnumFacing[] faces=EnumFacing.values();int start=random.nextInt(faces.length);for(int n=0;n<faces.length;n++){EnumFacing face=faces[(start+n)%faces.length];BlockPos support=pos.offset(face);if((support.getX()>>4)!=cx||(support.getZ()>>4)!=cz)continue;if(((GlowLichen)Content.block("glow_lichen")).attach(world,pos,face,false))break;}}
            else {
                CaveVines vine=(CaveVines)Content.block("cave_vines");if(!vine.canPlaceBlockAt(world,pos))continue;int length=1+random.nextInt(6),actual=0;
                while(actual<length&&pos.getY()-actual>1&&world.isAirBlock(pos.down(actual)))actual++;
                for(int y=0;y<actual;y++){Block target=Content.block(y==actual-1?"cave_vines":"cave_vines_plant");world.setBlockState(pos.down(y),target.getDefaultState().withProperty(CaveVines.BERRIES,random.nextInt(9)==0),18);}
            }
        }
    }
    static boolean bamboo(World world,Random random,BlockPos pos){
        if(!Content.available("bamboo")||!BambooBlock.soil(world.getBlockState(pos.down())))return false;int height=6+random.nextInt(11);if(pos.getY()+height>=world.getHeight()||reserved(world,pos,pos.up(height)))return false;
        for(int y=0;y<height;y++)if(!world.isAirBlock(pos.up(y)))return false;
        for(int y=0;y<height;y++)world.setBlockState(pos.up(y),Content.block("bamboo").getDefaultState().withProperty(BambooBlock.AGE,1).withProperty(BambooBlock.STAGE,y==height-1?1:0).withProperty(BambooBlock.LEAVES,y>=height-2?BambooBlock.Leaves.LARGE:y>=height-3?BambooBlock.Leaves.SMALL:BambooBlock.Leaves.NONE),18);
        return true;
    }
}
