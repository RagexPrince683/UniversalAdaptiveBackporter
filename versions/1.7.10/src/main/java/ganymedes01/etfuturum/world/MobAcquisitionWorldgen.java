package ganymedes01.etfuturum.world;

import cpw.mods.fml.common.IWorldGenerator;
import ganymedes01.etfuturum.configuration.configs.ConfigEntities;
import ganymedes01.etfuturum.entities.*;
import net.minecraft.util.MathHelper;
import net.minecraft.world.*;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraftforge.common.BiomeDictionary;
import java.util.Random;

/** Native acquisition in newly populated chunks; outpost pieces never request distant terrain. */
public final class MobAcquisitionWorldgen implements IWorldGenerator {
	@Override public void generate(Random random, int cx, int cz, World world, IChunkProvider generator, IChunkProvider provider) {
		if (!ConfigEntities.enableOutposts || world.provider.dimensionId != 0 || !world.getWorldInfo().isMapFeaturesEnabled()) return;
		OutpostStructures.populatePending(world, random, cx, cz);
		if (random.nextInt(600) != 0) return;
		int x = cx * 16 + 7, z = cz * 16 + 7;
		if (!BiomeDictionary.isBiomeOfType(world.getBiomeGenForCoords(x, z), BiomeDictionary.Type.PLAINS) && !BiomeDictionary.isBiomeOfType(world.getBiomeGenForCoords(x, z), BiomeDictionary.Type.SAVANNA) && !BiomeDictionary.isBiomeOfType(world.getBiomeGenForCoords(x, z), BiomeDictionary.Type.SANDY) && !BiomeDictionary.isBiomeOfType(world.getBiomeGenForCoords(x, z), BiomeDictionary.Type.FOREST)) return;
		int y = world.getTopSolidOrLiquidBlock(x, z);
		if (y < 60 || y > 230 || world.villageCollectionObj.findNearestVillage(x, y, z, 80) != null) return;
		OutpostStructures.tower(world, random, cx, cz);
	}
	public static void playerSpawns(net.minecraft.entity.player.EntityPlayer player) {
		World world = player.worldObj;
		if (world.provider.dimensionId != 0 || world.difficultySetting == EnumDifficulty.PEACEFUL || !world.getGameRules().getGameRuleBooleanValue("doMobSpawning") || player.capabilities.disableDamage) return;
		int x = MathHelper.floor_double(player.posX), z = MathHelper.floor_double(player.posZ);
		if (ConfigEntities.enableModernPassiveSpawning && world.getTotalWorldTime() > 24000 && world.rand.nextInt(10) == 0 && world.getEntitiesWithinAABB(EntityWanderingTrader.class, player.boundingBox.expand(96, 48, 96)).isEmpty()) {
			int px = x + 24 + world.rand.nextInt(17), pz = z + world.rand.nextInt(49) - 24;
			if (world.blockExists(px, 64, pz)) { int y = world.getTopSolidOrLiquidBlock(px, pz); EntityWanderingTrader trader = new EntityWanderingTrader(world); trader.setLocationAndAngles(px + .5, y, pz + .5, 0, 0); if (!world.isAnyLiquid(trader.boundingBox) && world.getCollidingBoundingBoxes(trader, trader.boundingBox).isEmpty() && world.spawnEntityInWorld(trader)) for (int i = 0; i < 2; i++) { EntityTraderLlama llama = new EntityTraderLlama(world); llama.setLocationAndAngles(px + i + .5, y, pz + 2.5, 0, 0); llama.onSpawnWithEgg(null); if (world.spawnEntityInWorld(llama)) llama.setLeashedToEntity(trader, true); } }
		}
		if (!ConfigEntities.enablePatrols || world.getTotalWorldTime() < 120000 || !world.isDaytime() || world.rand.nextInt(5) != 0 || !world.getEntitiesWithinAABB(EntityPillager.class, player.boundingBox.expand(96, 48, 96)).isEmpty()) return;
		int px = x + (world.rand.nextBoolean() ? 1 : -1) * (32 + world.rand.nextInt(16)), pz = z + world.rand.nextInt(65) - 32;
		if (!world.blockExists(px, 64, pz)) return;
		int y = world.getTopSolidOrLiquidBlock(px, pz);
		if (world.villageCollectionObj.findNearestVillage(px, y, pz, 32) != null) return;
		for (int i = 0; i < 3 + world.difficultySetting.getDifficultyId(); i++) { EntityPillager mob = new EntityPillager(world); mob.setLocationAndAngles(px + .5, y, pz + .5, 0, 0); if (world.isAnyLiquid(mob.boundingBox) || !world.getCollidingBoundingBoxes(mob, mob.boundingBox).isEmpty()) continue; mob.setCaptain(i == 0); mob.setHomeArea(x, MathHelper.floor_double(player.posY), z, 48); world.spawnEntityInWorld(mob); }
	}
}
