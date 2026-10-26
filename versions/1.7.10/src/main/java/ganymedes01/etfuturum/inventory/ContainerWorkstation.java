package ganymedes01.etfuturum.inventory;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.*;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

/** Transient, block-bound workstations. Output extraction is one atomic recipe operation. */
public abstract class ContainerWorkstation extends Container {

	public final InventoryCrafting inputs;
	protected final InventoryCraftResult output = new InventoryCraftResult();
	protected final World world;
	private final Block block;
	private final int x, y, z;
	public final int inputCount;
	protected boolean consuming;

	protected ContainerWorkstation(InventoryPlayer inventory, World world, int x, int y, int z, Block block, int count) {
		this.world = world;
		this.x = x;
		this.y = y;
		this.z = z;
		this.block = block;
		inputCount = count;
		inputs = new InventoryCrafting(this, count, 1);
		for (int i = 0; i < count; i++) {
			final int input = i;
			addSlotToContainer(new Slot(inputs, i, 16 + i * 24, 112) {
				@Override public boolean isItemValid(ItemStack stack) { return accepts(input, stack); }
			});
		}
		addSlotToContainer(new Slot(output, 0, 166, 112) {
			@Override public boolean isItemValid(ItemStack stack) { return false; }
			@Override public boolean canTakeStack(EntityPlayer player) { return canInteractWith(player); }
		});
		for (int row = 0; row < 3; row++) for (int col = 0; col < 9; col++)
			addSlotToContainer(new Slot(inventory, col + row * 9 + 9, 16 + col * 18, 146 + row * 18));
		for (int col = 0; col < 9; col++) addSlotToContainer(new Slot(inventory, col, 16 + col * 18, 204));
	}

	protected abstract boolean accepts(int slot, ItemStack stack);
	protected abstract void refresh();
	protected abstract ItemStack craft(EntityPlayer player);
	public abstract String title();

	@Override public void onCraftMatrixChanged(IInventory inventory) { if (!consuming) refresh(); }

	@Override public boolean canInteractWith(EntityPlayer player) {
		return player.worldObj == world && !player.isDead && world.blockExists(x, y, z)
				&& world.getBlock(x, y, z) == block && player.getDistanceSq(x + .5, y + .5, z + .5) <= 64;
	}

	@Override public void onContainerClosed(EntityPlayer player) {
		super.onContainerClosed(player);
		if (!world.isRemote) for (int i = 0; i < inputCount; i++) {
			ItemStack stack = inputs.getStackInSlotOnClosing(i);
			if (stack != null) player.dropPlayerItemWithRandomChoice(stack, false);
		}
	}

	@Override public boolean func_94530_a(ItemStack stack, Slot slot) { return slot.inventory != output; }

	@Override public ItemStack slotClick(int slot, int button, int mode, EntityPlayer player) {
		if (!canInteractWith(player)) return null;
		if (slot >= inventorySlots.size() || slot < 0 && slot != -999 && slot != -1) return null;
		if (slot != inputCount) return super.slotClick(slot, button, mode, player);
		// Never split a recipe result or let hotbar/drop/collect paths bypass ingredient consumption.
		if (world.isRemote) return null;
		if (mode == 1) {
			for (int i = 0; i < 64 && moveResult(player); i++) { /* bounded shift crafting */ }
		} else if (mode == 0 && (button == 0 || button == 1)) {
			ItemStack preview = output.getStackInSlot(0), carried = player.inventory.getItemStack();
			if (preview != null && (carried == null || same(carried, preview)
					&& carried.stackSize + preview.stackSize <= carried.getMaxStackSize())) {
				ItemStack result = take(player);
				if (carried == null) player.inventory.setItemStack(result);
				else carried.stackSize += result.stackSize;
			}
		} else if (mode == 2 && button >= 0 && button < 9) {
			ItemStack preview = output.getStackInSlot(0), hotbar = player.inventory.getStackInSlot(button);
			if (preview != null && (hotbar == null || same(hotbar, preview)
					&& hotbar.stackSize + preview.stackSize <= hotbar.getMaxStackSize())) {
				ItemStack result = take(player);
				if (hotbar == null) player.inventory.setInventorySlotContents(button, result);
				else hotbar.stackSize += result.stackSize;
			}
		} else if (mode == 4 && output.getStackInSlot(0) != null) player.dropPlayerItemWithRandomChoice(take(player), false);
		detectAndSendChanges();
		return null;
	}

	private ItemStack take(EntityPlayer player) {
		consuming = true;
		ItemStack result;
		try { result = craft(player); } finally { consuming = false; }
		refresh();
		return result;
	}

	private boolean moveResult(EntityPlayer player) {
		ItemStack preview = output.getStackInSlot(0);
		if (preview == null) return false;
		int capacity = 0;
		for (int i = inputCount + 1; i < inventorySlots.size(); i++) {
			Slot slot = (Slot) inventorySlots.get(i);
			ItemStack stack = slot.getStack();
			if (stack == null) capacity += Math.min(slot.getSlotStackLimit(), preview.getMaxStackSize());
			else if (same(stack, preview)) capacity += Math.max(0, Math.min(slot.getSlotStackLimit(), stack.getMaxStackSize()) - stack.stackSize);
		}
		if (capacity < preview.stackSize) return false;
		ItemStack result = take(player);
		mergeItemStack(result, inputCount + 1, inventorySlots.size(), true);
		return true;
	}

	@Override public ItemStack transferStackInSlot(EntityPlayer player, int index) {
		if (!canInteractWith(player) || index < 0 || index >= inventorySlots.size()) return null;
		if (index == inputCount) { if (!world.isRemote) moveResult(player); return null; }
		Slot slot = (Slot) inventorySlots.get(index);
		if (!slot.getHasStack()) return null;
		ItemStack stack = slot.getStack(), original = stack.copy();
		boolean merged = false;
		if (index < inputCount) merged = mergeItemStack(stack, inputCount + 1, inventorySlots.size(), true);
		else {
			for (int i = 0; i < inputCount && !merged; i++) if (accepts(i, stack)) merged = mergeItemStack(stack, i, i + 1, false);
			if (!merged) {
				int hotbar = inputCount + 28;
				merged = index < hotbar ? mergeItemStack(stack, hotbar, inventorySlots.size(), false)
						: mergeItemStack(stack, inputCount + 1, hotbar, false);
			}
		}
		if (!merged) return null;
		if (stack.stackSize == 0) slot.putStack(null); else slot.onSlotChanged();
		return original;
	}

	protected static boolean same(ItemStack a, ItemStack b) {
		return a.isItemEqual(b) && ItemStack.areItemStackTagsEqual(a, b);
	}
}
