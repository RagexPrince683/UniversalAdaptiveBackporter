package ganymedes01.etfuturum;

import cpw.mods.fml.common.registry.GameRegistry;
import ganymedes01.etfuturum.blocks.BlockWoodSign;
import ganymedes01.etfuturum.compat.ModsList;
import ganymedes01.etfuturum.configuration.configs.*;
import ganymedes01.etfuturum.core.utils.Utils;
import ganymedes01.etfuturum.items.*;
import ganymedes01.etfuturum.items.equipment.*;
import ganymedes01.etfuturum.items.rawore.modded.BaseRawOre;
import ganymedes01.etfuturum.items.rawore.modded.ItemGeneralModdedRawOre;
import ganymedes01.etfuturum.lib.Reference;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public enum ModItems {
	HEART_OF_THE_SEA(ganymedes01.etfuturum.configuration.ContentAvailability.enabled("conduits"), new BaseItem("heart_of_the_sea")),
	DRIED_KELP(ganymedes01.etfuturum.configuration.ContentAvailability.enabled("kelp"), new ItemDriedKelp()),
	WOODEN_SPEAR(ganymedes01.etfuturum.configuration.ContentAvailability.enabled("spears"), new ItemSpear(Item.ToolMaterial.WOOD, "wooden_spear")),
	STONE_SPEAR(ganymedes01.etfuturum.configuration.ContentAvailability.enabled("spears"), new ItemSpear(Item.ToolMaterial.STONE, "stone_spear")),
	COPPER_SPEAR(ganymedes01.etfuturum.configuration.ContentAvailability.enabled("spears"), new ItemSpear(net.minecraftforge.common.util.EnumHelper.addToolMaterial("EFR_COPPER_SPEAR", 1, 190, 4, 1, 13), "copper_spear")),
	IRON_SPEAR(ganymedes01.etfuturum.configuration.ContentAvailability.enabled("spears"), new ItemSpear(Item.ToolMaterial.IRON, "iron_spear")),
	GOLDEN_SPEAR(ganymedes01.etfuturum.configuration.ContentAvailability.enabled("spears"), new ItemSpear(Item.ToolMaterial.GOLD, "golden_spear")),
	DIAMOND_SPEAR(ganymedes01.etfuturum.configuration.ContentAvailability.enabled("spears"), new ItemSpear(Item.ToolMaterial.EMERALD, "diamond_spear")),
	CROSSBOW(ganymedes01.etfuturum.configuration.ContentAvailability.enabled("crossbows"), new ItemCrossbow()),
	OMINOUS_BOTTLE(ganymedes01.etfuturum.configuration.ContentAvailability.enabled("ominous_bottles"), new ItemOminousBottle()),
	ANGLER_POTTERY_SHERD(ganymedes01.etfuturum.configuration.ContentAvailability.enabled("archaeology"), new ItemPotterySherd("angler")),
	ARCHER_POTTERY_SHERD(ganymedes01.etfuturum.configuration.ContentAvailability.enabled("archaeology"), new ItemPotterySherd("archer")),
	ARMS_UP_POTTERY_SHERD(ganymedes01.etfuturum.configuration.ContentAvailability.enabled("archaeology"), new ItemPotterySherd("arms_up")),
	BLADE_POTTERY_SHERD(ganymedes01.etfuturum.configuration.ContentAvailability.enabled("archaeology"), new ItemPotterySherd("blade")),
	BREWER_POTTERY_SHERD(ganymedes01.etfuturum.configuration.ContentAvailability.enabled("archaeology"), new ItemPotterySherd("brewer")),
	BURN_POTTERY_SHERD(ganymedes01.etfuturum.configuration.ContentAvailability.enabled("archaeology"), new ItemPotterySherd("burn")),
	DANGER_POTTERY_SHERD(ganymedes01.etfuturum.configuration.ContentAvailability.enabled("archaeology"), new ItemPotterySherd("danger")),
	EXPLORER_POTTERY_SHERD(ganymedes01.etfuturum.configuration.ContentAvailability.enabled("archaeology"), new ItemPotterySherd("explorer")),
	FLOW_POTTERY_SHERD(ganymedes01.etfuturum.configuration.ContentAvailability.enabled("archaeology"), new ItemPotterySherd("flow")),
	FRIEND_POTTERY_SHERD(ganymedes01.etfuturum.configuration.ContentAvailability.enabled("archaeology"), new ItemPotterySherd("friend")),
	GUSTER_POTTERY_SHERD(ganymedes01.etfuturum.configuration.ContentAvailability.enabled("archaeology"), new ItemPotterySherd("guster")),
	HEART_POTTERY_SHERD(ganymedes01.etfuturum.configuration.ContentAvailability.enabled("archaeology"), new ItemPotterySherd("heart")),
	HEARTBREAK_POTTERY_SHERD(ganymedes01.etfuturum.configuration.ContentAvailability.enabled("archaeology"), new ItemPotterySherd("heartbreak")),
	HOWL_POTTERY_SHERD(ganymedes01.etfuturum.configuration.ContentAvailability.enabled("archaeology"), new ItemPotterySherd("howl")),
	MINER_POTTERY_SHERD(ganymedes01.etfuturum.configuration.ContentAvailability.enabled("archaeology"), new ItemPotterySherd("miner")),
	MOURNER_POTTERY_SHERD(ganymedes01.etfuturum.configuration.ContentAvailability.enabled("archaeology"), new ItemPotterySherd("mourner")),
	PLENTY_POTTERY_SHERD(ganymedes01.etfuturum.configuration.ContentAvailability.enabled("archaeology"), new ItemPotterySherd("plenty")),
	PRIZE_POTTERY_SHERD(ganymedes01.etfuturum.configuration.ContentAvailability.enabled("archaeology"), new ItemPotterySherd("prize")),
	SCRAPE_POTTERY_SHERD(ganymedes01.etfuturum.configuration.ContentAvailability.enabled("archaeology"), new ItemPotterySherd("scrape")),
	SHEAF_POTTERY_SHERD(ganymedes01.etfuturum.configuration.ContentAvailability.enabled("archaeology"), new ItemPotterySherd("sheaf")),
	SHELTER_POTTERY_SHERD(ganymedes01.etfuturum.configuration.ContentAvailability.enabled("archaeology"), new ItemPotterySherd("shelter")),
	SKULL_POTTERY_SHERD(ganymedes01.etfuturum.configuration.ContentAvailability.enabled("archaeology"), new ItemPotterySherd("skull")),
	SNORT_POTTERY_SHERD(ganymedes01.etfuturum.configuration.ContentAvailability.enabled("archaeology"), new ItemPotterySherd("snort")),
	SENTRY_ARMOR_TRIM_SMITHING_TEMPLATE(ganymedes01.etfuturum.configuration.ContentAvailability.enabled("armor_trims"), new ItemSmithingTemplate("sentry")),
	DUNE_ARMOR_TRIM_SMITHING_TEMPLATE(ganymedes01.etfuturum.configuration.ContentAvailability.enabled("armor_trims"), new ItemSmithingTemplate("dune")),
	COAST_ARMOR_TRIM_SMITHING_TEMPLATE(ganymedes01.etfuturum.configuration.ContentAvailability.enabled("armor_trims"), new ItemSmithingTemplate("coast")),
	WILD_ARMOR_TRIM_SMITHING_TEMPLATE(ganymedes01.etfuturum.configuration.ContentAvailability.enabled("armor_trims"), new ItemSmithingTemplate("wild")),
	WARD_ARMOR_TRIM_SMITHING_TEMPLATE(ganymedes01.etfuturum.configuration.ContentAvailability.enabled("armor_trims"), new ItemSmithingTemplate("ward")),
	EYE_ARMOR_TRIM_SMITHING_TEMPLATE(ganymedes01.etfuturum.configuration.ContentAvailability.enabled("armor_trims"), new ItemSmithingTemplate("eye")),
	VEX_ARMOR_TRIM_SMITHING_TEMPLATE(ganymedes01.etfuturum.configuration.ContentAvailability.enabled("armor_trims"), new ItemSmithingTemplate("vex")),
	TIDE_ARMOR_TRIM_SMITHING_TEMPLATE(ganymedes01.etfuturum.configuration.ContentAvailability.enabled("armor_trims"), new ItemSmithingTemplate("tide")),
	SNOUT_ARMOR_TRIM_SMITHING_TEMPLATE(ganymedes01.etfuturum.configuration.ContentAvailability.enabled("armor_trims"), new ItemSmithingTemplate("snout")),
	RIB_ARMOR_TRIM_SMITHING_TEMPLATE(ganymedes01.etfuturum.configuration.ContentAvailability.enabled("armor_trims"), new ItemSmithingTemplate("rib")),
	SPIRE_ARMOR_TRIM_SMITHING_TEMPLATE(ganymedes01.etfuturum.configuration.ContentAvailability.enabled("armor_trims"), new ItemSmithingTemplate("spire")),
	WAYFINDER_ARMOR_TRIM_SMITHING_TEMPLATE(ganymedes01.etfuturum.configuration.ContentAvailability.enabled("armor_trims"), new ItemSmithingTemplate("wayfinder")),
	SHAPER_ARMOR_TRIM_SMITHING_TEMPLATE(ganymedes01.etfuturum.configuration.ContentAvailability.enabled("armor_trims"), new ItemSmithingTemplate("shaper")),
	SILENCE_ARMOR_TRIM_SMITHING_TEMPLATE(ganymedes01.etfuturum.configuration.ContentAvailability.enabled("armor_trims"), new ItemSmithingTemplate("silence")),
	RAISER_ARMOR_TRIM_SMITHING_TEMPLATE(ganymedes01.etfuturum.configuration.ContentAvailability.enabled("armor_trims"), new ItemSmithingTemplate("raiser")),
	HOST_ARMOR_TRIM_SMITHING_TEMPLATE(ganymedes01.etfuturum.configuration.ContentAvailability.enabled("armor_trims"), new ItemSmithingTemplate("host")),
	FLOW_ARMOR_TRIM_SMITHING_TEMPLATE(ganymedes01.etfuturum.configuration.ContentAvailability.enabled("armor_trims"), new ItemSmithingTemplate("flow")),
	BOLT_ARMOR_TRIM_SMITHING_TEMPLATE(ganymedes01.etfuturum.configuration.ContentAvailability.enabled("armor_trims"), new ItemSmithingTemplate("bolt")),
	NETHERITE_UPGRADE_SMITHING_TEMPLATE(ganymedes01.etfuturum.configuration.ContentAvailability.enabled("template_smithing"), new ItemSmithingTemplate("netherite_upgrade")),
	BRUSH(ganymedes01.etfuturum.configuration.ContentAvailability.enabled("brushes"), new ItemBrush()),
	ARMADILLO_SCUTE(ganymedes01.etfuturum.configuration.ContentAvailability.enabled("wolf_armor"), new BaseItem("armadillo_scute")),
	WOLF_ARMOR(ganymedes01.etfuturum.configuration.ContentAvailability.enabled("wolf_armor"), new BaseItem("wolf_armor").setMaxStackSize(1).setMaxDamage(64)),
	WIND_CHARGE(ganymedes01.etfuturum.configuration.ContentAvailability.enabled("wind_charges"), new ItemWindCharge()),
	BREEZE_ROD(ganymedes01.etfuturum.configuration.ContentAvailability.enabled("wind_charges"), new BaseItem("breeze_rod")),
	TORCHFLOWER_SEEDS(ganymedes01.etfuturum.configuration.ContentAvailability.enabled("ancient_plants"), new net.minecraft.item.ItemSeeds(ModBlocks.TORCHFLOWER_CROP.get(), Blocks.farmland).setUnlocalizedName("etfuturum.torchflower_seeds").setTextureName("torchflower_seeds")),
	PITCHER_POD(ganymedes01.etfuturum.configuration.ContentAvailability.enabled("ancient_plants"), new net.minecraft.item.ItemSeeds(ModBlocks.PITCHER_CROP.get(), Blocks.farmland).setUnlocalizedName("etfuturum.pitcher_pod").setTextureName("pitcher_pod")),
	HARNESS(ganymedes01.etfuturum.configuration.ContentAvailability.enabled("harnesses"), new BaseSubtypesItem("white_harness", "orange_harness", "magenta_harness", "light_blue_harness", "yellow_harness", "lime_harness", "pink_harness", "gray_harness", "light_gray_harness", "cyan_harness", "purple_harness", "blue_harness", "brown_harness", "green_harness", "red_harness", "black_harness").setNames("harness").setMaxStackSize(1)),
	NAUTILUS_ARMOR(ganymedes01.etfuturum.configuration.ContentAvailability.enabled("nautilus_armor"), new BaseSubtypesItem("copper_nautilus_armor", "golden_nautilus_armor", "iron_nautilus_armor", "diamond_nautilus_armor", "netherite_nautilus_armor").setNames("nautilus_armor").setMaxStackSize(1)),
	COD_BUCKET(ganymedes01.etfuturum.configuration.ContentAvailability.enabled("fish_buckets"), new ItemFishBucket(0, "cod_bucket")),
	SALMON_BUCKET(ganymedes01.etfuturum.configuration.ContentAvailability.enabled("fish_buckets"), new ItemFishBucket(1, "salmon_bucket")),
	TROPICAL_FISH_BUCKET(ganymedes01.etfuturum.configuration.ContentAvailability.enabled("fish_buckets"), new ItemFishBucket(2, "tropical_fish_bucket")),
	PUFFERFISH_BUCKET(ganymedes01.etfuturum.configuration.ContentAvailability.enabled("fish_buckets"), new ItemFishBucket(3, "pufferfish_bucket")),
	AXOLOTL_BUCKET(ganymedes01.etfuturum.configuration.ContentAvailability.enabled("axolotl_buckets"), new ItemFishBucket(4, "axolotl_bucket")),
	GLOW_INK_SAC(ganymedes01.etfuturum.configuration.ContentAvailability.enabled("glow_ink"), new BaseItem("glow_ink_sac")),
	TURTLE_SCUTE(ganymedes01.etfuturum.configuration.ContentAvailability.enabled("scutes"), new BaseItem("turtle_scute")),
	TURTLE_HELMET(ganymedes01.etfuturum.configuration.ContentAvailability.enabled("scutes"), new ItemTurtleHelmet()),
	PHANTOM_MEMBRANE(ganymedes01.etfuturum.configuration.ContentAvailability.enabled("phantom_membranes"), new BaseItem("phantom_membrane")),
	TRIDENT(ganymedes01.etfuturum.configuration.ContentAvailability.enabled("tridents"), new ItemTrident()),
	NAUTILUS_SHELL(ganymedes01.etfuturum.configuration.ContentAvailability.enabled("nautilus_shells"), new BaseItem("nautilus_shell")),
	MUTTON_RAW(ConfigBlocksItems.enableMutton, new BaseFood(2, 0.3F, true).setNames("mutton")),
	MUTTON_COOKED(ConfigBlocksItems.enableMutton, new BaseFood(6, 0.8F, true).setNames("cooked_mutton")),
	PRISMARINE_SHARD(ConfigBlocksItems.enablePrismarine, new BaseItem("prismarine_shard")),
	PRISMARINE_CRYSTALS(ConfigBlocksItems.enablePrismarine, new BaseItem("prismarine_crystals")),
	WOODEN_ARMORSTAND(ConfigBlocksItems.enableArmourStand, new ItemArmorStand()),
	RABBIT_RAW(ConfigEntities.enableRabbit, new BaseFood(3, 0.3F, true).setNames("rabbit")),
	RABBIT_COOKED(ConfigEntities.enableRabbit, new BaseFood(5, 0.6F, true).setNames("cooked_rabbit")),
	RABBIT_FOOT(ConfigEntities.enableRabbit, new BaseItem("rabbit_foot").setPotionEffect("+0+1-2+3&4-4+13")),
	RABBIT_HIDE(ConfigEntities.enableRabbit, new BaseItem("rabbit_hide")),
	RABBIT_STEW(ConfigEntities.enableRabbit, new ItemRabbitStew()),
	BEETROOT(ConfigBlocksItems.enableBeetroot, new BaseFood(1, 0.6F, false).setNames("beetroot")),
	BEETROOT_SEEDS(ConfigBlocksItems.enableBeetroot, new ItemBeetrootSeeds()),
	BEETROOT_SOUP(ConfigBlocksItems.enableBeetroot, new ItemBeetrootSoup()),
	CHORUS_FRUIT(ConfigBlocksItems.enableChorusFruit, new ItemChorusFruit()),
	CHORUS_FRUIT_POPPED(ConfigBlocksItems.enableChorusFruit, new BaseItem("popped_chorus_fruit")),
	TIPPED_ARROW(ConfigBlocksItems.enableTippedArrows, new ItemArrowTipped()),
	SPECTRAL_ARROW(ConfigBlocksItems.enableSpectralArrows, new ItemSpectralArrow()),
	LINGERING_POTION(ConfigBlocksItems.enableLingeringPotions, new ItemLingeringPotion()),
	DRAGON_BREATH(ConfigBlocksItems.enableLingeringPotions, new BaseItem("dragon_breath").setContainerItem(Items.glass_bottle).setPotionEffect("-14+13")),
	ELYTRA(ConfigMixins.enableElytra, new ItemArmorElytra()),
	END_CRYSTAL(ConfigEntities.enableDragonRespawn, new ItemEndCrystal()),
	NUGGET_IRON(ConfigBlocksItems.enableIronNugget, new BaseItem("iron_nugget")),
	RAW_ORE(ConfigBlocksItems.enableRawOres, new BaseSubtypesItem("raw_copper", "raw_iron", "raw_gold").setNames("raw_ore")),
	//modded_raw_ore(true, new ItemRawOre(true)),
	NETHERITE_SCRAP(ConfigBlocksItems.enableNetherite, new BaseItem("netherite_scrap")),
	NETHERITE_INGOT(ConfigBlocksItems.enableNetherite, new ItemNetheriteIngot()),
	NETHERITE_HELMET(ConfigBlocksItems.enableNetherite, new ItemEFRArmour(ModMaterials.NETHERITE_ARMOUR, 0, ConfigBlocksItems.netheriteHelmetDurability)),
	NETHERITE_CHESTPLATE(ConfigBlocksItems.enableNetherite, new ItemEFRArmour(ModMaterials.NETHERITE_ARMOUR, 1, ConfigBlocksItems.netheriteChestplateDurability)),
	NETHERITE_LEGGINGS(ConfigBlocksItems.enableNetherite, new ItemEFRArmour(ModMaterials.NETHERITE_ARMOUR, 2, ConfigBlocksItems.netheriteLeggingsDurability)),
	NETHERITE_BOOTS(ConfigBlocksItems.enableNetherite, new ItemEFRArmour(ModMaterials.NETHERITE_ARMOUR, 3, ConfigBlocksItems.netheriteBootsDurability)),
	NETHERITE_PICKAXE(ConfigBlocksItems.enableNetherite, new ItemEFRPickaxe(ModMaterials.NETHERITE_TOOL, ConfigBlocksItems.netheritePickaxeDurability)),
	NETHERITE_SPADE(ConfigBlocksItems.enableNetherite, new ItemEFRSpade(ModMaterials.NETHERITE_TOOL, ConfigBlocksItems.netheriteSpadeDurability)),
	NETHERITE_AXE(ConfigBlocksItems.enableNetherite, new ItemEFRAxe(ModMaterials.NETHERITE_TOOL, ConfigBlocksItems.netheriteAxeDurability)),
	NETHERITE_HOE(ConfigBlocksItems.enableNetherite, new ItemEFRHoe(ModMaterials.NETHERITE_TOOL, ConfigBlocksItems.netheriteHoeDurability)),
	NETHERITE_SWORD(ConfigBlocksItems.enableNetherite, new ItemEFRSword(ModMaterials.NETHERITE_TOOL, ConfigBlocksItems.netheriteSwordDurability)),
	NETHERITE_SPEAR(ConfigBlocksItems.enableNetherite, new ItemSpear(ModMaterials.NETHERITE_TOOL, "netherite_spear")),
	TOTEM_OF_UNDYING(ConfigBlocksItems.enableTotemUndying, new BaseItem("totem_of_undying").setMaxStackSize(1)),
	DYE(ConfigBlocksItems.enableNewDyes, new BaseSubtypesItem("white_dye", "blue_dye", "brown_dye", "black_dye").setNames("dye")),
	COPPER_INGOT(ConfigBlocksItems.enableCopper && !ConfigModCompat.disableCopperOreAndIngotOnly, new BaseItem("copper_ingot")),
	SUSPICIOUS_STEW(ConfigBlocksItems.enableSuspiciousStew, new ItemSuspiciousStew()),
	SWEET_BERRIES(ConfigBlocksItems.enableSweetBerryBushes, new ItemSweetBerries()),
	GLOW_BERRIES_ITEM(ConfigBlocksItems.enableGlowBerries, new ItemGlowBerries(ModBlocks.CAVE_VINE.get())),
	SHULKER_SHELL(ConfigBlocksItems.enableShulkerBoxes, new BaseItem("shulker_shell")),
	RELIC_RECORD(ganymedes01.etfuturum.configuration.ContentAvailability.enabled("archaeology"), new ItemEtFuturumRecord("relic")),
	PIGSTEP_RECORD(ConfigBlocksItems.enablePigstep, new ItemEtFuturumRecord("pigstep")),
	OTHERSIDE_RECORD(ConfigBlocksItems.enableOtherside, new ItemEtFuturumRecord("otherside")),
	PRECIPICE_RECORD(ConfigBlocksItems.enablePrecipice, new ItemEtFuturumRecord("precipice")),
	CREATOR_MUSIC_BOX_RECORD(ConfigBlocksItems.enableCreatorMusicBox, new ItemEtFuturumRecord("creator_music_box")),
	CREATOR_RECORD(ConfigBlocksItems.enableCreator, new ItemEtFuturumRecord("creator")),
	TEARS_RECORD(ConfigBlocksItems.enableTears, new ItemEtFuturumRecord("tears")),
	LAVA_CHICKEN_RECORD(ConfigBlocksItems.enableLavaChicken, new ItemEtFuturumRecord("lava_chicken")),
	FIVE_RECORD(ConfigBlocksItems.enable5, new ItemEtFuturumRecord("5")),
	DISC_FRAGMENT_5(ConfigBlocksItems.enable5, new BaseItem("disc_fragment_5", true)),
	AMETHYST_SHARD(ConfigBlocksItems.enableAmethyst, new BaseItem("amethyst_shard")),
	SHULKER_BOX_UPGRADE(ModsList.IRON_CHEST.isLoaded() && ConfigBlocksItems.enableShulkerBoxes
			&& ConfigModCompat.shulkerBoxesIronChest && ConfigModCompat.shulkerBoxesIronChestUpgradeItem, new ItemShulkerBoxUpgrade()),
	BARREL_UPGRADE(ModsList.IRON_CHEST.isLoaded() && ConfigBlocksItems.enableBarrel
			&& ConfigModCompat.barrelIronChest && ConfigModCompat.barrelIronChestUpgradeItem, new ItemBarrelUpgrade()),
	HONEYCOMB(ConfigBlocksItems.enableHoney, new BaseItem("honeycomb")),
	HONEY_BOTTLE(ConfigBlocksItems.enableHoney, new ItemHoneyBottle()),
	BAMBOO(ConfigBlocksItems.enableBambooBlocks, new ItemBamboo()),
	GOAT_HORN(ConfigEntities.enableGoats, new ItemGoatHorn()),
	RESIN_BRICK(ConfigExperiments.enablePaleGarden, new BaseItem("resin_brick")),
	TADPOLE_BUCKET(ConfigEntities.enableFrogs, new ItemTadpoleBucket()),
	WARPED_FUNGUS_ON_A_STICK(ConfigEntities.enableNetherMobs && ConfigExperiments.enableWarpedBlocks, new ItemWarpedFungusOnAStick()),
	PALE_OAK_BOAT(ConfigBlocksItems.enableNewBoats && ConfigExperiments.enablePaleGarden, new ItemNewBoat("minecraft", "pale_oak", ModBlocks.PALE_OAK_PLANKS::getItem, 0, false, false)),
	PALE_OAK_CHEST_BOAT(ConfigBlocksItems.enableNewBoats && ConfigExperiments.enablePaleGarden, new ItemNewBoat("minecraft", "pale_oak", ModBlocks.PALE_OAK_PLANKS::getItem, 0, true, false)),

	OAK_BOAT(ConfigBlocksItems.enableNewBoats && !ConfigBlocksItems.replaceOldBoats, new ItemNewBoat("minecraft", "oak", () -> Item.getItemFromBlock(Blocks.planks), 0, false, false)),
	OAK_CHEST_BOAT(ConfigBlocksItems.enableNewBoats, new ItemNewBoat("minecraft", "oak", () -> Item.getItemFromBlock(Blocks.planks), 0, true, false)),
	SPRUCE_BOAT(ConfigBlocksItems.enableNewBoats, new ItemNewBoat("minecraft", "spruce", () -> Item.getItemFromBlock(Blocks.planks), 1, false, false)),
	SPRUCE_CHEST_BOAT(ConfigBlocksItems.enableNewBoats, new ItemNewBoat("minecraft", "spruce", () -> Item.getItemFromBlock(Blocks.planks), 1, true, false)),
	BIRCH_BOAT(ConfigBlocksItems.enableNewBoats, new ItemNewBoat("minecraft", "birch", () -> Item.getItemFromBlock(Blocks.planks), 2, false, false)),
	BIRCH_CHEST_BOAT(ConfigBlocksItems.enableNewBoats, new ItemNewBoat("minecraft", "birch", () -> Item.getItemFromBlock(Blocks.planks), 2, true, false)),
	JUNGLE_BOAT(ConfigBlocksItems.enableNewBoats, new ItemNewBoat("minecraft", "jungle", () -> Item.getItemFromBlock(Blocks.planks), 3, false, false)),
	JUNGLE_CHEST_BOAT(ConfigBlocksItems.enableNewBoats, new ItemNewBoat("minecraft", "jungle", () -> Item.getItemFromBlock(Blocks.planks), 3, true, false)),
	ACACIA_BOAT(ConfigBlocksItems.enableNewBoats, new ItemNewBoat("minecraft", "acacia", () -> Item.getItemFromBlock(Blocks.planks), 4, false, false)),
	ACACIA_CHEST_BOAT(ConfigBlocksItems.enableNewBoats, new ItemNewBoat("minecraft", "acacia", () -> Item.getItemFromBlock(Blocks.planks), 4, true, false)),
	DARK_OAK_BOAT(ConfigBlocksItems.enableNewBoats, new ItemNewBoat("minecraft", "dark_oak", () -> Item.getItemFromBlock(Blocks.planks), 5, false, false)),
	DARK_OAK_CHEST_BOAT(ConfigBlocksItems.enableNewBoats, new ItemNewBoat("minecraft", "dark_oak", () -> Item.getItemFromBlock(Blocks.planks), 5, true, false)),
	MANGROVE_OAK_BOAT(ConfigBlocksItems.enableNewBoats && ConfigExperiments.enableMangroveBlocks, new ItemNewBoat("minecraft", "mangrove", ModBlocks.WOOD_PLANKS::getItem, 2, false, false)),
	MANGROVE_CHEST_BOAT(ConfigBlocksItems.enableNewBoats && ConfigExperiments.enableMangroveBlocks, new ItemNewBoat("minecraft", "mangrove", ModBlocks.WOOD_PLANKS::getItem, 2, true, false)),
	CHERRY_BOAT(ConfigBlocksItems.enableNewBoats && ConfigBlocksItems.enableCherryBlocks, new ItemNewBoat("minecraft", "cherry", ModBlocks.WOOD_PLANKS::getItem, 3, false, false)),
	CHERRY_CHEST_BOAT(ConfigBlocksItems.enableNewBoats && ConfigBlocksItems.enableCherryBlocks, new ItemNewBoat("minecraft", "cherry", ModBlocks.WOOD_PLANKS::getItem, 3, true, false)),
	BAMBOO_RAFT(ConfigBlocksItems.enableNewBoats && ConfigBlocksItems.enableBambooBlocks, new ItemNewBoat("minecraft", "bamboo", ModBlocks.WOOD_PLANKS::getItem, 4, false, true)),
	BAMBOO_CHEST_RAFT(ConfigBlocksItems.enableNewBoats && ConfigBlocksItems.enableBambooBlocks, new ItemNewBoat("minecraft", "bamboo", ModBlocks.WOOD_PLANKS::getItem, 4, true, true)),

	//legacy sign items -- new signs use their ItemBlock as the sign item instead
	ITEM_SIGN_SPRUCE(ConfigBlocksItems.enableVanillaSigns, new ItemWoodSign((BlockWoodSign) ModBlocks.SIGN_SPRUCE.get())),
	ITEM_SIGN_BIRCH(ConfigBlocksItems.enableVanillaSigns, new ItemWoodSign((BlockWoodSign) ModBlocks.SIGN_BIRCH.get())),
	ITEM_SIGN_JUNGLE(ConfigBlocksItems.enableVanillaSigns, new ItemWoodSign((BlockWoodSign) ModBlocks.SIGN_JUNGLE.get())),
	ITEM_SIGN_ACACIA(ConfigBlocksItems.enableVanillaSigns, new ItemWoodSign((BlockWoodSign) ModBlocks.SIGN_ACACIA.get())),
	ITEM_SIGN_DARK_OAK(ConfigBlocksItems.enableVanillaSigns, new ItemWoodSign((BlockWoodSign) ModBlocks.SIGN_DARK_OAK.get())),

	//Mod Support
	MODDED_RAW_ORE(Utils.enableModdedRawOres(), new ItemGeneralModdedRawOre("raw_aluminum", "raw_tin", "raw_silver", "raw_lead", "raw_nickel", "raw_platinum", "raw_mythril",
			"raw_uranium", "raw_thorium", "raw_tungsten", "raw_titanium", "raw_zinc", "raw_magnesium", "raw_boron")),
	RAW_ADAMANTIUM(Utils.enableModdedRawOres(ModsList.SIMPLEORES), new BaseRawOre("simpleores", "adamantium")),

	//Debug Item
	DEBUGGING_TOOL(Reference.DEV_ENVIRONMENT, new DebugTestItem()),
	BANNER_PATTERN(ConfigBlocksItems.enableBanners, new ItemBannerPattern()),
	TRIAL_KEY(ConfigWorld.enableTrialChambers, new BaseItem("trial_key")),
	OMINOUS_TRIAL_KEY(ConfigWorld.enableTrialChambers, new BaseItem("ominous_trial_key")),
	ECHO_SHARD(ConfigExperiments.enableSculk, new BaseItem("echo_shard"));

	public static final ModItems[] CHEST_BOATS = new ModItems[]{OAK_CHEST_BOAT, SPRUCE_CHEST_BOAT, BIRCH_CHEST_BOAT, JUNGLE_CHEST_BOAT, ACACIA_CHEST_BOAT, DARK_OAK_CHEST_BOAT, CHERRY_CHEST_BOAT, BAMBOO_CHEST_RAFT};
	public static final ModItems[] BOATS = new ModItems[]{OAK_BOAT, SPRUCE_BOAT, BIRCH_BOAT, JUNGLE_BOAT, ACACIA_BOAT, DARK_OAK_BOAT, CHERRY_BOAT, BAMBOO_RAFT};
	public static final ModItems[] OLD_SIGN_ITEMS = new ModItems[]{ITEM_SIGN_SPRUCE, ITEM_SIGN_BIRCH, ITEM_SIGN_JUNGLE, ITEM_SIGN_ACACIA, ITEM_SIGN_DARK_OAK};

	/*
	 * Stand-in static final fields because some mods incorrectly referenced my code directly.
	 * They should be using GameRegistry.findItem but it is what it is I guess.
	 */

	//D-Mod
	@Deprecated
	public static final Item sweet_berries = SWEET_BERRIES.get();

	public static final ModItems[] VALUES = values();

	public static void init() {
        // Saved item identities remain registered when new acquisition is disabled.
        for (ModItems item : VALUES) GameRegistry.registerItem(item.get(), item.name().toLowerCase(java.util.Locale.ROOT));
    }

	final private boolean isEnabled;
	final private Item theItem;

	ModItems(boolean enabled, Item item) {
		isEnabled = enabled;
		theItem = item;
	}

	public boolean isEnabled() {
		return isEnabled;
	}

	public static boolean isModItemEnabled(Item item) {
		if (item == null) return false;
		for (ModItems modItem : VALUES) {
			if (modItem.get() == item) return modItem.isEnabled();
		}
		return false;
	}

	public Item get() {
		return theItem;
	}

	public ItemStack newItemStack() {
		return newItemStack(1);
	}

	public ItemStack newItemStack(int count) {
		return newItemStack(count, 0);
	}

	public ItemStack newItemStack(int count, int meta) {
		return new ItemStack(this.get(), count, meta);
	}
}
