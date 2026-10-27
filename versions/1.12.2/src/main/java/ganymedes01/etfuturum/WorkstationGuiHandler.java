package ganymedes01.etfuturum;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;

final class WorkstationGuiHandler implements IGuiHandler {
    private WorkstationContainer selectable(int id, EntityPlayer player, World world, BlockPos pos) {
        if (id == 2) return new StonecutterContainer(player.inventory, world, pos);
        if (id == 3) return new LoomContainer(player.inventory, world, pos);
        if (id == 4) return new CartographyContainer(player.inventory, world, pos);
        return null;
    }
    @Override public Object getServerGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
        BlockPos pos = new BlockPos(x, y, z);
        if(id==5&&world.getTileEntity(pos) instanceof net.minecraft.inventory.IInventory)return new StorageContainer(player.inventory,(net.minecraft.inventory.IInventory)world.getTileEntity(pos),player);
        if (id == 0) return new SmithingContainer(player.inventory, world, pos);
        if (id == 1 && world.getTileEntity(pos) instanceof CookingTile) return ((CookingTile) world.getTileEntity(pos)).createContainer(player.inventory, player);
        return selectable(id, player, world, pos);
    }
    @Override public Object getClientGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
        BlockPos pos = new BlockPos(x, y, z);
        if(id==5&&world.getTileEntity(pos) instanceof net.minecraft.inventory.IInventory)return UAB.proxy.storageGui(player,(net.minecraft.inventory.IInventory)world.getTileEntity(pos));
        if (id == 0) return UAB.proxy.smithingGui(player, world, pos);
        if (id == 1 && world.getTileEntity(pos) instanceof CookingTile) return UAB.proxy.cookingGui(player, (CookingTile) world.getTileEntity(pos));
        WorkstationContainer container = selectable(id, player, world, pos);
        return container == null ? null : UAB.proxy.workstationGui(container);
    }
}
