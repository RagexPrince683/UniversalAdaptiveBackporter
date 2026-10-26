package ganymedes01.etfuturum;

import java.util.*;
import net.minecraft.init.*;
import net.minecraft.item.*;
import net.minecraftforge.oredict.OreDictionary;

/** Native composting percentages, including the maintained multi-layer extension. */
public final class Composting {
    private static final Map<Item, Map<Integer, Integer>> CHANCES = new IdentityHashMap<>();
    private static final Map<String, Integer> TAGS = new LinkedHashMap<>();
    private static final boolean AUTOMATIC = Features.enabled("functions.enableAutoAddComposter");
    private Composting() { }
    static void bind() { }
    public static void register(ItemStack stack, int percent) {
        if (stack.isEmpty() || percent < 1 || percent > 700) throw new IllegalArgumentException("Invalid composting entry");
        CHANCES.computeIfAbsent(stack.getItem(), item -> new HashMap<>()).put(stack.getMetadata(), percent);
    }
    public static void register(String ore, int percent) {
        if (percent < 1 || percent > 700) throw new IllegalArgumentException("Invalid composting percentage"); TAGS.put(ore, percent);
    }
    public static void remove(ItemStack stack) { if (!stack.isEmpty()) CHANCES.computeIfAbsent(stack.getItem(), item -> new HashMap<>()).put(stack.getMetadata(), 0); }
    public static int chance(ItemStack stack) {
        if (stack.isEmpty()) return 0;
        Map<Integer, Integer> chances = CHANCES.get(stack.getItem());
        if (chances != null) { Integer value = chances.get(stack.getMetadata()); if (value == null) value = chances.get(OreDictionary.WILDCARD_VALUE); if (value != null) return value; }
        for (int ore : OreDictionary.getOreIDs(stack)) { Integer value = TAGS.get(OreDictionary.getOreName(ore)); if (value != null) return value; }
        return 0;
    }
    private static void item(int percent, Item... items) { for (Item item : items) register(new ItemStack(item, 1, OreDictionary.WILDCARD_VALUE), percent); }
    private static void block(int percent, net.minecraft.block.Block... blocks) { for (net.minecraft.block.Block block : blocks) if (Item.getItemFromBlock(block) != Items.AIR) register(new ItemStack(block, 1, OreDictionary.WILDCARD_VALUE), percent); }
    static void initialize() {
        if (!AUTOMATIC) return;
        register("listAllSeeds", 30); register("treeSapling", 30); register("treeLeaves", 30);
        item(30, Items.MELON_SEEDS, Items.PUMPKIN_SEEDS, Items.WHEAT_SEEDS, Items.BEETROOT_SEEDS);
        register(new ItemStack(Blocks.TALLGRASS, 1, 1), 30); block(30, Blocks.LEAVES, Blocks.LEAVES2);
        block(50, Blocks.CACTUS, Blocks.VINE); item(50, Items.MELON, Items.REEDS); register(new ItemStack(Blocks.DOUBLE_PLANT, 1, 2), 50);
        item(65, Items.APPLE, Items.BEETROOT, Items.CARROT, Items.POTATO, Items.WHEAT, Items.NETHER_WART);
        register("cropCarrot", 65); register("cropPotato", 65); register("cropWheat", 65);
        register(new ItemStack(Items.DYE, 1, 3), 65); register(new ItemStack(Blocks.TALLGRASS, 1, 2), 65);
        for (int meta : new int[]{0, 1, 3, 4, 5}) register(new ItemStack(Blocks.DOUBLE_PLANT, 1, meta), 65);
        block(65, Blocks.RED_FLOWER, Blocks.YELLOW_FLOWER, Blocks.WATERLILY, Blocks.MELON_BLOCK, Blocks.BROWN_MUSHROOM, Blocks.RED_MUSHROOM, Blocks.PUMPKIN);
        item(85, Items.BAKED_POTATO, Items.BREAD, Items.COOKIE); block(85, Blocks.HAY_BLOCK, Blocks.RED_MUSHROOM_BLOCK, Blocks.BROWN_MUSHROOM_BLOCK);
        item(100, Items.CAKE, Items.PUMPKIN_PIE); block(100, Blocks.CAKE);
        for (String name : new String[]{"sweet_berries", "mangrove_roots"}) if (Content.available(name)) register(Content.stack(name), 30);
        for(String name:new String[]{"glow_berries","moss_carpet"})if(Content.available(name))register(Content.stack(name),30);
        for(String name:new String[]{"moss_block","azalea"})if(Content.available(name))register(Content.stack(name),65);
        if(Content.available("flowering_azalea"))register(Content.stack("flowering_azalea"),85);
    }
}
