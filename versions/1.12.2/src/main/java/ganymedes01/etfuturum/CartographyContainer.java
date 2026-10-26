package ganymedes01.etfuturum;

import java.util.*;
import net.minecraft.entity.player.*;
import net.minecraft.init.*;
import net.minecraft.item.*;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.storage.MapData;

public final class CartographyContainer extends WorkstationContainer {
    private String operation;
    public CartographyContainer(InventoryPlayer inventory, World world, BlockPos pos) { super(inventory, world, pos, Content.block("cartography_table"), 2); }
    @Override protected boolean accepts(int slot, ItemStack stack) { return !stack.isEmpty() && (slot == 0 ? stack.getItem() == Items.FILLED_MAP : stack.getItem() == Items.PAPER || stack.getItem() == Items.MAP || stack.getItem() == Item.getItemFromBlock(Blocks.GLASS_PANE)); }
    @Override protected void refresh() {
        operation = null; ItemStack map = inputs.getStackInSlot(0), ingredient = inputs.getStackInSlot(1), result = ItemStack.EMPTY;
        if (accepts(0, map) && accepts(1, ingredient)) {
            MapData data = Items.FILLED_MAP.getMapData(map, world);
            boolean locked = MapLocks.locked(map,world);
            if (ingredient.getItem() == Items.MAP) operation = "copy";
            else if (ingredient.getItem() == Items.PAPER && data != null && data.scale < 4 && !locked) operation = "scale";
            else if (ingredient.getItem() == Item.getItemFromBlock(Blocks.GLASS_PANE) && data != null && !locked) operation = "lock";
            if (operation != null) {
                result = map.copy(); result.setCount(operation.equals("copy") ? 2 : 1);
                if (!operation.equals("copy")) result.getOrCreateSubCompound("etfuturumCartography").setString("Operation", operation);
            }
        }
        output.setInventorySlotContents(0, result);
    }
    @Override public List<String> choices() {
        String choice = operation;
        if (world.isRemote) {
            ItemStack preview = output.getStackInSlot(0);
            if (preview.isEmpty()) choice = null;
            else if (preview.getSubCompound("etfuturumCartography") != null) choice = preview.getSubCompound("etfuturumCartography").getString("Operation");
            else if (preview.getCount() == 2) choice = "copy";
        }
        return choice == null ? Collections.emptyList() : Collections.singletonList("gui.etfuturum.cartography." + choice);
    }
    @Override protected ItemStack craft(EntityPlayer player) {
        ItemStack result = output.getStackInSlot(0).copy();
        if (!operation.equals("copy")) {
            result.getTagCompound().removeTag("etfuturumCartography");
            if (operation.equals("scale")) {
                result.getTagCompound().setInteger("map_scale_direction", 1); Items.FILLED_MAP.onCreated(result, world, player);
            } else {
                MapData original = Items.FILLED_MAP.getMapData(inputs.getStackInSlot(0), world);
                int id = world.getUniqueDataId("map"); result.setItemDamage(id);
                MapData data = new MapData("map_" + id); data.readFromNBT(original.writeToNBT(new NBTTagCompound()).copy());
                ((LockedMapData) data).etfu$setLocked(true); result.getTagCompound().setBoolean("etfuturumLocked", true);
                data.markDirty(); world.setData(data.mapName, data);
            }
        }
        inputs.decrStackSize(0, 1); inputs.decrStackSize(1, 1); return result;
    }
    @Override public String title() { return "tile.etfuturum.cartography_table.name"; }
}
