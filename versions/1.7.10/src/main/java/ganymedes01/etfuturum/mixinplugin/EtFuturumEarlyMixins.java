package ganymedes01.etfuturum.mixinplugin;

import com.gtnewhorizon.gtnhmixins.IEarlyMixinLoader;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin;
import ganymedes01.etfuturum.Tags;
import ganymedes01.etfuturum.compat.CompatMisc;
import ganymedes01.etfuturum.configuration.ConfigBase;
import ganymedes01.etfuturum.configuration.configs.*;
import ganymedes01.etfuturum.lib.Reference;
import java.util.ArrayList;
import net.minecraft.launchwrapper.Launch;
import org.spongepowered.asm.mixin.MixinEnvironment;

import java.io.File;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import java.util.Set;

@IFMLLoadingPlugin.Name("EtFuturumEarlyMixins")
@IFMLLoadingPlugin.MCVersion("1.7.10")
public class EtFuturumEarlyMixins implements IFMLLoadingPlugin, IEarlyMixinLoader {

	public static final MixinEnvironment.Side side = MixinEnvironment.getCurrentEnvironment().getSide();

	public void initConfigs() {
		ConfigBase.initializeConfigs();

		CompatMisc.doLwjgl3ifyCompat();
	}

	@Override
	public String getMixinConfig() {
		return "mixins." + Tags.MOD_ID + ".early.json";
	}

	@Override
	public List<String> getMixins(Set<String> loadedCoreMods) {

		initConfigs();

		List<String> mixins = new ArrayList<>();
		mixins.add("potionids.MixinHandshakeModList");
		mixins.add("potionids.MixinPotion");
		mixins.add("configuration.MixinWorldFeatureAcquisition");
		mixins.add("configuration.MixinEnchantmentAvailability");
        mixins.add("strongholds.MixinMapGenStronghold");
        mixins.add("mace.MixinLivingArmor");
        mixins.add("bundle.BundleInventoryMixin");
        mixins.add("powdersnow.MixinSnowPrecipitation");
        mixins.add("respawnanchor.MixinAnchorRespawn");
        mixins.add("respawnanchor.MixinAnchorSpawnPoint");
        mixins.add("shield.MixinShieldBlocking");
        mixins.add("shield.ShieldUseAccess");
        mixins.add("sculk.MixinCatalystExperience");
        mixins.add("copper.MixinHorseArmor");
        mixins.add("spring.MixinAnimalData");
        mixins.add("spring.MixinCowBreeding");
mixins.add("spring.MixinPigBreeding");
mixins.add("spring.MixinChickenBreeding");
mixins.add("spring.MixinWolfBreeding");
        mixins.add("spring.MixinSheep");
        mixins.add("spring.MixinCactus");
        mixins.add("spring.MixinEgg");
        mixins.add("spring.MixinChickenEggs");
        mixins.add("spring.MixinWolfSounds");
        mixins.add("mace.MixinPlayerArmor");
        mixins.add("mace.MixinMaceEnchantments");
        mixins.add("mace.MixinPlayerAttack");
        mixins.add("mace.MixinMobAttack");
        mixins.add("mace.MixinDamageEnchantments");

		// Core APIs owned by Et Futurum: metadata-aware tags and nested generation tracking.
		mixins.add("tags.MixinBlock");
		mixins.add("tags.MixinItem");
		mixins.add("tags.MixinBiomeGenBase");
		mixins.add("deepslateores.MixinChunkProviderServer");
		if (ConfigExperiments.enableLightningRod) {
			mixins.add("lightningrod.MixinWorldServer");
			mixins.add("lightningrod.MixinEntityLightningBolt");
		}
		if (ConfigWorld.enableCoarseDirtReplacement && ConfigBlocksItems.enableCoarseDirt) {
			mixins.add("coarsedirt.MixinBiomeGenBase");
			mixins.add("coarsedirt.MixinBiomeGenMesa");
		}

		if (side == MixinEnvironment.Side.CLIENT) {
            mixins.add("shield.MixinShieldFirstPerson");
            mixins.add("shield.MixinShieldArmPose");
			mixins.add("spear.MixinSpearAirClick");
            mixins.add("copper.MixinHorseArmorTextures");
            mixins.add("spring.client.MixinLeaves");
            mixins.add("spring.client.MixinRenderCow");
            mixins.add("spring.client.MixinRenderPig");
            mixins.add("spring.client.MixinRenderChicken");
            mixins.add("spring.client.MixinRenderWolf");
			if (ConfigSounds.bookPageTurn) mixins.add("sounds.client.MixinGuiButton");
			if (ConfigWorld.endFlashes || ConfigWorld.modernEndAmbientColor || ConfigWorld.modernBlockLightTint) {
				mixins.add("endflashes.client.MixinEntityRenderer");
			}
			if (ConfigWorld.endFlashes) mixins.add("endflashes.client.MixinRenderGlobal");
			if (ConfigWorld.modernLightmapGamma) mixins.add("modernlightmap.client.MixinEntityRenderer");
			if (ConfigWorld.modernBlockLightTint || ConfigWorld.modernNightVision) {
				mixins.add("modernblocklight.client.MixinEntityRenderer");
			}
		}

		// These hooks add modern behavior without changing the identity of the placed vanilla block.
		if (ConfigBlocksItems.enableEnchantingTable) mixins.add("functionalblocks.MixinBlockEnchantmentTable");
		if (ConfigBlocksItems.enableBrewingStands) mixins.add("functionalblocks.MixinBlockBrewingStand");
		if (ConfigBlocksItems.enableColourfulBeacons) mixins.add("functionalblocks.MixinBlockBeacon");
		if (ConfigBlocksItems.enableAnvil) mixins.add("functionalblocks.MixinBlockAnvil");

		if (ConfigMixins.endPortalFix) {
			mixins.add("endportal.MixinBlockEndPortal");
		}

		if (ConfigMixins.fenceWallConnectFix) {
			mixins.add("fencewallconnect.MixinBlockWall");
			mixins.add("fencewallconnect.MixinBlockFence");
		}

		if (ConfigMixins.stepHeightFix) {
			mixins.add("stepfix.MixinEntity");
		}

		if (ConfigMixins.enableSpectatorMode) {
			mixins.add("spectator.MixinGameType");
			mixins.add("spectator.MixinEntity");
			mixins.add("spectator.MixinEntityLivingBase");
			mixins.add("spectator.MixinWorld");
			mixins.add("spectator.MixinWorldServer");
			mixins.add("spectator.MixinEntityPlayer");
			mixins.add("spectator.MixinNetHandlerPlayServer");
			mixins.add("spectator.MixinInventoryPlayer");
			mixins.add("spectator.MixinContainerChest");
			mixins.add("spectator.MixinSlot");
			mixins.add("spectator.MixinCommandGameMode");
			if (side == MixinEnvironment.Side.CLIENT) {
				mixins.add("spectator.client.MixinPlayerControllerMP");
				mixins.add("spectator.client.MixinEntityRenderer");
				mixins.add("spectator.client.MixinEntity");
				mixins.add("spectator.client.MixinEntityPlayer");
				mixins.add("spectator.client.MixinWorldRenderer");
			}
		}

		if (ConfigMixins.avoidDroppingItemsWhenClosing) {
			mixins.add("closedrops.MixinEntityPlayerMP");
		}
        
		if (ConfigMixins.enableElytra) {
			mixins.add("backlytra.MixinEntityPlayer");
			mixins.add("backlytra.MixinEntityLivingBase");
			mixins.add("backlytra.MixinNetHandlerPlayServer");
			mixins.add("backlytra.MixinEntityTrackerEntry");
			if (side == MixinEnvironment.Side.CLIENT) {
				mixins.add("backlytra.client.MixinAbstractClientPlayer");
				mixins.add("backlytra.client.MixinEntityPlayerSP");
				mixins.add("backlytra.client.MixinRenderPlayer");
				mixins.add("backlytra.client.MixinModelBiped");
				mixins.add("backlytra.client.MixinEntityRenderer");
			}
		}

		if (ConfigMixins.enableDoWeatherCycle) {
			mixins.add("doweathercycle.MixinCommandHandler");
			mixins.add("doweathercycle.MixinWorldInfo");
		}

		if (ConfigMixins.enableRandomTickSpeed) {
			mixins.add("randomtickspeed.MixinWorldServer");
			mixins.add("randomtickspeed.MixinGameRules");
		}

		if (ConfigMixins.creativeFlightSpeedModifier > 1 || ConfigTweaks.creativeFlightVerticalModifier > 1) {
			mixins.add("flyspeed.MixinEntityPlayer");
		}

		if (ConfigMixins.bouncyBeds) {
			mixins.add("bouncybeds.MixinBlockBed");
		}

		if (ConfigMixins.newHurtSounds) {
			mixins.add("sounds.MixinEntityPlayer");
			if (side == MixinEnvironment.Side.CLIENT) {
				mixins.add("sounds.client.MixinEntityClientPlayerMP");
			}
		}

		if (ConfigMixins.newMobSounds) {
			mixins.add("sounds.MixinEntitySnowman");
			mixins.add("sounds.MixinEntitySkeleton");
			mixins.add("sounds.MixinEntitySquid");
			mixins.add("sounds.MixinEntityWitch");
		}

		if (ConfigMixins.modernSkeletonBehavior) {
			mixins.add("skeleton.MixinEntitySkeleton");

			if (side == MixinEnvironment.Side.CLIENT) {
				mixins.add("skeleton.client.MixinEntityLivingBase");
				mixins.add("skeleton.client.MixinModelSkeleton");
			}
		}

		if (ConfigMixins.modernZombieBehavior) {
			mixins.add("zombie.MixinEntityZombie");
		}

		if (ConfigMixins.floorCeilingButtons) {
			mixins.add("floorceilbutton.MixinBlockButton");
		}

		if (ConfigMixins.newEnderEyeSounds) {
			mixins.add("sounds.MixinItemEnderEye");
			mixins.add("sounds.MixinEntityEnderEye");
		}

		if (ConfigMixins.newEnchantingSounds) {
			mixins.add("sounds.MixinContainerEnchantment");
		}

		if (ConfigMixins.newFishingRodSounds) {
			mixins.add("sounds.MixinItemFishingRod");
		}

		if (ConfigMixins.newBeaconSounds) {
			mixins.add("sounds.MixinTileEntityBeacon");
		}

		if (ConfigMixins.hoeTilling) {
			mixins.add("sounds.MixinItemHoe");
		}

		if (ConfigMixins.enableObservers) {
			mixins.add("observer.MixinWorld");
			mixins.add("observer.MixinWorldServer");
			mixins.add("observer.MixinChunk");
		}

		if (ConfigMixins.arrowFallingFix) {
			mixins.add("fallingarrowfix.MixinEntityArrow");
		}

		if (ConfigMixins.blockHopperInteraction) {
			mixins.add("blockinventories.MixinTileEntityHopper");
		}

		if (ConfigMixins.collidedThrowableFix) {
			mixins.add("projectilecollidefix.MixinEntityThrowable");
		}

		if (ConfigMixins.postTreeGenEvent && ConfigEntities.enableBees) {
			mixins.add("posttreegen.MixinWorldGenAbstractTree");
		}

		if (ConfigMixins.ladderTrapdoors) {
			mixins.add("laddertrapdoors.MixinBlockLadder");
			mixins.add("laddertrapdoors.MixinBlockTrapdoor");
		}

		if (ConfigMixins.betterPistons) {
			mixins.add("backinslime.MixinBlockPistonBase");
		}

		// Saved locked maps must remain locked even if new cartography tables are disabled.
		mixins.add("cartography.MixinMapData");
		mixins.add("cartography.MixinItemMap");
		if (ganymedes01.etfuturum.configuration.configs.ConfigExperiments.endDimensionProvider) mixins.add("endgateway.MixinEntityDragon");
		{
			mixins.add("nethermobs.MixinSpawnerAnimals");
			mixins.add("nethermobs.MixinWorldAquaticPopulation");
			mixins.add("nethermobs.MixinBiomeGenBase");
			mixins.add("nethermobs.MixinRiderInput");
			mixins.add("nethermobs.MixinNautilusBreath");
			mixins.add("nethermobs.MixinDolphinGrace");
			mixins.add("nethermobs.MixinMobDungeons");
			mixins.add("nethermobs.MixinUndergroundDungeons");
			mixins.add("nethermobs.MixinUndergroundLakes");
			mixins.add("nethermobs.MixinUndergroundOres");
			mixins.add("nethermobs.MixinUndergroundSprings");
			mixins.add("nethermobs.MixinUndergroundFlowers");
			mixins.add("nethermobs.MixinUndergroundOrientation");
			mixins.add("nethermobs.MixinChestLifecycle");
			mixins.add("archaeology.MixinDesertPyramid");
			mixins.add("archaeology.MixinDesertWell");
			mixins.add("archaeology.MixinStructureChest");
			mixins.add("archaeology.FallingBlockAccess");
			mixins.add("archaeology.MixinPotArrow");
			mixins.add("archaeology.MixinPotProjectile");
			if (side == MixinEnvironment.Side.CLIENT) {
				mixins.add("nethermobs.MixinWolfArmor");
				mixins.add("nethermobs.MixinArmorTrims");
				mixins.add("nethermobs.MixinTrimItemIcon");
				mixins.add("nethermobs.MixinCrossbowItemRenderer");
				mixins.add("nethermobs.MixinCrossbowModel");
			}
		}
		if (ganymedes01.etfuturum.configuration.configs.ConfigEnchantsPotions.enableSoulSpeed) mixins.add("soulspeed.MixinBlockSoulSand");
		if (ConfigMixins.soulFire) {
			mixins.add("soulfire.MixinBlockFire");
			if(loadedCoreMods.stream().anyMatch(name -> name.contains("rple"))) { //RPLE compat
				mixins.add("soulfire.MixinBlockFireRPLE");
			}
			mixins.add("soulfire.MixinEntity");
			if (side == MixinEnvironment.Side.CLIENT) {
				mixins.add("soulfire.MixinRenderBlocks");
			}
		}

		if (ConfigMixins.hideSingleLevelEnchants) {
			mixins.add("singlelevel.MixinEnchantment");
		}

		if (ConfigMixins.fireproofItems) {
			mixins.add("uninflammableitem.MixinEntityItem");
		}
        
    if (ConfigMixins.enableJumpClimbing)
    {
      mixins.add("isLadderFix.MixinEntityLivingBase");
    }
        

		if (ConfigMixins.adjustedLiquidPhysics) {
			mixins.add("liquidphysics.MixinEntity");
			mixins.add("liquidphysics.MixinWorld");
			mixins.add("liquidphysics.MixinBlockLiquid");
		}

		if (ConfigMixins.riddenHorsesInWater) {
			mixins.add("horsewater.MixinEntityHorse");
		}

		if (ConfigMixins.liquidItemFloat) {
			mixins.add("liquidphysics.MixinEntityItem");
		}

		if (false) { //Does not work for some reason, investigate in 2.6.1
			mixins.add("darkspawns.MixinEntityMob");
		}

		if (side == MixinEnvironment.Side.CLIENT) {
			if (ConfigMixins.dustUnderFallingBlocks) {
				mixins.add("blockfallingparticles.client.MixinBlockFalling");
			}

			if (ConfigMixins.adjustedAttenuation) {
				mixins.add("sounds.client.MixinSoundManager_Attenuation");
			}

			if (ConfigMixins.boundedBlockBreakingParticles) {
				mixins.add("boundedparticles.client.MixinEffectRenderer");
			}

			if (ConfigMixins.furnaceCrackle) {
				mixins.add("sounds.client.MixinBlockFurnace");
			}

			if (ConfigEnchantsPotions.enableSwiftSneak) {
				mixins.add("swiftsneak.client.MixinMovementInputFromOptions");
			}

			if (ConfigMixins.flowerPotFixes) {
				mixins.add("flowerpotfix.client.MixinRenderBlocks");
			}

			if (ConfigMixins.colorGrassBlockItemSides) {
				mixins.add("coloredgrassitem.client.MixinRenderBlocks");
			}

			if (ConfigMixins.worldSaveThumbnails) {
				mixins.add("worldthumbnail.client.MixinGuiSelectWorld");
				mixins.add("worldthumbnail.client.MixinGuiSelectWorldList");
			}

			if (ConfigMixins.modernLoadingScreen) {
				mixins.add("worldthumbnail.client.MixinLoadingScreenRenderer");
				mixins.add("worldthumbnail.client.MixinGuiDownloadTerrain");
				mixins.add("worldthumbnail.client.MixinMinecraft_LoadingBridge");
				mixins.add("worldthumbnail.MixinMinecraftServer");
				mixins.add("worldthumbnail.MixinChunkProviderServer");
				mixins.add("worldthumbnail.MixinChunkProviderGenerate");
				mixins.add("worldthumbnail.MixinChunkProviderHell");
				mixins.add("worldthumbnail.MixinChunkProviderEnd");
			}

			if(ConfigSounds.newBlockSounds) {
				mixins.add("sounds.client.MixinBlockStepSounds");
			}
			
			mixins.add("client.MixinRendererLivingEntity");
			mixins.add("items.MixinRenderBiped");
			mixins.add("items.MixinRenderPlayer");
		}

		if (ConfigMixins.thinPanes) {
			mixins.add("thinpanes.MixinBlockPane");
			if (side == MixinEnvironment.Side.CLIENT) {
				mixins.add("thinpanes.MixinRenderBlocks");
			}
		}

		if (ConfigMixins.enablePlayersSleepingPecentageGamerule) {
			mixins.add("playerssleepingpercentage.MixinWorldServer");
		}

		if (ConfigEntities.enableFoxes) {
			mixins.add("foxes.MixinEntityLivingBase");
			mixins.add("foxes.MixinEntityWolf");
		}

		if (ConfigEntities.enableGoats && side == MixinEnvironment.Side.CLIENT) {
			mixins.add("goats.client.MixinItemRenderer");
			mixins.add("goats.client.MixinModelBiped");
		}

		mixins.add("items.MixinItemSkull");
		if (side == MixinEnvironment.Side.CLIENT) {
			mixins.add("items.MixinItemSkullClient");
		}
		mixins.add("blocks.MixinTileEntitySkull");

		mixins.add("deepslateores.MixinChunk");

		return mixins;
	}

	@Override
	public String[] getASMTransformerClass() {
		return null;
	}

	@Override
	public String getModContainerClass() {
		return null;
	}

	@Override
	public String getSetupClass() {
		return null;
	}

	@Override
	public void injectData(Map<String, Object> data) {

	}

	@Override
	public String getAccessTransformerClass() {
		return null;
	}
}
