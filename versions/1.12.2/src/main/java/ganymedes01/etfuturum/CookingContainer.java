package ganymedes01.etfuturum;

import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.player.*;
import net.minecraft.inventory.*;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraftforge.fml.common.FMLCommonHandler;

public final class CookingContainer extends Container {
    public final CookingTile tile;
    private final int[] last = new int[6];
    public CookingContainer(InventoryPlayer inventory, CookingTile tile) {
        this.tile = tile;
        addSlotToContainer(new Slot(tile, 0, 56, 17));
        addSlotToContainer(new Slot(tile, 1, 56, 53) { @Override public boolean isItemValid(ItemStack stack) { return tile.isItemValidForSlot(1, stack); } });
        addSlotToContainer(new Output(inventory.player, tile));
        for (int row = 0; row < 3; row++) for (int col = 0; col < 9; col++) addSlotToContainer(new Slot(inventory, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
        for (int col = 0; col < 9; col++) addSlotToContainer(new Slot(inventory, col, 8 + col * 18, 142));
    }
    @Override public void addListener(IContainerListener listener) { super.addListener(listener); listener.sendAllWindowProperties(this, tile); }
    @Override public void detectAndSendChanges() {
        super.detectAndSendChanges();
        for (int id = 0; id < last.length; id++) {
            int value = tile.getField(id);
            if (last[id] != value) for (IContainerListener listener : listeners) listener.sendWindowProperty(this, id, value);
            last[id] = value;
        }
    }
    @Override public void updateProgressBar(int id, int value) { tile.setField(id, value); }
    @Override public boolean canInteractWith(EntityPlayer player) { return tile.isUsableByPlayer(player); }
    @Override public ItemStack transferStackInSlot(EntityPlayer player, int index) {
        if (index < 0 || index >= inventorySlots.size()) return ItemStack.EMPTY;
        Slot slot = inventorySlots.get(index); if (!slot.getHasStack()) return ItemStack.EMPTY;
        ItemStack held = slot.getStack(), original = held.copy();
        if (index == 2) { if (!mergeItemStack(held, 3, 39, true)) return ItemStack.EMPTY; slot.onSlotChange(held, original); }
        else if (index >= 3) {
            if (!tile.recipes().getSmeltingResult(held).isEmpty()) { if (!mergeItemStack(held, 0, 1, false)) return ItemStack.EMPTY; }
            else if (TileEntityFurnace.getItemBurnTime(held) > 1) { if (!mergeItemStack(held, 1, 2, false)) return ItemStack.EMPTY; }
            else if (index < 30) { if (!mergeItemStack(held, 30, 39, false)) return ItemStack.EMPTY; }
            else if (!mergeItemStack(held, 3, 30, false)) return ItemStack.EMPTY;
        } else if (!mergeItemStack(held, 3, 39, false)) return ItemStack.EMPTY;
        if (held.isEmpty()) slot.putStack(ItemStack.EMPTY); else slot.onSlotChanged();
        if (held.getCount() == original.getCount()) return ItemStack.EMPTY;
        slot.onTake(player, held); return original;
    }
    private static final class Output extends Slot {
        private final EntityPlayer player; private final CookingTile tile; private int removed;
        Output(EntityPlayer player, CookingTile tile) { super(tile, 2, 116, 35); this.player = player; this.tile = tile; }
        @Override public boolean isItemValid(ItemStack stack) { return false; }
        @Override public ItemStack decrStackSize(int amount) { if (getHasStack()) removed += Math.min(amount, getStack().getCount()); return super.decrStackSize(amount); }
        @Override protected void onCrafting(ItemStack stack, int amount) { removed += amount; onCrafting(stack); }
        @Override public ItemStack onTake(EntityPlayer player, ItemStack stack) { onCrafting(stack); return super.onTake(player, stack); }
        @Override protected void onCrafting(ItemStack stack) {
            if (removed <= 0) return;
            stack.onCrafting(player.world, player, removed);
            if (!player.world.isRemote) {
                float xp = removed * tile.recipes().getSmeltingExperience(stack); int count = (int) Math.floor(xp);
                if (player.world.rand.nextFloat() < xp - count) count++;
                while (count > 0) { int split = EntityXPOrb.getXPSplit(count); count -= split; player.world.spawnEntity(new EntityXPOrb(player.world, player.posX, player.posY + .5, player.posZ + .5, split)); }
                FMLCommonHandler.instance().firePlayerSmeltedEvent(player, stack);
            }
            removed = 0;
        }
    }
}
