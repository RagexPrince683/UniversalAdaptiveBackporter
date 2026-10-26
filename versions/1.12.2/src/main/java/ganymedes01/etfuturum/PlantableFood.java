package ganymedes01.etfuturum;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.*;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public final class PlantableFood extends ItemFood {
    private final String plant;
    PlantableFood(int hunger, float saturation, String plant) { super(hunger, saturation, false); this.plant = plant; }
    @Override public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing side, float x, float y, float z) {
        BlockPos target = pos.offset(side);
        if (side != EnumFacing.UP || !player.canPlayerEdit(target, side, player.getHeldItem(hand)) || !Content.block(plant).canPlaceBlockAt(world, target)) return EnumActionResult.PASS;
        if (!world.isRemote) { world.setBlockState(target, Content.block(plant).getDefaultState(), 3); if (!player.capabilities.isCreativeMode) player.getHeldItem(hand).shrink(1); }
        return EnumActionResult.SUCCESS;
    }
}
