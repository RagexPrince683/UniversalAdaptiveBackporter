package ganymedes01.etfuturum.core.handlers;

import ganymedes01.etfuturum.ModItems;
import ganymedes01.etfuturum.tileentities.TileEntityBeeHive;
import net.minecraft.block.BlockDispenser;
import net.minecraft.dispenser.*;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityDispenser;
import net.minecraftforge.common.util.ForgeDirection;

/** Adds hive harvesting while retaining previously registered non-hive dispenser behavior. */
public final class HoneyDispenserBehaviors {
    private HoneyDispenserBehaviors() { }
    public static void register() { install(Items.shears, true); install(Items.glass_bottle, false); }
    private static void install(Item item, boolean shear) {
        IBehaviorDispenseItem previous = (IBehaviorDispenseItem) BlockDispenser.dispenseBehaviorRegistry.getObject(item);
        BlockDispenser.dispenseBehaviorRegistry.putObject(item, (IBehaviorDispenseItem) (source, stack) -> {
            ForgeDirection face = ForgeDirection.getOrientation(source.getBlockMetadata() & 7);
            int x = source.getXInt() + face.offsetX, y = source.getYInt() + face.offsetY, z = source.getZInt() + face.offsetZ;
            TileEntity tile = source.getWorld().getTileEntity(x, y, z);
            if (!(tile instanceof TileEntityBeeHive) || ((TileEntityBeeHive) tile).getHoneyLevel() < 5
                    || !(shear ? ModItems.HONEYCOMB.isEnabled() : ModItems.HONEY_BOTTLE.isEnabled()))
                return previous.dispense(source, stack);
            ItemStack product = shear ? ModItems.HONEYCOMB.newItemStack(3) : ModItems.HONEY_BOTTLE.newItemStack();
            if (shear) {
                if (!source.getWorld().spawnEntityInWorld(new EntityItem(source.getWorld(), x + .5, y + .5, z + .5, product))) return stack;
                if (stack.attemptDamageItem(1, source.getWorld().rand)) stack.stackSize = 0;
            } else {
                --stack.stackSize;
                if (stack.stackSize == 0) stack = product;
                else if (((TileEntityDispenser) source.getBlockTileEntity()).func_146019_a(product) < 0)
                    new BehaviorDefaultDispenseItem().dispense(source, product);
            }
            ((TileEntityBeeHive) tile).setHoneyLevel(0);
            source.getWorld().playAuxSFX(1000, source.getXInt(), source.getYInt(), source.getZInt(), 0);
            return stack;
        });
    }
}
