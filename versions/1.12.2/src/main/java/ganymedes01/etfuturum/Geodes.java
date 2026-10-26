package ganymedes01.etfuturum;

import java.util.*;
import ganymedes01.etfuturum.configuration.UabConfiguration;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.*;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.world.*;
import net.minecraft.world.gen.structure.StructureBoundingBox;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

/** Persist candidates until their complete footprint is loaded; generation never loads border chunks. */
public final class Geodes {
    private final boolean enabled = Features.enabled("world.enableAmethystGeodes");
    private final int rarity = Features.integer("amethystRarity", 53, 1, 127), maxY = Features.integer("amethystMaxY", 46, 6, 245);
    private final int[] dimensions = UabConfiguration.get().property("world", "amethystDimensionBlacklist", new int[]{-1, 1}, Property.Type.INTEGER).getIntList();
    private final boolean whitelist = UabConfiguration.get().bool("world", "amethystDimensionBlacklistAsWhitelist", false);
    private final String outer = UabConfiguration.get().property("world", "amethystOuterBlockID", "etfuturum:smooth_basalt", Property.Type.STRING).getString();
    private final String middle = UabConfiguration.get().property("world", "amethystMiddleBlockID", "etfuturum:calcite", Property.Type.STRING).getString();
    private boolean allowed(World world) {
        boolean listed = false; for (int dim : dimensions) if (dim == world.provider.getDimension()) listed = true;
        return enabled && listed == whitelist && (world.getWorldInfo().getTerrainType() != WorldType.FLAT || world.getWorldInfo().getGeneratorOptions().contains("decoration"));
    }
    void generate(World world, Random random, int cx, int cz) {
        if (!allowed(world) || random.nextInt(rarity) != 0 || material(outer) == null || material(middle) == null) return;
        BlockPos pos = new BlockPos((cx << 4) + random.nextInt(16) + 8, 5 + random.nextInt(Math.max(1, maxY - 9)), (cz << 4) + random.nextInt(16) + 8);
        Pending data = Pending.get(world); long key = ChunkPos.asLong(cx, cz);
        if (data.plans.containsKey(key)) return;
        Plan plan = new Plan(key, pos, random.nextLong());
        if (loaded(world, pos)) place(world, new Random(plan.seed), pos);
        else { data.plans.put(key, plan); data.queue.addLast(plan); data.markDirty(); }
    }
    @SubscribeEvent public void tick(TickEvent.WorldTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.world.isRemote || event.world.getTotalWorldTime() % 20 != 0 || !allowed(event.world)) return;
        Pending data = Pending.get(event.world);
        int checks = Math.min(16, data.queue.size());
        while (checks-- > 0) {
            Plan plan = data.queue.removeFirst();
            if (!loaded(event.world, plan.pos)) { data.queue.addLast(plan); continue; }
            place(event.world, new Random(plan.seed), plan.pos);
            data.plans.remove(plan.key); data.markDirty(); break;
        }
    }
    private static boolean loaded(World world, BlockPos pos) {
        if (!world.isAreaLoaded(pos.add(-17, 0, -17), pos.add(17, 0, 17))) return false;
        for (int x = (pos.getX()-17) >> 4; x <= (pos.getX()+17) >> 4; x++) for (int z = (pos.getZ()-17) >> 4; z <= (pos.getZ()+17) >> 4; z++) {
            if (!world.getChunk(x, z).isTerrainPopulated()) return false;
        }
        return true;
    }
    private static IBlockState material(String text) {
        String[] parts = text.split(":");
        if (parts.length < 2 || parts.length > 3) return null;
        try {
            ResourceLocation id = new ResourceLocation(parts[0], parts[1]);
            if (!Block.REGISTRY.containsKey(id) || id.getNamespace().equals(UAB.ID) && !Content.available(id.getPath())) return null;
            int meta = parts.length == 3 ? Integer.parseInt(parts[2]) : 0;
            return meta < 0 || meta > 15 ? null : Block.REGISTRY.getObject(id).getStateFromMeta(meta);
        } catch (IllegalArgumentException invalid) { return null; }
    }
    boolean place(World world, Random random, BlockPos origin) {
        IBlockState outerState = material(outer), middleState = material(middle);
        if (!enabled || world.isRemote || outerState == null || middleState == null || !loaded(world, origin)) return false;
        StructureBoundingBox area = new StructureBoundingBox(origin.add(-16, -16, -16), origin.add(16, 16, 16));
        if (StructureReservations.vanilla(world, area) || OceanStructures.reserved(world, area) || OceanTreasure.reserved(world, area)) return false;
        int points = 3 + random.nextInt(2); GeodeNoise noise = new GeodeNoise(random);
        BlockPos[] centers = new BlockPos[points]; int[] offsets = new int[points]; int invalid = 0;
        double wall = points / 6D, filling = 1 / Math.sqrt(1.7), inner = 1 / Math.sqrt(2.2 + wall), middle = 1 / Math.sqrt(3.2 + wall), outer = 1 / Math.sqrt(4.2 + wall);
        double crackSize = 1 / Math.sqrt(2 + random.nextDouble() / 2 + (points > 3 ? wall : 0));
        boolean crack = random.nextFloat() < .95;
        for (int i = 0; i < points; i++) {
            centers[i] = origin.add(4 + random.nextInt(3), 4 + random.nextInt(3), 4 + random.nextInt(3));
            IBlockState state = world.getBlockState(centers[i]);
            if ((state.getMaterial() != Material.ROCK || !state.isOpaqueCube()) && ++invalid > 1) return false;
            offsets[i] = 1 + random.nextInt(2);
        }
        List<BlockPos> cracks = new ArrayList<>(), budding = new ArrayList<>();
        if (crack) {
            int distance = points * 2 + 1, side = random.nextInt(4);
            int x = side == 0 || side == 2 ? distance : 0, z = side == 1 || side == 2 ? distance : 0;
            for (int y : new int[]{7, 5, 1}) cracks.add(origin.add(x, y, z));
        }
        // Validate every affected block before writing so inventories and existing tile entities survive.
        Map<BlockPos, IBlockState> plan = new LinkedHashMap<>();
        for (BlockPos pos : BlockPos.getAllInBox(origin.add(-16, -16, -16), origin.add(16, 16, 16))) {
            if (pos.getY() < 1 || pos.getY() >= world.getHeight()) continue;
            double perturbation = noise.sample(pos.getX(), pos.getY(), pos.getZ()) * .05, value = 0, split = 0;
            for (int i = 0; i < points; i++) value += 1 / Math.sqrt(pos.distanceSq(centers[i]) + offsets[i]) + perturbation;
            if (value < outer) continue;
            for (BlockPos point : cracks) split += 1 / Math.sqrt(pos.distanceSq(point) + 2) + perturbation;
            IBlockState current = world.getBlockState(pos);
            if (current.getBlockHardness(world, pos) < 0) continue;
            if (current.getBlock().hasTileEntity(current) || world.getTileEntity(pos) != null) return false;
            IBlockState state;
            if (crack && split >= crackSize && value < filling || value >= filling) state = Blocks.AIR.getDefaultState();
            else if (value >= inner) {
                boolean alternate = random.nextFloat() < .083;
                state = Content.block(alternate ? "budding_amethyst" : "amethyst_block").getDefaultState();
                if (alternate && random.nextFloat() < .35) budding.add(pos.toImmutable());
            } else state = value >= middle ? middleState : outerState;
            plan.put(pos.toImmutable(), state);
        }
        for (Map.Entry<BlockPos, IBlockState> entry : plan.entrySet()) world.setBlockState(entry.getKey(), entry.getValue(), 18);
        for (BlockPos pos : budding) {
            Block bud = Content.block(AmethystBud.STAGES[random.nextInt(2) * 2 + (random.nextBoolean() ? 0 : 1)]);
            for (EnumFacing direction : EnumFacing.values()) if (world.isAirBlock(pos.offset(direction))) {
                world.setBlockState(pos.offset(direction), bud.getDefaultState().withProperty(AmethystBud.FACING, direction), 18); break;
            }
        }
        return !plan.isEmpty();
    }
    private static final class Plan {
        final long key, seed; final BlockPos pos;
        Plan(long key, BlockPos pos, long seed) { this.key = key; this.pos = pos; this.seed = seed; }
    }
    public static final class Pending extends WorldSavedData {
        private static final String ID = "etfuturum_pending_geodes";
        final Map<Long, Plan> plans = new LinkedHashMap<>(); final Deque<Plan> queue = new ArrayDeque<>();
        public Pending() { super(ID); }
        public Pending(String name) { super(name); }
        static Pending get(World world) {
            Pending data = (Pending) world.getPerWorldStorage().getOrLoadData(Pending.class, ID);
            if (data == null) { data = new Pending(); world.getPerWorldStorage().setData(ID, data); } return data;
        }
        @Override public void readFromNBT(NBTTagCompound tag) {
            plans.clear(); queue.clear(); NBTTagList list = tag.getTagList("Candidates", 10);
            for (int i = 0; i < list.tagCount(); i++) {
                NBTTagCompound entry = list.getCompoundTagAt(i); Plan plan = new Plan(entry.getLong("Chunk"), BlockPos.fromLong(entry.getLong("Position")), entry.getLong("Seed"));
                if (!plans.containsKey(plan.key)) { plans.put(plan.key, plan); queue.addLast(plan); }
            }
        }
        @Override public NBTTagCompound writeToNBT(NBTTagCompound tag) {
            NBTTagList list = new NBTTagList();
            for (Plan plan : plans.values()) { NBTTagCompound entry = new NBTTagCompound(); entry.setLong("Chunk", plan.key); entry.setLong("Position", plan.pos.toLong()); entry.setLong("Seed", plan.seed); list.appendTag(entry); }
            tag.setTag("Candidates", list); return tag;
        }
    }
}
