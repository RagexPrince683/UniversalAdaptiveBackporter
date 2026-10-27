package ganymedes01.etfuturum.api;
import ganymedes01.etfuturum.api.tags.helpers.*;
import net.minecraft.block.Block;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraftforge.oredict.OreDictionary;
/** Legacy entry points remain adapters to the canonical block/item tags. */
@Deprecated public final class BeePlantRegistry {
    public BeePlantRegistry(){}
    public static void addFlower(Block block,int meta){BlockTags.addTags(block,meta,"minecraft:bee_attractive");Item item=Item.getItemFromBlock(block);if(item!=Items.AIR)ItemTags.addTags(item,meta,"minecraft:bee_food");}
    public static void removeFlower(Block block,int meta){BlockTags.removeTags(block,meta,"minecraft:bee_attractive");Item item=Item.getItemFromBlock(block);if(item!=Items.AIR)ItemTags.removeTags(item,meta,"minecraft:bee_food");}
    public static void addCrop(Block block){BlockTags.addTags(block,OreDictionary.WILDCARD_VALUE,"minecraft:bee_growables");}
    public static void removeCrop(Block block){BlockTags.removeTags(block,OreDictionary.WILDCARD_VALUE,"minecraft:bee_growables");}
    public static boolean isFlower(Block block,int meta){Item item=Item.getItemFromBlock(block);return item!=Items.AIR&&ItemTags.hasTag(item,meta,"minecraft:bee_food")||BlockTags.hasTag(block,meta,"minecraft:bee_attractive");}
    public static boolean isCrop(Block block){return BlockTags.hasTag(block,OreDictionary.WILDCARD_VALUE,"minecraft:bee_growables");}
    public static void init(){}
}
