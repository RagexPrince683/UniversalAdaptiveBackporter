package ganymedes01.etfuturum.world.structure;

import ganymedes01.etfuturum.configuration.UabConfiguration;
import ganymedes01.etfuturum.core.utils.Logger;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.*;
import net.minecraft.world.biome.BiomeGenBase;
import java.util.List;
import java.util.Random;

/** Locks the opt-in distribution and its biome-adjusted positions into each world's saved data. */
public final class StrongholdDistribution extends WorldSavedData {
    private static final String KEY = "uab_stronghold_distribution";
    private boolean modern;
    private int[] positions = new int[0];
    public StrongholdDistribution() { super(KEY); }
    public StrongholdDistribution(String key) { super(key); }
    public static StrongholdDistribution get(World world) {
        StrongholdDistribution data = (StrongholdDistribution) world.perWorldStorage.loadData(StrongholdDistribution.class, KEY);
        if (data == null) {
            data = new StrongholdDistribution();
            boolean requested = UabConfiguration.get().bool("world", "useModernStrongholds", false);
            net.minecraft.world.gen.structure.MapGenStructureData strongholds = (net.minecraft.world.gen.structure.MapGenStructureData)
                    world.perWorldStorage.loadData(net.minecraft.world.gen.structure.MapGenStructureData.class, "Stronghold");
            // MapGenStructure creates an empty record before its first candidate check, including in a new world.
            // Anvil's chunk-existence queries may create empty region files during fresh-world generation.
            // Saved level metadata, rather than the presence of those files, identifies an existing world.
            boolean existing = world.getTotalWorldTime() > 0 || world.getSaveHandler().loadWorldInfo() != null
                    || strongholds != null && !strongholds.func_143041_a().hasNoTags();
            data.modern = world.provider.dimensionId == 0 && requested && !existing;
            if (requested && existing) Logger.warn("[UAB] Existing world retains its legacy stronghold distribution. Modern rings apply only to new worlds.");
            world.perWorldStorage.setData(KEY, data);
            data.markDirty();
        }
        return data;
    }
    public boolean modern() { return modern; }
    public ChunkCoordIntPair[] positions(World world, List<BiomeGenBase> biomes) {
        if (positions.length == 0) {
            positions = new int[256];
            Random random = new Random(world.getSeed());
            double angle = random.nextDouble() * Math.PI * 2;
            int ring = 0, inRing = 0, spread = 3;
            for (int i = 0; i < 128; i++) {
                double radius = 128 + 192 * ring + (random.nextDouble() - .5) * 80;
                int x = (int) Math.round(Math.cos(angle) * radius), z = (int) Math.round(Math.sin(angle) * radius);
                ChunkPosition biome = world.getWorldChunkManager().findBiomePosition((x << 4) + 8, (z << 4) + 8, 112, biomes, random);
                if (biome != null) { x = biome.chunkPosX >> 4; z = biome.chunkPosZ >> 4; }
                positions[i * 2] = x; positions[i * 2 + 1] = z;
                angle += Math.PI * 2 / spread;
                if (++inRing == spread) {
                    ring++; inRing = 0; spread += 2 * spread / (ring + 1);
                    spread = Math.min(spread, 128 - i);
                    angle += random.nextDouble() * Math.PI * 2;
                }
            }
            markDirty();
        }
        ChunkCoordIntPair[] result = new ChunkCoordIntPair[positions.length / 2];
        for (int i = 0; i < result.length; i++) result[i] = new ChunkCoordIntPair(positions[i * 2], positions[i * 2 + 1]);
        return result;
    }
    @Override public void writeToNBT(NBTTagCompound tag) { tag.setBoolean("Modern", modern); tag.setIntArray("Positions", positions); }
    @Override public void readFromNBT(NBTTagCompound tag) {
        modern = tag.getBoolean("Modern"); positions = tag.getIntArray("Positions");
        if (positions.length != 0 && positions.length != 256) throw new IllegalStateException("Invalid saved UAB stronghold positions; refusing to relocate strongholds");
    }
}
