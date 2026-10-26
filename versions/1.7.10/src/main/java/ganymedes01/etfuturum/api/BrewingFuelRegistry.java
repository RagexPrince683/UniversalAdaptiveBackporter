package ganymedes01.etfuturum.api;

import ganymedes01.etfuturum.api.tags.helpers.ItemTags;
import ganymedes01.etfuturum.api.blocksanditems.utils.ItemMetaPair;
import ganymedes01.etfuturum.core.utils.ItemStackMap;
import ganymedes01.etfuturum.core.utils.Logger;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;
import java.util.*;

/** Count overrides and exclusions for the authoritative brewing-fuel tag. */
public class BrewingFuelRegistry {
	private static final String TAG = "minecraft:brewing_fuel";
	private static final Map<Item, Map<Integer, Integer>> counts = new HashMap<>();
	private static final Set<ItemMetaPair> addedTags = new HashSet<>();

	public static void registerFuel(Object itemObj, int count) {
		if (count <= 0) throw new IllegalArgumentException("Brewing fuel must supply at least one cycle");
		if (itemObj instanceof String ore) {
			for (ItemStack stack : OreDictionary.getOres(ore)) registerFuel(stack, count);
			return;
		}
		ItemStack stack;
		if (itemObj instanceof ItemStack input) stack = input;
		else if (itemObj instanceof Item item) stack = new ItemStack(item, 1, OreDictionary.WILDCARD_VALUE);
		else if (itemObj instanceof Block block && Item.getItemFromBlock(block) != null)
			stack = new ItemStack(block, 1, OreDictionary.WILDCARD_VALUE);
		else throw new IllegalArgumentException("Invalid brewing fuel: " + itemObj);
		if (stack.getItem() == null || stack.stackSize <= 0 || stack.getItemDamage() < 0)
			throw new IllegalArgumentException("Invalid brewing fuel stack: " + stack);
		ItemMetaPair pair = ItemMetaPair.intern(stack.getItem(), stack.getItemDamage());
		if (!ItemTags.getInTag(TAG).contains(pair)) {
			ItemTags.addTags(stack.getItem(), stack.getItemDamage(), TAG);
			addedTags.add(pair);
		}
		counts.computeIfAbsent(stack.getItem(), item -> new HashMap<>()).put(stack.getItemDamage(), count);
	}

	public static void registerFuel(List<Object> list, int count) {
		if (list == null) throw new IllegalArgumentException("Null brewing fuel list");
		list.forEach(o -> registerFuel(o, count));
	}

	public static void remove(String ore) {
		if (ore != null) for (ItemStack stack : OreDictionary.getOres(ore)) remove(stack);
	}

	public static void remove(ItemStack stack) {
		if (stack == null || stack.getItem() == null) return;
		Map<Integer, Integer> values = counts.computeIfAbsent(stack.getItem(), item -> new HashMap<>());
		if (stack.getItemDamage() == OreDictionary.WILDCARD_VALUE) values.clear();
		// An exact exclusion must also mask an inherited wildcard tag.
		values.put(stack.getItemDamage(), 0);
	}

	public static boolean isFuel(String ore) {
		if (ore != null) for (ItemStack stack : OreDictionary.getOres(ore)) if (isFuel(stack)) return true;
		return false;
	}

	public static boolean isFuel(ItemStack stack) { return getBrewingAmount(stack) > 0; }

	public static int getBrewingAmount(ItemStack stack) {
		if (stack == null || stack.getItem() == null || stack.stackSize <= 0 || stack.getItemDamage() < 0) return 0;
		if (!ItemTags.hasTag(stack.getItem(), stack.getItemDamage(), TAG)) return 0;
		Map<Integer, Integer> values = counts.get(stack.getItem());
		if (values == null) return 20;
		return values.getOrDefault(stack.getItemDamage(), values.getOrDefault(OreDictionary.WILDCARD_VALUE, 20));
	}

	/** A detached view. Zero-count exact entries represent exclusions from wildcard fuels. */
	public static ItemStackMap<Integer> getFuels() {
		ItemStackMap<Integer> fuels = new ItemStackMap<Integer>() {
			@Override public Integer get(Object key) {
				// The general-purpose map prioritizes wildcards; fuels prioritize exact overrides.
				if (key instanceof ItemStack stack && stack.getItem() != null) {
					for (Map.Entry<ItemStack, Integer> entry : entrySet()) {
						ItemStack fuel = entry.getKey();
						if (fuel.getItem() == stack.getItem() && fuel.getItemDamage() == stack.getItemDamage()) return entry.getValue();
					}
				}
				return super.get(key);
			}
		};
		for (ItemMetaPair pair : ItemTags.getInTag(TAG)) {
			ItemStack stack = new ItemStack(pair.get(), 1, pair.getMeta());
			int amount = getBrewingAmount(stack);
			if (amount > 0) fuels.put(stack, amount);
		}
		counts.forEach((item, values) -> values.forEach((meta, count) -> {
			ItemStack stack = new ItemStack(item, 1, meta);
			if (count == 0 || getBrewingAmount(stack) > 0) fuels.put(stack, getBrewingAmount(stack));
		}));
		return fuels;
	}

	public void printFuels() {
		getFuels().forEach((stack, count) -> Logger.info(stack + ": " + count + " brewing cycles"));
	}

	/** Script actions restore overrides, exclusions and only the tags added by this API. */
	public static final class State {
		private final Map<Item, Map<Integer, Integer>> values = new HashMap<>();
		private final Set<ItemMetaPair> tags = new HashSet<>(addedTags);
		private State() { counts.forEach((item, map) -> values.put(item, new HashMap<>(map))); }
	}

	public static State snapshot() { return new State(); }

	public static void restore(State state) {
		for (ItemMetaPair pair : new HashSet<>(addedTags)) {
			if (!state.tags.contains(pair) && ItemTags.getInTag(TAG).contains(pair))
				ItemTags.removeTags(pair.get(), pair.getMeta(), TAG);
		}
		for (ItemMetaPair pair : state.tags) ItemTags.addTags(pair.get(), pair.getMeta(), TAG);
		addedTags.clear();
		addedTags.addAll(state.tags);
		counts.clear();
		state.values.forEach((item, map) -> counts.put(item, new HashMap<>(map)));
	}
}
