package ganymedes01.etfuturum.mixins.early.backlytra.client;

import ganymedes01.etfuturum.elytra.IElytraPlayer;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.EntityRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(EntityRenderer.class)
public class MixinEntityRenderer {
	@Shadow
	private Minecraft mc;

	@ModifyExpressionValue(method = "orientCamera", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/EntityLivingBase;yOffset:F"))
	private float elytraCameraOffset(float original) {
		if (this.mc.renderViewEntity instanceof IElytraPlayer
				&& ((IElytraPlayer) this.mc.renderViewEntity).etfu$isElytraFlying()
				&& !this.mc.renderViewEntity.isPlayerSleeping()) {
			// Camera placement must never change the physical anchor used by movement packets.
			return original + 1.62F - this.mc.renderViewEntity.getEyeHeight() - this.mc.renderViewEntity.ySize;
		}
		return original;
	}
}
