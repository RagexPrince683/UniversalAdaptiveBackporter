package ganymedes01.etfuturum;

import java.util.*;
import ganymedes01.etfuturum.world.GenerationSeeds;
import net.minecraft.block.material.Material;
import net.minecraft.nbt.*;
import net.minecraft.util.math.*;
import net.minecraft.world.World;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraft.world.gen.structure.StructureBoundingBox;
import net.minecraftforge.common.BiomeDictionary;

/** Saved plans place each affected new chunk once, retaining consumed loot across restarts. */
public final class OceanStructures extends WorldSavedData {
    private static final String ID = "etfuturum_ocean_sites";
    private static final String[] WRECKS = {"rightsideup_backhalf", "rightsideup_fronthalf", "rightsideup_full", "sideways_backhalf", "sideways_fronthalf", "sideways_full", "upsidedown_backhalf", "upsidedown_fronthalf", "upsidedown_full", "with_mast"};
    private final List<Plan> plans = new ArrayList<>();
    private final Map<Long, List<Plan>> byChunk = new HashMap<>();
    public OceanStructures() { super(ID); }
    public OceanStructures(String id) { super(id); }
    private static OceanStructures data(World world) {
        OceanStructures value = (OceanStructures) world.getPerWorldStorage().getOrLoadData(OceanStructures.class, ID);
        if (value == null) { value = new OceanStructures(); world.getPerWorldStorage().setData(ID, value); }
        return value;
    }
    private void index(Plan plan) {
        plans.add(plan);
        OceanTemplate template = OceanTemplate.get(plan.name);
        for (int x = plan.pos.getX() >> 4; x <= (plan.pos.getX() + template.width(plan.rotation) - 1) >> 4; x++)
            for (int z = plan.pos.getZ() >> 4; z <= (plan.pos.getZ() + template.depth(plan.rotation) - 1) >> 4; z++) byChunk.computeIfAbsent(ChunkPos.asLong(x, z), key -> new ArrayList<>()).add(plan);
    }
    static void populate(World world, int cx, int cz) {
        if (!world.getWorldInfo().isMapFeaturesEnabled()) return;
        OceanStructures saved = data(world);
        for (int kind = 0; kind < 2; kind++) {
            String feature = kind == 0 ? "content.shipwrecks" : "content.ocean_ruins";
            if (!Features.enabled(feature)) continue;
            int spacing = kind == 0 ? 24 : 20; long salt = kind == 0 ? 165745295 : 14357621;
            int[] candidate = GenerationSeeds.candidate(world.getSeed(), cx, cz, spacing, 8, salt);
            net.minecraft.world.biome.Biome biome = world.getBiome(new BlockPos((cx << 4) + 8, 0, (cz << 4) + 8));
            if (candidate[0] != cx || candidate[1] != cz || !BiomeDictionary.hasType(biome, BiomeDictionary.Type.OCEAN) && !(kind == 0 && BiomeDictionary.hasType(biome, BiomeDictionary.Type.BEACH))) continue;
            Random random = GenerationSeeds.chunk(world.getSeed(), Math.floorDiv(cx, spacing), Math.floorDiv(cz, spacing), salt);
            random.nextInt(spacing - 8); random.nextInt(spacing - 8);
            String name;
            if (kind == 0) name = "shipwreck/" + WRECKS[random.nextInt(WRECKS.length)] + (random.nextBoolean() ? "_degraded" : "");
            else {
                boolean big = random.nextFloat() < .3F, warm = biome.getDefaultTemperature() > .8F;
                int index = big ? warm ? 4 + random.nextInt(4) : new int[]{1, 2, 3, 8}[random.nextInt(4)] : 1 + random.nextInt(8);
                name = "underwater_ruin/" + (big ? "big_" : "") + (warm ? "warm_" : "brick_") + index;
            }
            saved.admit(world, name, cx, cz, random.nextInt(4), random.nextLong());
        }
        long key = ChunkPos.asLong(cx, cz);
        List<Plan> pending = saved.byChunk.get(key);
        if (pending == null) return;
        for (Plan plan : pending) if (!plan.completed.contains(key)) {
            if (!Features.enabled(plan.name.startsWith("shipwreck/") ? "content.shipwrecks" : "content.ocean_ruins")) continue;
            OceanTemplate template = OceanTemplate.get(plan.name);
            if (!template.available()) continue;
            if (plan.name.contains("brick_") && (!OceanTemplate.get(plan.name.replace("brick_", "mossy_")).available() || !OceanTemplate.get(plan.name.replace("brick_", "cracked_")).available())) continue;
            template.place(world, plan.name, plan.pos, plan.rotation, cx, cz, plan.seed, plan.name.startsWith("shipwreck/") ? 1 : .9F);
            if (plan.name.contains("brick_")) for (String kind : new String[]{"mossy_", "cracked_"}) {
                String overlay = plan.name.replace("brick_", kind);
                OceanTemplate.get(overlay).place(world, overlay, plan.pos, plan.rotation, cx, cz, plan.seed, .35F);
            }
            plan.completed.add(key); saved.markDirty();
        }
    }
    private void admit(World world, String name, int cx, int cz, int rotation, long seed) {
        OceanTemplate template = OceanTemplate.get(name);
        if (!template.available()) return;
        int x = cx << 4, z = cz << 4, maxX = x + template.width(rotation) - 1, maxZ = z + template.depth(rotation) - 1;
        for (int px = cx; px <= maxX >> 4; px++) for (int pz = cz; pz <= maxZ >> 4; pz++) {
            if (!world.getChunkProvider().isChunkGeneratedAt(px, pz) || !world.isBlockLoaded(new BlockPos(px << 4, 0, pz << 4)) || byChunk.containsKey(ChunkPos.asLong(px, pz))) return;
            if ((px != cx || pz != cz) && world.getChunk(px, pz).isTerrainPopulated()) return;
        }
        int min = 255, max = 0;
        for (int px : new int[]{x + 1, maxX - 1}) for (int pz : new int[]{z + 1, maxZ - 1}) {
            BlockPos p = new BlockPos(px, world.getSeaLevel(), pz);
            while (p.getY() > 20 && (world.getBlockState(p).getMaterial() == Material.WATER || !world.getBlockState(p).getMaterial().isSolid())) p = p.down();
            min = Math.min(min, p.getY()); max = Math.max(max, p.getY());
        }
        if (min < 30 || max > world.getSeaLevel() + 5 || max - min > 5) return;
        int y = min - (name.startsWith("shipwreck/") ? 0 : 1);
        StructureBoundingBox box = new StructureBoundingBox(x, y, z, maxX, y + template.sy - 1, maxZ);
        if (StructureReservations.vanilla(world, box) || OceanTreasure.reserved(world, box)) return;
        Plan plan = new Plan(); plan.name = name; plan.pos = new BlockPos(x, y, z); plan.rotation = rotation; plan.seed = seed;
        index(plan); markDirty();
    }
    static BlockPos nearest(World world, BlockPos origin, String type) {
        BlockPos best = null; double distance = Double.MAX_VALUE;
        for (Plan plan : data(world).plans) if (plan.name.startsWith(type + "/") && plan.pos.distanceSq(origin) < distance) { best = plan.pos; distance = best.distanceSq(origin); }
        return best;
    }
    static boolean reserved(World world, StructureBoundingBox box) {
        for (Plan plan : data(world).plans) {
            OceanTemplate template = OceanTemplate.get(plan.name);
            if (new StructureBoundingBox(plan.pos.getX(), plan.pos.getY(), plan.pos.getZ(), plan.pos.getX() + template.width(plan.rotation) - 1, plan.pos.getY() + template.sy - 1, plan.pos.getZ() + template.depth(plan.rotation) - 1).intersectsWith(box)) return true;
        }
        return false;
    }
    @Override public void readFromNBT(NBTTagCompound tag) {
        plans.clear(); byChunk.clear(); NBTTagList list = tag.getTagList("Sites", 10);
        for (int i = 0; i < list.tagCount(); i++) {
            NBTTagCompound entry = list.getCompoundTagAt(i); Plan plan = new Plan(); plan.name = entry.getString("Template");
            plan.pos = new BlockPos(entry.getInteger("X"), entry.getInteger("Y"), entry.getInteger("Z")); plan.rotation = entry.getInteger("Rotation") & 3; plan.seed = entry.getLong("Seed");
            NBTTagList done = entry.getTagList("Completed", 4); for (int n = 0; n < done.tagCount(); n++) plan.completed.add(((NBTTagLong) done.get(n)).getLong());
            index(plan);
        }
    }
    @Override public NBTTagCompound writeToNBT(NBTTagCompound tag) {
        NBTTagList list = new NBTTagList();
        for (Plan plan : plans) {
            NBTTagCompound entry = new NBTTagCompound(); entry.setString("Template", plan.name); entry.setInteger("X", plan.pos.getX()); entry.setInteger("Y", plan.pos.getY()); entry.setInteger("Z", plan.pos.getZ()); entry.setInteger("Rotation", plan.rotation); entry.setLong("Seed", plan.seed);
            NBTTagList done = new NBTTagList(); for (long key : plan.completed) done.appendTag(new NBTTagLong(key)); entry.setTag("Completed", done); list.appendTag(entry);
        }
        tag.setTag("Sites", list); return tag;
    }
    private static final class Plan { String name; BlockPos pos; int rotation; long seed; final Set<Long> completed = new HashSet<>(); }
}
