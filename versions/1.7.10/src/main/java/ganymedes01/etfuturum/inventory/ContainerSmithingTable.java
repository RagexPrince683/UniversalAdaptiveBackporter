package ganymedes01.etfuturum.inventory;

import ganymedes01.etfuturum.inventory.slot.SlotSmithingResult;
import ganymedes01.etfuturum.recipes.SmithingTableRecipes;
import ganymedes01.etfuturum.ModBlocks;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.*;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class ContainerSmithingTable extends Container {
	private final World world;
	private final int tableX, tableY, tableZ;
	private final boolean tableBound;
	public final InventoryCrafting inputMatrix = new InventoryCrafting(this, 3, 1);
	private final InventoryCraftResult resultInventory = new InventoryCraftResult();
	private final Slot template = new Slot(inputMatrix, 2, 8, 47) {
		@Override public boolean isItemValid(ItemStack stack) { return stack != null && stack.getItem() instanceof ganymedes01.etfuturum.items.ItemSmithingTemplate; }
	};
	private final Slot applicant = new Slot(inputMatrix, 0, 48, 47);
	private final Slot ingot = new Slot(inputMatrix, 1, 88, 47);
	private final Slot result;

	public ContainerSmithingTable(InventoryPlayer inv, World world) {
		this(inv, world, net.minecraft.util.MathHelper.floor_double(inv.player.posX),
				net.minecraft.util.MathHelper.floor_double(inv.player.posY), net.minecraft.util.MathHelper.floor_double(inv.player.posZ), false);
	}

	public ContainerSmithingTable(InventoryPlayer inv, World world, int blockX, int blockY, int blockZ) {
		this(inv, world, blockX, blockY, blockZ, true);
	}

	private ContainerSmithingTable(InventoryPlayer inv, World world, int blockX, int blockY, int blockZ, boolean tableBound) {
		this.world = world;
		this.tableBound = tableBound;
		tableX = blockX;
		tableY = blockY;
		tableZ = blockZ;
		addSlotToContainer(template);
		addSlotToContainer(applicant);
		addSlotToContainer(ingot);
		result = addSlotToContainer(new SlotSmithingResult(inv.player, inputMatrix, resultInventory, 0, 142, 47));
		for (int y = 0; y < 3; y++)
			for (int x = 0; x < 9; x++) addSlotToContainer(new Slot(inv, x + y * 9 + 9, 8 + x * 18, 84 + y * 18));
		for (int y = 0; y < 9; y++) addSlotToContainer(new Slot(inv, y, 8 + y * 18, 142));
	}

	@Override
	public boolean canInteractWith(EntityPlayer player) {
		return player.worldObj == world && !player.isDead
				&& (!tableBound || world.blockExists(tableX, tableY, tableZ)
				&& world.getBlock(tableX, tableY, tableZ) == ModBlocks.SMITHING_TABLE.get())
				&& player.getDistanceSq(tableX + 0.5, tableY + 0.5, tableZ + 0.5) <= 64;
	}

	@Override
	public void onContainerClosed(EntityPlayer player) {
		super.onContainerClosed(player);
		if (!world.isRemote) {
			for (int i = 0; i < inputMatrix.getSizeInventory(); i++) {
				final ItemStack stack = inputMatrix.getStackInSlotOnClosing(i);
				if (stack != null) player.dropPlayerItemWithRandomChoice(stack, false);
			}
		}
	}

	@Override
	public ItemStack transferStackInSlot(EntityPlayer player, int index) {
		final Object s = inventorySlots.get(index);
		if (!(s instanceof Slot)) return null;
		final Slot slot = (Slot) s;
		if (!slot.getHasStack()) return null;
		final ItemStack newStack = slot.getStack();
		final ItemStack oldStack = newStack.copy();
		boolean merged = false;
		if (index <= 3) {
			merged = mergeItemStack(newStack, 4, 40, true);
		} else if (index < 31) {
			merged = tryUpper(newStack) || mergeItemStack(newStack, 31, 40, false);
		} else {
			merged = tryUpper(newStack) || mergeItemStack(newStack, 4, 31, false);
		}
		if (!merged || newStack.stackSize == oldStack.stackSize) return null;
		if (newStack.stackSize == 0) {
			slot.putStack(null);
		} else slot.onSlotChanged();
		slot.onPickupFromSlot(player, newStack);
		return oldStack;
	}

	@Override
	public void onCraftMatrixChanged(IInventory inv) {
		resultInventory.setInventorySlotContents(0, SmithingTableRecipes.getInstance().findMatchingRecipe(this.inputMatrix, this.world));
		super.onCraftMatrixChanged(inv);
	}

	private boolean tryUpper(ItemStack newStack) {
		if (template.isItemValid(newStack)) return mergeItemStack(newStack, 0, 1, false);
		SmithingTableRecipes recipes = SmithingTableRecipes.getInstance();
		if (recipes.isMaterial(newStack) && mergeItemStack(newStack, 2, 3, false)) return true;
		return recipes.isBase(newStack) && mergeItemStack(newStack, 1, 2, false);
	}

	public boolean unable() {
		return (template.getHasStack() || applicant.getHasStack() || ingot.getHasStack()) && !result.getHasStack();
	}
}
