package ganymedes01.etfuturum;

import ganymedes01.etfuturum.tileentities.TileEntityBeeHive;
import net.minecraft.block.BlockDispenser;
import net.minecraft.dispenser.*;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityDispenser;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

public final class HoneyDispenserBehaviors {
    private HoneyDispenserBehaviors() { }
    public static void register() { install(Items.SHEARS, true); install(Items.GLASS_BOTTLE, false); }
    private static void install(Item item, boolean shear) {
        IBehaviorDispenseItem previous = BlockDispenser.DISPENSE_BEHAVIOR_REGISTRY.getObject(item);
        BlockDispenser.DISPENSE_BEHAVIOR_REGISTRY.putObject(item, (source, stack) -> {
            EnumFacing face = source.getBlockState().getValue(BlockDispenser.FACING);
            BlockPos pos = source.getBlockPos().offset(face);
            TileEntity tile = source.getWorld().getTileEntity(pos);
            if (!(tile instanceof TileEntityBeeHive) || ((TileEntityBeeHive) tile).getHoneyLevel() < 5
                    || !Content.available(shear ? "honeycomb" : "honey_bottle")) return previous.dispense(source, stack);
            ItemStack product = new ItemStack(Content.item(shear ? "honeycomb" : "honey_bottle"), shear ? 3 : 1);
            if (shear) {
                if (!ItemDelivery.drop(source.getWorld(), pos.getX() + .5, pos.getY() + .5, pos.getZ() + .5, product)) return stack;
                if (stack.attemptDamageItem(1, source.getWorld().rand, null)) stack.setCount(0);
            } else {
                stack.shrink(1);
                if (stack.isEmpty()) stack = product;
                else if (((TileEntityDispenser) source.getBlockTileEntity()).addItemStack(product) < 0)
                    new BehaviorDefaultDispenseItem().dispense(source, product);
            }
            ((TileEntityBeeHive) tile).setHoneyLevel(0);
            source.getWorld().playEvent(1000, source.getBlockPos(), 0);
            return stack;
        });
    }
}
