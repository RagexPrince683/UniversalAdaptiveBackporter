package ganymedes01.etfuturum;

import net.minecraft.entity.player.*;
import net.minecraft.inventory.*;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntityLockableLoot;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;

/** Native lootable inventory without chest adjacency or the chest renderer. */
public final class BarrelTile extends TileEntityLockableLoot implements ITickable {
    @Override public boolean shouldRefresh(net.minecraft.world.World world, net.minecraft.util.math.BlockPos pos, net.minecraft.block.state.IBlockState before, net.minecraft.block.state.IBlockState after) { return before.getBlock() != after.getBlock(); }
    private NonNullList<ItemStack> items = NonNullList.withSize(27, ItemStack.EMPTY);
    private int viewers, ticks;
    @Override protected NonNullList<ItemStack> getItems() { return items; }
    @Override public int getSizeInventory() { return 27; }
    @Override public boolean isEmpty() { for (ItemStack stack : items) if (!stack.isEmpty()) return false; return true; }
    @Override public int getInventoryStackLimit() { return 64; }
    @Override public String getName() { return hasCustomName() ? customName : "container.etfuturum.barrel"; }
    @Override public String getGuiID() { return "minecraft:chest"; }
    @Override public Container createContainer(InventoryPlayer inventory, EntityPlayer player) { fillWithLoot(player); return new ContainerChest(inventory, this, player); }
    @Override public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag); items = NonNullList.withSize(27, ItemStack.EMPTY);
        if (!checkLootAndRead(tag)) ItemStackHelper.loadAllItems(tag, items);
        if (tag.hasKey("CustomName", 8)) customName = tag.getString("CustomName");
    }
    @Override public NBTTagCompound writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag); if (!checkLootAndWrite(tag)) ItemStackHelper.saveAllItems(tag, items);
        if (hasCustomName()) tag.setString("CustomName", customName); return tag;
    }
    @Override public void openInventory(EntityPlayer player) { if (!player.isSpectator()) { viewers++; opened(); } }
    @Override public void closeInventory(EntityPlayer player) { if (!player.isSpectator()) { viewers = Math.max(0, viewers - 1); opened(); } }
    private void opened() {
        if (world == null || world.isRemote || world.getBlockState(pos).getBlock() != Content.block("barrel")) return;
        boolean open = viewers > 0;
        if (world.getBlockState(pos).getValue(BarrelBlock.OPEN) != open) world.setBlockState(pos, world.getBlockState(pos).withProperty(BarrelBlock.OPEN, open), 3);
    }
    @Override public void update() {
        if (world.isRemote) return;
        if (ticks++ == 0) opened();
        if (ticks % 200 != 0 || viewers == 0) return;
        viewers = 0;
        for (EntityPlayer player : world.getEntitiesWithinAABB(EntityPlayer.class, new AxisAlignedBB(pos).grow(5)))
            if (player.openContainer instanceof ContainerChest && ((ContainerChest) player.openContainer).getLowerChestInventory() == this) viewers++;
        opened();
    }
}
