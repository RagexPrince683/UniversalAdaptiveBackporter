package ganymedes01.etfuturum;

import java.util.Random;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

class ModernSapling extends BlockBush implements IGrowable {
    private final String tree;
    ModernSapling(String tree) { this.tree = tree; setTickRandomly(true); }
    @Override public void updateTick(World world, BlockPos pos, IBlockState state, Random random) {
        super.updateTick(world, pos, state, random);
        if (!world.isRemote && world.getLight(pos.up()) >= 9 && random.nextInt(7) == 0) grow(world, random, pos, state);
    }
    @Override public boolean canGrow(World world, BlockPos pos, IBlockState state, boolean client) { return true; }
    @Override public boolean canUseBonemeal(World world, Random random, BlockPos pos, IBlockState state) { return random.nextFloat() < .45F; }
    @Override public void grow(World world, Random random, BlockPos pos, IBlockState state) {
        if (!net.minecraftforge.event.terraingen.TerrainGen.saplingGrowTree(world, random, pos)) return;
        if (tree.equals("pale_oak")) {
            for (int x = 0; x >= -1; x--) for (int z = 0; z >= -1; z--) {
                BlockPos base = pos.add(x, 0, z);
                if (world.getBlockState(base).getBlock() == this && world.getBlockState(base.east()).getBlock() == this && world.getBlockState(base.south()).getBlock() == this && world.getBlockState(base.east().south()).getBlock() == this) { ModernTreeGenerator.generate(world, random, base, tree, true); return; }
            }
        } else ModernTreeGenerator.generate(world, random, pos, tree, true);
    }
    static boolean generate(World world, Random random, BlockPos pos, String tree) {
        return ModernTreeGenerator.generate(world, random, pos, tree, false);
    }
}
