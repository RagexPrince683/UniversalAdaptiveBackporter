package ganymedes01.etfuturum;

import net.minecraft.entity.player.*;
import net.minecraft.inventory.*;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.*;
import net.minecraft.util.*;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.*;
import net.minecraftforge.items.wrapper.*;

/** Server-owned cooking state; native inventories, sided automation and window properties. */
public abstract class CookingTile extends TileEntityLockableLoot implements ISidedInventory, ITickable {
    @Override public boolean shouldRefresh(net.minecraft.world.World world, net.minecraft.util.math.BlockPos pos, net.minecraft.block.state.IBlockState before, net.minecraft.block.state.IBlockState after) { return before.getBlock() != after.getBlock(); }
    private static final int[] TOP = {0}, SIDE = {1}, BOTTOM = {2, 1};
    private NonNullList<ItemStack> items = NonNullList.withSize(3, ItemStack.EMPTY);
    private final boolean blast;
    private int burnTime, fuelTime, cookTime;
    private final IItemHandler[] handlers = new IItemHandler[6];
    private final IItemHandler inventory = new InvWrapper(this);
    protected CookingTile(boolean blast) { this.blast = blast; }
    public static final class Smoker extends CookingTile { public Smoker() { super(false); } }
    public static final class BlastFurnace extends CookingTile { public BlastFurnace() { super(true); } }
    public CookingRecipes recipes() { return blast ? CookingRecipes.BLAST : CookingRecipes.SMOKER; }
    public String kind() { return blast ? "blast_furnace" : "smoker"; }
    @Override protected NonNullList<ItemStack> getItems() { return items; }
    @Override public int getSizeInventory() { return 3; }
    @Override public int getInventoryStackLimit() { return 64; }
    @Override public boolean isEmpty() { for (ItemStack stack : items) if (!stack.isEmpty()) return false; return true; }
    @Override public String getName() { return hasCustomName() ? customName : "container.etfuturum." + kind(); }
    @Override public String getGuiID() { return UAB.ID + ":" + kind(); }
    @Override public Container createContainer(InventoryPlayer inventory, EntityPlayer player) { fillWithLoot(player); return new CookingContainer(inventory, this); }
    @Override public void setInventorySlotContents(int slot, ItemStack stack) {
        boolean same = slot != 0 || ItemStack.areItemsEqual(items.get(0), stack) && ItemStack.areItemStackTagsEqual(items.get(0), stack);
        super.setInventorySlotContents(slot, stack);
        if (!same) { cookTime = 0; markDirty(); }
    }
    public boolean isBurning() { return burnTime > 0; }
    private boolean canCook(ItemStack result) {
        if (result.isEmpty() || result.getCount() > Math.min(getInventoryStackLimit(), result.getMaxStackSize())) return false;
        ItemStack output = items.get(2);
        return output.isEmpty() || ItemStack.areItemsEqual(output, result) && ItemStack.areItemStackTagsEqual(output, result)
            && output.getCount() + result.getCount() <= Math.min(getInventoryStackLimit(), output.getMaxStackSize());
    }
    @Override public void update() {
        if (world.isRemote) return;
        boolean wasBurning = isBurning(), dirty = wasBurning;
        if (burnTime > 0) burnTime--;
        ItemStack result = recipes().getSmeltingResult(items.get(0)); boolean valid = canCook(result);
        if (burnTime == 0 && valid && !items.get(1).isEmpty()) {
            fuelTime = burnTime = TileEntityFurnace.getItemBurnTime(items.get(1)) / 2;
            if (burnTime > 0) {
                ItemStack fuel = items.get(1); ItemStack remainder = fuel.getItem().getContainerItem(fuel);
                fuel.shrink(1); if (fuel.isEmpty()) items.set(1, remainder); dirty = true;
            }
        }
        if (isBurning() && valid) {
            dirty = true;
            if (++cookTime >= 100) {
                cookTime = 0;
                if (items.get(2).isEmpty()) items.set(2, result.copy()); else items.get(2).grow(result.getCount());
                items.get(0).shrink(1); dirty = true;
            }
        } else if (cookTime != 0) { cookTime = 0; dirty = true; }
        if (wasBurning != isBurning()) {
            if (world.getBlockState(pos).getBlock() instanceof CookingBlock) world.setBlockState(pos, world.getBlockState(pos).withProperty(CookingBlock.LIT, isBurning()), 3);
            dirty = true;
        }
        if (dirty) markDirty();
    }
    @Override public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag); items = NonNullList.withSize(3, ItemStack.EMPTY);
        if (!checkLootAndRead(tag)) ItemStackHelper.loadAllItems(tag, items);
        burnTime = Math.max(0, tag.getInteger("BurnTime")); fuelTime = Math.max(0, tag.getInteger("FuelTime")); cookTime = Math.max(0, Math.min(99, tag.getInteger("CookTime")));
        if (!tag.hasKey("FuelTime")) fuelTime = Math.max(burnTime, TileEntityFurnace.getItemBurnTime(items.get(1)) / 2);
        if (tag.hasKey("CustomName", 8)) customName = tag.getString("CustomName");
    }
    @Override public NBTTagCompound writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag); if (!checkLootAndWrite(tag)) ItemStackHelper.saveAllItems(tag, items);
        tag.setInteger("BurnTime", burnTime); tag.setInteger("FuelTime", fuelTime); tag.setInteger("CookTime", cookTime);
        if (hasCustomName()) tag.setString("CustomName", customName); return tag;
    }
    @Override public boolean isItemValidForSlot(int slot, ItemStack stack) { return slot == 0 || slot == 1 && TileEntityFurnace.getItemBurnTime(stack) > 1; }
    @Override public int[] getSlotsForFace(EnumFacing face) { return face == EnumFacing.DOWN ? BOTTOM : face == EnumFacing.UP ? TOP : SIDE; }
    @Override public boolean canInsertItem(int slot, ItemStack stack, EnumFacing face) { return isItemValidForSlot(slot, stack); }
    @Override public boolean canExtractItem(int slot, ItemStack stack, EnumFacing face) { return face != EnumFacing.DOWN || slot != 1 || stack.getItem() == Items.BUCKET; }
    @Override public int getField(int id) {
        switch (id) { case 0: return burnTime; case 1: return fuelTime; case 2: return cookTime; case 3: return 100; case 4: return burnTime >>> 16; case 5: return fuelTime >>> 16; default: return 0; }
    }
    @Override public void setField(int id, int value) {
        switch (id) {
            case 0: burnTime = burnTime & 0xffff0000 | value & 0xffff; break;
            case 1: fuelTime = fuelTime & 0xffff0000 | value & 0xffff; break;
            case 2: cookTime = value; break;
            case 4: burnTime = value << 16 | burnTime & 0xffff; break;
            case 5: fuelTime = value << 16 | fuelTime & 0xffff; break;
        }
    }
    @Override public int getFieldCount() { return 6; }
    @Override public boolean hasCapability(Capability<?> capability, EnumFacing face) { return capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY || super.hasCapability(capability, face); }
    @Override public <T> T getCapability(Capability<T> capability, EnumFacing face) {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            if (face == null) return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(inventory);
            int index = face.getIndex(); if (handlers[index] == null) handlers[index] = new SidedInvWrapper(this, face);
            return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(handlers[index]);
        }
        return super.getCapability(capability, face);
    }
}
