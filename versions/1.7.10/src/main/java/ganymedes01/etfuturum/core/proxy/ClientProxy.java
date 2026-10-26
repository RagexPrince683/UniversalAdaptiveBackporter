package ganymedes01.etfuturum.core.proxy;

import com.mojang.authlib.minecraft.MinecraftSessionService;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import ganymedes01.etfuturum.client.network.ClientPacketHandler;
import ganymedes01.etfuturum.ModBlocks;
import ganymedes01.etfuturum.api.event.GlowLichenHighlightEvent;
import ganymedes01.etfuturum.ModItems;
import ganymedes01.etfuturum.client.model.ModelShulker;
import ganymedes01.etfuturum.client.renderer.GlowingEffectRenderer;
import ganymedes01.etfuturum.client.renderer.block.*;
import ganymedes01.etfuturum.client.renderer.entity.*;
import ganymedes01.etfuturum.client.renderer.item.*;
import ganymedes01.etfuturum.client.renderer.tileentity.*;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import ganymedes01.etfuturum.client.skins.NewRenderPlayer;
import ganymedes01.etfuturum.client.skins.NewSkinManager;
import ganymedes01.etfuturum.client.subtitle.GuiSubtitles;
import ganymedes01.etfuturum.compat.CompatIronChests;
import ganymedes01.etfuturum.compat.ModsList;
import ganymedes01.etfuturum.configuration.configs.ConfigFunctions;
import ganymedes01.etfuturum.configuration.configs.ConfigMixins;
import ganymedes01.etfuturum.configuration.configs.ConfigSounds;
import ganymedes01.etfuturum.core.handlers.client.ClientEventHandler;
import ganymedes01.etfuturum.core.handlers.client.ArmorSoundEventHandler;
import ganymedes01.etfuturum.core.handlers.client.SpectatorEventHandlerClient;
import ganymedes01.etfuturum.entities.*;
import ganymedes01.etfuturum.lib.RenderIDs;
import ganymedes01.etfuturum.tileentities.*;
import ganymedes01.etfuturum.world.nether.biome.utils.BiomeFogEventHandler;
import net.minecraft.block.BlockBed;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.entity.passive.EntityPig;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntitySkull;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.common.MinecraftForge;

import java.io.File;

public class ClientProxy extends CommonProxy {

	@Override
	public void handleClientPacket(IMessage packet, MessageContext context) {
		ClientPacketHandler.handle(packet, context);
	}

	public static boolean isRenderingInventoryPlayer = false;

	@Override
	public void registerEvents() {
		super.registerEvents();
		FMLCommonHandler.instance().bus().register(ClientEventHandler.INSTANCE);
		MinecraftForge.EVENT_BUS.register(ClientEventHandler.INSTANCE);
		if (ConfigMixins.enableSpectatorMode) {
			MinecraftForge.EVENT_BUS.register(SpectatorEventHandlerClient.INSTANCE);
		}
		if (ConfigSounds.armorEquip) {
			MinecraftForge.EVENT_BUS.register(ArmorSoundEventHandler.INSTANCE);
		}

		if (ConfigFunctions.enableSubtitles) {
			GuiSubtitles.INSTANCE = new GuiSubtitles(FMLClientHandler.instance().getClient());
			MinecraftForge.EVENT_BUS.register(GuiSubtitles.INSTANCE);
		}
		
		MinecraftForge.EVENT_BUS.register(new GlowLichenHighlightEvent());
		MinecraftForge.EVENT_BUS.register(BiomeFogEventHandler.INSTANCE);
		MinecraftForge.EVENT_BUS.register(new GlowingEffectRenderer());
	}

	@Override
	public void registerRenderers() {
		registerItemRenderers();
		registerBlockRenderers();
		registerEntityRenderers();
	}

	private void registerItemRenderers() {
		MinecraftForge.EVENT_BUS.register(new ganymedes01.etfuturum.client.renderer.ArmorTrimRenderer());
		MinecraftForgeClient.registerItemRenderer(ModBlocks.DECORATED_POT.getItem(), new ganymedes01.etfuturum.client.renderer.item.ItemDecoratedPotRenderer());
		MinecraftForgeClient.registerItemRenderer(Item.getItemFromBlock(ModBlocks.BANNER.get()), new ItemBannerRenderer());
		if (ConfigFunctions.enableFancySkulls) {
			MinecraftForgeClient.registerItemRenderer(Items.skull, new ItemSkullRenderer());
		}
		if (ConfigFunctions.enableBowRendering) {
			MinecraftForgeClient.registerItemRenderer(Items.bow, new ItemBowRenderer());
		}
		if (ModItems.GOAT_HORN.isEnabled()) {
			MinecraftForgeClient.registerItemRenderer(ModItems.GOAT_HORN.get(), new ItemGoatHornRenderer());
		}
		MinecraftForgeClient.registerItemRenderer(Item.getItemFromBlock(ModBlocks.SHULKER_BOX.get()), new ItemShulkerBoxRenderer());
		if (ConfigFunctions.inventoryBedModels) {
			MinecraftForgeClient.registerItemRenderer(Items.bed, new Item3DBedRenderer((BlockBed) Blocks.bed));
			for (ModBlocks bed : ModBlocks.BEDS) {
				if (bed.isEnabled()) {
					MinecraftForgeClient.registerItemRenderer(bed.getItem(), new Item3DBedRenderer((BlockBed) bed.get()));
				}
			}
		}

		if(ModsList.APPLIED_ENERGISTICS_2.isLoaded()) {
			MinecraftForgeClient.registerItemRenderer(ModBlocks.DEEPSLATE_CERTUS_QUARTZ_ORE.getItem(), new BlockDeepslateCertusQuartzRenderer());
		}
	}

	private void registerBlockRenderers() {
		ClientRegistry.bindTileEntitySpecialRenderer(ganymedes01.etfuturum.tileentities.TileEntityDecoratedPot.class, new ganymedes01.etfuturum.client.renderer.tileentity.TileEntityDecoratedPotRenderer());
		ClientRegistry.bindTileEntitySpecialRenderer(ganymedes01.etfuturum.tileentities.TileEntityBrushable.class, new ganymedes01.etfuturum.client.renderer.tileentity.TileEntityBrushableRenderer());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityWoodSign.class, new TileEntityWoodSignRenderer());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityBanner.class, new TileEntityBannerRenderer());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntitySkull.class, new TileEntityFancySkullRenderer());
		// Replace the vanilla skull renderer singleton with our dragon-head-aware subclass.
		// This intercepts worn-on-player skull rendering (called from RenderBiped.renderEquippedItems)
		// and renders the dragon head model for skull type 5, delegating to vanilla for all other types.
		// new DragonHeadSkullRenderer().func_147497_a(TileEntityRendererDispatcher.instance);
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityNewBeacon.class, new TileEntityNewBeaconRenderer());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityShulkerBox.class, new TileEntityShulkerBoxRenderer(new ModelShulker()));
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityGateway.class, new TileEntityGatewayRenderer());
		if(ModsList.IRON_CHEST.isLoaded() && CompatIronChests.enableCrystalRendering()) {
			ClientRegistry.bindTileEntitySpecialRenderer(TileEntityBarrel.ClearTE.class, new TileEntityClearChestItemRenderer(key -> {
				if (key instanceof TileEntityBarrel.ClearTE barrel) {
					return barrel.getTopItemStacks();
				}
				return null;
			}));
		}

		RenderingRegistry.registerBlockHandler(new BlockExtendedCrossedSquaresRenderer());
		RenderingRegistry.registerBlockHandler(new ReleasedBlockRenderer());
		RenderingRegistry.registerBlockHandler(new BlockNewFenceRenderer());
		RenderingRegistry.registerBlockHandler(new BlockTrapDoorRenderer(RenderIDs.TRAPDOOR));
		RenderingRegistry.registerBlockHandler(new BlockDoorRenderer(RenderIDs.DOOR));
		RenderingRegistry.registerBlockHandler(new BlockDoubleCubeRenderer(13, RenderIDs.SLIME_BLOCK));
		RenderingRegistry.registerBlockHandler(new BlockEndRodRenderer(RenderIDs.END_ROD));
		RenderingRegistry.registerBlockHandler(new BlockChorusFlowerRenderer(RenderIDs.CHORUS_FLOWER));
		RenderingRegistry.registerBlockHandler(new BlockChorusPlantRenderer(RenderIDs.CHORUS_PLANT));
		RenderingRegistry.registerBlockHandler(new BlockLanternRenderer(RenderIDs.LANTERN));
		RenderingRegistry.registerBlockHandler(new BlockBarrelRenderer(RenderIDs.BARREL));
		RenderingRegistry.registerBlockHandler(new BlockGlazedTerracottaRenderer(RenderIDs.GLAZED_TERRACOTTA));
		RenderingRegistry.registerBlockHandler(new BlockLavaCauldronRenderer(RenderIDs.LAVA_CAULDRON));
		RenderingRegistry.registerBlockHandler(new BlockStonecutterRenderer(RenderIDs.STONECUTTER));
		RenderingRegistry.registerBlockHandler(new BlockComposterRenderer(RenderIDs.COMPOSTER));
		RenderingRegistry.registerBlockHandler(new BlockLoomRenderer(RenderIDs.LOOM));
		RenderingRegistry.registerBlockHandler(new BlockAmethystClusterRenderer(RenderIDs.AMETHYST_CLUSTER));
		RenderingRegistry.registerBlockHandler(new BlockPointedDripstoneRenderer(RenderIDs.POINTED_DRIPSTONE));
		RenderingRegistry.registerBlockHandler(new BlockColoredWaterCauldronRenderer(RenderIDs.COLORED_CAULDRON));
		RenderingRegistry.registerBlockHandler(new BlockChestRenderer());
		RenderingRegistry.registerBlockHandler(new BlockObserverRenderer(RenderIDs.OBSERVER));
		RenderingRegistry.registerBlockHandler(new BlockDoubleCubeRenderer(15, RenderIDs.HONEY_BLOCK));
		RenderingRegistry.registerBlockHandler(new BlockChainRenderer(RenderIDs.CHAIN));
		RenderingRegistry.registerBlockHandler(new BlockAzaleaRenderer(RenderIDs.AZALEA));
		RenderingRegistry.registerBlockHandler(new BlockLightningRodRenderer());
		RenderingRegistry.registerBlockHandler(new BlockMangroveRootsRenderer(RenderIDs.MANGROVE_ROOTS));
		RenderingRegistry.registerBlockHandler(new BlockPinkPetalsRenderer(RenderIDs.PINK_PETALS));
		RenderingRegistry.registerBlockHandler(new BlockBambooRenderer(RenderIDs.BAMBOO));
		RenderingRegistry.registerBlockHandler(new BlockBubbleColumnRenderer(RenderIDs.BUBBLE_COLUMN));
		RenderingRegistry.registerBlockHandler(new BlockGlowLichenRenderer(RenderIDs.GLOW_LICHEN));
		if(ModsList.APPLIED_ENERGISTICS_2.isLoaded()) {
			RenderingRegistry.registerBlockHandler(new BlockDeepslateCertusQuartzRenderer());
		}

		RenderingRegistry.registerBlockHandler(new BlockEmissiveLayerRenderer(RenderIDs.EMISSIVE_DOUBLE_LAYER));
	}

	private void registerEntityRenderers() {
		MinecraftForgeClient.registerItemRenderer(ModItems.CROSSBOW.get(), new ItemCrossbowRenderer());
		ModernMobRenderer.register();
		ganymedes01.etfuturum.client.renderer.tileentity.CopperMobBlockRenderer.register();
		MinecraftForge.EVENT_BUS.register(new ganymedes01.etfuturum.client.renderer.entity.MobVisualEffects());
		RenderingRegistry.registerEntityRenderingHandler(EntityCod.class, new AquaticMobRenderer(0));
		RenderingRegistry.registerEntityRenderingHandler(EntitySalmon.class, new AquaticMobRenderer(1));
		RenderingRegistry.registerEntityRenderingHandler(EntityTropicalFish.class, new AquaticMobRenderer(2));
		RenderingRegistry.registerEntityRenderingHandler(EntityPufferfish.class, new AquaticMobRenderer(10));
		RenderingRegistry.registerEntityRenderingHandler(EntityDolphin.class, new AquaticMobRenderer(7));
		RenderingRegistry.registerEntityRenderingHandler(EntityTurtle.class, new AquaticMobRenderer(9));
		RenderingRegistry.registerEntityRenderingHandler(EntityAxolotl.class, new AquaticMobRenderer(8));
		RenderingRegistry.registerEntityRenderingHandler(EntityDrowned.class, new DrownedRenderer());
		RenderingRegistry.registerEntityRenderingHandler(EntityPhantom.class, new PhantomRenderer());
		RenderingRegistry.registerEntityRenderingHandler(EntityTrident.class, new TridentRenderer());
		RenderingRegistry.registerEntityRenderingHandler(EntityGlowSquid.class, new net.minecraft.client.renderer.entity.RenderSquid(new net.minecraft.client.model.ModelSquid(), .7F) {
			private final net.minecraft.util.ResourceLocation texture = new net.minecraft.util.ResourceLocation("etfuturum", "textures/entity/squid/glow_squid.png");
			@Override protected net.minecraft.util.ResourceLocation getEntityTexture(net.minecraft.entity.Entity entity) { return texture; }
		});
		RenderingRegistry.registerEntityRenderingHandler(EntityGuardian.class, new ganymedes01.etfuturum.client.renderer.entity.GuardianRenderer());
		RenderingRegistry.registerEntityRenderingHandler(EntityFrog.class, new AmphibianRenderer(false));
		RenderingRegistry.registerEntityRenderingHandler(EntityTadpole.class, new AmphibianRenderer(true));
		RenderingRegistry.registerEntityRenderingHandler(EntityCreaking.class, new NetherMobRenderer(3, "creaking"));
		RenderingRegistry.registerEntityRenderingHandler(EntityPiglin.class, new NetherMobRenderer(0, "piglin"));
		RenderingRegistry.registerEntityRenderingHandler(EntityPiglinBrute.class, new NetherMobRenderer(0, "piglin_brute"));
		RenderingRegistry.registerEntityRenderingHandler(EntityHoglin.class, new NetherMobRenderer(1, "hoglin"));
		RenderingRegistry.registerEntityRenderingHandler(EntityZoglin.class, new NetherMobRenderer(1, "zoglin"));
		RenderingRegistry.registerEntityRenderingHandler(EntityStrider.class, new NetherMobRenderer(2, "strider"));
		RenderingRegistry.registerEntityRenderingHandler(EntityArmourStand.class, new ArmourStandRenderer());
		RenderingRegistry.registerEntityRenderingHandler(EntityEndermite.class, new EndermiteRenderer());
		RenderingRegistry.registerEntityRenderingHandler(EntityRabbit.class, new RabbitRenderer());
		RenderingRegistry.registerEntityRenderingHandler(EntityHusk.class, new HuskRenderer());
		RenderingRegistry.registerEntityRenderingHandler(EntityStray.class, new StrayRenderer());
		RenderingRegistry.registerEntityRenderingHandler(EntityLingeringPotion.class, new LingeringPotionRenderer());
		RenderingRegistry.registerEntityRenderingHandler(EntityLingeringEffect.class, new LingeringEffectRenderer());
		RenderingRegistry.registerEntityRenderingHandler(EntityZombieVillager.class, new VillagerZombieRenderer());
		RenderingRegistry.registerEntityRenderingHandler(EntityPlacedEndCrystal.class, new PlacedEndCrystalRenderer());
		RenderingRegistry.registerEntityRenderingHandler(EntityBrownMooshroom.class, new BrownMooshroomRenderer());
		RenderingRegistry.registerEntityRenderingHandler(EntityNewBoat.class, new NewBoatRenderer());
		RenderingRegistry.registerEntityRenderingHandler(EntityNewBoatWithChest.class, new ChestBoatRenderer());
		RenderingRegistry.registerEntityRenderingHandler(EntityShulker.class, new ShulkerRenderer());
		RenderingRegistry.registerEntityRenderingHandler(EntityShulkerBullet.class, new ShulkerBulletRenderer());
		RenderingRegistry.registerEntityRenderingHandler(EntityBee.class, new BeeRenderer());
		RenderingRegistry.registerEntityRenderingHandler(EntityNewSnowGolem.class, new NewSnowGolemRenderer());
		RenderingRegistry.registerEntityRenderingHandler(EntityFox.class, new FoxRenderer());
		RenderingRegistry.registerEntityRenderingHandler(EntityPolarBear.class, new PolarBearRenderer());
		RenderingRegistry.registerEntityRenderingHandler(EntityGoat.class, new GoatRenderer());
		RenderingRegistry.registerEntityRenderingHandler(EntitySpectralArrow.class, new SpectralArrowRenderer());

		RenderingRegistry.registerEntityRenderingHandler(EntityPig.class, new TechnobladeCrownRenderer());

//      {
//          RenderingRegistry.registerEntityRenderingHandler(EntityFallingDripstone.class, new FallingDripstoneRenderer());
//      }

		if (ConfigFunctions.enablePlayerSkinOverlay) {
			TextureManager texManager = Minecraft.getMinecraft().renderEngine;
			File skinFolder = new File(Minecraft.getMinecraft().fileAssets, "skins");
			MinecraftSessionService sessionService = Minecraft.getMinecraft().func_152347_ac(); // getSessionService
			Minecraft.getMinecraft().field_152350_aA/*skinManager*/ = new NewSkinManager(Minecraft.getMinecraft().func_152342_ad()/*getSkinManager*/, texManager, skinFolder, sessionService);

			RenderManager.instance.entityRenderMap.put(EntityPlayer.class, new NewRenderPlayer());
		}
	}
}
