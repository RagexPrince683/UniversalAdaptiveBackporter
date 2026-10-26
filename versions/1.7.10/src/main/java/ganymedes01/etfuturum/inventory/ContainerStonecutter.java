package ganymedes01.etfuturum.inventory;

import ganymedes01.etfuturum.ModBlocks;
import ganymedes01.etfuturum.recipes.StonecuttingRecipes;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import java.util.*;

public class ContainerStonecutter extends ContainerSelectableWorkstation {
	private List<ItemStack> recipes = Collections.emptyList();
	private ItemStack previous;
	public ContainerStonecutter(InventoryPlayer inventory, World world, int x, int y, int z) {
		super(inventory, world, x, y, z, ModBlocks.STONECUTTER.get(), 1);
	}
	@Override protected boolean accepts(int slot, ItemStack stack) { return !StonecuttingRecipes.results(stack).isEmpty(); }
	@Override protected void refresh() {
		ItemStack input = inputs.getStackInSlot(0);
		if (input == null || previous == null || !same(input, previous)) selected = 0;
		previous = input == null ? null : input.copy();
		recipes = StonecuttingRecipes.results(input);
		if (selected >= recipes.size()) selected = 0;
		output.setInventorySlotContents(0, recipes.isEmpty() ? null : recipes.get(selected).copy());
	}
	@Override public List<String> choices() {
		List<String> names = new ArrayList<>();
		for (ItemStack result : recipes) names.add(result.stackSize + " x " + result.getDisplayName());
		return names;
	}
	@Override protected ItemStack craft(EntityPlayer player) {
		ItemStack result = output.getStackInSlot(0).copy();
		inputs.decrStackSize(0, 1);
		return result;
	}
	@Override public String title() { return "tile.etfuturum.stonecutter.name"; }
}
