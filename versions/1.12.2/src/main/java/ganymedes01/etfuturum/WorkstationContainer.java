package ganymedes01.etfuturum;

import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.entity.player.*;
import net.minecraft.inventory.*;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/** Transient native inputs; every output path commits a complete server-owned recipe. */
public abstract class WorkstationContainer extends Container {
    public final InventoryCrafting inputs;
    protected final InventoryCraftResult output = new InventoryCraftResult();
    protected final World world;
    private final Block block;
    private final BlockPos pos;
    public final int inputCount;
    public int selected;
    private int lastSelected = -1;
    private boolean consuming;
    protected WorkstationContainer(InventoryPlayer inventory, World world, BlockPos pos, Block block, int count) {
        this.world = world; this.pos = pos; this.block = block; inputCount = count;
        inputs = new InventoryCrafting(this, count, 1);
        for (int i = 0; i < count; i++) {
            final int input = i;
            addSlotToContainer(new Slot(inputs, i, 16 + i * 24, 112) { @Override public boolean isItemValid(ItemStack stack) { return accepts(input, stack); } });
        }
        addSlotToContainer(new Slot(output, 0, 166, 112) {
            @Override public boolean isItemValid(ItemStack stack) { return false; }
            @Override public boolean canTakeStack(EntityPlayer player) { return canInteractWith(player); }
        });
        for (int row = 0; row < 3; row++) for (int col = 0; col < 9; col++) addSlotToContainer(new Slot(inventory, col + row * 9 + 9, 16 + col * 18, 146 + row * 18));
        for (int col = 0; col < 9; col++) addSlotToContainer(new Slot(inventory, col, 16 + col * 18, 204));
    }
    protected abstract boolean accepts(int slot, ItemStack stack);
    protected abstract void refresh();
    protected abstract ItemStack craft(EntityPlayer player);
    public abstract String title();
    public abstract List<String> choices();
    @Override public void onCraftMatrixChanged(IInventory inventory) { if (!consuming) refresh(); }
    @Override public boolean canInteractWith(EntityPlayer player) { return player.world == world && !player.isDead && world.isBlockLoaded(pos) && world.getBlockState(pos).getBlock() == block && player.getDistanceSq(pos.getX()+.5, pos.getY()+.5, pos.getZ()+.5) <= 64; }
    @Override public void onContainerClosed(EntityPlayer player) { super.onContainerClosed(player); if (!world.isRemote) clearContainer(player, world, inputs); }
    @Override public boolean canMergeSlot(ItemStack stack, Slot slot) { return slot.inventory != output; }
    @Override public boolean canDragIntoSlot(Slot slot) { return slot.inventory != output; }
    @Override public boolean enchantItem(EntityPlayer player, int choice) {
        if (!canInteractWith(player) || choice < 0 || choice >= choices().size()) return false;
        selected = choice; refresh(); return true;
    }
    @Override public void addListener(IContainerListener listener) { super.addListener(listener); listener.sendWindowProperty(this, 0, selected); }
    @Override public void detectAndSendChanges() {
        super.detectAndSendChanges();
        if (selected != lastSelected) { for (IContainerListener listener : listeners) listener.sendWindowProperty(this, 0, selected); lastSelected = selected; }
    }
    @Override public void updateProgressBar(int id, int value) { if (id == 0) { selected = value; refresh(); } }
    @Override public ItemStack slotClick(int slot, int button, ClickType type, EntityPlayer player) {
        if (!canInteractWith(player) || slot >= inventorySlots.size() || slot < 0 && slot != -999 && slot != -1) return ItemStack.EMPTY;
        if (slot != inputCount) return super.slotClick(slot, button, type, player);
        if (world.isRemote) return ItemStack.EMPTY;
        if (type == ClickType.QUICK_MOVE) { for (int i = 0; i < 64 && moveResult(player); i++) { } }
        else if (type == ClickType.PICKUP && (button == 0 || button == 1)) {
            ItemStack preview = output.getStackInSlot(0), carried = player.inventory.getItemStack();
            if (!preview.isEmpty() && (carried.isEmpty() || same(carried, preview) && carried.getCount()+preview.getCount() <= carried.getMaxStackSize())) {
                ItemStack result = take(player);
                if (carried.isEmpty()) player.inventory.setItemStack(result); else carried.grow(result.getCount());
            }
        } else if (type == ClickType.SWAP && button >= 0 && button < 9) {
            ItemStack preview = output.getStackInSlot(0), hotbar = player.inventory.getStackInSlot(button);
            if (!preview.isEmpty() && (hotbar.isEmpty() || same(hotbar, preview) && hotbar.getCount()+preview.getCount() <= hotbar.getMaxStackSize())) {
                ItemStack result = take(player);
                if (hotbar.isEmpty()) player.inventory.setInventorySlotContents(button, result); else hotbar.grow(result.getCount());
            }
        } else if (type == ClickType.THROW && !output.getStackInSlot(0).isEmpty()) player.dropItem(take(player), false);
        detectAndSendChanges(); return ItemStack.EMPTY;
    }
    private ItemStack take(EntityPlayer player) {
        consuming = true; ItemStack result;
        try { result = craft(player); } finally { consuming = false; }
        refresh(); return result;
    }
    private boolean moveResult(EntityPlayer player) {
        ItemStack preview = output.getStackInSlot(0); if (preview.isEmpty()) return false;
        int capacity = 0;
        for (int i = inputCount+1; i < inventorySlots.size(); i++) {
            Slot slot = inventorySlots.get(i); ItemStack stack = slot.getStack();
            if (stack.isEmpty()) capacity += Math.min(slot.getSlotStackLimit(), preview.getMaxStackSize());
            else if (same(stack, preview)) capacity += Math.max(0, Math.min(slot.getSlotStackLimit(), stack.getMaxStackSize())-stack.getCount());
        }
        if (capacity < preview.getCount()) return false;
        ItemStack result = take(player); mergeItemStack(result, inputCount+1, inventorySlots.size(), true); return true;
    }
    @Override public ItemStack transferStackInSlot(EntityPlayer player, int index) {
        if (!canInteractWith(player) || index < 0 || index >= inventorySlots.size()) return ItemStack.EMPTY;
        if (index == inputCount) { if (!world.isRemote) moveResult(player); return ItemStack.EMPTY; }
        Slot slot = inventorySlots.get(index); if (!slot.getHasStack()) return ItemStack.EMPTY;
        ItemStack stack = slot.getStack(), original = stack.copy(); boolean merged = false;
        if (index < inputCount) merged = mergeItemStack(stack, inputCount+1, inventorySlots.size(), true);
        else {
            for (int i = 0; i < inputCount && !merged; i++) if (accepts(i, stack)) merged = mergeItemStack(stack, i, i+1, false);
            if (!merged) { int hotbar = inputCount+28; merged = index < hotbar ? mergeItemStack(stack, hotbar, inventorySlots.size(), false) : mergeItemStack(stack, inputCount+1, hotbar, false); }
        }
        if (!merged) return ItemStack.EMPTY;
        if (stack.isEmpty()) slot.putStack(ItemStack.EMPTY); else slot.onSlotChanged(); return original;
    }
    protected static boolean same(ItemStack a, ItemStack b) { return ItemStack.areItemsEqual(a, b) && ItemStack.areItemStackTagsEqual(a, b); }
}
