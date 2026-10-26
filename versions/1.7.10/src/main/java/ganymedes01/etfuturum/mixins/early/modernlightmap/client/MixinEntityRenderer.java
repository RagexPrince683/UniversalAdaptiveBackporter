package ganymedes01.etfuturum.mixins.early.modernlightmap.client;

import ganymedes01.etfuturum.client.ModernLightmap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.EntityRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderer.class)
public abstract class MixinEntityRenderer {
	@Shadow private Minecraft mc;
	@Shadow private int[] lightmapColors;

	@Inject(method = "updateLightmap", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/texture/DynamicTexture;updateDynamicTexture()V"), require = 0)
	private void etfuturum$applyModernGamma(float partialTicks, CallbackInfo ci) {
		if (mc.theWorld == null || !ModernLightmap.supports(mc.theWorld.provider)) return;
		float gamma = mc.gameSettings.gammaSetting;
		if (gamma <= 0.0F) return;
		for (int i = 0; i < lightmapColors.length; i++) {
			int color = lightmapColors[i];
			float r = etfuturum$undoLegacyGamma((color >> 16 & 255) / 255.0F, gamma);
			float g = etfuturum$undoLegacyGamma((color >> 8 & 255) / 255.0F, gamma);
			float b = etfuturum$undoLegacyGamma((color & 255) / 255.0F, gamma);
			float brightest = Math.max(r, Math.max(g, b));
			float brightened = ModernLightmap.modernGamma(brightest, gamma);
			float scale = brightest > 0.00001F ? brightened / brightest : 1.0F;
			r *= scale;
			g *= scale;
			b *= scale;
			lightmapColors[i] = 0xFF000000 | (int) (ModernLightmap.clamp(r) * 255.0F) << 16
					| (int) (ModernLightmap.clamp(g) * 255.0F) << 8 | (int) (ModernLightmap.clamp(b) * 255.0F);
		}
	}

	@Unique
	private static float etfuturum$undoLegacyGamma(float result, float gamma) {
		float low = 0.0F;
		float high = 1.0F;
		for (int i = 0; i < 8; i++) {
			float middle = (low + high) * 0.5F;
			if (ModernLightmap.modernGamma(middle, gamma) < result) low = middle;
			else high = middle;
		}
		return (low + high) * 0.5F;
	}
}
