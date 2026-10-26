package ganymedes01.etfuturum;

import java.util.*;
import net.minecraft.item.*;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraftforge.oredict.OreDictionary;

/** Native smoker/blast recipe ownership, including explicit overrides and removal blacklists. */
public final class CookingRecipes {
    public static final CookingRecipes SMOKER = new CookingRecipes(false), BLAST = new CookingRecipes(true);
    private final boolean blast, automatic;
    private final Map<Key, ItemStack> explicit = new HashMap<>(), cache = new HashMap<>();
    private final Map<Key, Float> experience = new HashMap<>();
    private final Set<Key> removed = new HashSet<>();
    private CookingRecipes(boolean blast) {
        this.blast = blast;
        automatic = Features.enabled("functions." + (blast ? "enableAutoAddBlastFurnace" : "enableAutoAddSmoker"));
    }
    static void initialize() { }
    public ItemStack getSmeltingResult(ItemStack input) {
        if (input.isEmpty() || lookup(removed, input)) return ItemStack.EMPTY;
        ItemStack result = lookup(explicit, input);
        if (result != null) return result.copy();
        Key key = new Key(input);
        result = cache.get(key);
        if (result == null) {
            ItemStack furnace = FurnaceRecipes.instance().getSmeltingResult(input);
            result = automatic && accepts(input, furnace) ? furnace.copy() : ItemStack.EMPTY;
            cache.put(key, result);
        }
        return result.copy();
    }
    public void addRecipe(ItemStack input, ItemStack output, float xp) {
        if (input.isEmpty() || output.isEmpty() || !Float.isFinite(xp) || xp < 0) throw new IllegalArgumentException("Invalid cooking recipe");
        explicit.put(new Key(input), output.copy()); experience.put(new Key(output), xp); removed.remove(new Key(input)); clearLists();
    }
    public void removeRecipe(ItemStack input) { ItemStack output = explicit.remove(new Key(input)); if (output != null) experience.remove(new Key(output)); removed.add(new Key(input)); clearLists(); }
    public void clearLists() { cache.clear(); }
    public float getSmeltingExperience(ItemStack output) {
        float item = output.getItem().getSmeltingExperience(output); if (item >= 0) return item;
        Float custom = lookup(experience, output);
        return custom == null ? FurnaceRecipes.instance().getSmeltingExperience(output) : custom;
    }
    private boolean accepts(ItemStack input, ItemStack output) {
        if (output.isEmpty()) return false;
        if (!blast) return output.getItem() instanceof ItemFood && ((ItemFood) output.getItem()).getHealAmount(output) > 0;
        for (int id : OreDictionary.getOreIDs(input)) {
            String name = OreDictionary.getOreName(id);
            if (name.startsWith("ore") || name.matches("^raw[A-Z].*") || name.startsWith("cluster") || name.startsWith("crushed")) return true;
        }
        for (int id : OreDictionary.getOreIDs(output)) {
            String name = OreDictionary.getOreName(id);
            if (name.startsWith("ingot") || name.startsWith("nugget") || name.startsWith("chunkGt") || name.equalsIgnoreCase("quicksilver")) return true;
        }
        return false;
    }
    private static <T> T lookup(Map<Key, T> map, ItemStack stack) {
        T result = map.get(new Key(stack)); return result == null ? map.get(new Key(stack.getItem(), OreDictionary.WILDCARD_VALUE)) : result;
    }
    private static boolean lookup(Set<Key> set, ItemStack stack) { return set.contains(new Key(stack)) || set.contains(new Key(stack.getItem(), OreDictionary.WILDCARD_VALUE)); }
    private static final class Key {
        final Item item; final int meta;
        Key(ItemStack stack) { this(stack.getItem(), stack.getMetadata()); }
        Key(Item item, int meta) { this.item = item; this.meta = meta; }
        @Override public int hashCode() { return System.identityHashCode(item) * 31 + meta; }
        @Override public boolean equals(Object other) { return other instanceof Key && ((Key) other).item == item && ((Key) other).meta == meta; }
    }
}
