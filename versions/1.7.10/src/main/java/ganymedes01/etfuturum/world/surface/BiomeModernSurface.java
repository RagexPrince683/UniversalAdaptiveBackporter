package ganymedes01.etfuturum.world.surface;

import ganymedes01.etfuturum.ModBlocks;
import ganymedes01.etfuturum.configuration.configs.ConfigWorld;
import ganymedes01.etfuturum.world.generate.decorate.*;
import net.minecraft.entity.passive.*;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.gen.feature.*;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.BiomeDictionary.Type;
import net.minecraftforge.event.terraingen.TerrainGen;
import net.minecraftforge.event.terraingen.DecorateBiomeEvent;

import java.util.Random;

public class BiomeModernSurface extends BiomeGenBase {
	public enum Kind { CHERRY, MANGROVE, PALE, MEADOW, GROVE, SLOPES, JAGGED, FROZEN, STONY, BAMBOO }
	private final Kind kind;
	private final WorldGenAbstractTree tree;
	public BiomeModernSurface(int id, Kind kind) {
		super(id);
		this.kind = kind;
		theBiomeDecorator.treesPerChunk = -999;
		theBiomeDecorator.grassPerChunk = 5;
		theBiomeDecorator.flowersPerChunk = 0;
		setTemperatureRainfall(0.5F, 0.7F);
		setHeight(new Height(0.8F, 0.3F));
		switch (kind) {
			case CHERRY:
				tree = new WorldGenCherryTrees(false);
				setTemperatureRainfall(0.5F, 0.8F);
				spawnableCreatureList.clear();
				ganymedes01.etfuturum.configuration.SpawnSettings.add(spawnableCreatureList, EntityPig.class, 1, 1, 2);
				ganymedes01.etfuturum.configuration.SpawnSettings.add(spawnableCreatureList, EntitySheep.class, 1, 1, 2);
				BiomeDictionary.registerBiomeType(this, Type.FOREST, Type.MOUNTAIN);
				break;
			case MANGROVE:
				tree = new WorldGenMangroveTrees(false);
				setHeight(new Height(-0.15F, 0.1F));
				setTemperatureRainfall(0.8F, 0.9F);
				topBlock = ModBlocks.MUD.get();
				fillerBlock = Blocks.dirt;
				waterColorMultiplier = 0x3A7A6A;
				spawnableCreatureList.clear();
				BiomeDictionary.registerBiomeType(this, Type.SWAMP, Type.WET, Type.HOT);
				break;
			case PALE:
				tree = new WorldGenPaleOak(false, true);
				theBiomeDecorator.grassPerChunk = 0;
				setHeight(new Height(0.1F, 0.2F));
				setTemperatureRainfall(0.7F, 0.8F);
				spawnableCreatureList.clear();
				BiomeDictionary.registerBiomeType(this, Type.FOREST, Type.DENSE);
				break;
			case MEADOW:
				tree = new WorldGenTrees(false);
				theBiomeDecorator.flowersPerChunk = 12;
				BiomeDictionary.registerBiomeType(this, Type.PLAINS, Type.MOUNTAIN);
				spawnableCreatureList.clear();
				ganymedes01.etfuturum.configuration.SpawnSettings.add(spawnableCreatureList, EntitySheep.class, 2, 2, 6);
				ganymedes01.etfuturum.configuration.SpawnSettings.add(spawnableCreatureList, EntityHorse.class, 1, 1, 2);
				break;
			case BAMBOO:
				tree = new WorldGenMegaJungle(false, 10, 20, 3, 3);
				setHeight(new Height(0.1F, 0.2F));
				setTemperatureRainfall(0.95F, 0.9F);
				BiomeDictionary.registerBiomeType(this, Type.JUNGLE, Type.HOT, Type.WET);
				break;
			default:
				tree = new WorldGenTaiga2(false);
				setHeight(new Height(kind == Kind.SLOPES ? 1.0F : 1.3F, kind == Kind.GROVE ? 0.3F : 0.5F));
				setTemperatureRainfall(kind == Kind.STONY ? 1.0F : -0.2F, 0.4F);
				if (kind != Kind.STONY) { setEnableSnow(); BiomeDictionary.registerBiomeType(this, Type.SNOWY, Type.COLD); }
				if (kind == Kind.STONY || kind == Kind.JAGGED || kind == Kind.FROZEN) topBlock = fillerBlock = kind == Kind.FROZEN ? Blocks.packed_ice : Blocks.stone;
				BiomeDictionary.registerBiomeType(this, Type.MOUNTAIN);
				if (kind == Kind.GROVE) BiomeDictionary.registerBiomeType(this, Type.CONIFEROUS, Type.FOREST);
				spawnableCreatureList.clear();
		}
	}
	@Override public WorldGenAbstractTree func_150567_a(Random random) { return tree; }
	public void finishSpawns() {
		spawnableCreatureList.clear();
		spawnableWaterCreatureList.clear();
		switch (kind) {
			case CHERRY:
				ganymedes01.etfuturum.configuration.SpawnSettings.add(spawnableCreatureList, EntityPig.class, 1, 1, 2); ganymedes01.etfuturum.configuration.SpawnSettings.add(spawnableCreatureList, EntitySheep.class, 1, 1, 2); rabbit(); break;
			case MANGROVE:
				if (ganymedes01.etfuturum.configuration.configs.ConfigEntities.enableFrogs) ganymedes01.etfuturum.configuration.SpawnSettings.add(spawnableCreatureList, ganymedes01.etfuturum.entities.EntityFrog.class, 1, 2, 5); break;
			case MEADOW:
				ganymedes01.etfuturum.configuration.SpawnSettings.add(spawnableCreatureList, EntitySheep.class, 2, 2, 6); ganymedes01.etfuturum.configuration.SpawnSettings.add(spawnableCreatureList, EntityHorse.class, 1, 1, 2); rabbit(); break;
			case GROVE:
				ganymedes01.etfuturum.configuration.SpawnSettings.add(spawnableCreatureList, EntityWolf.class, 8, 4, 4); rabbit();
				if (ganymedes01.etfuturum.configuration.configs.ConfigEntities.enableFoxes) ganymedes01.etfuturum.configuration.SpawnSettings.add(spawnableCreatureList, ganymedes01.etfuturum.entities.EntityFox.class, 8, 2, 4); break;
			case SLOPES: rabbit(); goat(); break;
			case JAGGED: case FROZEN: goat(); break;
			case BAMBOO: ganymedes01.etfuturum.configuration.SpawnSettings.add(spawnableCreatureList, EntityChicken.class, 10, 4, 4); ganymedes01.etfuturum.configuration.SpawnSettings.add(spawnableCreatureList, EntityOcelot.class, 2, 1, 1); break;
			default: break;
		}
	}
	private void rabbit() { if (ganymedes01.etfuturum.configuration.configs.ConfigEntities.enableRabbit) ganymedes01.etfuturum.configuration.SpawnSettings.add(spawnableCreatureList, ganymedes01.etfuturum.entities.EntityRabbit.class, 2, 2, 3); }
	private void goat() { if (ganymedes01.etfuturum.configuration.configs.ConfigEntities.enableGoats) ganymedes01.etfuturum.configuration.SpawnSettings.add(spawnableCreatureList, ganymedes01.etfuturum.entities.EntityGoat.class, 5, 1, 3); }
	@Override public int getBiomeGrassColor(int x, int y, int z) { return kind == Kind.PALE ? 0x778272 : kind == Kind.CHERRY ? 0xB6DB61 : kind == Kind.MANGROVE ? 0x6A7039 : super.getBiomeGrassColor(x, y, z); }
	@Override public int getBiomeFoliageColor(int x, int y, int z) { return kind == Kind.PALE ? 0x878D76 : kind == Kind.MANGROVE ? 0x8DB127 : super.getBiomeFoliageColor(x, y, z); }
	@Override public void decorate(World world, Random random, int chunkX, int chunkZ) {
		super.decorate(world, random, chunkX, chunkZ);
		boolean treesAllowed = TerrainGen.decorate(world, random, chunkX, chunkZ, DecorateBiomeEvent.Decorate.EventType.TREE);
		int count = !treesAllowed ? 0 : kind == Kind.PALE ? 8 : kind == Kind.MANGROVE ? 7 : kind == Kind.CHERRY ? 5 : kind == Kind.GROVE || kind == Kind.BAMBOO ? 6 : kind == Kind.MEADOW && random.nextInt(100) == 0 ? 1 : 0;
		for (int i = 0; i < count; i++) {
			int x = chunkX + 8 + random.nextInt(16), z = chunkZ + 8 + random.nextInt(16);
			if (!world.blockExists(x, 0, z) || world.getBiomeGenForCoords(x, z) != this) continue;
			int y = world.getHeightValue(x, z);
			if (tree.generate(world, random, x, y, z)) {
				if ((kind == Kind.MEADOW || kind == Kind.CHERRY && random.nextInt(20) == 0) && ModBlocks.BEE_NEST.isEnabled()) ganymedes01.etfuturum.core.handlers.WorldEventHandler.tryPlaceBeeNest(world, x, y, z, random, 3);
			}
		}
		if (kind == Kind.BAMBOO && ConfigWorld.bambooWorldgen && TerrainGen.decorate(world, random, chunkX, chunkZ, DecorateBiomeEvent.Decorate.EventType.GRASS)) {
			WorldGenBamboo bamboo = new WorldGenBamboo(ModBlocks.BAMBOO.get());
			for (int i = 0; i < 64; i++) {
				int x = chunkX + 8 + random.nextInt(16), z = chunkZ + 8 + random.nextInt(16);
				if (world.blockExists(x, 0, z)) bamboo.generate(world, random, x, world.getHeightValue(x, z) - 1, z);
			}
		}
		if (kind == Kind.CHERRY && ModBlocks.PINK_PETALS.isEnabled() && TerrainGen.decorate(world, random, chunkX, chunkZ, DecorateBiomeEvent.Decorate.EventType.FLOWERS)) new WorldGenPinkPetals(ModBlocks.PINK_PETALS.get()).generate(world, random, chunkX + 16, world.getHeightValue(chunkX + 16, chunkZ + 16), chunkZ + 16);
	}
}
