package ganymedes01.etfuturum;

import java.util.Random;
import net.minecraft.block.*;
import net.minecraft.block.material.*;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

final class BuddingAmethyst extends Block {
    private final int mode = Features.integer("buddingAmethystMode", 0, 0, 2);
    BuddingAmethyst() { super(Material.ROCK); setHardness(1.5F); setResistance(1.5F); setSoundType(SoundType.GLASS); setTickRandomly(true); setHarvestLevel("pickaxe", 0); }
    @Override public EnumPushReaction getPushReaction(IBlockState state) { return mode == 0 ? EnumPushReaction.DESTROY : EnumPushReaction.NORMAL; }
    @Override public boolean canSilkHarvest(World world, BlockPos pos, IBlockState state, EntityPlayer player) { return mode != 0; }
    @Override public Item getItemDropped(IBlockState state, Random random, int fortune) { return mode == 2 ? Item.getItemFromBlock(this) : Items.AIR; }
    @Override public void updateTick(World world, BlockPos pos, IBlockState state, Random random) {
        if (world.isRemote || !Content.available("budding_amethyst") || random.nextInt(5) != 0) return;
        EnumFacing facing = EnumFacing.values()[random.nextInt(6)]; BlockPos target = pos.offset(facing);
        if (!world.isBlockLoaded(target)) return;
        IBlockState current = world.getBlockState(target); int stage = -1;
        if (current.getBlock() instanceof AmethystBud && current.getValue(AmethystBud.FACING) == facing) stage = ((AmethystBud) current.getBlock()).stage;
        else if (current.getMaterial() != Material.AIR && current.getMaterial() != Material.WATER) return;
        if (stage >= 3) return;
        world.setBlockState(target, Content.block(AmethystBud.STAGES[stage + 1]).getDefaultState().withProperty(AmethystBud.FACING, facing), 3);
    }
}
