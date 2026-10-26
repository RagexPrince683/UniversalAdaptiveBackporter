package ganymedes01.etfuturum.world;

import cpw.mods.fml.common.eventhandler.Event.Result;
import ganymedes01.etfuturum.configuration.configs.ConfigEntities;
import ganymedes01.etfuturum.entities.*;
import net.minecraft.entity.*;
import net.minecraft.entity.passive.EntitySquid;
import net.minecraft.util.*;
import net.minecraft.world.*;
import java.util.*;

/** Loaded-entity lifecycle index. Natural admission alone is capped; saved/bucket/command entities are never rejected. */
public final class AquaticPopulation {
	public static final String[] NAMES = {"cod", "salmon", "tropical_fish", "pufferfish", "dolphin", "axolotl", "glow_squid", "nautilus", "drowned", "guardian", "squid"};
	private static final Map<World, AquaticPopulation> WORLDS = new IdentityHashMap<>();
	private final IdentityHashMap<EntityLiving, Boolean> loaded = new IdentityHashMap<>();
	private final int[] counts = new int[NAMES.length];
	private final long[] attempts = new long[NAMES.length], successful = new long[NAMES.length], habitat = new long[NAMES.length], global = new long[NAMES.length], local = new long[NAMES.length], failed = new long[NAMES.length];
	private int eligibleChunks = 256;
	private long preparedTick = Long.MIN_VALUE;
	private AquaticPopulation() { }
	public static AquaticPopulation get(World world) { AquaticPopulation population = WORLDS.get(world); if (population == null) { population = new AquaticPopulation(); WORLDS.put(world, population); } return population; }
	public static void unload(World world) { WORLDS.remove(world); }
	public static int species(Entity entity) {
		if (entity instanceof EntityFish fish) return fish.fishType();
		if (entity instanceof EntityDolphin) return 4;
		if (entity instanceof EntityAxolotl) return 5;
		if (entity instanceof EntityGlowSquid) return 6;
		if (entity instanceof EntityNautilus) return 7;
		if (entity instanceof EntityDrowned) return 8;
		if (entity instanceof EntityGuardian) return 9;
		return entity.getClass() == EntitySquid.class ? 10 : -1;
	}
	private static boolean counted(EntityLiving entity) { return !entity.isDead && !entity.isNoDespawnRequired() && !entity.hasCustomNameTag() && (!(entity instanceof EntityNautilus nautilus) || !nautilus.isTamed()) && (!(entity instanceof EntityGuardian guardian) || !guardian.isElder()); }
	public static void added(Entity entity) {
		if (entity.worldObj.isRemote || !(entity instanceof EntityLiving living) || species(entity) < 0) return;
		AquaticPopulation population = get(entity.worldObj);
		if (!population.loaded.containsKey(living)) { boolean count = counted(living); population.loaded.put(living, count); if (count) population.counts[species(entity)]++; }
	}
	public static void removed(Entity entity) {
		AquaticPopulation population = WORLDS.get(entity.worldObj);
		if (population != null && entity instanceof EntityLiving living && Boolean.TRUE.equals(population.loaded.remove(living))) population.counts[species(entity)]--;
	}
	public static void updated(Entity entity) {
		if (entity.worldObj.isRemote || !(entity instanceof EntityLiving living) || species(entity) < 0) return;
		AquaticPopulation population = WORLDS.get(entity.worldObj);
		if (population == null) return;
		Boolean old = population.loaded.get(living);
		if (old != null) { boolean count = counted(living); if (old != count) { population.loaded.put(living, count); population.counts[species(entity)] += count ? 1 : -1; } }
	}
	public void prepare(World world, Map<ChunkCoordIntPair, Boolean> eligible) {
		if (preparedTick == world.getTotalWorldTime()) return;
		preparedTick = world.getTotalWorldTime(); eligibleChunks = 0;
		for (ChunkCoordIntPair position : eligible.keySet()) if (world.getChunkProvider().chunkExists(position.chunkXPos, position.chunkZPos)) eligibleChunks++;
		eligibleChunks = Math.max(1, eligibleChunks);
	}
	public static boolean indexed(EnumCreatureType type) { return MobSpawning.isAquatic(type) || type != null && type == ganymedes01.etfuturum.world.structure.MonumentSpawning.GUARDIANS; }
	public int count(EnumCreatureType type) {
		if (type == MobSpawning.FISH) return counts[0] + counts[1] + counts[2] + counts[3];
		if (type == MobSpawning.DOLPHINS) return counts[4]; if (type == MobSpawning.AXOLOTLS) return counts[5];
		if (type == MobSpawning.GLOW_SQUID) return counts[6]; if (type == MobSpawning.NAUTILUS) return counts[7];
		if (type == MobSpawning.DROWNED) return counts[8]; if (type == EnumCreatureType.waterCreature) return counts[10];
		return counts[9];
	}
	private int scaled(int maximum, int minimum) { return Math.max(Math.min(maximum, minimum), (int) Math.min(Integer.MAX_VALUE, (long) maximum * eligibleChunks / 256)); }
	public int nativeCount(EnumCreatureType type, int nativeChunks) {
		int count = count(type), minimum = type == MobSpawning.FISH ? 8 : type == MobSpawning.DOLPHINS ? 5 : type == MobSpawning.AXOLOTLS ? 3 : type == MobSpawning.GLOW_SQUID ? 4 : type == MobSpawning.NAUTILUS || type == MobSpawning.DROWNED ? 2 : 4;
		boolean hostile = type == MobSpawning.DROWNED || type == ganymedes01.etfuturum.world.structure.MonumentSpawning.GUARDIANS;
		int shared = hostile ? counts[8] + counts[9] : counts[0] + counts[1] + counts[2] + counts[3] + counts[4] + counts[5] + counts[6] + counts[7];
		if (count >= scaled(type.getMaxNumberOfCreature(), minimum) || shared >= scaled(hostile ? ConfigEntities.hostileAquaticPopulation : ConfigEntities.aquaticPopulation, hostile ? 4 : 12)) {
			// The native loop uses <= and checks only once per category. Close full categories
			// before it walks every eligible chunk; admit() still rechecks every successful candidate.
			return Math.max(count, type.getMaxNumberOfCreature() * nativeChunks / 256 + 1);
		}
		return count;
	}
	public boolean admit(EntityLiving entity, Result eventResult) {
		int species = species(entity); attempts[species]++;
		World world = entity.worldObj; AxisAlignedBB box = entity.boundingBox;
		// Collision queries must not reach an absent neighboring chunk at the admission boundary.
		for (int cx = MathHelper.floor_double(box.minX) >> 4; cx <= MathHelper.floor_double(box.maxX) >> 4; cx++) for (int cz = MathHelper.floor_double(box.minZ) >> 4; cz <= MathHelper.floor_double(box.maxZ) >> 4; cz++) if (!world.getChunkProvider().chunkExists(cx, cz)) { habitat[species]++; return false; }
		if (eventResult == Result.DENY || eventResult == Result.DEFAULT && !entity.getCanSpawnHere()) { habitat[species]++; return false; }
		int[] maximums = {ConfigEntities.codPopulation, ConfigEntities.salmonPopulation, ConfigEntities.tropicalFishPopulation, ConfigEntities.pufferfishPopulation, ConfigEntities.dolphinPopulation, ConfigEntities.axolotlPopulation, ConfigEntities.glowSquidPopulation, ConfigEntities.nautilusPopulation, ConfigEntities.drownedPopulation, ConfigEntities.guardianPopulation, ConfigEntities.squidPopulation};
		int[] schools = {6, 5, 8, 3, 5, 3, 4, 2, 2, 4, 4};
		boolean hostile = species == 8 || species == 9;
		int shared = 0;
		for (int i = 0; i < 10; i++) if ((i == 8 || i == 9) == hostile) shared += counts[i];
		if (counts[species] >= scaled(maximums[species], schools[species]) || species < 4 && count(MobSpawning.FISH) >= scaled(ConfigEntities.fishPopulation, 8)
				|| species < 10 && shared >= scaled(hostile ? ConfigEntities.hostileAquaticPopulation : ConfigEntities.aquaticPopulation, hostile ? 4 : 12)) { global[species]++; return false; }
		int nearby = 0, same = 0;
		for (Object object : world.getEntitiesWithinAABB(EntityLiving.class, box.expand(24, 12, 24))) {
			EntityLiving other = (EntityLiving) object; int kind = species(other);
			if (kind < 0 || !counted(other)) continue;
			if (kind == species) same++;
			if (kind < 10 && (kind == 8 || kind == 9) == hostile) nearby++;
		}
		if (same >= Math.min(maximums[species], schools[species] * 2) || species < 10 && nearby >= ConfigEntities.aquaticLocalPopulation) { local[species]++; return false; }
		return true;
	}
	public void spawned(EntityLiving entity, boolean success) { if (success) successful[species(entity)]++; else failed[species(entity)]++; }
	public static boolean managed(Entity entity) { int species = species(entity); return species >= 0 && (species != 10 || ConfigEntities.limitVanillaSquid); }
	public List<String> report(boolean reset) {
		List<String> lines = new ArrayList<>(); lines.add("Aquatic loaded natural counts; eligible loaded chunks=" + eligibleChunks + "; attempts include habitat/event and population rejections.");
		for (int i = 0; i < NAMES.length; i++) { lines.add(NAMES[i] + " count=" + counts[i] + " attempts=" + attempts[i] + " success=" + successful[i] + " habitat=" + habitat[i] + " cap=" + global[i] + " density=" + local[i] + " failedJoin=" + failed[i]); if (reset) attempts[i] = successful[i] = habitat[i] = global[i] = local[i] = failed[i] = 0; }
		return lines;
	}
}
