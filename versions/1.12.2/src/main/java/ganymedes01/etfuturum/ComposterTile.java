package ganymedes01.etfuturum;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.text.*;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.*;
import net.minecraftforge.items.wrapper.SidedInvWrapper;

/** Pure automation queries; insertion/extraction commit only when the native inventory mutates. */
public final class ComposterTile extends TileEntity implements ISidedInventory {
    @Override public boolean shouldRefresh(net.minecraft.world.World world, net.minecraft.util.math.BlockPos pos, net.minecraft.block.state.IBlockState before, net.minecraft.block.state.IBlockState after) { return before.getBlock() != after.getBlock(); }
    private static final int[] ONE = {0}, NONE = {};
    private final IItemHandler[] handlers = new IItemHandler[6];
    private boolean extractionPending;
    private int level() { return world != null && world.getBlockState(pos).getBlock() instanceof ComposterBlock ? world.getBlockState(pos).getValue(ComposterBlock.LEVEL) : -1; }
    @Override public void onLoad() { if (!world.isRemote && level() == 7) world.scheduleUpdate(pos, Content.block("composter"), 10); }
    @Override public int getSizeInventory() { return 1; }
    @Override public int getInventoryStackLimit() { return 1; }
    @Override public boolean isEmpty() { return level() != 8; }
    @Override public ItemStack getStackInSlot(int slot) { return slot == 0 && level() == 8 ? new ItemStack(Items.DYE, 1, 15) : ItemStack.EMPTY; }
    @Override public ItemStack decrStackSize(int slot, int amount) {
        ItemStack result = amount > 0 ? getStackInSlot(slot) : ItemStack.EMPTY;
        if (!result.isEmpty()) { extractionPending = true; world.setBlockState(pos, world.getBlockState(pos).withProperty(ComposterBlock.LEVEL, 0), 3); markDirty(); } return result;
    }
    @Override public ItemStack removeStackFromSlot(int slot) { return decrStackSize(slot, 1); }
    @Override public void setInventorySlotContents(int slot, ItemStack stack) {
        if (slot != 0 || stack.isEmpty()) return;
        if (extractionPending && level() == 0 && stack.getItem() == Items.DYE && stack.getMetadata() == 15) {
            world.setBlockState(pos, world.getBlockState(pos).withProperty(ComposterBlock.LEVEL, 8), 3); extractionPending = false;
        } else { extractionPending = false; if (level() >= 0 && level() < 7) ((ComposterBlock) Content.block("composter")).add(world, pos, stack); }
        markDirty();
    }
    @Override public boolean isItemValidForSlot(int slot, ItemStack stack) { return slot == 0 && level() >= 0 && level() < 7 && Composting.chance(stack) > 0; }
    @Override public int[] getSlotsForFace(EnumFacing side) { return side == EnumFacing.UP && level() < 7 || side == EnumFacing.DOWN && level() == 8 ? ONE : NONE; }
    @Override public boolean canInsertItem(int slot, ItemStack stack, EnumFacing side) { return side == EnumFacing.UP && isItemValidForSlot(slot, stack); }
    @Override public boolean canExtractItem(int slot, ItemStack stack, EnumFacing side) { return slot == 0 && side == EnumFacing.DOWN && level() == 8; }
    @Override public boolean isUsableByPlayer(EntityPlayer player) { return false; }
    @Override public void openInventory(EntityPlayer player) { }
    @Override public void closeInventory(EntityPlayer player) { }
    @Override public String getName() { return "tile.etfuturum.composter.name"; }
    @Override public boolean hasCustomName() { return false; }
    @Override public ITextComponent getDisplayName() { return new TextComponentTranslation(getName()); }
    @Override public int getField(int id) { return 0; }
    @Override public void setField(int id, int value) { }
    @Override public int getFieldCount() { return 0; }
    @Override public void clear() { if (level() >= 0) world.setBlockState(pos, world.getBlockState(pos).withProperty(ComposterBlock.LEVEL, 0), 3); }
    @Override public boolean hasCapability(Capability<?> capability, EnumFacing side) { return capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY && side != null || super.hasCapability(capability, side); }
    @Override public <T> T getCapability(Capability<T> capability, EnumFacing side) {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY && side != null) {
            if (handlers[side.getIndex()] == null) handlers[side.getIndex()] = new SidedInvWrapper(this, side);
            return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(handlers[side.getIndex()]);
        }
        return super.getCapability(capability, side);
    }
}
