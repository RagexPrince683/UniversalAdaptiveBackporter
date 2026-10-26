package ganymedes01.etfuturum.world.structure;

import ganymedes01.etfuturum.ModItems;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.world.gen.structure.*;
import java.util.*;

/** Separate template rolls preserve the existing treasure pools and their selection weights. */
public final class ExplorationLoot {
	private ExplorationLoot() { }
	public static void put(IInventory inventory, ModItems item, int count, Random random) {
		if (inventory == null || !item.isEnabled()) return;
		List<Integer> slots = new ArrayList<>(); for (int i = 0; i < inventory.getSizeInventory(); i++) if (inventory.getStackInSlot(i) == null) slots.add(i);
		if (!slots.isEmpty()) { inventory.setInventorySlotContents(slots.get(random.nextInt(slots.size())), item.newItemStack(count)); inventory.markDirty(); }
	}
	public static void nativeChest(StructureComponent piece, IInventory inventory, Random random) {
		if (piece instanceof ComponentScatteredFeaturePieces.DesertPyramid && random.nextInt(7) == 0) put(inventory, ModItems.DUNE_ARMOR_TRIM_SMITHING_TEMPLATE, 2, random);
		else if (piece instanceof ComponentScatteredFeaturePieces.JunglePyramid && random.nextInt(3) == 0) put(inventory, ModItems.WILD_ARMOR_TRIM_SMITHING_TEMPLATE, 2, random);
		else if (piece instanceof StructureStrongholdPieces.Library || piece instanceof StructureStrongholdPieces.ChestCorridor && random.nextInt(10) == 0) put(inventory, ModItems.EYE_ARMOR_TRIM_SMITHING_TEMPLATE, 1, random);
		else if ((piece instanceof StructureNetherBridgePieces.Corridor || piece instanceof StructureNetherBridgePieces.Corridor2) && random.nextInt(15) == 0) put(inventory, ModItems.RIB_ARMOR_TRIM_SMITHING_TEMPLATE, 1, random);
	}
	public static void bastion(IInventory inventory, String table, Random random) {
		if (random.nextInt(12) == 0) put(inventory, ModItems.SNOUT_ARMOR_TRIM_SMITHING_TEMPLATE, 1, random);
		if (table.endsWith("bastion_treasure") || random.nextInt(10) == 0) put(inventory, ModItems.NETHERITE_UPGRADE_SMITHING_TEMPLATE, 1, random);
	}
}
