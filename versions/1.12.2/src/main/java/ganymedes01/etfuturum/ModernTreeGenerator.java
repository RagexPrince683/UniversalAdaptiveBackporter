package ganymedes01.etfuturum;

import java.util.*;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/** Native ports of the maintained cherry, shallow-water mangrove and two-by-two pale oak trees. */
final class ModernTreeGenerator {
    private final World world;
    private final Random random;
    private final String tree;
    private final Map<BlockPos, IBlockState> plan = new LinkedHashMap<>();
    private ModernTreeGenerator(World world, Random random, String tree) { this.world = world; this.random = random; this.tree = tree; }
    static boolean generate(World world, Random random, BlockPos pos, String tree, boolean notify) {
        if (!Content.available(tree + "_sapling") || !world.isAreaLoaded(pos.add(-8, -6, -8), pos.add(8, 15, 8))) return false;
        ModernTreeGenerator generator = new ModernTreeGenerator(world, random, tree);
        boolean valid = tree.equals("cherry") ? generator.cherry(pos) : tree.equals("mangrove") ? generator.mangrove(pos) : tree.equals("pale_oak") && generator.paleOak(pos);
        if(valid&&!notify&&tree.equals("pale_oak")&&Content.available("creaking_heart")&&random.nextInt(10)==0){int height=0;while(height<32&&generator.plan.containsKey(pos.up(height))&&generator.plan.get(pos.up(height)).getBlock()==Content.block("pale_oak_log"))height++;if(height>=4)generator.plan.put(pos.up(height-3),Content.block("creaking_heart").getDefaultState());}
        return valid && generator.apply(notify);
    }
    private void log(BlockPos pos, EnumFacing.Axis axis) { plan.put(pos, Content.block(tree + "_log").getDefaultState().withProperty(BlockLog.LOG_AXIS, BlockLog.EnumAxis.fromFacingAxis(axis))); }
    private void leaf(BlockPos pos) { if (!plan.containsKey(pos)) plan.put(pos, Content.block(tree + "_leaves").getDefaultState()); }
    private boolean soil(BlockPos pos, boolean wet) {
        Block block = world.getBlockState(pos).getBlock();
        return block == Blocks.GRASS || block == Blocks.DIRT || block instanceof PaleMoss || wet && (block == Blocks.CLAY || block == Content.block("mud"));
    }
    private boolean apply(boolean notify) {
        for (Map.Entry<BlockPos, IBlockState> entry : plan.entrySet()) {
            BlockPos p = entry.getKey(); IBlockState old = world.getBlockState(p);
            if (p.getY() < 1 || p.getY() >= world.getHeight() || world.getTileEntity(p) != null) return false;
            if (old.getMaterial() != Material.AIR && old.getMaterial() != Material.LEAVES && !(old.getBlock() instanceof ModernSapling) && !(tree.equals("mangrove") && old.getMaterial() == Material.WATER) && !old.getBlock().isReplaceable(world, p)) {
                if (!(entry.getValue().getBlock() == Content.BLOCKS.get("muddy_mangrove_roots") && old.getBlock() == Content.block("mud"))) return false;
            }
        }
        for (Map.Entry<BlockPos, IBlockState> entry : plan.entrySet()) world.setBlockState(entry.getKey(), entry.getValue(), notify ? 3 : 18);
        return true;
    }
    private boolean cherry(BlockPos pos) {
        if (!soil(pos.down(), false)) return false;
        int first = 2 + random.nextInt(2), second = 2 + random.nextInt(2);
        if (second >= first) second++;
        int branches = 1 + random.nextInt(3), height = branches == 3 ? 7 : branches == 2 ? Math.max(first, second) + 1 : first + 1;
        for (int y = 0; y < height; y++) log(pos.up(y), EnumFacing.Axis.Y);
        if (branches == 3) cherryLeaves(pos.up(height));
        EnumFacing direction = EnumFacing.HORIZONTALS[random.nextInt(4)];
        cherryBranch(pos, first, direction, first < height - 1);
        if (branches >= 2) cherryBranch(pos, second, direction.getOpposite(), second < height - 1);
        return true;
    }
    private void cherryBranch(BlockPos base, int start, EnumFacing facing, boolean lower) {
        int length = 2 + random.nextInt(3) + (lower ? 1 : 0);
        BlockPos cursor = base.up(start), end = base.offset(facing, length).up(5 + random.nextInt(2));
        for (int i = 0, count = lower ? 2 : 1; i < count; i++) { cursor = cursor.offset(facing); log(cursor, facing.getAxis()); }
        while (!cursor.equals(end)) {
            int vertical = Math.abs(end.getY() - cursor.getY()), horizontal = Math.abs(end.getX() - cursor.getX()) + Math.abs(end.getZ() - cursor.getZ());
            boolean up = vertical != 0 && random.nextFloat() < (float) vertical / (vertical + horizontal);
            EnumFacing step = up ? end.getY() > cursor.getY() ? EnumFacing.UP : EnumFacing.DOWN : facing;
            cursor = cursor.offset(step); log(cursor, step.getAxis());
        }
        cherryLeaves(end.up());
    }
    private void cherryLeaves(BlockPos center) {
        for (int y = -2; y <= 2; y++) {
            int radius = y == 2 ? 1 : y == 1 || y == -2 ? 2 : 3;
            for (int x = -radius; x <= radius; x++) for (int z = -radius; z <= radius; z++) {
                boolean corner = Math.abs(x) == radius && Math.abs(z) == radius;
                if (corner && (radius > 2 || random.nextFloat() < .25F) || y == -1 && (Math.abs(x) == radius || Math.abs(z) == radius) && random.nextFloat() < .25F) continue;
                BlockPos p = center.add(x, y, z); leaf(p);
                if (y == -1 && random.nextFloat() < 1F / 6) { leaf(p.down()); if (random.nextFloat() < 1F / 3) leaf(p.down(2)); }
            }
        }
    }
    private boolean mangrove(BlockPos pos) {
        int height = 7 + random.nextInt(5); BlockPos ground = pos.down();
        while (ground.getY() > pos.getY() - 6 && world.getBlockState(ground).getMaterial() == Material.WATER) ground = ground.down();
        if (!soil(ground, true)) return false;
        // Root columns start below the raised trunk and reach the shallow-water bed.
        for (EnumFacing direction : EnumFacing.HORIZONTALS) for (int step = 1; step <= 3; step++) {
            BlockPos root = pos.offset(direction, step).up(4 - step);
            for (; root.getY() >= pos.getY() - 5 && root.getY() > 0; root = root.down()) {
                IBlockState old = world.getBlockState(root);
                if (old.getBlock() == Content.block("mud")) { if (Content.available("mud")) plan.put(root, Content.block("muddy_mangrove_roots").getDefaultState()); break; }
                if (old.getMaterial().isSolid() && old.getMaterial() != Material.LEAVES && !old.getBlock().isReplaceable(world, root)) break;
                plan.put(root, Content.block("mangrove_roots").getDefaultState());
            }
        }
        for (int y = 0; y < 3; y++) plan.put(pos.up(y), Content.block("mangrove_roots").getDefaultState());
        for (int y = 3; y < height; y++) log(pos.up(y), EnumFacing.Axis.Y);
        for (int y = height - 3; y <= height + 1; y++) {
            int radius = y >= height ? 2 : 4;
            for (int x = -radius; x <= radius; x++) for (int z = -radius; z <= radius; z++) {
                if (x * x + z * z > radius * radius + 1 || x == 0 && z == 0 && y < height) continue;
                BlockPos p = pos.add(x, y, z); leaf(p);
                if (y == height - 3 && random.nextInt(8) == 0 && !plan.containsKey(p.down())) plan.put(p.down(), Content.block("mangrove_sapling").getDefaultState().withProperty(MangroveSapling.HANGING, true));
            }
        }
        return true;
    }
    private boolean paleOak(BlockPos pos) {
        int height = 6 + random.nextInt(4);
        for (int i = 0; i < 4; i++) if (!soil(pos.add(i % 2, -1, i / 2), false)) return false;
        for (int y = 0; y < height; y++) for (int i = 0; i < 4; i++) log(pos.add(i % 2, y, i / 2), EnumFacing.Axis.Y);
        for (int y = height - 3; y <= height + 1; y++) {
            int radius = y > height ? 2 : y == height ? 3 : 4;
            for (int x = -radius; x <= radius + 1; x++) for (int z = -radius; z <= radius + 1; z++) {
                if ((x == -radius || x == radius + 1) && (z == -radius || z == radius + 1)) continue;
                leaf(pos.add(x, y, z));
            }
        }
        if(Content.available("pale_hanging_moss"))for(Map.Entry<BlockPos,IBlockState> entry:new ArrayList<>(plan.entrySet())){
            BlockPos leaf=entry.getKey();if(entry.getValue().getBlock()!=Content.block("pale_oak_leaves")||plan.containsKey(leaf.down())||random.nextInt(5)!=0)continue;
            for(int y=1,n=1+random.nextInt(3);y<=n&&world.isAirBlock(leaf.down(y))&&!plan.containsKey(leaf.down(y));y++)plan.put(leaf.down(y),Content.block("pale_hanging_moss").getDefaultState());
        }
        return true;
    }
}
