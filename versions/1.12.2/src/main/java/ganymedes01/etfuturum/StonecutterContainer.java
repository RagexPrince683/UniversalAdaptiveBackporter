package ganymedes01.etfuturum;

import java.util.*;
import net.minecraft.entity.player.*;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public final class StonecutterContainer extends WorkstationContainer {
    private List<ItemStack> recipes = Collections.emptyList();
    private ItemStack previous = ItemStack.EMPTY;
    public StonecutterContainer(InventoryPlayer inventory, World world, BlockPos pos) { super(inventory, world, pos, Content.block("stonecutter"), 1); }
    @Override protected boolean accepts(int slot, ItemStack stack) { return !StonecuttingRecipes.results(stack).isEmpty(); }
    @Override protected void refresh() {
        ItemStack input = inputs.getStackInSlot(0);
        if (input.isEmpty() || previous.isEmpty() || !same(input, previous)) selected = 0;
        previous = input.copy(); recipes = StonecuttingRecipes.results(input);
        if (selected >= recipes.size()) selected = 0;
        output.setInventorySlotContents(0, recipes.isEmpty() ? ItemStack.EMPTY : recipes.get(selected).copy());
    }
    @Override public List<String> choices() { List<String> names = new ArrayList<>(); for (ItemStack result : recipes) names.add(result.getCount()+" x "+result.getDisplayName()); return names; }
    @Override protected ItemStack craft(EntityPlayer player) { ItemStack result = output.getStackInSlot(0).copy(); inputs.decrStackSize(0, 1); return result; }
    @Override public String title() { return "tile.etfuturum.stonecutter.name"; }
}
