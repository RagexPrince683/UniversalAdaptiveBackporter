package ganymedes01.etfuturum.inventory;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ICrafting;
import net.minecraft.world.World;
import java.util.List;

public abstract class ContainerSelectableWorkstation extends ContainerWorkstation {
	public int selected;
	private int lastSelected = -1;
	protected ContainerSelectableWorkstation(InventoryPlayer inventory, World world, int x, int y, int z, Block block, int count) {
		super(inventory, world, x, y, z, block, count);
	}
	public abstract List<String> choices();
	@Override public boolean enchantItem(EntityPlayer player, int choice) {
		if (!canInteractWith(player) || choice < 0 || choice >= choices().size()) return false;
		selected = choice;
		refresh();
		return true;
	}
	@Override public void addCraftingToCrafters(ICrafting crafter) {
		super.addCraftingToCrafters(crafter);
		crafter.sendProgressBarUpdate(this, 0, selected);
	}
	@Override public void detectAndSendChanges() {
		super.detectAndSendChanges();
		if (selected != lastSelected) {
			for (Object crafter : crafters) ((ICrafting) crafter).sendProgressBarUpdate(this, 0, selected);
			lastSelected = selected;
		}
	}
	@Override public void updateProgressBar(int id, int value) {
		if (id == 0) { selected = value; refresh(); }
	}
}
