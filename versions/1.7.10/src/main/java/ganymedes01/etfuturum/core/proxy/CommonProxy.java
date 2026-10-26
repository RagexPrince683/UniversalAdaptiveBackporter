package ganymedes01.etfuturum.core.proxy;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.network.IGuiHandler;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cpw.mods.fml.common.registry.EntityRegistry;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import ganymedes01.etfuturum.EtFuturum;
import ganymedes01.etfuturum.ModBlocks;
import ganymedes01.etfuturum.ModItems;
import ganymedes01.etfuturum.client.gui.inventory.*;
import ganymedes01.etfuturum.compat.ModsList;
import ganymedes01.etfuturum.configuration.configs.*;
import ganymedes01.etfuturum.core.handlers.EntityEventHandler;
import ganymedes01.etfuturum.core.handlers.ServerEventHandler;
import ganymedes01.etfuturum.core.handlers.SpectatorEventHandler;
import ganymedes01.etfuturum.core.handlers.WorldEventHandler;
import ganymedes01.etfuturum.core.utils.Logger;
import ganymedes01.etfuturum.core.utils.Utils;
import ganymedes01.etfuturum.entities.*;
import ganymedes01.etfuturum.inventory.*;
import ganymedes01.etfuturum.lib.GUIIDs;
import ganymedes01.etfuturum.network.ServerPacketExecutor;
import ganymedes01.etfuturum.tileentities.*;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.entity.monster.EntityMagmaCube;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.BiomeDictionary.Type;
import net.minecraftforge.common.MinecraftForge;
import org.apache.commons.lang3.ArrayUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CommonProxy implements IGuiHandler {

	public void handleClientPacket(IMessage packet, MessageContext context) {
	}

	public void registerEvents() {
		MinecraftForge.EVENT_BUS.register(ganymedes01.etfuturum.core.handlers.MobContentEvents.INSTANCE);
		FMLCommonHandler.instance().bus().register(ganymedes01.etfuturum.core.handlers.MobContentEvents.INSTANCE);
		FMLCommonHandler.instance().bus().register(ganymedes01.etfuturum.world.end.EndGatewayProgress.EVENTS);
		MinecraftForge.EVENT_BUS.register(ganymedes01.etfuturum.core.handlers.NetherContentEvents.INSTANCE);
		FMLCommonHandler.instance().bus().register(ServerPacketExecutor.INSTANCE);
		MinecraftForge.EVENT_BUS.register(EntityEventHandler.INSTANCE);
		if (ConfigMixins.enableSpectatorMode) {
			MinecraftForge.EVENT_BUS.register(SpectatorEventHandler.INSTANCE);
		}

		FMLCommonHandler.instance().bus().register(ServerEventHandler.INSTANCE);
		MinecraftForge.EVENT_BUS.register(ServerEventHandler.INSTANCE);

		FMLCommonHandler.instance().bus().register(WorldEventHandler.INSTANCE);
		MinecraftForge.TERRAIN_GEN_BUS.register(WorldEventHandler.INSTANCE);

		// Catalyst spreading is incomplete. Keep block/save identities without intercepting XP.

		if (ModItems.DEBUGGING_TOOL.isEnabled()) {
			MinecraftForge.EVENT_BUS.register(ModItems.DEBUGGING_TOOL.get());
		}
	}

	public void registerEntities() {
		net.minecraftforge.common.MinecraftForge.EVENT_BUS.register(new ganymedes01.etfuturum.core.handlers.ExplorationEvents());
		GameRegistry.registerTileEntity(ganymedes01.etfuturum.tileentities.TileEntityDecoratedPot.class, "etfuturum.decorated_pot");
		GameRegistry.registerTileEntity(ganymedes01.etfuturum.tileentities.TileEntityBrushable.class, "etfuturum.brushable");
		GameRegistry.registerTileEntity(ganymedes01.etfuturum.tileentities.TileEntityConduit.class, Utils.getUnlocalisedName("conduit"));
		{
			ganymedes01.etfuturum.world.structure.MonumentSpawning.init();
			ModEntityList.registerEntity(EntityGuardian.class, "guardian", 34, EtFuturum.instance, 80, 3, true, 0x5A8272, 0xF17D30);
		}
		{
			ModEntityList.registerEntity(EntityFrog.class, "frog", 32, EtFuturum.instance, 80, 3, true, 0xD07444, 0xFFC77C);
			ModEntityList.registerEntity(EntityTadpole.class, "tadpole", 33, EtFuturum.instance, 64, 3, true, 0x6D533D, 0x160A00);
			ganymedes01.etfuturum.configuration.SpawnSettings.addSpawn(EntityFrog.class, 10, 2, 5, EnumCreatureType.creature, BiomeDictionary.getBiomesForType(Type.SWAMP));
		}
		{
			GameRegistry.registerTileEntity(TileEntityCreakingHeart.class, Utils.getUnlocalisedName("creaking_heart"));
			ModEntityList.registerEntity(EntityCreaking.class, "creaking", 31, EtFuturum.instance, 80, 3, true, 0x5F5F4C, 0xFC7812);
		}
		{
			ganymedes01.etfuturum.world.nether.NetherSpawning.init();
			ModEntityList.registerEntity(EntityPiglin.class, "piglin", 26, EtFuturum.instance, 80, 3, true, 0x995F40, 0xF9F3A4);
			ModEntityList.registerEntity(EntityPiglinBrute.class, "piglin_brute", 27, EtFuturum.instance, 80, 3, true, 0x592A10, 0xF9F3A4);
			ModEntityList.registerEntity(EntityHoglin.class, "hoglin", 28, EtFuturum.instance, 80, 3, true, 0xC66E55, 0x5F6464);
			ModEntityList.registerEntity(EntityZoglin.class, "zoglin", 29, EtFuturum.instance, 80, 3, true, 0xC66E55, 0xE6E6E6);
			ModEntityList.registerEntity(EntityStrider.class, "strider", 30, EtFuturum.instance, 80, 3, true, 0x9C3436, 0x4D494D);
			ganymedes01.etfuturum.configuration.SpawnSettings.addSpawn(EntityPiglin.class, 15, 2, 4, EnumCreatureType.monster, BiomeGenBase.hell);
		}

			GameRegistry.registerTileEntity(TileEntityLightningRod.class, Utils.getUnlocalisedName("lightning_rod"));
		{
			GameRegistry.registerTileEntity(TileEntityNewBrewingStand.class, Utils.getUnlocalisedName("brewing_stand"));
		}
		{
			GameRegistry.registerTileEntity(TileEntityNewBeacon.class, Utils.getUnlocalisedName("beacon"));
		}
		{
			GameRegistry.registerTileEntity(TileEntityBarrel.class, Utils.getUnlocalisedName("barrel"));
			if(ModsList.IRON_CHEST.isLoaded()) {
				GameRegistry.registerTileEntity(TileEntityBarrel.ClearTE.class, Utils.getUnlocalisedName("clear_barrel"));
			}
		}
		{
			GameRegistry.registerTileEntity(TileEntitySmoker.class, Utils.getUnlocalisedName("smoker"));
		}
		{
			GameRegistry.registerTileEntity(TileEntityBlastFurnace.class, Utils.getUnlocalisedName("blast_furnace"));
		}
		{
			GameRegistry.registerTileEntity(TileEntityWoodSign.class, Utils.getUnlocalisedName("sign"));
		}
		{
			GameRegistry.registerTileEntity(TileEntitySculkCatalyst.class, Utils.getUnlocalisedName("sculk_catalyst"));
		}
		{
			GameRegistry.registerTileEntity(TileEntityGateway.class, Utils.getUnlocalisedName("end_gateway"));
		}
		{
			GameRegistry.registerTileEntity(TileEntityShulkerBox.class, Utils.getUnlocalisedName("shulker_box"));
		}
		{
			GameRegistry.registerTileEntity(TileEntityCauldronPotion.class, Utils.getUnlocalisedName("potion_cauldron"));
		}
		{
			GameRegistry.registerTileEntity(TileEntityBanner.class, Utils.getUnlocalisedName("banner"));
		}
		{
			GameRegistry.registerTileEntity(TileEntityBeeHive.class, Utils.getUnlocalisedName("hive"));
		}

		{
			GameRegistry.registerTileEntity(TileEntityGlowLichen.class, Utils.getUnlocalisedName("glow_lichen"));
		}
		{
			GameRegistry.registerTileEntity(TileEntityCaveVines.class, Utils.getUnlocalisedName("cave_vines"));
		}
		{
			ModEntityList.registerEntity(EntityRabbit.class, "rabbit", 3, EtFuturum.instance, 80, 3, true, 0x995F40, 0x734831);

			ganymedes01.etfuturum.configuration.SpawnSettings.addSpawn(EntityRabbit.class, 3, 1, 3, EnumCreatureType.creature, BiomeDictionary.getBiomesForType(Type.SANDY));
			BiomeGenBase[] array = BiomeDictionary.getBiomesForType(Type.SNOWY);
			array = ArrayUtils.addAll(array, BiomeDictionary.getBiomesForType(Type.PLAINS));
			array = ArrayUtils.addAll(array, BiomeDictionary.getBiomesForType(Type.FOREST));
			for (BiomeGenBase biome : ArrayUtils.clone(array)) {
				if (biome.getClass().getName().toLowerCase().contains("divinerpg")) {
					array = ArrayUtils.removeElement(array, biome);
				}
			}
			ganymedes01.etfuturum.configuration.SpawnSettings.addSpawn(EntityRabbit.class, 6, 1, 3, EnumCreatureType.creature, array);
		}

		{
			ModEntityList.registerEntity(EntityArmourStand.class, "wooden_armorstand", 0, EtFuturum.instance, 64, 1, true);
		}

		{
			ModEntityList.registerEntity(EntityEndermite.class, "endermite", 1, EtFuturum.instance, 64, 1, true, 0x161616, 0x6E6E6E);
		}

		{
			ModEntityList.registerEntity(EntityTippedArrow.class, "tipped_arrow", 2, EtFuturum.instance, 64, 20, true);
		}

		{
			ModEntityList.registerEntity(EntitySpectralArrow.class, "spectral_arrow", 25, EtFuturum.instance, 64, 20, true);
		}

		if (FMLCommonHandler.instance().getSide() == Side.CLIENT && FMLClientHandler.instance().hasOptifine()) {
			if (!ConfigWorld.oldHuskSpawning) {
				Logger.warn("OptiFine detected, old husk spawn logic will be enabled since OptiFine is stupid and breaks the default behavior.");
				ConfigWorld.oldHuskSpawning = true;
			}
			if (!ConfigWorld.oldStraySpawning) {
				Logger.warn("OptiFine detected, old stray spawn logic will be enabled since OptiFine is stupid and breaks the default behavior.");
				ConfigWorld.oldStraySpawning = true;
			}
		}

		{
			ModEntityList.registerEntity(EntityHusk.class, "husk", 4, EtFuturum.instance, 80, 3, true, 0x777561, 0xE0D991);

			if (ConfigEntities.enableHusk && ConfigWorld.oldHuskSpawning) {
				List<BiomeGenBase> biomes = new ArrayList<>(Arrays.asList(BiomeDictionary.getBiomesForType(Type.SANDY)));
				biomes.retainAll(Arrays.asList(BiomeDictionary.getBiomesForType(Type.DRY)));
				biomes.retainAll(Arrays.asList(BiomeDictionary.getBiomesForType(Type.HOT)));

				BiomeGenBase[] biomeArray = (BiomeGenBase[]) ArrayUtils.addAll(new BiomeGenBase[]{}, biomes.toArray());
				//change spawn weights
				EntityRegistry.removeSpawn(EntityZombie.class, EnumCreatureType.monster, biomeArray);

				ganymedes01.etfuturum.configuration.SpawnSettings.addSpawn(EntityZombie.class, 20, 4, 4, EnumCreatureType.monster, biomeArray);
				ganymedes01.etfuturum.configuration.SpawnSettings.addSpawn(EntityHusk.class, 80, 4, 4, EnumCreatureType.monster, biomeArray);
			}
		}

		{
			ModEntityList.registerEntity(EntityStray.class, "stray", 5, EtFuturum.instance, 80, 3, true, 0x617778, 0xE6EAEA);

			if (ConfigEntities.enableStray && ConfigWorld.oldStraySpawning) {
				//change spawn weights
				EntityRegistry.removeSpawn(EntitySkeleton.class, EnumCreatureType.monster, BiomeDictionary.getBiomesForType(Type.SNOWY));

				ganymedes01.etfuturum.configuration.SpawnSettings.addSpawn(EntitySkeleton.class, 20, 4, 4, EnumCreatureType.monster, BiomeDictionary.getBiomesForType(Type.SNOWY));
				ganymedes01.etfuturum.configuration.SpawnSettings.addSpawn(EntityStray.class, 80, 4, 4, EnumCreatureType.monster, BiomeDictionary.getBiomesForType(Type.SNOWY));
			}
		}

		if (ConfigEntities.enableNetherEndermen) {
			ganymedes01.etfuturum.configuration.SpawnSettings.addSpawn(EntityEnderman.class, 1, 4, 4, EnumCreatureType.monster, BiomeGenBase.hell);
//			 {
//				EntityEnderman.setCarriable(ModBlocks.NYLIUM.get(), true);
//			}
		}

		{
			ModEntityList.registerEntity(EntityLingeringPotion.class, "lingering_potion", 6, EtFuturum.instance, 64, 10, true);
			ModEntityList.registerEntity(EntityLingeringEffect.class, "lingering_effect", 7, EtFuturum.instance, 64, 1, true);
		}


			ModEntityList.registerEntity(EntityZombieVillager.class, "villager_zombie", 8, EtFuturum.instance, 80, 3, true, 0x00AFAF, 0x799C65);

		{
			ModEntityList.registerEntity(EntityPlacedEndCrystal.class, "end_crystal", 9, EtFuturum.instance, 256, Integer.MAX_VALUE, false);
			ModEntityList.registerEntity(EntityRespawnedDragon.class, "ender_dragon", 10, EtFuturum.instance, 160, 3, true);
		}


			ModEntityList.registerEntity(EntityNewSnowGolem.class, "snow_golem", 11, EtFuturum.instance, 80, 3, true);


			ModEntityList.registerEntity(EntityBrownMooshroom.class, "brown_mooshroom", 12, EtFuturum.instance, 80, 3, true);

		ModEntityList.registerEntity(EntityItemUninflammable.class, "fireproof_item", 15, EtFuturum.instance, 64, 1, true);

		{
			ModEntityList.registerEntity(EntityShulker.class, "shulker", 16, EtFuturum.instance, 80, 1, false, 0x946794, 0x4D3852);
			ModEntityList.registerEntity(EntityShulkerBullet.class, "shulker_candy", 17, EtFuturum.instance, 64, 1, true);

			if (ConfigTweaks.shulkersSpawnAnywhere) {
				ganymedes01.etfuturum.configuration.SpawnSettings.addSpawn(EntityShulker.class, 1, 1, 2, EnumCreatureType.monster, BiomeDictionary.getBiomesForType(Type.END));
			}
		}

//      {
//          ModEntityList.registerEntity(EntityFallingDripstone.class, "falling_dripstone", 18, EtFuturum.instance, 64, 1, true);
//      }

		{
			ModEntityList.registerEntity(EntityNewBoat.class, "new_boat", 13, EtFuturum.instance, 64, 1, true);
			ModEntityList.registerEntity(EntityNewBoatWithChest.class, "chest_boat", 19, EtFuturum.instance, 64, 1, true);
			ModEntityList.registerEntity(EntityNewBoatSeat.class, "new_boat_seat", 14, EtFuturum.instance, 64, 1, false);
		}

		{
			ModEntityList.registerEntity(EntityBoostingFireworkRocket.class, "boosting_firework_rocket", 20, EtFuturum.instance, 64, 1, true);
		}

		{
			ModEntityList.registerEntity(EntityBee.class, "bee", 21, EtFuturum.instance, 80, 1, true, 0xEDC343, 0x43241B);
		}

		{
			ModEntityList.registerEntity(EntityFox.class, "fox", 22, EtFuturum.instance, 64, 1, true, 0xD5B69F, 0xCC6920);
			ganymedes01.etfuturum.configuration.SpawnSettings.addSpawn(EntityFox.class, 8, 2, 4, EnumCreatureType.creature, BiomeDictionary.getBiomesForType(Type.CONIFEROUS));
		}

		{
			ModEntityList.registerEntity(EntityPolarBear.class, "polar_bear", 23, EtFuturum.instance, 80, 3, true, 0xF2F2F2, 0x959590);
			ganymedes01.etfuturum.configuration.SpawnSettings.addSpawn(EntityPolarBear.class, 1, 1, 2, EnumCreatureType.creature, BiomeDictionary.getBiomesForType(Type.SNOWY));
		}

		{
			ModEntityList.registerEntity(EntityGoat.class, "goat", 24, EtFuturum.instance, 80, 3, true, 0xA5947C, 0x55493E);
			List<BiomeGenBase> goatBiomes = new ArrayList<>(Arrays.asList(BiomeDictionary.getBiomesForType(Type.MOUNTAIN)));
			goatBiomes.retainAll(Arrays.asList(BiomeDictionary.getBiomesForType(Type.SNOWY)));
			if (!goatBiomes.isEmpty()) {
				ganymedes01.etfuturum.configuration.SpawnSettings.addSpawn(EntityGoat.class, 5, 1, 3, EnumCreatureType.creature, goatBiomes.toArray(new BiomeGenBase[goatBiomes.size()]));
			}
		}

		//make magmas slightly more common, hopefully.
		EntityRegistry.removeSpawn(EntityMagmaCube.class, EnumCreatureType.monster, BiomeGenBase.hell);
		ganymedes01.etfuturum.configuration.SpawnSettings.addSpawn(EntityMagmaCube.class, 2, 4, 4, EnumCreatureType.monster, BiomeGenBase.hell);
		ganymedes01.etfuturum.world.surface.SurfaceBiomeManager.finishSpawns();
		ganymedes01.etfuturum.world.MobSpawning.init();
		ganymedes01.etfuturum.world.ModernMobs.init();
	}

	@Override
	public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
		if (ID == GUIIDs.LLAMA) return world.getEntityByID(x) instanceof EntityLlama llama && llama.isUseableByPlayer(player) ? new ContainerLlama(player.inventory, llama) : null;
		if (ID >= GUIIDs.STONECUTTER && ID <= GUIIDs.CARTOGRAPHY_TABLE) {
			ModBlocks table = ID == GUIIDs.STONECUTTER ? ModBlocks.STONECUTTER : ID == GUIIDs.LOOM ? ModBlocks.LOOM : ModBlocks.CARTOGRAPHY_TABLE;
			if (!table.isEnabled() || !world.blockExists(x, y, z) || world.getBlock(x, y, z) != table.get()
					|| player.getDistanceSq(x + .5, y + .5, z + .5) > 64) return null;
		}
		return switch (ID) {
			case GUIIDs.STONECUTTER -> new ContainerStonecutter(player.inventory, world, x, y, z);
			case GUIIDs.LOOM -> new ContainerLoom(player.inventory, world, x, y, z);
			case GUIIDs.CARTOGRAPHY_TABLE -> new ContainerCartography(player.inventory, world, x, y, z);
			case GUIIDs.ENCHANTING_TABLE -> new ContainerEnchantment(player.inventory, world, x, y, z);
			case GUIIDs.ANVIL -> new ContainerAnvil(player, world, x, y, z);
			case GUIIDs.BREWING_STAND ->
					new ContainerNewBrewingStand(player.inventory, (TileEntityNewBrewingStand) world.getTileEntity(x, y, z));
			case GUIIDs.BARREL ->
					new ContainerChestGeneric(player.inventory, (TileEntityBarrel) world.getTileEntity(x, y, z), ((TileEntityBarrel) world.getTileEntity(x, y, z)).getRowSize(), ((TileEntityBarrel) world.getTileEntity(x, y, z)).getSizeInventory() != 27);
			case GUIIDs.SMOKER ->
					new ContainerSmoker(player.inventory, (TileEntitySmoker) world.getTileEntity(x, y, z));
			case GUIIDs.BLAST_FURNACE ->
					new ContainerBlastFurnace(player.inventory, (TileEntityBlastFurnace) world.getTileEntity(x, y, z));
			case GUIIDs.SHULKER_BOX ->
					new ContainerChestGeneric(player.inventory, (TileEntityShulkerBox) world.getTileEntity(x, y, z), ((TileEntityShulkerBox) world.getTileEntity(x, y, z)).getRowSize(), ((TileEntityShulkerBox) world.getTileEntity(x, y, z)).getSizeInventory() != 27);
			case GUIIDs.SMITHING_TABLE -> world.blockExists(x, y, z)
					&& world.getBlock(x, y, z) == ModBlocks.SMITHING_TABLE.get()
					&& player.getDistanceSq(x + 0.5, y + 0.5, z + 0.5) <= 64
					? new ContainerSmithingTable(player.inventory, world, x, y, z) : null;
			default -> null;
		};
	}

	@Override
	public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
		if (ID == GUIIDs.LLAMA) return world.getEntityByID(x) instanceof EntityLlama llama && llama.hasChest() ? new GuiLlama(new ContainerLlama(player.inventory, llama)) : null;
		return switch (ID) {
			case GUIIDs.STONECUTTER -> new GuiWorkstation(new ContainerStonecutter(player.inventory, world, x, y, z));
			case GUIIDs.LOOM -> new GuiWorkstation(new ContainerLoom(player.inventory, world, x, y, z));
			case GUIIDs.CARTOGRAPHY_TABLE -> new GuiWorkstation(new ContainerCartography(player.inventory, world, x, y, z));
			case GUIIDs.ENCHANTING_TABLE -> new GuiEnchantment(player.inventory, world, null);
			case GUIIDs.ANVIL -> new GuiAnvil(player, world, x, y, z);
			case GUIIDs.BREWING_STAND ->
					new GuiNewBrewingStand(player.inventory, (TileEntityNewBrewingStand) world.getTileEntity(x, y, z));
			case GUIIDs.BARREL -> new GuiBarrel(player.inventory, (TileEntityBarrel) world.getTileEntity(x, y, z));
			case GUIIDs.SMOKER -> new GuiSmoker(player.inventory, (TileEntitySmoker) world.getTileEntity(x, y, z));
			case GUIIDs.BLAST_FURNACE ->
					new GuiBlastFurnace(player.inventory, (TileEntityBlastFurnace) world.getTileEntity(x, y, z));
			case GUIIDs.SHULKER_BOX ->
					new GuiShulkerBox(player.inventory, (TileEntityShulkerBox) world.getTileEntity(x, y, z));
			case GUIIDs.SMITHING_TABLE -> new GuiSmithingTable(new ContainerSmithingTable(player.inventory, world, x, y, z));
			default -> null;
		};
	}

	public void registerRenderers() {
	}
}
