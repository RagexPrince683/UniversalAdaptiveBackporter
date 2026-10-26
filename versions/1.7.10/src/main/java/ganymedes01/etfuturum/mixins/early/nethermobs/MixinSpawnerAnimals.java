package ganymedes01.etfuturum.mixins.early.nethermobs;

import ganymedes01.etfuturum.world.nether.NetherSpawning;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.world.SpawnerAnimals;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SpawnerAnimals.class)
public class MixinSpawnerAnimals {
	@Shadow private java.util.HashMap eligibleChunksForSpawning;
	@Redirect(method = "findChunksForSpawning", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/WorldServer;countEntities(Lnet/minecraft/entity/EnumCreatureType;Z)I", remap = false))
	private int etfu$indexedCount(net.minecraft.world.WorldServer world, EnumCreatureType type, boolean spawning) {
		if (!ganymedes01.etfuturum.world.AquaticPopulation.indexed(type)) return world.countEntities(type, spawning);
		ganymedes01.etfuturum.world.AquaticPopulation population = ganymedes01.etfuturum.world.AquaticPopulation.get(world);
		population.prepare(world, eligibleChunksForSpawning);
		return population.nativeCount(type, eligibleChunksForSpawning.size());
	}
	@Redirect(method = "findChunksForSpawning", at = @At(value = "INVOKE", target = "Lnet/minecraftforge/event/ForgeEventFactory;canEntitySpawn(Lnet/minecraft/entity/EntityLiving;Lnet/minecraft/world/World;FFF)Lcpw/mods/fml/common/eventhandler/Event$Result;", remap = false))
	private cpw.mods.fml.common.eventhandler.Event.Result etfu$boundedAdmission(net.minecraft.entity.EntityLiving entity, World world, float x, float y, float z) {
		cpw.mods.fml.common.eventhandler.Event.Result result = net.minecraftforge.event.ForgeEventFactory.canEntitySpawn(entity, world, x, y, z);
		if (!ganymedes01.etfuturum.world.AquaticPopulation.managed(entity)) return result;
		return ganymedes01.etfuturum.world.AquaticPopulation.get(world).admit(entity, result) ? cpw.mods.fml.common.eventhandler.Event.Result.ALLOW : cpw.mods.fml.common.eventhandler.Event.Result.DENY;
	}
	@Redirect(method = "findChunksForSpawning", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/WorldServer;spawnEntityInWorld(Lnet/minecraft/entity/Entity;)Z"))
	private boolean etfu$spawnResult(net.minecraft.world.WorldServer world, net.minecraft.entity.Entity entity) {
		boolean spawned = world.spawnEntityInWorld(entity);
		if (ganymedes01.etfuturum.world.AquaticPopulation.managed(entity)) ganymedes01.etfuturum.world.AquaticPopulation.get(world).spawned((net.minecraft.entity.EntityLiving) entity, spawned);
		return spawned;
	}
	@Inject(method = "func_151350_a", at = @At("HEAD"), cancellable = true)
	private static void etfu$loadedCandidate(World world, int x, int z, CallbackInfoReturnable<net.minecraft.world.ChunkPosition> cir) {
		if (!world.getChunkProvider().chunkExists(x, z)) cir.setReturnValue(new net.minecraft.world.ChunkPosition(x * 16, -1, z * 16));
	}
	@Inject(method = "canCreatureTypeSpawnAtLocation", at = @At("HEAD"), cancellable = true)
	private static void etfu$striderLava(EnumCreatureType type, World world, int x, int y, int z, CallbackInfoReturnable<Boolean> cir) {
		if (ganymedes01.etfuturum.world.MobSpawning.isAquatic(type)) {
			cir.setReturnValue(world.provider.dimensionId == 0 && world.blockExists(x, y - 1, z) && world.blockExists(x, y + 1, z)
					&& world.getBlock(x, y, z).getMaterial() == Material.water && world.getBlock(x, y - 1, z).getMaterial() == Material.water && !world.getBlock(x, y + 1, z).isNormalCube());
			return;
		}
		if (ganymedes01.etfuturum.world.structure.MonumentSpawning.GUARDIANS != null && type == ganymedes01.etfuturum.world.structure.MonumentSpawning.GUARDIANS) {
			cir.setReturnValue(world.getBlock(x, y, z).getMaterial() == Material.water && world.getBlock(x, y - 1, z).getMaterial() == Material.water
					&& !world.getBlock(x, y + 1, z).isNormalCube() && ganymedes01.etfuturum.world.structure.MapGenOceanMonument.contains(world, x, y, z));
		}
		if (NetherSpawning.STRIDERS != null && type == NetherSpawning.STRIDERS) cir.setReturnValue(world.provider.isHellWorld && world.getBlock(x, y, z).getMaterial() == Material.lava && !world.getBlock(x, y + 1, z).isNormalCube());
	}
}
