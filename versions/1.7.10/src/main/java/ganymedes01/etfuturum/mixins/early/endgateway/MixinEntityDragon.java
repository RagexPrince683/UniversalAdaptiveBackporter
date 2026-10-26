package ganymedes01.etfuturum.mixins.early.endgateway;

import ganymedes01.etfuturum.world.end.EndGatewayProgress;
import net.minecraft.entity.boss.EntityDragon;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityDragon.class)
public class MixinEntityDragon {
	@Inject(method = "onDeathUpdate", at = @At("TAIL"))
	private void etfu$gateway(CallbackInfo ci) {
		EntityDragon dragon = (EntityDragon) (Object) this;
		if (dragon.deathTicks == 200 && dragon.isDead) EndGatewayProgress.defeated(dragon);
	}
}
