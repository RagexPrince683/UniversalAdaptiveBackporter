package ganymedes01.etfuturum.world;

import cpw.mods.fml.common.registry.EntityRegistry;
import ganymedes01.etfuturum.EtFuturum;
import ganymedes01.etfuturum.configuration.configs.ConfigEntities;
import ganymedes01.etfuturum.entities.*;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.BiomeDictionary.Type;
import net.minecraftforge.common.util.EnumHelper;

import java.util.*;

public final class MobSpawning {
	public static EnumCreatureType FISH, DOLPHINS, AXOLOTLS, GLOW_SQUID, DROWNED, NAUTILUS;
	private static final List<BiomeGenBase.SpawnListEntry> EMPTY = Collections.emptyList();
	private static List<BiomeGenBase.SpawnListEntry> cod, salmon, tropical, dolphins, axolotls, squid, drowned, nautilus;
	private MobSpawning() { }
	public static void init() {
		FISH = EnumHelper.addCreatureType("EFR_FISH", EntityFish.class, ConfigEntities.fishPopulation, Material.water, true, false);
		DOLPHINS = EnumHelper.addCreatureType("EFR_DOLPHIN", EntityDolphin.class, ConfigEntities.dolphinPopulation, Material.water, true, false);
		AXOLOTLS = EnumHelper.addCreatureType("EFR_AXOLOTL", EntityAxolotl.class, ConfigEntities.axolotlPopulation, Material.water, true, false);
		GLOW_SQUID = EnumHelper.addCreatureType("EFR_GLOW_SQUID", EntityGlowSquid.class, ConfigEntities.glowSquidPopulation, Material.water, true, false);
		DROWNED = EnumHelper.addCreatureType("EFR_DROWNED", EntityDrowned.class, ConfigEntities.drownedPopulation, Material.water, false, false);
		NAUTILUS = EnumHelper.addCreatureType("EFR_NAUTILUS", EntityNautilus.class, ConfigEntities.nautilusPopulation, Material.water, true, false);
		nautilus = ganymedes01.etfuturum.configuration.SpawnSettings.list(new BiomeGenBase.SpawnListEntry(EntityNautilus.class, 5, 1, 2), new BiomeGenBase.SpawnListEntry(EntityZombieNautilus.class, 1, 1, 1));
		cod = ganymedes01.etfuturum.configuration.SpawnSettings.list(new BiomeGenBase.SpawnListEntry(EntityCod.class, 15, 3, 6));
		salmon = ganymedes01.etfuturum.configuration.SpawnSettings.list(new BiomeGenBase.SpawnListEntry(EntitySalmon.class, 15, 1, 5));
		tropical = ganymedes01.etfuturum.configuration.SpawnSettings.list(new BiomeGenBase.SpawnListEntry(EntityTropicalFish.class, 25, 4, 8), new BiomeGenBase.SpawnListEntry(EntityPufferfish.class, 5, 1, 3));
		dolphins = ganymedes01.etfuturum.configuration.SpawnSettings.list(new BiomeGenBase.SpawnListEntry(EntityDolphin.class, 2, 1, 5));
		axolotls = ganymedes01.etfuturum.configuration.SpawnSettings.list(new BiomeGenBase.SpawnListEntry(EntityAxolotl.class, 1, 1, 3));
		squid = ganymedes01.etfuturum.configuration.SpawnSettings.list(new BiomeGenBase.SpawnListEntry(EntityGlowSquid.class, 1, 2, 4));
		drowned = ganymedes01.etfuturum.configuration.SpawnSettings.list(new BiomeGenBase.SpawnListEntry(EntityDrowned.class, 1, 1, 2));
		register(EntityCod.class, "cod", 35, 0xBEA085, 0xC5BCA2);
		register(EntitySalmon.class, "salmon", 36, 0xA00F10, 0x0E8474);
		register(EntityTropicalFish.class, "tropical_fish", 37, 0xEF6915, 0xFFF9F2);
		register(EntityPufferfish.class, "pufferfish", 38, 0xF6B31B, 0x37C3F2);
		register(EntityDolphin.class, "dolphin", 39, 0x223B4D, 0xF9F9F9);
		register(EntityTurtle.class, "turtle", 40, 0xE7E7E7, 0x00AFAF);
		register(EntityDrowned.class, "drowned", 41, 0x8FF1D7, 0x799C65);
		register(EntityPhantom.class, "phantom", 42, 0x435990, 0x88FF00);
		register(EntityAxolotl.class, "axolotl", 43, 0xFBC1E3, 0xA62D74);
		register(EntityGlowSquid.class, "glow_squid", 44, 0x095656, 0x85F1BC);
		ModEntityList.registerEntity(EntityTrident.class, "trident", 45, EtFuturum.instance, 64, 1, true);
		if (ConfigEntities.enableAquaticSpawning) {
			for (BiomeGenBase biome : BiomeDictionary.getBiomesForType(Type.BEACH)) if (!BiomeDictionary.isBiomeOfType(biome, Type.COLD) && !BiomeDictionary.isBiomeOfType(biome, Type.NETHER) && !BiomeDictionary.isBiomeOfType(biome, Type.END)) ganymedes01.etfuturum.configuration.SpawnSettings.addSpawn(EntityTurtle.class, ConfigEntities.turtleSpawnWeight, 2, 5, EnumCreatureType.creature, biome);
		}
	}
	private static void register(Class<? extends net.minecraft.entity.Entity> type, String name, int id, int primary, int secondary) { ModEntityList.registerEntity(type, name, id, EtFuturum.instance, 80, 3, true, primary, secondary); }
	public static boolean isAquatic(EnumCreatureType type) { return type != null && (type == FISH || type == DOLPHINS || type == AXOLOTLS || type == GLOW_SQUID || type == DROWNED || type == NAUTILUS); }
	public static List<BiomeGenBase.SpawnListEntry> list(EnumCreatureType type, BiomeGenBase biome) {
		if (!isAquatic(type)) return null;
		if (BiomeDictionary.isBiomeOfType(biome, Type.NETHER) || BiomeDictionary.isBiomeOfType(biome, Type.END)) return EMPTY;
		boolean ocean = BiomeDictionary.isBiomeOfType(biome, Type.OCEAN), river = BiomeDictionary.isBiomeOfType(biome, Type.RIVER);
		if (type == DROWNED) return ConfigEntities.enableDrownedSpawning && (ocean || river) ? drowned : EMPTY;
		if (!ConfigEntities.enableAquaticSpawning) return EMPTY;
		if (type == NAUTILUS) return ocean && ConfigEntities.enableModernPassiveSpawning ? nautilus : EMPTY;
		if (type == AXOLOTLS) return axolotls;
		if (type == GLOW_SQUID) return squid;
		if (type == DOLPHINS) return ocean && biome.temperature > .1F ? dolphins : EMPTY;
		if (river) return salmon;
		if (!ocean) return EMPTY;
		if (biome.temperature > .8F) return tropical;
		return biome.temperature < .3F ? salmon : cod;
	}
	public static void seagrass(World world, Random random, int chunkX, int chunkZ) {
		if (!ganymedes01.etfuturum.ModBlocks.SEAGRASS.isEnabled() || world.provider.dimensionId != 0) return;
		for (int attempt = 0; attempt < 12; attempt++) {
			int x = chunkX * 16 + random.nextInt(16), z = chunkZ * 16 + random.nextInt(16);
			if (!world.blockExists(x, 63, z)) continue;
			for (int y = 62; y >= 45; y--) {
				if (world.getBlock(x, y, z).getMaterial() != Material.water || world.getBlockMetadata(x, y, z) != 0) continue;
				if (ganymedes01.etfuturum.ModBlocks.SEAGRASS.get().canPlaceBlockAt(world, x, y, z)) { world.setBlock(x, y, z, ganymedes01.etfuturum.ModBlocks.SEAGRASS.get(), 0, 2); break; }
			}
		}
	}
}
