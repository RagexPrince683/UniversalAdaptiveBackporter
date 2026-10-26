package ganymedes01.etfuturum.configuration.configs;

import ganymedes01.etfuturum.configuration.ConfigBase;

import java.io.File;

public class ConfigEntities extends ConfigBase {
	public static boolean enableAquaticSpawning, enableDrownedSpawning, enablePhantomSpawning;
	public static int fishPopulation, drownedPopulation, turtleSpawnWeight;
	public static int aquaticPopulation, hostileAquaticPopulation, aquaticLocalPopulation;
	public static int codPopulation, salmonPopulation, tropicalFishPopulation, pufferfishPopulation, dolphinPopulation, axolotlPopulation, glowSquidPopulation, nautilusPopulation, guardianPopulation, squidPopulation;
	public static boolean limitVanillaSquid;
	public static boolean enableModernPassiveSpawning, enableModernHostileSpawning, enablePatrols, enableRaids, enableOutposts, enableMobDungeonContent;

	public static boolean enableStray;
	public static boolean enableEndermite;
	public static boolean enableVillagerZombies;
	public static boolean enableVillagerTurnsIntoWitch;
	public static boolean enableHusk;
	public static boolean enableShulker;
	public static boolean enableBrownMooshroom;
	public static boolean enableBabyGrowthBoost;
	public static boolean enableRabbit;
	public static boolean enableDragonRespawn;
	public static boolean enableNetherEndermen;
	public static boolean enableShearableSnowGolems;
	public static boolean enableBees;
	public static boolean enablePolarBears;
	public static boolean polarBearRealism;
	public static boolean enableGoats;
	public static boolean enableSquidInk;
	public static boolean enableFoxes;
	public static boolean enableNetherMobs;
	public static boolean enableFrogs;
	public static boolean enableGuardians;

	static final String catHostile = "hostile";
	static final String catNeutral = "neutral";
	static final String catPassive = "passive";
	static final String catPlayer = "player";
	static final String catMisc = "misc";

	public ConfigEntities() {
		super("entities");
	}

	@Override
	protected void syncConfigOptions() {
		enableModernPassiveSpawning = getBoolean("enableModernPassiveSpawning", catPassive, true, "Native biome spawning for cats, pandas, llamas, armadillos, camels and nautiluses. All registry identities remain when disabled.");
		enableModernHostileSpawning = getBoolean("enableModernHostileSpawning", catHostile, true, "Native biome spawning for bogged, parched and undead mounts; naturally placed shriekers may summon wardens.");
		enablePatrols = getBoolean("enablePatrols", catHostile, true, "Bounded native pillager patrol attempts around players in the Overworld.");
		enableRaids = getBoolean("enableRaids", catHostile, true, "Ominous bottles trigger saved, finite raids at existing native villages.");
		enableOutposts = getBoolean("enableOutposts", catHostile, true, "Generate released outpost towers with pillagers and saved surrounding cages, tents, logs and targets in suitable native surface biomes. Each piece waits for its own chunk's first population; no retrogen or forced neighbor loading.");
		enableMobDungeonContent = getBoolean("enableMobDungeonContent", catHostile, true, "Adapt a minority of newly generated native dungeons into finite breeze/bogged fights or underground shrieker encounters. This does not generate Trial Chambers, vaults, keys or cave biomes.");
		enableAquaticSpawning = getBoolean("enableAquaticSpawning", catPassive, true, "Natural fish, dolphins, turtles, axolotls, glow squid and seagrass. Disabling spawning retains their entity, egg, item and block registrations for saved worlds.");
		enableDrownedSpawning = getBoolean("enableDrownedSpawning", catHostile, true, "Drowned spawning in rivers/oceans and underwater zombie conversion. Registrations remain available when disabled.");
		enablePhantomSpawning = getBoolean("enablePhantomSpawning", catHostile, true, "Phantoms may spawn above exposed players after three days without rest. Sleeping resets the timer. Registrations remain available when disabled.");
		fishPopulation = getInt("fishPopulation", catPassive, 20, 1, 70, "Native fish population cap, scaled by eligible spawning chunks.");
		drownedPopulation = getInt("drownedPopulation", catHostile, 15, 1, 70, "Native drowned population cap, scaled by eligible spawning chunks.");
		aquaticPopulation = getInt("aquaticPopulation", catPassive, 48, 1, 1024, "Shared natural EFR aquatic animal budget per 256 eligible loaded chunks. A small loaded area retains room for one school. Named, bucket-released and tamed animals do not consume natural budgets.");
		hostileAquaticPopulation = getInt("hostileAquaticPopulation", catHostile, 80, 1, 1024, "Shared natural drowned and guardian budget per 256 eligible loaded chunks. Conversions, spawners, commands and existing saved entities are never rejected.");
		aquaticLocalPopulation = getInt("aquaticLocalPopulation", catPassive, 24, 1, 256, "Shared natural animal or hostile density within 24 blocks horizontally and 12 vertically. Each species also permits at most two natural schools nearby.");
		codPopulation = getInt("codPopulation", catPassive, 20, 1, 256, "Natural cod budget per 256 eligible loaded chunks, also bounded by fishPopulation.");
		salmonPopulation = getInt("salmonPopulation", catPassive, 20, 1, 256, "Natural salmon budget per 256 eligible loaded chunks, also bounded by fishPopulation.");
		tropicalFishPopulation = getInt("tropicalFishPopulation", catPassive, 20, 1, 256, "Natural tropical fish budget per 256 eligible loaded chunks, also bounded by fishPopulation.");
		pufferfishPopulation = getInt("pufferfishPopulation", catPassive, 5, 1, 256, "Natural pufferfish budget per 256 eligible loaded chunks, also bounded by fishPopulation.");
		dolphinPopulation = getInt("dolphinPopulation", catPassive, 5, 1, 256, "Natural dolphin budget per 256 eligible loaded chunks.");
		axolotlPopulation = getInt("axolotlPopulation", catPassive, 5, 1, 256, "Natural axolotl budget per 256 eligible loaded chunks.");
		glowSquidPopulation = getInt("glowSquidPopulation", catPassive, 5, 1, 256, "Natural glow squid budget per 256 eligible loaded chunks.");
		nautilusPopulation = getInt("nautilusPopulation", catPassive, 5, 1, 256, "Shared natural nautilus and zombie nautilus budget per 256 eligible loaded chunks.");
		guardianPopulation = getInt("guardianPopulation", catHostile, 70, 1, 256, "Natural monument guardian budget per 256 eligible loaded chunks. Persistent elders are excluded.");
		limitVanillaSquid = getBoolean("limitVanillaSquid", catPassive, false, "Optional strict natural admission and local density limits for the exact vanilla squid class. Other mods' squid subclasses and existing squid are untouched.");
		squidPopulation = getInt("squidPopulation", catPassive, 5, 1, 256, "Natural vanilla squid budget per 256 eligible loaded chunks when limitVanillaSquid is enabled; independent of EFR shared budgets.");
		turtleSpawnWeight = 5; // Compatibility field; the authoritative per-species value is applied by SpawnSettings. zero disables natural turtle spawning.");
		enableGuardians = getBoolean("enableGuardians", catHostile, true, "Guardians spawn in saved ocean-monument bounds. Three persistent elders guard each new monument and drop wet sponge when killed by a player.");
		enableNetherMobs = getBoolean("enableNetherMobs", catNeutral, true, "Enables piglins, brutes, hoglins, zoglins and striders, including bartering, conversion and riding. Natural spawns use the enabled Nether biomes; brutes belong to bastions. The Nether master toggle takes precedence.") && ConfigBlocksItems.masterNetherToggle;
		//passive
		enableFrogs = getBoolean("enableFrogs", catPassive, true, "Frogs spawn in swamps, breed with slime balls, lay frogspawn, and grow from bucketable tadpoles. Small magma cubes yield the frog variant's froglight.");
		enableRabbit = getBoolean("enableRabbits", catPassive, true, "");
		enableBrownMooshroom = getBoolean("enableBrownMooshroom", catPassive, true, "Brown mooshroom variant, the red mooshrooms turn into then when they are hit by lightning.");
		enableFoxes = getBoolean("enableFoxes", catPassive, true, "");
		enableGoats = getBoolean("enableGoats", catPassive, true, "Goats spawn in snowy mountain biomes, can be milked, and drop horns when ramming hard blocks.");

		//neutral
		enableBees = getBoolean("enableBees", catNeutral, true, "");
		enablePolarBears = getBoolean("enablePolarBears", catNeutral, true, "Polar bears spawn in snowy biomes and defend nearby cubs.");
		polarBearRealism = getBoolean("polarBearRealism", catNeutral, true, "Makes adult polar bears hostile to players by default. Disable this to make them attack players only when defending nearby cubs.");

		//hostile
		enableEndermite = getBoolean("enableEndermite", catHostile, true, "Rarely spawns when the player lands from Ender Pearl throws");
		enableHusk = getBoolean("enableHusks", catHostile, true, "Desert zombie variant");
		enableStray = getBoolean("enableStrays", catHostile, true, "Tundra skeleton variant");
		enableShulker = getBoolean("enableShulker", catHostile, true, "Shell-lurking mobs from the End.");
		enableVillagerZombies = getBoolean("enableZombieVillager", catHostile, true, "");

		//function
		enableShearableSnowGolems = getBoolean("enableShearableSnowGolems", catMisc, true, "");
		enableBabyGrowthBoost = getBoolean("enableBabyGrowthBoost", catMisc, true, "");
		enableVillagerTurnsIntoWitch = getBoolean("enableVillagerTurnsIntoWitch", catMisc, true, "Villagers turn into Witches when struck by lightning");
		enableDragonRespawn = getBoolean("enableDragonRespawn", catMisc, true, "Crude implementation of respawning the dragon using four End crystals.");
		enableNetherEndermen = getBoolean("enableNetherEndermen", catMisc, true, "Allow endermen to rarely spawn in the Nether");
		enableSquidInk = getBoolean("enableSquidInk", catMisc, true, "Squid now produce a cloud of floating black ink particles when attacked.");
	}

}
