package ganymedes01.etfuturum.mixins.early.nethermobs;

import ganymedes01.etfuturum.world.AquaticPopulation;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(World.class)
public class MixinWorldAquaticPopulation {
	@Inject(method = "onEntityAdded", at = @At("RETURN")) private void etfu$added(Entity entity, CallbackInfo ci) { AquaticPopulation.added(entity); }
	@Inject(method = "onEntityRemoved", at = @At("RETURN")) private void etfu$removed(Entity entity, CallbackInfo ci) { AquaticPopulation.removed(entity); }
	@Inject(method = "updateEntityWithOptionalForce", at = @At("RETURN")) private void etfu$updated(Entity entity, boolean force, CallbackInfo ci) { AquaticPopulation.updated(entity); }
}
