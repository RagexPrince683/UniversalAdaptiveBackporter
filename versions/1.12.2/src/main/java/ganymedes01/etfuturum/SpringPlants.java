package ganymedes01.etfuturum;

import java.util.*;
import net.minecraft.block.*;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.*;
import net.minecraft.item.*;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.*;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;

public final class SpringPlants {
    public static final String[] PLANTS={"bush","firefly_bush","short_dry_grass","tall_dry_grass","cactus_flower","wildflowers","leaf_litter"};
    private SpringPlants(){}
    static void define(){for(String name:PLANTS)Content.add(name,"content.spring_to_life",name.equals("wildflowers")||name.equals("leaf_litter")?new SpringPatch(name.equals("leaf_litter")):new SpringPlant(name));}
    public static int fuel(Block block){String name=block.getRegistryName().getPath();return name.equals("leaf_litter")||name.endsWith("dry_grass")?100:0;}
    static void recipes(RegistryEvent.Register<IRecipe> event){
        for(String egg:new String[]{"brown_egg","blue_egg"}){Crafting.shaped(event,"cake_from_"+egg,new ItemStack(Items.CAKE),"MMM","SES","WWW",'M',Items.MILK_BUCKET,'S',Items.SUGAR,'E',Content.item(egg),'W',Items.WHEAT);ModernRecipes.shapeless(event,"pumpkin_pie_from_"+egg,new ItemStack(Items.PUMPKIN_PIE),Blocks.PUMPKIN,Items.SUGAR,Content.item(egg));}

        ModernRecipes.shapeless(event,"pink_dye_from_cactus_flower",new ItemStack(Items.DYE,1,9),Content.item("cactus_flower"));ModernRecipes.shapeless(event,"yellow_dye_from_wildflowers",new ItemStack(Items.DYE,1,11),Content.item("wildflowers"));
        if(Content.available("leaf_litter")){GameRegistry.addSmelting(new ItemStack(Blocks.LEAVES,1,32767),Content.stack("leaf_litter"),.1F);GameRegistry.addSmelting(new ItemStack(Blocks.LEAVES2,1,32767),Content.stack("leaf_litter"),.1F);}
        for(String name:PLANTS)if(Content.available(name)&&Features.enabled("functions.enableAutoAddComposter"))Composting.register(Content.stack(name),name.equals("wildflowers")||name.equals("cactus_flower")?65:30);
    }
    public static void cactus(World world,BlockPos pos,Random random){if(world.isRemote||!Content.available("cactus_flower")||world.getBlockState(pos).getBlock()!=Blocks.CACTUS||!world.isAirBlock(pos.up())||random.nextInt(30)!=0)return;world.setBlockState(pos.up(),Content.block("cactus_flower").getDefaultState(),3);}
    static void generate(World world,int cx,int cz){
        if(world.isRemote||world.provider.getDimension()!=0||!Features.enabled("content.spring_to_life")||world.getWorldType()==WorldType.FLAT&&!world.getWorldInfo().getGeneratorOptions().contains("decoration"))return;
        Random random=new Random(world.getSeed()^cx*341873128712L^cz*132897987541L^0x535052494e47L);net.minecraft.world.biome.Biome biome=world.getBiome(new BlockPos(cx*16+8,64,cz*16+8));boolean forest=BiomeDictionary.hasType(biome,BiomeDictionary.Type.FOREST),dry=BiomeDictionary.hasType(biome,BiomeDictionary.Type.DRY)&&biome.getDefaultTemperature()>1,wets=BiomeDictionary.hasType(biome,BiomeDictionary.Type.SWAMP)||BiomeDictionary.hasType(biome,BiomeDictionary.Type.RIVER);
        for(int i=0;i<9;i++){BlockPos pos=world.getTopSolidOrLiquidBlock(new BlockPos(cx*16+1+random.nextInt(14),0,cz*16+1+random.nextInt(14)));String name=dry?(random.nextBoolean()?"short_dry_grass":"tall_dry_grass"):wets&&random.nextInt(3)==0?"firefly_bush":forest?(random.nextInt(3)==0?"wildflowers":"leaf_litter"):"bush";Block block=Content.block(name);if(world.isAirBlock(pos)&&block.canPlaceBlockAt(world,pos)){IBlockState state=block.getDefaultState();if(block instanceof SpringPatch)state=((SpringPatch)block).getStateFromMeta((random.nextInt(4)<<2)|random.nextInt(4));world.setBlockState(pos,state,18);}}
        if(forest&&random.nextInt(12)==0){int x=cx*16+3,z=cz*16+3,length=3+random.nextInt(4),axis=random.nextInt(2),y=world.getTopSolidOrLiquidBlock(new BlockPos(x,0,z)).getY();if(y<10||y>240)return;for(int i=0;i<length;i++){BlockPos pos=new BlockPos(x+(axis==0?i:0),y,z+(axis==1?i:0));if(!world.isAirBlock(pos)||!world.getBlockState(pos.down()).isFullCube()||world.getTileEntity(pos.down())!=null)return;}IBlockState log=Blocks.LOG.getStateFromMeta(biome.getRegistryName()!=null&&biome.getRegistryName().getPath().contains("birch")?2:0).withProperty(BlockLog.LOG_AXIS,axis==0?BlockLog.EnumAxis.X:BlockLog.EnumAxis.Z);for(int i=0;i<length;i++)world.setBlockState(new BlockPos(x+(axis==0?i:0),y,z+(axis==1?i:0)),log,18);}
    }
}
