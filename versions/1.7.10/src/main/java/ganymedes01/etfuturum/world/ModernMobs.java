package ganymedes01.etfuturum.world;
import cpw.mods.fml.common.registry.*;
import ganymedes01.etfuturum.*;
import ganymedes01.etfuturum.entities.*;
import ganymedes01.etfuturum.configuration.configs.ConfigEntities;
import net.minecraft.entity.*;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.BiomeDictionary.Type;
import java.util.*;
public final class ModernMobs {
	private ModernMobs() { }
	private static void mob(Class<? extends Entity> type, String name, int id, int color, int spots) { ModEntityList.registerEntity(type, name, id, EtFuturum.instance, 96, 3, true, color, spots); }
	private static void projectile(Class<? extends Entity> type, String name, int id) { ModEntityList.registerEntity(type, name, id, EtFuturum.instance, 96, 1, true); }
	public static void init() {
		mob(EntityPillager.class, "pillager", 46, 0x532F36, 0x959B9B); mob(EntityVindicator.class, "vindicator", 47, 0x275E61, 0x959B9B); mob(EntityEvoker.class, "evoker", 48, 0x1E1C1A, 0x959B9B); mob(EntityVex.class, "vex", 49, 0x7A90A4, 0xE8EDF1); mob(EntityIllusioner.class, "illusioner", 50, 0x263D63, 0x959B9B); mob(EntityRavager.class, "ravager", 51, 0x757470, 0x5B5049); projectile(EntityEvokerFangs.class, "evoker_fangs", 52);
		mob(EntityBogged.class, "bogged", 53, 0x8A9C72, 0xA24434); mob(EntityParched.class, "parched", 54, 0xCCB36C, 0x6B5840); projectile(EntityWindCharge.class, "wind_charge", 55); mob(EntityBreeze.class, "breeze", 56, 0xA4BCD8, 0xAF94DF);
		mob(EntityCat.class, "cat", 57, 0xEFC88E, 0x95704A); mob(EntityPanda.class, "panda", 58, 0xE7E7E7, 0x1B1B22); mob(EntityLlama.class, "llama", 59, 0xC09E7D, 0x995F40); mob(EntityTraderLlama.class, "trader_llama", 60, 0xEAA430, 0x456296); mob(EntityWanderingTrader.class, "wandering_trader", 61, 0x456296, 0xEAA430); mob(EntityAllay.class, "allay", 62, 0x22AAE2, 0x99F2FF); mob(EntityWarden.class, "warden", 63, 0x0F4649, 0xC6DA9D);
		mob(EntityCamel.class, "camel", 64, 0xD7B17D, 0xA06A3F); mob(EntityCamelHusk.class, "camel_husk", 65, 0xA28B50, 0x5A573F); mob(EntitySniffer.class, "sniffer", 66, 0x871E1D, 0x25AB70); mob(EntityArmadillo.class, "armadillo", 67, 0xAD716D, 0x734942); mob(EntityCopperGolem.class, "copper_golem", 68, 0xCC784E, 0x67A47C); mob(EntityHappyGhast.class, "happy_ghast", 69, 0xF4F2ED, 0xC6C6C6); mob(EntityNautilus.class, "nautilus", 70, 0xD6C1AB, 0xAE7756); mob(EntityZombieNautilus.class, "zombie_nautilus", 71, 0x648B76, 0xB4A78B); projectile(EntityMobSeat.class, "mob_seat", 72); projectile(EntityLlamaSpit.class, "llama_spit", 73); projectile(EntityStatusArrow.class, "status_arrow", 74); projectile(EntityCrossbowBolt.class, "crossbow_bolt", 75);
		GameRegistry.registerTileEntity(ganymedes01.etfuturum.tileentities.TileEntityCopperStatue.class, "etfuturum.copper_golem_statue");
		GameRegistry.registerTileEntity(ganymedes01.etfuturum.tileentities.TileEntityCopperChest.class, "etfuturum.copper_chest");
		GameRegistry.registerTileEntity(ganymedes01.etfuturum.tileentities.TileEntityTrialSpawner.class, "etfuturum.trial_spawner");
		GameRegistry.registerTileEntity(ganymedes01.etfuturum.tileentities.TileEntityDriedGhast.class, "etfuturum.dried_ghast");
		GameRegistry.registerTileEntity(ganymedes01.etfuturum.tileentities.TileEntitySculkSensor.class, "etfuturum.sculk_sensor");
		GameRegistry.registerTileEntity(ganymedes01.etfuturum.tileentities.TileEntitySculkShrieker.class, "etfuturum.sculk_shrieker");
		GameRegistry.registerTileEntity(ganymedes01.etfuturum.tileentities.TileEntityVault.class, "etfuturum.vault");
		GameRegistry.registerWorldGenerator(new MobAcquisitionWorldgen(), 10);
		if (ConfigEntities.enableModernPassiveSpawning) {
			spawn(EntityCat.class, 3, 1, 2, Type.PLAINS, Type.SWAMP, Type.SANDY, Type.CONIFEROUS);
			if (ModBlocks.BAMBOO.isEnabled()) spawn(EntityPanda.class, 2, 1, 2, Type.JUNGLE);
			spawn(EntityLlama.class, 5, 2, 4, Type.MOUNTAIN, Type.SAVANNA); spawn(EntityArmadillo.class, 6, 2, 3, Type.SAVANNA, Type.MESA); spawn(EntityCamel.class, 1, 1, 1, Type.SANDY);
		}
		if (ConfigEntities.enableModernHostileSpawning) {
			for (BiomeGenBase biome : BiomeDictionary.getBiomesForType(Type.SWAMP)) ganymedes01.etfuturum.configuration.SpawnSettings.addSpawn(EntityBogged.class, 15, 1, 3, EnumCreatureType.monster, biome);
			for (BiomeGenBase biome : BiomeDictionary.getBiomesForType(Type.SANDY)) if (!BiomeDictionary.isBiomeOfType(biome, Type.COLD) && !BiomeDictionary.isBiomeOfType(biome, Type.NETHER)) { ganymedes01.etfuturum.configuration.SpawnSettings.addSpawn(EntityParched.class, 15, 1, 3, EnumCreatureType.monster, biome); ganymedes01.etfuturum.configuration.SpawnSettings.addSpawn(EntityCamelHusk.class, 1, 1, 1, EnumCreatureType.monster, biome); }
		}
	}
	private static void spawn(Class<? extends EntityLiving> entity, int weight, int low, int high, Type... types) { Set<BiomeGenBase> added = new HashSet<>(); for (Type type : types) for (BiomeGenBase biome : BiomeDictionary.getBiomesForType(type)) if (added.add(biome) && !BiomeDictionary.isBiomeOfType(biome, Type.NETHER) && !BiomeDictionary.isBiomeOfType(biome, Type.END)) ganymedes01.etfuturum.configuration.SpawnSettings.addSpawn(entity, weight, low, high, EnumCreatureType.creature, biome); }
}
