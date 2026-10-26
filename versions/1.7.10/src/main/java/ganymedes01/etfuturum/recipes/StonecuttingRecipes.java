package ganymedes01.etfuturum.recipes;

import ganymedes01.etfuturum.ModBlocks;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import java.util.*;

/** Explicit material conversions; never infers cutting from arbitrary crafting recipes. */
public final class StonecuttingRecipes {
	public static final class Recipe {
		public final ItemStack input, output;
		private Recipe(ItemStack input, ItemStack output) { this.input = input; this.output = output; }
	}
	private static final List<Recipe> RECIPES = new ArrayList<>();
	private static final List<Recipe> EDGES = new ArrayList<>();
	private StonecuttingRecipes() { }
	public static List<Recipe> all() { return Collections.unmodifiableList(RECIPES); }
	public static List<ItemStack> results(ItemStack input) {
		List<ItemStack> result = new ArrayList<>();
		if (input != null && !input.hasTagCompound()) for (Recipe recipe : RECIPES)
			if (input.isItemEqual(recipe.input)) result.add(recipe.output.copy());
		return result;
	}
	private static ItemStack stack(Object block, int meta) {
		if (block == null || block instanceof ModBlocks && !((ModBlocks) block).isEnabled()) return null;
		return new ItemStack(block instanceof ModBlocks ? ((ModBlocks) block).get() : (Block) block, 1, meta);
	}
	private static void cut(Object in, int im, Object out, int om, int count) {
		ItemStack input = stack(in, im), output = stack(out, om);
		if (input != null && output != null) { output.stackSize = count; EDGES.add(new Recipe(input, output)); }
	}
	private static void forms(Object in, int im, Object slab, int sm, Object stairs, Object wall, int wm) {
		if (slab != null) cut(in, im, slab, sm, 2);
		if (stairs != null) cut(in, im, stairs, 0, 1);
		if (wall != null) cut(in, im, wall, wm, 1);
	}
	public static void init() {
		EDGES.clear(); RECIPES.clear();
		forms(Blocks.stone, 0, ModBlocks.STONE_SLAB, 0, ModBlocks.STONE_STAIRS, null, 0);
		cut(Blocks.stone, 0, Blocks.stonebrick, 0, 1);
		forms(Blocks.stonebrick, 0, Blocks.stone_slab, 5, Blocks.stone_brick_stairs, ModBlocks.STONE_WALL, 0);
		cut(Blocks.stonebrick, 0, Blocks.stonebrick, 3, 1);
		forms(Blocks.stonebrick, 1, ModBlocks.STONE_SLAB, 2, ModBlocks.MOSSY_STONE_BRICK_STAIRS, ModBlocks.STONE_WALL, 1);
		forms(Blocks.cobblestone, 0, Blocks.stone_slab, 3, Blocks.stone_stairs, Blocks.cobblestone_wall, 0);
		forms(Blocks.mossy_cobblestone, 0, ModBlocks.STONE_SLAB, 1, ModBlocks.MOSSY_COBBLESTONE_STAIRS, Blocks.cobblestone_wall, 1);
		forms(Blocks.brick_block, 0, Blocks.stone_slab, 4, Blocks.brick_stairs, ModBlocks.STONE_WALL, 3);
		forms(Blocks.nether_brick, 0, Blocks.stone_slab, 6, Blocks.nether_brick_stairs, ModBlocks.NETHER_BRICK_WALL, 0);
		cut(Blocks.nether_brick, 0, ModBlocks.RED_NETHERBRICK, 1, 1);
		forms(ModBlocks.RED_NETHERBRICK, 0, ModBlocks.RED_NETHERBRICK_SLAB, 0, ModBlocks.RED_NETHERBRICK_STAIRS, ModBlocks.RED_NETHER_BRICK_WALL, 0);
		forms(Blocks.sandstone, 0, Blocks.stone_slab, 1, Blocks.sandstone_stairs, ModBlocks.STONE_WALL, 2);
		cut(Blocks.sandstone, 0, Blocks.sandstone, 1, 1); cut(Blocks.sandstone, 0, Blocks.sandstone, 2, 1);
		forms(Blocks.sandstone, 2, ModBlocks.STONE_SLAB, 3, null, null, 0);
		forms(ModBlocks.SMOOTH_SANDSTONE, 0, ModBlocks.SMOOTH_SANDSTONE_SLAB, 0, ModBlocks.SMOOTH_SANDSTONE_STAIRS, null, 0);
		forms(ModBlocks.RED_SANDSTONE, 0, ModBlocks.RED_SANDSTONE_SLAB, 0, ModBlocks.RED_SANDSTONE_STAIRS, ModBlocks.RED_SANDSTONE_WALL, 0);
		cut(ModBlocks.RED_SANDSTONE, 0, ModBlocks.RED_SANDSTONE, 1, 1); cut(ModBlocks.RED_SANDSTONE, 0, ModBlocks.RED_SANDSTONE, 2, 1);
		forms(ModBlocks.RED_SANDSTONE, 2, ModBlocks.RED_SANDSTONE_SLAB, 1, null, null, 0);
		forms(ModBlocks.SMOOTH_RED_SANDSTONE, 0, ModBlocks.SMOOTH_RED_SANDSTONE_SLAB, 0, ModBlocks.SMOOTH_RED_SANDSTONE_STAIRS, null, 0);
		forms(Blocks.quartz_block, 0, Blocks.stone_slab, 7, Blocks.quartz_stairs, null, 0);
		cut(Blocks.quartz_block, 0, Blocks.quartz_block, 1, 1); cut(Blocks.quartz_block, 0, Blocks.quartz_block, 2, 1);
		cut(Blocks.quartz_block, 0, ModBlocks.QUARTZ_BRICKS, 0, 1);
		forms(ModBlocks.SMOOTH_QUARTZ, 0, ModBlocks.SMOOTH_QUARTZ_SLAB, 0, ModBlocks.SMOOTH_QUARTZ_STAIRS, null, 0);
		forms(ModBlocks.SMOOTH_STONE, 0, Blocks.stone_slab, 0, null, null, 0);
		ModBlocks[] stoneStairs = {ModBlocks.GRANITE_STAIRS, ModBlocks.POLISHED_GRANITE_STAIRS, ModBlocks.DIORITE_STAIRS,
				ModBlocks.POLISHED_DIORITE_STAIRS, ModBlocks.ANDESITE_STAIRS, ModBlocks.POLISHED_ANDESITE_STAIRS};
		for (int i = 0; i < 6; i++) {
			forms(ModBlocks.STONE, i + 1, ModBlocks.STONE_SLAB_2, i, stoneStairs[i], i % 2 == 0 ? ModBlocks.STONE_WALL_2 : null, i / 2);
			if (i % 2 == 0) cut(ModBlocks.STONE, i + 1, ModBlocks.STONE, i + 2, 1);
		}
		forms(ModBlocks.PRISMARINE_BLOCK, 0, ModBlocks.PRISMARINE_SLAB, 0, ModBlocks.PRISMARINE_STAIRS, ModBlocks.PRISMARINE_WALL, 0);
		forms(ModBlocks.PRISMARINE_BLOCK, 1, ModBlocks.PRISMARINE_SLAB, 1, ModBlocks.PRISMARINE_STAIRS_BRICK, null, 0);
		forms(ModBlocks.PRISMARINE_BLOCK, 2, ModBlocks.PRISMARINE_SLAB, 2, ModBlocks.PRISMARINE_STAIRS_DARK, null, 0);
		cut(Blocks.end_stone, 0, ModBlocks.END_BRICKS, 0, 1);
		forms(ModBlocks.END_BRICKS, 0, ModBlocks.END_BRICK_SLAB, 0, ModBlocks.END_BRICK_STAIRS, ModBlocks.END_BRICK_WALL, 0);
		forms(ModBlocks.PURPUR_BLOCK, 0, ModBlocks.PURPUR_SLAB, 0, ModBlocks.PURPUR_STAIRS, null, 0);
		cut(ModBlocks.PURPUR_BLOCK, 0, ModBlocks.PURPUR_PILLAR, 0, 1);
		forms(ModBlocks.BLACKSTONE, 0, ModBlocks.BLACKSTONE_SLAB, 0, ModBlocks.BLACKSTONE_STAIRS, ModBlocks.BLACKSTONE_WALL, 0);
		cut(ModBlocks.BLACKSTONE, 0, ModBlocks.BLACKSTONE, 1, 1);
		forms(ModBlocks.BLACKSTONE, 1, ModBlocks.BLACKSTONE_SLAB, 1, ModBlocks.POLISHED_BLACKSTONE_STAIRS, ModBlocks.BLACKSTONE_WALL, 1);
		cut(ModBlocks.BLACKSTONE, 1, ModBlocks.BLACKSTONE, 2, 1); cut(ModBlocks.BLACKSTONE, 1, ModBlocks.BLACKSTONE, 4, 1);
		forms(ModBlocks.BLACKSTONE, 2, ModBlocks.BLACKSTONE_SLAB, 2, ModBlocks.POLISHED_BLACKSTONE_BRICK_STAIRS, ModBlocks.BLACKSTONE_WALL, 2);
		forms(ModBlocks.COBBLED_DEEPSLATE, 0, ModBlocks.DEEPSLATE_SLAB, 0, ModBlocks.COBBLED_DEEPSLATE_STAIRS, ModBlocks.DEEPSLATE_WALL, 0);
		cut(ModBlocks.COBBLED_DEEPSLATE, 0, ModBlocks.POLISHED_DEEPSLATE, 0, 1);
		cut(ModBlocks.COBBLED_DEEPSLATE, 0, ModBlocks.DEEPSLATE_BRICKS, 4, 1);
		forms(ModBlocks.POLISHED_DEEPSLATE, 0, ModBlocks.DEEPSLATE_SLAB, 1, ModBlocks.POLISHED_DEEPSLATE_STAIRS, ModBlocks.DEEPSLATE_WALL, 1);
		cut(ModBlocks.POLISHED_DEEPSLATE, 0, ModBlocks.DEEPSLATE_BRICKS, 0, 1);
		forms(ModBlocks.DEEPSLATE_BRICKS, 0, ModBlocks.DEEPSLATE_BRICK_SLAB, 0, ModBlocks.DEEPSLATE_BRICK_STAIRS, ModBlocks.DEEPSLATE_BRICK_WALL, 0);
		cut(ModBlocks.DEEPSLATE_BRICKS, 0, ModBlocks.DEEPSLATE_BRICKS, 2, 1);
		forms(ModBlocks.DEEPSLATE_BRICKS, 2, ModBlocks.DEEPSLATE_BRICK_SLAB, 1, ModBlocks.DEEPSLATE_TILE_STAIRS, ModBlocks.DEEPSLATE_BRICK_WALL, 1);
		ModBlocks[] tuffStairs = {ModBlocks.TUFF_STAIRS, ModBlocks.POLISHED_TUFF_STAIRS, ModBlocks.TUFF_BRICK_STAIRS};
		for (int i = 0; i < 3; i++) forms(ModBlocks.TUFF, i, ModBlocks.TUFF_SLAB, i, tuffStairs[i], ModBlocks.TUFF_WALL, i);
		cut(ModBlocks.TUFF, 0, ModBlocks.TUFF, 1, 1); cut(ModBlocks.TUFF, 0, ModBlocks.TUFF, 3, 1);
		cut(ModBlocks.TUFF, 1, ModBlocks.TUFF, 2, 1); cut(ModBlocks.TUFF, 2, ModBlocks.TUFF, 4, 1);
		forms(ModBlocks.PACKED_MUD, 1, ModBlocks.MUD_BRICK_SLAB, 0, ModBlocks.MUD_BRICK_STAIRS, ModBlocks.MUD_BRICK_WALL, 0);
		forms(ModBlocks.RESIN_BRICKS, 0, ModBlocks.RESIN_BRICK_SLAB, 0, ModBlocks.RESIN_BRICK_STAIRS, ModBlocks.RESIN_BRICK_WALL, 0);
		cut(ModBlocks.RESIN_BRICKS, 0, ModBlocks.CHISELED_RESIN_BRICKS, 0, 1);
		ModBlocks[] copperStairs = {ModBlocks.CUT_COPPER_STAIRS, ModBlocks.EXPOSED_CUT_COPPER_STAIRS, ModBlocks.WEATHERED_CUT_COPPER_STAIRS,
				ModBlocks.OXIDIZED_CUT_COPPER_STAIRS, ModBlocks.WAXED_CUT_COPPER_STAIRS, ModBlocks.WAXED_EXPOSED_CUT_COPPER_STAIRS,
				ModBlocks.WAXED_WEATHERED_CUT_COPPER_STAIRS, ModBlocks.WAXED_OXIDIZED_CUT_COPPER_STAIRS};
		for (int i = 0; i < 8; i++) {
			int base = i < 4 ? i : i + 4;
			cut(ModBlocks.COPPER_BLOCK, base, ModBlocks.COPPER_BLOCK, base + 4, 4);
			cut(ModBlocks.COPPER_BLOCK, base, ModBlocks.CUT_COPPER_SLAB, i, 8);
			cut(ModBlocks.COPPER_BLOCK, base, copperStairs[i], 0, 4);
			cut(ModBlocks.COPPER_BLOCK, base, ModBlocks.CHISELED_COPPER, i, 4);
			forms(ModBlocks.COPPER_BLOCK, base + 4, ModBlocks.CUT_COPPER_SLAB, i, copperStairs[i], null, 0);
			cut(ModBlocks.COPPER_BLOCK, base + 4, ModBlocks.CHISELED_COPPER, i, 1);
		}
		// Flatten only one-block transformations: slabs never become a source for free material.
		for (Recipe edge : EDGES) expand(edge.input, edge.input, new HashSet<String>());
	}
	private static void expand(ItemStack root, ItemStack current, Set<String> visited) {
		String key = net.minecraft.item.Item.getIdFromItem(current.getItem()) + ":" + current.getItemDamage();
		if (!visited.add(key)) return;
		for (Recipe edge : EDGES) if (current.isItemEqual(edge.input)) {
			boolean exists = root.isItemEqual(edge.output);
			for (Recipe recipe : RECIPES) if (root.isItemEqual(recipe.input) && edge.output.isItemEqual(recipe.output)) { exists = true; break; }
			if (!exists) RECIPES.add(new Recipe(root.copy(), edge.output.copy()));
			if (edge.output.stackSize == 1) expand(root, edge.output, visited);
		}
	}
}
