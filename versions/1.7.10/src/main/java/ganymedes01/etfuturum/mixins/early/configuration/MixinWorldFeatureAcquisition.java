package ganymedes01.etfuturum.mixins.early.configuration;

import ganymedes01.etfuturum.configuration.ContentAvailability;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Chunk entity deserialization uses addLoadedEntities and deliberately bypasses this creation gate. */
@Mixin(World.class)
public class MixinWorldFeatureAcquisition {
	@Inject(method = "spawnEntityInWorld", at = @At("HEAD"), cancellable = true)
	private void uab$acquire(Entity entity, CallbackInfoReturnable<Boolean> callback) {
		if (entity != null && !((World) (Object) this).isRemote && !ContentAvailability.mobEnabled(entity.getClass())) callback.setReturnValue(false);
	}
}
