package ganymedes01.etfuturum;

import java.util.*;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.*;

/** Explicit material graph; only one-block conversions propagate to downstream cuts. */
public final class StonecuttingRecipes {
    private static final Map<Key, List<ItemStack>> EDGES = new LinkedHashMap<>(), RECIPES = new LinkedHashMap<>();
    private StonecuttingRecipes() { }
    public static List<ItemStack> results(ItemStack input) {
        List<ItemStack> values = input.isEmpty() || input.hasTagCompound() ? null : RECIPES.get(new Key(input));
        List<ItemStack> result = new ArrayList<>(); if (values != null) for (ItemStack value : values) result.add(value.copy()); return result;
    }
    private static boolean available(ItemStack stack) { return !stack.isEmpty() && (!stack.getItem().getRegistryName().getNamespace().equals(UAB.ID) || Content.available(stack.getItem().getRegistryName().getPath())); }
    private static ItemStack stack(Block block, int meta) { return new ItemStack(block, 1, meta); }
    private static void cut(ItemStack input, ItemStack output, int count) {
        if (!available(input) || !available(output)) return;
        output = output.copy(); output.setCount(count); EDGES.computeIfAbsent(new Key(input), key -> new ArrayList<>()).add(output);
    }
    private static void forms(Block input, int meta, Block slab, int slabMeta, Block stairs, Block wall, int wallMeta) {
        if (slab != null) cut(stack(input, meta), stack(slab, slabMeta), 2);
        if (stairs != null) cut(stack(input, meta), stack(stairs, 0), 1);
        if (wall != null) cut(stack(input, meta), stack(wall, wallMeta), 1);
    }
    private static void change(String input, String output, int count) { cut(Content.stack(input), Content.stack(output), count); }
    static void initialize() {
        EDGES.clear(); RECIPES.clear();
        for (Map.Entry<String, IBlockState> shape : Content.SHAPE_BASES.entrySet()) {
            if (shape.getValue().getMaterial() == net.minecraft.block.material.Material.WOOD) continue;
            IBlockState base = shape.getValue();
            cut(new ItemStack(base.getBlock(), 1, base.getBlock().getMetaFromState(base)), Content.stack(shape.getKey()), shape.getKey().endsWith("_slab") ? 2 : 1);
        }
        forms(Blocks.COBBLESTONE, 0, Blocks.STONE_SLAB, 3, Blocks.STONE_STAIRS, Blocks.COBBLESTONE_WALL, 0);
        forms(Blocks.MOSSY_COBBLESTONE, 0, null, 0, null, Blocks.COBBLESTONE_WALL, 1);
        cut(stack(Blocks.STONE, 0), stack(Blocks.STONEBRICK, 0), 1);
        forms(Blocks.STONEBRICK, 0, Blocks.STONE_SLAB, 5, Blocks.STONE_BRICK_STAIRS, null, 0);
        cut(stack(Blocks.STONEBRICK, 0), stack(Blocks.STONEBRICK, 3), 1);
        forms(Blocks.BRICK_BLOCK, 0, Blocks.STONE_SLAB, 4, Blocks.BRICK_STAIRS, null, 0);
        forms(Blocks.NETHER_BRICK, 0, Blocks.STONE_SLAB, 6, Blocks.NETHER_BRICK_STAIRS, null, 0);
        forms(Blocks.SANDSTONE, 0, Blocks.STONE_SLAB, 1, Blocks.SANDSTONE_STAIRS, null, 0);
        forms(Blocks.RED_SANDSTONE, 0, Blocks.STONE_SLAB2, 0, Blocks.RED_SANDSTONE_STAIRS, null, 0);
        for (Block sandstone : new Block[]{Blocks.SANDSTONE, Blocks.RED_SANDSTONE}) for (int meta = 1; meta <= 2; meta++) cut(stack(sandstone, 0), stack(sandstone, meta), 1);
        forms(Blocks.QUARTZ_BLOCK, 0, Blocks.STONE_SLAB, 7, Blocks.QUARTZ_STAIRS, null, 0);
        for (int meta = 1; meta <= 2; meta++) cut(stack(Blocks.QUARTZ_BLOCK, 0), stack(Blocks.QUARTZ_BLOCK, meta), 1);
        cut(stack(Blocks.QUARTZ_BLOCK, 0), Content.stack("quartz_bricks"), 1);
        cut(stack(Blocks.END_STONE, 0), stack(Blocks.END_BRICKS, 0), 1);
        forms(Blocks.PURPUR_BLOCK, 0, Blocks.PURPUR_SLAB, 0, Blocks.PURPUR_STAIRS, null, 0);
        cut(stack(Blocks.PURPUR_BLOCK, 0), stack(Blocks.PURPUR_PILLAR, 0), 1);
        for (int meta : new int[]{1, 3, 5}) cut(stack(Blocks.STONE, meta), stack(Blocks.STONE, meta+1), 1);
        for (String[] path : new String[][]{{"blackstone","polished_blackstone"},{"polished_blackstone","polished_blackstone_bricks"},{"polished_blackstone","chiseled_polished_blackstone"},{"cobbled_deepslate","polished_deepslate"},{"cobbled_deepslate","chiseled_deepslate"},{"polished_deepslate","deepslate_bricks"},{"deepslate_bricks","deepslate_tiles"},{"tuff","polished_tuff"},{"tuff","chiseled_tuff"},{"polished_tuff","tuff_bricks"},{"tuff_bricks","chiseled_tuff_bricks"}}) change(path[0], path[1], 1);
        for (String stage : new String[]{"", "exposed_", "weathered_", "oxidized_"}) for (String wax : new String[]{"", "waxed_"}) change(wax + (stage.isEmpty() ? "copper_block" : stage + "copper"), wax + stage + "cut_copper", 4);
        for (Key input : EDGES.keySet()) expand(input, input, new HashSet<>());
    }
    private static void expand(Key root, Key current, Set<Key> visited) {
        if (!visited.add(current)) return;
        List<ItemStack> edges = EDGES.get(current); if (edges == null) return;
        List<ItemStack> results = RECIPES.computeIfAbsent(root, key -> new ArrayList<>());
        for (ItemStack edge : edges) {
            Key output = new Key(edge); boolean found = root.equals(output);
            for (ItemStack existing : results) if (new Key(existing).equals(output)) { found = true; break; }
            if (!found) results.add(edge.copy());
            if (edge.getCount() == 1) expand(root, output, visited);
        }
    }
    private static final class Key {
        final Item item; final int meta;
        Key(ItemStack stack) { item = stack.getItem(); meta = stack.getMetadata(); }
        @Override public int hashCode() { return System.identityHashCode(item)*31+meta; }
        @Override public boolean equals(Object value) { return value instanceof Key && ((Key) value).item == item && ((Key) value).meta == meta; }
    }
}
