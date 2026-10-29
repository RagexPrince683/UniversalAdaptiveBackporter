package ganymedes01.etfuturum;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.*;
import cpw.mods.fml.common.event.FMLInterModComms.IMCEvent;
import cpw.mods.fml.common.event.FMLInterModComms.IMCMessage;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.ReflectionHelper;
import cpw.mods.fml.relauncher.Side;
import ganymedes01.etfuturum.api.*;
import ganymedes01.etfuturum.api.event.BlockItemIterateEvent;
import ganymedes01.etfuturum.api.utils.RecipeHelper;
import ganymedes01.etfuturum.client.BuiltInResourcePack;
import ganymedes01.etfuturum.client.DynamicSoundsResourcePack;
import ganymedes01.etfuturum.client.GrayscaleWaterResourcePack;
import ganymedes01.etfuturum.client.sound.ModSounds;
import ganymedes01.etfuturum.command.CommandFill;
import ganymedes01.etfuturum.compat.*;
import ganymedes01.etfuturum.configuration.ConfigBase;
import ganymedes01.etfuturum.configuration.configs.*;
import ganymedes01.etfuturum.core.handlers.RegistryIterateEventHandler;
import ganymedes01.etfuturum.core.handlers.ServerEventHandler;
import ganymedes01.etfuturum.core.handlers.WorldEventHandler;
import ganymedes01.etfuturum.core.proxy.CommonProxy;
import ganymedes01.etfuturum.core.utils.IInitAction;
import ganymedes01.etfuturum.core.utils.Logger;
import ganymedes01.etfuturum.entities.ModEntityList;
import ganymedes01.etfuturum.items.ItemWoodSign;
import ganymedes01.etfuturum.lib.Reference;
import ganymedes01.etfuturum.network.*;
import ganymedes01.etfuturum.potion.ModPotions;
import ganymedes01.etfuturum.recipes.ModRecipes;
import ganymedes01.etfuturum.recipes.ModTagging;
import ganymedes01.etfuturum.recipes.SmithingTableRecipes;
import ganymedes01.etfuturum.spectator.SpectatorMode;
import ganymedes01.etfuturum.world.EtFuturumEarlyWorldGenerator;
import ganymedes01.etfuturum.world.EtFuturumLateWorldGenerator;
import ganymedes01.etfuturum.world.EtFuturumWorldGenerator;
import ganymedes01.etfuturum.world.end.dimension.DimensionProviderEFREnd;
import ganymedes01.etfuturum.world.end.gen.EndCityPieces;
import ganymedes01.etfuturum.world.end.gen.MapGenEndCity;
import ganymedes01.etfuturum.world.nether.biome.utils.NetherBiomeManager;
import ganymedes01.etfuturum.world.nether.dimension.DimensionProviderEFRNether;
import ganymedes01.etfuturum.world.structure.OceanMonument;
import makamys.mclib.core.MCLib;
import makamys.mclib.core.MCLibModules;
import makamys.mclib.ext.assetdirector.ADConfig;
import makamys.mclib.ext.assetdirector.AssetDirectorAPI;
import net.minecraft.block.Block;
import net.minecraft.block.BlockTrapDoor;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.WeightedRandomChestContent;
import net.minecraft.world.gen.structure.MapGenStructureIO;
import net.minecraftforge.common.ChestGenHooks;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.oredict.OreDictionary;
import org.apache.commons.lang3.ArrayUtils;

import java.lang.reflect.Field;
import java.util.*;

@Mod(
		modid = Tags.MOD_ID,
		name = Tags.MOD_NAME,
		version = Tags.VERSION,
		dependencies = Reference.DEPENDENCIES,
		guiFactory = Tags.MOD_GROUP + ".configuration.ConfigGuiFactory"
)

public class EtFuturum {

	@Instance(Tags.MOD_ID)
	public static EtFuturum instance;

	@SidedProxy(clientSide = Tags.MOD_GROUP + ".core.proxy.ClientProxy", serverSide = Tags.MOD_GROUP + ".core.proxy.CommonProxy")
	public static CommonProxy proxy;

	public static SimpleNetworkWrapper networkWrapper;

	@cpw.mods.fml.common.network.NetworkCheckHandler
	public boolean checkNetwork(java.util.Map<String, String> remote, Side side) {
		if (!remote.containsKey(Tags.MOD_ID)) return side == Side.SERVER;
		return Tags.VERSION.equals(remote.get(Tags.MOD_ID)) && ModPotions.IDS.matches(remote) && ganymedes01.etfuturum.configuration.UabConfiguration.get().matches(remote);
	}

	@EventHandler
	public void serverStopped(FMLServerStoppedEvent event) {
		ServerPacketExecutor.stop();
		ServerEventHandler.INSTANCE.onServerStopped();
	}

	public static CreativeTabs creativeTabItems = new CreativeTabs(Tags.MOD_ID + ".items") {
		@Override
		public Item getTabIconItem() {
			return  ModItems.RAW_ORE.isEnabled() ? ModItems.RAW_ORE.get()
					: ModItems.NETHERITE_SCRAP.isEnabled() ? ModItems.NETHERITE_SCRAP.get()
					: ModItems.PRISMARINE_SHARD.isEnabled() ? ModItems.PRISMARINE_SHARD.get()
					: Items.magma_cream;
		}

		@Override
		public void displayAllReleventItems(List<ItemStack> p_78018_1_) {
			for (byte i = 1; i <= 3; i++) {
				ItemStack firework = new ItemStack(Items.fireworks);
				NBTTagCompound nbt = new NBTTagCompound();
				NBTTagCompound nbt2 = new NBTTagCompound();
				nbt2.setByte("Flight", i);
				nbt.setTag("Fireworks", nbt2);
				firework.setTagCompound(nbt);
				p_78018_1_.add(firework);
			}
			for (int i : ModEntityList.eggIDs) {
				Class<?> entityType = (Class<?>) net.minecraft.entity.EntityList.IDtoClassMapping.get(i);
				if (entityType != null && !ganymedes01.etfuturum.configuration.ContentAvailability.mobEnabled(entityType)) continue;
				p_78018_1_.add(new ItemStack(Items.spawn_egg, 1, i));
			}
			super.displayAllReleventItems(p_78018_1_);
		}
	};

	public static CreativeTabs creativeTabBlocks = new CreativeTabs(Tags.MOD_ID + ".blocks") {
		@Override
		public Item getTabIconItem() {
			return ModBlocks.COPPER_BLOCK.isEnabled() ? ModBlocks.COPPER_BLOCK.getItem()
					: ModBlocks.CHERRY_LOG.isEnabled() ? ModBlocks.CHERRY_LOG.getItem()
					: ModBlocks.SMOKER.isEnabled() ? ModBlocks.SMOKER.getItem()
					: ModBlocks.CHORUS_FLOWER.isEnabled() ? ModBlocks.CHORUS_FLOWER.getItem()
					: Item.getItemFromBlock(Blocks.ender_chest);
		}

		@Override
		public void displayAllReleventItems(List<ItemStack> list) {
			list.add(new ItemStack(Blocks.mob_spawner));
			super.displayAllReleventItems(list);

			//Remove the sign items from the list; we'll add them back in a moment
			Iterator<ItemStack> iterator = list.iterator();
			while (iterator.hasNext()) {
				ItemStack stack = iterator.next();
				for (ModItems sign : ModItems.OLD_SIGN_ITEMS) {
					if (sign.isEnabled() && stack.getItem() == sign.get()) {
						iterator.remove();
					}
				}
			}

			//Add the sign items back but in a way so they are sorted by their block ID instead of their item ID.
			//This allows them to be in the correct place instead of always at the bottom of the block ID list, since item IDs are always above block IDs
			for (ModItems sign : ModItems.OLD_SIGN_ITEMS) {
				if (!sign.isEnabled()) continue;
				for (ItemStack stack : list) {
					if (Item.getIdFromItem(stack.getItem()) > Block.getIdFromBlock(((ItemWoodSign) sign.get()).getSignBlock())) {
						list.add(list.indexOf(stack), sign.newItemStack());
						break;
					}
				}
			}
		}
	};

	@EventHandler
	public void onConstruction(FMLConstructionEvent event) {
		if(Reference.SNAPSHOT_BUILD && !Reference.DEV_ENVIRONMENT) {
			Logger.info(Tags.MOD_ID + " is in snapshot mode. Disabling update checker... Other features may also be different.");
		}

		ConfigBase.onConstructing();
		ModPotions.reserveIds();
		MCLib.init();

		ADConfig config = new ADConfig();

		getSounds(config);

		AssetDirectorAPI.register(config);

		FMLCommonHandler.instance().bus().register(RegistryIterateEventHandler.INSTANCE);
		MinecraftForge.EVENT_BUS.register(RegistryIterateEventHandler.INSTANCE);

		ModTagging.registerEarlyTags();
	}

	static final String NETHER_FORTRESS = "netherFortress";
	private Field fortressWeightedField;

	@EventHandler
	@SuppressWarnings("unchecked")
	public void preInit(FMLPreInitializationEvent event) {
		RecipeHelper.init();
		if (event.getSide() == Side.CLIENT && ConfigSounds.armorEquip) {
			ArmorSoundsRegistry.init();
			MinecraftForge.EVENT_BUS.register(ArmorSoundsRegistry.INSTANCE);
		}
		if(ModsList.IRON_CHEST.isLoaded()) {
			CompatIronChests.init();
		}
		try {
			Field chestInfo = ChestGenHooks.class.getDeclaredField("chestInfo");
			chestInfo.setAccessible(true);
			if (!((HashMap<String, ChestGenHooks>) chestInfo.get(null)).containsKey(NETHER_FORTRESS)) {
				fortressWeightedField = Class.forName("net.minecraft.world.gen.structure.StructureNetherBridgePieces$Piece").getDeclaredField("field_111019_a");
				fortressWeightedField.setAccessible(true);
				((HashMap<String, ChestGenHooks>) chestInfo.get(null)).put(NETHER_FORTRESS, new ChestGenHooks(NETHER_FORTRESS, (WeightedRandomChestContent[]) fortressWeightedField.get(null), 2, 5));
			}
		} catch (Exception e) {
			System.out.println("Failed to get Nether fortress loot table:");
			e.printStackTrace();
		}

		ModBlocks.init();
		ModItems.init();
        ModernAdditions.initialize();
        ganymedes01.etfuturum.core.handlers.SculkEventHandler.initialize();
		ganymedes01.etfuturum.configuration.ContentAvailability.indexContent();
		ExternalWoodCompat.initContent();
		ModEnchantments.init();
		ModPotions.init();
		MinecraftForge.EVENT_BUS.register(ModPotions.IDS);
		SpectatorMode.init();

		for (ModBlocks block : ModBlocks.values()) {
			if (block.isEnabled() && block.get() instanceof IInitAction) {
				((IInitAction) block.get()).preInitAction();
			}
		}
		for (ModItems item : ModItems.values()) {
			if (item.isEnabled() && item.get() instanceof IInitAction) {
				((IInitAction) item.get()).preInitAction();
			}
		}

		if (event.getSide() == Side.CLIENT) {

			if (ConfigFunctions.enableNewTextures || ConfigFunctions.enableLangReplacements) {
				BuiltInResourcePack.register("vanilla_overrides");
			}

			GrayscaleWaterResourcePack.inject();

			DynamicSoundsResourcePack.inject();
		}

		NetherBiomeManager.init();
		ganymedes01.etfuturum.world.surface.SurfaceBiomeManager.init();

		GameRegistry.registerWorldGenerator(EtFuturumEarlyWorldGenerator.INSTANCE, Integer.MIN_VALUE);
		GameRegistry.registerWorldGenerator(EtFuturumWorldGenerator.INSTANCE, 0);
		GameRegistry.registerWorldGenerator(ganymedes01.etfuturum.world.end.gen.EndCityGenerationBridge.INSTANCE, 0);
		MinecraftForge.EVENT_BUS.register(ganymedes01.etfuturum.world.end.gen.EndCityGenerationBridge.INSTANCE);
		GameRegistry.registerWorldGenerator(EtFuturumLateWorldGenerator.INSTANCE, Integer.MAX_VALUE);

		OceanMonument.makeMap();

		NetworkRegistry.INSTANCE.registerGuiHandler(instance, proxy);
		networkWrapper = NetworkRegistry.INSTANCE.newSimpleChannel(Tags.MOD_ID);
		networkWrapper.registerMessage(ArmourStandInteractHandler.class, ArmourStandInteractMessage.class, 0, Side.SERVER);
		networkWrapper.registerMessage(BlackHeartParticlesHandler.class, BlackHeartParticlesMessage.class, 1, Side.CLIENT);
		networkWrapper.registerMessage(WoodSignOpenHandler.class, WoodSignOpenMessage.class, 3, Side.CLIENT);
		networkWrapper.registerMessage(BoatMoveHandler.class, BoatMoveMessage.class, 4, Side.SERVER);
		networkWrapper.registerMessage(ChestBoatOpenInventoryHandler.class, ChestBoatOpenInventoryMessage.class, 5, Side.SERVER);
		networkWrapper.registerMessage(StartElytraFlyingHandler.class, StartElytraFlyingMessage.class, 6, Side.SERVER);
		networkWrapper.registerMessage(AttackYawHandler.class, AttackYawMessage.class, 7, Side.CLIENT);

		if (!Reference.SNAPSHOT_BUILD && !Reference.DEV_ENVIRONMENT) {
			MCLibModules.updateCheckAPI.submitModTask(Tags.MOD_ID, Tags.VERSION, Reference.VERSION_URL);
		}

		CompatMisc.runModHooksPreInit();

		if(ModsList.RPLE.isLoaded()) {
			CompatRPLEEventHandler.registerRPLECompat();
		}
	}

	@EventHandler
	public void init(FMLInitializationEvent event) {
		// Iterate after every mod has completed pre-init, matching the former utility lifecycle.
		for (Object entryObject : Block.blockRegistry.getKeys()) {
			String id = entryObject.toString();
			MinecraftForge.EVENT_BUS.post(new BlockItemIterateEvent.BlockRegister.Init((Block) Block.blockRegistry.getObject(id), id));
		}
		for (Object entryObject : Item.itemRegistry.getKeys()) {
			String id = entryObject.toString();
			MinecraftForge.EVENT_BUS.post(new BlockItemIterateEvent.ItemRegister.Init((Item) Item.itemRegistry.getObject(id), id));
		}
		for (ModBlocks block : ModBlocks.values()) {
			if (block.isEnabled() && block.get() instanceof IInitAction) {
				((IInitAction) block.get()).initAction();
			}
		}
		for (ModItems item : ModItems.values()) {
			if (item.isEnabled() && item.get() instanceof IInitAction) {
				((IInitAction) item.get()).initAction();
			}
		}

		if (ModsList.WAILA.isLoaded()) {
			CompatWaila.register();
		}

		proxy.registerEvents();
		proxy.registerEntities();
		ganymedes01.etfuturum.core.handlers.HoneyDispenserBehaviors.register();
		net.minecraftforge.common.MinecraftForge.EVENT_BUS.register(new ganymedes01.etfuturum.combat.MaceCombat());
		proxy.registerRenderers();

		CompatMisc.runModHooksInit();

		ConfigBase.init();
	}

	@EventHandler
	public void processIMCRequests(IMCEvent event) {
		for (IMCMessage message : event.getMessages()) {
			if (message.key.equals("register-brewing-fuel")) {
				NBTTagCompound nbt = message.getNBTValue();
				ItemStack stack = ItemStack.loadItemStackFromNBT(nbt.getCompoundTag("Fuel"));
				int brews = nbt.getInteger("Brews");
				try {
					BrewingFuelRegistry.registerFuel(stack, brews);
				} catch (IllegalArgumentException invalidFuel) {
					Logger.warn("Ignoring invalid register-brewing-fuel message from " + message.getSender() + ": " + invalidFuel.getMessage());
				}
			}
		}
	}

	@EventHandler
	@SuppressWarnings("unchecked")
	public void postInit(FMLPostInitializationEvent event) {
		if (ConfigFunctions.enableUpdatedFoodValues) {
			((ItemFood) Items.carrot).healAmount = 3;
			((ItemFood) Items.baked_potato).healAmount = 5;
		}

		if (ConfigFunctions.enableUpdatedHarvestLevels) {
			Blocks.packed_ice.setHarvestLevel("pickaxe", 0);
			Blocks.ladder.setHarvestLevel("axe", 0);
			Blocks.melon_block.setHarvestLevel("axe", 0);
		}

		if (ConfigFunctions.enableFloatingTrapDoors) {
			BlockTrapDoor.disableValidation = true;
		}

		CompatMisc.runModHooksPostInit();

		Items.blaze_rod.setFull3D();
		Blocks.trapped_chest.setCreativeTab(CreativeTabs.tabRedstone);

		if (ConfigBlocksItems.enableOtherside) {
			ChestGenHooks.addItem(ChestGenHooks.STRONGHOLD_CORRIDOR, new WeightedRandomChestContent(ModItems.OTHERSIDE_RECORD.get(), 0, 1, 1, 1));
			ChestGenHooks.addItem(ChestGenHooks.DUNGEON_CHEST, new WeightedRandomChestContent(ModItems.OTHERSIDE_RECORD.get(), 0, 1, 1, 1));
		}

		if (ConfigBlocksItems.enablePrecipice) {
			ChestGenHooks.addItem(ChestGenHooks.STRONGHOLD_CORRIDOR, new WeightedRandomChestContent(ModItems.PRECIPICE_RECORD.get(), 0, 1, 1, 1));
			ChestGenHooks.addItem(ChestGenHooks.DUNGEON_CHEST, new WeightedRandomChestContent(ModItems.PRECIPICE_RECORD.get(), 0, 1, 1, 1));
		}

		if (ConfigBlocksItems.enableCreatorMusicBox) {
			ChestGenHooks.addItem(ChestGenHooks.STRONGHOLD_CORRIDOR, new WeightedRandomChestContent(ModItems.CREATOR_MUSIC_BOX_RECORD.get(), 0, 1, 1, 1));
			ChestGenHooks.addItem(ChestGenHooks.DUNGEON_CHEST, new WeightedRandomChestContent(ModItems.CREATOR_MUSIC_BOX_RECORD.get(), 0, 1, 1, 1));
		}

		if (ConfigBlocksItems.enableCreator) {
			ChestGenHooks.addItem(ChestGenHooks.STRONGHOLD_CORRIDOR, new WeightedRandomChestContent(ModItems.CREATOR_RECORD.get(), 0, 1, 1, 1));
			ChestGenHooks.addItem(ChestGenHooks.DUNGEON_CHEST, new WeightedRandomChestContent(ModItems.CREATOR_RECORD.get(), 0, 1, 1, 1));
		}

		if (ConfigBlocksItems.enable5) {
			ChestGenHooks.addItem(ChestGenHooks.STRONGHOLD_CORRIDOR, new WeightedRandomChestContent(ModItems.DISC_FRAGMENT_5.get(), 0, 1, 1, 1));
			ChestGenHooks.addItem(ChestGenHooks.DUNGEON_CHEST, new WeightedRandomChestContent(ModItems.DISC_FRAGMENT_5.get(), 0, 1, 1, 1));
		}

		if (ConfigBlocksItems.enablePigstep) {
			ChestGenHooks.addItem(NETHER_FORTRESS, new WeightedRandomChestContent(ModItems.PIGSTEP_RECORD.get(), 0, 1, 1, 5));

			if (fortressWeightedField != null) {
				try {
					Field contents = ChestGenHooks.class.getDeclaredField("contents");
					contents.setAccessible(true);
					ArrayList<WeightedRandomChestContent> fortressContentList;
					fortressContentList = (ArrayList<WeightedRandomChestContent>) contents.get(ChestGenHooks.getInfo("netherFortress"));
					if (!fortressContentList.isEmpty()) {
						WeightedRandomChestContent[] fortressChest = new WeightedRandomChestContent[fortressContentList.size()];
						for (int i = 0; i < fortressContentList.size(); i++) {
							fortressChest[i] = fortressContentList.get(i);
						}
						fortressWeightedField.set(null, fortressChest);
					}
				} catch (Exception e) {
					System.out.println("Failed to fill Nether fortress loot table:");
					e.printStackTrace();
				}
			}
		}

		for (ModBlocks block : ModBlocks.values()) {
			if (block.isEnabled() && block.get() instanceof IInitAction) {
				((IInitAction) block.get()).postInitAction();
			}
		}
		for (ModItems item : ModItems.values()) {
			if (item.isEnabled() && item.get() instanceof IInitAction) {
				((IInitAction) item.get()).postInitAction();
			}
		}

		if (ConfigModCompat.elytraBaublesExpandedCompat > 0 && ModsList.BAUBLES_EXPANDED.isLoaded()) {
			CompatBaublesExpanded.postInit();
		}

		EtFuturumLootTables.init();
		ganymedes01.etfuturum.world.nether.structure.BastionLoot.init();
		MapGenStructureIO.registerStructure(ganymedes01.etfuturum.world.structure.surface.SurfaceStructures.Start.class, "UABSurfaceStructures");
		MapGenStructureIO.func_143031_a(ganymedes01.etfuturum.world.structure.surface.SurfaceStructures.Piece.class, "UABSurfacePiece");
		MapGenStructureIO.registerStructure(ganymedes01.etfuturum.world.structure.underground.UndergroundStructures.Start.class, "EFRUnderground");
		MapGenStructureIO.func_143031_a(ganymedes01.etfuturum.world.structure.underground.UndergroundStructures.Piece.class, "EFRUndergroundPiece");
		MapGenStructureIO.registerStructure(ganymedes01.etfuturum.world.nether.structure.MapGenBastion.Start.class, "EFRBastion");
		MapGenStructureIO.registerStructure(ganymedes01.etfuturum.world.structure.MapGenOceanMonument.Start.class, "EFROceanMonument");
		MapGenStructureIO.func_143031_a(ganymedes01.etfuturum.world.structure.MapGenOceanMonument.Piece.class, "EFRMonumentPiece");
		MapGenStructureIO.func_143031_a(ganymedes01.etfuturum.world.nether.structure.MapGenBastion.Piece.class, "EFRBastionPiece");
		if (ConfigExperiments.enableEndCities) {
			MapGenStructureIO.registerStructure(MapGenEndCity.Start.class, "EndCity");
			MapGenStructureIO.func_143031_a(EndCityPieces.EndCityPiece.class, "ECP");
		}
		ModRecipes.init();
		ExternalWoodCompat.initRecipesAndStripping();
		DeepslateOreRegistry.init();
		StrippedLogRegistry.init();
		RawOreRegistry.init();
		SmithingTableRecipes.init();
		CompostingRegistry.init();
		BeePlantRegistry.init();
		PistonBehaviorRegistry.init();

		if (ModsList.TINKERS_CONSTRUCT.isLoaded()) {
			CompatTinkersConstruct.postInit();
		}
	}

	@EventHandler
	@SuppressWarnings("unchecked")
	public void onLoadComplete(FMLLoadCompleteEvent e) {
		ModPotions.IDS.verify();
		for (ModBlocks block : ModBlocks.values()) {
			if (block.isEnabled() && block.get() instanceof IInitAction) {
				((IInitAction) block.get()).onLoadAction();
			}
		}
		for (ModItems item : ModItems.values()) {
			if (item.isEnabled() && item.get() instanceof IInitAction) {
				((IInitAction) item.get()).onLoadAction();
			}
		}

		EtFuturumWorldGenerator.INSTANCE.postInit();
		WorldEventHandler.INSTANCE.postInit();

		if (ConfigSounds.newBlockSounds) {
			Blocks.jukebox.setStepSound(Block.soundTypeWood);
			Blocks.noteblock.setStepSound(Block.soundTypeWood);
			Blocks.heavy_weighted_pressure_plate.setStepSound(Block.soundTypeMetal);
			Blocks.light_weighted_pressure_plate.setStepSound(Block.soundTypeMetal);
			Blocks.tripwire_hook.setStepSound(Block.soundTypeWood);
			Blocks.lever.setStepSound(Block.soundTypeStone);
			Blocks.powered_repeater.setStepSound(Block.soundTypeStone);
			Blocks.unpowered_repeater.setStepSound(Block.soundTypeStone);
			Blocks.powered_comparator.setStepSound(Block.soundTypeStone);
			Blocks.unpowered_comparator.setStepSound(Block.soundTypeStone);
			Blocks.sponge.setStepSound(ModSounds.soundSponge);
		}
		if (ConfigSounds.paintingItemFramePlacing) {
			Block block = GameRegistry.findBlock("torchLevers", "paintingDoor");
			if(block != null) {
				block.stepSound = ModSounds.soundPainting;
			}
		}
		if (ConfigBlocksItems.enableDyedBeds) {
			Blocks.bed.blockMaterial = Material.wood;
			Blocks.bed.setStepSound(Block.soundTypeWood);
		}

		CompatMisc.runModHooksLoadComplete();

		if (ConfigExperiments.netherDimensionProvider && !ModsList.NETHERLICIOUS.isLoaded()) {
			DimensionProviderEFRNether.init();
		}

		if (ConfigExperiments.endDimensionProvider && !ModsList.ENDERLICIOUS.isLoaded() && !ModsList.HARDCORE_ENDER_EXPANSION.isLoaded()) {
			DimensionProviderEFREnd.init(); // Come back to
		}
	}

	@EventHandler
	public void onMissingMapping(FMLMissingMappingsEvent e) {
		for (FMLMissingMappingsEvent.MissingMapping mapping : e.getAll()) {
			if (mapping.name.startsWith("etfuturum")) {
				if(mapping.type == GameRegistry.Type.ITEM) {
					if (Block.getBlockById(mapping.id) != null) {
						// As of 2.5.0, I removed some ItemBlocks that are just technical blocks (EG, lit EFR furnaces)
						// We need to use this event since unregistering specifically an ItemBlock from a block makes Forge mistakenly think a save is corrupted.
						// I add the EFR name check at the beginning just as a safety precaution.
						//
						// Forge does some bad checks on if the item is an ItemBlock before letting you run ignoreItemBlock, leading to erroneous errors.
						// It doesn't look much different than what I do above but their code rarely spits out "Cannot skip an ItemBlock that doesn't have a Block"
						// Which makes no sense since if the block != null then we're skipping an ItemBlock that DOES have a block, if my block check != null then what else would it be?
						// So their check must be wrong. Some of Forge's many registry finders are a little faulty at times.
						// I already know we're running this on an item, and the only other requirement has a broken check.
						// So I use reflection to force my way. It's rare for a save to actually throw an error, but just in case....
						// They really should have just had an ITEMBLOCK mapping type to avoid all these hacky checks.
						//
						// All this because Forge falsely declares a world corrupt if you remove an ItemBlock from an existing block.
						// Gee, all that for removing ItemBlocks.
						// I wrote this bad code to get around Forge's bad code, only to reveal EVEN MORE bad code in Forge I have to write even worse code to avoid.
						mapping.ignore();
						ReflectionHelper.setPrivateValue(FMLMissingMappingsEvent.MissingMapping.class, mapping, FMLMissingMappingsEvent.Action.BLOCKONLY, "action");
					}
				}
			}
			if (mapping.name.equals("etfuturum:glow_berries")) {
				mapping.ignore();
			}
		}
	}


	@EventHandler
	public void serverStarting(FMLServerStartingEvent event) {
		ServerPacketExecutor.start();
		event.registerServerCommand(new ganymedes01.etfuturum.command.CommandGameplayAudit());
		event.registerServerCommand(new ganymedes01.etfuturum.command.CommandLocateStructure());
		if (ConfigFunctions.enableFillCommand) {
			event.registerServerCommand(new CommandFill());
		}
		if (ConfigExperiments.enableEndCities) {
			event.registerServerCommand(new ganymedes01.etfuturum.command.CommandLocateEndCity());
		}
	}

	/**
	 * Utility for running string.contains() on a list of strings.
	 */
	public static boolean stringListContainsPhrase(Set<String> set, String string) {
		for (String stringInSet : set) {
			if (string.contains(stringInSet)) {
				return true;
			}
		}
		return false;
	}

	public static List<String> getOreStrings(ItemStack stack) {
		final List<String> list = new ArrayList<>();
		for (int oreID : OreDictionary.getOreIDs(stack)) {
			list.add(OreDictionary.getOreName(oreID));
		}
		return list;
	}

	public static boolean hasDictTag(Block block, String... tags) {
		return hasDictTag(new ItemStack(block), tags);
	}

	public static boolean hasDictTag(Item item, String... tags) {
		return hasDictTag(new ItemStack(item), tags);
	}

	public static boolean hasDictTag(ItemStack stack, String... tags) {
		for (String oreName : getOreStrings(stack)) {
			if (ArrayUtils.contains(tags, oreName)) {
				return true;
			}
		}
		return false;
	}

	public static boolean dictTagsStartWith(Block block, String stringToFind) {
		return dictTagsContain(new ItemStack(block), stringToFind);
	}

	public static boolean dictTagsStartWith(Item item, String stringToFind) {
		return dictTagsContain(new ItemStack(item), stringToFind);
	}

	public static boolean dictTagsStartWith(ItemStack stack, String stringToFind) {
		for (String oreName : getOreStrings(stack)) {
			if (oreName.startsWith(stringToFind)) {
				return true;
			}
		}
		return false;
	}

	public static boolean dictTagsContain(Block block, String stringToFind) {
		return dictTagsContain(new ItemStack(block), stringToFind);
	}

	public static boolean dictTagsContain(Item item, String stringToFind) {
		return dictTagsContain(new ItemStack(item), stringToFind);
	}

	public static boolean dictTagsContain(ItemStack stack, String stringToFind) {
		for (String oreName : getOreStrings(stack)) {
			if (oreName.contains(stringToFind)) {
				return true;
			}
		}
		return false;
	}

	public static PotionEffect getSuspiciousStewEffect(ItemStack stack) {

		if (stack == null)
			return null;

		Item item = stack.getItem();
		if (ModBlocks.EYEBLOSSOM.isEnabled() && item == ModBlocks.EYEBLOSSOM.getItem()) {
			return (stack.getItemDamage() & 1) == 0 ? new PotionEffect(Potion.confusion.id, 140, 0) : new PotionEffect(Potion.blindness.id, 220, 0);
		}

		if (item == Item.getItemFromBlock(Blocks.red_flower)) {
			switch (stack.getItemDamage()) {
				default:
				case 0:
					return new PotionEffect(Potion.nightVision.id, 100, 0);
				case 1:
					return new PotionEffect(Potion.field_76443_y.id, 7, 0); // saturation
				case 2:
					return new PotionEffect(Potion.fireResistance.id, 80, 0);
				case 3:
					return new PotionEffect(Potion.blindness.id, 160, 0);
				case 4:
				case 5:
				case 6:
				case 7:
					return new PotionEffect(Potion.weakness.id, 180, 0);
				case 8:
					return new PotionEffect(Potion.regeneration.id, 160, 0);
			}
		}

		if (item == Item.getItemFromBlock(Blocks.yellow_flower)) {
			return new PotionEffect(Potion.field_76443_y.id, 7, 0); // saturation
		}

		if (item == Item.getItemFromBlock(ModBlocks.CORNFLOWER.get())) {
			return new PotionEffect(Potion.jump.id, 120, 0);
		}

		if (item == Item.getItemFromBlock(ModBlocks.LILY_OF_THE_VALLEY.get())) {
			return new PotionEffect(Potion.poison.id, 240, 0);
		}

		if (item == Item.getItemFromBlock(ModBlocks.WITHER_ROSE.get())) {
			return new PotionEffect(Potion.wither.id, 160, 0);
		}
		return null;
	}

	private static void getSounds(ADConfig config) {
		String ver = Tags.MC_ASSET_VER.split("_")[1];
		for (String sound : new String[]{"ambient", "death", "eat", "fly", "hurt", "step"}) config.addSoundEvent(ver, "entity.parrot." + sound, "neutral");
		for (String mob : ganymedes01.etfuturum.entities.EntityParrot.IMITATIONS) config.addSoundEvent(ver, "entity.parrot.imitate." + mob, "neutral");
		for (String sound : new String[]{"entity.allay.ambient_with_item", "entity.allay.ambient_without_item", "entity.allay.death", "entity.allay.hurt", "entity.allay.item_given", "entity.allay.item_taken", "entity.allay.item_thrown", "entity.armadillo.ambient", "entity.armadillo.brush", "entity.armadillo.death", "entity.armadillo.eat", "entity.armadillo.hurt", "entity.armadillo.hurt_reduced", "entity.armadillo.land", "entity.armadillo.peek", "entity.armadillo.roll", "entity.armadillo.scute_drop", "entity.armadillo.step", "entity.armadillo.unroll_finish", "entity.armadillo.unroll_start", "entity.bogged.ambient", "entity.bogged.death", "entity.bogged.hurt", "entity.bogged.shear", "entity.bogged.step", "entity.breeze.charge", "entity.breeze.death", "entity.breeze.deflect", "entity.breeze.hurt", "entity.breeze.idle_air", "entity.breeze.idle_ground", "entity.breeze.inhale", "entity.breeze.jump", "entity.breeze.land", "entity.breeze.shoot", "entity.breeze.slide", "entity.breeze.whirl", "entity.breeze.wind_burst", "entity.camel.ambient", "entity.camel.dash", "entity.camel.dash_ready", "entity.camel.death", "entity.camel.eat", "entity.camel.hurt", "entity.camel.saddle", "entity.camel.sit", "entity.camel.stand", "entity.camel.step", "entity.camel.step_sand", "entity.camel_husk.ambient", "entity.camel_husk.dash", "entity.camel_husk.dash_ready", "entity.camel_husk.death", "entity.camel_husk.eat", "entity.camel_husk.hurt", "entity.camel_husk.saddle", "entity.camel_husk.sit", "entity.camel_husk.stand", "entity.camel_husk.step", "entity.camel_husk.step_sand", "entity.cat.ambient", "entity.cat.beg_for_food", "entity.cat.death", "entity.cat.eat", "entity.cat.hiss", "entity.cat.hurt", "entity.cat.purr", "entity.cat.purreow", "entity.cat.stray_ambient", "entity.copper_golem.death", "entity.copper_golem.hurt", "entity.copper_golem.item_drop", "entity.copper_golem.item_no_drop", "entity.copper_golem.no_item_get", "entity.copper_golem.no_item_no_get", "entity.copper_golem.shear", "entity.copper_golem.spawn", "entity.copper_golem.spin", "entity.copper_golem.step", "entity.evoker.ambient", "entity.evoker.cast_spell", "entity.evoker.celebrate", "entity.evoker.death", "entity.evoker.hurt", "entity.evoker.prepare_attack", "entity.evoker.prepare_summon", "entity.evoker.prepare_wololo", "entity.ghastling.ambient", "entity.ghastling.death", "entity.ghastling.hurt", "entity.ghastling.spawn", "entity.guardian.ambient", "entity.guardian.ambient_land", "entity.guardian.attack", "entity.guardian.death", "entity.guardian.death_land", "entity.guardian.flop", "entity.guardian.hurt", "entity.guardian.hurt_land", "entity.happy_ghast.ambient", "entity.happy_ghast.death", "entity.happy_ghast.equip", "entity.happy_ghast.harness_goggles_down", "entity.happy_ghast.harness_goggles_up", "entity.happy_ghast.hurt", "entity.happy_ghast.riding", "entity.happy_ghast.unequip", "entity.hoglin.ambient", "entity.hoglin.angry", "entity.hoglin.attack", "entity.hoglin.converted_to_zombified", "entity.hoglin.death", "entity.hoglin.hurt", "entity.hoglin.retreat", "entity.hoglin.step", "entity.illusioner.ambient", "entity.illusioner.cast_spell", "entity.illusioner.death", "entity.illusioner.hurt", "entity.illusioner.mirror_move", "entity.illusioner.prepare_blindness", "entity.illusioner.prepare_mirror", "entity.llama.ambient", "entity.llama.angry", "entity.llama.chest", "entity.llama.death", "entity.llama.eat", "entity.llama.hurt", "entity.llama.spit", "entity.llama.step", "entity.llama.swag", "entity.nautilus.ambient", "entity.nautilus.ambient_land", "entity.nautilus.dash", "entity.nautilus.dash_land", "entity.nautilus.dash_ready", "entity.nautilus.dash_ready_land", "entity.nautilus.death", "entity.nautilus.death_land", "entity.nautilus.eat", "entity.nautilus.hurt", "entity.nautilus.hurt_land", "entity.nautilus.riding", "entity.nautilus.swim", "entity.panda.aggressive_ambient", "entity.panda.ambient", "entity.panda.bite", "entity.panda.cant_breed", "entity.panda.death", "entity.panda.eat", "entity.panda.hurt", "entity.panda.pre_sneeze", "entity.panda.sneeze", "entity.panda.step", "entity.panda.worried_ambient", "entity.parched.ambient", "entity.parched.death", "entity.parched.hurt", "entity.parched.step", "entity.piglin.admiring_item", "entity.piglin.ambient", "entity.piglin.angry", "entity.piglin.celebrate", "entity.piglin.converted_to_zombified", "entity.piglin.death", "entity.piglin.hurt", "entity.piglin.jealous", "entity.piglin.retreat", "entity.piglin.step", "entity.piglin_brute.ambient", "entity.piglin_brute.angry", "entity.piglin_brute.converted_to_zombified", "entity.piglin_brute.death", "entity.piglin_brute.hurt", "entity.piglin_brute.step", "entity.pillager.ambient", "entity.pillager.celebrate", "entity.pillager.death", "entity.pillager.hurt", "entity.ravager.ambient", "entity.ravager.attack", "entity.ravager.celebrate", "entity.ravager.death", "entity.ravager.hurt", "entity.ravager.roar", "entity.ravager.step", "entity.ravager.stunned", "entity.sniffer.death", "entity.sniffer.digging", "entity.sniffer.digging_stop", "entity.sniffer.drop_seed", "entity.sniffer.eat", "entity.sniffer.happy", "entity.sniffer.hurt", "entity.sniffer.idle", "entity.sniffer.scenting", "entity.sniffer.searching", "entity.sniffer.sniffing", "entity.sniffer.step", "entity.strider.ambient", "entity.strider.death", "entity.strider.eat", "entity.strider.happy", "entity.strider.hurt", "entity.strider.retreat", "entity.strider.saddle", "entity.strider.step", "entity.strider.step_lava", "entity.vex.ambient", "entity.vex.charge", "entity.vex.death", "entity.vex.hurt", "entity.vindicator.ambient", "entity.vindicator.celebrate", "entity.vindicator.death", "entity.vindicator.hurt", "entity.wandering_trader.ambient", "entity.wandering_trader.death", "entity.wandering_trader.disappeared", "entity.wandering_trader.drink_milk", "entity.wandering_trader.drink_potion", "entity.wandering_trader.hurt", "entity.wandering_trader.no", "entity.wandering_trader.reappeared", "entity.wandering_trader.trade", "entity.wandering_trader.yes", "entity.warden.agitated", "entity.warden.ambient", "entity.warden.angry", "entity.warden.attack_impact", "entity.warden.death", "entity.warden.dig", "entity.warden.emerge", "entity.warden.heartbeat", "entity.warden.hurt", "entity.warden.listening", "entity.warden.listening_angry", "entity.warden.nearby_close", "entity.warden.nearby_closer", "entity.warden.nearby_closest", "entity.warden.roar", "entity.warden.sniff", "entity.warden.sonic_boom", "entity.warden.sonic_charge", "entity.warden.step", "entity.warden.tendril_clicks", "entity.zoglin.ambient", "entity.zoglin.angry", "entity.zoglin.attack", "entity.zoglin.death", "entity.zoglin.hurt", "entity.zoglin.step", "entity.zombie_nautilus.ambient", "entity.zombie_nautilus.ambient_land", "entity.zombie_nautilus.dash", "entity.zombie_nautilus.dash_land", "entity.zombie_nautilus.dash_ready", "entity.zombie_nautilus.dash_ready_land", "entity.zombie_nautilus.death", "entity.zombie_nautilus.death_land", "entity.zombie_nautilus.eat", "entity.zombie_nautilus.hurt", "entity.zombie_nautilus.hurt_land", "entity.zombie_nautilus.swim"}) config.addSoundEvent(ver, sound, "neutral");
		for (String mob : new String[]{"cod", "salmon", "tropical_fish", "puffer_fish", "dolphin", "turtle", "axolotl", "glow_squid", "phantom", "drowned"}) {
			config.addSoundEvent(ver, "entity." + mob + ".hurt", "neutral");
			config.addSoundEvent(ver, "entity." + mob + ".death", "neutral");
		}
		for (String sound : new String[]{"puffer_fish.blow_up", "puffer_fish.blow_out", "puffer_fish.sting", "dolphin.ambient_water", "dolphin.eat", "turtle.ambient_land", "axolotl.idle_air", "axolotl.idle_water", "glow_squid.ambient", "phantom.ambient", "phantom.bite", "phantom.swoop", "drowned.ambient", "drowned.ambient_water", "drowned.shoot"}) config.addSoundEvent(ver, "entity." + sound, "neutral");
		for (String sound : new String[]{"copper_chest.open", "copper_chest.close", "sculk_shrieker.shriek", "sniffer_egg.crack", "sniffer_egg.hatch", "frogspawn.hatch", "trial_spawner.spawn_mob", "trial_spawner.eject_item", "trial_spawner.open_shutter", "trial_spawner.close_shutter", "dried_ghast.transition"}) config.addSoundEvent(ver, "block." + sound, "block");
		for (String sound : new String[]{"egg_break", "egg_crack", "egg_hatch"}) config.addSoundEvent(ver, "entity.turtle." + sound, "block");
		for (String sound : new String[]{"loading_start", "loading_middle", "loading_end", "shoot"}) config.addSoundEvent(ver, "item.crossbow." + sound, "player");
		for (String sound : new String[]{"smash_air", "smash_ground", "smash_ground_heavy"}) config.addSoundEvent(ver, "item.mace." + sound, "player");
		config.addSoundEvent(ver, "entity.wind_charge.wind_burst", "player");
        config.addSoundEvent(ver,"block.bell.use","block");
        config.addSoundEvent(ver,"block.bell.resonate","block");
        config.addSoundEvent(ver,"item.spyglass.use","player");
        config.addSoundEvent(ver,"item.spyglass.stop_using","player");
        config.addSoundEvent(ver,"block.dry_grass.ambient","ambient");config.addSoundEvent(ver,"block.firefly_bush.idle","ambient");
        for(String voice:new String[]{"angry","big","cute","grumpy","puglin","sad"})for(String kind:new String[]{"ambient","death","growl","hurt","pant","whine"})config.addSoundEvent(ver,"entity.wolf_"+voice+"."+kind,"neutral");

		config.addObject(ver, "minecraft/sounds/ambient/cave/cave14.ogg");
		config.addObject(ver, "minecraft/sounds/ambient/cave/cave15.ogg");
		config.addObject(ver, "minecraft/sounds/ambient/cave/cave16.ogg");
		config.addObject(ver, "minecraft/sounds/ambient/cave/cave17.ogg");
		config.addObject(ver, "minecraft/sounds/ambient/cave/cave18.ogg");
		config.addObject(ver, "minecraft/sounds/ambient/cave/cave19.ogg");

		config.addSoundEvent(ver, "weather.rain", "weather");
		config.addSoundEvent(ver, "weather.rain.above", "weather");

		config.addSoundEvent(ver, "music.nether.nether_wastes", "music");
		config.addSoundEvent(ver, "ambient.nether_wastes.additions", "ambient");
		config.addSoundEvent(ver, "ambient.nether_wastes.loop", "ambient");
		config.addSoundEvent(ver, "ambient.nether_wastes.mood", "ambient");

		config.addSoundEvent(ver, "music.nether.crimson_forest", "music");
		config.addSoundEvent(ver, "ambient.crimson_forest.additions", "ambient");
		config.addSoundEvent(ver, "ambient.crimson_forest.loop", "ambient");
		config.addSoundEvent(ver, "ambient.crimson_forest.mood", "ambient");

		config.addSoundEvent(ver, "music.nether.warped_forest", "music");
		config.addSoundEvent(ver, "ambient.warped_forest.additions", "ambient");
		config.addSoundEvent(ver, "ambient.warped_forest.loop", "ambient");
		config.addSoundEvent(ver, "ambient.warped_forest.mood", "ambient");

		config.addSoundEvent(ver, "music.nether.soul_sand_valley", "music");
		config.addSoundEvent(ver, "ambient.soul_sand_valley.additions", "ambient");
		config.addSoundEvent(ver, "ambient.soul_sand_valley.loop", "ambient");
		config.addSoundEvent(ver, "ambient.soul_sand_valley.mood", "ambient");

		config.addSoundEvent(ver, "music.nether.basalt_deltas", "music");
		config.addSoundEvent(ver, "ambient.basalt_deltas.additions", "ambient");
		config.addSoundEvent(ver, "ambient.basalt_deltas.loop", "ambient");
		config.addSoundEvent(ver, "ambient.basalt_deltas.mood", "ambient");

		config.addSoundEvent(ver, "music_disc.pigstep", "record");
		config.addSoundEvent(ver, "music_disc.otherside", "record");
		config.addSoundEvent(ver, "music_disc.precipice", "record");
		config.addSoundEvent(ver, "music_disc.creator_music_box", "record");
		config.addSoundEvent(ver, "music_disc.creator", "record");
		config.addSoundEvent(ver, "music_disc.tears", "record");
		config.addSoundEvent(ver, "music_disc.lava_chicken", "record");
		config.addSoundEvent(ver, "music_disc.5", "record");
		config.addSoundEvent(ver, "music_disc.relic", "record");

		config.addSoundEvent(ver, "item.elytra.flying", "player");
		config.addSoundEvent(ver, "enchant.thorns.hit", "player");
		config.addSoundEvent(ver, "entity.boat.paddle_land", "player");
		config.addSoundEvent(ver, "entity.boat.paddle_water", "player");
		config.addSoundEvent(ver, "entity.rabbit.ambient", "neutral");
		config.addSoundEvent(ver, "entity.rabbit.jump", "neutral");
		config.addSoundEvent(ver, "entity.rabbit.attack", "neutral");
		config.addSoundEvent(ver, "entity.rabbit.hurt", "neutral");
		config.addSoundEvent(ver, "entity.rabbit.death", "neutral");
		config.addSoundEvent(ver, "entity.zombie_villager.ambient", "hostile");
		config.addSoundEvent(ver, "entity.zombie_villager.step", "hostile");
		config.addSoundEvent(ver, "entity.zombie_villager.hurt", "hostile");
		config.addSoundEvent(ver, "entity.zombie_villager.death", "hostile");
		config.addSoundEvent(ver, "entity.husk.ambient", "hostile");
		config.addSoundEvent(ver, "entity.husk.step", "hostile");
		config.addSoundEvent(ver, "entity.husk.hurt", "hostile");
		config.addSoundEvent(ver, "entity.husk.death", "hostile");
		config.addSoundEvent(ver, "entity.zombie.converted_to_drowned", "hostile");
		config.addSoundEvent(ver, "entity.husk.converted_to_zombie", "hostile");
		config.addSoundEvent(ver, "entity.stray.ambient", "hostile");
		config.addSoundEvent(ver, "entity.stray.step", "hostile");
		config.addSoundEvent(ver, "entity.stray.hurt", "hostile");
		config.addSoundEvent(ver, "entity.stray.death", "hostile");
		config.addSoundEvent(ver, "entity.skeleton.converted_to_stray", "hostile");
		config.addSoundEvent(ver, "entity.shulker_bullet.hurt", "hostile");
		config.addSoundEvent(ver, "entity.shulker_bullet.hit", "hostile");
		config.addSoundEvent(ver, "entity.shulker.ambient", "hostile");
		config.addSoundEvent(ver, "entity.shulker.open", "hostile");
		config.addSoundEvent(ver, "entity.shulker.close", "hostile");
		config.addSoundEvent(ver, "entity.shulker.shoot", "hostile");
		config.addSoundEvent(ver, "entity.shulker.hurt", "hostile");
		config.addSoundEvent(ver, "entity.shulker.hurt_closed", "hostile");
		config.addSoundEvent(ver, "entity.shulker.death", "hostile");
		config.addSoundEvent(ver, "entity.shulker.teleport", "hostile");
		config.addSoundEvent(ver, "entity.snow_golem.ambient", "neutral");
		config.addSoundEvent(ver, "entity.snow_golem.hurt", "neutral");
		config.addSoundEvent(ver, "entity.snow_golem.death", "neutral");
		config.addSoundEvent(ver, "entity.polar_bear.ambient", "neutral");
		config.addSoundEvent(ver, "entity.polar_bear.ambient_baby", "neutral");
		config.addSoundEvent(ver, "entity.polar_bear.hurt", "neutral");
		config.addSoundEvent(ver, "entity.polar_bear.death", "neutral");
		config.addSoundEvent(ver, "entity.polar_bear.step", "neutral");
		config.addSoundEvent(ver, "entity.polar_bear.warning", "neutral");
		config.addSoundEvent(ver, "entity.goat.ambient", "neutral");
		config.addSoundEvent(ver, "entity.goat.death", "neutral");
		config.addSoundEvent(ver, "entity.goat.eat", "neutral");
		config.addSoundEvent(ver, "entity.goat.hurt", "neutral");
		config.addSoundEvent(ver, "entity.goat.long_jump", "neutral");
		config.addSoundEvent(ver, "entity.goat.milk", "neutral");
		config.addSoundEvent(ver, "entity.goat.prepare_ram", "neutral");
		config.addSoundEvent(ver, "entity.goat.ram_impact", "neutral");
		config.addSoundEvent(ver, "entity.goat.horn_break", "neutral");
		config.addSoundEvent(ver, "entity.goat.step", "neutral");
		config.addSoundEvent(ver, "entity.goat.screaming.ambient", "neutral");
		config.addSoundEvent(ver, "entity.goat.screaming.death", "neutral");
		config.addSoundEvent(ver, "entity.goat.screaming.eat", "neutral");
		config.addSoundEvent(ver, "entity.goat.screaming.hurt", "neutral");
		config.addSoundEvent(ver, "entity.goat.screaming.long_jump", "neutral");
		config.addSoundEvent(ver, "entity.goat.screaming.milk", "neutral");
		config.addSoundEvent(ver, "entity.goat.screaming.prepare_ram", "neutral");
		config.addSoundEvent(ver, "entity.goat.screaming.ram_impact", "neutral");
		for (int i = 0; i < 8; ++i) {
			config.addSoundEvent(ver, "item.goat_horn.sound." + i, "record");
		}
		config.addSoundEvent(ver, "entity.wither_skeleton.ambient", "hostile");
		config.addSoundEvent(ver, "entity.wither_skeleton.hurt", "hostile");
		config.addSoundEvent(ver, "entity.wither_skeleton.death", "hostile");
		config.addSoundEvent(ver, "entity.wither_skeleton.step", "hostile");
		config.addSoundEvent(ver, "entity.squid.ambient", "neutral");
		config.addSoundEvent(ver, "entity.squid.hurt", "neutral");
		config.addSoundEvent(ver, "entity.squid.death", "neutral");
		config.addSoundEvent(ver, "entity.squid.squirt", "neutral");
		config.addSoundEvent(ver, "entity.witch.ambient", "hostile");
		config.addSoundEvent(ver, "entity.witch.hurt", "hostile");
		config.addSoundEvent(ver, "entity.witch.death", "hostile");
		config.addSoundEvent(ver, "entity.witch.drink", "hostile");
		config.addSoundEvent(ver, "entity.item_frame.add_item", "player");
		config.addSoundEvent(ver, "entity.item_frame.break", "player");
		config.addSoundEvent(ver, "entity.item_frame.place", "player");
		config.addSoundEvent(ver, "entity.item_frame.remove_item", "player");
		config.addSoundEvent(ver, "entity.item_frame.rotate_item", "player");
		config.addSoundEvent(ver, "entity.painting.break", "player");
		config.addSoundEvent(ver, "entity.painting.place", "player");
		config.addSoundEvent(ver, "entity.leash_knot.break", "player");
		config.addSoundEvent(ver, "entity.leash_knot.place", "player");
		config.addSoundEvent(ver, "entity.ender_eye.death", "neutral");
		config.addSoundEvent(ver, "entity.ender_eye.launch", "neutral");
		config.addSoundEvent(ver, "entity.fishing_bobber.retrieve", "neutral");
		config.addSoundEvent(ver, "entity.fishing_bobber.throw", "neutral");
		config.addSoundEvent(ver, "entity.horse.eat", "neutral");
		config.addSoundEvent(ver, "entity.cow.milk", "neutral");
		config.addSoundEvent(ver, "entity.mooshroom.milk", "neutral");
		config.addSoundEvent(ver, "entity.mooshroom.convert", "neutral");
		config.addSoundEvent(ver, "entity.bee.loop", "neutral");
		config.addSoundEvent(ver, "entity.bee.loop_aggressive", "neutral");
		config.addSoundEvent(ver, "entity.bee.hurt", "neutral");
		config.addSoundEvent(ver, "entity.bee.death", "neutral");
		config.addSoundEvent(ver, "entity.bee.pollinate", "neutral");
		config.addSoundEvent(ver, "entity.bee.sting", "neutral");
		config.addSoundEvent(ver, "entity.fox.eat", "neutral");
		config.addSoundEvent(ver, "entity.fox.aggro", "neutral");
		config.addSoundEvent(ver, "entity.fox.spit", "neutral");
		config.addSoundEvent(ver, "entity.fox.screech", "neutral");
		config.addSoundEvent(ver, "entity.fox.sleep", "neutral");
		config.addSoundEvent(ver, "entity.fox.ambient", "neutral");
		config.addSoundEvent(ver, "entity.fox.hurt", "neutral");
		config.addSoundEvent(ver, "entity.fox.death", "neutral");
		config.addSoundEvent(ver, "entity.fox.bite", "neutral");
		config.addSoundEvent(ver, "entity.fox.sniff", "neutral");


		config.addSoundEvent(ver, "entity.player.hurt_on_fire", "player");
		config.addSoundEvent(ver, "entity.player.hurt_drown", "player");
		config.addSoundEvent(ver, "entity.player.hurt_sweet_berry_bush", "player");
		config.addSoundEvent(ver, "entity.player.attack.crit", "player");
		config.addSoundEvent(ver, "entity.player.attack.knockback", "player");
		config.addSoundEvent(ver, "entity.player.attack.nodamage", "player");
		config.addSoundEvent(ver, "entity.player.attack.strong", "player");
		config.addSoundEvent(ver, "entity.player.attack.sweep", "player");
		config.addSoundEvent(ver, "entity.player.attack.weak", "player");
		config.addSoundEvent(ver, "entity.player.splash.high_speed", "player");
		config.addSoundEvent(ver, "weather.end_flash", "weather");
		for (String sound : new String[]{"block.dried_ghast.spawn", "block.sculk_sensor.clicking", "block.sculk_sensor.clicking_stop", "block.vault.insert_item", "block.vault.eject_item", "block.trial_spawner.ominous_activate", "block.conduit.activate", "block.conduit.deactivate", "block.conduit.attack.target"}) config.addSoundEvent(ver, sound, "block");

		config.addSoundEvent(ver, "item.axe.scrape", "player");
		config.addSoundEvent(ver, "item.axe.wax_off", "player");
		config.addSoundEvent(ver, "item.axe.strip", "player");
		config.addSoundEvent(ver, "item.hoe.till", "player");
		config.addSoundEvent(ver, "item.honeycomb.wax_on", "player");
		config.addSoundEvent(ver, "item.totem.use", "player");
		config.addSoundEvent(ver, "item.shovel.flatten", "player");
		config.addSoundEvent(ver, "item.chorus_fruit.teleport", "player");
		config.addSoundEvent(ver, "item.book.page_turn", "player");
		config.addSoundEvent(ver, "item.bucket.fill", "player");
		config.addSoundEvent(ver, "item.bucket.fill_lava", "player");
		config.addSoundEvent(ver, "item.bucket.empty", "player");
		config.addSoundEvent(ver, "item.bucket.empty_lava", "player");
		config.addSoundEvent(ver, "item.bottle.fill", "player");
		config.addSoundEvent(ver, "item.bottle.empty", "player");
		config.addSoundEvent(ver, "item.bone_meal.use", "player");
		config.addSoundEvent(ver, "item.honey_bottle.drink", "player");

		config.addSoundEvent(ver, "item.armor.equip_leather", "player");
		config.addSoundEvent(ver, "item.armor.equip_copper", "player");
		config.addSoundEvent(ver, "item.armor.equip_gold", "player");
		config.addSoundEvent(ver, "item.armor.equip_chain", "player");
		config.addSoundEvent(ver, "item.armor.equip_iron", "player");
		config.addSoundEvent(ver, "item.armor.equip_diamond", "player");
		config.addSoundEvent(ver, "item.armor.equip_netherite", "player");
		config.addSoundEvent(ver, "item.armor.equip_turtle", "player");
		config.addSoundEvent(ver, "item.armor.equip_generic", "player");
		config.addSoundEvent(ver, "item.armor.equip_elytra", "player");

		config.addSoundEvent(ver, "block.note_block.banjo", "record");
		config.addSoundEvent(ver, "block.note_block.bell", "record");
		config.addSoundEvent(ver, "block.note_block.bit", "record");
		config.addSoundEvent(ver, "block.note_block.chime", "record");
		config.addSoundEvent(ver, "block.note_block.cow_bell", "record");
		config.addSoundEvent(ver, "block.note_block.didgeridoo", "record");
		config.addSoundEvent(ver, "block.note_block.flute", "record");
		config.addSoundEvent(ver, "block.note_block.guitar", "record");
		config.addSoundEvent(ver, "block.note_block.harp", "record");
		config.addSoundEvent(ver, "block.note_block.iron_xylophone", "record");
		config.addSoundEvent(ver, "block.note_block.xylophone", "record");

		config.addSoundEvent(ver, "block.barrel.open", "block");
		config.addSoundEvent(ver, "block.barrel.close", "block");
		config.addSoundEvent(ver, "block.chorus_flower.grow", "block");
		config.addSoundEvent(ver, "block.chorus_flower.death", "block");
		config.addSoundEvent(ver, "block.end_portal.spawn", "ambient");
		config.addSoundEvent(ver, "block.end_portal_frame.fill", "block");
		config.addSoundEvent(ver, "block.shulker_box.open", "block");
		config.addSoundEvent(ver, "block.shulker_box.close", "block");
		config.addSoundEvent(ver, "block.sweet_berry_bush.pick_berries", "player");
		config.addSoundEvent(ver, "block.brewing_stand.brew", "block");
		config.addSoundEvent(ver, "block.furnace.fire_crackle", "block");
		config.addSoundEvent(ver, "block.blastfurnace.fire_crackle", "block");
		config.addSoundEvent(ver, "block.smoker.smoke", "block");
		config.addSoundEvent(ver, "block.chest.close", "block");
		config.addSoundEvent(ver, "block.ender_chest.open", "block");
		config.addSoundEvent(ver, "block.ender_chest.close", "block");
		config.addSoundEvent(ver, "block.composter.empty", "block");
		config.addSoundEvent(ver, "block.composter.fill", "block");
		config.addSoundEvent(ver, "block.composter.fill_success", "block");
		config.addSoundEvent(ver, "block.composter.ready", "block");
		config.addSoundEvent(ver, "block.amethyst_block.hit", "block");
		config.addSoundEvent(ver, "block.amethyst_block.chime", "block");
		config.addSoundEvent(ver, "block.smithing_table.use", "player");
		config.addSoundEvent(ver, "block.enchantment_table.use", "player");
		config.addSoundEvent(ver, "block.beacon.activate", "block");
		config.addSoundEvent(ver, "block.beacon.ambient", "block");
		config.addSoundEvent(ver, "block.beacon.deactivate", "block");
		config.addSoundEvent(ver, "block.beacon.power_select", "block");
		config.addSoundEvent(ver, "block.honey_block.slide", "neutral");
		config.addSoundEvent(ver, "block.beehive.drip", "block");
		config.addSoundEvent(ver, "block.beehive.enter", "neutral");
		config.addSoundEvent(ver, "block.beehive.exit", "neutral");
		config.addSoundEvent(ver, "block.beehive.work", "neutral");
		config.addSoundEvent(ver, "block.beehive.shear", "player");
		config.addSoundEvent(ver, "block.sponge.absorb", "block");
		config.addSoundEvent(ver, "block.copper_bulb.turn_on", "block");
		config.addSoundEvent(ver, "block.copper_bulb.turn_off", "block");
		config.addSoundEvent(ver, "block.bubble_column.bubble_pop", "block");
		config.addSoundEvent(ver, "block.bubble_column.upwards_ambient", "block");
		config.addSoundEvent(ver, "block.bubble_column.upwards_inside", "neutral");
		config.addSoundEvent(ver, "block.bubble_column.whirlpool_ambient", "block");
		config.addSoundEvent(ver, "block.bubble_column.whirlpool_inside", "neutral");

		config.addSoundEvent(ver, "block.fence_gate.open", "block");
		config.addSoundEvent(ver, "block.fence_gate.close", "block");
		config.addSoundEvent(ver, "block.nether_wood_fence_gate.open", "block");
		config.addSoundEvent(ver, "block.nether_wood_fence_gate.close", "block");
		config.addSoundEvent(ver, "block.cherry_wood_fence_gate.open", "block");
		config.addSoundEvent(ver, "block.cherry_wood_fence_gate.close", "block");
		config.addSoundEvent(ver, "block.bamboo_wood_fence_gate.open", "block");
		config.addSoundEvent(ver, "block.bamboo_wood_fence_gate.close", "block");

		config.addSoundEvent(ver, "block.wooden_door.open", "block");
		config.addSoundEvent(ver, "block.wooden_door.close", "block");
		config.addSoundEvent(ver, "block.nether_wood_door.open", "block");
		config.addSoundEvent(ver, "block.nether_wood_door.close", "block");
		config.addSoundEvent(ver, "block.cherry_wood_door.open", "block");
		config.addSoundEvent(ver, "block.cherry_wood_door.close", "block");
		config.addSoundEvent(ver, "block.bamboo_wood_door.open", "block");
		config.addSoundEvent(ver, "block.bamboo_wood_door.close", "block");
		config.addSoundEvent(ver, "block.iron_door.open", "block");
		config.addSoundEvent(ver, "block.iron_door.close", "block");
		config.addSoundEvent(ver, "block.copper_door.open", "block");
		config.addSoundEvent(ver, "block.copper_door.close", "block");

		config.addSoundEvent(ver, "block.wooden_trapdoor.open", "block");
		config.addSoundEvent(ver, "block.wooden_trapdoor.close", "block");
		config.addSoundEvent(ver, "block.nether_wood_trapdoor.open", "block");
		config.addSoundEvent(ver, "block.nether_wood_trapdoor.close", "block");
		config.addSoundEvent(ver, "block.cherry_wood_trapdoor.open", "block");
		config.addSoundEvent(ver, "block.cherry_wood_trapdoor.close", "block");
		config.addSoundEvent(ver, "block.bamboo_wood_trapdoor.open", "block");
		config.addSoundEvent(ver, "block.bamboo_wood_trapdoor.close", "block");
		config.addSoundEvent(ver, "block.iron_trapdoor.open", "block");
		config.addSoundEvent(ver, "block.iron_trapdoor.close", "block");
		config.addSoundEvent(ver, "block.copper_trapdoor.open", "block");
		config.addSoundEvent(ver, "block.copper_trapdoor.close", "block");

		config.addSoundEvent(ver, "block.wooden_button.click_off", "block");
		config.addSoundEvent(ver, "block.wooden_button.click_on", "block");
		config.addSoundEvent(ver, "block.nether_wood_button.click_off", "block");
		config.addSoundEvent(ver, "block.nether_wood_button.click_on", "block");
		config.addSoundEvent(ver, "block.cherry_wood_button.click_off", "block");
		config.addSoundEvent(ver, "block.cherry_wood_button.click_on", "block");
		config.addSoundEvent(ver, "block.bamboo_wood_button.click_off", "block");
		config.addSoundEvent(ver, "block.bamboo_wood_button.click_on", "block");

		config.addSoundEvent(ver, "block.wooden_pressure_plate.click_off", "block");
		config.addSoundEvent(ver, "block.wooden_pressure_plate.click_on", "block");
		config.addSoundEvent(ver, "block.nether_wood_pressure_plate.click_off", "block");
		config.addSoundEvent(ver, "block.nether_wood_pressure_plate.click_on", "block");
		config.addSoundEvent(ver, "block.cherry_wood_pressure_plate.click_off", "block");
		config.addSoundEvent(ver, "block.cherry_wood_pressure_plate.click_on", "block");
		config.addSoundEvent(ver, "block.metal_pressure_plate.click_off", "block");
		config.addSoundEvent(ver, "block.metal_pressure_plate.click_on", "block");

		//Automatically register block sounds for AssetDirector, but only if they contain the MC version (which means it needs to be registered here)
		//Then we remove the mc version prefix and register that sound.

		for (ModSounds.CustomSound sound : ModSounds.getSounds()) {
			if (sound.getStepResourcePath().startsWith(Tags.MC_ASSET_VER)) { //Step sound
				config.addSoundEvent(ver, sound.getStepResourcePath().substring(Tags.MC_ASSET_VER.length() + 1), "neutral");
			}
			if (sound.func_150496_b/*getPlaceSound*/().startsWith(Tags.MC_ASSET_VER)) { //Place sound
				config.addSoundEvent(ver, sound.func_150496_b/*getPlaceSound*/().substring(Tags.MC_ASSET_VER.length() + 1), "block");
			}
			if (sound.getBreakSound().startsWith(Tags.MC_ASSET_VER)) { //Break sound
				config.addSoundEvent(ver, sound.getBreakSound().substring(Tags.MC_ASSET_VER.length() + 1), "block");
			}
		}
	}
}
