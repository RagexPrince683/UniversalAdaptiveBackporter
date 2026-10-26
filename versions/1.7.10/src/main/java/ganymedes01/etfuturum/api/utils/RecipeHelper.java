package ganymedes01.etfuturum.api.utils;

import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.RecipeSorter;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;
import ganymedes01.etfuturum.Tags;

import java.util.function.Predicate;

public final class RecipeHelper {

    private RecipeHelper() {}

    public static void addRecipe(ItemStack output, Object... input) {
        addShapedRecipe(output, input);
    }

    public static void addRecipe(IRecipe recipe) {
        if (!validateItems(recipe.getRecipeOutput())) return;
        Object ingredients = null;
        if (recipe instanceof ShapedOreRecipe) ingredients = ((ShapedOreRecipe) recipe).getInput();
        else if (recipe instanceof ShapelessOreRecipe) ingredients = ((ShapelessOreRecipe) recipe).getInput();
        else if (recipe instanceof net.minecraft.item.crafting.ShapedRecipes) ingredients = ((net.minecraft.item.crafting.ShapedRecipes) recipe).recipeItems;
        else if (recipe instanceof net.minecraft.item.crafting.ShapelessRecipes) ingredients = ((net.minecraft.item.crafting.ShapelessRecipes) recipe).recipeItems;
        if (ingredients instanceof Object[]) for (Object input : (Object[]) ingredients) if (input != null && !validateIngredient(input)) return;
        if (ingredients instanceof Iterable) for (Object input : (Iterable<?>) ingredients) if (input != null && !validateIngredient(input)) return;
        GameRegistry.addRecipe(recipe);
    }

    private static boolean validateIngredient(Object input) {
        if (input instanceof Iterable) {
            for (Object alternative : (Iterable<?>) input) if (validateIngredient(alternative)) return true;
            return false;
        }
        return validateItems(input);
    }

    public static void init() {
        RecipeSorter.register(Tags.MOD_ID + ":shaped_high_priority",
            HighPriorityShapedRecipe.class, RecipeSorter.Category.SHAPED, "before:minecraft:shaped");
        RecipeSorter.register(Tags.MOD_ID + ":shapeless_high_priority",
            HighPriorityShapelessRecipe.class, RecipeSorter.Category.SHAPELESS, "before:minecraft:shapeless");

        RecipeSorter.register(Tags.MOD_ID + ":shaped_low_priority",
            LowPriorityShapedRecipe.class, RecipeSorter.Category.SHAPED, "after:minecraft:shaped");
        RecipeSorter.register(Tags.MOD_ID + ":shapeless_low_priority",
            LowPriorityShapelessRecipe.class, RecipeSorter.Category.SHAPELESS, "after:minecraft:shapeless");
    }

    public static void registerOre(String oreName, ItemStack ore) {
        if (validateItems(ore)) {
            OreDictionary.registerOre(oreName, ore);
        }
    }

    public static void registerOre(String oreName, Item ore) {
        if (validateItems(ore)) {
            OreDictionary.registerOre(oreName, ore);
        }
    }

    public static void registerOre(String oreName, Block ore) {
        if (validateItems(ore)) {
            OreDictionary.registerOre(oreName, ore);
        }
    }

    public static void addSmelting(Item input, ItemStack output, float exp) {
        if (validateItems(input) && validateItems(output)) {
            GameRegistry.addSmelting(input, output, exp);
        }
    }

    public static void addSmelting(Block input, ItemStack output, float exp) {
        if (validateItems(input) && validateItems(output)) {
            GameRegistry.addSmelting(input, output, exp);
        }
    }

    public static void addSmelting(ItemStack input, ItemStack output, float exp) {
        if (validateItems(input) && validateItems(output)) {
            GameRegistry.addSmelting(input, output, exp);
        }
    }

    /// Registers recipes with no priority sorting.
    /// This function checks the recipe against {@link RecipeHelper#validateItems(Object...)} to ensure everything inside it exists.
    /// This also means your recipe must be registered AFTER pre-init, where all blocks would be registered by then.
    public static void addShapelessRecipe(Item output, Object... objects) {
        addShapelessRecipe(Priority.NORMAL, new ItemStack(output), objects);
    }

    /// Registers recipes with no priority sorting.
    /// This function checks the recipe against {@link RecipeHelper#validateItems(Object...)} to ensure everything inside it exists.
    /// This also means your recipe must be registered AFTER pre-init, where all blocks would be registered by then.
    public static void addShapelessRecipe(Block output, Object... objects) {
        addShapelessRecipe(Priority.NORMAL, new ItemStack(output), objects);
    }

    /// Registers recipes with no priority sorting.
    /// This function checks the recipe against {@link RecipeHelper#validateItems(Object...)} to ensure everything inside it exists.
    /// This also means your recipe must be registered AFTER pre-init, where all blocks would be registered by then.
    public static void addShapelessRecipe(ItemStack output, Object... objects) {
        addShapelessRecipe(Priority.NORMAL, output, objects);
    }

    /// Registers recipes with the specified priority, check the enum comments for more information.
    /// This function checks the recipe against {@link RecipeHelper#validateItems(Object...)} to ensure everything inside it exists.
    /// This also means your recipe must be registered AFTER pre-init, where all blocks would be registered by then.
    public static void addShapelessRecipe(Priority priority, Item output, Object... objects) {
        addShapelessRecipe(priority, new ItemStack(output), objects);
    }

    /// Registers recipes with the specified priority, check the enum comments for more information.
    /// This function checks the recipe against {@link RecipeHelper#validateItems(Object...)} to ensure everything inside it exists.
    /// This also means your recipe must be registered AFTER pre-init, where all blocks would be registered by then.
    public static void addShapelessRecipe(Priority priority, Block output, Object... objects) {
        addShapelessRecipe(priority, new ItemStack(output), objects);
    }

    /// Registers recipes with the specified priority, check the enum comments for more information.
    /// This function checks the recipe against {@link RecipeHelper#validateItems(Object...)} to ensure everything inside it exists.
    /// This also means your recipe must be registered AFTER pre-init, where all blocks would be registered by then.
    public static void addShapelessRecipe(Priority priority, ItemStack output, Object... objects) {
        priority.addShapelessRecipe(output, objects);
    }

    /// Registers recipes with no priority sorting.
    /// This function checks the recipe against {@link RecipeHelper#validateItems(Object...)} to ensure everything inside it exists.
    /// This also means your recipe must be registered AFTER pre-init, where all blocks would be registered by then.
    public static void addShapedRecipe(Item output, Object... objects) {
        addShapedRecipe(Priority.NORMAL, new ItemStack(output), objects);
    }

    /// Registers recipes with no priority sorting.
    /// This function checks the recipe against {@link RecipeHelper#validateItems(Object...)} to ensure everything inside it exists.
    /// This also means your recipe must be registered AFTER pre-init, where all blocks would be registered by then.
    public static void addShapedRecipe(Block output, Object... objects) {
        addShapedRecipe(Priority.NORMAL, new ItemStack(output), objects);
    }

    /// Registers recipes with no priority sorting.
    /// This function checks the recipe against {@link RecipeHelper#validateItems(Object...)} to ensure everything inside it exists.
    /// This also means your recipe must be registered AFTER pre-init, where all blocks would be registered by then.
    public static void addShapedRecipe(ItemStack output, Object... objects) {
        addShapedRecipe(Priority.NORMAL, output, objects);
    }

    /// Registers recipes with the specified priority, check the enum comments for more information.
    /// This function checks the recipe against {@link RecipeHelper#validateItems(Object...)} to ensure everything inside it exists.
    /// This also means your recipe must be registered AFTER pre-init, where all blocks would be registered by then.
    public static void addShapedRecipe(Priority priority, Item output, Object... objects) {
        addShapedRecipe(priority, new ItemStack(output), objects);
    }

    /// Registers recipes with the specified priority, check the enum comments for more information.
    /// This function checks the recipe against {@link RecipeHelper#validateItems(Object...)} to ensure everything inside it exists.
    /// This also means your recipe must be registered AFTER pre-init, where all blocks would be registered by then.
    public static void addShapedRecipe(Priority priority, Block output, Object... objects) {
        addShapedRecipe(priority, new ItemStack(output), objects);
    }

    /// Registers recipes with the specified priority, check the enum comments for more information.
    /// This function checks the recipe against {@link RecipeHelper#validateItems(Object...)} to ensure everything inside it exists.
    /// This also means your recipe must be registered AFTER pre-init, where all blocks would be registered by then.
    public static void addShapedRecipe(Priority priority, ItemStack output, Object... objects) {
        priority.addShapedRecipe(output, objects);
    }

    /// Does not evaluate NBT of passed in stacks
    public static void removeAllRecipesWithOutput(ItemStack find) {
        removeAllRecipesWithOutput(find, true);
    }

    /// Does not evaluate NBT of passed in stacks
    public static void removeAllRecipesWithOutput(ItemStack find, boolean matchWildcards) {
        removeAllRecipesWithOutput(find.getItem(), find.getItemDamage(), matchWildcards);
    }

    public static void removeAllRecipesWithOutput(Block block, int meta, boolean matchWildcards) {
        removeAllRecipesWithOutput(Item.getItemFromBlock(block), meta, matchWildcards);
    }

    public static void removeAllRecipesWithOutput(Item item, int meta, boolean matchWildcards) {
        removeAllMatchingRecipes(iRecipe -> {
            ItemStack stack = iRecipe.getRecipeOutput();
            return stack != null
                && stack.getItem() == item
                && ((matchWildcards && stack.getItemDamage() == OreDictionary.WILDCARD_VALUE) || meta == stack.getItemDamage());
        });
    }

    /// Removes any recipe that tests true for the specific predicate.
    public static void removeAllMatchingRecipes(Predicate<IRecipe> removeCondition) {
        CraftingManager.getInstance().getRecipeList().removeIf(removeCondition);
    }

    /// Does not evaluate NBT of passed in stacks
    public static void removeFirstRecipeWithOutput(ItemStack find) {
        removeFirstRecipeWithOutput(find, true);
    }

    /// Does not evaluate NBT of passed in stacks
    public static void removeFirstRecipeWithOutput(ItemStack find, boolean matchWildcards) {
        removeFirstRecipeWithOutput(find.getItem(), find.getItemDamage(), matchWildcards);
    }

    public static void removeFirstRecipeWithOutput(Block block, int meta, boolean matchWildcards) {
        removeFirstRecipeWithOutput(Item.getItemFromBlock(block), meta, matchWildcards);
    }

    public static void removeFirstRecipeWithOutput(Item item, int meta, boolean matchWildcards) {
        removeFirstMatchingRecipe(iRecipe -> {
            ItemStack stack = iRecipe.getRecipeOutput();
            return stack != null
                && stack.getItem() == item
                && ((matchWildcards && stack.getItemDamage() == OreDictionary.WILDCARD_VALUE) || meta == stack.getItemDamage());
        });
    }

    /// Removes any recipe that tests true for the specific predicate.
    public static void removeFirstMatchingRecipe(Predicate<IRecipe> removeCondition) {
        for(IRecipe recipe : CraftingManager.getInstance().getRecipeList()) {
            if(removeCondition.test(recipe)) {
                CraftingManager.getInstance().getRecipeList().remove(recipe);
                return;
            }
        }
    }

    /// Checks if the objects actually exist in the item/block registry, returns false if any of them are not registered.
    /// Useful for registering recipes and stuff, but will only really work after preInit.
    /// This is because we can't expect items and blocks to be finished registering before that.
    public static boolean validateItems(Object... objects) {
        for (Object object : objects) {
            if (object == null || object == Blocks.air || !ganymedes01.etfuturum.configuration.ContentAvailability.available(object)) return false;
            if (object instanceof String) continue;
            if (object instanceof ItemStack stack) {
                if (stack.getItem() == null || stack.getItem().delegate.name() == null) return false;
            } else if (object instanceof Item item) {
                if (item.delegate.name() == null) return false;
            } else if (object instanceof Block block) {
                if (block.delegate.name() == null) return false;
            }
        }
        return true;
    }

    public static class Templates {
        /// Registers a recipe to compress an ore block to compress to 9 (or 4), then back to 9 (or 4)
        /// @param small If the block is "small" (compresses to/from 4)
        public void addOreBlockRecipe(Priority priority, Block oreBlock, int oreBlockMeta, Item material, int materialMeta, boolean small) {
            if(small) {
                add2by2Recipe(priority, new ItemStack(oreBlock, 1, oreBlockMeta), new ItemStack(material, 1, materialMeta));
            } else {
                add3by3Recipe(priority, new ItemStack(oreBlock, 1, oreBlockMeta), new ItemStack(material, 1, materialMeta));
            }
            addShapedRecipe(priority, new ItemStack(material, small ? 4 : 9, materialMeta),
                "x", 'x', new ItemStack(oreBlock, 1, oreBlockMeta));
        }

        /// Registers a shaped recipe, that returns the input item in a 2x2 shape, to the output stack.
        public void add2by2Recipe(Priority priority, ItemStack output, Object input) {
            addShapedRecipe(priority, output, "xx", "xx", 'x', input);
        }

        /// Registers a shaped recipe, that returns the input item in a 3x3 shape, to the output stack.
        public void add3by3Recipe(Priority priority, ItemStack output, Object input) {
            addShapedRecipe(priority, output, "xxx", "xxx", "xxx", 'x', input);
        }

        /// Registers a recipe that takes 3 of the input object across the bottom of a 3x3 crafting grid, and outputs 6 of the output block
        public void addSlabRecipe(Priority priority, Block output, int outputMeta, Object input) {
            addShapedRecipe(priority, new ItemStack(output, 6, outputMeta), "xxx", 'x', input);
        }

        /// Registers a recipe that takes 6 of the input object, in a staircase shape on a 3x3 crafting grid, and outputs 4 of the output block.
        public void addStairRecipe(Priority priority, Block output, int outputMeta, Object input) {
            addShapedRecipe(priority, new ItemStack(output, 4, outputMeta), "x  ", "xx ", "xxx", 'x', input);
        }
    }

    // Recipes need a different class to be sorted differently in the recipe sorter

    /// These get registered AFTER other shaped recipes in vanilla and most mods, allowing those recipes to take priority if they overlap.
    public static class LowPriorityShapedRecipe extends ShapedOreRecipe {
        public LowPriorityShapedRecipe(ItemStack result, Object... recipe) {
            super(result, recipe);
        }
    }

    /// These get registered BEFORE other shaped recipes in vanilla and most mods, taking priority over them if they overlap.
    public static class HighPriorityShapedRecipe extends ShapedOreRecipe {
        public HighPriorityShapedRecipe(ItemStack result, Object... recipe) {
            super(result, recipe);
        }
    }

    /// These get registered AFTER other shapeless recipes in vanilla and most mods, allowing those recipes to take priority if they overlap.
    public static class LowPriorityShapelessRecipe extends ShapelessOreRecipe {
        public LowPriorityShapelessRecipe(ItemStack result, Object... recipe) {
            super(result, recipe);
        }
    }

    /// These get registered BEFORE other shapeless recipes in vanilla and most mods, taking priority over them if they overlap.
    public static class HighPriorityShapelessRecipe extends ShapelessOreRecipe {
        public HighPriorityShapelessRecipe(ItemStack result, Object... recipe) {
            super(result, recipe);
        }
    }

    public enum Priority {
        /// Registers recipes BEFORE other shaped recipes in vanilla and most mods, taking priority over them if they overlap.
        HIGH,
        /// Registers recipes without a priority, being thrown with other recipes in no particular order.
        /// Mods that ask to have high or lower priority will be placed below and above this recipe, respectively.
        NORMAL,
        /// These get registered AFTER other shaped recipes in vanilla and most mods, allowing those recipes to take priority if they overlap.
        LOW;

        private void addShapelessRecipe(ItemStack output, Object... objects) {
            if (validateItems(output) && validateItems(objects)) {
                switch (this) {
                    case LOW -> GameRegistry.addRecipe(new LowPriorityShapelessRecipe(output, objects));
                    case NORMAL -> GameRegistry.addShapelessRecipe(output, objects);
                    case HIGH -> GameRegistry.addRecipe(new HighPriorityShapelessRecipe(output, objects));
                }
            }
        }

        private void addShapedRecipe(ItemStack output, Object... objects) {
            if (validateItems(output) && validateItems(objects)) {
                switch (this) {
                    case LOW -> GameRegistry.addRecipe(new LowPriorityShapedRecipe(output, objects));
                    case NORMAL -> GameRegistry.addShapedRecipe(output, objects);
                    case HIGH -> GameRegistry.addRecipe(new HighPriorityShapedRecipe(output, objects));
                }
            }
        }
    }
}
