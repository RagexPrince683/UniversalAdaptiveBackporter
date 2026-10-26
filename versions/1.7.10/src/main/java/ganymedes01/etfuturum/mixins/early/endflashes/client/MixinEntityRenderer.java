package ganymedes01.etfuturum.mixins.early.endflashes.client;

import ganymedes01.etfuturum.client.EndFlashState;
import ganymedes01.etfuturum.client.ModernLightmap;
import ganymedes01.etfuturum.configuration.configs.ConfigWorld;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.EntityRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderer.class)
public abstract class MixinEntityRenderer {
	@Shadow private Minecraft mc;
	@Shadow private int[] lightmapColors;

	@Inject(method = "updateLightmap", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/texture/DynamicTexture;updateDynamicTexture()V"), require = 0)
	private void etfuturum$applyEndLight(float partialTicks, CallbackInfo ci) {
		if (mc.theWorld == null || !ModernLightmap.isEnd(mc.theWorld.provider)) return;
		float flash = ConfigWorld.endFlashes ? EndFlashState.INSTANCE.getIntensity(partialTicks) : 0.0F;
		if (!ConfigWorld.modernEndAmbientColor && flash <= 0.0F) return;
		for (int i = 0; i < lightmapColors.length; i++) {
			int color = lightmapColors[i];
			float r = (color >> 16 & 255) / 255.0F;
			float g = (color >> 8 & 255) / 255.0F;
			float b = (color & 255) / 255.0F;
			if (ConfigWorld.modernEndAmbientColor) {
				r = Math.max(r, ModernLightmap.END_AMBIENT[0]);
				g = Math.max(g, ModernLightmap.END_AMBIENT[1]);
				b = Math.max(b, ModernLightmap.END_AMBIENT[2]);
			}
			r += (172.0F / 255.0F - r) * flash;
			g += (96.0F / 255.0F - g) * flash;
			b += (205.0F / 255.0F - b) * flash;
			lightmapColors[i] = 0xFF000000 | (int) (ModernLightmap.clamp(r) * 255.0F) << 16
					| (int) (ModernLightmap.clamp(g) * 255.0F) << 8 | (int) (ModernLightmap.clamp(b) * 255.0F);
		}
	}
}
