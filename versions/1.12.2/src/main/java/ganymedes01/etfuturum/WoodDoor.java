package ganymedes01.etfuturum;

import java.util.Random;
import net.minecraft.block.BlockDoor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Items;
import net.minecraft.item.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

final class WoodDoor extends BlockDoor {
    WoodDoor() { super(Material.WOOD); setHardness(3); setResistance(3); }
    @Override public Item getItemDropped(IBlockState state, Random random, int fortune) { return state.getValue(HALF) == EnumDoorHalf.UPPER ? Items.AIR : Content.item(getRegistryName().getPath()); }
    @Override public ItemStack getItem(World world, BlockPos pos, IBlockState state) { return Content.stack(getRegistryName().getPath()); }
}
