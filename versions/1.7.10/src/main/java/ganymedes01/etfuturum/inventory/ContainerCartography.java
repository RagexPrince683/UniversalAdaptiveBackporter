package ganymedes01.etfuturum.inventory;

import ganymedes01.etfuturum.ModBlocks;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraft.world.storage.MapData;
import java.util.*;

public class ContainerCartography extends ContainerSelectableWorkstation {
	private String operation;
	public ContainerCartography(InventoryPlayer inventory, World world, int x, int y, int z) {
		super(inventory, world, x, y, z, ModBlocks.CARTOGRAPHY_TABLE.get(), 2);
	}
	@Override protected boolean accepts(int slot, ItemStack stack) {
		return slot == 0 ? stack.getItem() == Items.filled_map : stack.getItem() == Items.paper
				|| stack.getItem() == Items.map || stack.getItem() == Item.getItemFromBlock(Blocks.glass_pane);
	}
	@Override protected void refresh() {
		operation = null;
		ItemStack map = inputs.getStackInSlot(0), ingredient = inputs.getStackInSlot(1), result = null;
		if (map != null && ingredient != null && accepts(0, map) && accepts(1, ingredient)) {
			MapData data = Items.filled_map.getMapData(map, world);
			boolean locked = data instanceof LockedMapData && ((LockedMapData) data).etfu$isLocked();
			if (ingredient.getItem() == Items.map) operation = "copy";
			else if (ingredient.getItem() == Items.paper && data != null && data.scale < 4 && !locked) operation = "scale";
			else if (ingredient.getItem() == Item.getItemFromBlock(Blocks.glass_pane) && data != null && !locked) operation = "lock";
			if (operation != null) {
				result = map.copy(); result.stackSize = operation.equals("copy") ? 2 : 1;
				if (!operation.equals("copy")) {
					if (!result.hasTagCompound()) result.setTagCompound(new NBTTagCompound());
					// A distinct preview prevents merging a not-yet-created map into an existing identity.
					result.getTagCompound().setString("etfuturumCartography", operation);
				}
			}
		}
		output.setInventorySlotContents(0, result);
	}
	@Override public List<String> choices() {
		return operation == null ? Collections.<String>emptyList() : Collections.singletonList("gui.etfuturum.cartography." + operation);
	}
	@Override protected ItemStack craft(EntityPlayer player) {
		ItemStack result = output.getStackInSlot(0).copy();
		if (!operation.equals("copy")) {
			result.getTagCompound().removeTag("etfuturumCartography");
			MapData original = Items.filled_map.getMapData(inputs.getStackInSlot(0), world);
			result.setItemDamage(world.getUniqueDataId("map"));
			MapData data = new MapData("map_" + result.getItemDamage());
			data.scale = (byte) (original.scale + (operation.equals("scale") ? 1 : 0));
			data.xCenter = original.xCenter; data.zCenter = original.zCenter; data.dimension = original.dimension;
			if (operation.equals("lock")) {
				data.colors = original.colors.clone();
				((LockedMapData) data).etfu$setLocked(true);
			}
			data.markDirty(); world.setItemData(data.mapName, data);
		}
		inputs.decrStackSize(0, 1); inputs.decrStackSize(1, 1);
		return result;
	}
	@Override public String title() { return "tile.etfuturum.cartography_table.name"; }
}
