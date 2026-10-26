package ganymedes01.etfuturum;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

final class SmithingBlock extends Block {
    SmithingBlock() { super(Material.WOOD); setHardness(2.5F); }
    @Override public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing side, float x, float y, float z) {
        if (!world.isRemote) player.openGui(UAB.instance, 0, world, pos.getX(), pos.getY(), pos.getZ());
        return true;
    }
}
