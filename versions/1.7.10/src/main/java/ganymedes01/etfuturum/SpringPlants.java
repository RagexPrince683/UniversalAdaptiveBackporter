package ganymedes01.etfuturum;

import java.util.*;
import cpw.mods.fml.common.registry.GameRegistry;
import ganymedes01.etfuturum.blocks.*;
import ganymedes01.etfuturum.configuration.ContentAvailability;
import net.minecraft.block.Block;
import net.minecraft.init.*;
import net.minecraft.item.*;
import net.minecraft.world.World;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.oredict.ShapelessOreRecipe;

public final class SpringPlants {
    public static final String[] PLANTS={"bush","firefly_bush","short_dry_grass","tall_dry_grass","cactus_flower","wildflowers","leaf_litter"};
    private SpringPlants(){}
    public static void initialize(){for(String name:PLANTS)ModernAdditions.add(name,"spring_to_life",name.equals("wildflowers")||name.equals("leaf_litter")?new BlockSpringPatch(name.equals("leaf_litter")):new BlockSpringPlant(name));GameRegistry.registerFuelHandler(stack->{String name=Item.itemRegistry.getNameForObject(stack.getItem());return name!=null&&(name.equals("etfuturum:leaf_litter")||name.equals("etfuturum:short_dry_grass")||name.equals("etfuturum:tall_dry_grass"))?100:0;});}
    public static void recipes(){
        if(ContentAvailability.enabled("spring_to_life"))for(String egg:new String[]{"brown_egg","blue_egg"}){GameRegistry.addRecipe(new net.minecraftforge.oredict.ShapedOreRecipe(new ItemStack(Items.cake),"MMM","SES","WWW",'M',Items.milk_bucket,'S',Items.sugar,'E',ModernAdditions.item(egg),'W',Items.wheat));GameRegistry.addRecipe(new ShapelessOreRecipe(new ItemStack(Items.pumpkin_pie),Blocks.pumpkin,Items.sugar,ModernAdditions.item(egg)));}

        if(ContentAvailability.available(ModernAdditions.item("cactus_flower")))GameRegistry.addRecipe(new ShapelessOreRecipe(new ItemStack(Items.dye,1,9),ModernAdditions.item("cactus_flower")));
        if(ContentAvailability.available(ModernAdditions.item("wildflowers")))GameRegistry.addRecipe(new ShapelessOreRecipe(new ItemStack(Items.dye,1,11),ModernAdditions.item("wildflowers")));
        if(ContentAvailability.available(ModernAdditions.item("leaf_litter"))){GameRegistry.addSmelting(new ItemStack(Blocks.leaves,1,32767),ModernAdditions.stack("leaf_litter"),.1F);GameRegistry.addSmelting(new ItemStack(Blocks.leaves2,1,32767),ModernAdditions.stack("leaf_litter"),.1F);}
        if(ganymedes01.etfuturum.configuration.configs.ConfigFunctions.enableAutoAddComposter)for(String name:PLANTS)if(ContentAvailability.available(ModernAdditions.item(name)))ganymedes01.etfuturum.api.CompostingRegistry.registerCompostable(ModernAdditions.stack(name),name.equals("wildflowers")||name.equals("cactus_flower")?65:30);
    }
    public static void cactus(World world,int x,int y,int z,Random random){if(world.isRemote||!ContentAvailability.enabled("spring_to_life")||world.getBlock(x,y,z)!=Blocks.cactus||!world.isAirBlock(x,y+1,z)||random.nextInt(30)!=0)return;world.setBlock(x,y+1,z,ModernAdditions.block("cactus_flower"),0,3);}
    public static void generate(World world,int cx,int cz){
        if(world.isRemote||world.provider.dimensionId!=0||!ContentAvailability.enabled("spring_to_life"))return;
        Random random=new Random(world.getSeed()^cx*341873128712L^cz*132897987541L^0x535052494e47L);net.minecraft.world.biome.BiomeGenBase biome=world.getBiomeGenForCoords(cx*16+8,cz*16+8);boolean forest=BiomeDictionary.isBiomeOfType(biome,BiomeDictionary.Type.FOREST),dry=BiomeDictionary.isBiomeOfType(biome,BiomeDictionary.Type.DRY)&&biome.temperature>1,wets=BiomeDictionary.isBiomeOfType(biome,BiomeDictionary.Type.SWAMP)||BiomeDictionary.isBiomeOfType(biome,BiomeDictionary.Type.RIVER);
        for(int i=0;i<9;i++){int x=cx*16+1+random.nextInt(14),z=cz*16+1+random.nextInt(14),y=world.getTopSolidOrLiquidBlock(x,z);String name=dry?(random.nextBoolean()?"short_dry_grass":"tall_dry_grass"):wets&&random.nextInt(3)==0?"firefly_bush":forest?(random.nextInt(3)==0?"wildflowers":"leaf_litter"):"bush";Block block=ModernAdditions.block(name);if(world.isAirBlock(x,y,z)&&block.canPlaceBlockAt(world,x,y,z))world.setBlock(x,y,z,block,block instanceof BlockSpringPatch?(random.nextInt(4)<<2)|random.nextInt(4):0,2);}
        if(forest&&random.nextInt(12)==0){int x=cx*16+3,z=cz*16+3,length=3+random.nextInt(4),axis=random.nextInt(2),y=world.getTopSolidOrLiquidBlock(x,z);if(y<10||y>240)return;for(int i=0;i<length;i++){int px=x+(axis==0?i:0),pz=z+(axis==1?i:0);if(!world.isAirBlock(px,y,pz)||!world.getBlock(px,y-1,pz).isNormalCube()||world.getTileEntity(px,y-1,pz)!=null)return;}int meta=(biome.biomeName.toLowerCase(Locale.ROOT).contains("birch")?2:0)|(axis==0?4:8);for(int i=0;i<length;i++)world.setBlock(x+(axis==0?i:0),y,z+(axis==1?i:0),Blocks.log,meta,2);}
    }
}
