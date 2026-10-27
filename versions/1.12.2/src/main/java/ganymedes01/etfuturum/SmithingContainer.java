package ganymedes01.etfuturum;

import net.minecraft.entity.player.*;
import net.minecraft.inventory.*;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import ganymedes01.etfuturum.recipes.SmithingTableRecipes;

public final class SmithingContainer extends Container {
    private final World world;
    private final BlockPos pos;
    private final InventoryBasic output = new InventoryBasic("Result", false, 1);
    private boolean consuming;
    public final InventoryBasic input = new InventoryBasic("Smithing", false, 3) {
        @Override public void markDirty() { super.markDirty(); if (!consuming) updateResult(); }
    };
    public SmithingContainer(InventoryPlayer inventory, World world, BlockPos pos) {
        this.world = world; this.pos = pos;
        addSlotToContainer(new Slot(input,0,26,35){@Override public boolean isItemValid(ItemStack stack){return SmithingTableRecipes.getInstance().isTemplate(stack);}});
        addSlotToContainer(new Slot(input,1,62,35){@Override public boolean isItemValid(ItemStack stack){return SmithingTableRecipes.getInstance().isBase(stack);}});
        addSlotToContainer(new Slot(input,2,98,35){@Override public boolean isItemValid(ItemStack stack){return SmithingTableRecipes.getInstance().isMaterial(stack);}});
        addSlotToContainer(new Slot(output, 0, 144, 35) {
            @Override public boolean isItemValid(ItemStack stack) { return false; }
            @Override public boolean canTakeStack(EntityPlayer player) { return !getStack().isEmpty(); }
            @Override public ItemStack onTake(EntityPlayer player, ItemStack stack) {
                consuming = true;
                try{for(int i=0;i<3;i++)input.decrStackSize(i,1);}finally{consuming=false;}
                if(!world.isRemote){stack.onCrafting(world,player,stack.getCount());world.playSound(null,pos,ModernSounds.get("block.smithing_table.use"),net.minecraft.util.SoundCategory.BLOCKS,1,1);}
                updateResult();
                return super.onTake(player, stack);
            }
        });
        for (int row = 0; row < 3; row++) for (int column = 0; column < 9; column++) addSlotToContainer(new Slot(inventory, column + row * 9 + 9, 8 + column * 18, 84 + row * 18));
        for (int column = 0; column < 9; column++) addSlotToContainer(new Slot(inventory, column, 8 + column * 18, 142));
    }
    private void updateResult() {
        ItemStack result=SmithingTableRecipes.getInstance().findMatchingRecipe(input.getStackInSlot(0),input.getStackInSlot(1),input.getStackInSlot(2),world);
        output.setInventorySlotContents(0, result); detectAndSendChanges();
    }
    @Override public boolean canInteractWith(EntityPlayer player) { return world.getBlockState(pos).getBlock() == Content.block("smithing_table") && player.getDistanceSq(pos) <= 64; }
    @Override public ItemStack transferStackInSlot(EntityPlayer player, int index) {
        Slot slot = inventorySlots.get(index);
        if (!slot.getHasStack()) return ItemStack.EMPTY;
        ItemStack original = slot.getStack(), copy = original.copy();
        if (index == 3) {
            if (!mergeItemStack(original, 4, 40, true)) return ItemStack.EMPTY;
        } else if (index < 3) {
            if (!mergeItemStack(original, 4, 40, false)) return ItemStack.EMPTY;
        } else {
            SmithingTableRecipes recipes=SmithingTableRecipes.getInstance();int target=recipes.isTemplate(original)?0:recipes.isBase(original)?1:recipes.isMaterial(original)?2:-1;
            if(target<0)return ItemStack.EMPTY;
            if (!mergeItemStack(original, target, target + 1, false)) return ItemStack.EMPTY;
        }
        if (original.isEmpty()) slot.putStack(ItemStack.EMPTY); else slot.onSlotChanged();
        if (original.getCount() == copy.getCount()) return ItemStack.EMPTY;
        slot.onTake(player,index==3?copy:original);
        return copy;
    }
    @Override public void onContainerClosed(EntityPlayer player) { super.onContainerClosed(player); if (!world.isRemote) clearContainer(player, world, input); }
}
