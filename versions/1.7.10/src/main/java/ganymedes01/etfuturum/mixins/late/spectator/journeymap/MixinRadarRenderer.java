package ganymedes01.etfuturum.mixins.late.spectator.journeymap;

import ganymedes01.etfuturum.api.spectator.SpectatorUtils;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** JourneyMap is optional and may not exist on the compile classpath, so its string target must be pseudo. */
@Pseudo
@Mixin(targets = "journeymap.client.render.map.RadarRenderer", remap = false)
public class MixinRadarRenderer {
	@Inject(method = "isEntityShown", at = @At("HEAD"), cancellable = true, remap = false)
	private void hideSpectators(EntityLivingBase entity, CallbackInfoReturnable<Boolean> cir) {
		if (entity instanceof EntityPlayer && SpectatorUtils.isSpectator(entity)) {
			cir.setReturnValue(false);
		}
	}
}
