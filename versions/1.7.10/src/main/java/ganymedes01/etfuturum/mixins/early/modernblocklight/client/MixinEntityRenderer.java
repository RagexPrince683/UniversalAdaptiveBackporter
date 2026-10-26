package ganymedes01.etfuturum.mixins.early.modernblocklight.client;

import ganymedes01.etfuturum.client.ModernLightmap;
import ganymedes01.etfuturum.configuration.configs.ConfigWorld;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.potion.Potion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderer.class)
public abstract class MixinEntityRenderer {
	@Shadow private Minecraft mc;
	@Shadow private int[] lightmapColors;
	@Shadow private float torchFlickerX;
	@Shadow protected abstract float getNightVisionBrightness(net.minecraft.entity.player.EntityPlayer player, float partialTicks);

	@Inject(method = "updateLightmap", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/texture/DynamicTexture;updateDynamicTexture()V"), require = 0)
	private void etfuturum$applyModernBlockLight(float partialTicks, CallbackInfo ci) {
		if (mc.theWorld == null || !ModernLightmap.supports(mc.theWorld.provider)) return;
		float nightVision = ConfigWorld.modernNightVision && mc.thePlayer.isPotionActive(Potion.nightVision)
				? getNightVisionBrightness(mc.thePlayer, partialTicks) : 0.0F;
		for (int i = 0; i < lightmapColors.length; i++) {
			int color = lightmapColors[i];
			float r = (color >> 16 & 255) / 255.0F;
			float g = (color >> 8 & 255) / 255.0F;
			float b = (color & 255) / 255.0F;
			if (ConfigWorld.modernBlockLightTint) {
				int blockLevel = i & 15;
				float legacy = mc.theWorld.provider.lightBrightnessTable[blockLevel] * (torchFlickerX * 0.1F + 1.5F);
				r += ModernLightmap.blockTint(blockLevel, 0, torchFlickerX) - legacy;
				g += ModernLightmap.blockTint(blockLevel, 1, torchFlickerX) - legacy * 0.6F;
				b += ModernLightmap.blockTint(blockLevel, 2, torchFlickerX) - legacy * 0.2F;
			}
			if (nightVision > 0.0F) {
				r = ModernLightmap.nightVisionFloor(r, 0, nightVision);
				g = ModernLightmap.nightVisionFloor(g, 1, nightVision);
				b = ModernLightmap.nightVisionFloor(b, 2, nightVision);
			}
			lightmapColors[i] = 0xFF000000 | (int) (ModernLightmap.clamp(r) * 255.0F) << 16
					| (int) (ModernLightmap.clamp(g) * 255.0F) << 8 | (int) (ModernLightmap.clamp(b) * 255.0F);
		}
	}
}
