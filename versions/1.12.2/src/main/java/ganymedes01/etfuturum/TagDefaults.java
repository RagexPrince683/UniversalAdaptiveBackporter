package ganymedes01.etfuturum;
import ganymedes01.etfuturum.api.tags.helpers.BlockTags;
import net.minecraft.block.*;
import net.minecraft.item.*;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import ganymedes01.etfuturum.api.tags.helpers.ItemTags;
import ganymedes01.etfuturum.api.crops.IBeeGrowable;
import net.minecraft.init.Blocks;
final class TagDefaults {
    private TagDefaults(){}
    static void initialize(){for(Block block:new Block[]{Blocks.GRASS,Blocks.STONE,Blocks.SNOW,Blocks.SNOW_LAYER,Blocks.PACKED_ICE,Blocks.GRAVEL})BlockTags.addTags(block,"minecraft:goats_spawnable_on");for(Block block:new Block[]{Blocks.STONE,Blocks.PACKED_ICE,Blocks.LOG,Blocks.LOG2,Blocks.IRON_ORE,Blocks.COAL_ORE,Blocks.EMERALD_ORE,Content.block("copper_ore"),Content.block("mangrove_log"),Content.block("cherry_log")})BlockTags.addTags(block,"minecraft:snaps_goat_horn");}
    static void dynamic(){
        BlockTags.addInheritors("minecraft:campfires",UAB.ID+":bee_hive_fumigator");
        for(Block block:ForgeRegistries.BLOCKS){
            if(block instanceof BlockFlower){BlockTags.addTags(block,"minecraft:bee_attractive");Item item=Item.getItemFromBlock(block);if(item!=net.minecraft.init.Items.AIR)ItemTags.addTags(item,"minecraft:bee_food");}
            if(block instanceof BlockCrops||block instanceof BlockStem||block instanceof BerryBush||block instanceof IBeeGrowable)BlockTags.addTags(block,"minecraft:bee_growables");
            if(block instanceof BlockLeaves||block instanceof BlockHay||block instanceof BlockSponge||block instanceof BlockNetherWart)BlockTags.addTags(block,"minecraft:mineable/hoe");
        }
        for(int meta:new int[]{0,1,4,5}){BlockTags.addTags(Blocks.DOUBLE_PLANT,meta,"minecraft:bee_attractive");ItemTags.addTags(Item.getItemFromBlock(Blocks.DOUBLE_PLANT),meta,"minecraft:bee_food");}
        for(String name:new String[]{"lily_of_the_valley","cornflower","wither_rose","torchflower","pitcher_plant","flowering_azalea","flowering_azalea_leaves","mangrove_sapling","cherry_leaves","pink_petals"})if(Content.BLOCKS.containsKey(name)){Block block=Content.block(name);BlockTags.addTags(block,"minecraft:bee_attractive");if(Item.getItemFromBlock(block)!=net.minecraft.init.Items.AIR)ItemTags.addTags(Item.getItemFromBlock(block),"minecraft:bee_food");}
        BlockTags.addTags(Blocks.CHORUS_FLOWER,"minecraft:bee_attractive");ItemTags.addTags(Item.getItemFromBlock(Blocks.CHORUS_FLOWER),"minecraft:bee_food");
        for(Item item:ForgeRegistries.ITEMS){java.util.Set<String> tools=item.getToolClasses(new ItemStack(item));if(tools.contains("shovel"))ItemTags.addTags(item,UAB.ID+":dirt_path_tools");if(tools.contains("axe"))ItemTags.addTags(item,UAB.ID+":stripped_log_tools");}
        ItemTags.addTags(net.minecraft.init.Items.BLAZE_POWDER,"minecraft:brewing_fuel");ItemTags.addTags(net.minecraft.init.Items.DYE,4,UAB.ID+":enchantment_fuel");
    }
}
