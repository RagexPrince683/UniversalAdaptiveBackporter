package ganymedes01.etfuturum.compat.cthandlers;

import ganymedes01.etfuturum.api.BrewingFuelRegistry;
import ganymedes01.etfuturum.compat.CompatCraftTweaker;
import minetweaker.IUndoableAction;
import minetweaker.MineTweakerAPI;
import minetweaker.api.item.IIngredient;
import minetweaker.api.item.IItemStack;
import net.minecraft.item.ItemStack;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

import java.util.List;


@ZenClass("mods.etfuturum.brewingFuel")
public class CTBrewingFuels {

	@ZenMethod
	public static void remove(IIngredient fuel) {

		if (fuel == null || fuel.getItems() == null) {
			MineTweakerAPI.logError("Cannot resolve brewing fuel " + fuel);
			return;
		}
		MineTweakerAPI.apply(new RemoveAction(CompatCraftTweaker.getItemStacks(fuel.getItems())));
	}

	@ZenMethod
	public static void addFuel(IIngredient fuel, int count) {
		List<IItemStack> items = fuel == null ? null : fuel.getItems();
		if (items == null) {
			MineTweakerAPI.logError("Cannot turn " + fuel + " into a brewing fuel");
			return;
		}
		if (count <= 0) {
			MineTweakerAPI.logError("Brewing fuel must supply at least one cycle");
			return;
		}

		final ItemStack[] toAdd = CompatCraftTweaker.getItemStacks(items);
		MineTweakerAPI.apply(new AddAction(fuel, toAdd, count));
	}

	// ######################
	// ### Action classes ###
	// ######################

	private static class RemoveAction implements IUndoableAction {

		private final ItemStack[] items;
		private BrewingFuelRegistry.State previous;

		public RemoveAction(ItemStack[] items) {
			this.items = items;
		}

		@Override
		public void apply() {
			previous = BrewingFuelRegistry.snapshot();
			for (ItemStack item : items) {
				BrewingFuelRegistry.remove(item);
			}
		}

		@Override
		public boolean canUndo() {
			return true;
		}

		@Override
		public void undo() {
			BrewingFuelRegistry.restore(previous);
		}

		@Override
		public String describe() {
			return "Removing " + items.length + " brewing fuels";
		}

		@Override
		public String describeUndo() {
			return "Restoring " + items.length + " brewing fuels";
		}

		@Override
		public Object getOverrideKey() {
			return null;
		}
	}

	private static class AddAction implements IUndoableAction {

		private final IIngredient ingredient;
		private final ItemStack[] fuels;
		private final int count;
		private BrewingFuelRegistry.State previous;

		public AddAction(IIngredient ingredient, ItemStack[] fuels, int count) {
			this.ingredient = ingredient;
			this.fuels = fuels;
			this.count = count;
		}

		@Override
		public void apply() {
			previous = BrewingFuelRegistry.snapshot();
			for (ItemStack inputStack : fuels) {
				BrewingFuelRegistry.registerFuel(inputStack, count);
			}
		}

		@Override
		public boolean canUndo() {
			return true;
		}

		@Override
		public void undo() {
			BrewingFuelRegistry.restore(previous);
		}

		@Override
		public String describe() {
			return "Adding brewing fuel for " + ingredient;
		}

		@Override
		public String describeUndo() {
			return "Removing brewing fuel for " + ingredient;
		}

		@Override
		public Object getOverrideKey() {
			return null;
		}
	}
}
